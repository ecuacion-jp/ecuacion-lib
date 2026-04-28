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
package jp.ecuacion.lib.core.jakartavalidation.constraintvalidator;

import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassAlwaysFalse;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassAlwaysFalseValidator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link CreateMultipleConstraintViolationsConstraintValidatorFactory}. */
@DisplayName("CreateMultipleConstraintViolationsConstraintValidatorFactory")
public class CreateMultipleConstraintViolationsConstraintValidatorFactoryTest {

  @Nested
  @DisplayName("getInstance")
  class GetInstance {

    @Test
    @DisplayName("MultiplePropertyPathsValidator subclass: setCreatesMultipleConstraintViolations called")
    void multiplePropertyPathsValidatorSubclass() {
      var factory = new CreateMultipleConstraintViolationsConstraintValidatorFactory();
      ClassAlwaysFalseValidator v = factory.getInstance(ClassAlwaysFalseValidator.class);
      assertThat(v).isNotNull();
    }

    @Test
    @DisplayName("non-MultiplePropertyPathsValidator: returned as-is without flag set")
    void nonMultiplePropertyPathsValidator() {
      var factory = new CreateMultipleConstraintViolationsConstraintValidatorFactory();
      SimpleValidator v = factory.getInstance(SimpleValidator.class);
      assertThat(v).isNotNull();
    }
  }

  @Nested
  @DisplayName("releaseInstance")
  class ReleaseInstance {

    @Test
    @DisplayName("no-op: does not throw")
    void noOp() {
      var factory = new CreateMultipleConstraintViolationsConstraintValidatorFactory();
      factory.releaseInstance(null);
    }
  }

  @Nested
  @DisplayName("isValidCommon with createsMultipleConstraintViolations=true")
  class IsValidCommonWithFactory {

    @Test
    @DisplayName("validator creates violations via factory; validation produces violations")
    void validationWithFactory() {
      ValidatorFactory vf = Validation.byDefaultProvider().configure()
          .constraintValidatorFactory(
              new CreateMultipleConstraintViolationsConstraintValidatorFactory())
          .buildValidatorFactory();
      Validator v = vf.getValidator();
      Set<ConstraintViolation<@NonNull BeanWithAlwaysFalse>> violations =
          v.validate(new BeanWithAlwaysFalse());
      assertThat(violations).isNotEmpty();
    }
  }

  @ClassAlwaysFalse(propertyPath = "field")
  static class BeanWithAlwaysFalse {
    @SuppressWarnings("unused")
    private @Nullable String field;
  }

  /** A simple validator that does NOT extend MultiplePropertyPathsValidator. */
  public static class SimpleValidator implements ConstraintValidator<SimpleConstraint, Object> {
    @Override
    public boolean isValid(@Nullable Object value, @Nullable ConstraintValidatorContext context) {
      return true;
    }
  }

  @Target({})
  @Retention(RetentionPolicy.RUNTIME)
  @jakarta.validation.Constraint(validatedBy = SimpleValidator.class)
  @interface SimpleConstraint {
    String message() default "simple";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};
  }
}
