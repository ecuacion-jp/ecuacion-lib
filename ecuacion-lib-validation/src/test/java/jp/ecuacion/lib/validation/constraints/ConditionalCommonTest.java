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

  // -------------------------------------------------------------------------
  // conditionValue inference/omission
  // -------------------------------------------------------------------------

  @Test
  public void conditionValueInference_string_isInferredFromConditionValueString() {
    // condValue == "a" -> condition satisfied -> value must be not empty
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredString(null, "a")))
        .hasSize(1);
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredString("x", "a")))
        .isEmpty();
    // condValue != "a" -> condition not satisfied -> always passes
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredString(null, "b")))
        .isEmpty();
  }

  @Test
  public void conditionValueInference_pattern_isInferredFromConditionValuePatternRegexp() {
    // condValue matches ".*test.*" -> condition satisfied -> value must be not empty
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredPattern(null, "test")))
        .hasSize(1);
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredPattern("x", "test")))
        .isEmpty();
    // condValue does not match -> condition not satisfied -> always passes
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredPattern(null, "other")))
        .isEmpty();
  }

  @Test
  public void conditionValueInference_valueOfPropertyPath_isInferredFromConditionValuePropertyPath() {
    // condValue == condEqual -> condition satisfied -> value must be not empty
    assertThat(validator.validate(new ConditionalCommonTestBean.ConditionValueInference
        .InferredValueOfPropertyPath(null, "a", "a"))).hasSize(1);
    assertThat(validator.validate(new ConditionalCommonTestBean.ConditionValueInference
        .InferredValueOfPropertyPath("x", "a", "a"))).isEmpty();
    // condValue != condEqual -> condition not satisfied -> always passes
    assertThat(validator.validate(new ConditionalCommonTestBean.ConditionValueInference
        .InferredValueOfPropertyPath(null, "a", "b"))).isEmpty();
  }

  @Test
  public void conditionValueInference_ambiguousElements_throws() {
    try {
      validator.validate(
          new ConditionalCommonTestBean.ConditionValueInference.AmbiguousStringAndPattern(null,
              "a"));
      Assertions.fail();
    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(Objects.requireNonNull(ex.getCause()).getMessage())
          .contains("cannot set more than one");
    }
  }

  @Test
  public void conditionValueInference_nothingSet_throws() {
    try {
      validator.validate(
          new ConditionalCommonTestBean.ConditionValueInference.NothingSet(null, "a"));
      Assertions.fail();
    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(Objects.requireNonNull(ex.getCause()).getMessage())
          .contains("'conditionValue' must be set");
    }
  }

  @Test
  public void conditionValueInference_explicitConflictsWithSetElement_stillThrows() {
    try {
      validator.validate(new ConditionalCommonTestBean.ConditionValueInference
          .ExplicitConflictsWithSetElement(null, "a"));
      Assertions.fail();
    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(Objects.requireNonNull(ex.getCause()).getMessage())
          .contains("conditionValueString");
    }
  }

  @Test
  public void conditionValueInference_true_isInferredFromConditionValueBoolean() {
    // condValue == true -> condition satisfied -> value must be not empty
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredTrue(null, true)))
        .hasSize(1);
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredTrue("x", true)))
        .isEmpty();
    // condValue == false -> condition not satisfied -> always passes
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredTrue(null, false)))
        .isEmpty();
  }

  @Test
  public void conditionValueInference_false_isInferredFromConditionValueBoolean() {
    // condValue == false -> condition satisfied -> value must be not empty
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredFalse(null, false)))
        .hasSize(1);
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredFalse("x", false)))
        .isEmpty();
    // condValue == true -> condition not satisfied -> always passes
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredFalse(null, true)))
        .isEmpty();
  }

  @Test
  public void conditionValueInference_booleanBothValuesSet_firstElementWinsAsTrue() {
    // {true, false} is treated as TRUE (first element), not an error
    assertThat(validator.validate(new ConditionalCommonTestBean.ConditionValueInference
        .BooleanBothValuesFirstWins(null, true))).hasSize(1);
    assertThat(validator.validate(new ConditionalCommonTestBean.ConditionValueInference
        .BooleanBothValuesFirstWins(null, false))).isEmpty();
  }

  @Test
  public void conditionValueInference_null_isInferredFromConditionValueState() {
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredStateNull(null, null)))
        .hasSize(1);
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.InferredStateNull(null, "a")))
        .isEmpty();
  }

  @Test
  public void conditionValueInference_notEmpty_isInferredFromConditionValueState() {
    assertThat(validator.validate(new ConditionalCommonTestBean.ConditionValueInference
        .InferredStateNotEmpty(null, "a"))).hasSize(1);
    assertThat(validator.validate(new ConditionalCommonTestBean.ConditionValueInference
        .InferredStateNotEmpty(null, null))).isEmpty();
  }

  @Test
  public void conditionValueInference_ambiguousBooleanAndState_throws() {
    try {
      validator.validate(new ConditionalCommonTestBean.ConditionValueInference
          .AmbiguousBooleanAndState(null, true));
      Assertions.fail();
    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(Objects.requireNonNull(ex.getCause()).getMessage())
          .contains("cannot set more than one");
    }
  }

  @Test
  public void conditionValueInference_explicitMatchesBoolean_redundantButAllowed() {
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.ExplicitMatchesBoolean(null, true)))
        .hasSize(1);
  }

  @Test
  public void conditionValueInference_explicitConflictsWithBoolean_throws() {
    try {
      validator.validate(new ConditionalCommonTestBean.ConditionValueInference
          .ExplicitConflictsWithBoolean(null, false));
      Assertions.fail();
    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(Objects.requireNonNull(ex.getCause()).getMessage())
          .contains("conditionValueBoolean");
    }
  }

  @Test
  public void conditionValueInference_explicitMatchesState_redundantButAllowed() {
    assertThat(validator.validate(
        new ConditionalCommonTestBean.ConditionValueInference.ExplicitMatchesState(null, null)))
        .hasSize(1);
  }

  @Test
  public void conditionValueInference_booleanSetWithNonBooleanConditionValue_throws() {
    try {
      validator.validate(new ConditionalCommonTestBean.ConditionValueInference
          .BooleanSetWithNonBooleanConditionValue(null, "a"));
      Assertions.fail();
    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(Objects.requireNonNull(ex.getCause()).getMessage())
          .contains("conditionValueBoolean");
    }
  }

  @Test
  public void conditionValueInference_stateSetWithNonStateConditionValue_throws() {
    try {
      validator.validate(new ConditionalCommonTestBean.ConditionValueInference
          .StateSetWithNonStateConditionValue(null, "a"));
      Assertions.fail();
    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(Objects.requireNonNull(ex.getCause()).getMessage())
          .contains("conditionValueState");
    }
  }

  @Test
  public void conditionValueInference_explicitConflictsWithState_throws() {
    try {
      validator.validate(new ConditionalCommonTestBean.ConditionValueInference
          .ExplicitConflictsWithState(null, null));
      Assertions.fail();
    } catch (ValidationException ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(Objects.requireNonNull(ex.getCause()).getMessage())
          .contains("conditionValueState");
    }
  }
}
