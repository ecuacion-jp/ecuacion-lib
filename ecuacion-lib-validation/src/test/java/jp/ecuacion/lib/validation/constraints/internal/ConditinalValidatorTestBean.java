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
package jp.ecuacion.lib.validation.constraints.internal;

import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.validation.constraints.EmptyWhen;

@SuppressWarnings("unused")
public class ConditinalValidatorTestBean {

  public static enum TestEnum {
    value1, value2, value3;
  }

  public static class ConditionValueString {

    private String field = "X";
    private String condField;

    public ConditionValueString(String condFieldValue) {
      condField = condFieldValue;
    }

    // public static class Null {
    //
    // private String field;
    // private String condField;
    //
    // public Null(String fieldValue, String condFieldValue) {
    // field = fieldValue;
    // condField = condFieldValue;
    // }
    // }
    //
    // public static class Blank {
    //
    // private String field;
    // private String condField;
    //
    // public Blank(String fieldValue, String condFieldValue) {
    // field = fieldValue;
    // condField = condFieldValue;
    // }
    // }
    //
    // public static class StrA {
    //
    // private String field;
    // private String condField;
    //
    // public StrA(String fieldValue, String condFieldValue) {
    // field = fieldValue;
    // condField = condFieldValue;
    // }
    // }
  }

  public static class ConditionValueIsEmpty {

    public static class String {

      private java.lang.String field = "X";
      private java.lang.String condField;

      public String(java.lang.String condFieldValue) {
        condField = condFieldValue;
      }
    }

    public static class Integer {

      private java.lang.String field = "X";
      private java.lang.Integer condField;

      public Integer(java.lang.Integer condFieldValue) {
        condField = condFieldValue;
      }
    }

    public static class TestEnum {

      private java.lang.String field = "X";
      private ConditinalValidatorTestBean.TestEnum condField;

      public TestEnum(ConditinalValidatorTestBean.TestEnum condFieldValue) {
        condField = condFieldValue;
      }
    }
  }

  public static class ConditionValueIsNotEmpty {
    public static class String {

      private java.lang.String field = "X";
      private java.lang.String condField;

      public String(java.lang.String condFieldValue) {
        condField = condFieldValue;
      }
    }

    public static class Integer {

      private java.lang.String field = "X";
      private java.lang.Integer condField;

      public Integer(java.lang.Integer condFieldValue) {
        condField = condFieldValue;
      }
    }

    public static class TestEnum {

      private java.lang.String field = "X";
      private ConditinalValidatorTestBean.TestEnum condField;

      public TestEnum(ConditinalValidatorTestBean.TestEnum condFieldValue) {
        condField = condFieldValue;
      }
    }
  }

  public static class ConditionValueBoolean {

    public static class Boolean {

      private java.lang.String field = "X";
      private java.lang.Boolean condField;

      public Boolean(java.lang.Boolean condFieldValue) {
        condField = condFieldValue;
      }
    }
  }

  public static class ConditionValueField {

    public static class NotExist {

      private java.lang.String field = "X";
      private java.lang.String condField;

      public NotExist(java.lang.String condFieldValue) {
        this.condField = condFieldValue;
      }
    }

    public static class DataTypeNotMatch {

      private java.lang.String field = "X";
      private java.lang.String condField;
      private java.lang.Integer fieldHoldingConditionValue;

      public DataTypeNotMatch(java.lang.String condFieldValue,
          java.lang.Integer fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class DataTypeNotMatchArray {

      private java.lang.String field = "X";
      private java.lang.String condField;
      private java.lang.Integer[] fieldHoldingConditionValue;

      public DataTypeNotMatchArray(java.lang.String condFieldValue,
          java.lang.Integer[] fieldHoldingConditionValue) {
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

      public EnumArray(TestEnum condFieldValue, TestEnum[] fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }
  }
}
