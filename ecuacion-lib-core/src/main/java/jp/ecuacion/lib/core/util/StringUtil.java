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

    // "_" が開始または終了文字の場合はsnakeCaseStringとして機能していないのでエラーとする
    if (snakeCaseString.startsWith("_")) {
      throw new EclibRuntimeException(
          "snake-case string cannot start with '_'. (argment string: '" + snakeCaseString + "')");
    }

    if (snakeCaseString.endsWith("_")) {
      throw new EclibRuntimeException(
          "snake-case string cannot end with '_'. (argment string: '" + snakeCaseString + "')");
    }

    // '_'が連続で入っている場合もエラー
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

    // 一文字目は小文字にしておく
    camelCaseString = StringUtils.uncapitalize(camelCaseString);

    char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    for (char c : chars) {
      String s = String.valueOf(c);
      camelCaseString = camelCaseString.replaceAll(s, "_" + s);
    }

    return camelCaseString;
  }

  /* ■□■□ number関連 ■□■□ */

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

  /* ■□■□ csv関連 ■□■□ */

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
    ObjectsUtil.requireNonNull(array, separator);

    boolean isFirstTime = true;
    StringBuilder sb = new StringBuilder();
    for (String value : array) {
      if (isFirstTime) {
        isFirstTime = false;

      } else {
        sb.append(separator);
      }

      sb.append(value);
    }

    return sb.toString();
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

  /* ■□■□ htmlエスケープ関連 ■□■□ */

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
        case '&':
          result.append("&amp;");
          break;
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        case '"':
          result.append("&quot;");
          break;
        case '\'':
          result.append("&#39;");
          break;
        case ' ':
          result.append("&nbsp;");
          break;
        default:
          result.append(c);
          break;
      }
    }

    return result.toString();
  }
}
