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
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Stores properties extracted from .properties files.
 * 
 * <ul>
 * <li>One instance stores one kind (like "messages") with multiple locales.<br>
 *     It also stores messages files in multiple modules.</li>
 * </ul>
 */
public class PropertiesFileUtilValueGetter {

  private boolean throwsExceptionWhenKeyDoesNotExist;

  /**
   * Is used for test to use filePrefix which is not included in PropertiesFileUtilPropFileKindEnum.
   */
  private String[][] filePrefixes;

  private static final String[] LIB_MODULES = new String[] {"core", "jpa", "validation"};
  private static final String[] SPLIB_MODULES = new String[] {"core", "web", "web_jpa"};
  private static final String[] UTIL_MODULES = new String[] {"jpa", "poi"};

  private static final String[] APP_MODULES =
      new String[] {"", "base", "core", "core_web", "core_batch"};
  private static final String[] APP_ENVS = new String[] {"", "profile"};

  private static final List<String> dynamicPostfixList = new ArrayList<>();

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
   * Constructs a new instance with {@code PropertiesFileUtilPropFileKindEnum}.
   * 
   * @param fileKindEnum fileKindEnum
   */
  public PropertiesFileUtilValueGetter(PropertiesFileUtilFileKindEnum fileKindEnum) {
    this.filePrefixes = ObjectsUtil.requireNonNull(fileKindEnum).getActualFilePrefixes();
    this.throwsExceptionWhenKeyDoesNotExist = fileKindEnum.throwsExceptionWhenKeyDoesNotExist();
  }

  /**
   * Is used for test. It can be accessed from the same package.
   * It is used to accept non-PropFileKindEnum file prefix.
   */
  PropertiesFileUtilValueGetter(String[][] filePrefixes) {
    this.filePrefixes = ObjectsUtil.requireNonNull(filePrefixes);
    throwsExceptionWhenKeyDoesNotExist = true;
  }

  /**
   * Obtains a list of postfixes.
   * 
   * @return postfix list
   */
  List<String> getPostfixes() {
    List<String> rtnList = new ArrayList<>();
    rtnList.addAll(
        Arrays.asList(LIB_MODULES).stream().map(str -> "_lib_" + str).collect(Collectors.toList()));
    rtnList.addAll(Arrays.asList(SPLIB_MODULES).stream().map(str -> "_splib_" + str)
        .collect(Collectors.toList()));
    rtnList.addAll(Arrays.asList(UTIL_MODULES).stream().map(str -> "_util_" + str).toList());
    rtnList.addAll(dynamicPostfixList.stream().map(str -> "_" + str).toList());

    // APP_MODULES are combined to APP_ENVS
    for (String moduleName : APP_MODULES) {
      for (String envName : APP_ENVS) {
        // Add "-" and "_"
        rtnList.add((moduleName.equals("") ? "" : "_" + moduleName)
            + (envName.equals("") ? "" : "-" + envName));
      }
    }

    return rtnList;
  }

  /*
   * Obtains value from key.
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   */
  private String getValue(@Nullable Locale locale, String key,
      Map<@NonNull String, @Nullable Object> elParameterMap) {
    ObjectsUtil.requireNonNull(key);

    String str = getRawValue(locale, key);

    return PropertiesFileUtil.analyzedValueString(locale, str, elParameterMap);
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
  private String getRawValue(@Nullable Locale locale, String key) {
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

  /*
   * Obtains value from key and locale by reading multiple properties files 
   *     with prefixes and postfixes of the filename.
   * 
   * <p>The duplicate check of the key is also executed here.</p>
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   */
  private @Nullable String getValueFromPropertiesFilesWithSamePriority(@Nullable Locale locale,
      String key, String[] filePrefixesOfSamePriority) {
    // Search the key in properties files.
    List<String> postfixes = getPostfixes();
    Map<String, ResourceBundle> rbMap = new HashMap<>();

    for (String prefix : filePrefixesOfSamePriority) {
      for (int i = 0; i < postfixes.size(); i++) {
        String postfix = postfixes.get(i);
        String filename = prefix + postfix;

        ResourceBundle bundle = getResourceBundle(filename, locale);
        rbMap.put(filename, bundle);
      }
    }

    String valueNonDefault = getValueAndDuplicationCheck(rbMap, key);
    String valueDefault = getValueAndDuplicationCheck(rbMap, key + ".default");

    return valueNonDefault != null ? valueNonDefault : valueDefault;
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
  }

  private @Nullable String getValueAndDuplicationCheck(
      Map<String, ResourceBundle> resourceBundleMap, String key) {
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

  /*
   * Checks if the properties file has the key.
   */
  public boolean hasProp(String key) {
    Objects.requireNonNull(key);

    try {
      getRawValue(null, key);
      return true;

    } catch (NoKeyInPropertiesFileException ex) {
      return false;
    }
  }

  /*
   * Checks if the properties file has the key.
   */
  public boolean hasProp(@Nullable Locale locale, String key) {
    Objects.requireNonNull(key);

    try {
      getRawValue(locale, key);
      return true;

    } catch (NoKeyInPropertiesFileException ex) {
      return false;
    }
  }

  /*
   * Obtains value from a key. 
   */
  public String getProp(String key,
      @Nullable Map<@NonNull String, @Nullable Object> elParameterMap) {
    return getProp(null, key, elParameterMap);
  }

  /*
   * Obtains value from a key. 
   * 
   * @param locale locale, may be {@code null} 
   *     which means no {@code Locale} specified.
   * @param key the key of the property
   */
  public String getProp(@Nullable Locale locale, String key,
      @Nullable Map<@NonNull String, @Nullable Object> elParameterMap) {
    ObjectsUtil.requireNonNull(key);

    // Throw an exception when msgId is empty.
    if (StringUtils.isEmpty(key)) {
      throw new RuntimeException("Message ID is blank.");
    }

    try {
      return getValue(locale, key, elParameterMap == null ? new HashMap<>() : elParameterMap);

    } catch (NoKeyInPropertiesFileException ex) {
      if (throwsExceptionWhenKeyDoesNotExist) {
        throw ex;

      } else {
        return key;
      }
    }
  }

  public static class NoKeyInPropertiesFileException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoKeyInPropertiesFileException(String key) {
      super("No key in .properties. key: " + key);
    }
  }

  public static class KeyDupliccatedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public KeyDupliccatedException(String key) {
      super("Duplicated key in .properties. key: " + key);
    }
  }
}
