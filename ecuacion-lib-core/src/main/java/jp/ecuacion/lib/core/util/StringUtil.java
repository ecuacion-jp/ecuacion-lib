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
import java.text.NumberFormat;
import java.util.Collection;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides string-related utility methods.
 * 
 * <ul>
 * <li>Methods which StringUtils in apache-commons-lang has are not implemented in this class.
 *     Use StringUtils.</li>
 * <li>The way of implementation follows to the one of apache-commons-lang.</li>
 * </ul>
 */
public class StringUtil {

  /**
   * Prevents other classes from instantiating it.
   */
  private StringUtil() {}

  /**
   * Returns a lowerCamelCase string from a snake case string.
   * 
   * @param snakeCaseString snakeCaseString
   * @return camel case string
   */
  @Nonnull
  public static String getLowerCamelFromSnake(@RequireNonnull String snakeCaseString) {
    ObjectsUtil.requireNonNull(snakeCaseString);

    // Throw an exception if "_" exsits at the start or end because it means it's not a snake case.
    if (snakeCaseString.startsWith("_")) {
      throw new EclibRuntimeException(
          "snake-case string cannot start with '_'. (argment string: '" + snakeCaseString + "')");
    }

    if (snakeCaseString.endsWith("_")) {
      throw new EclibRuntimeException(
          "snake-case string cannot end with '_'. (argment string: '" + snakeCaseString + "')");
    }

    // Throw an exception if continuous '_' exists.
    if (snakeCaseString.contains("__")) {
      throw new EclibRuntimeException("snake-case strings are not supposed to have '__' "
          + "(double underscores). (argument string: '" + snakeCaseString + "')");
    }

    // snake string can be like "validation_messages_ja", "VALIDATION_MESSAGES_JA" and
    // "ValidationMessages_ja".
    // Lower camel strings for these are supposed to be "validationMessagesJa",
    // "validationMessagesJa" and "validationMessagesJa".
    // To realize the transformation of 2nd one, argument.toLowerCase() is needed
    // but it makes "validationmessagesJa" (m is changed to lowercase).
    // It's not good so only the strings without lower case alphabets are changed to lower case.
    Pattern pattern = Pattern.compile("^[A-Z0-9_]*$");
    if (pattern.matcher(snakeCaseString).find()) {
      snakeCaseString = snakeCaseString.toLowerCase();
    }

    String lowStr = StringUtils.uncapitalize(snakeCaseString);
    while (lowStr.indexOf("_") >= 0) {
      int firstUsPos = lowStr.indexOf("_");
      lowStr = lowStr.substring(0, firstUsPos)
          + lowStr.substring(firstUsPos + 1, firstUsPos + 2).toUpperCase()
          + lowStr.substring(firstUsPos + 2);
    }

    return lowStr;
  }

  /**
   * Returns a upperCamelCase string from a snake case string.
   * 
   * @param snakeCaseString snakeCaseString
   * @return camel case string
   */
  @Nonnull
  public static String getUpperCamelFromSnake(@RequireNonnull String snakeCaseString) {
    return StringUtils.capitalize(getLowerCamelFromSnake(snakeCaseString));
  }

  /**
   * Returns a lowerSnakeCase string from a camel case string.
   * 
   * @param camelCaseString snakeCaseString, may be null.
   * @return camel case string, may be null when camelCaseString is null.
   */
  @Nonnull
  public static String getLowerSnakeFromCamel(@RequireNonnull String camelCaseString) {
    ObjectsUtil.requireNonNull(camelCaseString);

    // uncapitalized
    camelCaseString = StringUtils.uncapitalize(camelCaseString);

    char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    for (char c : chars) {
      String s = String.valueOf(c);
      camelCaseString = camelCaseString.replaceAll(s, "_" + s);
    }

    return camelCaseString;
  }

  /* ■□■□ number ■□■□ */

  /**
   * Returns comma-separated number from number.
   *
   * @param number number
   * @return String comma-separated number.
   * @throws NumberFormatException NumberFormatException.
   */
  @Nonnull
  public static String toCurrencyFormat(@RequireNonnull String number) {
    ObjectsUtil.requireNonNull(number);

    NumberFormat formatter = NumberFormat.getNumberInstance();
    return formatter.format(Integer.valueOf(number));
  }

  /* ■□■□ separated values ■□■□ */

  /**
   * Returns String with values separated by {@code separator}.
   * 
   * <p>If you set "," as {@code separator}, you'll get csv String.</p>
   * 
   * @param array string array
   * @param separator separator string
   * @return String
   */
  @Nonnull
  public static String getSeparatedValuesString(@RequireNonnull String[] array,
      @RequireNonnull String separator, String leftHandSideEnclosedBy,
      String rightHandSideEnclosedBy, boolean firstElemnetUncapitalized) {
    ObjectsUtil.requireNonNull(array, separator, leftHandSideEnclosedBy, rightHandSideEnclosedBy);

    boolean is1stTime = true;
    StringBuilder sb = new StringBuilder();
    for (String value : array) {
      if (is1stTime) {
        is1stTime = false;

      } else {
        sb.append(separator);
      }

      sb.append(leftHandSideEnclosedBy + (is1stTime ? StringUtils.uncapitalize(value) : value)
          + rightHandSideEnclosedBy);
    }

    return sb.toString();
  }

  /**
   * Returns String with values separated by {@code separator}.
   * 
   * <p>If you set "," as {@code separator}, you'll get csv String.</p>
   * 
   * @param collection string collection
   * @param separator separator string
   * @return String
   */
  @Nonnull
  public static String getSeparatedValuesString(@RequireNonnull Collection<String> collection,
      @RequireNonnull String separator, String leftHandSideEnclosedBy,
      String rightHandSideEnclosedBy, boolean firstElemnetUncapitalized) {
    return getSeparatedValuesString(collection.toArray(new String[collection.size()]), separator,
        leftHandSideEnclosedBy, rightHandSideEnclosedBy, firstElemnetUncapitalized);
  }

  /**
   * Returns String with values separated by {@code separator}.
   * 
   * <p>If you set "," as {@code separator}, you'll get csv String.</p>
   * 
   * @param array string array
   * @param separator separator string
   * @return String
   */
  @Nonnull
  public static String getSeparatedValuesString(@RequireNonnull String[] array,
      @RequireNonnull String separator, String elementEnclosedBy,
      boolean firstElemnetUncapitalized) {
    ObjectsUtil.requireNonNull(array, separator);

    boolean is1stTime = true;
    StringBuilder sb = new StringBuilder();
    for (String value : array) {
      if (is1stTime) {
        is1stTime = false;

      } else {
        sb.append(separator);
      }

      String enclosedBy = elementEnclosedBy == null ? "" : elementEnclosedBy;
      sb.append(enclosedBy + (is1stTime ? StringUtils.uncapitalize(value) : value) + enclosedBy);
    }

    return sb.toString();
  }

  /**
   * Returns String with values separated by {@code separator}.
   * 
   * <p>If you set "," as {@code separator}, you'll get csv String.</p>
   * 
   * @param collection string collection
   * @param separator separator string
   * @return String
   */
  @Nonnull
  public static String getSeparatedValuesString(@RequireNonnull Collection<String> collection,
      @RequireNonnull String separator, String elementEnclosedBy,
      boolean firstElemnetUncapitalized) {
    return getSeparatedValuesString(collection.toArray(new String[collection.size()]), separator,
        elementEnclosedBy, firstElemnetUncapitalized);
  }

  /**
   * Returns String with values separated by {@code separator}.
   * 
   * <p>If you set "," as {@code separator}, you'll get csv String.</p>
   * 
   * @param array string array
   * @param separator separator string
   * @return String
   */
  @Nonnull
  public static String getSeparatedValuesString(@RequireNonnull String[] array,
      @RequireNonnull String separator) {
    return getSeparatedValuesString(array, separator, null, false);
  }


  /**
   * Returns String with values separated by {@code separator}.
   * 
   * <p>If you set "," as {@code separator}, you'll get csv String.</p>
   * 
   * @param collection string collection
   * @return String
   */
  @Nonnull
  public static String getSeparatedValuesString(@RequireNonnull Collection<String> collection,
      @RequireNonnull String separator) {
    return getSeparatedValuesString(collection.toArray(new String[collection.size()]), separator);
  }

  /**
   * Returns csv from string array.
   * 
   * @param array string array
   * @return csv
   */
  @Nonnull
  public static String getCsv(@Nonnull String... array) {
    return getSeparatedValuesString(array, ",");
  }

  /**
   * Returns csv from list of string.
   * 
   * @param collection collection of string.
   * @return csv.
   */
  @Nonnull
  public static String getCsv(@RequireNonnull Collection<String> collection) {
    return getCsv(collection.toArray(new String[collection.size()]));
  }

  /**
   * Returns csv with spaces after commas from list of string.
   * 
   * <p>This is used not for creating csv file, but for logging or program code creation.</p>
   * 
   * @param array list of string.
   * @return csv with spaces after commas.
   */
  @Nonnull
  public static String getCsvWithSpace(@RequireNonnull String[] array) {
    return getSeparatedValuesString(array, ", ");
  }

  /**
   * Returns csv with spaces after commas from list of string.
   * 
   * <p>This is used not for creating csv file, but for logging or program code creation.</p>
   * 
   * @param collection collection of string.
   * @return csv with spaces after commas.
   */
  @Nonnull
  public static String getCsvWithSpace(@RequireNonnull Collection<String> collection) {
    return getCsvWithSpace(collection.toArray(new String[collection.size()]));
  }

  /* ■□■□ html escape ■□■□ */

  /**
   * Returns html-escaped strings.
   * 
   * @param str string.
   * @return html-escaped strings.
   */
  @Nonnull
  public static String escapeHtml(@RequireNonnull String str) {
    StringBuffer result = new StringBuffer();
    for (char c : str.toCharArray()) {
      switch (c) {
        case '&' -> result.append("&amp;");
        case '<' -> result.append("&lt;");
        case '>' -> result.append("&gt;");
        case '"' -> result.append("&quot;");
        case '\'' -> result.append("&#39;");
        case ' ' -> result.append("&nbsp;");
        default -> result.append(c);
      }
    }

    return result.toString();
  }
}
