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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.unchecked.RuntimeSystemException;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.util.LogUtil;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.PropertyFileUtil;
import jp.ecuacion.lib.core.util.StringUtil;

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

  /**
   * Constructs a new instance with {@code PropertyFileUtilPropFileKindEnum}.
   * 
   * @param fileKindEnum fileKindEnum
   */
  public PropertyFileUtilKeyGetterByFileKind(
      @RequireNonnull PropertyFileUtilPropFileKindEnum fileKindEnum) {

    this.kind = ObjectsUtil.paramRequireNonNull(fileKindEnum);
    this.filePrefix = fileKindEnum.getFilePrefix();
    onConstruct();
  }

  /*
   * テスト用。同じpackageからのみアクセス可。<br>
   * PropertyFileUtilとしては、PropFileKindEnumの値に対応するprefixにしか対応しないのだが、MultiLangPropStoreとしては
   * 特に制限なく受け入れられる仕様とする。でないとテストがやりにくい・・・
   */
  PropertyFileUtilKeyGetterByFileKind(@RequireNonnull String filePrefix) {
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

  private Map<String, ResourceBundle> rbMapForModule;

  /*
   * 指定のlocaleに対してResourceBundleを丸ごと取得し、それをtableに突っ込む。
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   */
  @Nonnull
  private String readPropFile(@Nullable Locale locale, @RequireNonnull String key) {
    List<String> postfixes = getFileNamePostfixes();

    // 初回のみResourceBundleデータを取得
    if (rbMapForModule == null) {
      rbMapForModule = new HashMap<>();

      for (int i = 0; i < postfixes.size(); i++) {
        String postfix = postfixes.get(i);
        String filename = filePrefix + ((postfix.equals("")) ? "" : "_") + postfix;

        ResourceBundle bundle = getOneResourceBundle(filePrefix, postfix, locale, key);
        rbMapForModule.put(filename, bundle);

        // msgの場合は追加でファイル読み込み
        if (kind == PropertyFileUtilPropFileKindEnum.MSG) {
          filename = "ValidationMessages";
          rbMapForModule.put(filename, getOneResourceBundle(filePrefix, postfix, locale, key));
        }
      }

      // 複数bundle間でのkey重複チェック
      Set<String> duplicateCheckMap = new HashSet<>();
      for (Entry<String, ResourceBundle> entry : rbMapForModule.entrySet()) {
        if (entry.getValue() != null) {
          for (String keyInBundle : entry.getValue().keySet()) {
            if (duplicateCheckMap.contains(keyInBundle)) {
              throw new RuntimeSystemException(
                  "Key '" + keyInBundle + "' in properties file duplicated. ");
            }
            duplicateCheckMap.add(keyInBundle);
          }
        }
        rbMapForModule.put(entry.getKey(), entry.getValue());
      }
    }

    for (Entry<String, ResourceBundle> entry : rbMapForModule.entrySet()) {
      if (entry.getValue() == null || !entry.getValue().containsKey(key)) {
        continue;

      } else {
        return entry.getValue().getString(key);
      }
    }

    return null;
  }

  /**
   * Reads a property file and returns the value to the key.
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   */
  @Nonnull
  private ResourceBundle getOneResourceBundle(@RequireNonnull String filePrefix,
      @RequireNonnull String postfix, @Nullable Locale locale, @RequireNonnull String key) {

    String filename = filePrefix + ((postfix.equals("")) ? "" : "_") + postfix;

    ObjectsUtil.paramRequireNonNull(filename);
    ObjectsUtil.paramRequireNonNull(key);

    ResourceBundle bundle = null;

    if (locale == null) {
      locale = Locale.ROOT;
    }

    // java 9 module system
    try {
      PropertyFileUtil.bundleNameForModule.set(filename);
      PropertyFileUtil.specifiedLocale.set(locale);

      ResourceBundle.clearCache();
      bundle = ResourceBundle.getBundle(
          "jp.ecuacion.lib.core.Messages" + new StringUtil()
              .getUpperCamelFromSnakeOrNullIfInputIsNull(postfix.replace("-", "_")),
          // Locale.of(locale.getLanguage(), locale.getCountry(), postfix));
          locale);

    } catch (MissingResourceException ex) {
      // do nothing.
    }

    // non-module apps
    if (bundle == null) {
      try {

        if (locale == null) {
          locale = Locale.ROOT;
        }

        /*
         * ファイルの探し順を指定。
         * 
         * <p>自localeのファイル（例えばLocale.JAPANを指定している場合はmessages_ja.properties）
         * がない場合、ResourceBundleのdefaultではLocale.getDefault()で探しに行ってしまうが、
         * それだと実行PC / server環境に依存して結果が変わってしまうためよくない。
         * （もちろん、Locale.getDefault()を使用したい場合は、明示的にそれを設定すれば可能）</p>
         * 
         * <p>messages_ja.propertiesが存在しない場合は次にmessages.propertiesを探す、
         * それもなければmessages_en.propertiesを探す、という順番に変更する。</p>
         * 
         * <p>java 9 module systemでは使用不可。
         */
        bundle = ResourceBundle.getBundle(filename, locale,
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));

      } catch (MissingResourceException | UnsupportedOperationException e) {
        // 例えばapplication.propertiesに対して、fw_cmn, fw_web, base, ...の全てのモジュールに対するpropertiesファイルが
        // 存在する必要はないので、探しに行ったプロパティファイルがなくても見逃す
        detailLogger.trace("(not a problem) jp.ecuacion.lib.core.util.internal.PropertyFileUtil: "
            + "property file not exist（" + filename + "）");

        // 本処理は終了
        return null;
      }
    }

    return bundle;
  }

  /*
  * プロパティファイルのシリーズごとに、キーが存在するかどうかを確認する。
  */
  public boolean hasProp(String key) {
    Objects.requireNonNull(key);

    String val = readPropFile(null, key);

    return val != null;
  }

  /*
   * プロパティファイルのシリーズごとに、キーに対する値を取得する。 application.propertiesなどlocale別ファイルが存在しないものはこちらを使用。
   */
  public String getProp(String key) {
    return getProp(null, key);
  }

  /*
   * プロパティファイルのシリーズごとに、キーに対する値を取得する。
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   */
  public String getProp(@Nullable Locale locale, @RequireNonnull String key) {
    ObjectsUtil.paramRequireNonNull(key);

    // msgIdが空だったらエラー
    if (key.equals("")) {
      throw new RuntimeSystemException("Message ID is blank.");
    }

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
  public boolean isOverrided(@RequireNonnull String key) {
    return System.getProperties().keySet().contains(key);
  }
}
