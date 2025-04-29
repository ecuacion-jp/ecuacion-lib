package jp.ecuacion.lib.core.jakartavalidation.validator;

import jakarta.validation.ValidationException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.util.ValidationUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConditinalCommonTest {
  private ValidationUtil valUtil = new ValidationUtil();
  private MultipleAppException mae;

  @Test
  public void fieldNotExistTest() {

    // No Field
    try {
      valUtil.validateThenReturn(new ConditinalCommonTestBean.NoField("X", null));
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);

    }

    // No Condition Field
    try {
      valUtil.validateThenReturn(new ConditinalCommonTestBean.NoConditionField("X", null));
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);
    }
  }

  @Test
  public void multipleConditionsTest() {

    // ValueAndIsEmpty
    try {
      valUtil.validateThenReturn(new ConditinalCommonTestBean.MultipleConditions.ValueAndIsEmpty());
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);
    }

    // ValueAndIsNotEmpty
    try {
      valUtil
          .validateThenReturn(new ConditinalCommonTestBean.MultipleConditions.ValueAndIsNotEmpty());
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);
    }

    // ValueAndFieldHoldingConditionValue
    try {
      valUtil.validateThenReturn(
          new ConditinalCommonTestBean.MultipleConditions.ValueAndFieldHoldingConditionValue());
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);
    }

    // IsEmptyAndIsNotEmpty
    try {
      valUtil.validateThenReturn(
          new ConditinalCommonTestBean.MultipleConditions.IsEmptyAndIsNotEmpty());
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);
    }

    // IsEmptyAndFieldHoldingConditionValue
    try {
      valUtil.validateThenReturn(
          new ConditinalCommonTestBean.MultipleConditions.IsEmptyAndFieldHoldingConditionValue());
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);
    }

    // IsNotEmptyAndFieldHoldingConditionValue
    try {
      valUtil.validateThenReturn(
          new ConditinalCommonTestBean.MultipleConditions.IsNotEmptyAndFieldHoldingConditionValue());
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);
    }
  }

  @Test
  public void validatesWhenConditionNotSatisfiedTest() {

    // true
    mae = valUtil.validateThenReturn(
        new ConditinalCommonTestBean.ValidatesWhenConditionNotSatisfied.TrueClass());
    Assertions.assertEquals(1, mae.getList().size());

    // false
    mae = valUtil.validateThenReturn(
        new ConditinalCommonTestBean.ValidatesWhenConditionNotSatisfied.FalseClass());
    Assertions.assertEquals(null, mae);
  }

  @Test
  public void multipleFieldsTest() {

    mae = valUtil.validateThenReturn(new ConditinalCommonTestBean.MultipleFields.AllTrue());
    Assertions.assertEquals(null, mae);

    mae = valUtil.validateThenReturn(new ConditinalCommonTestBean.MultipleFields.OneFalse());
    Assertions.assertEquals(1, mae.getList().size());

    mae = valUtil.validateThenReturn(new ConditinalCommonTestBean.MultipleFields.AllFalse());
    Assertions.assertEquals(1, mae.getList().size());

    mae = valUtil.validateThenReturn(
        new ConditinalCommonTestBean.MultipleFields.AllTrueConditionNotSatisfied());
    Assertions.assertEquals(1, mae.getList().size());

    mae = valUtil.validateThenReturn(
        new ConditinalCommonTestBean.MultipleFields.OneFalseConditionNotSatisfied());
    Assertions.assertEquals(1, mae.getList().size());

    mae = valUtil.validateThenReturn(
        new ConditinalCommonTestBean.MultipleFields.AllFalseConditionNotSatisfied());
    Assertions.assertEquals(null, mae);
  }

  @Test
  public void fieldInParentClassTest() {
    mae = valUtil.validateThenReturn(new ConditinalCommonTestBean.FieldInParentClass.Child());
    Assertions.assertEquals(1, mae.getList().size());
  }
  
}
