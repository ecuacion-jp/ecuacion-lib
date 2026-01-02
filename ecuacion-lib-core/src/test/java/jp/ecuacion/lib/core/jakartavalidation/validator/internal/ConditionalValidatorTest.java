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
package jp.ecuacion.lib.core.jakartavalidation.validator.internal;

import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionOperator.equalTo;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionOperator.notEqualTo;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionValuePattern.string;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionValuePattern.booleanFalse;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionValuePattern.booleanTrue;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionValuePattern.empty;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionValuePattern.valueOfPropertyPath;

import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionOperator;
import jp.ecuacion.lib.core.jakartavalidation.validator.internal.ConditinalValidatorTestBean.TestEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConditionalValidatorTest {
  private ConditionalValidator obj = new ConditionalValidator() {
    @Override
    protected boolean isValid(Object valueOfField) {
      return false;
    }
  };

  @Test
  public void getSatisfiesConditionConditionValueStringIsEqualToTest() {

    // getSatisfiesCondition is true when the second argument value of StrA Constructor contains the
    // third String[] argument of initialize.
    // "X" is not related to getSatisfiesCondition() test.

    // null
    obj.initialize(new String[] {"field"}, "condField", string, ConditionOperator.equalTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    // blank
    obj.initialize(new String[] {"field"}, "condField", string, ConditionOperator.equalTo,
        new String[] {""}, EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    // non-empty value
    obj.initialize(new String[] {"field"}, "condField", string, ConditionOperator.equalTo,
        new String[] {"a"}, EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("b")));

    // null % blank
    obj.initialize(new String[] {"field"}, "condField", string, ConditionOperator.equalTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL, ""},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    // null % non-empty value
    obj.initialize(new String[] {"field"}, "condField", string, ConditionOperator.equalTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL, "a"},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("b")));

    // blank % non-empty value
    obj.initialize(new String[] {"field"}, "condField", string, ConditionOperator.equalTo,
        new String[] {"", "a"}, EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("b")));

    // null & blank % non-empty value
    obj.initialize(new String[] {"field"}, "condField", string, ConditionOperator.equalTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL, "", "a"},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("b")));
  }

  @Test
  public void getSatisfiesConditionConditionValueStringIsNotEqualToTest() {

    // getSatisfiesCondition is true when the second argument value of StrA Constructor contains the
    // third String[] argument of initialize.
    // "X" is not related to getSatisfiesCondition() test.

    // null
    obj.initialize(new String[] {"field"}, "condField", string, notEqualTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    // blank
    obj.initialize(new String[] {"field"}, "condField", string, notEqualTo, new String[] {""},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    // non-empty value
    obj.initialize(new String[] {"field"}, "condField", string, notEqualTo, new String[] {"a"},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("b")));

    // null % blank
    obj.initialize(new String[] {"field"}, "condField", string, notEqualTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL, ""},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    // null % non-empty value
    obj.initialize(new String[] {"field"}, "condField", string, notEqualTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL, "a"},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("b")));

    // blank % non-empty value
    obj.initialize(new String[] {"field"}, "condField", string, notEqualTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL, "a"},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("b")));

    // null & blank % non-empty value
    obj.initialize(new String[] {"field"}, "condField", string, notEqualTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL, "", "a"},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString(null)));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("a")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueString("b")));
  }

  @Test
  public void getSatisfiesConditionConditionValueIsEmptyTest() {

    // dataType of conditionField = String
    obj.initialize(new String[] {"field"}, "condField", empty, equalTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueIsEmpty.String(null)));

    Assertions.assertEquals(true, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueIsEmpty.String("")));

    Assertions.assertEquals(false, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueIsEmpty.String("a")));

    // dataType of conditionField = Integer
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsEmpty.Integer(null)));

    Assertions.assertEquals(false, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueIsEmpty.Integer(1)));

    // dataType of conditionField = TestEnum
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsEmpty.TestEnum(null)));

    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsEmpty.TestEnum(TestEnum.value1)));
  }

  @Test
  public void getSatisfiesConditionConditionValueIsNotEmptyTest() {

    // dataType of conditionField = String
    obj.initialize(new String[] {"field"}, "condField", empty, notEqualTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.String(null)));

    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.String("")));

    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.String("a")));

    // dataType of conditionField = Integer
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.Integer(null)));

    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.Integer(1)));

    // dataType of conditionField = TestEnum
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.TestEnum(null)));

    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.TestEnum(TestEnum.value1)));
  }

  @Test
  public void getSatisfiesConditionConditionValueIsBooleanTrueTest() {

    // dataType of conditionField = Boolean
    obj.initialize(new String[] {"field"}, "condField", booleanTrue, equalTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(null)));

    Assertions.assertEquals(true, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(true)));

    Assertions.assertEquals(false, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(false)));
  }

  @Test
  public void getSatisfiesConditionConditionValueIsNotBooleanTrueTest() {

    // dataType of conditionField = Boolean
    obj.initialize(new String[] {"field"}, "condField", booleanTrue, notEqualTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(null)));

    Assertions.assertEquals(false, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(true)));

    Assertions.assertEquals(true, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(false)));
  }

  @Test
  public void getSatisfiesConditionConditionValueIsBooleanFalseTest() {

    // dataType of conditionField = Boolean
    obj.initialize(new String[] {"field"}, "condField", booleanFalse, equalTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(null)));

    Assertions.assertEquals(false, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(true)));

    Assertions.assertEquals(true, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(false)));
  }

  @Test
  public void getSatisfiesConditionConditionValueIsNotBooleanFalseTest() {

    // dataType of conditionField = Boolean
    obj.initialize(new String[] {"field"}, "condField", booleanFalse, notEqualTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL},
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(null)));

    Assertions.assertEquals(true, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(true)));

    Assertions.assertEquals(false, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueBoolean.Boolean(false)));
  }

  @Test
  public void getSatisfiesConditionValueOfConditionFieldIsEqualToValueOfTest() {

    obj.initialize(new String[] {"field"}, "condField", valueOfPropertyPath, equalTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL}, "fieldHoldingConditionValue",
        false);

    // conditionValueField not exist
    Assertions.assertThrows(EclibRuntimeException.class, () -> obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.NotExist(null)));

    // conditionValueField datatype not match
    Assertions.assertThrows(EclibRuntimeException.class, () -> obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.DataTypeNotMatch("a", 1)));

    // conditionValueField datatype not match(array)
    Assertions.assertThrows(EclibRuntimeException.class,
        () -> obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.ConditionValueField.DataTypeNotMatchArray("a",
                new Integer[] {1})));

    // String - null
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String(null, null)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String(null, "")));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String(null, "a")));

    // String - blank
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("", null)));
    Assertions.assertEquals(true, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.String("", "")));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("", "a")));

    // String - not empty
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("a", null)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("a", "")));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("a", "a")));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("a", "b")));

    // Integer
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.Integer(null, null)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.Integer(null, 1)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.Integer(1, null)));
    Assertions.assertEquals(true, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.Integer(1, 1)));
    Assertions.assertEquals(false, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.Integer(1, 2)));

    // enum
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.AnEnum(null, null)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.AnEnum(null, TestEnum.value1)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.AnEnum(TestEnum.value1, null)));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.AnEnum(
            TestEnum.value1, TestEnum.value1)));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.AnEnum(
            TestEnum.value1, TestEnum.value2)));

    // String[] - null
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            null, new String[] {null})));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            null, new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL})));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray(null, new String[] {""})));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray(null, new String[] {"a"})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            null, new String[] {null, "a"})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            null, new String[] {"", "a"})));

    // String[] - empty
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("", new String[] {null})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "", new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL})));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("", new String[] {""})));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("", new String[] {"a"})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "", new String[] {"", "a"})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "", new String[] {null, "a"})));

    // String[] - not empty
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("a", new String[] {null})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "a", new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL})));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("a", new String[] {""})));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("a", new String[] {"a"})));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("a", new String[] {"b"})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "a", new String[] {null, "a"})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "a", new String[] {"", "b"})));

    // Integer[] - null
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(
            null, new Integer[] {null})));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(null, new Integer[] {1})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(
            null, new Integer[] {null, 1})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(
            null, new Integer[] {1, 2})));

    // Integer[] - not null
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(1, new Integer[] {null})));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(1, new Integer[] {1})));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(1, new Integer[] {2})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(
            1, new Integer[] {null, 1})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(
            1, new Integer[] {null, 2})));

    // Enum[] - null
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            null, new TestEnum[] {null})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            null, new TestEnum[] {TestEnum.value1})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            null, new TestEnum[] {null, TestEnum.value1})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            null, new TestEnum[] {TestEnum.value1, TestEnum.value2})));

    // Enum[] - not null
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            TestEnum.value1, new TestEnum[] {null})));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            TestEnum.value1, new TestEnum[] {TestEnum.value1})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            TestEnum.value1, new TestEnum[] {TestEnum.value2})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            TestEnum.value1, new TestEnum[] {null, TestEnum.value1})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            TestEnum.value1, new TestEnum[] {null, TestEnum.value2})));
  }

  @Test
  public void getSatisfiesConditionValueOfConditionFieldIsNotEqualToValueOfTest() {

    obj.initialize(new String[] {"field"}, "condField", valueOfPropertyPath, notEqualTo,
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL}, "fieldHoldingConditionValue",
        false);

    // conditionValueField not exist
    Assertions.assertThrows(EclibRuntimeException.class, () -> obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.NotExist(null)));

    // conditionValueField datatype not match
    Assertions.assertThrows(EclibRuntimeException.class, () -> obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.DataTypeNotMatch("a", 1)));

    // conditionValueField datatype not match(array)
    Assertions.assertThrows(EclibRuntimeException.class,
        () -> obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.ConditionValueField.DataTypeNotMatchArray("a",
                new Integer[] {1})));

    // String - null
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String(null, null)));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String(null, "")));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String(null, "a")));

    // String - blank
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("", null)));
    Assertions.assertEquals(false, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.String("", "")));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("", "a")));

    // String - not empty
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("a", null)));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("a", "")));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("a", "a")));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.String("a", "b")));

    // Integer
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.Integer(null, null)));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.Integer(null, 1)));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.Integer(1, null)));
    Assertions.assertEquals(false, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.Integer(1, 1)));
    Assertions.assertEquals(true, obj
        .getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.Integer(1, 2)));

    // enum
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.AnEnum(null, null)));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.AnEnum(null, TestEnum.value1)));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.AnEnum(TestEnum.value1, null)));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.AnEnum(
            TestEnum.value1, TestEnum.value1)));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.AnEnum(
            TestEnum.value1, TestEnum.value2)));

    // String[] - null
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            null, new String[] {null})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            null, new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL})));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray(null, new String[] {""})));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray(null, new String[] {"a"})));
    // multiple values & contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            null, new String[] {null, "a"})));
    // multiple values & not contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            null, new String[] {"", "a"})));

    // String[] - empty
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("", new String[] {null})));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "", new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL})));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("", new String[] {""})));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("", new String[] {"a"})));
    // multiple values & contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "", new String[] {"", "a"})));
    // multiple values & not contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "", new String[] {null, "a"})));

    // String[] - not empty
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("a", new String[] {null})));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "a", new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL})));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("a", new String[] {""})));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("a", new String[] {"a"})));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.StringArray("a", new String[] {"b"})));
    // multiple values & contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "a", new String[] {null, "a"})));
    // multiple values & not contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.StringArray(
            "a", new String[] {"", "b"})));

    // Integer[] - null
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(
            null, new Integer[] {null})));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(null, new Integer[] {1})));
    // multiple values & contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(
            null, new Integer[] {null, 1})));
    // multiple values & not contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(
            null, new Integer[] {1, 2})));

    // Integer[] - not null
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(1, new Integer[] {null})));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(1, new Integer[] {1})));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(1, new Integer[] {2})));
    // multiple values & contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(
            1, new Integer[] {null, 1})));
    // multiple values & not contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.IntegerArray(
            1, new Integer[] {null, 2})));

    // Enum[] - null
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            null, new TestEnum[] {null})));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            null, new TestEnum[] {TestEnum.value1})));
    // multiple values & contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            null, new TestEnum[] {null, TestEnum.value1})));
    // multiple values & not contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            null, new TestEnum[] {TestEnum.value1, TestEnum.value2})));

    // Enum[] - not null
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            TestEnum.value1, new TestEnum[] {null})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            TestEnum.value1, new TestEnum[] {TestEnum.value1})));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            TestEnum.value1, new TestEnum[] {TestEnum.value2})));
    // multiple values & contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            TestEnum.value1, new TestEnum[] {null, TestEnum.value1})));
    // multiple values & not contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValueField.EnumArray(
            TestEnum.value1, new TestEnum[] {null, TestEnum.value2})));
  }
}
