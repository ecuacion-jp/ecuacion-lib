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

import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.STRING;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.VALUE_OF_PROPERTY_PATH;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValueState;
import jp.ecuacion.lib.validation.constraints.internal.ValidateWhenValidator;
import org.jspecify.annotations.Nullable;

/** Test beans for {@link ConditionalCommonTest}. */
@SuppressWarnings({"unused", "removal"})
public class ConditionalCommonTestBean {

  @EmptyWhen(propertyPath = "field", conditionPropertyPath = "condField",
      conditionValue = STRING,
      conditionValueString = EclibValidationConstants.VALIDATOR_PARAMETER_NULL)
  public static class NoField {

    private String afield;
    private @Nullable String condField;

    public NoField(String fieldValue, @Nullable String condFieldValue) {
      afield = fieldValue;
      condField = condFieldValue;
    }
  }

  @EmptyWhen(propertyPath = "field", conditionPropertyPath = "condField",
      conditionValue = STRING,
      conditionValueString = EclibValidationConstants.VALIDATOR_PARAMETER_NULL)
  public static class NoConditionField {

    private String field;
    private @Nullable String acondField;

    public NoConditionField(String fieldValue, @Nullable String condFieldValue) {
      field = fieldValue;
      acondField = condFieldValue;
    }
  }

  public static class ValidatesWhenConditionNotSatisfied {

    @EmptyWhen(propertyPath = "field", conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a",
        notEmptyWhenConditionNotSatisfied = true)
    public static class TrueClass {
      private @Nullable String field = null;
      private String condField = "b";
    }

    @EmptyWhen(propertyPath = "field", conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a",
        notEmptyWhenConditionNotSatisfied = false)
    public static class FalseClass {
      private @Nullable String field = null;
      private String condField = "b";
    }
  }

  public static class MultipleFields {

    @EmptyWhen(propertyPath = {"field1", "field2"}, conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a")
    public static class AllTrue {
      private @Nullable String field1 = null;
      private String field2 = "";
      private String condField = "a";
    }

    @EmptyWhen(propertyPath = {"field1", "field2"}, conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a")
    public static class OneFalse {
      private @Nullable String field1 = null;
      private String field2 = "X";
      private String condField = "a";
    }

    @EmptyWhen(propertyPath = {"field1", "field2"}, conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a")
    public static class AllFalse {
      private String field1 = "X";
      private String field2 = "X";
      private String condField = "a";
    }

    @EmptyWhen(propertyPath = {"field1", "field2"}, conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a",
        notEmptyWhenConditionNotSatisfied = true)
    public static class AllTrueConditionNotSatisfied {
      private @Nullable String field1 = null;
      private String field2 = "";
      private String condField = "b";
    }

    @EmptyWhen(propertyPath = {"field1", "field2"}, conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a",
        notEmptyWhenConditionNotSatisfied = true)
    public static class OneFalseConditionNotSatisfied {
      private @Nullable String field1 = null;
      private String field2 = "X";
      private String condField = "b";
    }

    @EmptyWhen(propertyPath = {"field1", "field2"}, conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a",
        notEmptyWhenConditionNotSatisfied = true)
    public static class AllFalseConditionNotSatisfied {
      private String field1 = "X";
      private String field2 = "X";
      private String condField = "b";
    }
  }

  public static class FieldInParentClass {
    public static class Parent {
      private String field = "X";
      private String condField = "a";
      private String fieldHoldingConditionValue = "a";
    }

    @EmptyWhen(propertyPath = "field", conditionPropertyPath = "condField",
        conditionValue = VALUE_OF_PROPERTY_PATH,
        conditionValuePropertyPath = "fieldHoldingConditionValue")
    public static class Child extends Parent {

    }
  }

  public static class ItemNameKey {
    @NotEmptyWhen(propertyPath = "field", conditionPropertyPath = "condField",
        conditionOperator = ConditionOperator.EQUAL_TO,
        conditionValue = ConditionValue.EMPTY)
    public static class Obj {
      private @Nullable String field = null;
      private @Nullable String condField = null;
    }
  }

  /**
   * Test beans for {@code conditionValue} inference/omission
   * (see {@link ValidateWhenValidator#resolveConditionValue}).
   */
  public static class ConditionValueInference {

    // conditionValue omitted, inferred as STRING from conditionValueString.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValueString = "a")
    public static record InferredString(@Nullable String value, String condValue) {}

    // conditionValue omitted, inferred as PATTERN from conditionValuePatternRegexp.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValuePatternRegexp = ".*test.*")
    public static record InferredPattern(@Nullable String value, String condValue) {}

    // conditionValue omitted, inferred as VALUE_OF_PROPERTY_PATH from
    // conditionValuePropertyPath.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValuePropertyPath = "condEqual")
    public static record InferredValueOfPropertyPath(@Nullable String value, String condValue,
        String condEqual) {}

    // conditionValue omitted, but 2 of the 3 inferable elements are set: ambiguous.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValueString = "a", conditionValuePatternRegexp = ".*test.*")
    public static record AmbiguousStringAndPattern(@Nullable String value, String condValue) {}

    // conditionValue omitted and none of the 3 inferable elements are set: cannot infer.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue")
    public static record NothingSet(@Nullable String value, String condValue) {}

    // conditionValue explicitly set to a value that conflicts with the element that is set:
    // still rejected, exactly as when conditionValue is explicit today.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValue = ConditionValue.PATTERN, conditionValueString = "a")
    public static record ExplicitConflictsWithSetElement(@Nullable String value,
        String condValue) {}

    // conditionValue omitted, inferred as TRUE from conditionValueBoolean = true.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValueBoolean = true)
    public static record InferredTrue(@Nullable String value, boolean condValue) {}

    // conditionValue omitted, inferred as FALSE from conditionValueBoolean = false.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValueBoolean = false)
    public static record InferredFalse(@Nullable String value, boolean condValue) {}

    // conditionValueBoolean set to both true and false: not an error, first element wins (TRUE).
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValueBoolean = {true, false})
    public static record BooleanBothValuesFirstWins(@Nullable String value, boolean condValue) {}

    // conditionValue omitted, inferred as NULL from conditionValueState.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValueState = ConditionValueState.NULL)
    public static record InferredStateNull(@Nullable String value, @Nullable String condValue) {}

    // conditionValue omitted, inferred as NOT_EMPTY from conditionValueState.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValueState = ConditionValueState.NOT_EMPTY)
    public static record InferredStateNotEmpty(@Nullable String value,
        @Nullable String condValue) {}

    // conditionValueBoolean and conditionValueState both set: ambiguous.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValueBoolean = true, conditionValueState = ConditionValueState.NULL)
    public static record AmbiguousBooleanAndState(@Nullable String value, boolean condValue) {}

    // conditionValue explicit and matching conditionValueBoolean: redundant but allowed.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValue = ConditionValue.TRUE, conditionValueBoolean = true)
    public static record ExplicitMatchesBoolean(@Nullable String value, boolean condValue) {}

    // conditionValue explicit but conflicting with conditionValueBoolean: rejected.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValue = ConditionValue.TRUE, conditionValueBoolean = false)
    public static record ExplicitConflictsWithBoolean(@Nullable String value,
        boolean condValue) {}

    // conditionValue explicit and matching conditionValueState: redundant but allowed.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValue = ConditionValue.EMPTY, conditionValueState = ConditionValueState.EMPTY)
    public static record ExplicitMatchesState(@Nullable String value,
        @Nullable String condValue) {}

    // conditionValue explicit but conflicting with conditionValueState: rejected.
    @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
        conditionValue = ConditionValue.EMPTY,
        conditionValueState = ConditionValueState.NOT_EMPTY)
    public static record ExplicitConflictsWithState(@Nullable String value,
        @Nullable String condValue) {}
  }
}
