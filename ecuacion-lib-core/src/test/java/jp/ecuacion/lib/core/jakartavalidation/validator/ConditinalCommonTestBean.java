package jp.ecuacion.lib.core.jakartavalidation.validator;

import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.jakartavalidation.validator.ConditionalEmpty;

@SuppressWarnings("unused")
public class ConditinalCommonTestBean {

  @ConditionalEmpty(field = "field", conditionField = "condField",
      conditionValue = EclibCoreConstants.VALIDATOR_PARAMETER_NULL)
  public static class NoField {

    private String afield;
    private String condField;

    public NoField(String fieldValue, String condFieldValue) {
      afield = fieldValue;
      condField = condFieldValue;
    }
  }

  @ConditionalEmpty(field = "field", conditionField = "condField",
      conditionValue = EclibCoreConstants.VALIDATOR_PARAMETER_NULL)
  public static class NoConditionField {

    private String field;
    private String acondField;

    public NoConditionField(String fieldValue, String condFieldValue) {
      field = fieldValue;
      acondField = condFieldValue;
    }
  }

  public static class MultipleConditions {

    @ConditionalEmpty(field = "field", conditionField = "condField", conditionValue = "a",
        conditionValueIsEmpty = true)
    public static class ValueAndIsEmpty {
      private String field;
      private String condField;
    }

    @ConditionalEmpty(field = "field", conditionField = "condField", conditionValue = "a",
        conditionValueIsNotEmpty = true)
    public static class ValueAndIsNotEmpty {
      private String field;
      private String condField;
    }

    @ConditionalEmpty(field = "field", conditionField = "condField", conditionValue = "a",
        fieldHoldingConditionValue = "otherField")
    public static class ValueAndFieldHoldingConditionValue {
      private String field;
      private String condField;
    }

    @ConditionalEmpty(field = "field", conditionField = "condField", conditionValueIsEmpty = true,
        conditionValueIsNotEmpty = true)
    public static class IsEmptyAndIsNotEmpty {
      private String field;
      private String condField;
    }

    @ConditionalEmpty(field = "field", conditionField = "condField", conditionValueIsEmpty = true,
        fieldHoldingConditionValue = "otherField")
    public static class IsEmptyAndFieldHoldingConditionValue {
      private String field;
      private String condField;
    }

    @ConditionalEmpty(field = "field", conditionField = "condField",
        conditionValueIsNotEmpty = true, fieldHoldingConditionValue = "otherField")
    public static class IsNotEmptyAndFieldHoldingConditionValue {
      private String field;
      private String condField;
    }
  }

  public static class ValidatesWhenConditionNotSatisfied {

    @ConditionalEmpty(field = "field", conditionField = "condField", conditionValue = "a",
        notEmptyForOtherValues = true)
    public static class TrueClass {
      private String field = null;
      private String condField = "b";
    }

    @ConditionalEmpty(field = "field", conditionField = "condField", conditionValue = "a",
        notEmptyForOtherValues = false)
    public static class FalseClass {
      private String field = null;
      private String condField = "b";
    }
  }

  public static class MultipleFields {

    @ConditionalEmpty(field = {"field1", "field2"}, conditionField = "condField",
        conditionValue = "a")
    public static class AllTrue {
      private String field1 = null;
      private String field2 = "";
      private String condField = "a";
    }

    @ConditionalEmpty(field = {"field1", "field2"}, conditionField = "condField",
        conditionValue = "a")
    public static class OneFalse {
      private String field1 = null;
      private String field2 = "X";
      private String condField = "a";
    }

    @ConditionalEmpty(field = {"field1", "field2"}, conditionField = "condField",
        conditionValue = "a")
    public static class AllFalse {
      private String field1 = "X";
      private String field2 = "X";
      private String condField = "a";
    }

    @ConditionalEmpty(field = {"field1", "field2"}, conditionField = "condField",
        conditionValue = "a", notEmptyForOtherValues = true)
    public static class AllTrueConditionNotSatisfied {
      private String field1 = null;
      private String field2 = "";
      private String condField = "b";
    }

    @ConditionalEmpty(field = {"field1", "field2"}, conditionField = "condField",
        conditionValue = "a", notEmptyForOtherValues = true)
    public static class OneFalseConditionNotSatisfied {
      private String field1 = null;
      private String field2 = "X";
      private String condField = "b";
    }

    @ConditionalEmpty(field = {"field1", "field2"}, conditionField = "condField",
        conditionValue = "a", notEmptyForOtherValues = true)
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

    @ConditionalEmpty(field = "field", conditionField = "condField",
        fieldHoldingConditionValue = "fieldHoldingConditionValue")
    public static class Child extends Parent {

    }
  }
}
