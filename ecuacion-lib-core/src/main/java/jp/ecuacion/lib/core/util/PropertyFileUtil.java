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
package jp.ecuacion.lib.core.util;

import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilPropFileKindEnum.APP;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilPropFileKindEnum.ENUM_NAME;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilPropFileKindEnum.FIELD_NAME;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilPropFileKindEnum.MSG;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.text.MessageFormat;
import java.util.Locale;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.util.internal.PropertyFileUtilKeyGetterByFileKind;

/**
 * Provides utility methods to read {@code *.properties} files.
 * 
 * <p>It has following functions added to {@code ResourceBundle} class packaged in JRE.</p>
 * 
 * <ol>
 * <li>To read all the ".properties" files in library modules 
 *     and multiple modules in app projects</li>
 * <li>To read multiple kinds of ".properties" 
 *     ({@code application, messages, enum_names, field_names})</li>
 * <li>To use "default" message by putting the postfix of the message ID ".default"</li>
 * <li>To have the override function by java launch parameter (-D) or System.setProperty(...) </li>
 * <li>To have the default locale setting function by .properties file
 *     (application_for_property-file-util_base.properties)</li>
 * </ol>
 * <br>
 * 
 * <p><b>1. To read all the ".properties" files in library modules
 *     and multiple modules in app projects</b><br><br>
 *     If we talk about {@code messages[_xxx].properties}, 
 *     this class reads ones in ecuacion libraries, and ones in your apps.<br>
 *     In ecuacion libraries an app is assumed to devided to some modules
 *     (=usually called "projects" in IDE), 
 *     which are {@code base}, {@code core}, {@code web (or none)}, {@code batch}.<br><br>
 *     If the name of your app is {@code sample-app}, module names would be :<br>
 *     {@code sample-app-base : messages_base.properties}<br>
 *     {@code sample-app-core : messages_core.properties}<br>
 *     {@code sample-app-web  : messages.properties}<br>
 *     {@code sample-app-batch: messages.properties}<br><br>
 *     
 *     {@code PropertyFileUtil.getMsg(...)} will read all the messages properties above.<br>
 *     Duplicated definition detectable. (causes throwing exception)<br><br>
 *     And of course you can use localized files like {@code messages_core_ja.properties}
 *     because This class uses {@code ResourceBundle} inside to read properties files.
 * </p>
 * <br>
 * 
 * <p><b>2. To read multiple kinds of ".properties" 
 *     (application, messages, enum_names, field_names)</b><br><br>
 *     Firstly, In {@code ecuacion-lib} we have 4 kinds of property files.<br><br>
 *     
 *     {@code PropertyFileUtil.getMsg(...) : messages[_xxx].properties}<br>
 *     {@code PropertyFileUtil.getApp(...) : application[_xxx].properties}<br>
 *     {@code PropertyFileUtil.getEnumName(...) : enum_names[_xxx].properties}<br>
 *     {@code PropertyFileUtil.getFieldName(...) : fiels_names[_xxx].properties}<br><br>
 *     
 *     {@code messages.properties} and {@code application.properties} are well-known.<br>
 *     {@code enum_names.properties} stores the localized name of the enum element, and
 *     {@code field_names.properties} stores the localized name of the entity field.<br>
 *     Usually these are also stored in {@code messages.properties},
 *     but it's kind of messy so divided files are prepared.<br><br>
 *     
 *     {@code PropertyFileUtil} supports these 4 kinds of properties files.
 * </p>
 * <table border="1">
 * <caption>kinds of property files</caption>
 * <tr>
 * <th>kind</th>
 * <th>data the file has</th>
 * </tr>
 * <tr>
 * <td>application</td>
 * <td>system settings</td>
 * </tr>
 * <tr>
 * <td>messages</td>
 * <td>messages</td>
 * </tr>
 * <tr>
 * <td>field_names</td>
 * <td>names of the fields of items</td>
 * </tr>
 * <tr>
 * <td>enum_names</td>
 * <td>names of the elements of enums</td>
 * </tr>
 * </table>
 * <br>
 * 
 * <p><b>3. To use "default" message by putting the postfix of the message ID ".default"</b><br><br>
 * </p>
 * 
 * <p><b>4. To Have the override function by java launch parameter (-D) 
 *     or System.setProperty(...)</b><br><br>
 * </p>
 * 
 * <p><b>5. To Have the default locale setting function by .properties file
 *     (application_for_property-file-util_base.properties)</b><br><br>
 *     
 *     When you don't prepare the properties file, 
 *     values are obtained from properties files without locale specification 
 *     (like {@code message.properties}, not like {@code messages_en.properties}).<br>
 *     {@code ResourceBundle} uses {@code Locale.getDefault()} but this is not good
 *     because the resulting locale depends on the PC or the server the program is executing.<br>
 *     Of course when you want to depends on the PC or the server, 
 *     you just have to explicitly pass {@code Locale.getDefault()} to it.
 * </p>
 * 
 * <p><b>Miscellaneous</b><br><br>
 * {@code messages[_xxx].properties}, {@code enum_names[_xxx].properties}, 
 * {@code fiels_names[_xxx].properties} need to have default locale file 
 * (like {@code messages.properties}. This is the rule of the library.<br>
 * It leads the conclusion that {@code hasXxx(...) (like hasMsg(...))} 
 * doesn't need to have {@code locale} argument. (default locale used)
 * </p>
 */
public class PropertyFileUtil {

  // propertiesファイルの種類ごとに、その複数言語分のメッセージを格納する入れ物を定義
  private static PropertyFileUtilKeyGetterByFileKind appPropStore =
      new PropertyFileUtilKeyGetterByFileKind(APP);
  private static PropertyFileUtilKeyGetterByFileKind svrMsgStore =
      new PropertyFileUtilKeyGetterByFileKind(MSG);
  private static PropertyFileUtilKeyGetterByFileKind fieldNamesStore =
      new PropertyFileUtilKeyGetterByFileKind(FIELD_NAME);
  private static PropertyFileUtilKeyGetterByFileKind enumNamesStore =
      new PropertyFileUtilKeyGetterByFileKind(ENUM_NAME);

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

  /** Does not construct an instance.  */
  private PropertyFileUtil() {}

  
  // ■□■ application ■□■

  /**
   * Returns the value in application_xxx.properties.
   * 
   * <p>Names should exist but some function uses this 
   * to show message ID when the key does not exist in the file.</p>
   * 
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getApp(@RequireNonnull String key) {
    return appPropStore.getProp(key);
  }


  /**
   * Returns the existence of the key in application_xxx.properties.
   * 
   * <p>Names should exist but some function uses this 
   * to show message ID when the key does not exist in the file.</p>
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasApp(@RequireNonnull String key) {
    return appPropStore.hasProp(key);
  }

  /**
   * Returns the value in application_xxx.properties.
   * 
   * <p>Names should exist but some function uses this 
   * to show message ID when the key does not exist in the file.</p>
   * 
   * @param key the key of the property
   * @param args message arguments, 
   *     may be {@code null} which is treated as <code>new String[] {}</code>.
   * @return the message corresponding to the message ID
   */
  @Nonnull
  public static String getMsg(@RequireNonnull String key, @RequireNonnull String... args) {
    return getMsg(null, key, args);
  }

  /**
   * Returns the value in messages_xxx.properties.
   * 
   * <p>Names should exist but some function uses this 
   * to show message ID when the key does not exist in the file.</p>
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   * @param args message arguments, 
   *     may be {@code null} which is treated as <code>new String[] {}</code>.
   * @return the message corresponding to the message ID
   */
  @Nonnull
  public static String getMsg(@Nullable Locale locale, @RequireNonnull String key,
      @RequireNonnull String... args) {

    String msgStr = svrMsgStore.getProp(locale, key);

    // データパターンにより処理を分岐
    return (args.length == 0) ? msgStr : MessageFormat.format(msgStr, (Object[]) args);
  }

  /**
   * Returns the existence of the key in field_names_xxx.properties.
   * 
   * <p>Names should exist but some function uses this 
   * to show message ID when the key does not exist in the file.</p>
   * 
   * @param msgId message ID
   * @return boolean value that shows whether properties has the message ID
   */
  public static boolean hasMsg(@RequireNonnull String msgId) {
    return svrMsgStore.hasProp(msgId);
  }

  // ■□■ field_name ■□■

  /**
   * Returns the field name of default locale in field_names_xxx.properties.
   * 
   * <p>Names should exist but some function uses this 
   * to show message ID when the key does not exist in the file.</p>
   * 
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getFieldName(@RequireNonnull String key) {
    return fieldNamesStore.getProp(null, key);
  }

  /**
   * Returns the localized field name in field_names_xxx.properties.
   * 
   * <p>Names should exist but some function uses this 
   * to show message ID when the key does not exist in the file.</p>
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getFieldName(@Nullable Locale locale, @RequireNonnull String key) {
    return fieldNamesStore.getProp(locale, key);
  }

  /**
   * Returns the existence of the key in field_names_xxx.properties.
   * 
   * <p>Names should exist but some function uses this 
   * to show message ID when the key does not exist in the file.</p>
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasFieldName(@RequireNonnull String key) {
    return fieldNamesStore.hasProp(key);
  }

  // ■□■ enum_name ■□■

  /**
   * Returns the enum name of default locale in enum_names_xxx.properties.
   * 
   * <p>Names should exist but some function uses this 
   * to show message ID when the key does not exist in the file.</p>
   * 
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getEnumName(@RequireNonnull String key) {
    return enumNamesStore.getProp(null, key);
  }

  /**
   * Returns the localized enum name in enum_names_xxx.properties.
   * 
   * <p>Names should exist but some function uses this 
   * to show message ID when the key does not exist in the file.</p>
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getEnumName(@Nullable Locale locale, @RequireNonnull String key) {
    return enumNamesStore.getProp(locale, key);
  }

  /**
   * Returns the existence of the key in enam_names_xxx.properties.
   * 
   * <p>Names should exist but some function uses this 
   * to show message ID when the key does not exist in the file.</p>
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasEnumName(@RequireNonnull String key) {
    return enumNamesStore.hasProp(key);
  }
}
