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

import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
public class ValidateWhenTestBean {

  public static enum TestEnum {
    value1, value2, value3;
  }

  public static class ConditionValueString {

    private String field = "X";
    private @Nullable String condField;

    public ConditionValueString(@Nullable String condFieldValue) {
      condField = condFieldValue;
    }
  }

  public static class ConditionValueIsEmpty {

    public static class xString {

      private java.lang.String field = "X";
      private @Nullable String condField;

      public xString(@Nullable String condFieldValue) {
        condField = condFieldValue;
      }
    }

    public static class xInteger {

      private java.lang.String field = "X";
      private @Nullable Integer condField;

      public xInteger(@Nullable Integer condFieldValue) {
        condField = condFieldValue;
      }
    }

    public static class TestEnum {

      private java.lang.String field = "X";
      private ValidateWhenTestBean.@Nullable TestEnum condField;

      public TestEnum(ValidateWhenTestBean.@Nullable TestEnum condFieldValue) {
        condField = condFieldValue;
      }
    }
  }

  public static class ConditionValueIsNotEmpty {
    public static class xString {

      private java.lang.String field = "X";
      private java.lang.@Nullable String condField;

      public xString(java.lang.@Nullable String condFieldValue) {
        condField = condFieldValue;
      }
    }

    public static class xInteger {

      private java.lang.String field = "X";
      private @Nullable Integer condField;

      public xInteger(@Nullable Integer condFieldValue) {
        condField = condFieldValue;
      }
    }

    public static class TestEnum {

      private java.lang.String field = "X";
      private ValidateWhenTestBean.@Nullable TestEnum condField;

      public TestEnum(ValidateWhenTestBean.@Nullable TestEnum condFieldValue) {
        condField = condFieldValue;
      }
    }
  }

  public static class ConditionValueBoolean {

    public static class Boolean {

      private java.lang.String field = "X";
      private java.lang.@Nullable Boolean condField;

      public Boolean(java.lang.@Nullable Boolean condFieldValue) {
        condField = condFieldValue;
      }
    }
  }

  public static class ConditionValueField {

    public static class NotExist {

      private java.lang.String field = "X";
      private @Nullable String condField;

      public NotExist(@Nullable String condFieldValue) {
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

    public static class xString {

      private java.lang.String field = "X";
      private @Nullable String condField;
      private @Nullable String fieldHoldingConditionValue;

      public xString(@Nullable String condFieldValue, @Nullable String fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class xInteger {

      private java.lang.String field = "X";
      private @Nullable Integer condField;
      private @Nullable Integer fieldHoldingConditionValue;

      public xInteger(@Nullable Integer condFieldValue,
          @Nullable Integer fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class AnEnum {

      private java.lang.String field = "X";
      private @Nullable TestEnum condField;
      private @Nullable TestEnum fieldHoldingConditionValue;

      public AnEnum(@Nullable TestEnum condFieldValue, @Nullable TestEnum fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class StringArray {

      private java.lang.String field = "X";
      private @Nullable String condField;
      private java.lang.String[] fieldHoldingConditionValue;

      public StringArray(@Nullable String condFieldValue,
          java.lang.String[] fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class IntegerArray {

      private java.lang.String field = "X";
      private @Nullable Integer condField;
      private java.lang.Integer[] fieldHoldingConditionValue;

      public IntegerArray(@Nullable Integer condFieldValue,
          java.lang.Integer[] fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }

    public static class EnumArray {

      private String field = "X";
      private @Nullable TestEnum condField;
      private TestEnum[] fieldHoldingConditionValue;

      public EnumArray(@Nullable TestEnum condFieldValue, TestEnum[] fieldHoldingConditionValue) {
        this.condField = condFieldValue;
        this.fieldHoldingConditionValue = fieldHoldingConditionValue;
      }
    }
  }
}
