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
package jp.ecuacion.lib.validation.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for string-format validators. */
@DisplayName("String type validators")
@SuppressWarnings("SameNameButDifferent")
public class StringValidatorsTest {

  private static final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  // -------------------------------------------------------------------------
  // BooleanString
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@BooleanString")
  class BooleanStringTests {

    @Test
    @DisplayName("null and empty string are valid")
    void nullAndEmpty() {
      assertThat(validator.validate(new Bean(null))).isEmpty();
      assertThat(validator.validate(new Bean(""))).isEmpty();
    }

    @Test
    @DisplayName("recognized boolean strings are valid (case-insensitive)")
    void validValues() {
      for (String v : new String[] {
          "true", "True", "TRUE", "t", "T",
          "on", "ON", "yes", "YES", "y", "Y", "○",
          "false", "False", "FALSE", "f", "F",
          "off", "OFF", "no", "NO", "n", "N", "×"}) {
        assertThat(validator.validate(new Bean(v))).as("value: %s", v).isEmpty();
      }
    }

    @Test
    @DisplayName("unrecognized strings are invalid")
    void invalid() {
      assertThat(validator.validate(new Bean("abc"))).hasSize(1);
      assertThat(validator.validate(new Bean("1"))).hasSize(1);
    }

    private static record Bean(@BooleanString @Nullable String value) {}
  }

  // -------------------------------------------------------------------------
  // IntegerString
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@IntegerString")
  class IntegerStringTests {

    @Test
    @DisplayName("null and empty string are valid")
    void nullAndEmpty() {
      assertThat(validator.validate(new Bean(null))).isEmpty();
      assertThat(validator.validate(new Bean(""))).isEmpty();
    }

    @Test
    @DisplayName("integer strings including comma-separated are valid")
    void valid() {
      assertThat(validator.validate(new Bean("123"))).isEmpty();
      assertThat(validator.validate(new Bean("-456"))).isEmpty();
      assertThat(validator.validate(new Bean("1,234"))).isEmpty();
    }

    @Test
    @DisplayName("non-integer strings are invalid")
    void invalid() {
      assertThat(validator.validate(new Bean("1.5"))).hasSize(1);
      assertThat(validator.validate(new Bean("abc"))).hasSize(1);
    }

    private static record Bean(@IntegerString @Nullable String value) {}
  }

  // -------------------------------------------------------------------------
  // LongString
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@LongString")
  class LongStringTests {

    @Test
    @DisplayName("null and empty string are valid")
    void nullAndEmpty() {
      assertThat(validator.validate(new Bean(null))).isEmpty();
      assertThat(validator.validate(new Bean(""))).isEmpty();
    }

    @Test
    @DisplayName("long strings including values exceeding Integer range are valid")
    void valid() {
      assertThat(validator.validate(new Bean("123"))).isEmpty();
      // Integer.MAX_VALUE + 1 exceeds int range but fits in long
      assertThat(validator.validate(new Bean("2147483648"))).isEmpty();
      assertThat(validator.validate(new Bean("9,999,999,999"))).isEmpty();
    }

    @Test
    @DisplayName("non-long strings are invalid")
    void invalid() {
      assertThat(validator.validate(new Bean("1.5"))).hasSize(1);
      assertThat(validator.validate(new Bean("abc"))).hasSize(1);
    }

    private static record Bean(@LongString @Nullable String value) {}
  }

  // -------------------------------------------------------------------------
  // SizeString
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@SizeString")
  class SizeStringTests {

    @Test
    @DisplayName("null and empty string are valid regardless of size constraints")
    void nullAndEmpty() {
      assertThat(validator.validate(new Bean(null))).isEmpty();
      assertThat(validator.validate(new Bean(""))).isEmpty();
    }

    @Test
    @DisplayName("strings within min-max range are valid")
    void withinRange() {
      assertThat(validator.validate(new Bean("ab"))).isEmpty();   // exactly min
      assertThat(validator.validate(new Bean("abc"))).isEmpty();  // in between
      assertThat(validator.validate(new Bean("abcd"))).isEmpty(); // exactly max
    }

    @Test
    @DisplayName("strings outside min-max range are invalid")
    void outsideRange() {
      assertThat(validator.validate(new Bean("a"))).hasSize(1);     // below min
      assertThat(validator.validate(new Bean("abcde"))).hasSize(1); // above max
    }

    private static record Bean(@SizeString(min = 2, max = 4) @Nullable String value) {}
  }
}
