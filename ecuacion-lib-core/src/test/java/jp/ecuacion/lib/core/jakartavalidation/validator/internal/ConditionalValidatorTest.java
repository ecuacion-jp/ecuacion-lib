package jp.ecuacion.lib.core.jakartavalidation.validator.internal;

import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
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
  public void getSatisfiesConditionConditionValueTest() {

    // getSatisfiesCondition is true when the second argument value of StrA Constructor contains the
    // third String[] argument of initialize.
    // "X" is not related to getSatisfiesCondition() test.

    // null
    obj.initialize(new String[] {"field"}, "condField",
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL}, false, false,
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", null)));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "a")));

    // blank
    obj.initialize(new String[] {"field"}, "condField", new String[] {""}, false, false,
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", null)));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "a")));

    // non-empty value
    obj.initialize(new String[] {"field"}, "condField", new String[] {"a"}, false, false,
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", null)));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "a")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "b")));

    // null % blank
    obj.initialize(new String[] {"field"}, "condField",
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL, ""}, false, false,
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", null)));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "a")));

    // null % non-empty value
    obj.initialize(new String[] {"field"}, "condField",
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL, "a"}, false, false,
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", null)));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "a")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "b")));

    // blank % non-empty value
    obj.initialize(new String[] {"field"}, "condField",
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL, "a"}, false, false,
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", null)));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "a")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "b")));

    // null & blank % non-empty value
    obj.initialize(new String[] {"field"}, "condField",
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL, "", "a"}, false, false,
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", null)));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "")));

    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "a")));

    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.ConditionValue.StrA("X", "b")));
  }

  @Test
  public void getSatisfiesConditionConditionValueIsEmptyTest() {

    // dataType of conditionField = String
    obj.initialize(new String[] {"field"}, "condField",
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL}, true, false,
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsEmpty.String("X", null)));

    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsEmpty.String("X", "")));

    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsEmpty.String("X", "a")));

    // dataType of conditionField = Integer
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsEmpty.Integer("X", null)));

    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsEmpty.Integer("X", 1)));

    // dataType of conditionField = TestEnum
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsEmpty.TestEnum("X", null)));

    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsEmpty.TestEnum("X", TestEnum.value1)));
  }

  @Test
  public void getSatisfiesConditionConditionValueIsNotEmptyTest() {

    // dataType of conditionField = String
    obj.initialize(new String[] {"field"}, "condField",
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL}, false, true,
        EclibCoreConstants.VALIDATOR_PARAMETER_NULL, false);

    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.String("X", null)));

    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.String("X", "")));

    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.String("X", "a")));

    // dataType of conditionField = Integer
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.Integer("X", null)));

    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.Integer("X", 1)));

    // dataType of conditionField = TestEnum
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.TestEnum("X", null)));

    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.ConditionValueIsNotEmpty.TestEnum("X", TestEnum.value1)));
  }

  @Test
  public void getSatisfiesConditionFieldHoldingConditionValueTest() {

    obj.initialize(new String[] {"field"}, "condField",
        new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL}, false, false,
        "fieldHoldingConditionValue", false);

    // fieldHoldingConditionValue not exist
    try {
      obj.getSatisfiesCondition(
          new ConditinalValidatorTestBean.FieldHoldingConditionValue.NotExist("X", null));
      Assertions.fail();

    } catch (EclibRuntimeException ex) {
      Assertions.assertTrue(true);

    } catch (Exception ex) {
      Assertions.fail();
    }

    // fieldHoldingConditionValue datatype not match
    try {
      obj.getSatisfiesCondition(
          new ConditinalValidatorTestBean.FieldHoldingConditionValue.DataTypeNotMatch("X", "a", 1));
      Assertions.fail();

    } catch (EclibRuntimeException ex) {
      Assertions.assertTrue(true);

    } catch (Exception ex) {
      Assertions.fail();
    }

    // fieldHoldingConditionValue datatype not match(array)
    try {
      obj.getSatisfiesCondition(
          new ConditinalValidatorTestBean.FieldHoldingConditionValue.DataTypeNotMatchArray("X", "a",
              new Integer[] {1}));
      Assertions.fail();

    } catch (EclibRuntimeException ex) {
      Assertions.assertTrue(true);

    } catch (Exception ex) {
      Assertions.fail();
    }

    // String - null
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.String(null, null)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.String(null, "")));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.String(null, "a")));

    // String - blank
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.String("", null)));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.String("", "")));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.String("", "a")));

    // String - not empty
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.String("a", null)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.String("a", "")));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.String("a", "a")));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.String("a", "b")));

    // Integer
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.Integer(null, null)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.Integer(null, 1)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.Integer(1, null)));
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.Integer(1, 1)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.Integer(1, 2)));

    // enum
    Assertions.assertEquals(true, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.AnEnum(null, null)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.AnEnum(null, TestEnum.value1)));
    Assertions.assertEquals(false, obj.getSatisfiesCondition(
        new ConditinalValidatorTestBean.FieldHoldingConditionValue.AnEnum(TestEnum.value1, null)));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.FieldHoldingConditionValue.AnEnum(
            TestEnum.value1, TestEnum.value1)));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(new ConditinalValidatorTestBean.FieldHoldingConditionValue.AnEnum(
            TestEnum.value1, TestEnum.value2)));

    // String[] - null
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray(null,
                new String[] {null})));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray(null,
                new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray(null,
                new String[] {""})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray(null,
                new String[] {"a"})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray(null,
                new String[] {null, "a"})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray(null,
                new String[] {"", "a"})));

    // String[] - empty
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("",
                new String[] {null})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("",
                new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL})));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("",
                new String[] {""})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("",
                new String[] {"a"})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("",
                new String[] {"", "a"})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("",
                new String[] {null, "a"})));

    // String[] - not empty
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("a",
                new String[] {null})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("a",
                new String[] {EclibCoreConstants.VALIDATOR_PARAMETER_NULL})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("a",
                new String[] {""})));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("a",
                new String[] {"a"})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("a",
                new String[] {"b"})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("a",
                new String[] {null, "a"})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.StringArray("a",
                new String[] {"", "b"})));

    // Integer[] - null
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.IntegerArray(null,
                new Integer[] {null})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.IntegerArray(null,
                new Integer[] {1})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.IntegerArray(null,
                new Integer[] {null, 1})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.IntegerArray(null,
                new Integer[] {1, 2})));

    // Integer[] - not null
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.IntegerArray(1,
                new Integer[] {null})));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.IntegerArray(1,
                new Integer[] {1})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.IntegerArray(1,
                new Integer[] {2})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.IntegerArray(1,
                new Integer[] {null, 1})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.IntegerArray(1,
                new Integer[] {null, 2})));

    // Enum[] - null
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.EnumArray(null,
                new TestEnum[] {null})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.EnumArray(null,
                new TestEnum[] {TestEnum.value1})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.EnumArray(null,
                new TestEnum[] {null, TestEnum.value1})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.EnumArray(null,
                new TestEnum[] {TestEnum.value1, TestEnum.value2})));

    // Enum[] - not null
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.EnumArray(TestEnum.value1,
                new TestEnum[] {null})));
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.EnumArray(TestEnum.value1,
                new TestEnum[] {TestEnum.value1})));
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.EnumArray(TestEnum.value1,
                new TestEnum[] {TestEnum.value2})));
    // multiple values & contains
    Assertions.assertEquals(true,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.EnumArray(TestEnum.value1,
                new TestEnum[] {null, TestEnum.value1})));
    // multiple values & not contains
    Assertions.assertEquals(false,
        obj.getSatisfiesCondition(
            new ConditinalValidatorTestBean.FieldHoldingConditionValue.EnumArray(TestEnum.value1,
                new TestEnum[] {null, TestEnum.value2})));
  }
}
