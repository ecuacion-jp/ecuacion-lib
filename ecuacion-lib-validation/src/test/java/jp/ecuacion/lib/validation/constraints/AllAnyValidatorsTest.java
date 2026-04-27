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

/** Tests for All/Any-type class-level validators. */
@DisplayName("All/Any validators")
@SuppressWarnings("SameNameButDifferent")
public class AllAnyValidatorsTest {

  private static final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  // -------------------------------------------------------------------------
  // AllNullOrAllNotNull
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@AllNullOrAllNotNull")
  class AllNullOrAllNotNullTests {

    @Test
    @DisplayName("all null passes")
    void allNull() {
      assertThat(validator.validate(new Bean(null, null, null))).isEmpty();
    }

    @Test
    @DisplayName("all not-null passes")
    void allNotNull() {
      assertThat(validator.validate(new Bean("a", "b", "c"))).isEmpty();
    }

    @Test
    @DisplayName("mixed null and not-null fails")
    void mixed() {
      assertThat(validator.validate(new Bean("a", null, "c"))).hasSize(1);
    }

    @AllNullOrAllNotNull(propertyPath = {"f1", "f2", "f3"})
    private static record Bean(@Nullable String f1, @Nullable String f2, @Nullable String f3) {}
  }

  // -------------------------------------------------------------------------
  // AllEmptyOrAllNotEmpty
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@AllEmptyOrAllNotEmpty")
  class AllEmptyOrAllNotEmptyTests {

    @Test
    @DisplayName("all null passes")
    void allNull() {
      assertThat(validator.validate(new Bean(null, null, null))).isEmpty();
    }

    @Test
    @DisplayName("all empty string passes")
    void allEmptyString() {
      assertThat(validator.validate(new Bean("", "", ""))).isEmpty();
    }

    @Test
    @DisplayName("all non-empty passes")
    void allNonEmpty() {
      assertThat(validator.validate(new Bean("a", "b", "c"))).isEmpty();
    }

    @Test
    @DisplayName("mixed empty and non-empty fails")
    void mixed() {
      assertThat(validator.validate(new Bean("a", null, "c"))).hasSize(1);
      assertThat(validator.validate(new Bean("a", "", "c"))).hasSize(1);
    }

    @AllEmptyOrAllNotEmpty(propertyPath = {"f1", "f2", "f3"})
    private static record Bean(@Nullable String f1, @Nullable String f2, @Nullable String f3) {}
  }

  // -------------------------------------------------------------------------
  // AnyNull
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@AnyNull")
  class AnyNullTests {

    @Test
    @DisplayName("at least one null passes")
    void atLeastOneNull() {
      assertThat(validator.validate(new Bean("a", null, "c"))).isEmpty();
      assertThat(validator.validate(new Bean(null, null, null))).isEmpty();
    }

    @Test
    @DisplayName("all not-null fails")
    void allNotNull() {
      assertThat(validator.validate(new Bean("a", "b", "c"))).hasSize(1);
    }

    @AnyNull(propertyPath = {"f1", "f2", "f3"})
    private static record Bean(@Nullable String f1, @Nullable String f2, @Nullable String f3) {}
  }

  // -------------------------------------------------------------------------
  // AnyNotNull
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@AnyNotNull")
  class AnyNotNullTests {

    @Test
    @DisplayName("at least one not-null passes")
    void atLeastOneNotNull() {
      assertThat(validator.validate(new Bean(null, "b", null))).isEmpty();
      assertThat(validator.validate(new Bean("a", "b", "c"))).isEmpty();
    }

    @Test
    @DisplayName("all null fails")
    void allNull() {
      assertThat(validator.validate(new Bean(null, null, null))).hasSize(1);
    }

    @AnyNotNull(propertyPath = {"f1", "f2", "f3"})
    private static record Bean(@Nullable String f1, @Nullable String f2, @Nullable String f3) {}
  }

  // -------------------------------------------------------------------------
  // AnyEmpty
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@AnyEmpty")
  class AnyEmptyTests {

    @Test
    @DisplayName("at least one null or empty string passes")
    void atLeastOneEmpty() {
      assertThat(validator.validate(new Bean("a", null, "c"))).isEmpty();
      assertThat(validator.validate(new Bean("a", "", "c"))).isEmpty();
    }

    @Test
    @DisplayName("all non-empty fails")
    void allNonEmpty() {
      assertThat(validator.validate(new Bean("a", "b", "c"))).hasSize(1);
    }

    @AnyEmpty(propertyPath = {"f1", "f2", "f3"})
    private static record Bean(@Nullable String f1, @Nullable String f2, @Nullable String f3) {}
  }

  // -------------------------------------------------------------------------
  // AnyNotEmpty
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@AnyNotEmpty")
  class AnyNotEmptyTests {

    @Test
    @DisplayName("at least one non-empty passes")
    void atLeastOneNotEmpty() {
      assertThat(validator.validate(new Bean(null, "b", null))).isEmpty();
      assertThat(validator.validate(new Bean("a", "", "c"))).isEmpty();
    }

    @Test
    @DisplayName("all null or empty fails")
    void allEmpty() {
      assertThat(validator.validate(new Bean(null, null, null))).hasSize(1);
      assertThat(validator.validate(new Bean("", "", ""))).hasSize(1);
    }

    @AnyNotEmpty(propertyPath = {"f1", "f2", "f3"})
    private static record Bean(@Nullable String f1, @Nullable String f2, @Nullable String f3) {}
  }
}
