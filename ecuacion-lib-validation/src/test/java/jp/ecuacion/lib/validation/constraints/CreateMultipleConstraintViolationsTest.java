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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import jp.ecuacion.lib.core.jakartavalidation.constraintvalidator.CreateMultipleConstraintViolationsConstraintValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link CreateMultipleConstraintViolationsConstraintValidatorFactory}
 *     with the validators of ecuacion-lib-validation which have {@code propertyPath}.
 */
@DisplayName("CreateMultipleConstraintViolationsConstraintValidatorFactory with validators")
@SuppressWarnings({"SameNameButDifferent", "UnusedMethod", "unused"})
public class CreateMultipleConstraintViolationsTest {

  private static final Validator defaultValidator =
      Validation.buildDefaultValidatorFactory().getValidator();

  private static final Validator multipleValidator = Validation.byDefaultProvider().configure()
      .constraintValidatorFactory(new CreateMultipleConstraintViolationsConstraintValidatorFactory())
      .buildValidatorFactory().getValidator();

  private static Set<String> propertyPaths(Set<? extends ConstraintViolation<?>> violations) {
    return violations.stream().map(cv -> cv.getPropertyPath().toString())
        .collect(java.util.stream.Collectors.toSet());
  }

  @Nested
  @DisplayName("class-level validator (@ReturnTrue)")
  class ClassLevel {

    @Test
    @DisplayName("default: one violation with empty propertyPath")
    void defaultFactory() {
      Set<ConstraintViolation<ReturnTrueBean>> set = defaultValidator.validate(new ReturnTrueBean());
      assertThat(set).hasSize(1);
      assertThat(propertyPaths(set)).containsExactly("");
    }

    @Test
    @DisplayName("factory: one violation for each propertyPath")
    void multipleFactory() {
      Set<ConstraintViolation<ReturnTrueBean>> set =
          multipleValidator.validate(new ReturnTrueBean());
      assertThat(set).hasSize(2);
      assertThat(propertyPaths(set)).containsExactlyInAnyOrder("startDate", "endDate");
    }

    @Test
    @DisplayName("factory: no violation when valid")
    void multipleFactoryValid() {
      assertThat(multipleValidator.validate(new ReturnTrueValidBean())).isEmpty();
    }
  }

  @Nested
  @DisplayName("method-level validator (@AssertTrueWithPropertyPath)")
  class MethodLevel {

    @Test
    @DisplayName("default: one violation with the getter's property name")
    void defaultFactory() {
      Set<ConstraintViolation<AssertTrueBean>> set =
          defaultValidator.validate(new AssertTrueBean());
      assertThat(set).hasSize(1);
      assertThat(propertyPaths(set)).containsExactly("dateRangeValid");
    }

    @Test
    @DisplayName("factory: one violation for each propertyPath, prefixed by the getter's name")
    void multipleFactory() {
      Set<ConstraintViolation<AssertTrueBean>> set =
          multipleValidator.validate(new AssertTrueBean());
      assertThat(set).hasSize(2);
      assertThat(propertyPaths(set)).containsExactlyInAnyOrder("dateRangeValid.startDate",
          "dateRangeValid.endDate");
    }

    @Test
    @DisplayName("factory: no violation when valid")
    void multipleFactoryValid() {
      assertThat(multipleValidator.validate(new AssertTrueValidBean())).isEmpty();
    }
  }

  @ReturnTrue(methodName = "isDateRangeValid", propertyPath = {"startDate", "endDate"},
      message = "must be true")
  public static class ReturnTrueBean {
    private String startDate = "2025-01-10";
    private String endDate = "2025-01-01";

    public boolean isDateRangeValid() {
      return false;
    }
  }

  @ReturnTrue(methodName = "isDateRangeValid", propertyPath = {"startDate", "endDate"},
      message = "must be true")
  public static class ReturnTrueValidBean {
    private String startDate = "2025-01-01";
    private String endDate = "2025-01-10";

    public boolean isDateRangeValid() {
      return true;
    }
  }

  // Must be public static and return boolean (primitive) so that Hibernate Validator
  // recognizes isXxx() as a getter and validates its method-level constraint.
  public static class AssertTrueBean {
    private String startDate = "2025-01-10";
    private String endDate = "2025-01-01";

    @AssertTrueWithPropertyPath(propertyPath = {"startDate", "endDate"})
    public boolean isDateRangeValid() {
      return false;
    }
  }

  public static class AssertTrueValidBean {
    private String startDate = "2025-01-01";
    private String endDate = "2025-01-10";

    @AssertTrueWithPropertyPath(propertyPath = {"startDate", "endDate"})
    public boolean isDateRangeValid() {
      return true;
    }
  }
}
