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

import static jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum.APPLICATION;
import static jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum.ENUM_NAMES;
import static jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum.ITEM_NAMES;
import static jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum.MESSAGES;
import static jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum.MESSAGES_WITH_ITEM_NAMES;
import static jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum.STRINGS;
import static jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum.VALIDATION_MESSAGES;
import static jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum.VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS;
import static jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum.VALIDATION_MESSAGES_WITH_ITEM_NAMES;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.el.ELProcessor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean.FieldInfoBean;
import jp.ecuacion.lib.core.util.EmbeddedParameterUtil.Options;
import jp.ecuacion.lib.core.util.EmbeddedParameterUtil.StringFormatIncorrectException;
import jp.ecuacion.lib.core.util.internal.PropertyFileUtilValueGetter;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Provides utility methods to read {@code *.properties} files.
 * 
 * <p>It has following features added to {@code ResourceBundle} class packaged in JRE.</p>
 * 
 * <ol>
 * <li>To read multiple kinds of {@code *.properties} files</li>
 * <li>To read all the {@code *.properties} files in ecuacion modules 
 *     and multiple modules of an app</li>
 * <li>To remove default locale from candidate locales</li>
 * <li>To avoid throwing an exception exen if a message key does not exist</li>
 * <li>To use "default" value by putting ".default" postfix to the key</li>
 * <li>To have the override function by java launch parameter (-D) or System.setProperty(...) </li>
 * <li>To resolve property keys in the obtained value</li>
 * <li>To resolve property keys in arguments</li>
 * <li>To resolve EL expression</li>
 * </ol>
 * <br>
 * 
 * <p><b>1. To read multiple kinds of {@code *.properties} files</b><br><br>
 *     It treats {@code *.properties} files below.<br>
 *     Localized ones take {@code Locale} as an argument, 
 *     and parameterized ones does {@code String[]} or {@code Arg[]}. 
 *     (Details for {@code Arg} are in section 8.)</p>
 * 
 * <table border="1">
 *   <caption>kinds of *.properties files</caption>
 *   <tr>
 *     <th>file name</th>
 *     <th>method to obtain values from files</th>
 *     <th>localized</th>
 *     <th>parameterized</th>
 *     <th>description</th>
 *   </tr>
 *   <tr>
 *     <td>application[_xxx].properties</td>
 *     <td>getApplication(...)<br>
 *     <td style="text-align: center"></td>
 *     <td style="text-align: center"></td>
 *     <td>Treats app settings</td>
 *   </tr>
 *   <tr>
 *     <td>messages[_xxx].properties</td>
 *     <td>getMessage(...)<br>
 *     <td style="text-align: center">x</td>
 *     <td style="text-align: center">x️</td>
 *     <td>Treats localized messages</td>
 *   </tr>
 *   <tr>
 *     <td>strings[_xxx].properties</td>
 *     <td>getString(...)<br>
 *     <td style="text-align: center"></td>
 *     <td style="text-align: center">x️</td>
 *     <td>Treats non-localized messages</td>
 *   </tr>
 *   <tr>
 *     <td>item_names[_xxx].properties</td>
 *     <td>getItemName(...)<br>
 *     <td style="text-align: center">☑x</td>
 *     <td style="text-align: center"></td>
 *     <td>Treats enum item names</td>
 *   </tr>
 *   <tr>
 *     <td>enum_names[_xxx].properties</td>
 *     <td>getEnumName(...)<br>
 *     <td style="text-align: center">☑x</td>
 *     <td style="text-align: center"></td>
 *     <td>Treats enum value names</td>
 *   </tr>
 *   <tr>
 *     <td>ValidationMessages[_xxx].properties</td>
 *     <td>getValidationMessage(...)<br>
 *     <td style="text-align: center">☑x</td>
 *     <td style="text-align: center"></td>
 *     <td>Treats jakarta validation messages, 
 *         but it's never called from apps. It's used only from ecuacion-modules. 
 *         No item names in them.</td>
 *   </tr>
 *   <tr>
 *     <td>ValidationMessagesWithItemNames[_xxx].properties</td>
 *     <td>getValidationMessageWithItemName(...)<br>
 *     <td style="text-align: center">☑x</td>
 *     <td style="text-align: center"></td>
 *     <td>Treats jakarta validation messages, 
 *         but it's never called from apps. It's used only from ecuacion-modules. 
 *         Item names in them.</td>
 *   </tr>
 *   <tr>
 *     <td>ValidationMessagesPatternDescriptions[_xxx].properties</td>
 *     <td>getValidationMessagePatternDescription(...)<br>
 *     <td style="text-align: center">☑x</td>
 *     <td style="text-align: center"></td>
 *     <td>Treats pattern expressing localized strings used for jakarta validation messages, 
 *         but it's never called from apps. It's used only from ecuacion-modules. </td>
 *   </tr>
 * </table>
 *
 * <br>
 * 
 * <p><b>2. To read all the {@code *.properties} files in ecuacion modules 
 *     and multiple modules of an app</b><br><br>
 *     When we talk about {@code messages[_xxx].properties}, 
 *     this class reads ones in ecuacion modules 
 *     (like {@code messages_lib_core.properties} in {@code ecuacion-lib-core}), 
 *     and ones in your apps.<br>
 *     In ecuacion modules an app is assumed to divided into some modules
 *     (=usually called "projects" in IDE), 
 *     which are {@code base}, {@code core}, {@code web (or none)}, {@code batch}.<br><br>
 *     If the name of your app is {@code sample-app}, module names would be :<br>
 *     {@code sample-app-base : messages_base.properties}<br>
 *     {@code sample-app-core : messages_core.properties}<br>
 *     {@code sample-app-web  : messages.properties}<br>
 *     {@code sample-app-batch: messages.properties}<br><br>
 *     
 *     {@code PropertyFileUtil.getMessage(...)} will read all the properties above.<br>
 *     Duplicated definition detectable. (causes exception)<br><br>
 *     And of course you can use localized files like {@code messages_core_ja.properties}
 *     for localized files (see the table above) 
 *     because this class uses {@code ResourceBundle} inside to read them.
 * </p>
 * <br>
 * 
 * <p><b>3. To remove default locale from candidate locales</b><br><br>
 *     Java Standard {@code ResourceBundle} uses default locale 
 *     (which is obtained by {@code Locale.getDefault()}) 
 *     when the property file of specified locale is not found.<br>
 *     The default locale is usually equal to the locale of the OS 
 *     (of the server usually in web application environment),
 *     which means the result depends on the machine the program is executed on.<br><br>
 *     To avoid that situation deault locale is removed 
 *     from candidate locales with this class.<br><br>
 *     Note that it uses default locale 
 *     when you don't specify locale to get a string from localized *.properties files.
 * </p>
 * <br>
 * 
 * <p><b>4. To avoid throwing an exception exen if a message key does not exist</b><br><br>
 *     Since {@code application.properties} has settings, exception should be thrown 
 *     when a required key does not exist.<br>
 *     On the other hand, since {@code messages.properties} has messages only 
 *     and even if it's shown on the screen, it's weird but not very fatal,
 *     and furthermore it's better when developing because developers can see clearly
 *     which messages are not defined
 *     (System error screen has no concrete information),
 *     so exception should not be thrown and just show the message key with [ ].<br><br>
 *     This feature offers shown key on screen with non-application properties.</p>
 * <br>
 * 
 * <p><b>5. To use "default" value by putting ".default" postfix to the key</b><br><br>
 *     It's kind of troublesome that you have to create {@code *.properties}
 *     for pre-defined keys in ecuacion-modules.<br>
 *     But on the other hand, it's better for app developers to be able to change it
 *     when it has to be.
 *     So the keys in {@code *.properties} files contained in ecuacion modules
 *     have ".default" postfix and those can be overrided in app modules
 *     by defining the key in app {@code *.properties} files without ".default".
 * </p>
 * 
 * <p><b>6. To Have the override function by java launch parameter (-D) 
 *     or System.setProperty(...)</b><br><br>
 *     You can override values with those settings. 
 *     (It's implemented for {@code application.properties})
 * </p>
 * 
 * <p><b>7. To resolve property keys in the obtained value</b><br><br>
 *     You can put a property key into a property value.<br>
 *     For example, you can define keys and values like this in {@code messages.properties}. 
 *     By executing {@code PropertyFileUtil.getMessage("message")} you'll get {@code "a-b-c"}.</p>
 * <pre>
 *     message=a-${+messages:message_test1}-c
 *     message_test1=b</pre>
 * 
 * <p>Recursive resolution is also supported so you can even define like the one below. <br>
 * By executing {@code PropertyFileUtil.getMessage("message")} 
 * you'll get {@code "a-b-c-d-e-f-g"}.</p>
 * 
 * <pre>
 *     message=a-${+messages:message_test1}-c-${+messages:message_test2}-g
 *     message_test1=b
 *     message_test2=d-${messages:message_test3}-f
 *     message_test3=e</pre>
 * 
 * <p>Examples above uses {@code {+messages:...}} but you can also use other file kinds 
 * like {@code {+application:...}, {+item_names:...} and {+enum_names:...}}.</p>
 *     
 * <p>Recursive resolution is supported, but multiple layer of key is not supported. 
 *     (which does not seem to be needed)</p>
 *     <pre>
 *     message=a-${+messages:${+messages:message_prefix}_test1}-c
 *     message_prefix=message
 *     message_test1=b</pre>
 * <br>
 * 
 * <p><b>8. To resolve property keys in arguments</b><br><br>
 *     Sometimes you want to put not a static string, but a dynamic one, 
 *     and maybe localized one as a parameter 
 *     of {@code messages.properties} or {@code strings.properties}.<br><br>
 *     You can realize it by {@code Arg} instead of {@code String} as a parameter class.
 * </p>
 * <br>
 * 
 * <p><b>9. To resolve EL expression</b><br><br>
 *     EL expression is supported. (Since jakarta validation does.)<br>
 *     You can define it like this.</p>
 *     <pre>math.addition=1 + 1 = ${1 + 1}.</pre>
 * <br>
 * 
 * <p><b>Miscellaneous</b><br><br>
 * <ul>
 * <li>{@code messages[_xxx].properties}, {@code enum_names[_xxx].properties}, 
 * {@code fiels_names[_xxx].properties} need to have default locale file 
 * (like {@code messages.properties}. This is the rule of the library.<br>
 * It leads the conclusion that {@code hasXxx(...) (like hasMsg(...))} 
 * doesn't need to have {@code locale} argument. (default locale used)
 * </li>
 * </ul>
 */
public class PropertyFileUtil {

  private static Map<PropertyFileUtilFileKindEnum, PropertyFileUtilValueGetter> getterMap =
      new HashMap<>();

  static {
    for (PropertyFileUtilFileKindEnum anEnum : PropertyFileUtilFileKindEnum.values()) {
      getterMap.put(anEnum, new PropertyFileUtilValueGetter(anEnum));
    }
  }

  /**
   * Prevents other classes from instantiating it.
   */
  private PropertyFileUtil() {}

  // ■□■ application ■□■

  /**
   * Returns the value in application_xxx.properties.
   * 
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getApplication(@RequireNonnull String key) {
    return getterMap.get(APPLICATION).getProp(key, null);
  }


  /**
   * Returns the existence of the key in application_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasApplication(@RequireNonnull String key) {
    return getterMap.get(APPLICATION).hasProp(key);
  }

  /**
   * Returns the value in application_xxx.properties if it exists. 
   * Returns given default value if not.
   * 
   * @param key the key of the property
   * @param defaultValue default value
   * @return the value of the property
   */
  @Nullable
  public static String getApplicationOrElse(@RequireNonnull String key, String defaultValue) {
    if (getterMap.get(APPLICATION).hasProp(key)) {
      return getterMap.get(APPLICATION).getProp(key, null);

    } else {
      return defaultValue;
    }
  }

  // ■□■ message ■□■

  /**
   * Returns the value of default locale in messages_xxx.properties.
   * 
   * @param key the key of the property
   * @param args message arguments
   * @return the value (message) of the property key (message ID)
   */
  @Nonnull
  public static String getMessage(@RequireNonnull String key, @RequireNonnull String... args) {
    return getMessage(null, key, args);
  }

  /**
   * Returns the localized value in messages_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   * @param args message arguments
   * @return the value (message) of the property key (message ID)
   */
  @Nonnull
  public static String getMessage(@Nullable Locale locale, @RequireNonnull String key,
      @RequireNonnull String... args) {
    return formatMessage(getterMap.get(MESSAGES).getProp(locale, key, null), args);
  }

  /**
   * Returns the value of default locale in messages_xxx.properties.
   * 
   * @param key the key of the property
   * @param args message arguments, which can be message ID.
   *     The data type is {@code Arg[]}, not {@code Arg...} 
   *     because if {@code Arg} causes an error when you call {@code getMsg(key)}
   *     since the second parameter is unclear ({@code String...} or {@code Arg...}.
   * @return the value (message) of the property key (message ID)
   */
  @Nonnull
  public static String getMessage(@RequireNonnull String key, @RequireNonnull Arg[] args) {
    return getMessage(null, key, args);
  }

  /**
   * Returns the localized value in messages_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   * @param args message arguments, which can be message ID.
   *     The data type is {@code Arg[]}, not {@code Arg...} 
   *     because if {@code Arg} causes an error when you call {@code getMsg(key)}
   *     since the second parameter is unclear ({@code String...} or {@code Arg...}.
   * @return the message corresponding to the message ID
   */
  @Nonnull
  public static String getMessage(@Nullable Locale locale, @RequireNonnull String key,
      @RequireNonnull Arg[] args) {
    return getMessage(locale, key, getStringsFromArgs(locale, args));
  }

  /**
   * Returns the existence of the key in messages_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key (message ID)
   */
  public static boolean hasMessage(@RequireNonnull String key) {
    return getterMap.get(MESSAGES).hasProp(key);
  }

  // ■□■ messageWithItemName ■□■

  /**
   * Returns the value of default locale in messagesWithItemNames_xxx.properties.
   * 
   * @param key the key of the property
   * @param args message arguments
   * @return the value (message) of the property key (message ID)
   */
  @Nonnull
  public static String getMessageWithItemName(@RequireNonnull String key,
      @RequireNonnull String... args) {
    return getMessageWithItemName(null, key, args);
  }

  /**
   * Returns the localized value in messagesWithItemNames_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   * @param args message arguments
   * @return the value (message) of the property key (message ID)
   */
  @Nonnull
  public static String getMessageWithItemName(@Nullable Locale locale, @RequireNonnull String key,
      @RequireNonnull String... args) {
    return formatMessage(getterMap.get(MESSAGES_WITH_ITEM_NAMES).getProp(locale, key, null), args);
  }

  /**
   * Returns the value of default locale in messagesWithItemNames_xxx.properties.
   * 
   * @param key the key of the property
   * @param args message arguments, which can be message ID.
   *     The data type is {@code Arg[]}, not {@code Arg...} 
   *     because if {@code Arg} causes an error when you call {@code getMsg(key)}
   *     since the second parameter is unclear ({@code String...} or {@code Arg...}.
   * @return the value (message) of the property key (message ID)
   */
  @Nonnull
  public static String getMessageWithItemName(@RequireNonnull String key,
      @RequireNonnull Arg[] args) {
    return getMessageWithItemName(null, key, args);
  }

  /**
   * Returns the localized value in messagesWithItemNames_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   * @param args message arguments, which can be message ID.
   *     The data type is {@code Arg[]}, not {@code Arg...} 
   *     because if {@code Arg} causes an error when you call {@code getMsg(key)}
   *     since the second parameter is unclear ({@code String...} or {@code Arg...}.
   * @return the message corresponding to the message ID
   */
  @Nonnull
  public static String getMessageWithItemName(@Nullable Locale locale, @RequireNonnull String key,
      @RequireNonnull Arg[] args) {
    return getMessageWithItemName(locale, key, getStringsFromArgs(locale, args));
  }

  /**
   * Returns the existence of the key in messagesWithItemNames_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key (message ID)
   */
  public static boolean hasMessageWithItemName(@RequireNonnull String key) {
    return getterMap.get(MESSAGES_WITH_ITEM_NAMES).hasProp(key);
  }

  // ■□■ strings ■□■

  /**
   * Returns the value in string_xxx.properties.
   * 
   * @param key the key of the property
   * @param args message arguments
   * @return the value (message) of the property key (message ID)
   */
  @Nonnull
  public static String getString(@RequireNonnull String key, @RequireNonnull String... args) {
    return formatMessage(getterMap.get(STRINGS).getProp(key, null), args);
  }

  /**
   * Returns the value in string_xxx.properties.
   * 
   * @param key the key of the property
   * @param args message arguments, which can be message ID.
   *     The data type is {@code Arg[]}, not {@code Arg...} 
   *     because if {@code Arg} causes an error when you call {@code getMsg(key)}
   *     since the second parameter is unclear ({@code String...} or {@code Arg...}.
   * @return the value (message) of the property key (message ID)
   */
  @Nonnull
  public static String getString(@RequireNonnull String key, @RequireNonnull Arg[] args) {
    return getString(key, getStringsFromArgs(null, args));
  }

  /**
   * Returns the existence of the key in strings_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasString(@RequireNonnull String key) {
    return getterMap.get(STRINGS).hasProp(key);
  }

  // ■□■ item_names ■□■

  /**
   * Returns the item name of default locale in item_names_xxx.properties.
   * 
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getItemName(@RequireNonnull String key) {
    return getterMap.get(ITEM_NAMES).getProp(null, key, null);
  }

  /**
   * Returns the localized item name in item_names_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getItemName(@Nullable Locale locale, @RequireNonnull String key) {
    return getterMap.get(ITEM_NAMES).getProp(locale, key, null);
  }

  /**
   * Returns the existence of the key in item_names_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasItemName(@RequireNonnull String key) {
    return getterMap.get(ITEM_NAMES).hasProp(key);
  }

  // ■□■ enum_names ■□■

  /**
   * Returns the enum name of default locale in enum_names_xxx.properties.
   * 
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getEnumName(@RequireNonnull String key) {
    return getterMap.get(ENUM_NAMES).getProp(null, key, null);
  }

  /**
   * Returns the localized enum name in enum_names_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getEnumName(@Nullable Locale locale, @RequireNonnull String key) {
    return getterMap.get(ENUM_NAMES).getProp(locale, key, null);
  }

  /**
   * Returns the existence of the key in enam_names_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasEnumName(@RequireNonnull String key) {
    return getterMap.get(ENUM_NAMES).hasProp(key);
  }

  // ■□■ ValidationMessages ■□■

  /**
   * Returns the property value of default locale in ValidationMessages[_locale].properties.
   * 
   * <p>Usually {@code ValidationMessages[_locale].properties} file 
   *     satisfies validation message's requirement.
   *     But when you want to show error messages on the top message space and  
   * 
   * @param key the key of the property
   * @param argMap argMap
   * @return the value of the property
   */
  @Nonnull
  public static String getValidationMessage(@RequireNonnull String key,
      Map<String, Object> argMap) {
    return getValidationMessage(null, key, argMap);
  }

  /**
   * Returns the localized enum name in ValidationMessages[_locale].properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getValidationMessage(@Nullable Locale locale, @RequireNonnull String key,
      @Nullable Map<String, Object> argMap) {
    String message = getterMap.get(VALIDATION_MESSAGES).getProp(locale, key, argMap);

    return substituteArgsToValidationMessages(locale, message, argMap);
  }

  // ■□■ ValidationMessagesWithItemNames ■□■

  /**
   * Returns the property value of default locale in ValidationMessagesWithItemNames_xxx.properties.
   * 
   * <p>Usually {@code ValidationMessages[_locale].properties} file 
   *     satisfies validation message's requirement.
   *     But when you want to show error messages on the top message space and  
   * 
   * @param key the key of the property
   * @param argMap argMap
   * @return the value of the property
   */
  @Nonnull
  public static String getValidationMessageWithItemName(@RequireNonnull String key,
      @Nullable Map<String, Object> argMap) {
    return getValidationMessageWithItemName(null, key, argMap);
  }

  /**
   * Returns the localized enum name in enum_names_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getValidationMessageWithItemName(@Nullable Locale locale,
      @RequireNonnull String key, @Nullable Map<String, Object> argMap) {
    String message =
        getterMap.get(VALIDATION_MESSAGES_WITH_ITEM_NAMES).getProp(locale, key, argMap);

    return substituteArgsToValidationMessages(locale, message, argMap);
  }

  /**
   * Substitutes params to strings using argMap.
   * 
   * <p>argMap is created basically in {@code ConstraintViolationBean}, 
   *     but locale needed string cannot be resolved in the instance
   *     so some parameters are set to argMap here.</p>
   */
  @Nonnull
  private static String substituteArgsToValidationMessages(@Nullable Locale locale,
      @RequireNonnull String message, @Nullable Map<String, Object> argMap) {

    argMap = argMap == null ? new HashMap<>() : argMap;

    final String argAnnotationValue = (String) argMap.get("annotation");
    final FieldInfoBean[] fieldInfoBean = (FieldInfoBean[]) argMap.get("itemAttributes");

    // for @ConditionalXxx
    String annotationPrefix = "jp.ecuacion.lib.core.jakartavalidation.validator.Conditional";
    if (argAnnotationValue != null && argAnnotationValue.startsWith(annotationPrefix)) {

      // itemName
      List<String> itemNameList = Arrays.asList(fieldInfoBean).stream()
          .map(bean -> PropertyFileUtil.getItemName(locale, bean.itemNameKey)).toList();
      argMap.put("itemName", StringUtil.getCsvWithSpace(itemNameList));
    }

    String rtnMessage = message;

    for (Entry<String, Object> entry : argMap.entrySet()) {
      // Escape "{" and "}" to prevent errors at MessageFormat.
      rtnMessage = rtnMessage.replace("{" + entry.getKey() + "}", entry.getValue() == null ? "''"
          : entry.getValue().toString().replace("{", "'{'").replace("}", "'}'"));
    }

    return rtnMessage;
  }

  // ■□■ ValidationMessagesPatternDescriptions ■□■

  /**
   * Returns the property value of default locale in ValidationMessages[_locale].properties.
   * 
   * <p>Usually {@code ValidationMessages[_locale].properties} file 
   *     satisfies validation message's requirement.
   *     But when you want to show error messages on the top message space and  
   * 
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getValidationMessagePatternDescription(@RequireNonnull String key) {
    return getValidationMessagePatternDescription(null, key);
  }

  /**
   * Returns the localized enum name in ValidationMessages[_locale].properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getValidationMessagePatternDescription(@Nullable Locale locale,
      @RequireNonnull String key) {
    return getterMap.get(VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS).getProp(locale, key, null);
  }

  // ■□■ abstract property ■□■

  /**
   * Returns the property value of default locale.
   * 
   * @param propertyUtilFileKind String value of 
   *     {@code PropertyUtilFileKind} (application, messages, ...)
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String get(@RequireNonnull String propertyUtilFileKind,
      @RequireNonnull String key) {
    return getterMap.get(PropertyFileUtilFileKindEnum.valueOf(propertyUtilFileKind.toUpperCase()))
        .getProp(null, key, null);
  }

  /**
   * Returns the localized property value.
   * 
   * @param propertyUtilFileKind String value of 
   *     {@code PropertyUtilFileKind} (application, messages, ...)
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String get(@RequireNonnull String propertyUtilFileKind, @Nullable Locale locale,
      @RequireNonnull String key) {
    return getterMap.get(PropertyFileUtilFileKindEnum.valueOf(propertyUtilFileKind.toUpperCase()))
        .getProp(locale, key, null);
  }

  /**
   * Returns the localized property value.
   * 
   * @param propertyUtilFileKind String value of 
   *     {@code PropertyUtilFileKind} (application, messages, ...)
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String get(@RequireNonnull String propertyUtilFileKind, @Nullable Locale locale,
      @RequireNonnull String key, String... args) {
    return formatMessage(get(propertyUtilFileKind, locale, key), args);
  }

  /**
   * Returns the localized property value.
   * 
   * @param propertyUtilFileKind String value of 
   *     {@code PropertyUtilFileKind} (application, messages, ...)
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String get(@RequireNonnull String propertyUtilFileKind, @Nullable Locale locale,
      @RequireNonnull String key, Arg... args) {
    return get(propertyUtilFileKind, locale, key, getStringsFromArgs(locale, args));
  }

  /**
   * Returns the existence of the key.
   * 
   * @param propertyUtilFileKind String value of 
   *     {@code PropertyUtilFileKind} (application, messages, ...)
   * @param key the key of the property
   * @return the value of the property
   */
  public static boolean has(@RequireNonnull String propertyUtilFileKind,
      @RequireNonnull String key) {
    return getterMap.get(PropertyFileUtilFileKindEnum.valueOf(propertyUtilFileKind.toUpperCase()))
        .hasProp(key);
  }

  /**
   * Adds postfix dinamically.
   * 
   * <p>If you add {@code test} for example, 
   *     {@code messages_test[_lang].properties, 
   *     application_test[_lang].properties, ...} are searched.</p>
   *     
   * <p>In java 9 module system environment, you also need to Service Provider Interface(SPI)
   *     defined in `ecuacion-lib-core`.</p>
   * 
   * @param postfix postfix
   */
  public static void addResourceBundlePostfix(@RequireNonnull String postfix) {
    PropertyFileUtilValueGetter.addToDynamicPostfixList(postfix);
  }

  private static String formatMessage(String msgStr, String... args) {
    return (args.length == 0) ? msgStr : MessageFormat.format(msgStr, (Object[]) args);
  }

  /**
   * Obtains string from {@code Arg}.
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param arg message arguments, which can be message ID.
   * @return the message corresponding to the message ID or the string set to {@code Arg}.
   */
  @Nonnull
  public static String getStringFromArg(@Nullable Locale locale, @RequireNonnull Arg arg) {
    String str = null;

    String msgIdStr = "";
    if (arg.argKind == ArgKind.MESSAGE_ID) {
      for (String fileKind : arg.getFileKinds()) {
        if (PropertyFileUtil.has(fileKind, arg.getArgString())) {
          msgIdStr = PropertyFileUtil.get(fileKind, locale, arg.getArgString(), arg.messageArgs);
        }
      }
    }

    switch (arg.argKind) {
      case MESSAGE_ID -> str = msgIdStr;
      case FORMATTED_STRING -> str =
          PropertyFileUtil.analyzedValueString(locale, arg.getArgString(), null);
      case STRING -> str = arg.getArgString();
      default -> throw new EclibRuntimeException("Unexpected.");
    }

    return str;
  }

  private static String[] getStringsFromArgs(Locale locale, @RequireNonnull Arg[] args) {
    final List<String> list = new ArrayList<>();
    Arrays.asList(ObjectsUtil.requireNonNull(args)).stream()
        .forEach(arg -> list.add(getStringFromArg(locale, arg)));
    return list.toArray(new String[list.size()]);
  }

  /**
   * Returns analyzed string.
   */
  public static String analyzedValueString(@Nullable Locale locale, String rawString,
      Map<String, Object> elParameterMap) {
    StringBuilder sb = new StringBuilder();
    sb.append(rawString);
    List<Pair<String, String>> list = null;
    elParameterMap = elParameterMap == null ? new HashMap<>() : elParameterMap;
    locale = locale == null ? Locale.ENGLISH : locale;

    // conditional branch if el expression exists for processing speed.
    if (sb.toString().contains("${+")) {
      // Analyze messageString for ${+...:xxx} format parameters. (like ${+messages:...})
      list = analyze(sb.toString());
      sb = new StringBuilder();

      for (Pair<String, String> tuple : list) {
        if (tuple.getLeft() == null) {
          sb.append(tuple.getRight());

        } else {
          sb.append(PropertyFileUtil.get(tuple.getLeft(), locale, tuple.getRight()));
        }
      }
    }

    // conditional branch if el expression exists for processing speed.
    if (sb.toString().contains("${")) {
      // Analyze messageString for ${xxx} (EL expression) format parameters.
      try {
        list = EmbeddedParameterUtil.getPartList(sb.toString(), new String[] {"${"}, "}",
            new Options().setIgnoresEmergenceOfEndSymbolOnly(true));

      } catch (StringFormatIncorrectException | MultipleAppException ex) {
        throw new EclibRuntimeException(ex);
      }

      sb = new StringBuilder();
      ELProcessor elProcessor = new ELProcessor();
      elParameterMap.forEach(elProcessor::setValue);

      for (Pair<String, String> tuple : list) {
        if (tuple.getLeft() == null) {
          sb.append(tuple.getRight());

        } else {
          sb.append(elProcessor.eval(tuple.getRight()).toString());
        }
      }
    }

    return sb.toString();
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
  private static List<Pair<String, String>> analyze(String string) {
    String prefix = "${+";
    List<String> startSymbols = Arrays.asList(PropertyFileUtilFileKindEnum.values()).stream()
        .map(en -> prefix + en.toString().toLowerCase() + ":").toList();

    // properties files are not managed by users
    // so exceptions occurring while analyzing string are changed to unchecked exceptions.
    try {
      List<Pair<String, String>> list = EmbeddedParameterUtil.getPartList(string,
          startSymbols.toArray(new String[startSymbols.size()]), "}",
          new Options().setIgnoresEmergenceOfEndSymbolOnly(true));

      // left of the pair starts with "{+" and ends with ":" but they're not needed
      return list.stream()
          .map(pair -> pair.getLeft() == null ? pair
              : Pair.of(pair.getLeft().substring(prefix.length(), pair.getLeft().length() - 1),
                  pair.getRight()))
          .toList();

    } catch (AppException ex) {
      throw new EclibRuntimeException(ex);
    }
  }

  /**
   * Is considered as an argument string, but you can set message ID replaced to message string 
   * with {@code PropertyFileUtil.getMessage(String)}.
   * 
   * <p>In UI application like web, 
   *     usually {@code "throw new AppException"} part does not care about the {@code locale}.
   *     It's taken care at {@code ExceptionHandler}.<br>
   *     So you also don't want obtain an appropriate locale 
   *     when you put message obtained from {@code PropertyFileUtil.getMsg(...)} 
   *     into the argument of {@code AppException}.<br><br>
   *     That's why this is needed.</p>
   * 
   * <p>Usually message argument is like {@code {0}, {1}, ...} 
   *     but {@code BeanValidation} message argument is like {@code {value}, {min}, ...}
   *     so it supports both of them. 
   *     When you want to use the former format you need to set value to {@code messageArgs},
   *     the latter {@code messageArgMap}.
   */
  public static class Arg {
    private ArgKind argKind;
    private String[] fileKinds;
    private String argString;
    private Arg[] messageArgs;

    /**
     * Constructs a new instance considered as a normal string.
     * 
     * @param argString argument
     */
    private Arg(ArgKind argKind, String argString, Arg... messageArgs) {
      this.argKind = argKind;
      this.fileKinds = new String[] {};
      this.argString = argString;
      this.messageArgs = messageArgs;
    }

    /**
     * Constructs a new instance considered as a normal string.
     * 
     * @param argString argument
     */
    private Arg(String[] fileKinds, String argString, Arg... messageArgs) {
      this.argKind = ArgKind.MESSAGE_ID;
      this.fileKinds = fileKinds;
      this.argString = argString;
      this.messageArgs = messageArgs;
    }

    /**
     * Constructs a new instance of normal string.
     * 
     * @param argString normal string
     * @return Arg
     */
    public static Arg string(String argString) {
      return new Arg(ArgKind.STRING, argString);
    }

    /**
     * Constructs an array of new instances of normal string.
     * 
     * @param argStrings an array of normal string
     * @return Arg[]
     */
    public static Arg[] strings(String... argStrings) {
      return Arrays.asList(argStrings).stream().map(arg -> Arg.string(arg)).toList()
          .toArray(new Arg[argStrings.length]);
    }

    /**
     * Constructs a new instance of normal string.
     * 
     * @param formattedString formattedString
     * @return Arg
     */
    public static Arg formattedString(String formattedString) {
      return new Arg(ArgKind.FORMATTED_STRING, formattedString);
    }

    /**
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @return Arg
     */
    public static Arg message(String messageId) {
      return new Arg(new String[] {PropertyFileUtilFileKindEnum.MESSAGES.toString()}, messageId,
          new Arg[] {});
    }

    /**
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @param stringArgs stringArgs
     * @return Arg
     */
    public static Arg message(String messageId, String... stringArgs) {
      List<String> stringArgList = Arrays.asList(stringArgs);
      Arg[] args = stringArgList.stream().map(str -> Arg.string(str)).toList()
          .toArray(new Arg[stringArgList.size()]);
      return new Arg(new String[] {PropertyFileUtilFileKindEnum.MESSAGES.toString()}, messageId,
          args);
    }

    /**
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @param messageArgs messageArgs
     * @return Arg
     */
    public static Arg message(String messageId, Arg... messageArgs) {
      return new Arg(new String[] {PropertyFileUtilFileKindEnum.MESSAGES.toString()}, messageId,
          messageArgs);
    }

    /**
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @return Arg
     */
    public static Arg get(String[] fileKinds, String messageId) {
      return new Arg(fileKinds, messageId, new Arg[] {});
    }

    /**
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @param stringArgs stringArgs
     * @return Arg
     */
    public static Arg get(String[] fileKinds, String messageId, String... stringArgs) {
      List<String> stringArgList = Arrays.asList(stringArgs);
      Arg[] args = stringArgList.stream().map(str -> Arg.string(str)).toList()
          .toArray(new Arg[stringArgList.size()]);
      return new Arg(fileKinds, messageId, args);
    }

    /**
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @param messageArgs messageArgs
     * @return Arg
     */
    public static Arg get(String[] fileKinds, String messageId, Arg... messageArgs) {
      return new Arg(fileKinds, messageId, messageArgs);
    }

    public ArgKind getArgKind() {
      return argKind;
    }

    public String[] getFileKinds() {
      return fileKinds;
    }

    public String getArgString() {
      return argString;
    }

    public Arg[] getMessageArgs() {
      return messageArgs;
    }
  }

  /**
   * FORMATTED_STRING is like "item name is: ${+item_names:xxx}".
   */
  public static enum ArgKind {
    STRING, FORMATTED_STRING, MESSAGE_ID
  }

  /** 
   * Holds kinds of property files.
   * 
   * <p>The first argument of the enum value (like "application", "messages", ...) 
   *     is called {@code filePrefix},
   *     which is the file literally the prefix of the property files.<br>
   *     But in some reasons some properties file kind need to have multiple file prefixes
   *     so the second argument {@code actualFilePrefixes} needed.</p>
   *     
   * <p>{@code actualFilePrefixes} is a data type of {@code String[][]}.
   *     If you want to simply use multiple prefixes, it's realized by 
   *     {@code new String[][] {new String[] {messages1, messages2}}}.
   *     With this prefix setting {@code PropertyFileUtilValueGetter} searches the key
   *     from files with these prefixes, and duplication of the key causes an error.
   *     <br><br>
   *     When you know {@code messages1} and {@code messages2} has duplication and 
   *     if {@code messages1} contains the key you don't want to search from {@code messages2},
   *     it can be realized by this setting.<br>
   *     {@code new String[][] {new String[] {messages1}, new String[] {messages2}}}.<br>
   *     Outside array manipulates the priority of the property files.</p>
   */
  public enum PropertyFileUtilFileKindEnum {

    /** 
     * application.properties. 
     */
    APPLICATION(new String[][] {new String[] {"application"}}, true),

    /** 
     * messages.properties. 
     */
    MESSAGES(new String[][] {new String[] {"messages"}}, false),

    /** 
     * messagesWithItemNames.properties. 
     * 
     * <p>Generally, messages.properties is supposed to be used for messages, 
     *     and messagesWithItemNames is not very understandable for users.
     *     Besides that, since the case that both messages.properties and 
     *     messagesWithItemNames.properties 
     *     are used is very rare, it's better for messages.properties to be used even when
     *     you treat "messages with item names" without that rare case.
     *     That's why messages.properties is also contained with this value.
     */
    MESSAGES_WITH_ITEM_NAMES(
        new String[][] {new String[] {"messages_with_item_names"}, new String[] {"messages"}},
        false),

    /**
     * strings.properties.
     */
    STRINGS(new String[][] {new String[] {"strings"}}, false),

    /** item_names. */
    ITEM_NAMES(new String[][] {new String[] {"item_names"}}, false),

    /** enum_names. */
    ENUM_NAMES(new String[][] {new String[] {"enum_names"}}, false),

    /** ValidationMessags. */
    VALIDATION_MESSAGES(new String[][] {new String[] {"ValidationMessages"}}, false),

    /** ValidationMessagsWithField. */
    VALIDATION_MESSAGES_WITH_ITEM_NAMES(
        new String[][] {new String[] {"ValidationMessagesWithItemNames"}}, false),

    VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS(
        new String[][] {new String[] {"ValidationMessagesPatternDescriptions"}}, false);

    private String[][] actualFilePrefixes;
    private boolean throwsExceptionWhenKeyDoesNotExist;

    private PropertyFileUtilFileKindEnum(String[][] actualFilePrefixes,
        boolean throwsExceptionWhenKeyDoesNotExist) {
      this.actualFilePrefixes = actualFilePrefixes;
      this.throwsExceptionWhenKeyDoesNotExist = throwsExceptionWhenKeyDoesNotExist;
    }

    public String[][] getActualFilePrefixes() {
      return actualFilePrefixes;
    }

    /**
     * Returns throwsExceptionWhenKeyDoesNotExist.
     */
    public boolean throwsExceptionWhenKeyDoesNotExist() {
      return throwsExceptionWhenKeyDoesNotExist;
    }
  }
}
