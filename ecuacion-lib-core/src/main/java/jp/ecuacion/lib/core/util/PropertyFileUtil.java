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

import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.APP;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.ENUM_NAME;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.FIELD_NAME;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.MSG;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum;
import jp.ecuacion.lib.core.util.internal.PropertyFileUtilValueGetter;

/**
 * Provides utility methods to read {@code *.properties} files.
 * 
 * <p>It has following features added to {@code ResourceBundle} class packaged in JRE.</p>
 * 
 * <ol>
 * <li>To read all the ".properties" files in library modules 
 *     and multiple modules in projects of an app.</li>
 * <li>To read multiple kinds of ".properties" 
 *     ({@code application, messages, enum_names, field_names})</li>
 * <li>To remove default locale from candidate locales</li>
 * <li>To use "default" message by putting the postfix of the message ID ".default"</li>
 * <li>To have the override function by java launch parameter (-D) or System.setProperty(...) </li>
 * <li>To resolve property keys in the obtained value</li>
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
 * <p><b>3. To remove default locale from candidate locales</b><br><br>
 *     Java Standard {@code ResourceBundle} uses default locale 
 *     (which is obtained by {@code Locale.getDefault()}) 
 *     when the property file of specified locale is not found.<br>
 *     The default locale is usually equal to the locale of the OS,
 *     which means the result depends on the machine the program is executed on.<br><br>
 *     To avoid that situation deault locale is removed from candidate locales with this class.
 * </p>
 * <br>
 * 
 * <p><b>4. To use "default" message by putting the postfix of the message ID ".default"</b><br><br>
 * </p>
 * 
 * <p><b>5. To Have the override function by java launch parameter (-D) 
 *     or System.setProperty(...)</b><br><br>
 * </p>
 * 
 * <p><b>6. To resolve property keys in the obtained value</b><br><br>
 *     You can put a property key into a property value.<br>
 *     For example, you can define keys and values like this in {@code messages.properties}. 
 *     By executing {@code PropertyFileUtil.getMsg("message")} you'll get {@code "a-b-c"}.</p>
 * <pre>
 *     message=a-${messages:message_test1}-c
 *     message_test1=b</pre>
 * 
 * <p>Recursive resolution is also supported so you can even define like the one below. <br>
 * By executing {@code PropertyFileUtil.getMsg("message")} you'll get {@code "a-b-c-d-e-f-g"}.</p>
 * 
 * <pre>
 *     message=a-${messages:message_test1}-c-${messages:message_test2}-g
 *     message_test1=b
 *     message_test2=d-${messages:message_test3}-f
 *     message_test3=e</pre>
 * 
 * <p>Examples above uses {@code ${messages:...}} but you can also use other file kinds 
 * like {@code ${application:...}, ${field_names:...} and ${enum_names:...}}.</p>
 *     
 * <p>Recursive resolution is supported, but multiple layer of key is not supported. 
 *     (which does not seem to be needed really)</p>
 *     <pre>
 *     message=a-${messages:${messages:message_prefix}_test1}-c
 *     message_prefix=message
 *     message_test1=b</pre><br>
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

  private static Map<PropertyFileUtilFileKindEnum, PropertyFileUtilValueGetter> getterMap =
      new HashMap<>();

  static {
    getterMap.put(APP, new PropertyFileUtilValueGetter(APP));
    getterMap.put(MSG, new PropertyFileUtilValueGetter(MSG));
    getterMap.put(FIELD_NAME, new PropertyFileUtilValueGetter(FIELD_NAME));
    getterMap.put(ENUM_NAME, new PropertyFileUtilValueGetter(ENUM_NAME));
  }

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
    return getterMap.get(APP).getProp(key);
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
    return getterMap.get(APP).hasProp(key);
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

    String msgStr = getterMap.get(MSG).getProp(locale, key);

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
    return getterMap.get(MSG).hasProp(msgId);
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
    return getterMap.get(FIELD_NAME).getProp(null, key);
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
    return getterMap.get(FIELD_NAME).getProp(locale, key);
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
    return getterMap.get(FIELD_NAME).hasProp(key);
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
    return getterMap.get(ENUM_NAME).getProp(null, key);
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
    return getterMap.get(ENUM_NAME).getProp(locale, key);
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
    return getterMap.get(ENUM_NAME).hasProp(key);
  }

  // ■□■ abstract property ■□■

  /**
   * Returns the property value of default locale.
   * 
   * @param propertyUtilFileKind String value of propertyUtilFileKind (application, messages, ...)
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String get(@RequireNonnull String propertyUtilFileKind,
      @RequireNonnull String key) {
    return getterMap.get(PropertyFileUtilFileKindEnum.getEnumFromFilePrefix(propertyUtilFileKind))
        .getProp(null, key);
  }

  /**
   * Returns the property value of default locale.
   * 
   * @param propertyUtilFileKind String value of propertyUtilFileKind (application, messages, ...)
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String get(@RequireNonnull String propertyUtilFileKind, @Nullable Locale locale,
      @RequireNonnull String key) {
    return getterMap.get(PropertyFileUtilFileKindEnum.getEnumFromFilePrefix(propertyUtilFileKind))
        .getProp(locale, key);
  }

  /**
   * Returns the existence of the key in enam_names_xxx.properties.
   * 
   * @param propertyUtilFileKind String value of propertyUtilFileKind (application, messages, ...)
   * @param key the key of the property
   * @return the value of the property
   */
  public static boolean has(@RequireNonnull String propertyUtilFileKind,
      @RequireNonnull String key) {
    return getterMap.get(PropertyFileUtilFileKindEnum.getEnumFromFilePrefix(propertyUtilFileKind))
        .hasProp(key);
  }

  /**
   * Adds postfix dinamically.
   * 
   * <p>If you add {@code test} for example, 
   *     {@code messages_test[_xxx].properties, 
   *     application_test[_xxx}.properties, ...} are searched.</p>
   * 
   * @param postfix postfix
   */
  public static void addResourceBundlePostfix(String postfix) {
    PropertyFileUtilValueGetter.addToDynamicPostfixList(postfix);
  }
}
