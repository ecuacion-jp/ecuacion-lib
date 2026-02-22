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

import jakarta.validation.ValidationException;
import java.util.Set;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.util.ValidationUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConditinalCommonTest {

  @Test
  public void fieldNotExistTest() {

    // No Field
    try {
      ValidationUtil.validate(new ConditinalCommonTestBean.NoField("X", null));
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);

    }

    // No Condition Field
    try {
      ValidationUtil.validate(new ConditinalCommonTestBean.NoConditionField("X", null));
      Assertions.fail();
    } catch (ValidationException ex) {
      Assertions.assertEquals(true, ex.getCause() instanceof EclibRuntimeException);
    }
  }

  @Test
  public void validatesWhenConditionNotSatisfiedTest() {

    // true
    Set<?> setTrue = ValidationUtil
        .validate(new ConditinalCommonTestBean.ValidatesWhenConditionNotSatisfied.TrueClass());
    Assertions.assertEquals(1, setTrue.size());

    // false
    Set<?> setFalse = ValidationUtil
        .validate(new ConditinalCommonTestBean.ValidatesWhenConditionNotSatisfied.FalseClass());
    Assertions.assertEquals(0, setFalse.size());
  }

  @Test
  public void multipleFieldsTest() {

    Set<?> set = ValidationUtil.validate(new ConditinalCommonTestBean.MultipleFields.AllTrue());
    Assertions.assertEquals(0, set.size());

    set = ValidationUtil.validate(new ConditinalCommonTestBean.MultipleFields.OneFalse());
    Assertions.assertEquals(1, set.size());

    set = ValidationUtil.validate(new ConditinalCommonTestBean.MultipleFields.AllFalse());
    Assertions.assertEquals(1, set.size());

    set = ValidationUtil
        .validate(new ConditinalCommonTestBean.MultipleFields.AllTrueConditionNotSatisfied());
    Assertions.assertEquals(1, set.size());

    set = ValidationUtil
        .validate(new ConditinalCommonTestBean.MultipleFields.OneFalseConditionNotSatisfied());
    Assertions.assertEquals(1, set.size());

    set = ValidationUtil
        .validate(new ConditinalCommonTestBean.MultipleFields.AllFalseConditionNotSatisfied());
    Assertions.assertEquals(0, set.size());
  }

  @Test
  public void fieldInParentClassTest() {
    Set<?> set = ValidationUtil.validate(new ConditinalCommonTestBean.FieldInParentClass.Child());
    Assertions.assertEquals(1, set.size());
  }

  @Test
  public void itemNameKeyTest() {
    // values are null.
    Set<?> set = ValidationUtil.validate(new ConditinalCommonTestBean.ItemNameKey.Obj());
    Assertions.assertEquals(1, set.size());
  }

}
