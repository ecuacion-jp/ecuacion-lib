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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;

/**
 * Provivdes utility methods for ecuacion library enums.
 * 
 * <p>In apps with ecuacion library the extended enum format is used. 
 *     The format of enums created by code-generator are also that kind.<br>
 *     It has following features:</p>
 * 
 * <ol>
 * <li>To have {@code code} property which is used 
 *     for saving the value in database, identifing html dropdown menu elements and others.</li>
 * <li>To have {@code display name} for each enum value, 
 *     which is obtained by {@code PropertyFFileUtil.getEnumName()}.</li>
 * </ol>
 * 
 * <p>Since enums have these features, EnumUtils from apache-commons-lang is not used 
 *     in the library (which does not mean its use is banned.)</p>
 */
public class EnumUtil {

  /**
  * Prevents to create an instance.
  */
  private EnumUtil() {}

  /**
  * Obtains the enum value from the code.
  *
  * <p>The return value may be null when the code is null.<br>
  * When the code is not null and the enum value corresponding to the code is not found,
  * throw RuntimeExceptionWithMessageId.</p>
  *
  * @param <T> any enum class
  * @param enumClass enum class
  * @param code code
  * @return the enum value
  */
  @Nonnull
  public static <T> T getEnumFromCode(@RequireNonnull Class<T> enumClass,
      @RequireNonnull String code) {
    ObjectsUtil.paramRequireNonNull(enumClass);
    ObjectsUtil.paramRequireNonNull(code);

    if (!enumClass.isEnum()) {
      throw new IllegalArgumentException();
    }

    if (code == null) {
      return null;
    }

    for (EnumValueInfo<T> enumValue : getEnumInfo(enumClass).valueList) {
      if (enumValue.getCode().equals(code)) {
        return enumValue.getInstance();
      }
    }

    // ここまで来てしまうということは、存在しないコードを設定してしまったということ。
    throw new EclibRuntimeException(
        "Enum: " + enumClass.getSimpleName() + "doesn't have the code. (code : " + code + ")");
  }

  /**
   * Returns true if the argument code exists in the enum class.
   * 
   * @param <T> any enum class
   * @param enumClass enum class
   * @param code code
   * @return enum value
   */
  public static <T> boolean hasEnumFromCode(@RequireNonnull Class<T> enumClass,
      @RequireNonnull String code) {

    try {
      T anEnum = getEnumFromCode(ObjectsUtil.paramRequireNonNull(enumClass),
          ObjectsUtil.paramRequireNonNull(code));
      return anEnum != null;

    } catch (RuntimeException ex) {
      return false;
    }
  }

  /**
   * Returns paires of a code and a display name. 
   * This is mainly used for the dropdown item in html or other UI windows.
   * 
   * <p>You can designate an option to filter the elements in the enum class.<br>
   * You can set options as follows:</p>
   * 
   * <table>
   * <caption>kinds of options</caption>
   * <tr>
   * <th>option</th>
   * <th>description</th>
   * </tr>
   * <tr><td>including=value1|value2</td>
   * <td>"value1", "value2" are names of the elements of the enum. 
   *     Multiple selections of values available</td></tr>
   * <tr><td>excluding=value1|value2</td><td>Multiple selections of values available</td></tr>
   * <tr><td>firstCharOfCodeEqualTo=3|4</td><td>"3", "4" is codes of the elements of the enum. 
   *     Multiple selections of values available</td></tr>
   * <tr><td>firstCharOfCodeLessThanOrEqualTo=3</td>
   * <td>"3", "4" are the first character of codes of the elements of the enum. 
   *     Multiple selections of values available</td></tr>
   * <tr><td>firstCharOfCodeGreaterThanOrEqualTo=3</td>
   * <td>"3", "4" are the first character of codes of the elements of the enum. 
   *     Multiple selections of values available</td></tr>
   * </table>
   * 
   * <p>You can use only 1 option from the above. you cannot use multiple options at once. 
   *     "|" is the separator of values.</p>
   */
  @Nonnull
  public static <T> List<String[]> getListForHtmlSelect(@RequireNonnull Class<T> enumClass,
      @Nullable Locale locale, @Nullable String optionsString) {
    optionsString = (optionsString == null) ? "" : optionsString;
    String[] options = optionsString.split(",");
    EnumClassInfo<T> enumInfo = getEnumInfo(enumClass);

    // optionKey, optionValueを格納したmapを作成
    Map<String, String> optionMap = new HashMap<>();
    for (String option : options) {
      String optionKey = option.split("=")[0];
      String optionValue = option.split("=").length == 1 ? null : option.split("=")[1];

      // ListForHtmlSelectOptionEnumに存在するkeyのみを対象とし、それをMapに格納
      if (ListForHtmlSelectOptionEnum.getNameSet().contains(optionKey)) {
        optionMap.put(optionKey, optionValue);
      }
    }

    List<String[]> rtnList = new ArrayList<>();

    if (optionMap.keySet().size() > 1) {
      // 複数のoptionを設定した場合はエラー
      throw new RuntimeException(
          "Multiple options cannot be set. (" + optionMap.keySet().toString() + ")");
    }

    if (optionMap.keySet().size() == 0) {
      // optionがないので全てを追加して終了
      enumInfo.getValueList().stream()
          .forEach(value -> rtnList.add(new String[] {value.getCode(), value.getLabel()}));
      return rtnList;
    }

    // 以下、optionMap.keySet().size() == 1の場合。
    String optionKey = optionMap.keySet().stream().toList().get(0);
    String optionValue = optionMap.get(optionKey);

    for (EnumValueInfo<T> value : enumInfo.getValueList()) {
      if ((optionKey.equals("including")
          && Arrays.asList(optionValue.split("\\|")).contains(value.getName()))
          || (optionKey.equals("excluding")
              && !Arrays.asList(optionValue.split("\\|")).contains(value.getName()))
          || (optionKey.equals("firstCharOfCodeEqualTo")
              && Arrays.asList(optionValue.split("\\|")).contains(value.getCode().substring(0, 1)))
          || (optionKey.equals("firstCharOfCodeLessThanOrEqualTo") && value.getCode()
              .substring(0, 1).getBytes(StandardCharsets.US_ASCII)[0] <= optionValue
                  .getBytes(StandardCharsets.US_ASCII)[0])
          || (optionKey.equals("firstCharOfCodeGreaterThanOrEqualTo") && value.getCode()
              .substring(0, 1).getBytes(StandardCharsets.US_ASCII)[0] >= optionValue
                  .getBytes(StandardCharsets.US_ASCII)[0])) {

        rtnList.add(new String[] {value.getCode(), value.getLabel()});
      }
    }

    return rtnList;
  }

  /**
   * Returns enum class info. {@code Locale.getDefault()} is used for display name of the enum.
   * 
   * @param <T> any enum
   * @param enumClass enum class
   * @return EnumClassInfo
   */
  @Nonnull
  public static <T> EnumUtil.EnumClassInfo<T> getEnumInfo(@RequireNonnull Class<T> enumClass) {
    return getEnumInfo(enumClass, null);
  }

  /**
   * Returns enum class info.
   * 
   * @param <T> any enum
   * @param enumClass enum class
   * @param locale locale
   * @return EnumClassInfo 
   */
  @Nonnull
  public static <T> EnumUtil.EnumClassInfo<T> getEnumInfo(@RequireNonnull Class<T> enumClass,
      @Nullable Locale locale) {
    ObjectsUtil.paramRequireNonNull(enumClass);
    locale = locale == null ? Locale.getDefault() : locale;
    
    List<EnumUtil.EnumValueInfo<T>> valueList = new ArrayList<>();
    String enumClassName = enumClass.getSimpleName();

    if (!enumClass.isEnum()) {
      throw new IllegalArgumentException();
    }

    for (T enumValue : enumClass.getEnumConstants()) {
      String name = enumValue.toString();

      try {
        Method codeMethod = enumClass.getMethod("getCode", (Class<?>[]) null);
        String code = (String) codeMethod.invoke(enumValue, (Object[]) null);

        Method displayNameMethod =
            enumClass.getMethod("getDisplayName", (Class<?>[]) new Class<?>[] {Locale.class});
        String displayName = (String) displayNameMethod.invoke(enumValue, locale);

        valueList
            .add(new EnumUtil.EnumValueInfo<T>(name, code, displayName, enumValue, enumClassName));

      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }

    return new EnumUtil.EnumClassInfo<T>(enumClass.getSimpleName(), valueList);
  }

  private static enum ListForHtmlSelectOptionEnum {
    // eclipse auto-formatがこれを1行にし100文字超え警告が出るのであえて一行話しておく
    including, excluding, firstCharOfCodeEqualTo,

    firstCharOfCodeLessThanOrEqualTo, firstCharOfCodeGreaterThanOrEqualTo;

    public static Set<String> getNameSet() {
      Set<String> rtnSet = new HashSet<>();
      for (ListForHtmlSelectOptionEnum anEnum : ListForHtmlSelectOptionEnum.values()) {
        rtnSet.add(anEnum.name());
      }

      return rtnSet;
    }
  }

  /**
   * Contains Enum Class info.
   */
  public static class EnumClassInfo<T> {
    private String enumClassName;
    private List<EnumValueInfo<T>> valueList;

    /**
     * Constructs a new instance.
     * 
     * @param enumClassName the name of the enum class
     * @param valueList the list of the enum elements
     */
    public EnumClassInfo(String enumClassName, List<EnumValueInfo<T>> valueList) {
      this.enumClassName = enumClassName;
      this.valueList = valueList;
    }

    public String getEnumClassName() {
      return enumClassName;
    }

    public List<EnumValueInfo<T>> getValueList() {
      return valueList;
    }
  }

  /**
   * Contains Enum value info.
   */
  public static class EnumValueInfo<T> {
    private String name;
    private String code;
    private String displayName;
    private T instance;

    private String enumClassName;

    /** 
     * Constructs a new instance.
     * 
     * @param name the name of the enum element
     * @param code the code of the enum element
     * @param label the display name of the enum element
     * @param instance the instance of the enum value
     * @param enumClassName the name of the enum class
     */
    public EnumValueInfo(String name, String code, String label, T instance, String enumClassName) {
      this.name = name;
      this.code = code;
      this.displayName = label;
      this.instance = instance;
      this.enumClassName = enumClassName;
    }

    public String getName() {
      return name;
    }

    public String getCode() {
      return code;
    }

    public String getLabel() {
      return displayName;
    }

    public T getInstance() {
      return instance;
    }

    public String getEnumClassName() {
      return enumClassName;
    }
  }
}
