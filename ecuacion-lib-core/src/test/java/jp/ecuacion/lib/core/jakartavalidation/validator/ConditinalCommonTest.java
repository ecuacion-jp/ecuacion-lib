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
package jp.ecuacion.lib.core.jakartavalidation.validator;

import jakarta.validation.ValidationException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.util.ValidationUtil;
import jp.ecuacion.lib.core.util.ValidationUtil.ValidationExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConditinalCommonTest {
  private MultipleAppException mae;

  @Test
  public void fieldNotExistTest() {
    ValidationExecutor valUtil = ValidationUtil.builder().build();
    
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
    ValidationExecutor valUtil = ValidationUtil.builder().build();

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
    ValidationExecutor valUtil = ValidationUtil.builder().build();

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
    ValidationExecutor valUtil = ValidationUtil.builder().build();

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
    ValidationExecutor valUtil = ValidationUtil.builder().build();

    mae = valUtil.validateThenReturn(new ConditinalCommonTestBean.FieldInParentClass.Child());
    Assertions.assertEquals(1, mae.getList().size());
  }
  
}
