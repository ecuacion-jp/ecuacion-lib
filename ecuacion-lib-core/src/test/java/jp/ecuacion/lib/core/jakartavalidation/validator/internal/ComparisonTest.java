package jp.ecuacion.lib.core.jakartavalidation.validator.internal;

import jakarta.validation.ValidationException;
import java.util.Optional;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.jakartavalidation.validator.ConcreteComparisonValidator;
import jp.ecuacion.lib.core.util.ValidationUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * is the test class mainly for ComparisonValidator.
 * So the most of the tests are done with test common annotation {@code @Comparison}.
 * But there are tests for LessThan, LessThanOrEqualTo, GreaterThan, GreaterThanOrEqualTo
 * to ensure the settings for each validator is correct.
 */
public class ComparisonTest {

  private Optional<MultipleAppException> opt;

  @Test
  public void irregulars() {
    // propertyPath not found
    try {
      ValidationUtil.validateThenReturn(new ComparisonTestBean.Irregular.PropertyPathNotExist());
      Assertions.fail();

    } catch (ValidationException ex) {
      Throwable cause = ex.getCause().getCause();
      Assertions.assertTrue(cause instanceof NoSuchFieldException);
      Assertions.assertEquals("propertyPath2", cause.getMessage());
    }

    // basisPropertyPath not found
    try {
      ValidationUtil
          .validateThenReturn(new ComparisonTestBean.Irregular.BasisPropertyPathNotExist());
      Assertions.fail();

    } catch (ValidationException ex) {
      Throwable cause = ex.getCause().getCause();
      Assertions.assertTrue(cause instanceof NoSuchFieldException);
      Assertions.assertEquals("basisPropertyPath", cause.getMessage());
    }

    // types differ between propertyPath and basisPropertyPath
    try {
      ValidationUtil.validateThenReturn(
          new ComparisonTestBean.Irregular.TypesDifferBetweenPropertyPathAndBasisPropertyPath());
      Assertions.fail();

    } catch (ValidationException ex) {
      Assertions.assertTrue(ex.getCause() instanceof EclibRuntimeException);
    }

    // unsupported types
    try {
      ValidationUtil.validateThenReturn(new ComparisonTestBean.Irregular.UnsupportedType());
      Assertions.fail();

    } catch (ValidationException ex) {
      Assertions.assertTrue(ex.getCause() instanceof EclibRuntimeException);
    }
  }

  @Test
  public void validCheck() {
    // all valid
    opt = ValidationUtil
        .validateThenReturn(new ComparisonTestBean.ValidCheck.ValidWhenLessThanBasisBean());
    Assertions.assertTrue(opt.isEmpty());

    // all invalid
    opt = ValidationUtil
        .validateThenReturn(new ComparisonTestBean.ValidCheck.ValidWhenGreaterThanBasisBean());
    Assertions.assertTrue(opt.isPresent());
    Assertions.assertEquals(13, opt.get().getList().size());

    // all valid for equal values
    opt = ValidationUtil.validateThenReturn(new ComparisonTestBean.ValidCheck.EqualAllowedBean());
    Assertions.assertTrue(opt.isEmpty());

    // all invalid for equal values
    opt =
        ValidationUtil.validateThenReturn(new ComparisonTestBean.ValidCheck.EqualNotAllowedBean());
    Assertions.assertTrue(opt.isPresent());
    Assertions.assertEquals(5, opt.get().getList().size());
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

  @Test
  public void eachAnnotationTest() {
    // valid
    opt = ValidationUtil.validateThenReturn(new ComparisonTestBean.EachAnnotation.Valid());
    Assertions.assertEquals(true, opt.isEmpty());
    
    //invalid
    opt = ValidationUtil.validateThenReturn(new ComparisonTestBean.EachAnnotation.Invalid());
    Assertions.assertEquals(false, opt.isEmpty());
    Assertions.assertEquals(6, opt.get().getList().size());
  }
}
