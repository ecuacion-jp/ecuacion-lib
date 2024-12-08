/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ecuacion.lib.core.util.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.util.LogUtil;

/**
 * ファイルから抽出したプロパティを保持するクラス。
 * 
 * <ul>
 * <li>ひとつのMultiLangPropStoreインスタンスで、PropFileKindEnumのひとつの項目（例えばmessages）に対する複数locale分のデータを保持する<br>
 * （複数モジュールのファイルの情報かつそれらに対するlocale別の情報まで保持）</li>
 * <li>内部に持つMap（propMap）は、以下の型（Map&lt;Locale, Map&lt;String, String&gt;&gt;）でできており、
 * localeをkeyとして指定すると、その言語に対応したMap&lt;String, String&gt;が取得される。<br>
 * そのMap&lt;String, String&gt;には、個別メッセージのkeyとvalueのペアが格納される。</li>
 * <li>既に同一のキーが設定されている場合はエラーとする。<br>
 * これはつまり、例えばapplication_fw_cmnとapplicationで同じキーを書いたらエラーとする仕様。<br>
 * エラーではなく既存値をoverrideする仕様にするか迷ったが、overrideするくらいならそもそも定義すべきでないと思われるし、
 * 想定しないキーの重複が発生したことにより不正な挙動になるのも避けたいためエラーを選択。もし必要なら変更する。<br>
 * </li>
 * <li>TODO: default言語がen, カスタマイズされた言語がjaのみの場合に、例えばitのlocaleでアクセスすると、
 * itのlocaleをキーとして、enと同じメッセージ情報を格納してしまう仕様。<br>
 * 理想的ではないので、必要時に修正するべし。</li>
 * </ul>
 */
public class PropertyFileUtilKeyGetterByFileKind {

  private DetailLogger detailLogger;

  private PropertyFileUtilPropFileKindEnum kind;

  /*
   * kindの中にfilePrefixを持っているので冗長な持ち方なのだが、
   * テストでPropertyFileUtilPropFileKindEnumにないfilePrefixを使用したい場合があるため別で持つ。
   * constructor以降ではkind.getPrefix()は使用しない。（使用したらテストでエラーになるのでまぁ検知可能）
   */
  private String filePrefix;

  /*
   * application.propertiesはlocale別の情報を保持しないが、messages.propertiesなどと同様に
   * localeをキーとした本クラスに格納されるため、何らかlocaleが必要。<br>
   * default_localeでも良いのだが、途中で変更される可能性もあるので、固定用のlocaleを定義しておく。<br>
   * （変更されても、同じentryが2つできるだけで動作に問題が出るわけではないが、テスト上も固定の方が楽）
   */
  private static final Locale LOCALE_FOR_LOCALE_UNNEEDED_PROPERTIES = Locale.JAPANESE;

  /*
   * テスト用に変更可能とするため、同一パッケージからはアクセス可能としておく。 テスト以外では変更不可。
   */
  static String bundleForPropertyFileUtil = "application_for_property-file-util_base";

  private static final String[] ECUACION_LIB_MODULES = new String[] {"core", "jpa"};

  private static final String[] ECUACION_LIB_JAKARTAEE_MODULES = new String[] {"batch", "core",
      "jpa_eclipselink", "web", "web_jaxrs_client", "web_jaxrs_jersey", "web_jaxrs_client_jersey"};

  private static final String[] ECUACION_SPLIB_MODULES = new String[] {"web"};

  private static final String[] ECUACION_UTIL_TOOLS_MODULES = new String[] {"jpa", "poi"};

  private static final String[] SYSTEM_MODULES =
      new String[] {"", "base", "core", "core_web", "core_batch"};

  private static final String[] SYSTEM_ENVS = new String[] {"", "profile"};

  private static final String[] OTHERS = new String[] {"ValidationMessages", "test"};

  /*
   * PropFileKindEnum毎に、対象のファイルリストが存在する。 そのファイルリストをコンストラクタ呼び出し時に受け取る
   */
  public PropertyFileUtilKeyGetterByFileKind(PropertyFileUtilPropFileKindEnum fileKindEnum) {
    Objects.requireNonNull(fileKindEnum);
    this.kind = fileKindEnum;
    this.filePrefix = fileKindEnum.getFilePrefix();
    onConstruct();
  }

  /*
   * テスト用。同じpackageからのみアクセス可。<br>
   * PropertyFileUtilとしては、PropFileKindEnumの値に対応するprefixにしか対応しないのだが、MultiLangPropStoreとしては
   * 特に制限なく受け入れられる仕様とする。でないとテストがやりにくい・・・
   */
  PropertyFileUtilKeyGetterByFileKind(String filePrefix) {
    Objects.requireNonNull(filePrefix);
    this.kind = PropertyFileUtilPropFileKindEnum.MSG;
    this.filePrefix = filePrefix;
    onConstruct();
  }

  private void onConstruct() {
    // PropertyFileUtilが使用するためのpropertiesを普通に定義すると、呼び出し時に無限ループが起きてしまう。
    // そのため、PropertyFileUtilが使用するのみを目的としたproperty-file-util_base.propertiesというファイルを
    // baseの中に作って、そこに定義をする。このファイルを読む際は、通常のResourceBundleを使用する。
    String strDefaultLocale = null;
    ResourceBundle bundle = null;

    detailLogger = new DetailLogger(this);

    try {
      bundle = ResourceBundle.getBundle(bundleForPropertyFileUtil);
      strDefaultLocale = bundle.getString("DEFAULT_LOCALE");

    } catch (MissingResourceException e) {
      // 存在しない場合も可なので何もしない
    }

    if (strDefaultLocale != null && !strDefaultLocale.equals("")) {
      String[] args = strDefaultLocale.split("_");
      if (args.length == 1) {
        Locale.setDefault(Locale.of(strDefaultLocale));

      } else {
        Locale.setDefault(Locale.of(args[0], args[1]));
      }

    } else {
      detailLogger.trace("DEFAULT_LOCALE not defined. System.getDefault() is used.");
    }
  }

  private List<String> getFileNamePostfixes() {
    List<String> rtnList = new ArrayList<>();
    // ECUACION_LIB_MODULESは、頭に「lib_」を追加
    rtnList.addAll(Arrays.asList(ECUACION_LIB_MODULES).stream().map(str -> "lib_" + str)
        .collect(Collectors.toList()));
    // ECUACION_LIB_JAKARTAEE_MODULESは、頭に「jeelib_」を追加
    rtnList.addAll(Arrays.asList(ECUACION_LIB_JAKARTAEE_MODULES).stream()
        .map(str -> "jeelib_" + str).collect(Collectors.toList()));
    // ECUACION_SPLIB_MODULESは、頭に「lib_splib」を追加
    rtnList.addAll(Arrays.asList(ECUACION_SPLIB_MODULES).stream().map(str -> "splib_" + str)
        .collect(Collectors.toList()));
    rtnList.addAll(
        Arrays.asList(ECUACION_UTIL_TOOLS_MODULES).stream().map(str -> "util_" + str).toList());

    rtnList.addAll(Arrays.asList(OTHERS));
    // SYSTEM_MODULESは、SYSTEM_ENVと組み合わせになる
    for (String systemModuleName : SYSTEM_MODULES) {
      for (String envName : SYSTEM_ENVS) {
        // envNameが""でない場合は、_を追加してappend。
        boolean needsUs = !systemModuleName.equals("") && !envName.equals("");
        rtnList.add(systemModuleName + ((needsUs) ? "_" : "") + envName);
      }
    }

    return rtnList;
  }

  /*
   * 指定のlocaleに対してResourceBundleを丸ごと取得し、それをtableに突っ込む。
   */
  private String readPropFile(Locale locale, String key) {
    String value = null;

    List<String> postfixes = getFileNamePostfixes();
    // データを取得
    for (int i = 0; i < postfixes.size(); i++) {
      String postfix = postfixes.get(i);
      String filename = filePrefix + ((postfix.equals("")) ? "" : "_") + postfix;

      String tmpValue = readOnePropFile(filename, locale, key, value != null);
      if (tmpValue != null) {
        value = tmpValue;
      }
    }

    // msgの場合は追加でファイル読み込み
    if (value == null && kind == PropertyFileUtilPropFileKindEnum.MSG) {
      value = readOnePropFile("ValidationMessages", locale, key, false);
    }

    return value;
  }

  private String readOnePropFile(String filename, Locale locale, String key,
      boolean valueObtained) {
    ResourceBundle bundle = null;

    Objects.requireNonNull(filename);
    Objects.requireNonNull(locale);

    try {
      bundle = ResourceBundle.getBundle(filename, locale);

    } catch (MissingResourceException e) {
      // 例えばapplication.propertiesに対して、fw_cmn, fw_web, base, ...の全てのモジュールに対するpropertiesファイルが
      // 存在する必要はないので、探しに行ったプロパティファイルがなくても見逃す
      detailLogger.trace("(not a problem) jp.ecuacion.lib.core.util.internal.PropertyFileUtil: "
          + "property file not exist（" + filename + "）");

      // 本処理は終了
      return null;
    }

    if (!bundle.containsKey(key)) {
      return null;
    }

    String val = bundle.getString(key);
    if (val != null && valueObtained) {
      // 既に同一のキーが設定されている場合はエラー
      throw new RuntimeException("Duplicate key in Property File.[file="
          + bundle.getBaseBundleName() + ", key=" + key + "]");
    }

    return val;
  }

  /*
  * プロパティファイルのシリーズごとに、キーが存在するかどうかを確認する。 application.propertiesなどlocale別ファイルが存在しないものはこちらを使用。
  */
  public boolean hasProp(String key) {
    return hasProp(LOCALE_FOR_LOCALE_UNNEEDED_PROPERTIES, key);
  }

  /*
  * プロパティファイルのシリーズごとに、キーが存在するかどうかを確認する。
  */
  public boolean hasProp(Locale locale, String key) {
    Objects.requireNonNull(locale);
    Objects.requireNonNull(key);

    String val = readPropFile(locale, key);

    return val != null;
  }

  /*
   * プロパティファイルのシリーズごとに、キーに対する値を取得する。 application.propertiesなどlocale別ファイルが存在しないものはこちらを使用。
   */
  public String getProp(String key) {
    return getProp(LOCALE_FOR_LOCALE_UNNEEDED_PROPERTIES, key);
  }

  /*
   * プロパティファイルのシリーズごとに、キーに対する値を取得する。
   */
  public String getProp(Locale locale, String key) {
    Objects.requireNonNull(locale);
    Objects.requireNonNull(key);

    // 値を取得
    String str = readPropFile(locale, key);
    // 頭に"default."がつくdefault設定のmessageも検索
    String defaultStr = readPropFile(locale, key + ".default");

    // キーが存在しなかったらエラーとする。エラーが出るのが怖かったらあらかじめhasPropをすること。
    if (str == null && defaultStr == null) {
      // メッセージが取得できないときにまたメッセージ取得を必要とする処理（＝AppCheckRuntimeExceptionの生成）をすると無限ループになるので、
      // 失敗したときはログ出力のみとしておく
      try {
        throw new RuntimeException("No key in .properties. key: " + key);

      } catch (Exception e) {
        new LogUtil(this).logError(e, (String) null);
        throw e;
      }
    }

    // keyを渡して値を返す
    return str != null ? str : defaultStr;
  }

  /* 結局一行で書けるのだがちょっとwrapして書く量を減らした^^;。 */
  public boolean isOverrided(String key) {
    return System.getProperties().keySet().contains(key);
  }
}
