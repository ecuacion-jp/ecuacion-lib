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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.util.Set;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.validation.constraints.ConcreteComparisonValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * is the test class mainly for ComparisonValidator.
 * So the most of the tests are done with test common annotation {@code @Comparison}.
 * But there are tests for LessThan, LessThanOrEqualTo, GreaterThan, GreaterThanOrEqualTo
 * to ensure the settings for each validator is correct.
 */
public class ComparisonTest {

  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void irregulars() {
    // propertyPath not found
    try {
      validator.validate(new ComparisonTestBean.Irregular.PropertyPathNotExist());
      Assertions.fail();

    } catch (ValidationException ex) {
      Throwable cause = ex.getCause().getCause();
      Assertions.assertTrue(cause instanceof NoSuchFieldException);
      Assertions.assertEquals("propertyPath2", cause.getMessage());
    }

    // basisPropertyPath not found
    try {
      validator.validate(new ComparisonTestBean.Irregular.BasisPropertyPathNotExist());
      Assertions.fail();

    } catch (ValidationException ex) {
      Throwable cause = ex.getCause().getCause();
      Assertions.assertTrue(cause instanceof NoSuchFieldException);
      Assertions.assertEquals("basisPropertyPath", cause.getMessage());
    }

    // types differ between propertyPath and basisPropertyPath
    try {
      validator.validate(
          new ComparisonTestBean.Irregular.TypesDifferBetweenPropertyPathAndBasisPropertyPath());
      Assertions.fail();

    } catch (ValidationException ex) {
      Assertions.assertTrue(ex.getCause() instanceof EclibRuntimeException);
    }

    // unsupported types
    try {
      validator.validate(new ComparisonTestBean.Irregular.UnsupportedType());
      Assertions.fail();

    } catch (ValidationException ex) {
      Assertions.assertTrue(ex.getCause() instanceof EclibRuntimeException);
    }
  }

  @SuppressWarnings("null")
  @Test
  public void validCheck() {
    // all valid
    Set<ConstraintViolation<ComparisonTestBean.ValidCheck.ValidWhenLessThanBasisBean>> setValidWhenLessThanBasisBean =
        validator.validate(new ComparisonTestBean.ValidCheck.ValidWhenLessThanBasisBean());
    Assertions.assertTrue(setValidWhenLessThanBasisBean.isEmpty());

    // all invalid
    Set<ConstraintViolation<ComparisonTestBean.ValidCheck.ValidWhenGreaterThanBasisBean>> setValidWhenGreaterThanBasisBean =
        validator.validate(new ComparisonTestBean.ValidCheck.ValidWhenGreaterThanBasisBean());
    Assertions.assertEquals(13, setValidWhenGreaterThanBasisBean.size());

    // all valid for equal values
    Set<ConstraintViolation<ComparisonTestBean.ValidCheck.EqualAllowedBean>> setEqualAllowedBean =
        validator.validate(new ComparisonTestBean.ValidCheck.EqualAllowedBean());
    Assertions.assertTrue(setEqualAllowedBean.isEmpty());

    // all invalid for equal values
    Set<ConstraintViolation<ComparisonTestBean.ValidCheck.EqualNotAllowedBean>> setEqualNotAllowedBean =
        validator.validate(new ComparisonTestBean.ValidCheck.EqualNotAllowedBean());
    Assertions.assertEquals(5, setEqualNotAllowedBean.size());
  }

  @Test
  public void isStringValidWhenLessThanBasis() {
    ConcreteComparisonValidator obj = new ConcreteComparisonValidator();
    Assertions.assertTrue(obj.isStringValidWhenLessThanBasis("a", "b"));
    Assertions.assertTrue(obj.isStringValidWhenLessThanBasis("a", "ab"));
    Assertions.assertFalse(obj.isStringValidWhenLessThanBasis("ab", "a"));
    Assertions.assertTrue(obj.isStringValidWhenLessThanBasis("a", "bc"));
    Assertions.assertFalse(obj.isStringValidWhenLessThanBasis("bc", "a"));
    Assertions.assertTrue(obj.isStringValidWhenLessThanBasis("ab", "c"));
    Assertions.assertFalse(obj.isStringValidWhenLessThanBasis("c", "ab"));
  }

  @SuppressWarnings("null")
  @Test
  public void eachAnnotationTest() {
    // valid
    Set<ConstraintViolation<ComparisonTestBean.EachAnnotation.Valid>> setValid =
        validator.validate(new ComparisonTestBean.EachAnnotation.Valid());
    Assertions.assertEquals(true, setValid.isEmpty());

    // invalid
    Set<ConstraintViolation<ComparisonTestBean.EachAnnotation.Invalid>> setInvalid =
        validator.validate(new ComparisonTestBean.EachAnnotation.Invalid());
    Assertions.assertEquals(6, setInvalid.size());
  }

  @SuppressWarnings("null")
  @Test
  public void dotContainingPropertyPaths() {
    Set<ConstraintViolation<ComparisonTestBean.DotContainingPropertyPaths.Bean>> setBean =
        validator.validate(new ComparisonTestBean.DotContainingPropertyPaths.Bean());
    Assertions.assertEquals(1, setBean.size());
  }
}
