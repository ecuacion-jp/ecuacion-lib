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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import jp.ecuacion.lib.core.TestTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Test91_05_util_DateTimeApiUtil extends TestTools {

  @BeforeEach
  public void before() {
  }

  @Test
  public void test_getLocalDateTimeUserFriendlyString_arg_localDateTime_normal() {
    LocalDateTime dateTime = LocalDateTime.of(2001, 1, 1, 1, 1, 1, 111);
    String result = DateTimeApiUtil.getLocalDateTimeDisplayString(dateTime);

    assertEquals("2001-01-01 01:01:01", result);
  }

  @Test
  public void test_getLocalDateTimeUserFriendlyString_arg_localDateTime_abnormal_null() {
    try {
      DateTimeApiUtil.getLocalDateTimeDisplayString(null);
      fail();

    } catch (NullPointerException ex) {

    }
  }

  @Test
  public void test_normal_getLocalDateTimeUserFriendlyString_arg_offsetDateTime_normal() {
    OffsetDateTime dateTime =
        OffsetDateTime.of(LocalDateTime.of(2001, 1, 1, 1, 1, 1, 111), ZoneOffset.UTC);
    String result = DateTimeApiUtil.getLocalDateTimeDisplayString(dateTime, ZoneOffset.ofHours(9));

    assertEquals("2001-01-01 10:01:01", result);
  }

  @Test
  public void test_getLocalDateTimeUserFriendlyString_arg_offsetDateTime_abnormal_null_offsetDateTime() {
    try {
      DateTimeApiUtil.getLocalDateTimeDisplayString(null, ZoneOffset.UTC);
      fail();

    } catch (NullPointerException ex) {

    }
  }

  @Test
  public void test_getLocalDateTimeUserFriendlyString_arg_offsetDateTime_abnormal_null_ZoneOffset() {
    // OffsetDateTime dateTime =
    // OffsetDateTime.of(LocalDateTime.of(2001, 1, 1, 1, 1, 1, 111), ZoneOffset.UTC);
    // String result = DateTimeApiUtil.getLocalDateTimeUserFriendlyString(dateTime, null);

    // assertEquals(dateTime."2001-01-01 " + atZoneSameInstant(ZoneId.systemDefault()).getHour());
  }

  @Test
  public void test_getOffsetDateTimeUserFriendlyString_normal() {
    OffsetDateTime dateTime =
        OffsetDateTime.of(LocalDateTime.of(2001, 1, 1, 1, 1, 1, 111), ZoneOffset.UTC);
    String result = DateTimeApiUtil.getOffsetDateTimeDisplayString(dateTime, ZoneOffset.ofHours(9));

    assertEquals("2001-01-01 10:01:01 +09:00", result);
  }

  @Test
  public void test_getOffsetDateTimeUserFriendlyString_abnormal_null_offsetDateTime() {
    try {
      DateTimeApiUtil.getOffsetDateTimeDisplayString(null, ZoneOffset.UTC);
      fail();

    } catch (NullPointerException ex) {

    }
  }

  @Test
  public void test_getOffsetDateTimeUserFriendlyString_abnormal_null_ZoneOffset() {
    // OffsetDateTime dateTime =
    // OffsetDateTime.of(LocalDateTime.of(2001, 1, 1, 1, 1, 1, 111), ZoneOffset.UTC);
    // String result = DateTimeApiUtil.getOffsetDateTimeUserFriendlyString(OffsetDateTime.now(), null);
    //
    // assertEquals("2001-01-01 10:01:01 +09:00", result);
  }

  /*
   * <li>2001-01-01 01:01:01 (The separator of year, month and day of month is dash)</li>
   */
  @Test
  public void test_getLocalDateTime_normal1() {
    LocalDateTime result = DateTimeApiUtil.getLocalDateTime("2001-01-01 01:01:01");

    assertEquals(result.getYear(), 2001);
    assertEquals(result.getMonthValue(), 1);
    assertEquals(result.getDayOfMonth(), 1);
    assertEquals(result.getHour(), 1);
    assertEquals(result.getMinute(), 1);
    assertEquals(result.getSecond(), 1);
  }

  /*
   * <li>2001/01/01 01:01:01 (The separator of year, month and day of month is slash)</li>
   */
  @Test
  public void test_getLocalDateTime_normal2() {
    LocalDateTime result = DateTimeApiUtil.getLocalDateTime("2001/01/01 01:01:01");

    assertEquals(result.getYear(), 2001);
    assertEquals(result.getMonthValue(), 1);
    assertEquals(result.getDayOfMonth(), 1);
    assertEquals(result.getHour(), 1);
    assertEquals(result.getMinute(), 1);
    assertEquals(result.getSecond(), 1);
  }

  /*
   * <li> (The separator of the date and the time is "T")</li>
   */
  @Test
  public void test_getLocalDateTime_normal3() {
    LocalDateTime result = DateTimeApiUtil.getLocalDateTime("2001-01-01T01:01:01");

    assertEquals(result.getYear(), 2001);
    assertEquals(result.getMonthValue(), 1);
    assertEquals(result.getDayOfMonth(), 1);
    assertEquals(result.getHour(), 1);
    assertEquals(result.getMinute(), 1);
    assertEquals(result.getSecond(), 1);
  }

  /*
   * <li>2001-01-01 01:01:01.123 (Smaller seconds than 1 are added)</li>
   */
  @Test
  public void test_getLocalDateTime_normal4() {
    LocalDateTime result = DateTimeApiUtil.getLocalDateTime("2001-01-01 01:01:01.123");

    assertEquals(result.getYear(), 2001);
    assertEquals(result.getMonthValue(), 1);
    assertEquals(result.getDayOfMonth(), 1);
    assertEquals(result.getHour(), 1);
    assertEquals(result.getMinute(), 1);
    assertEquals(result.getSecond(), 1);
    assertEquals(result.getNano(), 123000000);
  }

  /*
   * day of month not padded with "0" 
   */
  @Test
  public void test_getLocalDateTime_abnormal1() {
    try {
      DateTimeApiUtil.getLocalDateTime("2001-01-1 01:01:01");
      fail();
      
    } catch (Exception ex) {
      // ex.printStackTrace();
    }
  }

  /*
   * <li>(localDateTime-part)+09:00</li>
   */
  @Test
  public void test_getOffsetDateTime_normal1() {
    OffsetDateTime tmp = DateTimeApiUtil.getOffsetDateTime("2001-01-01 01:01:01+00:00");
    LocalDateTime result = tmp.withOffsetSameInstant(ZoneOffset.ofHours(9)).toLocalDateTime();

    assertEquals(result.getYear(), 2001);
    assertEquals(result.getMonthValue(), 1);
    assertEquals(result.getDayOfMonth(), 1);
    assertEquals(result.getHour(), 10);
    assertEquals(result.getMinute(), 1);
    assertEquals(result.getSecond(), 1);
  }

  /*
   * <li>(localDateTime-part) +09:00</li>
   */
  @Test
  public void test_getOffsetDateTime_normal2() {
    OffsetDateTime tmp = DateTimeApiUtil.getOffsetDateTime("2001-01-01 01:01:01 +00:00");
    LocalDateTime result = tmp.withOffsetSameInstant(ZoneOffset.ofHours(9)).toLocalDateTime();

    assertEquals(result.getYear(), 2001);
    assertEquals(result.getMonthValue(), 1);
    assertEquals(result.getDayOfMonth(), 1);
    assertEquals(result.getHour(), 10);
    assertEquals(result.getMinute(), 1);
    assertEquals(result.getSecond(), 1);
  }
}
