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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.unchecked.RuntimeSystemException;
import jp.ecuacion.lib.core.util.LogUtil;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.PropertyFileUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;

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
 * </ul>
 */
public class PropertyFileUtilValueGetter {

  // private DetailLogger detailLogger = new DetailLogger(this);

  private PropertyFileUtilFileKindEnum kind;

  /*
   * kindの中にfilePrefixを持っているので冗長な持ち方なのだが、
   * テストでPropertyFileUtilPropFileKindEnumにないfilePrefixを使用したい場合があるため別で持つ。
   * constructor以降ではkind.getPrefix()は使用しない。（使用したらテストでエラーになるのでまぁ検知可能）
   */
  private String filePrefix;

  private static final String[] LIB_MODULES = new String[] {"core", "jpa"};
  private static final String[] SPLIB_MODULES = new String[] {"web"};
  private static final String[] UTIL_MODULES = new String[] {"jpa", "poi"};

  private static final String[] APP_MODULES =
      new String[] {"", "base", "core", "core_web", "core_batch"};
  private static final String[] APP_ENVS = new String[] {"", "profile"};

  private static final List<String> dynamicPostfixList = new ArrayList<>();

  public static void addToDynamicPostfixList(String postfix) {
    if (!dynamicPostfixList.contains(postfix)) {
      dynamicPostfixList.add(postfix);
    }
  }

  /*
   * テスト用に同一パッケージからはアクセス可能とする
   */
  static List<String> getDynamicPostfixList() {
    return new ArrayList<>(dynamicPostfixList);
  }

  /**
   * Provides bundle name in the case that the application is executed with a Jigsaw module.
   * 
   * <p>In a java 9 module system, ResourceBundle.Control cannot be used.<br>
   *     https://docs.oracle.com/javase/jp/21/docs/api/java.base/java/util/ResourceBundle.html<br>
   *     {@code ResourceBundle.Control is designed for an application deployed in an unnamed module,
   *     for example to support resource bundles 
   *     in non-standard formats or package localized resources in a non-traditional convention. 
   *     ResourceBundleProvider is the replacement for ResourceBundle.Control 
   *     when migrating to modules. UnsupportedOperationException will be thrown 
   *     when a factory method that takes the ResourceBundle.Control parameter is called.}<br><br>
   * 
   *     https://www.morling.dev/blog/resource-bundle-lookups-in-modular-java-applications/
   * </p>
   */
  public static final ThreadLocal<String> bundleNameForModule = new ThreadLocal<>();

  /**
   * Stores a specified locale to control the candidate locales in java 9 module system.
   */
  public static final ThreadLocal<Locale> specifiedLocale = new ThreadLocal<>();

  /**
   * Constructs a new instance with {@code PropertyFileUtilPropFileKindEnum}.
   * 
   * @param fileKindEnum fileKindEnum
   */
  public PropertyFileUtilValueGetter(@RequireNonnull PropertyFileUtilFileKindEnum fileKindEnum) {

    this.kind = ObjectsUtil.paramRequireNonNull(fileKindEnum);
    this.filePrefix = fileKindEnum.getFilePrefix();
  }

  /*
   * テスト用。同じpackageからのみアクセス可。<br>
   * PropertyFileUtilとしては、PropFileKindEnumの値に対応するprefixにしか対応しないのだが、MultiLangPropStoreとしては
   * 特に制限なく受け入れられる仕様とする。でないとテストがやりにくい・・・
   */
  PropertyFileUtilValueGetter(@RequireNonnull String filePrefix) {
    Objects.requireNonNull(filePrefix);
    this.kind = PropertyFileUtilFileKindEnum.MSG;
    this.filePrefix = filePrefix;
  }

  /**
   * postfix（messages_ などファイル種別を示す文字列の後ろにつける、プロジェクトごとのIDを示す文字列）の一覧を取得。
   * 
   * <p>テストを考慮し同一パッケージからのアクセスは可とする。</p>
   * 
   * @return postfix list
   */
  List<String> getPostfixes() {
    List<String> rtnList = new ArrayList<>();
    rtnList.addAll(
        Arrays.asList(LIB_MODULES).stream().map(str -> "lib_" + str).collect(Collectors.toList()));
    rtnList.addAll(Arrays.asList(SPLIB_MODULES).stream().map(str -> "splib_" + str)
        .collect(Collectors.toList()));
    rtnList.addAll(Arrays.asList(UTIL_MODULES).stream().map(str -> "util_" + str).toList());
    rtnList.addAll(dynamicPostfixList);

    // SYSTEM_MODULESは、SYSTEM_ENVと組み合わせになる
    for (String systemModuleName : APP_MODULES) {
      for (String envName : APP_ENVS) {
        // envNameが""でない場合は、_を追加してappend。
        boolean needsUs = !systemModuleName.equals("") && !envName.equals("");
        rtnList.add(systemModuleName + ((needsUs) ? "_" : "") + envName);
      }
    }

    return rtnList;
  }

  /*
   * 指定のlocaleに対し、postfix × candidate locales 分のbundleを取得しそこから値を取得。
   * 
   * <p>キーの重複定義チェックもここで行う。</p>
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   */
  @Nonnull
  private String readPropFile(@Nullable Locale locale, @RequireNonnull String key) {
    ObjectsUtil.paramRequireNonNull(key);

    List<String> postfixes = getPostfixes();
    Map<String, ResourceBundle> rbMapForModule = new HashMap<>();

    // 初回のみResourceBundleデータを取得
    for (int i = 0; i < postfixes.size(); i++) {
      String postfix = postfixes.get(i);
      String filename = filePrefix + ((postfix.equals("")) ? "" : "_") + postfix;

      ResourceBundle bundle = getResourceBundle(filename, locale);
      rbMapForModule.put(filename, bundle);

      // msgの場合は追加でファイル読み込み
      if (kind == PropertyFileUtilFileKindEnum.MSG) {
        filename = "ValidationMessages";
        rbMapForModule.put(filename, getResourceBundle(filename, locale));
      }
    }

    // 複数bundle間でのkey重複チェック
    String messageString = null;
    for (Entry<String, ResourceBundle> entry : rbMapForModule.entrySet()) {
      if (entry.getValue() != null && entry.getValue().containsKey(key)) {
        if (messageString != null) {
          throw new RuntimeSystemException("Key '" + key + "' in properties file duplicated. ");
        }

        messageString = entry.getValue().getString(key);
      }
    }

    if (messageString == null) {
      return null;
    }

    // analyzes messageString
    List<Pair<PropertyFileUtilFileKindEnum, String>> list = analyze(messageString);
    StringBuilder sb = new StringBuilder();

    for (Pair<PropertyFileUtilFileKindEnum, String> tuple : list) {
      if (tuple.getLeft() == null) {
        sb.append(tuple.getRight());

      } else {
        sb.append(PropertyFileUtil.get(tuple.getLeft().getFilePrefix(), tuple.getRight()));
      }
    }

    return sb.toString();
  }

  /**
   * Reads a property file and returns its {@code ResourceBundle}.<br>
   * Returns {@code null} when a resource bundle is not found.
   * 
   * @param bundleId resource bundle's bundle ID
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   */
  @Nonnull
  private ResourceBundle getResourceBundle(@RequireNonnull String bundleId,
      @Nullable Locale locale) {

    ObjectsUtil.paramRequireNonNull(bundleId);

    if (locale == null) {
      locale = Locale.ROOT;
    }

    // java 9 module system
    try {
      bundleNameForModule.set(bundleId);
      specifiedLocale.set(locale);

      String bundle = "jp.ecuacion.lib.core." + new StringUtil()
          .getUpperCamelFromSnakeOrNullIfInputIsNull(bundleId.replaceAll("-", "_"));
      return ResourceBundle.getBundle(bundle, locale);

    } catch (MissingResourceException ex) {
      // do nothing.
    }

    // non-module apps
    try {

      return ResourceBundle.getBundle(bundleId, locale,
          ResourceBundle.Control.getNoFallbackControl(Control.FORMAT_PROPERTIES));

    } catch (MissingResourceException | UnsupportedOperationException e) {
      // do nothing.
    }

    return null;
  }

  /**
   * Analyzes messages with the constructure of {@code message ID in message}.
   * 
   * <p>When there's no message ID in a message, return will be {(null, {@code <message>})}.<br>
   *     left hand side of a pair is {@code null} meeans 
   *     that the message doesn't have a message ID in it.</p>
   * 
   * <p>.When one message ID is included in a message return will be 
   *     {(null, {@code prefixOfMessage}),  ({@code PropertyFileUtilFileKindEnum.MSG, message ID}), 
   *     (null, {@code postfixOfMessage})}.<br>
   *     3 parts of a message will be concatenated into a single string, 
   *     and the middle part wlll be translated into a message.<br><br>
   *     For example, when the message is {@code Hello, {messages:human}!}, 
   *     the analyzed result is: <br>
   *     ({@code (null, "Hello, "), (PropertyFileUtilFileKindEnum.MSG, "human"), (null, "!")}}.</p>
   * 
   * @param string string
   * @return {@code List<Pair<PropertyFileUtilFileKindEnum, String>>}
   */
  private List<Pair<PropertyFileUtilFileKindEnum, String>> analyze(String string) {
    final String startBracket = "{";
    final String endBracket = "}";
    List<Pair<PropertyFileUtilFileKindEnum, String>> list = new ArrayList<>();

    String stringLeft = string;

    while (true) {
      int indexOfStartBracket = stringLeft.indexOf(startBracket);
      int indexOfEndBracket = stringLeft.indexOf(endBracket);

      if (indexOfStartBracket == -1) {
        list.add(Pair.of(null, stringLeft));
        break;
      }

      // the code below is executed when stringLeft contains the startBracket.

      if (indexOfStartBracket > indexOfEndBracket) {
        throw new RuntimeSystemException(
            "startBracketFollowsEndBracket. the brackets in the string is somehow wrong. string: "
                + stringLeft);
      }

      // front simple string part before startBracket
      list.add(Pair.of(null, stringLeft.substring(0, indexOfStartBracket)));

      String stringInBrackets =
          stringLeft.substring(indexOfStartBracket + startBracket.length(), indexOfEndBracket);
      PropertyFileUtilFileKindEnum fileKind = getFileKindFromStringInBrackets(stringInBrackets);

      if (fileKind == null) {
        list.add(Pair.of(null,
            stringLeft.substring(indexOfStartBracket, indexOfEndBracket + endBracket.length())));

      } else {
        list.add(Pair.of(fileKind, stringInBrackets.split(":")[1]));
      }

      stringLeft = stringLeft.substring(indexOfEndBracket + endBracket.length());
    }

    return list;
  }

  private PropertyFileUtilFileKindEnum getFileKindFromStringInBrackets(String stringInBrackets) {
    if (!stringInBrackets.contains(":")) {
      return null;
    }

    return PropertyFileUtilFileKindEnum.getEnumFromFilePrefix(stringInBrackets.split(":")[0]);
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
