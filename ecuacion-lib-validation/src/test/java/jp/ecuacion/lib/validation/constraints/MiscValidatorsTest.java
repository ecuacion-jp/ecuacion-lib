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

/** Tests for miscellaneous validators. */
@DisplayName("Miscellaneous validators")
@SuppressWarnings({"SameNameButDifferent", "UnusedMethod"})
public class MiscValidatorsTest {

  private static final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  // -------------------------------------------------------------------------
  // AssertTrueWithPropertyPath
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@AssertTrueWithPropertyPath")
  class AssertTrueWithPropertyPathTests {

    @Test
    @DisplayName("method returning true passes")
    void returnsTrue() {
      assertThat(validator.validate(new AssertTrueTrueBean())).isEmpty();
    }

    @Test
    @DisplayName("method returning false fails")
    void returnsFalse() {
      assertThat(validator.validate(new AssertTrueFalseBean())).hasSize(1);
    }
  }

  // -------------------------------------------------------------------------
  // EnumElement
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@EnumElement")
  class EnumElementTests {

    @Test
    @DisplayName("null and empty string are valid")
    void nullAndEmpty() {
      assertThat(validator.validate(new Bean(null))).isEmpty();
      assertThat(validator.validate(new Bean(""))).isEmpty();
    }

    @Test
    @DisplayName("valid enum name passes")
    void validEnumName() {
      assertThat(validator.validate(new Bean("A"))).isEmpty();
      assertThat(validator.validate(new Bean("B"))).isEmpty();
    }

    @Test
    @DisplayName("string not matching any enum name fails")
    void invalidEnumName() {
      assertThat(validator.validate(new Bean("C"))).hasSize(1);
      assertThat(validator.validate(new Bean("a"))).hasSize(1);
    }

    private enum TestEnum { A, B }

    private static record Bean(@EnumElement(enumClass = TestEnum.class) @Nullable String value) {}
  }

  // -------------------------------------------------------------------------
  // PatternWithDescription
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@PatternWithDescription")
  class PatternWithDescriptionTests {

    @Test
    @DisplayName("null and empty string are valid")
    void nullAndEmpty() {
      assertThat(validator.validate(new Bean(null))).isEmpty();
      assertThat(validator.validate(new Bean(""))).isEmpty();
    }

    @Test
    @DisplayName("string matching the pattern passes")
    void matching() {
      assertThat(validator.validate(new Bean("123"))).isEmpty();
    }

    @Test
    @DisplayName("string not matching the pattern fails")
    void notMatching() {
      assertThat(validator.validate(new Bean("abc"))).hasSize(1);
    }

    private static record Bean(
        @PatternWithDescription(regexp = "\\d+", description = "digits only")
        @Nullable String value) {}
  }

  // -------------------------------------------------------------------------
  // ReturnTrue
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@ReturnTrue")
  class ReturnTrueTests {

    @Test
    @DisplayName("method returning true passes")
    void returnsTrue() {
      assertThat(validator.validate(new TrueBean())).isEmpty();
    }

    @Test
    @DisplayName("method returning false fails")
    void returnsFalse() {
      assertThat(validator.validate(new FalseBean())).hasSize(1);
    }

    @ReturnTrue(methodName = "check", propertyPath = {"field"}, message = "must be true")
    private static class TrueBean {
      @SuppressWarnings("unused")
      private String field = "dummy";

      // public is required because ReturnTrueValidator looks up the method via
      // Class#getMethod, which only finds public methods.
      @SuppressWarnings({"unused", "EffectivelyPrivate"})
      public boolean check() {
        return true;
      }
    }

    @ReturnTrue(methodName = "check", propertyPath = {"field"}, message = "must be true")
    private static class FalseBean {
      @SuppressWarnings("unused")
      private String field = "dummy";

      // public is required because ReturnTrueValidator looks up the method via
      // Class#getMethod, which only finds public methods.
      @SuppressWarnings({"unused", "EffectivelyPrivate"})
      public boolean check() {
        return false;
      }
    }
  }

  // Bean classes for AssertTrueWithPropertyPath.
  // Must be top-level public static members: Hibernate Validator requires public access
  // to discover method-level constraints on getter methods.
  // Return type must be boolean (primitive): isXxx() is only recognized as a getter
  // when returning boolean primitive, not Boolean wrapper.

  public static class AssertTrueTrueBean {
    @AssertTrueWithPropertyPath(propertyPath = {"field"})
    public boolean isCondition() {
      return true;
    }
  }

  public static class AssertTrueFalseBean {
    @AssertTrueWithPropertyPath(propertyPath = {"field"})
    public boolean isCondition() {
      return false;
    }
  }
}
