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
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests that the deprecated validator-specific parameters
 *     (like {@code falseWhenConditionNotSatisfied}) work in the same way as
 *     {@code oppositeWhenConditionNotSatisfied}.
 *
 * <p>In every bean the condition is not satisfied ({@code cond} is {@code false})
 *     and the value violates the opposite rule.</p>
 */
@DisplayName("Deprecated xxxWhenConditionNotSatisfied parameters")
@SuppressWarnings("removal")
public class DeprecatedWhenConditionNotSatisfiedTest {

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private List<String> messages(Object bean) {
    return ExceptionUtil.getMessageList(validator.validate(bean), Locale.ENGLISH);
  }

  static Stream<Arguments> deprecatedAndNewBeans() {
    return Stream.of(
        Arguments.of(new TrueOld(true, false), new TrueNew(true, false)),
        Arguments.of(new FalseOld(false, false), new FalseNew(false, false)),
        Arguments.of(new NullOld(null, false), new NullNew(null, false)),
        Arguments.of(new NotNullOld("a", false), new NotNullNew("a", false)),
        Arguments.of(new EmptyOld("", false), new EmptyNew("", false)),
        Arguments.of(new NotEmptyOld("a", false), new NotEmptyNew("a", false)),
        Arguments.of(new PatternOld("a", false), new PatternNew("a", false)),
        Arguments.of(new NotPatternOld("b", false), new NotPatternNew("b", false)),
        Arguments.of(new StringOld("a", false), new StringNew("a", false)),
        Arguments.of(new NotStringOld("b", false), new NotStringNew("b", false)),
        Arguments.of(new ValueOfOld("a", "a", false), new ValueOfNew("a", "a", false)),
        Arguments.of(new NotValueOfOld("a", "b", false), new NotValueOfNew("a", "b", false)));
  }

  @ParameterizedTest
  @MethodSource("deprecatedAndNewBeans")
  @DisplayName("deprecated parameter validates the opposite with the same message")
  public void deprecatedParameterWorksAsNewParameter(Object deprecatedBean, Object newBean) {
    List<String> deprecatedMessages = messages(deprecatedBean);
    List<String> newMessages = messages(newBean);

    assertThat(newMessages).hasSize(1);
    assertThat(newMessages.get(0)).contains("when otherwise");
    assertThat(deprecatedMessages).isEqualTo(newMessages);
  }

  @Test
  @DisplayName("the opposite is validated when both parameters are true")
  public void bothParametersTrue() {
    assertThat(messages(new NotEmptyBoth("a", false)))
        .isEqualTo(messages(new NotEmptyNew("a", false)));
  }

  @Test
  @DisplayName("the opposite is not validated when both parameters are false")
  public void bothParametersFalse() {
    assertThat(validator.validate(new NotEmptyNone("a", false))).isEmpty();
  }

  // @TrueWhen

  @ItemNameKeyClass("oppositeTest")
  @TrueWhen(propertyPath = "value", conditionPropertyPath = "cond", conditionValueBoolean = true,
      falseWhenConditionNotSatisfied = true)
  public static record TrueOld(@Nullable Boolean value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @TrueWhen(propertyPath = "value", conditionPropertyPath = "cond", conditionValueBoolean = true,
      oppositeWhenConditionNotSatisfied = true)
  public static record TrueNew(@Nullable Boolean value, boolean cond) {}

  // @FalseWhen

  @ItemNameKeyClass("oppositeTest")
  @FalseWhen(propertyPath = "value", conditionPropertyPath = "cond", conditionValueBoolean = true,
      trueWhenConditionNotSatisfied = true)
  public static record FalseOld(@Nullable Boolean value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @FalseWhen(propertyPath = "value", conditionPropertyPath = "cond", conditionValueBoolean = true,
      oppositeWhenConditionNotSatisfied = true)
  public static record FalseNew(@Nullable Boolean value, boolean cond) {}

  // @NullWhen

  @ItemNameKeyClass("oppositeTest")
  @NullWhen(propertyPath = "value", conditionPropertyPath = "cond", conditionValueBoolean = true,
      notNullWhenConditionNotSatisfied = true)
  public static record NullOld(@Nullable String value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @NullWhen(propertyPath = "value", conditionPropertyPath = "cond", conditionValueBoolean = true,
      oppositeWhenConditionNotSatisfied = true)
  public static record NullNew(@Nullable String value, boolean cond) {}

  // @NotNullWhen

  @ItemNameKeyClass("oppositeTest")
  @NotNullWhen(propertyPath = "value", conditionPropertyPath = "cond",
      conditionValueBoolean = true, nullWhenConditionNotSatisfied = true)
  public static record NotNullOld(@Nullable String value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @NotNullWhen(propertyPath = "value", conditionPropertyPath = "cond",
      conditionValueBoolean = true, oppositeWhenConditionNotSatisfied = true)
  public static record NotNullNew(@Nullable String value, boolean cond) {}

  // @EmptyWhen

  @ItemNameKeyClass("oppositeTest")
  @EmptyWhen(propertyPath = "value", conditionPropertyPath = "cond", conditionValueBoolean = true,
      notEmptyWhenConditionNotSatisfied = true)
  public static record EmptyOld(@Nullable String value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @EmptyWhen(propertyPath = "value", conditionPropertyPath = "cond", conditionValueBoolean = true,
      oppositeWhenConditionNotSatisfied = true)
  public static record EmptyNew(@Nullable String value, boolean cond) {}

  // @NotEmptyWhen

  @ItemNameKeyClass("oppositeTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "cond",
      conditionValueBoolean = true, emptyWhenConditionNotSatisfied = true)
  public static record NotEmptyOld(@Nullable String value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "cond",
      conditionValueBoolean = true, oppositeWhenConditionNotSatisfied = true)
  public static record NotEmptyNew(@Nullable String value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "cond",
      conditionValueBoolean = true, emptyWhenConditionNotSatisfied = true,
      oppositeWhenConditionNotSatisfied = true)
  public static record NotEmptyBoth(@Nullable String value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "cond",
      conditionValueBoolean = true)
  public static record NotEmptyNone(@Nullable String value, boolean cond) {}

  // @PatternWhen

  @ItemNameKeyClass("oppositeTest")
  @PatternWhen(propertyPath = "value", regexp = "a", conditionPropertyPath = "cond",
      conditionValueBoolean = true, notPatternWhenConditionNotSatisfied = true)
  public static record PatternOld(@Nullable String value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @PatternWhen(propertyPath = "value", regexp = "a", conditionPropertyPath = "cond",
      conditionValueBoolean = true, oppositeWhenConditionNotSatisfied = true)
  public static record PatternNew(@Nullable String value, boolean cond) {}

  // @NotPatternWhen

  @ItemNameKeyClass("oppositeTest")
  @NotPatternWhen(propertyPath = "value", regexp = "a", conditionPropertyPath = "cond",
      conditionValueBoolean = true, patternWhenConditionNotSatisfied = true)
  public static record NotPatternOld(@Nullable String value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @NotPatternWhen(propertyPath = "value", regexp = "a", conditionPropertyPath = "cond",
      conditionValueBoolean = true, oppositeWhenConditionNotSatisfied = true)
  public static record NotPatternNew(@Nullable String value, boolean cond) {}

  // @StringWhen

  @ItemNameKeyClass("oppositeTest")
  @StringWhen(propertyPath = "value", string = "a", conditionPropertyPath = "cond",
      conditionValueBoolean = true, notStringWhenConditionNotSatisfied = true)
  public static record StringOld(@Nullable String value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @StringWhen(propertyPath = "value", string = "a", conditionPropertyPath = "cond",
      conditionValueBoolean = true, oppositeWhenConditionNotSatisfied = true)
  public static record StringNew(@Nullable String value, boolean cond) {}

  // @NotStringWhen

  @ItemNameKeyClass("oppositeTest")
  @NotStringWhen(propertyPath = "value", string = "a", conditionPropertyPath = "cond",
      conditionValueBoolean = true, stringWhenConditionNotSatisfied = true)
  public static record NotStringOld(@Nullable String value, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @NotStringWhen(propertyPath = "value", string = "a", conditionPropertyPath = "cond",
      conditionValueBoolean = true, oppositeWhenConditionNotSatisfied = true)
  public static record NotStringNew(@Nullable String value, boolean cond) {}

  // @ValueOfPropertyPathWhen

  @ItemNameKeyClass("oppositeTest")
  @ValueOfPropertyPathWhen(propertyPath = "value", valuePropertyPath = "other",
      conditionPropertyPath = "cond", conditionValueBoolean = true,
      notValueOfPropertyPathWhenConditionNotSatisfied = true)
  public static record ValueOfOld(@Nullable String value, @Nullable String other, boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @ValueOfPropertyPathWhen(propertyPath = "value", valuePropertyPath = "other",
      conditionPropertyPath = "cond", conditionValueBoolean = true,
      oppositeWhenConditionNotSatisfied = true)
  public static record ValueOfNew(@Nullable String value, @Nullable String other, boolean cond) {}

  // @NotValueOfPropertyPathWhen

  @ItemNameKeyClass("oppositeTest")
  @NotValueOfPropertyPathWhen(propertyPath = "value", valuePropertyPath = "other",
      conditionPropertyPath = "cond", conditionValueBoolean = true,
      valueOfPropertyPathWhenConditionNotSatisfied = true)
  public static record NotValueOfOld(@Nullable String value, @Nullable String other,
      boolean cond) {}

  @ItemNameKeyClass("oppositeTest")
  @NotValueOfPropertyPathWhen(propertyPath = "value", valuePropertyPath = "other",
      conditionPropertyPath = "cond", conditionValueBoolean = true,
      oppositeWhenConditionNotSatisfied = true)
  public static record NotValueOfNew(@Nullable String value, @Nullable String other,
      boolean cond) {}
}
