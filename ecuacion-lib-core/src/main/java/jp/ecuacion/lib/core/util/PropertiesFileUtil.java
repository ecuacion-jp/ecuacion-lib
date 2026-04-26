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

import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.APPLICATION;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.ENUM_NAMES;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.ITEM_NAMES;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.MESSAGES;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.MESSAGES_WITH_ITEM_NAMES;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.STRINGS;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.VALIDATION_MESSAGES;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.VALIDATION_MESSAGES_WITH_ITEM_NAMES;

import jakarta.el.ELProcessor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.util.EmbeddedVariableUtil.Options;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilValueGetter;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
 *     <td style="text-align: center">x?�?</td>
 *     <td>Treats localized messages</td>
 *   </tr>
 *   <tr>
 *     <td>strings[_xxx].properties</td>
 *     <td>getString(...)<br>
 *     <td style="text-align: center"></td>
 *     <td style="text-align: center">x?�?</td>
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
 *     {@code PropertiesFileUtil.getMessage(...)} will read all the properties above.<br>
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
 *     You can put a property key into a property value using {@code #{fileKind:key}} syntax.<br>
 *     For example, you can define keys and values like this in {@code messages.properties}.
 *     By executing {@code PropertiesFileUtil.getMessage("message")} you'll get {@code "a-b-c"}.</p>
 * <pre>
 *     message=a-#{messages:message_test1}-c
 *     message_test1=b</pre>
 *
 * <p>You can also omit the file kind using {@code #{key}} syntax.
 *     In that case the key is searched across
 *     messages, item_names, strings, and enum_names files.</p>
 *
 * <pre>
 *     message=a-#{message_test1}-c
 *     message_test1=b</pre>
 *
 * <p>Recursive resolution is also supported so you can even define like the one below. <br>
 * By executing {@code PropertiesFileUtil.getMessage("message")}
 * you'll get {@code "a-b-c-d-e-f-g"}.</p>
 *
 * <pre>
 *     message=a-#{messages:message_test1}-c-#{messages:message_test2}-g
 *     message_test1=b
 *     message_test2=d-#{messages:message_test3}-f
 *     message_test3=e</pre>
 *
 * <p>Other available file kinds:
 *     {@code #{application:...}}, {@code #{item_names:...}}, {@code #{enum_names:...}}.</p>
 *
 * <p>Recursive resolution is supported, but multiple layer of key is not supported.
 *     (which does not seem to be needed)</p>
 *     <pre>
 *     message=a-#{messages:#{messages:message_prefix}_test1}-c
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
public class PropertiesFileUtil {

  private static Map<PropertiesFileUtilFileKindEnum, PropertiesFileUtilValueGetter> getterMap =
      new HashMap<>();

  static {
    for (PropertiesFileUtilFileKindEnum anEnum : PropertiesFileUtilFileKindEnum.values()) {
      getterMap.put(anEnum, new PropertiesFileUtilValueGetter(anEnum));
    }
  }

  private static PropertiesFileUtilValueGetter obtainValueGetter(
      PropertiesFileUtilFileKindEnum fileKind) {
    return Objects.requireNonNull(getterMap.get(fileKind));
  }

  /**
   * Prevents other classes from instantiating it.
   */
  private PropertiesFileUtil() {}

  // === application ===

  /**
   * Returns the value in application_xxx.properties.
   * 
   * @param key the key of the property
   * @return the value of the property
   */
  public static String getApplication(String key) {
    return obtainValueGetter(APPLICATION).getProp(key, new HashMap<>());
  }


  /**
   * Returns the existence of the key in application_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasApplication(String key) {
    return obtainValueGetter(APPLICATION).hasProp(key);
  }

  /**
   * Returns the value in application_xxx.properties if it exists. 
   * Returns given default value if not.
   * 
   * @param key the key of the property
   * @param defaultValue default value
   * @return the value of the property
   */
  @SuppressWarnings("unchecked")
  public static <T extends @Nullable String> T getApplicationOrElse(String key, T defaultValue) {
    if (obtainValueGetter(APPLICATION).hasProp(key)) {
      return (T) obtainValueGetter(APPLICATION).getProp(key, new HashMap<>());

    } else {
      return defaultValue;
    }
  }

  // === message ===

  /**
   * Returns the localized value in messages_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   * @param args message arguments
   * @return the value (message) of the property key (message ID)
   */
  public static String getMessage(@Nullable Locale locale, String key, String... args) {
    return formatMessage(obtainValueGetter(MESSAGES).getProp(locale, key, new HashMap<>()), args);
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
  public static String getMessage(@Nullable Locale locale, String key, @NonNull Arg[] args) {
    return getMessage(locale, key, getStringsFromArgs(locale, args));
  }

  /**
   * Returns the existence of the key in messages_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key (message ID)
   */
  public static boolean hasMessage(String key) {
    return obtainValueGetter(MESSAGES).hasProp(key);
  }

  // === messageWithItemName ===

  /**
   * Returns the localized value in messagesWithItemNames_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   * @param args message arguments
   * @return the value (message) of the property key (message ID)
   */
  public static String getMessageWithItemName(@Nullable Locale locale, String key, String... args) {
    return formatMessage(
        obtainValueGetter(MESSAGES_WITH_ITEM_NAMES).getProp(locale, key, new HashMap<>()), args);
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
  public static String getMessageWithItemName(@Nullable Locale locale, String key,
      @NonNull Arg[] args) {
    return getMessageWithItemName(locale, key, getStringsFromArgs(locale, args));
  }

  /**
   * Returns the existence of the key in messagesWithItemNames_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key (message ID)
   */
  public static boolean hasMessageWithItemName(String key) {
    return obtainValueGetter(MESSAGES_WITH_ITEM_NAMES).hasProp(key);
  }

  // === strings ===

  /**
   * Returns the value in string_xxx.properties.
   * 
   * @param key the key of the property
   * @param args message arguments
   * @return the value (message) of the property key (message ID)
   */
  public static String getString(String key, String... args) {
    return formatMessage(obtainValueGetter(STRINGS).getProp(key, new HashMap<>()), args);
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
  public static String getString(String key, @NonNull Arg[] args) {
    return getString(key, getStringsFromArgs(null, args));
  }

  /**
   * Returns the existence of the key in strings_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasString(String key) {
    return obtainValueGetter(STRINGS).hasProp(key);
  }

  // === item_names ===

  /**
   * Returns the localized item name in item_names_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  public static String getItemName(@Nullable Locale locale, String key) {
    return obtainValueGetter(ITEM_NAMES).getProp(locale, key, new HashMap<>());
  }

  /**
   * Returns the existence of the key in item_names_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasItemName(String key) {
    return obtainValueGetter(ITEM_NAMES).hasProp(key);
  }

  // === enum_names ===

  /**
   * Returns the localized enum name in enum_names_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  public static String getEnumName(@Nullable Locale locale, String key) {
    return obtainValueGetter(ENUM_NAMES).getProp(locale, key, new HashMap<>());
  }

  /**
   * Returns the existence of the key in enam_names_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasEnumName(String key) {
    return obtainValueGetter(ENUM_NAMES).hasProp(key);
  }

  // === ValidationMessages ===

  /**
   * Returns the localized enum name in ValidationMessages[_locale].properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  public static String getValidationMessage(@Nullable Locale locale, String key,
      Map<@NonNull String, @Nullable Object> argMap) {
    String message = obtainValueGetter(VALIDATION_MESSAGES).getProp(locale, key, argMap);

    return substituteArgsToValidationMessages(locale, message, argMap);
  }

  /**
   * Returns true when the key exists in ValidationMessages[_xx].properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasValidationMessage(@Nullable Locale locale, String key) {
    return obtainValueGetter(VALIDATION_MESSAGES).hasProp(locale, key);
  }

  // === ValidationMessagesWithItemNames ===

  /**
   * Returns the property value of default locale in ValidationMessagesWithItemNames_xxx.properties.
   * 
   * <p>ValidationMessagesWithItemNames[_xx].properties stores messages with {@code {0}},
   *     which is a placeholder for item names.</p>
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property. Return the key string when the key does not exist.
   */
  public static String getValidationMessageWithItemName(@Nullable Locale locale, String key,
      Map<@NonNull String, @Nullable Object> argMap) {
    String message =
        obtainValueGetter(VALIDATION_MESSAGES_WITH_ITEM_NAMES).getProp(locale, key, argMap);

    return substituteArgsToValidationMessages(locale, message, argMap);
  }

  /**
   * Returns true when the key exists in ValidationMessagesWithItemNames[_xx].properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasValidationMessageWithItemName(@Nullable Locale locale, String key) {
    return obtainValueGetter(VALIDATION_MESSAGES_WITH_ITEM_NAMES).hasProp(locale, key);
  }

  /**
   * Substitutes params to strings using argMap.
   * 
   * <p>argMap is created basically in {@code ConstraintViolationBean}, 
   *     but locale needed string cannot be resolved in the instance
   *     so some parameters are set to argMap here.</p>
   */
  @SuppressWarnings("null")
  private static String substituteArgsToValidationMessages(@Nullable Locale locale, String message,
      Map<@NonNull String, @Nullable Object> argMap) {

    final String argAnnotationValue = (String) argMap.get("annotation");
    final Item[] item = (Item[]) argMap.get("itemAttributes");

    // for @ConditionalXxx
    String annotationPrefix = "jp.ecuacion.lib.core.jakartavalidation.validator.Conditional";
    if (argAnnotationValue != null && argAnnotationValue.startsWith(annotationPrefix)) {

      // itemName
      List<@NonNull String> itemNameList = Arrays.asList(item).stream()
          .map(bean -> PropertiesFileUtil.getItemName(locale, bean.getItemNameKey())).toList();
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

  // === ValidationMessagesPatternDescriptions ===

  /**
   * Returns the localized enum name in ValidationMessages[_locale].properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param key the key of the property
   * @return the value of the property
   */
  public static String getValidationMessagePatternDescription(@Nullable Locale locale, String key) {
    return obtainValueGetter(VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS).getProp(locale, key,
        new HashMap<>());
  }

  // === abstract property ===

  /**
   * Returns the property value of default locale.
   * 
   * @param propertyUtilFileKind String value of 
   *     {@code PropertyUtilFileKind} (application, messages, ...)
   * @param key the key of the property
   * @return the value of the property
   */
  public static String get(String propertyUtilFileKind, String key) {
    return obtainValueGetter(
        PropertiesFileUtilFileKindEnum.valueOf(propertyUtilFileKind.toUpperCase())).getProp(null,
            key, new HashMap<>());
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
  public static String get(String propertyUtilFileKind, @Nullable Locale locale, String key) {
    return obtainValueGetter(
        PropertiesFileUtilFileKindEnum.valueOf(propertyUtilFileKind.toUpperCase())).getProp(locale,
            key, new HashMap<>());
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
  public static String get(String propertyUtilFileKind, @Nullable Locale locale, String key,
      String... args) {
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
  public static String get(String propertyUtilFileKind, @Nullable Locale locale, String key,
      @NonNull Arg... args) {
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
  public static boolean has(String propertyUtilFileKind, String key) {
    return obtainValueGetter(
        PropertiesFileUtilFileKindEnum.valueOf(propertyUtilFileKind.toUpperCase())).hasProp(key);
  }

  /**
   * Adds postfix dynamically.
   * This is basically for unit-test. NOT RECOMMENDED for production code.
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
  public static void addResourceBundlePostfix(String postfix) {
    PropertiesFileUtilValueGetter.addToDynamicPostfixList(postfix);
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
  public static String getStringFromArg(@Nullable Locale locale, Arg arg) {

    if (arg.argKind == ArgKind.MESSAGE_ID) {
      String msgIdStr = "";
      String argString = Objects.requireNonNull(arg.getArgString());
      for (String fileKind : arg.getFileKinds()) {
        arg.messageArgs = arg.messageArgs == null ? new @NonNull Arg[] {} : arg.messageArgs;
        // Obtain the return value even if key does not exist because it returns the key string.
        msgIdStr = PropertiesFileUtil.get(fileKind, locale, argString, arg.messageArgs);

        if (PropertiesFileUtil.has(fileKind, argString)) {
          break;
        }
      }

      return msgIdStr;

    } else if (arg.argKind == ArgKind.FORMATTED_STRING) {
      List<String> argStrList = new ArrayList<>();
      String argString = PropertiesFileUtil.analyzedValueString(locale,
          Objects.requireNonNull(arg.getArgString()), new HashMap<>());

      Arg[] messageArgs = arg.getMessageArgs() == null ? new Arg[] {} : arg.getMessageArgs();
      for (Arg tmpArg : messageArgs) {
        argStrList.add(getStringFromArg(locale, tmpArg));
      }

      // replace ' to '' because MessageFormat removes single '.
      return MessageFormat.format(argString.replace("'", "''"),
          (Object[]) argStrList.toArray(new String[argStrList.size()]));

    } else if (arg.argKind == ArgKind.STRING) {
      return arg.getArgString();

    } else {
      throw new RuntimeException("Unexpected.");
    }
  }

  private static String[] getStringsFromArgs(@Nullable Locale locale, @NonNull Arg[] args) {
    final List<String> list = new ArrayList<>();
    Arrays.asList(ObjectsUtil.requireNonNull(args)).stream()
        .forEach(arg -> list.add(getStringFromArg(locale, arg)));
    return list.toArray(new String[list.size()]);
  }

  private static final List<PropertiesFileUtilFileKindEnum> FILE_KINDS_FOR_KEY_ONLY_SEARCH =
      List.of(MESSAGES, ITEM_NAMES, STRINGS, ENUM_NAMES);

  private static String searchKeyAcrossFileKinds(@Nullable Locale locale, String key) {
    for (PropertiesFileUtilFileKindEnum fileKind : FILE_KINDS_FOR_KEY_ONLY_SEARCH) {
      if (obtainValueGetter(fileKind).hasProp(locale, key)) {
        return PropertiesFileUtil.get(fileKind.toString().toLowerCase(), locale, key, new Arg[] {});
      }
    }
    throw new RuntimeException(
        "Key '" + key + "' not found in any properties file for '#{key}' syntax.");
  }

  /**
   * Returns analyzed string.
   */
  public static String analyzedValueString(@Nullable Locale locale, String rawString,
      Map<@NonNull String, @Nullable Object> elParameterMap) {
    StringBuilder sb = new StringBuilder();
    sb.append(rawString);
    List<Pair<String, String>> list = null;
    locale = locale == null ? Locale.ENGLISH : locale;

    // conditional branch if el expression exists for processing speed.
    if (sb.toString().contains("#{")) {
      // Analyze messageString for #{fileKind:key} and #{key} format parameters.
      list = analyze(sb.toString());
      sb = new StringBuilder();

      for (Pair<String, String> tuple : list) {
        if (tuple.getLeft() == null) {
          sb.append(tuple.getRight());

        } else if (tuple.getLeft().isEmpty()) {
          sb.append(searchKeyAcrossFileKinds(locale, tuple.getRight()));

        } else {
          sb.append(
              PropertiesFileUtil.get(tuple.getLeft(), locale, tuple.getRight(), new Arg[] {}));
        }
      }
    }

    // conditional branch if el expression exists for processing speed.
    if (sb.toString().contains("${")) {
      // Analyze messageString for ${xxx} (EL expression) format parameters.
      try {
        list = EmbeddedVariableUtil.getPartList(sb.toString(), new String[] {"${"}, "}",
            new Options().setIgnoresEmergenceOfEndSymbolOnly(true));

      } catch (ViolationException ex) {
        throw new RuntimeException(ex);
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
   * Analyzes and obtain final message part.
   *
   * <p>Handles two syntax forms:</p>
   * <ul>
   * <li>{@code #{fileKind:key}} - resolves key from the specified file kind</li>
   * <li>{@code #{key}} - resolves key by searching across messages, item_names,
   *     strings, and enum_names</li>
   * </ul>
   *
   * <p>For example, when the message is {@code Hello, #{messages:human}!},
   *     the analyzed result is:<br>
   *     {@code (null, "Hello, "), ("messages", "human"), (null, "!")}.</p>
   *
   * <p>When the message is {@code Hello, #{human}!}, the left side of the pair is
   *     {@code ""} (empty string), indicating a key-only reference.</p>
   *
   * @param string string
   * @return list of pairs where left is fileKind (or null for literals, "" for key-only)
   */
  private static List<Pair<String, String>> analyze(String string) {
    final String prefix = "#{";

    // Pass 1: #{fileKind:key} patterns (like #{messages:key}, #{item_names:key}).
    List<String> fileKindStartSymbols = Arrays.asList(PropertiesFileUtilFileKindEnum.values())
        .stream().map(en -> prefix + en.toString().toLowerCase() + ":").toList();

    List<Pair<String, String>> pass1Result = EmbeddedVariableUtil.getPartList(string,
        fileKindStartSymbols.toArray(new String[fileKindStartSymbols.size()]), "}",
        new Options().setIgnoresEmergenceOfEndSymbolOnly(true));

    // Pass 2: #{key} patterns (no fileKind) found in remaining literal parts.
    List<Pair<String, String>> resultList = new ArrayList<>();
    for (Pair<String, String> pair : pass1Result) {
      if (pair.getLeft() != null || !pair.getRight().contains(prefix)) {
        resultList.add(pair);
      } else {
        List<Pair<String, String>> subList = EmbeddedVariableUtil.getPartList(pair.getRight(),
            new String[] {prefix}, "}", new Options().setIgnoresEmergenceOfEndSymbolOnly(true));
        for (Pair<String, String> subPair : subList) {
          resultList.add(subPair.getLeft() != null ? Pair.of("", subPair.getRight()) : subPair);
        }
      }
    }

    // Error check: remaining "#{" in literal parts means incorrect syntax.
    if (resultList.stream().anyMatch(p -> p.getLeft() == null && p.getRight().contains(prefix))) {
      throw new RuntimeException("Improper '#{' symbols found in a message. message: " + string);
    }

    // Strip "#{" prefix and ":" suffix from fileKind start symbols; leave "" (key-only) as-is.
    return resultList.stream()
        .map(pair -> pair.getLeft() == null || pair.getLeft().isEmpty() ? pair
            : Pair.of(pair.getLeft().substring(prefix.length(), pair.getLeft().length() - 1),
                pair.getRight()))
        .toList();
  }

  /**
   * Is considered as an argument string, but you can set message ID replaced to message string 
   * with {@code PropertiesFileUtil.getMessage(String)}.
   * 
   * <p>In UI application like web, 
   *     usually {@code "throw new AppException"} part does not care about the {@code locale}.
   *     It's taken care at {@code ExceptionHandler}.<br>
   *     So you also don't want obtain an appropriate locale 
   *     when you put message obtained from {@code PropertiesFileUtil.getMsg(...)} 
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
    private @NonNull String[] fileKinds;
    /**
     * Argument string.
     * When created via {@link #string(String)} with a {@code null} argument,
     * the value is stored as the string {@code "null"}.
     */
    private String argString;
    private @NonNull Arg[] messageArgs;

    /**
     * Constructs a new instance considered as a normal string.
     * 
     * @param argString argument
     */
    private Arg(ArgKind argKind, String argString, @NonNull Arg... messageArgs) {
      this.argKind = argKind;
      this.fileKinds = new @NonNull String[] {};
      this.argString = argString;
      this.messageArgs = messageArgs;
    }

    /**
     * Constructs a new instance considered as a normal string.
     * 
     * @param argString argument
     */
    private Arg(@NonNull String[] fileKinds, String argString, @NonNull Arg... messageArgs) {
      this.argKind = ArgKind.MESSAGE_ID;
      this.fileKinds = fileKinds;
      this.argString = argString;
      this.messageArgs = messageArgs;
    }

    /**
     * Constructs a new instance of normal string.
     *
     * <p>If {@code argString} is {@code null}, the string {@code "null"} is stored.</p>
     *
     * @param argString normal string, may be {@code null}
     * @return Arg
     */
    public static Arg string(@Nullable String argString) {
      return new Arg(ArgKind.STRING, Objects.requireNonNull(Objects.toString(argString, "null")));
    }

    /**
     * Constructs an array of new instances of normal string.
     * 
     * @param argStrings an array of normal string
     * @return Arg[]
     */
    public static Arg[] strings(@NonNull String... argStrings) {
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
     * Constructs a new instance of normal string.
     * 
     * @param formattedString formattedString
     * @return Arg
     */
    public static Arg formattedString(String formattedString, @NonNull Arg... args) {
      return new Arg(ArgKind.FORMATTED_STRING, formattedString, args);
    }

    /**
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @return Arg
     */
    public static Arg message(String messageId) {
      return new Arg(new String[] {PropertiesFileUtilFileKindEnum.MESSAGES.toString()}, messageId,
          new @NonNull Arg[] {});
    }

    /**
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @param stringArgs stringArgs
     * @return Arg
     */
    @SuppressWarnings("null")
    public static Arg message(String messageId, @NonNull String... stringArgs) {
      List<String> stringArgList = Arrays.asList(stringArgs);
      Arg[] args = stringArgList.stream().map(str -> Arg.string(str)).toList()
          .toArray(new Arg[stringArgList.size()]);
      return new Arg(new String[] {PropertiesFileUtilFileKindEnum.MESSAGES.toString()}, messageId,
          args);
    }

    /**
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @param messageArgs messageArgs
     * @return Arg
     */
    public static Arg message(String messageId, @NonNull Arg... messageArgs) {
      return new Arg(new @NonNull String[] {PropertiesFileUtilFileKindEnum.MESSAGES.toString()},
          messageId, messageArgs);
    }

    /**
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @return Arg
     */
    public static Arg get(@NonNull String[] fileKinds, String messageId) {
      return new Arg(fileKinds, messageId, new @NonNull Arg[] {});
    }

    /**
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @param stringArgs stringArgs
     * @return Arg
     */
    public static Arg get(@NonNull String[] fileKinds, String messageId,
        @Nullable String... stringArgs) {
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
    public static Arg get(@NonNull String[] fileKinds, String messageId,
        @NonNull Arg... messageArgs) {
      return new Arg(fileKinds, messageId, messageArgs);
    }

    public ArgKind getArgKind() {
      return argKind;
    }

    public @NonNull String[] getFileKinds() {
      return fileKinds;
    }

    public String getArgString() {
      return argString;
    }

    public @NonNull Arg[] getMessageArgs() {
      return messageArgs;
    }
  }

  /**
   * FORMATTED_STRING is like "item name is: #{item_names:xxx}".
   */
  public static enum ArgKind {
    STRING, FORMATTED_STRING, MESSAGE_ID
  }
}
