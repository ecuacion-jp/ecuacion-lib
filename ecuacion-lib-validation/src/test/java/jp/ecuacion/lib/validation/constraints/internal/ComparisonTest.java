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
package jp.ecuacion.lib.validation.constraints.internal;

import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.util.Objects;
import java.util.Set;
import jp.ecuacion.lib.validation.constraints.ConcreteComparisonValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * is the test class mainly for ComparisonValidator.
 * So the most of the tests are done with test common annotation {@code @Comparison}.
 * But there are tests for LessThan, LessThanOrEqualTo, GreaterThan, GreaterThanOrEqualTo
 * to ensure the settings for each validator is correct.
 */
@DisplayName("Comparison validators")
public class ComparisonTest {

  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void irregulars() {
    // propertyPath not found
    try {
      validator.validate(new ComparisonTestBean.Irregular.PropertyPathNotExist());
      Assertions.fail();

    } catch (ValidationException ex) {
      Throwable cause =
          Objects.requireNonNull(Objects.requireNonNull(ex.getCause()).getCause());
      assertThat(cause).isInstanceOf(NoSuchFieldException.class);
      assertThat(cause.getMessage()).isEqualTo("propertyPath2");
    }

    // basisPropertyPath not found
    try {
      validator.validate(new ComparisonTestBean.Irregular.BasisPropertyPathNotExist());
      Assertions.fail();

    } catch (ValidationException ex) {
      Throwable cause =
          Objects.requireNonNull(Objects.requireNonNull(ex.getCause()).getCause());
      assertThat(cause).isInstanceOf(NoSuchFieldException.class);
      assertThat(cause.getMessage()).isEqualTo("basisPropertyPath");
    }

    // types differ between propertyPath and basisPropertyPath
    try {
      validator.validate(
          new ComparisonTestBean.Irregular.TypesDifferBetweenPropertyPathAndBasisPropertyPath());
      Assertions.fail();

    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
    }

    // unsupported types
    try {
      validator.validate(new ComparisonTestBean.Irregular.UnsupportedType());
      Assertions.fail();

    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
    }
  }

  @SuppressWarnings("null")
  @Test
  public void validCheck() {
    // all valid
    Set<ConstraintViolation<ComparisonTestBean.ValidCheck.ValidWhenLessThanBasisBean>> setValidWhenLessThanBasisBean =
        validator.validate(new ComparisonTestBean.ValidCheck.ValidWhenLessThanBasisBean());
    assertThat(setValidWhenLessThanBasisBean).isEmpty();

    // all invalid
    Set<ConstraintViolation<ComparisonTestBean.ValidCheck.ValidWhenGreaterThanBasisBean>> setValidWhenGreaterThanBasisBean =
        validator.validate(new ComparisonTestBean.ValidCheck.ValidWhenGreaterThanBasisBean());
    assertThat(setValidWhenGreaterThanBasisBean).hasSize(13);

    // all valid for equal values
    Set<ConstraintViolation<ComparisonTestBean.ValidCheck.EqualAllowedBean>> setEqualAllowedBean =
        validator.validate(new ComparisonTestBean.ValidCheck.EqualAllowedBean());
    assertThat(setEqualAllowedBean).isEmpty();

    // all invalid for equal values
    Set<ConstraintViolation<ComparisonTestBean.ValidCheck.EqualNotAllowedBean>> setEqualNotAllowedBean =
        validator.validate(new ComparisonTestBean.ValidCheck.EqualNotAllowedBean());
    assertThat(setEqualNotAllowedBean).hasSize(5);
  }

  @Test
  public void isStringValidWhenLessThanBasis() {
    ConcreteComparisonValidator obj = new ConcreteComparisonValidator();
    assertThat(obj.isStringValidWhenLessThanBasis("a", "b")).isTrue();
    assertThat(obj.isStringValidWhenLessThanBasis("a", "ab")).isTrue();
    assertThat(obj.isStringValidWhenLessThanBasis("ab", "a")).isFalse();
    assertThat(obj.isStringValidWhenLessThanBasis("a", "bc")).isTrue();
    assertThat(obj.isStringValidWhenLessThanBasis("bc", "a")).isFalse();
    assertThat(obj.isStringValidWhenLessThanBasis("ab", "c")).isTrue();
    assertThat(obj.isStringValidWhenLessThanBasis("c", "ab")).isFalse();
  }

  @SuppressWarnings("null")
  @Test
  public void eachAnnotationTest() {
    // valid
    Set<ConstraintViolation<ComparisonTestBean.EachAnnotation.Valid>> setValid =
        validator.validate(new ComparisonTestBean.EachAnnotation.Valid());
    assertThat(setValid).isEmpty();

    // invalid
    Set<ConstraintViolation<ComparisonTestBean.EachAnnotation.Invalid>> setInvalid =
        validator.validate(new ComparisonTestBean.EachAnnotation.Invalid());
    assertThat(setInvalid).hasSize(6);
  }

  @SuppressWarnings("null")
  @Test
  public void dotContainingPropertyPaths() {
    Set<ConstraintViolation<ComparisonTestBean.DotContainingPropertyPaths.Bean>> setBean =
        validator.validate(new ComparisonTestBean.DotContainingPropertyPaths.Bean());
    assertThat(setBean).hasSize(1);
  }
}
