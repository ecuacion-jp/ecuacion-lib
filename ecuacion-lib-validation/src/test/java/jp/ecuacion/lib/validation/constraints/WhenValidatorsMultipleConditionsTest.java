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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Locale;
import java.util.stream.Stream;
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValueState;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@code conditions} (multiple conditions) of {@code When} validators.
 *
 * <p>Evaluation of each single condition is covered by {@code ValidateWhenValidatorTest}
 *     and message parts of each single condition by {@code ValidateWhenValidatorMessageTest}.
 *     This class covers how multiple conditions are combined.</p>
 */
@DisplayName("When validators - multiple conditions")
@SuppressWarnings("removal")
public class WhenValidatorsMultipleConditionsTest {

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtil.addResourceBundlePostfix("lib-validation-test");
  }

  private String getMessage(Object bean, Locale locale) {
    return ExceptionUtil.getMessageList(validator.validate(bean), locale).get(0);
  }

  /**
   * Skips Japanese message tests when the JVM default language is Japanese.
   *
   * <p>In that case Japanese validation message templates fall back to the English ones
   *     in this test environment (it happens with {@code conditionPropertyPath} too,
   *     so it is not specific to {@code conditions}).</p>
   */
  private static void assumeJapaneseMessagesAvailable() {
    assumeFalse(Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage()),
        "Japanese messages cannot be tested when the default language is Japanese.");
  }

  @Nested
  @DisplayName("validation logic")
  class ValidationLogic {

    @ParameterizedTest(name = "type={1}, status={2}, value={0} → valid={3}")
    @MethodSource("twoConditionsArgs")
    @DisplayName("validation is executed only when all the conditions are satisfied")
    void twoConditions(@Nullable String value, String type, @Nullable String status,
        boolean expected) {
      assertThat(validator.validate(new TwoConditionsBean(value, type, status)).isEmpty())
          .isEqualTo(expected);
    }

    @SuppressWarnings("null")
    static Stream<Arguments> twoConditionsArgs() {
      return Stream.of(
          // both satisfied -> validated
          Arguments.of(null, "OTHER", "a", false),
          Arguments.of("x", "OTHER", "a", true),
          // only one satisfied -> not validated
          Arguments.of(null, "OTHER", null, true),
          Arguments.of(null, "NORMAL", "a", true),
          // none satisfied -> not validated
          Arguments.of(null, "NORMAL", null, true));
    }

    @ParameterizedTest(name = "type={1}, status={2}, flag={3}, value={0} → valid={4}")
    @MethodSource("threeConditionsArgs")
    @DisplayName("three conditions with various condition kinds and operators")
    void threeConditions(@Nullable String value, String type, @Nullable String status,
        boolean flag, boolean expected) {
      assertThat(validator.validate(new ThreeConditionsBean(value, type, status, flag)).isEmpty())
          .isEqualTo(expected);
    }

    @SuppressWarnings("null")
    static Stream<Arguments> threeConditionsArgs() {
      return Stream.of(
          // all satisfied (type does not match the pattern, status is empty, flag is true)
          Arguments.of(null, "abc", null, true, false),
          Arguments.of("x", "abc", null, true, true),
          // one of them not satisfied
          Arguments.of(null, "123", null, true, true),
          Arguments.of(null, "abc", "a", true, true),
          Arguments.of(null, "abc", null, false, true));
    }

    @Test
    @DisplayName("a single element of conditions works the same as conditionPropertyPath")
    void singleElement() {
      assertThat(validator.validate(new SingleElementBean(null, "OTHER"))).hasSize(1);
      assertThat(validator.validate(new SingleElementBean(null, "NORMAL"))).isEmpty();
    }

    @Test
    @DisplayName("emptyWhenConditionNotSatisfied validates when not all conditions are satisfied")
    void emptyWhenConditionNotSatisfied() {
      assertThat(validator.validate(new NotSatisfiedBean(null, "OTHER", "a"))).hasSize(1);
      assertThat(validator.validate(new NotSatisfiedBean("x", "OTHER", "a"))).isEmpty();
      assertThat(validator.validate(new NotSatisfiedBean("x", "OTHER", null))).hasSize(1);
      assertThat(validator.validate(new NotSatisfiedBean(null, "OTHER", null))).isEmpty();
    }

    @Test
    @DisplayName("works for other When validators too")
    void otherValidator() {
      assertThat(validator.validate(new NullWhenBean("x", "OTHER", "a"))).hasSize(1);
      assertThat(validator.validate(new NullWhenBean("x", "OTHER", null))).isEmpty();
    }
  }

  @Nested
  @DisplayName("misconfiguration")
  class Misconfiguration {

    @Test
    @DisplayName("throws when both conditionPropertyPath and conditions are set")
    void bothSet() {
      assertThatThrownBy(() -> validator.validate(new BothSetBean(null, "OTHER", "a")))
          .hasStackTraceContaining("when 'conditions' is set");
    }

    @Test
    @DisplayName("throws when other condition* parameter and conditions are set")
    void otherConditionParameterAndConditionsSet() {
      assertThatThrownBy(
          () -> validator.validate(new ConditionOperatorAndConditionsSetBean(null, "OTHER", "a")))
          .hasStackTraceContaining("when 'conditions' is set");
    }

    @Test
    @DisplayName("throws when neither conditionPropertyPath nor conditions is set")
    void neitherSet() {
      assertThatThrownBy(() -> validator.validate(new NeitherSetBean(null)))
          .hasStackTraceContaining("Either 'conditionPropertyPath' or 'conditions' must be set.");
    }

    @Test
    @DisplayName("throws when more than one condition value is set in a Condition")
    void multipleValuesInCondition() {
      assertThatThrownBy(() -> validator.validate(new MultipleValuesInConditionBean(null, "a")))
          .hasStackTraceContaining("You cannot set more than one of");
    }
  }

  @Nested
  @DisplayName("message")
  class Message {

    @Test
    @DisplayName("two conditions (en)")
    void twoConditionsEn() {
      assertThat(getMessage(new TwoConditionsBean(null, "OTHER", "a"), Locale.ENGLISH))
          .isEqualTo("needs to be not empty when 'type' is 'OTHER' and 'status' is not empty");
    }

    @Test
    @DisplayName("two conditions (ja)")
    void twoConditionsJa() {
      assumeJapaneseMessagesAvailable();
      assertThat(getMessage(new TwoConditionsBean(null, "OTHER", "a"), Locale.JAPANESE))
          .isEqualTo("「種別」が「OTHER」で、かつ「ステータス」が空欄以外の場合は空欄以外にしてください");
    }

    @Test
    @DisplayName("three conditions (en)")
    void threeConditionsEn() {
      assertThat(getMessage(new ThreeConditionsBean(null, "abc", null, true), Locale.ENGLISH))
          .isEqualTo("needs to be not empty when 'type' does not match the pattern: ^[0-9]+$"
              + " and 'status' is empty and 'flag' is ON");
    }

    @Test
    @DisplayName("three conditions (ja)")
    void threeConditionsJa() {
      assumeJapaneseMessagesAvailable();
      assertThat(getMessage(new ThreeConditionsBean(null, "abc", null, true), Locale.JAPANESE))
          .isEqualTo("「種別」が「^[0-9]+$」に合致せず、かつ「ステータス」が空欄で、"
              + "かつ「フラグ」が選択された場合は空欄以外にしてください");
    }

    @Test
    @DisplayName("a single element of conditions produces the same message as "
        + "conditionPropertyPath")
    void singleElement() {
      assertThat(getMessage(new SingleElementBean(null, "OTHER"), Locale.ENGLISH))
          .isEqualTo("needs to be not empty when 'type' is 'OTHER'");
      assumeJapaneseMessagesAvailable();
      assertThat(getMessage(new SingleElementBean(null, "OTHER"), Locale.JAPANESE))
          .isEqualTo("「種別」が「OTHER」の場合は空欄以外にしてください");
    }

    @Test
    @DisplayName("with emptyWhenConditionNotSatisfied")
    void emptyWhenConditionNotSatisfied() {
      assertThat(getMessage(new NotSatisfiedBean(null, "OTHER", "a"), Locale.ENGLISH))
          .isEqualTo("needs to be not empty when 'type' is 'OTHER' and 'status' is not empty"
              + ", and empty when otherwise");
    }

    @Test
    @DisplayName("with valueDisplayStringPropertyPath")
    void displayString() {
      assertThat(
          getMessage(new DisplayStringBean(null, "OTHER", "value.from.enum_names", "a", "a"),
              Locale.ENGLISH))
          .isEqualTo("needs to be not empty when 'type' is 'some value' and 'status' is 'a'");
    }
  }

  // -------------------------------------------------------------------------
  // Beans
  // -------------------------------------------------------------------------

  @ItemNameKeyClass("multipleConditionsBean")
  @NotEmptyWhen(propertyPath = "value", conditions = {
      @Condition(propertyPath = "type", valueString = "OTHER"),
      @Condition(propertyPath = "status", valueState = ConditionValueState.NOT_EMPTY)})
  public static record TwoConditionsBean(@Nullable String value, String type,
      @Nullable String status) {}

  @ItemNameKeyClass("multipleConditionsBean")
  @NotEmptyWhen(propertyPath = "value", conditions = {
      @Condition(propertyPath = "type", operator = ConditionOperator.NOT_EQUAL_TO,
          valuePatternRegexp = "^[0-9]+$"),
      @Condition(propertyPath = "status", valueState = ConditionValueState.EMPTY),
      @Condition(propertyPath = "flag", valueBoolean = true)})
  public static record ThreeConditionsBean(@Nullable String value, String type,
      @Nullable String status, boolean flag) {}

  @ItemNameKeyClass("multipleConditionsBean")
  @NotEmptyWhen(propertyPath = "value",
      conditions = {@Condition(propertyPath = "type", valueString = "OTHER")})
  public static record SingleElementBean(@Nullable String value, String type) {}

  @ItemNameKeyClass("multipleConditionsBean")
  @NotEmptyWhen(propertyPath = "value", conditions = {
      @Condition(propertyPath = "type", valueString = "OTHER"),
      @Condition(propertyPath = "status", valueState = ConditionValueState.NOT_EMPTY)},
      emptyWhenConditionNotSatisfied = true)
  public static record NotSatisfiedBean(@Nullable String value, String type,
      @Nullable String status) {}

  @ItemNameKeyClass("multipleConditionsBean")
  @NotEmptyWhen(propertyPath = "value", conditions = {
      @Condition(propertyPath = "type", valueString = "OTHER",
          valueDisplayStringPropertyPath = "typeDisplay"),
      @Condition(propertyPath = "status", valuePropertyPath = "statusValue")})
  public static record DisplayStringBean(@Nullable String value, String type,
      String typeDisplay, String status, String statusValue) {}

  @ItemNameKeyClass("multipleConditionsBean")
  @NullWhen(propertyPath = "value", conditions = {
      @Condition(propertyPath = "type", valueString = "OTHER"),
      @Condition(propertyPath = "status", valueState = ConditionValueState.NOT_EMPTY)})
  public static record NullWhenBean(@Nullable String value, String type,
      @Nullable String status) {}

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "type",
      conditionValueString = "OTHER",
      conditions = {@Condition(propertyPath = "status", valueState = ConditionValueState.EMPTY)})
  public static record BothSetBean(@Nullable String value, String type,
      @Nullable String status) {}

  @NotEmptyWhen(propertyPath = "value", conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditions = {@Condition(propertyPath = "status", valueState = ConditionValueState.EMPTY)})
  public static record ConditionOperatorAndConditionsSetBean(@Nullable String value, String type,
      @Nullable String status) {}

  @NotEmptyWhen(propertyPath = "value", conditionValue = ConditionValue.EMPTY)
  public static record NeitherSetBean(@Nullable String value) {}

  @NotEmptyWhen(propertyPath = "value", conditions = {
      @Condition(propertyPath = "status", valueString = "a",
          valueState = ConditionValueState.EMPTY)})
  public static record MultipleValuesInConditionBean(@Nullable String value,
      @Nullable String status) {}
}
