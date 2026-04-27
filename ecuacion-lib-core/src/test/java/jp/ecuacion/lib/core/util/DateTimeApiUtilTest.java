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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link DateTimeApiUtil}. */
@DisplayName("DateTimeApiUtil")
public class DateTimeApiUtilTest {

  @Test
  @DisplayName("getLocalDateTimeDisplayString(LocalDateTime) formats correctly")
  public void getLocalDateTimeDisplayString_localDateTime() {
    LocalDateTime dateTime = LocalDateTime.of(2001, 1, 1, 1, 1, 1, 111);
    assertThat(DateTimeApiUtil.getLocalDateTimeDisplayString(dateTime))
        .isEqualTo("2001-01-01 01:01:01");
  }

  @Test
  @DisplayName("getLocalDateTimeDisplayString(OffsetDateTime) converts to target zone")
  public void getLocalDateTimeDisplayString_offsetDateTime() {
    OffsetDateTime dateTime =
        OffsetDateTime.of(LocalDateTime.of(2001, 1, 1, 1, 1, 1, 111), ZoneOffset.UTC);
    assertThat(DateTimeApiUtil.getLocalDateTimeDisplayString(dateTime, ZoneOffset.ofHours(9)))
        .isEqualTo("2001-01-01 10:01:01");
  }

  @Test
  @DisplayName("getOffsetDateTimeDisplayString formats with offset suffix")
  public void getOffsetDateTimeDisplayString() {
    OffsetDateTime dateTime =
        OffsetDateTime.of(LocalDateTime.of(2001, 1, 1, 1, 1, 1, 111), ZoneOffset.UTC);
    assertThat(DateTimeApiUtil.getOffsetDateTimeDisplayString(dateTime, ZoneOffset.ofHours(9)))
        .isEqualTo("2001-01-01 10:01:01 +09:00");
  }

  @Test
  @DisplayName("getLocalDateTime parses date with dash separator")
  public void getLocalDateTime_dashSeparator() {
    LocalDateTime result = DateTimeApiUtil.getLocalDateTime("2001-01-01 01:01:01");
    assertThat(result.getYear()).isEqualTo(2001);
    assertThat(result.getMonthValue()).isEqualTo(1);
    assertThat(result.getDayOfMonth()).isEqualTo(1);
    assertThat(result.getHour()).isEqualTo(1);
    assertThat(result.getMinute()).isEqualTo(1);
    assertThat(result.getSecond()).isEqualTo(1);
  }

  @Test
  @DisplayName("getLocalDateTime parses date with slash separator")
  public void getLocalDateTime_slashSeparator() {
    LocalDateTime result = DateTimeApiUtil.getLocalDateTime("2001/01/01 01:01:01");
    assertThat(result.getYear()).isEqualTo(2001);
    assertThat(result.getMonthValue()).isEqualTo(1);
    assertThat(result.getDayOfMonth()).isEqualTo(1);
    assertThat(result.getHour()).isEqualTo(1);
    assertThat(result.getMinute()).isEqualTo(1);
    assertThat(result.getSecond()).isEqualTo(1);
  }

  @Test
  @DisplayName("getLocalDateTime parses date with 'T' separator between date and time")
  public void getLocalDateTime_tSeparator() {
    LocalDateTime result = DateTimeApiUtil.getLocalDateTime("2001-01-01T01:01:01");
    assertThat(result.getYear()).isEqualTo(2001);
    assertThat(result.getMonthValue()).isEqualTo(1);
    assertThat(result.getDayOfMonth()).isEqualTo(1);
    assertThat(result.getHour()).isEqualTo(1);
    assertThat(result.getMinute()).isEqualTo(1);
    assertThat(result.getSecond()).isEqualTo(1);
  }

  @Test
  @DisplayName("getLocalDateTime parses date with sub-second precision")
  public void getLocalDateTime_withMilliseconds() {
    LocalDateTime result = DateTimeApiUtil.getLocalDateTime("2001-01-01 01:01:01.123");
    assertThat(result.getYear()).isEqualTo(2001);
    assertThat(result.getMonthValue()).isEqualTo(1);
    assertThat(result.getDayOfMonth()).isEqualTo(1);
    assertThat(result.getHour()).isEqualTo(1);
    assertThat(result.getMinute()).isEqualTo(1);
    assertThat(result.getSecond()).isEqualTo(1);
    assertThat(result.getNano()).isEqualTo(123000000);
  }

  @Test
  @DisplayName("getLocalDateTime throws when day of month is not zero-padded")
  public void getLocalDateTime_unpadded_throws() {
    assertThatThrownBy(() -> DateTimeApiUtil.getLocalDateTime("2001-01-1 01:01:01"))
        .isInstanceOf(Exception.class);
  }

  @Test
  @DisplayName("getOffsetDateTime parses offset string with '+00:00' suffix")
  public void getOffsetDateTime_plusZeroOffset() {
    OffsetDateTime tmp = DateTimeApiUtil.getOffsetDateTime("2001-01-01 01:01:01+00:00");
    LocalDateTime result = tmp.withOffsetSameInstant(ZoneOffset.ofHours(9)).toLocalDateTime();
    assertThat(result.getYear()).isEqualTo(2001);
    assertThat(result.getMonthValue()).isEqualTo(1);
    assertThat(result.getDayOfMonth()).isEqualTo(1);
    assertThat(result.getHour()).isEqualTo(10);
    assertThat(result.getMinute()).isEqualTo(1);
    assertThat(result.getSecond()).isEqualTo(1);
  }

  @Test
  @DisplayName("getOffsetDateTime parses offset string with ' +00:00' suffix (space before sign)")
  public void getOffsetDateTime_spacedPlusZeroOffset() {
    OffsetDateTime tmp = DateTimeApiUtil.getOffsetDateTime("2001-01-01 01:01:01 +00:00");
    LocalDateTime result = tmp.withOffsetSameInstant(ZoneOffset.ofHours(9)).toLocalDateTime();
    assertThat(result.getYear()).isEqualTo(2001);
    assertThat(result.getMonthValue()).isEqualTo(1);
    assertThat(result.getDayOfMonth()).isEqualTo(1);
    assertThat(result.getHour()).isEqualTo(10);
    assertThat(result.getMinute()).isEqualTo(1);
    assertThat(result.getSecond()).isEqualTo(1);
  }
}
