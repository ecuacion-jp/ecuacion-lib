package jp.ecuacion.lib.core.jakartavalidation.validator.internal;

import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.jakartavalidation.validator.ConditionalEmpty;

@SuppressWarnings("unused")
public class ConditinalValidatorTestBean {

  public static enum TestEnum {
    value1, value2, value3;
  }

  public static class ConditionValue {

    public static class Null {

      private String field;
      private String condField;

      public Null(String fieldValue, String condFieldValue) {
        field = fieldValue;
        condField = condFieldValue;
      }
    }

    public static class Blank {

      private String field;
      private String condField;

      public Blank(String fieldValue, String condFieldValue) {
        field = fieldValue;
        condField = condFieldValue;
      }
    }

    public static class StrA {

      private String field;
      private String condField;

      public StrA(String fieldValue, String condFieldValue) {
        field = fieldValue;
        condField = condFieldValue;
      }
    }
  }

  public static class ConditionValueIsEmpty {

    public static class String {

      private java.lang.String field;
      private java.lang.String condField;

      public String(java.lang.String fieldValue, java.lang.String condFieldValue) {
        field = fieldValue;
        condField = condFieldValue;
      }
    }

    public static class Integer {

      private java.lang.String field;
      private java.lang.Integer condField;

      public Integer(java.lang.String fieldValue, java.lang.Integer condFieldValue) {
        field = fieldValue;
        condField = condFieldValue;
      }
    }

    public static class TestEnum {

      private java.lang.String field;
      private ConditinalValidatorTestBean.TestEnum condField;

      public TestEnum(java.lang.String fieldValue,
          ConditinalValidatorTestBean.TestEnum condFieldValue) {
        field = fieldValue;
        condField = condFieldValue;
      }
    }
  }

  public static class ConditionValueIsNotEmpty {
    public static class String {

      private java.lang.String field;
      private java.lang.String condField;

      public String(java.lang.String fieldValue, java.lang.String condFieldValue) {
        field = fieldValue;
        condField = condFieldValue;
      }
    }

    public static class Integer {

      private java.lang.String field;
      private java.lang.Integer condField;

      public Integer(java.lang.String fieldValue, java.lang.Integer condFieldValue) {
        field = fieldValue;
        condField = condFieldValue;
      }
    }

    public static class TestEnum {

      private java.lang.String field;
      private ConditinalValidatorTestBean.TestEnum condField;

      public TestEnum(java.lang.String fieldValue,
          ConditinalValidatorTestBean.TestEnum condFieldValue) {
        field = fieldValue;
        condField = condFieldValue;
      }
    }
  }

  public static class FieldHoldingConditionValue {

    public static class NotExist {

      private java.lang.String field;
      private java.lang.String condField;

      public NotExist(java.lang.String fieldValue, java.lang.String condFieldValue) {
        this.field = fieldValue;
        this.condField = condFieldValue;
      }
    }

    public static class DataTypeNotMatch {

      private java.lang.String field;
      private java.lang.String condField;
      private java.lang.Integer fieldHoldingConditionValue;

      public DataTypeNotMatch(java.lang.String fieldValue, java.lang.String condFieldValue,
          java.lang.Integer fieldHoldingConditionValue) {
        this.field = fieldValue;
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class DataTypeNotMatchArray {

      private java.lang.String field;
      private java.lang.String condField;
      private java.lang.Integer[] fieldHoldingConditionValue;

      public DataTypeNotMatchArray(java.lang.String fieldValue, java.lang.String condFieldValue,
          java.lang.Integer[] fieldHoldingConditionValue) {
        this.field = fieldValue;
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class String {

      private java.lang.String field = "X";
      private java.lang.String condField;
      private java.lang.String fieldHoldingConditionValue;

      public String(java.lang.String condFieldValue, java.lang.String fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class Integer {

      private java.lang.String field = "X";
      private java.lang.Integer condField;
      private java.lang.Integer fieldHoldingConditionValue;

      public Integer(java.lang.Integer condFieldValue,
          java.lang.Integer fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class AnEnum {

      private java.lang.String field = "X";
      private TestEnum condField;
      private TestEnum fieldHoldingConditionValue;

      public AnEnum(TestEnum condFieldValue, TestEnum fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class StringArray {

      private java.lang.String field = "X";
      private java.lang.String condField;
      private java.lang.String[] fieldHoldingConditionValue;

      public StringArray(java.lang.String condFieldValue,
          java.lang.String[] fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class IntegerArray {

      private java.lang.String field = "X";
      private java.lang.Integer condField;
      private java.lang.Integer[] fieldHoldingConditionValue;

      public IntegerArray(java.lang.Integer condFieldValue,
          java.lang.Integer[] fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class EnumArray {

      private java.lang.String field = "X";
      private TestEnum condField;
      private TestEnum[] fieldHoldingConditionValue;

      public EnumArray(TestEnum condFieldValue,
          TestEnum[] fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }
  }
}
