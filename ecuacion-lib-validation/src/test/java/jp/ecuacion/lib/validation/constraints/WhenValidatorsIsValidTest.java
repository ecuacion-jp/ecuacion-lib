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
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for the validation logic ({@code isValid}) of each {@code @XxxWhen} validator.
 *
 * <p>Each test uses {@code conditionValue = ConditionValue.TRUE} with {@code cond = true}
 *     so the condition is always satisfied, allowing us to focus on the target field
 *     validation logic only.</p>
 */
@DisplayName("When validators - isValid logic")
@SuppressWarnings("SameNameButDifferent")
public class WhenValidatorsIsValidTest {

  private static final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  // -------------------------------------------------------------------------
  // EmptyWhen: field must be empty (null or "")
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@EmptyWhen")
  class EmptyWhenTests {

    @Test
    @DisplayName("null field passes (empty)")
    void nullPasses() {
      assertThat(validator.validate(new Bean(null, true))).isEmpty();
    }

    @Test
    @DisplayName("empty string passes (empty)")
    void emptyStringPasses() {
      assertThat(validator.validate(new Bean("", true))).isEmpty();
    }

    @Test
    @DisplayName("non-empty field fails")
    void nonEmptyFails() {
      assertThat(validator.validate(new Bean("a", true))).hasSize(1);
    }

    @EmptyWhen(propertyPath = "field", conditionPropertyPath = "cond",
        conditionValue = ConditionValue.TRUE)
    private static record Bean(@Nullable String field, boolean cond) {}
  }

  // -------------------------------------------------------------------------
  // TrueWhen: field must be true
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@TrueWhen")
  class TrueWhenTests {

    @Test
    @DisplayName("true passes")
    void truePasses() {
      assertThat(validator.validate(new Bean(true, true))).isEmpty();
    }

    @Test
    @DisplayName("false fails")
    void falseFails() {
      assertThat(validator.validate(new Bean(false, true))).hasSize(1);
    }

    @Test
    @DisplayName("null fails")
    void nullFails() {
      assertThat(validator.validate(new Bean(null, true))).hasSize(1);
    }

    @TrueWhen(propertyPath = "field", conditionPropertyPath = "cond",
        conditionValue = ConditionValue.TRUE)
    private static record Bean(@Nullable Boolean field, boolean cond) {}
  }

  // -------------------------------------------------------------------------
  // FalseWhen: field must be false
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@FalseWhen")
  class FalseWhenTests {

    @Test
    @DisplayName("false passes")
    void falsePasses() {
      assertThat(validator.validate(new Bean(false, true))).isEmpty();
    }

    @Test
    @DisplayName("true fails")
    void trueFails() {
      assertThat(validator.validate(new Bean(true, true))).hasSize(1);
    }

    @Test
    @DisplayName("null fails")
    void nullFails() {
      assertThat(validator.validate(new Bean(null, true))).hasSize(1);
    }

    @FalseWhen(propertyPath = "field", conditionPropertyPath = "cond",
        conditionValue = ConditionValue.TRUE)
    private static record Bean(@Nullable Boolean field, boolean cond) {}
  }

  // -------------------------------------------------------------------------
  // NullWhen: field must be null
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@NullWhen")
  class NullWhenTests {

    @Test
    @DisplayName("null passes")
    void nullPasses() {
      assertThat(validator.validate(new Bean(null, true))).isEmpty();
    }

    @Test
    @DisplayName("non-null field fails")
    void nonNullFails() {
      assertThat(validator.validate(new Bean("a", true))).hasSize(1);
    }

    @Test
    @DisplayName("empty string fails (empty string is not null)")
    void emptyStringFails() {
      assertThat(validator.validate(new Bean("", true))).hasSize(1);
    }

    @NullWhen(propertyPath = "field", conditionPropertyPath = "cond",
        conditionValue = ConditionValue.TRUE)
    private static record Bean(@Nullable String field, boolean cond) {}
  }

  // -------------------------------------------------------------------------
  // NotNullWhen: field must not be null
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@NotNullWhen")
  class NotNullWhenTests {

    @Test
    @DisplayName("non-null field passes")
    void nonNullPasses() {
      assertThat(validator.validate(new Bean("a", true))).isEmpty();
    }

    @Test
    @DisplayName("empty string passes (empty string is not null)")
    void emptyStringPasses() {
      assertThat(validator.validate(new Bean("", true))).isEmpty();
    }

    @Test
    @DisplayName("null fails")
    void nullFails() {
      assertThat(validator.validate(new Bean(null, true))).hasSize(1);
    }

    @NotNullWhen(propertyPath = "field", conditionPropertyPath = "cond",
        conditionValue = ConditionValue.TRUE)
    private static record Bean(@Nullable String field, boolean cond) {}
  }

  // -------------------------------------------------------------------------
  // StringWhen: field must be one of the specified strings
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@StringWhen")
  class StringWhenTests {

    @Test
    @DisplayName("field matching one of the specified strings passes")
    void matchingPasses() {
      assertThat(validator.validate(new Bean("expected", true))).isEmpty();
    }

    @Test
    @DisplayName("field not matching any specified string fails")
    void notMatchingFails() {
      assertThat(validator.validate(new Bean("other", true))).hasSize(1);
    }

    @Test
    @DisplayName("null field fails (null is not in the string list)")
    void nullFails() {
      assertThat(validator.validate(new Bean(null, true))).hasSize(1);
    }

    @StringWhen(propertyPath = "field", string = {"expected"},
        conditionPropertyPath = "cond", conditionValue = ConditionValue.TRUE)
    private static record Bean(@Nullable String field, boolean cond) {}
  }

  // -------------------------------------------------------------------------
  // NotStringWhen: field must not be any of the specified strings
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@NotStringWhen")
  class NotStringWhenTests {

    @Test
    @DisplayName("field not matching any specified string passes")
    void notMatchingPasses() {
      assertThat(validator.validate(new Bean("other", true))).isEmpty();
    }

    @Test
    @DisplayName("field matching a specified string fails")
    void matchingFails() {
      assertThat(validator.validate(new Bean("forbidden", true))).hasSize(1);
    }

    @NotStringWhen(propertyPath = "field", string = {"forbidden"},
        conditionPropertyPath = "cond", conditionValue = ConditionValue.TRUE)
    private static record Bean(@Nullable String field, boolean cond) {}
  }

  // -------------------------------------------------------------------------
  // PatternWhen: field must match the pattern (null/empty are invalid)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@PatternWhen")
  class PatternWhenTests {

    @Test
    @DisplayName("field matching the pattern passes")
    void matchingPasses() {
      assertThat(validator.validate(new Bean("123", true))).isEmpty();
    }

    @Test
    @DisplayName("field not matching the pattern fails")
    void notMatchingFails() {
      assertThat(validator.validate(new Bean("abc", true))).hasSize(1);
    }

    @Test
    @DisplayName("null field fails (unlike most validators, null is invalid for PatternWhen)")
    void nullFails() {
      assertThat(validator.validate(new Bean(null, true))).hasSize(1);
    }

    @Test
    @DisplayName("empty string fails")
    void emptyFails() {
      assertThat(validator.validate(new Bean("", true))).hasSize(1);
    }

    @PatternWhen(propertyPath = "field", regexp = "\\d+",
        conditionPropertyPath = "cond", conditionValue = ConditionValue.TRUE)
    private static record Bean(@Nullable String field, boolean cond) {}
  }

  // -------------------------------------------------------------------------
  // NotPatternWhen: field must not match the pattern (null/empty are valid)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@NotPatternWhen")
  class NotPatternWhenTests {

    @Test
    @DisplayName("field not matching the pattern passes")
    void notMatchingPasses() {
      assertThat(validator.validate(new Bean("abc", true))).isEmpty();
    }

    @Test
    @DisplayName("field matching the pattern fails")
    void matchingFails() {
      assertThat(validator.validate(new Bean("123", true))).hasSize(1);
    }

    @Test
    @DisplayName("null field passes (unlike PatternWhen, null is valid for NotPatternWhen)")
    void nullPasses() {
      assertThat(validator.validate(new Bean(null, true))).isEmpty();
    }

    @Test
    @DisplayName("empty string passes")
    void emptyPasses() {
      assertThat(validator.validate(new Bean("", true))).isEmpty();
    }

    @NotPatternWhen(propertyPath = "field", regexp = "\\d+",
        conditionPropertyPath = "cond", conditionValue = ConditionValue.TRUE)
    private static record Bean(@Nullable String field, boolean cond) {}
  }

  // -------------------------------------------------------------------------
  // ValueOfPropertyPathWhen: field must equal another field's value
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@ValueOfPropertyPathWhen")
  class ValueOfPropertyPathWhenTests {

    @Test
    @DisplayName("field equal to the reference field passes")
    void equalPasses() {
      assertThat(validator.validate(new Bean("abc", "abc", true))).isEmpty();
    }

    @Test
    @DisplayName("field not equal to the reference field fails")
    void notEqualFails() {
      assertThat(validator.validate(new Bean("abc", "xyz", true))).hasSize(1);
    }

    @Test
    @DisplayName("both null passes (null equals null)")
    void bothNullPasses() {
      assertThat(validator.validate(new Bean(null, null, true))).isEmpty();
    }

    @ValueOfPropertyPathWhen(propertyPath = "field", valuePropertyPath = "expected",
        conditionPropertyPath = "cond", conditionValue = ConditionValue.TRUE)
    private static record Bean(
        @Nullable String field, @Nullable String expected, boolean cond) {}
  }

  // -------------------------------------------------------------------------
  // NotValueOfPropertyPathWhen: field must not equal another field's value
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@NotValueOfPropertyPathWhen")
  class NotValueOfPropertyPathWhenTests {

    @Test
    @DisplayName("field not equal to the reference field passes")
    void notEqualPasses() {
      assertThat(validator.validate(new Bean("abc", "xyz", true))).isEmpty();
    }

    @Test
    @DisplayName("field equal to the reference field fails")
    void equalFails() {
      assertThat(validator.validate(new Bean("abc", "abc", true))).hasSize(1);
    }

    @NotValueOfPropertyPathWhen(propertyPath = "field", valuePropertyPath = "forbidden",
        conditionPropertyPath = "cond", conditionValue = ConditionValue.TRUE)
    private static record Bean(
        @Nullable String field, @Nullable String forbidden, boolean cond) {}
  }
}
