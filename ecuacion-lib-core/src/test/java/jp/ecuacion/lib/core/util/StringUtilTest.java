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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link StringUtil}. */
@DisplayName("StringUtil")
public class StringUtilTest {

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
  }

  // -------------------------------------------------------------------------
  // escapeHtml
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("escapeHtml")
  class EscapeHtml {

    @Test
    @DisplayName("special HTML characters are escaped")
    void specialChars() {
      assertThat(StringUtil.escapeHtml("&")).isEqualTo("&amp;");
      assertThat(StringUtil.escapeHtml("<")).isEqualTo("&lt;");
      assertThat(StringUtil.escapeHtml(">")).isEqualTo("&gt;");
      assertThat(StringUtil.escapeHtml("\"")).isEqualTo("&quot;");
      assertThat(StringUtil.escapeHtml("'")).isEqualTo("&#39;");
      assertThat(StringUtil.escapeHtml(" ")).isEqualTo("&nbsp;");
    }

    @Test
    @DisplayName("plain text is returned unchanged")
    void plainText() {
      assertThat(StringUtil.escapeHtml("hello")).isEqualTo("hello");
    }

    @Test
    @DisplayName("mixed string is correctly escaped")
    void mixed() {
      assertThat(StringUtil.escapeHtml("<b>hello & world</b>"))
          .isEqualTo("&lt;b&gt;hello&nbsp;&amp;&nbsp;world&lt;/b&gt;");
    }
  }
}
