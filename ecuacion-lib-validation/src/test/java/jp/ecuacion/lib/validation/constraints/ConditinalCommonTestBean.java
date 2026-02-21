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

@SuppressWarnings("unused")
public class ConditinalCommonTestBean {

  @EmptyWhen(propertyPath = "field", conditionPropertyPath = "condField",
      conditionValue = STRING, conditionValueString = EclibValidationConstants.VALIDATOR_PARAMETER_NULL)
  public static class NoField {

    private String afield;
    private String condField;

    public NoField(String fieldValue, String condFieldValue) {
      afield = fieldValue;
      condField = condFieldValue;
    }
  }

  @EmptyWhen(propertyPath = "field", conditionPropertyPath = "condField",
      conditionValue = STRING, conditionValueString = EclibValidationConstants.VALIDATOR_PARAMETER_NULL)
  public static class NoConditionField {

    private String field;
    private String acondField;

    public NoConditionField(String fieldValue, String condFieldValue) {
      field = fieldValue;
      acondField = condFieldValue;
    }
  }

  public static class ValidatesWhenConditionNotSatisfied {

    @EmptyWhen(propertyPath = "field", conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a",
        notEmptyWhenConditionNotSatisfied = true)
    public static class TrueClass {
      private String field = null;
      private String condField = "b";
    }

    @EmptyWhen(propertyPath = "field", conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a",
        notEmptyWhenConditionNotSatisfied = false)
    public static class FalseClass {
      private String field = null;
      private String condField = "b";
    }
  }

  public static class MultipleFields {

    @EmptyWhen(propertyPath = {"field1", "field2"}, conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a")
    public static class AllTrue {
      private String field1 = null;
      private String field2 = "";
      private String condField = "a";
    }

    @EmptyWhen(propertyPath = {"field1", "field2"}, conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a")
    public static class OneFalse {
      private String field1 = null;
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
      private String field1 = null;
      private String field2 = "";
      private String condField = "b";
    }

    @EmptyWhen(propertyPath = {"field1", "field2"}, conditionPropertyPath = "condField",
        conditionValue = STRING, conditionValueString = "a",
        notEmptyWhenConditionNotSatisfied = true)
    public static class OneFalseConditionNotSatisfied {
      private String field1 = null;
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
      private String field = null;
      private String condField = null;
    }

  }
}
