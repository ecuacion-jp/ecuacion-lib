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
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.ITEM_NAME;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.MSG;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.VALIDATION_MESSAGES;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.VALIDATION_MESSAGES_WITH_ITEM_NAMES;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.jakartavalidation.validator.internal.ConditionalValidator;
import jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum;
import jp.ecuacion.lib.core.util.internal.PropertyFileUtilValueGetter;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides utility methods to read {@code *.properties} files.
 * 
 * <p>It has following features added to {@code ResourceBundle} class packaged in JRE.</p>
 * 
 * <ol>
 * <li>To read all the ".properties" files in library modules 
 *     and multiple modules in projects of an app.</li>
 * <li>To read multiple kinds of ".properties" 
 *     ({@code application, messages, enum_names, item_names, ValidationMessages, 
 *     ValidationMessagesWithItemNames})</li>
 * <li>To remove default locale from candidate locales</li>
 * <li>To use "default" message by putting the postfix of the message ID ".default"</li>
 * <li>To have the override function by java launch parameter (-D) or System.setProperty(...) </li>
 * <li>To resolve property keys in the obtained value</li>
 * <li>To resolve property keys in arguments</li>
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
 *     (application, messages, enum_names, item_names)</b><br><br>
 *     Firstly, In {@code ecuacion-lib} we have 4 kinds of property files.<br><br>
 *     
 *     {@code PropertyFileUtil.getMsg(...) : messages[_xxx].properties}<br>
 *     {@code PropertyFileUtil.getApp(...) : application[_xxx].properties}<br>
 *     {@code PropertyFileUtil.getEnumName(...) : enum_names[_xxx].properties}<br>
 *     {@code PropertyFileUtil.getItemName(...) : item_names[_xxx].properties}<br><br>
 *     
 *     {@code messages.properties} and {@code application.properties} are well-known.<br>
 *     {@code enum_names.properties} stores the localized name of the enum element, and
 *     {@code item_names.properties} stores the localized name of the item.<br>
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
 * <td>item_names</td>
 * <td>names of items</td>
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
 * like {@code ${application:...}, ${item_names:...} and ${enum_names:...}}.</p>
 *     
 * <p>Recursive resolution is supported, but multiple layer of key is not supported. 
 *     (which does not seem to be needed really)</p>
 *     <pre>
 *     message=a-${messages:${messages:message_prefix}_test1}-c
 *     message_prefix=message
 *     message_test1=b</pre><br>
 * 
 * <p><b>7. To resolve property keys in arguments</b></p>
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
    for (PropertyFileUtilFileKindEnum anEnum : PropertyFileUtilFileKindEnum.values()) {
      getterMap.put(anEnum, new PropertyFileUtilValueGetter(anEnum));
    }
  }

  /** Does not construct an instance.  */
  private PropertyFileUtil() {}

  // ■□■ application ■□■

  /**
   * Returns the value in application_xxx.properties.
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
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasApp(@RequireNonnull String key) {
    return getterMap.get(APP).hasProp(key);
  }

  /**
   * Returns the value in application_xxx.properties.
   * 
   * @param key the key of the property
   * @return the value of the property
   */
  @Nonnull
  public static String getApplication(@RequireNonnull String key) {
    return getterMap.get(APP).getProp(key);
  }


  /**
   * Returns the existence of the key in application_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasApplication(@RequireNonnull String key) {
    return getterMap.get(APP).hasProp(key);
  }

  /**
   * Returns the value of default locale in messages_xxx.properties.
   * 
   * @param key the key of the property
   * @param args message arguments
   * @return the value (message) of the property key (message ID)
   */
  @Nonnull
  public static String getMsg(@RequireNonnull String key, @RequireNonnull String... args) {
    return getMessage(key, args);
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
  public static String getMsg(@Nullable Locale locale, @RequireNonnull String key,
      @RequireNonnull String... args) {
    return getMessage(locale, key, args);
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
  public static String getMsg(@RequireNonnull String key, @RequireNonnull Arg[] args) {
    return getMessage(key, args);
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
  public static String getMsg(@Nullable Locale locale, @RequireNonnull String key,
      @RequireNonnull Arg[] args) {
    return getMessage(locale, key, args);
  }

  /**
   * Returns the existence of the key in messages_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key (message ID)
   */
  public static boolean hasMsg(@RequireNonnull String key) {
    return hasMessage(key);
  }

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

    String msgStr = getterMap.get(MSG).getProp(locale, key);

    // データパターンにより処理を分岐
    return (args.length == 0) ? msgStr : MessageFormat.format(msgStr, (Object[]) args);
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

    final List<String> list = new ArrayList<>();
    Arrays.asList(args).stream().forEach(arg -> list.add(getStringFromArg(locale, arg)));

    return getMessage(locale, key, list.toArray(new String[list.size()]));
  }

  /**
   * Obtains string from {@code Arg}.
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param arg message arguments, which can be message ID.
   * @return the message corresponding to the message ID or the string set to {@code Arg}.
   */
  public static String getStringFromArg(@Nullable Locale locale, @RequireNonnull Arg arg) {
    return arg.isMessageId()
        ? PropertyFileUtil.getMessage(locale, arg.getArgString(), arg.messageArgs)
        : arg.getArgString();
  }

  /**
   * Returns the existence of the key in messages_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key (message ID)
   */
  public static boolean hasMessage(@RequireNonnull String key) {
    return getterMap.get(MSG).hasProp(key);
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
    return getterMap.get(ITEM_NAME).getProp(null, key);
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
    return getterMap.get(ITEM_NAME).getProp(locale, key);
  }

  /**
   * Returns the existence of the key in item_names_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasItemName(@RequireNonnull String key) {
    return getterMap.get(ITEM_NAME).hasProp(key);
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
    return getterMap.get(ENUM_NAME).getProp(null, key);
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
    return getterMap.get(ENUM_NAME).getProp(locale, key);
  }

  /**
   * Returns the existence of the key in enam_names_xxx.properties.
   * 
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasEnumName(@RequireNonnull String key) {
    return getterMap.get(ENUM_NAME).hasProp(key);
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
      Map<String, Object> argMap) {
    String message = getterMap.get(VALIDATION_MESSAGES).getProp(locale, key);

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
      Map<String, Object> argMap) {
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
      @RequireNonnull String key, Map<String, Object> argMap) {
    String message = getterMap.get(VALIDATION_MESSAGES_WITH_ITEM_NAMES).getProp(locale, key);

    return substituteArgsToValidationMessages(locale, message, argMap);
  }

  private static String substituteArgsToValidationMessages(Locale locale, String message,
      Map<String, Object> argMap) {
    String annotation;
    String key;
    String newKey;
    String newValue;

    String argAnnotationValue = (String) argMap.get("annotation");
    // get patternDescription from descriptionId (for @PatternWithDescription)
    annotation = "jp.ecuacion.lib.core.jakartavalidation.validator.PatternWithDescription";
    if (argAnnotationValue != null && argAnnotationValue.equals(annotation)) {
      key = "descriptionId";
      newKey = "patternDescription";
      newValue =
          PropertyFileUtil.getValidationMessagePatternDescription(locale, (String) argMap.get(key));
      argMap.put(newKey, newValue);
    }

    // get fieldName from field (for @ConditionalXxx)
    annotation = "jp.ecuacion.lib.core.jakartavalidation.validator.Conditional";
    if (argAnnotationValue != null && argAnnotationValue.startsWith(annotation)) {
      String itemIdClass = (argMap.containsKey("itemIdClass")
          && StringUtils.isNotEmpty((String) argMap.get("itemIdClass")))
              ? (String) argMap.get("itemIdClass")
              : null;
      final String className = itemIdClass == null ? ((String) argMap.get("leafClassName"))
          .substring(((String) argMap.get("leafClassName")).lastIndexOf(".") + 1) : itemIdClass;

      // field -> fieldDisplayName
      key = "itemIds";
      newKey = "fieldDisplayName";
      String[] itemIds = (String[]) argMap.get(key);
      List<String> fieldDisplayNameList = new ArrayList<>();
      for (String itemId : itemIds) {
        fieldDisplayNameList.add(PropertyFileUtil.getItemName(locale, itemId));
      }
      argMap.put(newKey, StringUtil.getCsvWithSpace(fieldDisplayNameList));

      // conditionField -> conditionFieldDisplayName
      key = ConditionalValidator.CONDITION_FIELD;
      newKey = "conditionFieldDisplayName";
      newValue = PropertyFileUtil.getItemName(locale, className + "." + argMap.get(key));
      argMap.put(newKey, newValue);

      key = ConditionalValidator.CONDITION_VALUE_KIND;
      newKey = "conditionValueDescription";
      newValue = PropertyFileUtil.getMessage(locale,
          "jp.ecuacion.validation.constraints.Conditional.messagePart." + argMap.get(key),
          (String) argMap.get(ConditionalValidator.VALUE_OF_CONDITION_FIELD_TO_VALIDATE));
      argMap.put(newKey, newValue);
    }

    String rtnMessage = message;

    for (Entry<String, Object> entry : argMap.entrySet()) {
      // 設定する値は、MessageFormatでエラーにならないよう、中括弧をescape
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
    return getterMap.get(VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS).getProp(locale, key);
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
    return getterMap.get(PropertyFileUtilFileKindEnum.getEnumFromFilePrefix(propertyUtilFileKind))
        .getProp(null, key);
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
    return getterMap.get(PropertyFileUtilFileKindEnum.getEnumFromFilePrefix(propertyUtilFileKind))
        .getProp(locale, key);
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
    return getterMap.get(PropertyFileUtilFileKindEnum.getEnumFromFilePrefix(propertyUtilFileKind))
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
  public static void addResourceBundlePostfix(String postfix) {
    PropertyFileUtilValueGetter.addToDynamicPostfixList(postfix);
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
    private boolean isMessageId;
    private String argString;
    private Arg[] messageArgs;

    /**
     * Constructs a new instance of normal string.
     * 
     * @param argString normal string
     * @return Arg
     */
    public static Arg string(String argString) {
      return new Arg(false, argString);
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
     * Constructs a new instance of messageId and messageArgs.
     * 
     * @param messageId messageId
     * @param messageArgs messageArgs
     * @return Arg
     */
    public static Arg message(String messageId, Arg... messageArgs) {
      return new Arg(true, messageId, messageArgs);
    }

    /**
     * Constructs a new instance considered as a normal string.
     * 
     * @param argString argument
     */
    private Arg(boolean isMessageId, String argString, Arg... messageArgs) {
      this.isMessageId = isMessageId;
      this.argString = argString;
      this.messageArgs = messageArgs;
    }

    public String getArgString() {
      return argString;
    }

    public boolean isMessageId() {
      return isMessageId;
    }

    public Arg[] getMessageArgs() {
      return messageArgs;
    }
  }
}
