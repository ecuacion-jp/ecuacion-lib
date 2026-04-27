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

import static jp.ecuacion.lib.validation.constraints.enums.ConditionOperator.EQUAL_TO;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionOperator.NOT_EQUAL_TO;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.EMPTY;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.FALSE;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.PATTERN;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.STRING;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.TRUE;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.VALUE_OF_PROPERTY_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.lang.annotation.Annotation;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.internal.ValidateWhenTestBean.TestEnum;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Tests for {@link ValidateWhenValidator#getSatisfiesCondition}. */
@DisplayName("ValidateWhenValidator - getSatisfiesCondition")
public class ValidateWhenValidatorTest {

  private ValidateWhenValidator<Annotation, Object> obj = new ValidateWhenValidator<>() {
    @Override
    protected boolean isValid(Object valueOfField) {
      return false;
    }
  };

  private static final String NULL = EclibValidationConstants.VALIDATOR_PARAMETER_NULL;

  // -------------------------------------------------------------------------
  // STRING condition
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("STRING conditionValue")
  class StringConditionValue {

    @ParameterizedTest(name = "[{index}] condValues={0}, fieldValue=''{1}'' → {2}")
    @MethodSource("equalToArgs")
    @DisplayName("EQUAL_TO: condition satisfied when fieldValue is in condValues")
    void equalTo(String[] condValues, @Nullable String fieldValue, boolean expected) {
      obj.initialize("", new String[]{"field"}, "condField", STRING, EQUAL_TO,
          condValues, "", "", false);
      assertThat(obj.getSatisfiesCondition(
          new ValidateWhenTestBean.ConditionValueString(fieldValue))).isEqualTo(expected);
    }

    static Stream<Arguments> equalToArgs() {
      return Stream.of(
          // condValues = [__NULL__]
          Arguments.of(new String[]{NULL}, null, true),
          Arguments.of(new String[]{NULL}, "", false),
          Arguments.of(new String[]{NULL}, "a", false),
          // condValues = [""]
          Arguments.of(new String[]{""}, null, false),
          Arguments.of(new String[]{""}, "", true),
          Arguments.of(new String[]{""}, "a", false),
          // condValues = ["a"]
          Arguments.of(new String[]{"a"}, null, false),
          Arguments.of(new String[]{"a"}, "", false),
          Arguments.of(new String[]{"a"}, "a", true),
          Arguments.of(new String[]{"a"}, "b", false),
          // condValues = [__NULL__, ""]
          Arguments.of(new String[]{NULL, ""}, null, true),
          Arguments.of(new String[]{NULL, ""}, "", true),
          Arguments.of(new String[]{NULL, ""}, "a", false),
          // condValues = [__NULL__, "a"]
          Arguments.of(new String[]{NULL, "a"}, null, true),
          Arguments.of(new String[]{NULL, "a"}, "", false),
          Arguments.of(new String[]{NULL, "a"}, "a", true),
          Arguments.of(new String[]{NULL, "a"}, "b", false),
          // condValues = ["", "a"]
          Arguments.of(new String[]{"", "a"}, null, false),
          Arguments.of(new String[]{"", "a"}, "", true),
          Arguments.of(new String[]{"", "a"}, "a", true),
          Arguments.of(new String[]{"", "a"}, "b", false),
          // condValues = [__NULL__, "", "a"]
          Arguments.of(new String[]{NULL, "", "a"}, null, true),
          Arguments.of(new String[]{NULL, "", "a"}, "", true),
          Arguments.of(new String[]{NULL, "", "a"}, "a", true),
          Arguments.of(new String[]{NULL, "", "a"}, "b", false)
      );
    }

    @ParameterizedTest(name = "[{index}] condValues={0}, fieldValue=''{1}'' → {2}")
    @MethodSource("notEqualToArgs")
    @DisplayName("NOT_EQUAL_TO: condition satisfied when fieldValue is not in condValues")
    void notEqualTo(String[] condValues, @Nullable String fieldValue, boolean expected) {
      obj.initialize("", new String[]{"field"}, "condField", STRING, NOT_EQUAL_TO,
          condValues, "", "", false);
      assertThat(obj.getSatisfiesCondition(
          new ValidateWhenTestBean.ConditionValueString(fieldValue))).isEqualTo(expected);
    }

    static Stream<Arguments> notEqualToArgs() {
      return Stream.of(
          // condValues = [__NULL__]
          Arguments.of(new String[]{NULL}, null, false),
          Arguments.of(new String[]{NULL}, "", true),
          Arguments.of(new String[]{NULL}, "a", true),
          // condValues = [""]
          Arguments.of(new String[]{""}, null, true),
          Arguments.of(new String[]{""}, "", false),
          Arguments.of(new String[]{""}, "a", true),
          // condValues = ["a"]
          Arguments.of(new String[]{"a"}, null, true),
          Arguments.of(new String[]{"a"}, "", true),
          Arguments.of(new String[]{"a"}, "a", false),
          Arguments.of(new String[]{"a"}, "b", true),
          // condValues = [__NULL__, ""]
          Arguments.of(new String[]{NULL, ""}, null, false),
          Arguments.of(new String[]{NULL, ""}, "", false),
          Arguments.of(new String[]{NULL, ""}, "a", true),
          // condValues = [__NULL__, "a"]
          Arguments.of(new String[]{NULL, "a"}, null, false),
          Arguments.of(new String[]{NULL, "a"}, "", true),
          Arguments.of(new String[]{NULL, "a"}, "a", false),
          Arguments.of(new String[]{NULL, "a"}, "b", true),
          // condValues = ["", "a"]
          Arguments.of(new String[]{"", "a"}, null, true),
          Arguments.of(new String[]{"", "a"}, "", false),
          Arguments.of(new String[]{"", "a"}, "a", false),
          Arguments.of(new String[]{"", "a"}, "b", true),
          // condValues = [__NULL__, "", "a"]
          Arguments.of(new String[]{NULL, "", "a"}, null, false),
          Arguments.of(new String[]{NULL, "", "a"}, "", false),
          Arguments.of(new String[]{NULL, "", "a"}, "a", false),
          Arguments.of(new String[]{NULL, "", "a"}, "b", true)
      );
    }
  }

  // -------------------------------------------------------------------------
  // PATTERN condition
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("PATTERN conditionValue")
  class PatternConditionValue {

    @Nested
    @DisplayName("EQUAL_TO operator")
    class EqualTo {

      @Test
      @DisplayName("throws RuntimeException when regexp is not set")
      void throwsWhenRegexpNotSet() {
        obj.initialize("", new String[]{"field"}, "condField", PATTERN, EQUAL_TO,
            new String[]{NULL}, "", "", false);
        assertThatThrownBy(() ->
            obj.getSatisfiesCondition(new ValidateWhenTestBean.ConditionValueString("test")))
            .isInstanceOf(RuntimeException.class);
      }

      @Test
      @DisplayName("throws PatternSyntaxException when regexp is malformed")
      void throwsWhenRegexpMalformed() {
        obj.initialize("", new String[]{"field"}, "condField", PATTERN, EQUAL_TO,
            new String[]{NULL}, "^[.*$", "", false);
        assertThatThrownBy(() ->
            obj.getSatisfiesCondition(new ValidateWhenTestBean.ConditionValueString("test")))
            .isInstanceOf(PatternSyntaxException.class);
      }

      @Test
      @DisplayName("returns false when conditionField value is null or blank")
      void returnsFalseForNullOrBlankConditionValue() {
        obj.initialize("", new String[]{"field"}, "condField", PATTERN, EQUAL_TO,
            new String[]{NULL}, ".*", "", false);
        assertThat(obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueString(null))).isFalse();
        assertThat(obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueString(""))).isFalse();
      }

      @Test
      @DisplayName("returns true when conditionField value matches the pattern")
      void matchesPattern() {
        obj.initialize("", new String[]{"field"}, "condField", PATTERN, EQUAL_TO,
            new String[]{NULL}, "^[A-Z]*$", "", false);
        assertThat(obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueString("ABC"))).isTrue();
        assertThat(obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueString("abc"))).isFalse();
      }
    }

    @Nested
    @DisplayName("NOT_EQUAL_TO operator")
    class NotEqualTo {

      @Test
      @DisplayName("throws RuntimeException when regexp is not set")
      void throwsWhenRegexpNotSet() {
        obj.initialize("", new String[]{"field"}, "condField", PATTERN, NOT_EQUAL_TO,
            new String[]{NULL}, "", "", false);
        assertThatThrownBy(() ->
            obj.getSatisfiesCondition(new ValidateWhenTestBean.ConditionValueString("test")))
            .isInstanceOf(RuntimeException.class);
      }

      @Test
      @DisplayName("throws PatternSyntaxException when regexp is malformed")
      void throwsWhenRegexpMalformed() {
        obj.initialize("", new String[]{"field"}, "condField", PATTERN, NOT_EQUAL_TO,
            new String[]{NULL}, "^[.*$", "", false);
        assertThatThrownBy(() ->
            obj.getSatisfiesCondition(new ValidateWhenTestBean.ConditionValueString("test")))
            .isInstanceOf(PatternSyntaxException.class);
      }

      @Test
      @DisplayName("returns false when conditionField value is null or blank")
      void returnsFalseForNullOrBlankConditionValue() {
        obj.initialize("", new String[]{"field"}, "condField", PATTERN, NOT_EQUAL_TO,
            new String[]{NULL}, ".*", "", false);
        assertThat(obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueString(null))).isFalse();
        assertThat(obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueString(""))).isFalse();
      }

      @Test
      @DisplayName("returns true when conditionField value does not match the pattern")
      void doesNotMatchPattern() {
        obj.initialize("", new String[]{"field"}, "condField", PATTERN, NOT_EQUAL_TO,
            new String[]{NULL}, "^[A-Z]*$", "", false);
        assertThat(obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueString("ABC"))).isFalse();
        assertThat(obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueString("abc"))).isTrue();
      }
    }
  }

  // -------------------------------------------------------------------------
  // EMPTY condition
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("EMPTY conditionValue")
  class EmptyConditionValue {

    @ParameterizedTest(name = "[{index}] {0} → {1}")
    @MethodSource("equalToArgs")
    @DisplayName("EQUAL_TO: condition satisfied when conditionField is empty/null")
    void equalTo(Object bean, boolean expected) {
      obj.initialize("", new String[]{"field"}, "condField", EMPTY, EQUAL_TO,
          new String[]{NULL}, "", "", false);
      assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
    }

    static Stream<Arguments> equalToArgs() {
      return Stream.of(
          // String
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsEmpty.xString(null), true),
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsEmpty.xString(""), true),
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsEmpty.xString("a"), false),
          // Integer
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsEmpty.xInteger(null), true),
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsEmpty.xInteger(1), false),
          // Enum
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsEmpty.TestEnum(null), true),
          Arguments.of(
              new ValidateWhenTestBean.ConditionValueIsEmpty.TestEnum(TestEnum.value1), false)
      );
    }

    @ParameterizedTest(name = "[{index}] {0} → {1}")
    @MethodSource("notEqualToArgs")
    @DisplayName("NOT_EQUAL_TO: condition satisfied when conditionField is not empty")
    void notEqualTo(Object bean, boolean expected) {
      obj.initialize("", new String[]{"field"}, "condField", EMPTY, NOT_EQUAL_TO,
          new String[]{NULL}, "", "", false);
      assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
    }

    static Stream<Arguments> notEqualToArgs() {
      return Stream.of(
          // String
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsNotEmpty.xString(null), false),
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsNotEmpty.xString(""), false),
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsNotEmpty.xString("a"), true),
          // Integer
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsNotEmpty.xInteger(null), false),
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsNotEmpty.xInteger(1), true),
          // Enum
          Arguments.of(new ValidateWhenTestBean.ConditionValueIsNotEmpty.TestEnum(null), false),
          Arguments.of(
              new ValidateWhenTestBean.ConditionValueIsNotEmpty.TestEnum(TestEnum.value1), true)
      );
    }
  }

  // -------------------------------------------------------------------------
  // Boolean conditions (TRUE / FALSE)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Boolean conditionValues (TRUE / FALSE)")
  class BooleanConditionValues {

    @ParameterizedTest(name = "[{index}] condField={0} → {1}")
    @MethodSource("trueEqualToArgs")
    @DisplayName("TRUE / EQUAL_TO: condition satisfied when conditionField is true")
    void trueEqualTo(@Nullable Boolean condField, boolean expected) {
      obj.initialize("", new String[]{"field"}, "condField", TRUE, EQUAL_TO,
          new String[]{NULL}, "", "", false);
      assertThat(obj.getSatisfiesCondition(
          new ValidateWhenTestBean.ConditionValueBoolean.Boolean(condField))).isEqualTo(expected);
    }

    static Stream<Arguments> trueEqualToArgs() {
      return Stream.of(
          Arguments.of(null, false),
          Arguments.of(true, true),
          Arguments.of(false, false)
      );
    }

    @ParameterizedTest(name = "[{index}] condField={0} → {1}")
    @MethodSource("trueNotEqualToArgs")
    @DisplayName("TRUE / NOT_EQUAL_TO: condition satisfied when conditionField is not true")
    void trueNotEqualTo(@Nullable Boolean condField, boolean expected) {
      obj.initialize("", new String[]{"field"}, "condField", TRUE, NOT_EQUAL_TO,
          new String[]{NULL}, "", "", false);
      assertThat(obj.getSatisfiesCondition(
          new ValidateWhenTestBean.ConditionValueBoolean.Boolean(condField))).isEqualTo(expected);
    }

    static Stream<Arguments> trueNotEqualToArgs() {
      return Stream.of(
          Arguments.of(null, true),
          Arguments.of(true, false),
          Arguments.of(false, true)
      );
    }

    @ParameterizedTest(name = "[{index}] condField={0} → {1}")
    @MethodSource("falseEqualToArgs")
    @DisplayName("FALSE / EQUAL_TO: condition satisfied when conditionField is false")
    void falseEqualTo(@Nullable Boolean condField, boolean expected) {
      obj.initialize("", new String[]{"field"}, "condField", FALSE, EQUAL_TO,
          new String[]{NULL}, "", "", false);
      assertThat(obj.getSatisfiesCondition(
          new ValidateWhenTestBean.ConditionValueBoolean.Boolean(condField))).isEqualTo(expected);
    }

    static Stream<Arguments> falseEqualToArgs() {
      return Stream.of(
          Arguments.of(null, false),
          Arguments.of(true, false),
          Arguments.of(false, true)
      );
    }

    @ParameterizedTest(name = "[{index}] condField={0} → {1}")
    @MethodSource("falseNotEqualToArgs")
    @DisplayName("FALSE / NOT_EQUAL_TO: condition satisfied when conditionField is not false")
    void falseNotEqualTo(@Nullable Boolean condField, boolean expected) {
      obj.initialize("", new String[]{"field"}, "condField", FALSE, NOT_EQUAL_TO,
          new String[]{NULL}, "", "", false);
      assertThat(obj.getSatisfiesCondition(
          new ValidateWhenTestBean.ConditionValueBoolean.Boolean(condField))).isEqualTo(expected);
    }

    static Stream<Arguments> falseNotEqualToArgs() {
      return Stream.of(
          Arguments.of(null, true),
          Arguments.of(true, true),
          Arguments.of(false, false)
      );
    }
  }

  // -------------------------------------------------------------------------
  // VALUE_OF_PROPERTY_PATH condition
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("VALUE_OF_PROPERTY_PATH conditionValue")
  class ValueOfPropertyPathConditionValue {

    @Nested
    @DisplayName("EQUAL_TO operator")
    class EqualTo {

      @Test
      @DisplayName("throws RuntimeException when conditionValueField does not exist or types mismatch")
      void throwsOnInvalidFieldConfig() {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH, EQUAL_TO,
            new String[]{NULL}, "", "fieldHoldingConditionValue", false);

        assertThatThrownBy(() -> obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueField.NotExist(null)))
            .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueField.DataTypeNotMatch("a", 1)))
            .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueField.DataTypeNotMatchArray("a",
                new Integer[]{1})))
            .isInstanceOf(RuntimeException.class);
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("stringArgs")
      @DisplayName("String: condition satisfied when condField equals fieldHoldingConditionValue")
      void stringType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH, EQUAL_TO,
            new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> stringArgs() {
        return Stream.of(
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.xString(null, null), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.xString(null, ""), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.xString(null, "a"), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.xString("", null), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.xString("", ""), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.xString("", "a"), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.xString("a", null), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.xString("a", ""), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.xString("a", "a"), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.xString("a", "b"), false)
        );
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("integerArgs")
      @DisplayName("Integer: condition satisfied when condField equals fieldHoldingConditionValue")
      void integerType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH, EQUAL_TO,
            new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> integerArgs() {
        return Stream.of(
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xInteger(null, null), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xInteger(null, 1), false),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xInteger(1, null), false),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xInteger(1, 1), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xInteger(1, 2), false)
        );
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("enumArgs")
      @DisplayName("Enum: condition satisfied when condField equals fieldHoldingConditionValue")
      void enumType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH, EQUAL_TO,
            new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> enumArgs() {
        return Stream.of(
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.AnEnum(null, null), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.AnEnum(null, TestEnum.value1), false),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.AnEnum(TestEnum.value1, null), false),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.AnEnum(
                    TestEnum.value1, TestEnum.value1), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.AnEnum(
                    TestEnum.value1, TestEnum.value2), false)
        );
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("stringArrayArgs")
      @DisplayName("String[]: condition satisfied when condField is in fieldHoldingConditionValue")
      void stringArrayType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH, EQUAL_TO,
            new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> stringArrayArgs() {
        return Stream.of(
            // null condField
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{null}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{NULL}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{""}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{"a"}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{null, "a"}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{"", "a"}), false),
            // empty condField
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{null}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{NULL}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{""}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{"a"}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{"", "a"}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{null, "a"}), false),
            // non-empty condField
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{null}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{NULL}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{""}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{"a"}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{"b"}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{null, "a"}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{"", "b"}), false)
        );
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("integerArrayArgs")
      @DisplayName("Integer[]: condition satisfied when condField is in fieldHoldingConditionValue")
      void integerArrayType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH, EQUAL_TO,
            new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> integerArrayArgs() {
        return Stream.of(
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                null, new Integer[]{null}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                null, new Integer[]{1}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                null, new Integer[]{null, 1}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                null, new Integer[]{1, 2}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                1, new Integer[]{null}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                1, new Integer[]{1}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                1, new Integer[]{2}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                1, new Integer[]{null, 1}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                1, new Integer[]{null, 2}), false)
        );
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("enumArrayArgs")
      @DisplayName("Enum[]: condition satisfied when condField is in fieldHoldingConditionValue")
      void enumArrayType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH, EQUAL_TO,
            new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> enumArrayArgs() {
        return Stream.of(
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                null, new TestEnum[]{null}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                null, new TestEnum[]{TestEnum.value1}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                null, new TestEnum[]{null, TestEnum.value1}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                null, new TestEnum[]{TestEnum.value1, TestEnum.value2}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                TestEnum.value1, new TestEnum[]{null}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                TestEnum.value1, new TestEnum[]{TestEnum.value1}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                TestEnum.value1, new TestEnum[]{TestEnum.value2}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                TestEnum.value1, new TestEnum[]{null, TestEnum.value1}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                TestEnum.value1, new TestEnum[]{null, TestEnum.value2}), false)
        );
      }
    }

    @Nested
    @DisplayName("NOT_EQUAL_TO operator")
    class NotEqualTo {

      @Test
      @DisplayName("throws RuntimeException when conditionValueField does not exist or types mismatch")
      void throwsOnInvalidFieldConfig() {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH,
            NOT_EQUAL_TO, new String[]{NULL}, "", "fieldHoldingConditionValue", false);

        assertThatThrownBy(() -> obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueField.NotExist(null)))
            .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueField.DataTypeNotMatch("a", 1)))
            .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> obj.getSatisfiesCondition(
            new ValidateWhenTestBean.ConditionValueField.DataTypeNotMatchArray("a",
                new Integer[]{1})))
            .isInstanceOf(RuntimeException.class);
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("stringArgs")
      @DisplayName("String: condition satisfied when condField does not equal")
      void stringType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH,
            NOT_EQUAL_TO, new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> stringArgs() {
        return Stream.of(
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xString(null, null), false),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xString(null, ""), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xString(null, "a"), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xString("", null), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xString("", ""), false),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xString("", "a"), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xString("a", null), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xString("a", ""), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xString("a", "a"), false),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xString("a", "b"), true)
        );
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("integerArgs")
      @DisplayName("Integer: condition satisfied when condField does not equal")
      void integerType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH,
            NOT_EQUAL_TO, new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> integerArgs() {
        return Stream.of(
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xInteger(null, null), false),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xInteger(null, 1), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xInteger(1, null), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xInteger(1, 1), false),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.xInteger(1, 2), true)
        );
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("enumArgs")
      @DisplayName("Enum: condition satisfied when condField does not equal")
      void enumType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH,
            NOT_EQUAL_TO, new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> enumArgs() {
        return Stream.of(
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.AnEnum(null, null), false),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.AnEnum(null, TestEnum.value1), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.AnEnum(TestEnum.value1, null), true),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.AnEnum(
                    TestEnum.value1, TestEnum.value1), false),
            Arguments.of(
                new ValidateWhenTestBean.ConditionValueField.AnEnum(
                    TestEnum.value1, TestEnum.value2), true)
        );
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("stringArrayArgs")
      @DisplayName("String[]: condition satisfied when condField is not in array")
      void stringArrayType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH,
            NOT_EQUAL_TO, new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> stringArrayArgs() {
        return Stream.of(
            // null condField
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{null}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{NULL}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{""}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{"a"}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{null, "a"}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                null, new String[]{"", "a"}), true),
            // empty condField
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{null}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{NULL}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{""}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{"a"}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{"", "a"}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "", new String[]{null, "a"}), true),
            // non-empty condField
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{null}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{NULL}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{""}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{"a"}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{"b"}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{null, "a"}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.StringArray(
                "a", new String[]{"", "b"}), true)
        );
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("integerArrayArgs")
      @DisplayName("Integer[]: condition satisfied when condField is not in array")
      void integerArrayType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH,
            NOT_EQUAL_TO, new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> integerArrayArgs() {
        return Stream.of(
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                null, new Integer[]{null}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                null, new Integer[]{1}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                null, new Integer[]{null, 1}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                null, new Integer[]{1, 2}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                1, new Integer[]{null}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                1, new Integer[]{1}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                1, new Integer[]{2}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                1, new Integer[]{null, 1}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.IntegerArray(
                1, new Integer[]{null, 2}), true)
        );
      }

      @ParameterizedTest(name = "[{index}] {0} → {1}")
      @MethodSource("enumArrayArgs")
      @DisplayName("Enum[]: condition satisfied when condField is not in array")
      void enumArrayType(Object bean, boolean expected) {
        obj.initialize("", new String[]{"field"}, "condField", VALUE_OF_PROPERTY_PATH,
            NOT_EQUAL_TO, new String[]{NULL}, "", "fieldHoldingConditionValue", false);
        assertThat(obj.getSatisfiesCondition(bean)).isEqualTo(expected);
      }

      static Stream<Arguments> enumArrayArgs() {
        return Stream.of(
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                null, new TestEnum[]{null}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                null, new TestEnum[]{TestEnum.value1}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                null, new TestEnum[]{null, TestEnum.value1}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                null, new TestEnum[]{TestEnum.value1, TestEnum.value2}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                TestEnum.value1, new TestEnum[]{null}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                TestEnum.value1, new TestEnum[]{TestEnum.value1}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                TestEnum.value1, new TestEnum[]{TestEnum.value2}), true),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                TestEnum.value1, new TestEnum[]{null, TestEnum.value1}), false),
            Arguments.of(new ValidateWhenTestBean.ConditionValueField.EnumArray(
                TestEnum.value1, new TestEnum[]{null, TestEnum.value2}), true)
        );
      }
    }
  }
}
