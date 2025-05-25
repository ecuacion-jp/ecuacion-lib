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
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.util.EmbeddedParameterUtil;
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

  private boolean throwsExceptionWhenKeyDoesNotExist;
  /*
   * kindの中にfilePrefixを持っているので冗長な持ち方なのだが、
   * テストでPropertyFileUtilPropFileKindEnumにないfilePrefixを使用したい場合があるため別で持つ。
   * constructor以降ではkind.getPrefix()は使用しない。（使用したらテストでエラーになるのでまぁ検知可能）
   */
  private String[][] filePrefixes;

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
    this.filePrefixes = ObjectsUtil.requireNonNull(fileKindEnum).getActualFilePrefixes();
    this.throwsExceptionWhenKeyDoesNotExist = fileKindEnum.throwsExceptionWhenKeyDoesNotExist();
  }

  /*
   * テスト用。同じpackageからのみアクセス可。<br>
   * PropertyFileUtilとしては、PropFileKindEnumの値に対応するprefixにしか対応しないのだが、MultiLangPropStoreとしては
   * 特に制限なく受け入れられる仕様とする。でないとテストがやりにくい・・・
   */
  PropertyFileUtilValueGetter(@RequireNonnull String[][] filePrefixes) {
    this.filePrefixes = ObjectsUtil.requireNonNull(filePrefixes);
    throwsExceptionWhenKeyDoesNotExist = true;
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
   * 指定のlocaleに対し、postfix 分のbundleを取得しそこから値を取得。
   * 
   * <p>キーの重複定義チェックもここで行う。</p>
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   */
  @Nonnull
  private String getValue(@Nullable Locale locale, @RequireNonnull String key) {
    ObjectsUtil.requireNonNull(key);

    String value = null;

    if (System.getProperties().keySet().contains(key)) {
      // If the key is in System.getProperties(), just return it.
      value = System.getProperties().getProperty(key);

    } else {
      value = getValueFromPropertiesFiles(locale, key);
    }

    // analyzes messageString
    List<Pair<String, String>> list = analyze(value);
    StringBuilder sb = new StringBuilder();

    for (Pair<String, String> tuple : list) {
      if (tuple.getLeft() == null) {
        sb.append(tuple.getRight());

      } else {
        sb.append(PropertyFileUtil.get(tuple.getLeft(), tuple.getRight()));
      }
    }

    return sb.toString();
  }

  private String getValueFromPropertiesFiles(Locale locale, String key) {
    for (String[] filePrefixesOfSamePriority : filePrefixes) {
      String value =
          getValueFromPropertiesFilesWithSamePriority(locale, key, filePrefixesOfSamePriority);

      if (value != null) {
        return value;
      }
    }

    // The program reaches here means key not exist in properties files.
    // メッセージが取得できないときにまたメッセージ取得を必要とする処理（＝AppCheckRuntimeExceptionの生成）をすると無限ループになる場合があるので、
    // 失敗したときはRuntimeExceptionとしておく
    throw new NoKeyInPropertiesFileException(key);
  }

  /*
   * Obtains value from key and locale by reading multiple properties files 
   *     with prefixes and postfixes of the filename.
   * 
   * <p>キーの重複定義チェックもここで行う。</p>
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   */
  private String getValueFromPropertiesFilesWithSamePriority(Locale locale, String key,
      String[] filePrefixesOfSamePriority) {
    // Search the key in properties files.
    List<String> postfixes = getPostfixes();
    Map<String, ResourceBundle> rbMap = new HashMap<>();

    for (String prefix : filePrefixesOfSamePriority) {
      for (int i = 0; i < postfixes.size(); i++) {
        String postfix = postfixes.get(i);
        String filename = prefix + ((postfix.equals("")) ? "" : "_") + postfix;

        ResourceBundle bundle = getResourceBundle(filename, locale);
        rbMap.put(filename, bundle);
      }
    }

    String valueNonDefault = getValueAndDuplicationCheck(rbMap, key);
    String valueDefault = getValueAndDuplicationCheck(rbMap, key + ".default");

    return valueNonDefault == null ? valueDefault : valueNonDefault;
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

    ObjectsUtil.requireNonNull(bundleId);

    if (locale == null) {
      locale = Locale.ROOT;
    }

    // java 9 module system
    try {
      bundleNameForModule.set(bundleId);
      specifiedLocale.set(locale);

      String bundle = "jp.ecuacion.lib.core."
          + StringUtil.getUpperCamelFromSnake(bundleId.replaceAll("-", "_"));
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

  private String getValueAndDuplicationCheck(Map<String, ResourceBundle> resourceBundleMap,
      String key) {
    String messageString = null;
    for (Entry<String, ResourceBundle> entry : resourceBundleMap.entrySet()) {
      if (entry.getValue() != null && entry.getValue().containsKey(key)) {
        if (messageString != null) {
          throw new KeyDupliccatedException(key);
        }

        messageString = entry.getValue().getString(key);
      }
    }

    return messageString;
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
  private List<Pair<String, String>> analyze(String string) {
    List<String> startSymbols = Arrays.asList(PropertyFileUtilFileKindEnum.values()).stream()
        .map(en -> "{+" + en.toString().toLowerCase() + ":").toList();

    // properties files are not managed by users
    // so exceptions occurring while analyzing string are changed to unchecked exceptions.
    try {
      List<Pair<String, String>> list = EmbeddedParameterUtil.getPartList(string,
          startSymbols.toArray(new String[startSymbols.size()]), "}");

      // left of the pair starts with "{+" and ends with ":" but they're not needed
      return list.stream()
          .map(pair -> pair.getLeft() == null ? pair
              : Pair.of(pair.getLeft().substring(2, pair.getLeft().length() - 1), pair.getRight()))
          .toList();

    } catch (AppException ex) {
      throw new EclibRuntimeException(ex);
    }
  }

  /*
  * プロパティファイルのシリーズごとに、キーが存在するかどうかを確認する。
  */
  public boolean hasProp(String key) {
    Objects.requireNonNull(key);

    try {
      getValue(null, key);
      return true;

    } catch (NoKeyInPropertiesFileException ex) {
      return false;
    }
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
  @Nonnull
  public String getProp(@Nullable Locale locale, @RequireNonnull String key) {
    ObjectsUtil.requireNonNull(key);

    // msgIdが空だったらエラー
    if (key.equals("")) {
      throw new EclibRuntimeException("Message ID is blank.");
    }

    try {
      return getValue(locale, key);

    } catch (NoKeyInPropertiesFileException ex) {
      if (throwsExceptionWhenKeyDoesNotExist) {
        throw ex;

      } else {
        return "[ " + key + " ]";
      }
    }
  }

  public static class NoKeyInPropertiesFileException extends EclibRuntimeException {

    private static final long serialVersionUID = 1L;

    public NoKeyInPropertiesFileException(String key) {
      super("No key in .properties. key: " + key);
    }
  }

  public static class KeyDupliccatedException extends EclibRuntimeException {

    private static final long serialVersionUID = 1L;

    public KeyDupliccatedException(String key) {
      super("Duplicated key in .properties. key: " + key);
    }
  }
}
