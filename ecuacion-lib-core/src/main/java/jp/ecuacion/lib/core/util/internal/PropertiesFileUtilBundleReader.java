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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Reads raw property values from {@code *.properties} files, wrapping
 * {@link java.util.ResourceBundle}.
 *
 * <p>One instance covers one file kind (e.g., {@code messages}) across all locales
 * and all modules. Returned values are raw strings — no {@code #{...}} or
 * {@code ${...}} processing is applied here.</p>
 */
public class PropertiesFileUtilBundleReader {

  private final boolean throwsExceptionWhenKeyDoesNotExist;

  /**
   * Is used for test to use filePrefix which is not included in
   * PropertiesFileUtilPropFileKindEnum.
   */
  private final String[][] filePrefixes;

  private static final String[] LIB_MODULES =
      new String[] {"core", "jpa", "validation", "validation_business_messages"};
  private static final String[] SPLIB_MODULES = new String[] {"core", "web", "web_jpa"};
  private static final String[] UTIL_MODULES = new String[] {"excel_table", "excel_report_to_pdf"};

  private static final String[] APP_MODULES =
      new String[] {"", "base", "core", "core_web", "core_batch"};
  private static final String[] APP_ENVS = new String[] {"", "profile"};

  private static final List<@NonNull String> dynamicPostfixList = new ArrayList<>();

  /**
   * Offers a way to add postfixes dynamically.
   *
   * <p>By adding "postfix" using this method,
   *     "application_postfix.properties" are added to the file list.</p>
   *
   * @param postfix postfix
   */
  public static void addToDynamicPostfixList(String postfix) {
    if (!dynamicPostfixList.contains(postfix)) {
      dynamicPostfixList.add(postfix);
    }
  }

  /*
   * Is accessible only from the same package for unit test.
   */
  static List<@NonNull String> getDynamicPostfixList() {
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
   * Constructs a new instance with {@code PropertiesFileUtilPropFileKindEnum}.
   *
   * @param fileKindEnum fileKindEnum
   */
  public PropertiesFileUtilBundleReader(PropertiesFileUtilFileKindEnum fileKindEnum) {
    this.filePrefixes = ObjectsUtil.requireNonNull(fileKindEnum).getActualFilePrefixes();
    this.throwsExceptionWhenKeyDoesNotExist = fileKindEnum.throwsExceptionWhenKeyDoesNotExist();
  }

  /**
   * Is used for test. It can be accessed from the same package.
   * It is used to accept non-PropFileKindEnum file prefix.
   */
  PropertiesFileUtilBundleReader(String[][] filePrefixes) {
    this.filePrefixes = ObjectsUtil.requireNonNull(filePrefixes);
    throwsExceptionWhenKeyDoesNotExist = true;
  }

  /**
   * Obtains a list of postfixes.
   *
   * @return postfix list
   */
  List<@NonNull String> getPostfixes() {
    List<@NonNull String> rtnList = new ArrayList<>();
    rtnList.addAll(Arrays.stream(LIB_MODULES).map(str -> "_lib_" + str).toList());
    rtnList.addAll(Arrays.stream(SPLIB_MODULES).map(str -> "_splib_" + str).toList());
    rtnList.addAll(Arrays.stream(UTIL_MODULES).map(str -> "_util_" + str).toList());
    rtnList.addAll(dynamicPostfixList.stream().map(str -> "_" + str).toList());

    // APP_MODULES are combined to APP_ENVS
    for (String moduleName : APP_MODULES) {
      for (String envName : APP_ENVS) {
        // Add "-" and "_"
        rtnList.add((moduleName.isEmpty() ? "" : "_" + moduleName)
            + (envName.isEmpty() ? "" : "-" + envName));
      }
    }

    return rtnList;
  }

  /**
   * Obtains data from properties file or environment variable if exists.
   *
   * <p>Raw means return data is not processed after obtained from properties file.</p>
   *
   * <p>This is also used to find out whether the key exists.</p>
   *
   * @param locale locale
   * @param key key
   * @return raw value
   */
  String getValue(@Nullable Locale locale, String key) {
    String value;
    if (System.getProperties().keySet().contains(key)) {
      // If the key is in System.getProperties(), just return it.
      String tmpValue = System.getProperties().getProperty(key);
      value = tmpValue == null ? "" : tmpValue;

    } else {
      value = getValueFromPropertiesFiles(locale, key);
    }

    return value;
  }

  private String getValueFromPropertiesFiles(@Nullable Locale locale, String key) {
    for (String[] filePrefixesOfSamePriority : filePrefixes) {
      String value =
          getValueFromPropertiesFilesWithSamePriority(locale, key, filePrefixesOfSamePriority);

      if (value != null) {
        return value;
      }
    }

    // The program reaches here means key not exist in properties files.
    throw new NoKeyInPropertiesFileException(key);
  }

  /**
   * Obtains value from key and locale by reading multiple properties files
   * with prefixes and postfixes of the filename.
   *
   * <p>The duplicate check of the key is also executed here.</p>
   *
   * <p>Key lookup priority (highest to lowest):
   * <ol>
   *   <li>{@code key} — application-level override</li>
   *   <li>{@code key.default} — optional module override
   *       (e.g., business-messages module)</li>
   *   <li>{@code key.base} — library-level fallback (e.g., ecuacion-lib-core)</li>
   * </ol>
   * </p>
   *
   * @param locale locale, may be {@code null} which means no {@code Locale} specified.
   * @param key the key of the property
   * @param filePrefixesOfSamePriority filePrefixesOfSamePriority
   * @return value
   */
  private @Nullable String getValueFromPropertiesFilesWithSamePriority(@Nullable Locale locale,
      String key, String[] filePrefixesOfSamePriority) {
    // Search the key in properties files.
    List<@NonNull String> postfixes = getPostfixes();
    Map<String, @Nullable ResourceBundle> rbMap = new HashMap<>();

    for (String prefix : filePrefixesOfSamePriority) {
      for (int i = 0; i < postfixes.size(); i++) {
        String postfix = postfixes.get(i);
        String filename = prefix + postfix;

        @Nullable ResourceBundle bundle = getResourceBundle(filename, locale);
        rbMap.put(filename, bundle);
      }
    }

    String valueNonDefault = getValueAndDuplicationCheck(rbMap, key);
    String valueDefault = getValueAndDuplicationCheck(rbMap, key + ".default");
    String valueBase = getValueAndDuplicationCheck(rbMap, key + ".base");

    return valueNonDefault != null ? valueNonDefault
        : valueDefault != null ? valueDefault
        : valueBase;
  }

  /**
   * Reads a property file and returns its {@code ResourceBundle}.<br>
   * Returns {@code null} when a resource bundle is not found.
   *
   * @param bundleId resource bundle's bundle ID
   * @param locale locale, may be {@code null}
   *     which means no {@code Locale} specified.
   */
  private @Nullable ResourceBundle getResourceBundle(String bundleId, @Nullable Locale locale) {

    ObjectsUtil.requireNonNull(bundleId);

    if (locale == null) {
      locale = Locale.ROOT;
    }

    try {
      bundleNameForModule.set(bundleId);
      specifiedLocale.set(locale);

      // java 9 module system
      try {
        String bundle = "jp.ecuacion.lib.core."
            + StringUtil.getUpperCamelFromSnake(bundleId.replaceAll("-", "_"));
        return ResourceBundle.getBundle(bundle, locale);

      } catch (MissingResourceException ex) {
        // do nothing.
      }

      // java 9 module system for test
      try {
        String bundle = "jp.ecuacion.lib.core.test."
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

    } finally {
      bundleNameForModule.remove();
      specifiedLocale.remove();
    }
  }

  private @Nullable String getValueAndDuplicationCheck(
      Map<String, @Nullable ResourceBundle> resourceBundleMap, String key) {
    String messageString = null;
    for (Entry<String, @Nullable ResourceBundle> entry : resourceBundleMap.entrySet()) {

      if (entry.getValue() == null) {
        continue;
      }

      ResourceBundle nonNullRb = Objects.requireNonNull(entry.getValue());

      if (nonNullRb.containsKey(key)) {
        if (messageString != null) {
          throw new KeyDuplicatedException(key);
        }

        messageString = nonNullRb.getString(key);
      }
    }

    return messageString;
  }

  /**
   * Returns {@code true} when the key exists in any properties file (locale-independent).
   *
   * @param key the key to check
   * @return {@code true} if the key exists
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

  /**
   * Returns {@code true} when the key exists in a properties file for the given locale.
   *
   * @param locale locale, may be {@code null} which is treated as {@code Locale.ROOT}
   * @param key the key to check
   * @return {@code true} if the key exists for the given locale
   */
  public boolean hasProp(@Nullable Locale locale, String key) {
    Objects.requireNonNull(key);

    try {
      getValue(locale, key);
      return true;

    } catch (NoKeyInPropertiesFileException ex) {
      return false;
    }
  }

  /**
   * Obtains raw value from a key using the default locale
   * (no {@code #{...}} or {@code ${...}} processing).
   *
   * @param key the key of the property
   * @return raw value
   */
  public String getProp(String key) {
    return getProp(null, key);
  }

  /**
   * Obtains raw value from a key (no {@code #{...}} or {@code ${...}} processing).
   *
   * @param locale locale, may be {@code null} which means no {@code Locale} specified.
   * @param key the key of the property
   * @return raw value
   */
  public String getProp(@Nullable Locale locale, String key) {
    ObjectsUtil.requireNonNull(key);

    if (StringUtils.isEmpty(key)) {
      throw new RuntimeException("Message ID is blank.");
    }

    try {
      return getValue(locale, key);

    } catch (NoKeyInPropertiesFileException ex) {
      if (throwsExceptionWhenKeyDoesNotExist) {
        throw ex;

      } else {
        return key;
      }
    }
  }

  /** Thrown when the requested key is not found in any properties file. */
  public static class NoKeyInPropertiesFileException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Constructs the exception with the missing key name. */
    public NoKeyInPropertiesFileException(String key) {
      super("No key in .properties. key: " + key);
    }
  }

  /** Thrown when the same key appears in more than one properties file. */
  public static class KeyDuplicatedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Constructs the exception with the duplicated key name. */
    public KeyDuplicatedException(String key) {
      super("Duplicated key in .properties. key: " + key);
    }
  }
}
