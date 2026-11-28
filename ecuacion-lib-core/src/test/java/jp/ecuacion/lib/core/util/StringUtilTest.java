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
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link StringUtil}. */
@DisplayName("StringUtil")
public class StringUtilTest {

  // -------------------------------------------------------------------------
  // toCurrencyFormat
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("toCurrencyFormat")
  @SuppressWarnings("removal")
  class ToCurrencyFormat {

    @Test
    @DisplayName("integer string is formatted with comma separator (US locale)")
    void basic() {
      Locale prev = Locale.getDefault();
      try {
        Locale.setDefault(Locale.US);
        assertThat(StringUtil.toCurrencyFormat("0")).isEqualTo("0");
        assertThat(StringUtil.toCurrencyFormat("1234")).isEqualTo("1,234");
        assertThat(StringUtil.toCurrencyFormat("1000000")).isEqualTo("1,000,000");
      } finally {
        Locale.setDefault(prev);
      }
    }

    @Test
    @DisplayName("non-numeric string throws NumberFormatException")
    void nonNumericThrows() {
      assertThatThrownBy(() -> StringUtil.toCurrencyFormat("abc"))
          .isInstanceOf(NumberFormatException.class);
    }
  }

  // -------------------------------------------------------------------------
  // toNumberWithGroupingSeparators
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("toNumberWithGroupingSeparators")
  class ToNumberWithGroupingSeparators {

    @Test
    @DisplayName("integer string is formatted with comma separator")
    void integer() {
      assertThat(StringUtil.toNumberWithGroupingSeparators("0")).isEqualTo("0");
      assertThat(StringUtil.toNumberWithGroupingSeparators("123")).isEqualTo("123");
      assertThat(StringUtil.toNumberWithGroupingSeparators("1234567")).isEqualTo("1,234,567");
      assertThat(StringUtil.toNumberWithGroupingSeparators("-1234567")).isEqualTo("-1,234,567");
    }

    @Test
    @DisplayName("number beyond the range of int and long is accepted")
    void largeNumber() {
      assertThat(StringUtil.toNumberWithGroupingSeparators("12345678901"))
          .isEqualTo("12,345,678,901");
      assertThat(StringUtil.toNumberWithGroupingSeparators("12345678901234567890"))
          .isEqualTo("12,345,678,901,234,567,890");
    }

    @Test
    @DisplayName("digits after the decimal point are kept as they are")
    void decimal() {
      assertThat(StringUtil.toNumberWithGroupingSeparators("1234.5678")).isEqualTo("1,234.5678");
      assertThat(StringUtil.toNumberWithGroupingSeparators("1234.50")).isEqualTo("1,234.50");
      assertThat(StringUtil.toNumberWithGroupingSeparators("0.001")).isEqualTo("0.001");
      assertThat(StringUtil.toNumberWithGroupingSeparators("-0.5")).isEqualTo("-0.5");
    }

    @Test
    @DisplayName("result does not depend on the default locale")
    void notDependOnDefaultLocale() {
      Locale prev = Locale.getDefault();
      try {
        Locale.setDefault(Locale.GERMANY);
        assertThat(StringUtil.toNumberWithGroupingSeparators("1234567.89"))
            .isEqualTo("1,234,567.89");
      } finally {
        Locale.setDefault(prev);
      }
    }

    @Test
    @DisplayName("designated locale determines the separators")
    void withLocale() {
      assertThat(StringUtil.toNumberWithGroupingSeparators("1234567.50", Locale.GERMANY))
          .isEqualTo("1.234.567,50");
      assertThat(StringUtil.toNumberWithGroupingSeparators("1234567.50", Locale.US))
          .isEqualTo("1,234,567.50");
      assertThat(StringUtil.toNumberWithGroupingSeparators("1234567", Locale.JAPAN))
          .isEqualTo("1,234,567");
    }

    @Test
    @DisplayName("non-numeric string throws NumberFormatException")
    void nonNumericThrows() {
      assertThatThrownBy(() -> StringUtil.toNumberWithGroupingSeparators("abc"))
          .isInstanceOf(NumberFormatException.class);
      assertThatThrownBy(() -> StringUtil.toNumberWithGroupingSeparators(""))
          .isInstanceOf(NumberFormatException.class);
      assertThatThrownBy(() -> StringUtil.toNumberWithGroupingSeparators("1,234"))
          .isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("null arguments throw NullPointerException")
    @SuppressWarnings({"null", "NullAway"})
    void nullThrows() {
      assertThatThrownBy(() -> StringUtil.toNumberWithGroupingSeparators((String) null))
          .isInstanceOf(NullPointerException.class);
      assertThatThrownBy(() -> StringUtil.toNumberWithGroupingSeparators((String) null, Locale.US))
          .isInstanceOf(NullPointerException.class);
      assertThatThrownBy(() -> StringUtil.toNumberWithGroupingSeparators("1", (Locale) null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  // -------------------------------------------------------------------------
  // isObjectNullOrEmpty
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("isObjectNullOrEmpty")
  class IsObjectNullOrEmpty {

    @Test
    @DisplayName("null returns true")
    void nullReturnsTrue() {
      assertThat(StringUtil.isObjectNullOrEmpty(null)).isTrue();
    }

    @Test
    @DisplayName("empty string returns true")
    void emptyStringReturnsTrue() {
      assertThat(StringUtil.isObjectNullOrEmpty("")).isTrue();
    }

    @Test
    @DisplayName("non-empty string returns false")
    void nonEmptyStringReturnsFalse() {
      assertThat(StringUtil.isObjectNullOrEmpty("a")).isFalse();
      assertThat(StringUtil.isObjectNullOrEmpty(" ")).isFalse();
    }

    @Test
    @DisplayName("non-String non-null object returns false")
    void nonStringNonNullReturnsFalse() {
      assertThat(StringUtil.isObjectNullOrEmpty(1)).isFalse();
      assertThat(StringUtil.isObjectNullOrEmpty(false)).isFalse();
    }
  }

  // -------------------------------------------------------------------------
  // getLowerCamelFromSnake
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getLowerCamelFromSnake")
  class GetLowerCamelFromSnake {

    @Test
    @DisplayName("lower_snake → lowerCamel")
    void lowerSnake() {
      assertThat(StringUtil.getLowerCamelFromSnake("snake_case")).isEqualTo("snakeCase");
      assertThat(StringUtil.getLowerCamelFromSnake("validation_messages_ja"))
          .isEqualTo("validationMessagesJa");
    }

    @Test
    @DisplayName("UPPER_SNAKE → lowerCamel (all-uppercase is lowercased first)")
    void upperSnake() {
      assertThat(StringUtil.getLowerCamelFromSnake("SNAKE_CASE")).isEqualTo("snakeCase");
      assertThat(StringUtil.getLowerCamelFromSnake("VALIDATION_MESSAGES_JA"))
          .isEqualTo("validationMessagesJa");
    }

    @Test
    @DisplayName("mixed case with lowercase letters is not fully lowercased before conversion")
    void mixedCase() {
      // Has lowercase → not fully lowercased → prefix stays as-is, only separator removed
      assertThat(StringUtil.getLowerCamelFromSnake("ValidationMessages_ja"))
          .isEqualTo("validationMessagesJa");
    }

    @Test
    @DisplayName("no separator → returned as-is (uncapitalized)")
    void noSeparator() {
      assertThat(StringUtil.getLowerCamelFromSnake("simple")).isEqualTo("simple");
    }

    @Test
    @DisplayName("leading underscore throws RuntimeException")
    void leadingUnderscore() {
      assertThatThrownBy(() -> StringUtil.getLowerCamelFromSnake("_invalid"))
          .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("trailing underscore throws RuntimeException")
    void trailingUnderscore() {
      assertThatThrownBy(() -> StringUtil.getLowerCamelFromSnake("invalid_"))
          .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("double underscore throws RuntimeException")
    void doubleUnderscore() {
      assertThatThrownBy(() -> StringUtil.getLowerCamelFromSnake("a__b"))
          .isInstanceOf(RuntimeException.class);
    }
  }

  // -------------------------------------------------------------------------
  // getUpperCamelFromSnake
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getUpperCamelFromSnake")
  class GetUpperCamelFromSnake {

    @Test
    @DisplayName("snake_case → UpperCamel")
    void basic() {
      assertThat(StringUtil.getUpperCamelFromSnake("snake_case")).isEqualTo("SnakeCase");
      assertThat(StringUtil.getUpperCamelFromSnake("VALIDATION_MESSAGES"))
          .isEqualTo("ValidationMessages");
    }
  }

  // -------------------------------------------------------------------------
  // getLowerSnakeFromCamel
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getLowerSnakeFromCamel")
  class GetLowerSnakeFromCamel {

    @Test
    @DisplayName("lowerCamel → lower_snake")
    void lowerCamel() {
      assertThat(StringUtil.getLowerSnakeFromCamel("snakeCase")).isEqualTo("snake_case");
      assertThat(StringUtil.getLowerSnakeFromCamel("validationMessages"))
          .isEqualTo("validation_messages");
    }

    @Test
    @DisplayName("UpperCamel → lower_snake (first char uncapitalized)")
    void upperCamel() {
      assertThat(StringUtil.getLowerSnakeFromCamel("SnakeCase")).isEqualTo("snake_case");
    }
  }

  // -------------------------------------------------------------------------
  // getCsv / getSeparatedValuesString
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getCsv / getSeparatedValuesString")
  class GetCsv {

    @Test
    @DisplayName("array → comma-separated string")
    void arrayToCsv() {
      assertThat(StringUtil.getCsv("a", "b", "c")).isEqualTo("a,b,c");
      assertThat(StringUtil.getCsv("x")).isEqualTo("x");
    }

    @Test
    @DisplayName("collection → comma-separated string")
    void collectionToCsv() {
      assertThat(StringUtil.getCsv(List.of("a", "b", "c"))).isEqualTo("a,b,c");
    }

    @Test
    @DisplayName("getSeparatedValuesString with enclosure wraps each element")
    void withEnclosure() {
      assertThat(StringUtil.getSeparatedValuesString(new String[]{"a", "b"}, ", ", "'"))
          .isEqualTo("'a', 'b'");
    }

    @Test
    @DisplayName("getCsvWithSpace adds space after each comma")
    void csvWithSpace() {
      assertThat(StringUtil.getCsvWithSpace(new String[]{"a", "b", "c"})).isEqualTo("a, b, c");
    }

    @Test
    @DisplayName("getCsvWithSpace accepts variable arguments")
    void csvWithSpaceVarargs() {
      assertThat(StringUtil.getCsvWithSpace("a", "b", "c")).isEqualTo("a, b, c");
      assertThat(StringUtil.getCsvWithSpace("x")).isEqualTo("x");
    }
  }

}
