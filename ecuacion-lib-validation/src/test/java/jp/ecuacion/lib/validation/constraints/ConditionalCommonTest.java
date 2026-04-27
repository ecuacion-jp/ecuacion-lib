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
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for common conditional validation behavior. */
@DisplayName("Conditional validators - common behavior")
public class ConditionalCommonTest {
  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void fieldNotExistTest() {

    // No Field
    try {
      validator.validate(new ConditionalCommonTestBean.NoField("X", null));
      Assertions.fail();
    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(Objects.requireNonNull(ex.getCause()).getCause())
          .isInstanceOf(NoSuchFieldException.class);
    }

    // No Condition Field
    try {
      validator.validate(new ConditionalCommonTestBean.NoConditionField("X", null));
      Assertions.fail();
    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(Objects.requireNonNull(ex.getCause()).getCause())
          .isInstanceOf(NoSuchFieldException.class);
    }
  }

  @Test
  public void validatesWhenConditionNotSatisfiedTest() {

    // true
    Set<?> setTrue = validator
        .validate(new ConditionalCommonTestBean.ValidatesWhenConditionNotSatisfied.TrueClass());
    assertThat(setTrue).hasSize(1);

    // false
    Set<?> setFalse = validator
        .validate(new ConditionalCommonTestBean.ValidatesWhenConditionNotSatisfied.FalseClass());
    assertThat(setFalse).isEmpty();
  }

  @Test
  public void multipleFields_allTrue_passes() {
    assertThat(validator.validate(new ConditionalCommonTestBean.MultipleFields.AllTrue()))
        .isEmpty();
  }

  @Test
  public void multipleFields_oneFalse_fails() {
    assertThat(validator.validate(new ConditionalCommonTestBean.MultipleFields.OneFalse()))
        .hasSize(1);
  }

  @Test
  public void multipleFields_allFalse_fails() {
    assertThat(validator.validate(new ConditionalCommonTestBean.MultipleFields.AllFalse()))
        .hasSize(1);
  }

  @Test
  public void multipleFields_allTrue_conditionNotSatisfied_fails() {
    Set<?> set = validator
        .validate(new ConditionalCommonTestBean.MultipleFields.AllTrueConditionNotSatisfied());
    assertThat(set).hasSize(1);
  }

  @Test
  public void multipleFields_oneFalse_conditionNotSatisfied_fails() {
    Set<?> set = validator
        .validate(new ConditionalCommonTestBean.MultipleFields.OneFalseConditionNotSatisfied());
    assertThat(set).hasSize(1);
  }

  @Test
  public void multipleFields_allFalse_conditionNotSatisfied_passes() {
    Set<?> set = validator
        .validate(new ConditionalCommonTestBean.MultipleFields.AllFalseConditionNotSatisfied());
    assertThat(set).isEmpty();
  }

  @Test
  public void fieldInParentClassTest() {
    Set<?> set = validator.validate(new ConditionalCommonTestBean.FieldInParentClass.Child());
    assertThat(set).hasSize(1);
  }

  @Test
  public void itemNameKeyTest() {
    // values are null.
    Set<?> set = validator.validate(new ConditionalCommonTestBean.ItemNameKey.Obj());
    assertThat(set).hasSize(1);
  }
}
