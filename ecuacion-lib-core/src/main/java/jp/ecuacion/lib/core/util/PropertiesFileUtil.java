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
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.CONSTANTS;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.ENUM_NAMES;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.ITEM_NAMES;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.MESSAGES;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.MESSAGES_WITH_ITEM_NAMES;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.VALIDATION_MESSAGES;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.VALIDATION_MESSAGES_WITH_ITEM_NAMES;

import java.util.Locale;
import java.util.Map;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilBundleReader;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilFormatter;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Utility for reading {@code *.properties} files, wrapping {@link java.util.ResourceBundle}.
 *
 * <p>Extra features compared to {@code ResourceBundle}:</p>
 *
 * <ul>
 * <li><b>Multiple file kinds</b> — application, messages, constants, item_names, enum_names,
 *     ValidationMessages, etc. Each kind has a dedicated {@code get*()} method.</li>
 * <li><b>Cross-module loading</b> — reads all {@code messages[_xxx].properties} files
 *     across ecuacion modules and app modules simultaneously;
 *     duplicate keys across files throw an exception.</li>
 * <li><b>Missing-key behavior</b> — {@code application.properties} throws on missing key;
 *     all other kinds return the key itself.</li>
 * <li><b>{@code .default} override</b> — ecuacion module keys carry a {@code .default}
 *     suffix; apps override them by defining the key without the suffix.</li>
 * <li><b>System property override</b> — values in {@code application.properties} can be
 *     overridden via {@code -D} JVM argument or {@code System.setProperty}.</li>
 * <li><b>{@code #{fileKind:key}} / {@code #{key}} embedding</b> — property values may
 *     reference other property keys; resolution is recursive.
 *     {@code #{key}} searches across messages, item_names, constants, and enum_names.</li>
 * <li><b>{@link Arg} for typed arguments</b> — methods that accept {@code Object... args}
 *     allow plain {@code Object} values and {@link Arg} instances to be freely mixed.
 *     {@link Arg#object(Object)} preserves the original type for type-aware
 *     {@link java.text.MessageFormat} patterns (e.g., {@code {0,number,#,###}});
 *     {@link Arg#message(String)} resolves a message key at render time so that
 *     arguments themselves can be localized.</li>
 * <li><b>EL expression support</b> — {@code ${1 + 1}} in a value is evaluated via
 *     the Jakarta Expression Language processor.</li>
 * </ul>
 */
public class PropertiesFileUtil {

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
    return PropertiesFileUtilResolver.getProp(null, APPLICATION, key);
  }

  /**
   * Returns the existence of the key in application_xxx.properties.
   *
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasApplication(String key) {
    return PropertiesFileUtilResolver.hasProp(APPLICATION, key);
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
    if (PropertiesFileUtilResolver.hasProp(APPLICATION, key)) {
      return (T) PropertiesFileUtilResolver.getProp(null, APPLICATION, key);

    } else {
      return defaultValue;
    }
  }

  // === message ===

  /**
   * Returns the localized value in messages_xxx.properties.
   *
   * @param locale locale, may be {@code null} which means no {@code Locale} specified.
   * @param key the key of the property
   * @param args message arguments; {@link Arg} instances and plain {@code Object}s may be mixed
   * @return the value (message) of the property key (message ID)
   */
  public static String getMessage(@Nullable Locale locale, String key, @Nullable Object... args) {
    String template = PropertiesFileUtilResolver.getProp(locale, MESSAGES, key);
    return PropertiesFileUtilFormatter.formatWithArgs(locale, template,
        PropertiesFileUtilResolver.resolveArgElements(locale, args));
  }

  /**
   * Returns the value in messages_xxx.properties using the default locale.
   *
   * @param key the key of the property
   * @param args message arguments; {@link Arg} instances and plain {@code Object}s may be mixed
   * @return the value (message) of the property key (message ID)
   */
  public static String getMessage(String key, @Nullable Object... args) {
    return getMessage(null, key, args);
  }

  /**
   * Returns the existence of the key in messages_xxx.properties.
   *
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key (message ID)
   */
  public static boolean hasMessage(String key) {
    return PropertiesFileUtilResolver.hasProp(MESSAGES, key);
  }

  // === messageWithItemName ===

  /**
   * Returns the localized value in messagesWithItemNames_xxx.properties.
   *
   * @param locale locale, may be {@code null} which means no {@code Locale} specified.
   * @param key the key of the property
   * @param args message arguments; {@link Arg} instances and plain {@code Object}s may be mixed
   * @return the value (message) of the property key (message ID)
   */
  public static String getMessageWithItemName(@Nullable Locale locale, String key,
      @Nullable Object... args) {
    String template = PropertiesFileUtilResolver.getProp(locale, MESSAGES_WITH_ITEM_NAMES, key);
    return PropertiesFileUtilFormatter.formatWithArgs(locale, template,
        PropertiesFileUtilResolver.resolveArgElements(locale, args));
  }

  /**
   * Returns the value in messagesWithItemNames_xxx.properties using the default locale.
   *
   * @param key the key of the property
   * @param args message arguments; {@link Arg} instances and plain {@code Object}s may be mixed
   * @return the value (message) of the property key (message ID)
   */
  public static String getMessageWithItemName(String key, @Nullable Object... args) {
    return getMessageWithItemName(null, key, args);
  }

  /**
   * Returns the existence of the key in messagesWithItemNames_xxx.properties.
   *
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key (message ID)
   */
  public static boolean hasMessageWithItemName(String key) {
    return PropertiesFileUtilResolver.hasProp(MESSAGES_WITH_ITEM_NAMES, key);
  }

  // === constants ===

  /**
   * Returns the value in constants_xxx.properties.
   *
   * <p>Constants are not localized by design; no {@link Locale} parameter is provided.</p>
   *
   * @param key the key of the property
   * @param args message arguments; {@link Arg} instances and plain {@code Object}s may be mixed
   * @return the value of the property key
   */
  public static String getConstant(String key, @Nullable Object... args) {
    String template = PropertiesFileUtilResolver.getProp(null, CONSTANTS, key);
    return PropertiesFileUtilFormatter.formatWithArgs(null, template,
        PropertiesFileUtilResolver.resolveArgElements(null, args));
  }

  /**
   * Returns the existence of the key in constants_xxx.properties.
   *
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasConstant(String key) {
    return PropertiesFileUtilResolver.hasProp(CONSTANTS, key);
  }

  // === item_names ===

  /**
   * Returns the localized item name in item_names_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.ROOT}.
   * @param key the key of the property
   * @return the value of the property
   */
  public static String getItemName(@Nullable Locale locale, String key) {
    return PropertiesFileUtilResolver.getProp(locale, ITEM_NAMES, key);
  }

  /**
   * Returns the localized item name in item_names_xxx.properties using the default locale.
   *
   * @param key the key of the property
   * @return the value of the property
   */
  public static String getItemName(String key) {
    return getItemName(null, key);
  }

  /**
   * Returns the existence of the key in item_names_xxx.properties.
   *
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasItemName(String key) {
    return PropertiesFileUtilResolver.hasProp(ITEM_NAMES, key);
  }

  // === enum_names ===

  /**
   * Returns the localized enum name in enum_names_xxx.properties.
   * 
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.ROOT}.
   * @param key the key of the property
   * @return the value of the property
   */
  public static String getEnumName(@Nullable Locale locale, String key) {
    return PropertiesFileUtilResolver.getProp(locale, ENUM_NAMES, key);
  }

  /**
   * Returns the localized enum name in enum_names_xxx.properties using the default locale.
   *
   * @param key the key of the property
   * @return the value of the property
   */
  public static String getEnumName(String key) {
    return getEnumName(null, key);
  }

  /**
   * Returns the existence of the key in enum_names_xxx.properties.
   *
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasEnumName(String key) {
    return PropertiesFileUtilResolver.hasProp(ENUM_NAMES, key);
  }

  // === ValidationMessages ===

  /**
   * Returns the localized value in ValidationMessages[_locale].properties.
   *
   * @param locale locale, may be {@code null}
   *     which is treated as {@code Locale.ROOT}.
   * @param key the key of the property
   * @param argMap annotation attribute map used for named placeholder substitution
   * @return the value of the property
   */
  public static String getValidationMessage(@Nullable Locale locale, String key,
      Map<@NonNull String, @Nullable Object> argMap) {
    String template = PropertiesFileUtilResolver.getProp(locale, VALIDATION_MESSAGES, key, argMap);

    return PropertiesFileUtilFormatter.formatWithArgs(template,
        PropertiesFileUtilResolver.resolveArgElementsInMap(locale, argMap));
  }

  /**
   * Returns the value in ValidationMessages[_locale].properties using the default locale.
   *
   * @param key the key of the property
   * @param argMap annotation attribute map used for placeholder substitution
   * @return the value of the property
   */
  public static String getValidationMessage(String key,
      Map<@NonNull String, @Nullable Object> argMap) {
    return getValidationMessage(null, key, argMap);
  }

  /**
   * Returns true when the key exists in ValidationMessages[_xx].properties.
   *
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasValidationMessage(String key) {
    return PropertiesFileUtilResolver.hasProp(VALIDATION_MESSAGES, key);
  }

  // === ValidationMessagesWithItemNames ===

  /**
   * Returns the localized value in ValidationMessagesWithItemNames_xxx.properties.
   *
   * <p>ValidationMessagesWithItemNames[_xx].properties stores messages with {@code {0}},
   *     which is a placeholder for item names.</p>
   *
   * @param locale locale, may be {@code null}
   *     which is treated as {@code Locale.ROOT}.
   * @param key the key of the property
   * @param argMap annotation attribute map used for named placeholder substitution
   * @return the value of the property. Return the key string when the key does not exist.
   */
  public static String getValidationMessageWithItemName(@Nullable Locale locale, String key,
      Map<@NonNull String, @Nullable Object> argMap) {
    String template = PropertiesFileUtilResolver.getProp(locale,
        VALIDATION_MESSAGES_WITH_ITEM_NAMES, key, argMap);

    return PropertiesFileUtilFormatter.formatWithArgs(template,
        PropertiesFileUtilResolver.resolveArgElementsInMap(locale, argMap));
  }

  /**
   * Returns the value in ValidationMessagesWithItemNames_xxx.properties
   * using the default locale.
   *
   * @param key the key of the property
   * @param argMap annotation attribute map used for placeholder substitution
   * @return the value of the property
   */
  public static String getValidationMessageWithItemName(String key,
      Map<@NonNull String, @Nullable Object> argMap) {
    return getValidationMessageWithItemName(null, key, argMap);
  }

  /**
   * Returns true when the key exists in ValidationMessagesWithItemNames[_xx].properties.
   *
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasValidationMessageWithItemName(String key) {
    return PropertiesFileUtilResolver.hasProp(VALIDATION_MESSAGES_WITH_ITEM_NAMES, key);
  }

  // === ValidationMessagesPatternDescriptions ===

  /**
   * Returns the localized value in ValidationMessagesPatternDescriptions[_locale].properties.
   *
   * @param locale locale, may be {@code null}
   *     which is treated as {@code Locale.ROOT}.
   * @param key the key of the property
   * @return the value of the property
   */
  public static String getValidationMessagePatternDescription(@Nullable Locale locale, String key) {
    return PropertiesFileUtilResolver.getProp(locale, VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS,
        key);
  }

  /**
   * Returns the value in ValidationMessagesPatternDescriptions[_locale].properties
   * using the default locale.
   *
   * @param key the key of the property
   * @return the value of the property
   */
  public static String getValidationMessagePatternDescription(String key) {
    return getValidationMessagePatternDescription(null, key);
  }

  /**
   * Returns true when the key exists in ValidationMessagesPatternDescriptions[_xx].properties.
   *
   * @param key the key of the property
   * @return boolean value that shows whether properties has the key
   */
  public static boolean hasValidationMessagePatternDescription(String key) {
    return PropertiesFileUtilResolver.hasProp(VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS, key);
  }

  // === resource bundle setup ===

  /**
   * Adds a postfix to the resource bundle search list dynamically.
   *
   * <p>If you add {@code mymodule} for example,
   *     {@code messages_mymodule[_lang].properties,
   *     application_mymodule[_lang].properties, ...} are also searched.</p>
   *
   * <p>Typical use cases include unit tests and framework modules
   *     (e.g., ecuacion-splib) that bundle their own properties files.</p>
   *
   * <p>In java 9 module system environment, you also need to Service Provider Interface(SPI)
   *     defined in {@code ecuacion-lib-core}.</p>
   *
   * @param postfix postfix
   */
  public static void addResourceBundlePostfix(String postfix) {
    PropertiesFileUtilBundleReader.addToDynamicPostfixList(postfix);
  }

  /**
   * Represents a message argument that can carry an arbitrary {@link Object} value or refer to
   * a message key to be resolved at rendering time.
   *
   * <p>{@link ArgKind#OBJECT} args preserve the original type so that type-aware
   * {@link java.text.MessageFormat} patterns (e.g., {@code {0,number,#,###}}) work correctly.
   * {@link ArgKind#MESSAGE_ID} and {@link ArgKind#FORMATTED_STRING} args resolve to
   * {@link String} via the properties files.</p>
   */
  public static class Arg {

    /**
     * Kinds of {@link Arg}.
     *
     * <p>{@code FORMATTED_STRING} is like {@code "item name is: #{item_names:xxx}"}.</p>
     */
    public enum ArgKind {
      OBJECT, FORMATTED_STRING, MESSAGE_ID
    }

    private final ArgKind argKind;
    private final @NonNull PropertiesFileUtilFileKindEnum[] fileKinds;
    /** Holds the argument value; type depends on {@link ArgKind}. */
    private final @Nullable Object argObject;
    private final @Nullable Object[] messageArgs;

    private Arg(ArgKind argKind, @Nullable Object argObject, @Nullable Object... messageArgs) {
      this.argKind = argKind;
      this.fileKinds = new @NonNull PropertiesFileUtilFileKindEnum[] {};
      this.argObject = argObject;
      this.messageArgs = messageArgs;
    }

    private Arg(@NonNull PropertiesFileUtilFileKindEnum[] fileKinds, String messageId,
        @Nullable Object... messageArgs) {
      this.argKind = ArgKind.MESSAGE_ID;
      this.fileKinds = fileKinds;
      this.argObject = messageId;
      this.messageArgs = messageArgs;
    }

    /**
     * Constructs an {@link ArgKind#OBJECT} arg that holds {@code argObject} as-is.
     *
     * <p>The original type is preserved so that type-aware
     * {@link java.text.MessageFormat} patterns work correctly.
     * If {@code argObject} is {@code null}, the string {@code "null"} is displayed.</p>
     *
     * @param argObject the argument value, may be {@code null}
     * @return Arg
     */
    public static Arg object(@Nullable Object argObject) {
      return new Arg(ArgKind.OBJECT, argObject);
    }

    /**
     * Constructs a {@link ArgKind#FORMATTED_STRING} arg with arguments.
     *
     * <p>{@link Arg} instances and plain {@code Object}s may be freely mixed.
     * Plain objects are wrapped with {@link #object(Object)} automatically
     * when the template is resolved.</p>
     *
     * @param formattedString format template
     * @param args arguments to substitute into the template;
     *     {@link Arg} instances and plain {@code Object}s may be mixed
     * @return Arg
     */
    public static Arg formattedString(String formattedString, @Nullable Object... args) {
      return new Arg(ArgKind.FORMATTED_STRING, formattedString, args);
    }

    /**
     * Constructs a {@link ArgKind#MESSAGE_ID} arg that resolves from
     * {@code messages.properties}.
     *
     * <p>Shorthand for
     * {@link #fromFileKinds(PropertiesFileUtilFileKindEnum[], String, Object...)}
     * with {@code fileKinds} set to {@code [MESSAGES]}.
     * To search other file kinds, use {@link #fromFileKinds} directly.</p>
     *
     * <p>{@link Arg} instances and plain {@code Object}s may be freely mixed.
     * Plain objects are wrapped with {@link #object(Object)} automatically
     * when the message is resolved.</p>
     *
     * @param messageId the message key to resolve from {@code messages.properties}
     * @param args arguments substituted into the resolved message;
     *     {@link Arg} instances and plain {@code Object}s may be mixed
     * @return Arg
     */
    public static Arg message(String messageId, @Nullable Object... args) {
      return fromFileKinds(
          new @NonNull PropertiesFileUtilFileKindEnum[] {PropertiesFileUtilFileKindEnum.MESSAGES},
          messageId, args);
    }

    /**
     * Constructs a {@link ArgKind#MESSAGE_ID} arg with arguments.
     *
     * <p>{@link Arg} instances and plain {@code Object}s may be freely mixed.
     * Plain objects are wrapped with {@link #object(Object)} automatically
     * when the message is resolved.</p>
     *
     * @param fileKinds file kinds to search
     * @param messageId the key to resolve
     * @param args arguments substituted into the resolved message;
     *     {@link Arg} instances and plain {@code Object}s may be mixed
     * @return Arg
     */
    public static Arg fromFileKinds(@NonNull PropertiesFileUtilFileKindEnum[] fileKinds,
        String messageId, @Nullable Object... args) {
      return new Arg(fileKinds, messageId, args);
    }

    /** Returns the argument kind. */
    public ArgKind getArgKind() {
      return argKind;
    }

    /**
     * Returns the file kinds used for resolution.
     * Meaningful only for {@link ArgKind#MESSAGE_ID} args;
     * returns an empty array for other kinds.
     */
    public @NonNull PropertiesFileUtilFileKindEnum[] getFileKinds() {
      return fileKinds;
    }

    /**
     * Returns the raw argument value, preserving the original type.
     *
     * <p>Meaningful only for {@link ArgKind#OBJECT} args;
     * for other kinds, the stored value is an internal representation.
     * May be {@code null} if {@link #object(Object)} was called with {@code null}.</p>
     */
    public @Nullable Object getArgValue() {
      return argObject;
    }

    /**
     * Returns the nested args used for substitution.
     * Elements may be {@link Arg} instances or plain {@code Object}s.
     *
     * <p>Meaningful only for {@link ArgKind#MESSAGE_ID} and
     * {@link ArgKind#FORMATTED_STRING} args;
     * returns an empty array for {@link ArgKind#OBJECT} args.</p>
     */
    public @Nullable Object[] getMessageArgs() {
      return messageArgs;
    }

    /**
     * Resolves this {@code Arg} to a {@link String} as if the pattern were {@code "{0}"}.
     *
     * <p>Intended for use cases where an {@link Arg} is rendered as a standalone string
     *     (e.g., a message prefix) rather than as part of a
     *     {@link java.text.MessageFormat} pattern.</p>
     *
     * @param locale locale, may be {@code null} which means no {@code Locale} specified.
     * @return the string representation of this resolved {@code Arg}
     */
    public String resolveAsString(@Nullable Locale locale) {
      return PropertiesFileUtilResolver.resolveArgAsString(locale, this);
    }

    /**
     * Resolves this {@code Arg} to a {@link String} using the default locale.
     *
     * <p>Equivalent to {@link #resolveAsString(Locale) resolveAsString(null)}.</p>
     *
     * @return the string representation of this resolved {@code Arg}
     */
    public String resolveAsString() {
      return resolveAsString(null);
    }
  }

}
