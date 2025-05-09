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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import jp.ecuacion.lib.core.annotation.RequireNonnull;

/**
 * Provides Utility methods related to {@code dateTime Api}.
 */
public class DateTimeApiUtil {
  
  private static final String REG_OF_DIFF = "\\+[0-9]{2}:[0-9]{2}";
  private static final String FM_OF_DIFF = "[xxx][xx][X]";

  private static final String REG_OF_DATE_DASHES = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
  private static final String FM_OF_DATE_DASHES = "yyyy-MM-dd";
  private static final String REG_OF_DATE_SLASHES = "[0-9]{4}/[0-9]{2}/[0-9]{2}";
  private static final String FM_OF_DATE_SLASHES = "yyyy/MM/dd";
  private static final String REG_OF_SEP1_T = "T";
  private static final String FM_OF_SEP1_T = "'T'";
  private static final String REG_OF_SEP1_SP = " ";
  private static final String FM_OF_SEP1_SP = "' '";
  private static final String REG_OF_TIME = "[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]*)?";
  private static final String FM_OF_TIME = "HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS]";

  private static final String USER_FRIENDLY_LOCAL_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private static final String USER_FRIENDLY_OFFSET_FORMAT = "yyyy-MM-dd HH:mm:ss ZZZZZ";


  /**
  * Prevents to create an instance.
  */
  private DateTimeApiUtil() {}
  
  /**
   * Returns user-friendly LocalDateTime format string : {@code yyyy-MM-dd HH:mm:ss}.
   *  
   * @param localDateTime localDateTime
   * @return localDateTime string: {@code yyyy-MM-dd HH:mm:ss}
   */
  @Nonnull
  public static String getLocalDateTimeDisplayString(@RequireNonnull LocalDateTime localDateTime) {
    ObjectsUtil.paramRequireNonNull(localDateTime);

    return localDateTime.format(DateTimeFormatter.ofPattern(USER_FRIENDLY_LOCAL_FORMAT));
  }

  /**
   *  Returns user-friendly LocalDateTime format string : {@code yyyy-MM-dd HH:mm:ss}.
   *  
   * @param dateTime offsetDateTime
   * @param zoneId zoneId, may be {@code null} 
   *     which is treated as {@code ZoneId.systemDefault()}.
   *     {@code ZoneOffset} is also available, which extends {@code ZoneId}.
   * @return localDateTime string: {@code yyyy-MM-dd HH:mm:ss}
   */
  @Nonnull
  public static String getLocalDateTimeDisplayString(
      @RequireNonnull OffsetDateTime dateTime, @Nullable ZoneId zoneId) {
    ObjectsUtil.paramRequireNonNull(dateTime);
    
    return getLocalDateTimeDisplayStringOrNullIfDateTimeIsNull(dateTime, zoneId);
  }

  /**
   *  Returns user-friendly LocalDateTime format string : {@code yyyy-MM-dd HH:mm:ss}.
   *  
   * @param dateTime offsetDateTime
   * @param zoneId zoneId, may be {@code null} 
   *     which is treated as {@code ZoneId.systemDefault()}.
   *     {@code ZoneOffset} is also available, which extends {@code ZoneId}.
   * @return localDateTime string: {@code yyyy-MM-dd HH:mm:ss}
   */
  @Nullable
  public static String getLocalDateTimeDisplayStringOrNullIfDateTimeIsNull(
      @RequireNonnull OffsetDateTime dateTime, @Nullable ZoneId zoneId) {
    ObjectsUtil.paramRequireNonNull(dateTime);
    zoneId = zoneId == null ? ZoneId.systemDefault() : zoneId;

    return dateTime.atZoneSameInstant(zoneId).toLocalDateTime()
        .format(DateTimeFormatter.ofPattern(USER_FRIENDLY_LOCAL_FORMAT));
  }

  /**
   * Returns user-friendly OffsetDateTime format string : {@code yyyy-MM-dd HH:mm:ss +HH:mm}.
   * 
   * @param offsetDateTime offsetDateTime
   * @param zoneId zoneId, may be {@code null} 
   *     which is treated as {@code ZoneId.systemDefault()}.
   *     {@code ZoneOffset} is also available, which extends {@code ZoneId}.
   * @return offsetDateTime string: {@code yyyy-MM-dd HH:mm:ss +HH:mm}
   */
  @Nonnull
  public static String getOffsetDateTimeDisplayString(@RequireNonnull OffsetDateTime offsetDateTime,
      @Nullable ZoneId zoneId) {
    ObjectsUtil.paramRequireNonNull(offsetDateTime);
    zoneId = zoneId == null ? ZoneId.systemDefault() : zoneId;

    return offsetDateTime.atZoneSameInstant(zoneId)
        .format(DateTimeFormatter.ofPattern(USER_FRIENDLY_OFFSET_FORMAT));
  }

  /** 
   * Returns a {@code LocalDateTime} instance from date-time string.
   * 
   * <p>{@code year} must be 4 digits and other elements must be 2 digits. 
   * (not like "1", but like "01")<br>
   *  Valid formats are as follows.</p>
   * <ul>
   * <li>2001-01-01 01:01:01 (The separator of year, month and day of month is dash)</li>
   * <li>2001/01/01 01:01:01 (The separator of year, month and day of month is slash)</li>
   * <li>2001-01-01T01:01:01 (The separator of the date and the time is "T")</li>
   * <li>2001-01-01 01:01:01.123 (Smaller seconds than 1 are added)</li>
   * </ul>
   * 
   * @param dateTimeString dateTimeString
   * @return LocalDateTime
   */
  @Nonnull
  public static LocalDateTime getLocalDateTime(@RequireNonnull String dateTimeString) {
    FormatHolder obj = getLocalDateTimePartFormat(dateTimeString);
    return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(obj.fmStr));
  }

  /** 
   * Returns a {@code OffsetDateTime} instance from date-time string.
   * 
   * <p>{@code year} must be 4 digits and other elements must be 2 digits. 
   * (not like "1", but like "01")<br>
   *  Valid formats are as follows. (The validation of localDateTime-part is 
   *  exactly the same as {@code getLocalDateTime(String dateTimeString)}</p>
   * <ul>
   * <li>(localDateTime-part)+09:00</li>
   * <li>(localDateTime-part) +09:00</li>
   * </ul>
   * 
   * @param dateTimeString dateTimeString
   * @return OffsetDateTime
   */
  @Nonnull
  public static OffsetDateTime getOffsetDateTime(@RequireNonnull String dateTimeString) {
    FormatHolder obj = getLocalDateTimePartFormat(dateTimeString);

    // 後ろの時差表現を判別
    if (dateTimeString.matches("^" + obj.regStr + REG_OF_DIFF + ".*")) {
      obj.appendStrings(REG_OF_DIFF, FM_OF_DIFF);

    } else if (dateTimeString.matches("^" + obj.regStr + " " + REG_OF_DIFF + ".*")) {
      obj.appendStrings(" ", "' '");
      obj.appendStrings(REG_OF_DIFF, FM_OF_DIFF);

    } else {
      throw new RuntimeException(
          "Date format incorrect. (date time string: " + dateTimeString + ")");
    }

    return OffsetDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(obj.fmStr));
  }

  /**
   * Analyzes the format of the argument string and returns the format info.
   * 
   * <p>{@code year} must be 4 digits and other elements must be 2 digits.
   *  (not like "1", but like "01")<br>
   *  Valid formats are as follows.</p>
   * <ul>
   * <li>2001-01-01 01:01:01 (The separator of year, month and day of month is dash)</li>
   * <li>2001/01/01 01:01:01 (The separator of year, month and day of month is slash)</li>
   * <li>2001-01-01T01:01:01 (The separator of the date and the time is "T")</li>
   * <li>2001-01-01 01:01:01.123 (Smaller seconds than 1 are added)</li>
   * </ul>
   * 
   * @param dateTimeString dateTimeString
   * @return formatHolder
   */
  @Nonnull
  private static FormatHolder getLocalDateTimePartFormat(@RequireNonnull String dateTimeString) {
    ObjectsUtil.paramRequireNonNull(dateTimeString);

    FormatHolder obj = new FormatHolder();

    // 日付部分を判別
    if (dateTimeString.matches("^" + REG_OF_DATE_DASHES + ".*")) {
      obj.appendStrings(REG_OF_DATE_DASHES, FM_OF_DATE_DASHES);

    } else if (dateTimeString.matches("^" + REG_OF_DATE_SLASHES + ".*")) {
      obj.appendStrings(REG_OF_DATE_SLASHES, FM_OF_DATE_SLASHES);

    } else {
      throw new RuntimeException(
          "Date format incorrect. (date time string: " + dateTimeString + ")");
    }

    // 日付と時刻の間の文字列を判別
    if (dateTimeString.matches("^" + obj.regStr + REG_OF_SEP1_T + ".*")) {
      obj.appendStrings(REG_OF_SEP1_T, FM_OF_SEP1_T);

    } else if (dateTimeString.matches("^" + obj.regStr + REG_OF_SEP1_SP + ".*")) {
      obj.appendStrings(REG_OF_SEP1_SP, FM_OF_SEP1_SP);

    } else {
      throw new RuntimeException(
          "Date format incorrect. (date time string: " + dateTimeString + ")");
    }

    // 時刻を判別
    if (dateTimeString.matches("^" + obj.regStr + REG_OF_TIME + ".*")) {
      obj.appendStrings(REG_OF_TIME, FM_OF_TIME);

    } else {
      throw new RuntimeException(
          "Date format incorrect. (date time string: " + dateTimeString + ")");
    }

    return obj;
  }

  static class FormatHolder {
    /* timestamp文字列に一致する正規表現。チェックしながら文字列連結していく。 */
    public String regStr = "";
    /* timestamp文字列に一致するDateTimeFortmatterの書式。チェックしながら文字列連結していく。 */
    public String fmStr = "";

    public void appendStrings(String regStrAppender, String fmStrAppender) {
      regStr += regStrAppender;
      fmStr += fmStrAppender;
    }
  }
}
