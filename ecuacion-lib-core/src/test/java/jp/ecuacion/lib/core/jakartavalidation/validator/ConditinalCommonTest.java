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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConditinalCommonTest {
  private MultipleAppException mae;

  @Test
  public void fieldNotExistTest() {

    // No Field
    try {
      ValidationUtil.validateThenReturn(new ConditinalCommonTestBean.NoField("X", null));
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);

    }

    // No Condition Field
    try {
      ValidationUtil.validateThenReturn(new ConditinalCommonTestBean.NoConditionField("X", null));
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);
    }
  }

  @Test
  public void validatesWhenConditionNotSatisfiedTest() {

    // true
    ValidationUtil
        .validateThenReturn(
            new ConditinalCommonTestBean.ValidatesWhenConditionNotSatisfied.TrueClass())
        .ifPresentOrElse(mae -> Assertions.assertEquals(1, mae.getList().size()),
            () -> Assertions.fail());

    // false
    ValidationUtil
        .validateThenReturn(
            new ConditinalCommonTestBean.ValidatesWhenConditionNotSatisfied.FalseClass())
        .ifPresent(mae -> Assertions.fail());
  }

  @Test
  public void multipleFieldsTest() {

    mae = ValidationUtil.validateThenReturn(new ConditinalCommonTestBean.MultipleFields.AllTrue())
        .orElse(null);
    Assertions.assertEquals(null, mae);

    mae = ValidationUtil.validateThenReturn(new ConditinalCommonTestBean.MultipleFields.OneFalse())
        .get();
    Assertions.assertEquals(1, mae.getList().size());

    mae = ValidationUtil.validateThenReturn(new ConditinalCommonTestBean.MultipleFields.AllFalse())
        .get();
    Assertions.assertEquals(1, mae.getList().size());

    mae = ValidationUtil.validateThenReturn(
        new ConditinalCommonTestBean.MultipleFields.AllTrueConditionNotSatisfied()).get();
    Assertions.assertEquals(1, mae.getList().size());

    mae = ValidationUtil.validateThenReturn(
        new ConditinalCommonTestBean.MultipleFields.OneFalseConditionNotSatisfied()).get();
    Assertions.assertEquals(1, mae.getList().size());

    mae = ValidationUtil
        .validateThenReturn(
            new ConditinalCommonTestBean.MultipleFields.AllFalseConditionNotSatisfied())
        .orElse(null);;
    Assertions.assertEquals(null, mae);
  }

  @Test
  public void fieldInParentClassTest() {
    mae = ValidationUtil.validateThenReturn(new ConditinalCommonTestBean.FieldInParentClass.Child())
        .get();
    Assertions.assertEquals(1, mae.getList().size());
  }

}
