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
import jakarta.validation.Validator;
import java.util.Locale;
import java.util.stream.Stream;
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests messages for all {@code ValidateWhen} validators.
 *
 * <p>This class covers two concerns:</p>
 * <ul>
 *   <li>Basic wiring: each of the 9 validators produces a non-empty message.</li>
 *   <li>Message parameter creator: all {@code ConditionValue} × {@code ConditionOperator}
 *       × displayString combinations produce the expected message text.
 *       {@code @NotEmptyWhen} is used as the representative validator.</li>
 * </ul>
 */
@DisplayName("ValidateWhen validators - message content")
@SuppressWarnings("ArrayRecordComponent")
public class ValidateWhenValidatorMessageTest {

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtil.addResourceBundlePostfix("lib-validation-test");
  }

  private String getMessage(Object object) {
    return ExceptionUtil.getMessageList(validator.validate(object), Locale.ENGLISH).get(0);
  }

  // -------------------------------------------------------------------------
  // Each validator produces the expected message (basic wiring check)
  // -------------------------------------------------------------------------

  @ParameterizedTest(name = "{1}")
  @MethodSource("validatorBeanAndExpectedMessage")
  @DisplayName("each validator produces the expected message")
  void messageTest(Object bean, String expectedMessage) {
    assertThat(ExceptionUtil.getMessageList(validator.validate(bean), Locale.ENGLISH).get(0))
        .isEqualTo(expectedMessage);
  }

  static Stream<Arguments> validatorBeanAndExpectedMessage() {
    return Stream.of(
        Arguments.of(new EmptyWhenTest(),
            "needs to be empty when 'condition field' is ON"),
        Arguments.of(new NotEmptyWhenTest(),
            "needs to be not empty when 'condition field' is ON"),
        Arguments.of(new TrueWhenTest(),
            "needs to be true when 'condition field' is ON"),
        Arguments.of(new FalseWhenTest(),
            "needs to be false when 'condition field' is ON"),
        Arguments.of(new StringWhenTest(),
            "needs to be a specific value when 'condition field' is ON"),
        Arguments.of(new NotStringWhenTest(),
            "needs to not be a specific value when 'condition field' is ON"),
        Arguments.of(new PatternWhenTest(),
            "needs to match the pattern when 'condition field' is ON"),
        Arguments.of(new NotPatternWhenTest(),
            "needs to not match the pattern when 'condition field' is ON"),
        Arguments.of(new ValueOfPropertyPathWhenTest(),
            "needs to be the value of a specific field when 'condition field' is ON")
    );
  }

  // -------------------------------------------------------------------------
  // All ConditionValue × ConditionOperator × displayString combinations
  // (ValidateWhenValidatorMessageParameterCreator behavior, using @NotEmptyWhen)
  // -------------------------------------------------------------------------

  @ParameterizedTest(name = "{1}")
  @MethodSource("conditionValueAndOperatorCombinations")
  @DisplayName("message content matches expected for each ConditionValue and ConditionOperator")
  public void conditionValueAndOperatorTest(Object bean, String expectedMessage) {
    assertThat(getMessage(bean)).isEqualTo(expectedMessage);
  }

  static Stream<Arguments> conditionValueAndOperatorCombinations() {
    String p1 = "needs to be not empty when ";
    String p2 = p1 + "'condition value' ";
    return Stream.of(
        // EMPTY / NOT_EMPTY with EQUAL_TO / NOT_EQUAL_TO
        Arguments.of(new EmptyEqualToBean(null, null), p2 + "is empty"),
        Arguments.of(new NotEmptyEqualToBean(null, null), p2 + "is not empty"),
        Arguments.of(new EmptyNotEqualToBean(null, "a"), p2 + "is not empty"),
        Arguments.of(new NotEmptyNotEqualToBean(null, "a"), p2 + "is empty"),
        // TRUE / FALSE with EQUAL_TO / NOT_EQUAL_TO
        Arguments.of(new TrueEqualToBean(null, true), p2 + "is ON"),
        Arguments.of(new TrueNotEqualToBean(null, false), p2 + "is not ON"),
        Arguments.of(new FalseEqualToBean(null, false), p2 + "is OFF"),
        Arguments.of(new FalseNotEqualToBean(null, true), p2 + "is not OFF"),
        // STRING, EQUAL_TO: no displayString
        Arguments.of(new StringEqualToSinBean(null, "test"), p2 + "is 'test'"),
        Arguments.of(new StringEqualToMulBean(null, "test1"), p2 + "is one of 'test1', 'test2'"),
        // STRING, EQUAL_TO: displayString not in properties file
        Arguments.of(new StringEqualToWithDisplayStringSinBean(null, "test", "some value"),
            p2 + "is 'some value'"),
        Arguments.of(
            new StringEqualToWithDisplayStringMulBean(null, "test1",
                new String[]{"some value 1", "some value 2"}),
            p2 + "is one of 'some value 1', 'some value 2'"),
        // STRING, EQUAL_TO: displayString resolved from properties file
        Arguments.of(
            new StringEqualToWithDisplayStringSinBean(null, "test", "value.from.enum_names"),
            p2 + "is 'some value'"),
        Arguments.of(
            new StringEqualToWithDisplayStringMulBean(null, "test1",
                new String[]{"value.from.item_names.1", "value.from.item_names.2"}),
            p2 + "is one of 'some value 1', 'some value 2'"),
        // STRING, NOT_EQUAL_TO: no displayString
        Arguments.of(new StringNotEqualToSinBean(null, "a"), p2 + "is not 'test'"),
        Arguments.of(new StringNotEqualToMulBean(null, "a"),
            p2 + "is not one of 'test1', 'test2'"),
        // STRING, NOT_EQUAL_TO: displayString not in properties file
        Arguments.of(new StringNotEqualToWithDisplayStringSinBean(null, "a", "some value"),
            p2 + "is not 'some value'"),
        Arguments.of(
            new StringNotEqualToWithDisplayStringMulBean(null, "a",
                new String[]{"some value 1", "some value 2"}),
            p2 + "is not one of 'some value 1', 'some value 2'"),
        // STRING, NOT_EQUAL_TO: displayString resolved from properties file
        Arguments.of(
            new StringNotEqualToWithDisplayStringSinBean(null, "a", "value.from.enum_names"),
            p2 + "is not 'some value'"),
        Arguments.of(
            new StringNotEqualToWithDisplayStringMulBean(null, "a",
                new String[]{"value.from.item_names.1", "value.from.item_names.2"}),
            p2 + "is not one of 'some value 1', 'some value 2'"),
        // PATTERN, EQUAL_TO
        Arguments.of(new PatternEqualToBean(null, "test"),
            p2 + "matches the pattern: .*test.*"),
        Arguments.of(new PatternEqualToWithDescNonResolvedBean(null, "test"),
            p2 + "matches the pattern: notExistBean.test"),
        Arguments.of(new PatternEqualToWithDescResolvedBean(null, "test"),
            p2 + "matches the pattern: the string which contains 'test'"),
        // PATTERN, NOT_EQUAL_TO
        Arguments.of(new PatternNotEqualToBean(null, "a"),
            p2 + "does not match the pattern: .*test.*"),
        Arguments.of(new PatternNotEqualToWithDescNonResolvedBean(null, "a"),
            p2 + "does not match the pattern: notExistBean.test"),
        Arguments.of(new PatternNotEqualToWithDescResolvedBean(null, "a"),
            p2 + "does not match the pattern: the string which contains 'test'"),
        // VALUE_OF_PROPERTY_PATH, EQUAL_TO: no displayString
        Arguments.of(new PropertyPathEqualToSinBean(null, "abc", "abc"), p2 + "is 'abc'"),
        Arguments.of(new PropertyPathEqualToMulBean(null, "abc", new String[]{"abc", "def"}),
            p2 + "is one of 'abc', 'def'"),
        // VALUE_OF_PROPERTY_PATH, EQUAL_TO: displayString not in properties file
        Arguments.of(
            new PropertyPathEqualToWithDisplayStringSinBean(null, "abc", "abc", "some value"),
            p2 + "is 'some value'"),
        Arguments.of(
            new PropertyPathEqualToWithDisplayStringMulBean(null, "abc",
                new String[]{"abc", "def"}, new String[]{"some value 1", "some value 2"}),
            p2 + "is one of 'some value 1', 'some value 2'"),
        // VALUE_OF_PROPERTY_PATH, EQUAL_TO: displayString resolved from properties file
        Arguments.of(
            new PropertyPathEqualToWithDisplayStringSinBean(null, "abc", "abc",
                "value.from.enum_names"),
            p2 + "is 'some value'"),
        Arguments.of(
            new PropertyPathEqualToWithDisplayStringMulBean(null, "abc",
                new String[]{"abc", "def"},
                new String[]{"value.from.item_names.1", "value.from.item_names.2"}),
            p2 + "is one of 'some value 1', 'some value 2'"),
        // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO: no displayString
        Arguments.of(new PropertyPathNotEqualToSinBean(null, "xyz", "abc"), p2 + "is not 'abc'"),
        Arguments.of(
            new PropertyPathNotEqualToMulBean(null, "xyz", new String[]{"abc", "def"}),
            p2 + "is not one of 'abc', 'def'"),
        // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO: displayString not in properties file
        Arguments.of(
            new PropertyPathNotEqualToWithDisplayStringSinBean(null, "xyz", "abc", "some value"),
            p2 + "is not 'some value'"),
        Arguments.of(
            new PropertyPathNotEqualToWithDisplayStringMulBean(null, "xyz",
                new String[]{"abc", "def"}, new String[]{"some value 1", "some value 2"}),
            p2 + "is not one of 'some value 1', 'some value 2'"),
        // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO: displayString resolved from properties file
        Arguments.of(
            new PropertyPathNotEqualToWithDisplayStringSinBean(null, "xyz", "abc",
                "value.from.enum_names"),
            p2 + "is not 'some value'"),
        Arguments.of(
            new PropertyPathNotEqualToWithDisplayStringMulBean(null, "xyz",
                new String[]{"abc", "def"},
                new String[]{"value.from.item_names.1", "value.from.item_names.2"}),
            p2 + "is not one of 'some value 1', 'some value 2'"),
        // NULL / NOT_NULL with EQUAL_TO / NOT_EQUAL_TO
        Arguments.of(new NullEqualToBean(null, null), p2 + "is null"),
        Arguments.of(new NullNotEqualToBean(null, "a"), p2 + "is not null"),
        Arguments.of(new NotNullEqualToBean(null, "a"), p2 + "is not null"),
        Arguments.of(new NotNullNotEqualToBean(null, null), p2 + "is null"),
        // emptyWhenConditionNotSatisfied
        Arguments.of(new EmptyWhenConditionNotSatisfiedBean(null, null),
            p2 + "is empty, and empty when otherwise")
    );
  }

  @Test
  @DisplayName("locale with parentheses around item name wraps the name in quotes")
  public void messageUsesLocaleParentheses() {
    String msg = ExceptionUtil
        .getMessageList(validator.validate(new EmptyEqualToBean(null, null)), Locale.ITALIAN)
        .get(0);
    assertThat(msg).isEqualTo("needs to be not empty when 'condition value' is empty");
  }

  // -------------------------------------------------------------------------
  // Beans for the basic wiring check (9 validators × 1 case)
  // -------------------------------------------------------------------------

  @EmptyWhen(propertyPath = "field", conditionPropertyPath = "conditionField",
      conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class EmptyWhenTest {
    private String field = "test";
    private boolean conditionField = true;
  }

  @NotEmptyWhen(propertyPath = "field", conditionPropertyPath = "conditionField",
      conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class NotEmptyWhenTest {
    private String field = "";
    private boolean conditionField = true;
  }

  @TrueWhen(propertyPath = "field", conditionPropertyPath = "conditionField",
      conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class TrueWhenTest {
    private Boolean field = false;
    private boolean conditionField = true;
  }

  @FalseWhen(propertyPath = "field", conditionPropertyPath = "conditionField",
      conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class FalseWhenTest {
    private Boolean field = true;
    private boolean conditionField = true;
  }

  @StringWhen(propertyPath = "field", string = {"expected"},
      conditionPropertyPath = "conditionField", conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class StringWhenTest {
    private String field = "wrong";
    private boolean conditionField = true;
  }

  @NotStringWhen(propertyPath = "field", string = {"forbidden"},
      conditionPropertyPath = "conditionField", conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class NotStringWhenTest {
    private String field = "forbidden";
    private boolean conditionField = true;
  }

  @PatternWhen(propertyPath = "field", regexp = "\\d+",
      conditionPropertyPath = "conditionField", conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class PatternWhenTest {
    private String field = "abc";
    private boolean conditionField = true;
  }

  @NotPatternWhen(propertyPath = "field", regexp = "\\d+",
      conditionPropertyPath = "conditionField", conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class NotPatternWhenTest {
    private String field = "123";
    private boolean conditionField = true;
  }

  @ValueOfPropertyPathWhen(propertyPath = "field", valuePropertyPath = "otherField",
      conditionPropertyPath = "conditionField", conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class ValueOfPropertyPathWhenTest {
    private String field = "abc";
    private String otherField = "xyz";
    private boolean conditionField = true;
  }

  // -------------------------------------------------------------------------
  // Beans for ConditionValue × ConditionOperator combinations
  // -------------------------------------------------------------------------

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY)
  public static record EmptyEqualToBean(@Nullable String value, @Nullable String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.EMPTY)
  public static record EmptyNotEqualToBean(@Nullable String value, String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.NOT_EMPTY)
  public static record NotEmptyEqualToBean(@Nullable String value, @Nullable String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.NOT_EMPTY)
  public static record NotEmptyNotEqualToBean(@Nullable String value, String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.TRUE)
  public static record TrueEqualToBean(@Nullable String value, boolean condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.TRUE)
  public static record TrueNotEqualToBean(@Nullable String value, Boolean condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.FALSE)
  public static record FalseEqualToBean(@Nullable String value, boolean condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.FALSE)
  public static record FalseNotEqualToBean(@Nullable String value, Boolean condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.STRING, conditionValueString = {"test"})
  public static record StringEqualToSinBean(@Nullable String value, String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.STRING, conditionValueString = {"test1", "test2"})
  public static record StringEqualToMulBean(@Nullable String value, String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.STRING, conditionValueString = {"test"},
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record StringEqualToWithDisplayStringSinBean(@Nullable String value,
      String condValue, String condEqualDisplay) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.STRING, conditionValueString = {"test1", "test2"},
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record StringEqualToWithDisplayStringMulBean(@Nullable String value,
      String condValue, String[] condEqualDisplay) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.STRING,
      conditionValueString = {"test"})
  public static record StringNotEqualToSinBean(@Nullable String value, String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.STRING,
      conditionValueString = {"test1", "test2"})
  public static record StringNotEqualToMulBean(@Nullable String value, String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.STRING,
      conditionValueString = {"test"},
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record StringNotEqualToWithDisplayStringSinBean(@Nullable String value,
      String condValue, String condEqualDisplay) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.STRING,
      conditionValueString = {"test1", "test2"},
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record StringNotEqualToWithDisplayStringMulBean(@Nullable String value,
      String condValue, String[] condEqualDisplay) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.PATTERN, conditionValuePatternRegexp = ".*test.*")
  public static record PatternEqualToBean(@Nullable String value, String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.PATTERN, conditionValuePatternRegexp = ".*test.*",
      conditionValuePatternDescription = "notExistBean.test")
  public static record PatternEqualToWithDescNonResolvedBean(@Nullable String value,
      String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.PATTERN, conditionValuePatternRegexp = ".*test.*",
      conditionValuePatternDescription = "bean.test")
  public static record PatternEqualToWithDescResolvedBean(@Nullable String value,
      String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.PATTERN,
      conditionValuePatternRegexp = ".*test.*")
  public static record PatternNotEqualToBean(@Nullable String value, String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.PATTERN,
      conditionValuePatternRegexp = ".*test.*",
      conditionValuePatternDescription = "notExistBean.test")
  public static record PatternNotEqualToWithDescNonResolvedBean(@Nullable String value,
      String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.PATTERN,
      conditionValuePatternRegexp = ".*test.*", conditionValuePatternDescription = "bean.test")
  public static record PatternNotEqualToWithDescResolvedBean(@Nullable String value,
      String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathEqualToSinBean(@Nullable String value, String condValue,
      String condEqual) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathEqualToMulBean(@Nullable String value, String condValue,
      String[] condEqual) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathEqualToWithDisplayStringSinBean(@Nullable String value,
      String condValue, String condEqual, String condEqualDisplay) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathEqualToWithDisplayStringMulBean(@Nullable String value,
      String condValue, String[] condEqual, String[] condEqualDisplay) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathNotEqualToSinBean(@Nullable String value, String condValue,
      String condEqual) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathNotEqualToMulBean(@Nullable String value, String condValue,
      String[] condEqual) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathNotEqualToWithDisplayStringSinBean(@Nullable String value,
      String condValue, String condEqual, String condEqualDisplay) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathNotEqualToWithDisplayStringMulBean(@Nullable String value,
      String condValue, String[] condEqual, String[] condEqualDisplay) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.NULL)
  public static record NullEqualToBean(@Nullable String value, @Nullable String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.NULL)
  public static record NullNotEqualToBean(@Nullable String value, String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.NOT_NULL)
  public static record NotNullEqualToBean(@Nullable String value, String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.NOT_NULL)
  public static record NotNullNotEqualToBean(@Nullable String value, @Nullable String condValue) {}

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY, emptyWhenConditionNotSatisfied = true)
  public static record EmptyWhenConditionNotSatisfiedBean(@Nullable String value,
      @Nullable String condValue) {}
}
