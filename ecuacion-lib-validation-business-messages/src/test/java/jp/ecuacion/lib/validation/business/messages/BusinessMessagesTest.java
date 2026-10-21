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
package jp.ecuacion.lib.validation.business.messages;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Locale;
import java.util.stream.Stream;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.validation.constraints.Condition;
import jp.ecuacion.lib.validation.constraints.NotEmptyWhen;
import jp.ecuacion.lib.validation.constraints.NotNullWhen;
import jp.ecuacion.lib.validation.constraints.NullWhen;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValueState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests that the business-friendly messages, including the message parts of *When validators,
 *     are used when this module is added.
 */
@DisplayName("business-friendly messages")
public class BusinessMessagesTest {

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @ParameterizedTest(name = "{1}")
  @MethodSource("beanAndExpectedMessages")
  @DisplayName("*When validators produce business-friendly messages")
  void whenValidatorMessageTest(Object bean, String expectedEn, String expectedJa) {
    assertThat(ExceptionUtil.getMessageList(validator.validate(bean), Locale.ENGLISH).get(0))
        .isEqualTo(expectedEn);
    assertThat(ExceptionUtil.getMessageList(validator.validate(bean), Locale.JAPANESE).get(0))
        .isEqualTo(expectedJa);
  }

  @SuppressWarnings("null")
  static Stream<Arguments> beanAndExpectedMessages() {
    return Stream.of(
        Arguments.of(new NotNullWhenBean(),
            "must be provided when 'notNullWhenBean.cond' is on, and left empty otherwise",
            "「notNullWhenBean.cond」がオンの場合は入力、それ以外の場合は未入力にしてください"),
        Arguments.of(new NullWhenBean(),
            "must be left empty when 'nullWhenBean.cond' is on, and provided otherwise",
            "「nullWhenBean.cond」がオンの場合は未入力に、それ以外の場合は入力してください"),
        Arguments.of(new NotEmptyWhenConditionNullBean(),
            "must be provided when 'notEmptyWhenConditionNullBean.cond' is not provided",
            "「notEmptyWhenConditionNullBean.cond」が未入力の場合は入力してください"),
        Arguments.of(new NotEmptyWhenMultipleConditionsBean(),
            "must be provided when 'notEmptyWhenMultipleConditionsBean.cond1' is blank"
            + " and 'notEmptyWhenMultipleConditionsBean.cond2' is on",
            "「notEmptyWhenMultipleConditionsBean.cond1」が空欄で、かつ"
            + "「notEmptyWhenMultipleConditionsBean.cond2」がオンの場合は入力してください"));
  }

  @NotNullWhen(propertyPath = "field", conditionPropertyPath = "cond",
      conditionValueBoolean = true, nullWhenConditionNotSatisfied = true)
  @SuppressWarnings("unused")
  private static class NotNullWhenBean {
    private String field = null;
    private boolean cond = true;
  }

  @NullWhen(propertyPath = "field", conditionPropertyPath = "cond",
      conditionValueBoolean = true, notNullWhenConditionNotSatisfied = true)
  @SuppressWarnings("unused")
  private static class NullWhenBean {
    private String field = "a";
    private boolean cond = true;
  }

  @NotEmptyWhen(propertyPath = "field", conditionPropertyPath = "cond",
      conditionValueState = ConditionValueState.NULL)
  @SuppressWarnings("unused")
  private static class NotEmptyWhenConditionNullBean {
    private String field = "";
    private String cond = null;
  }

  @NotEmptyWhen(propertyPath = "field", conditions = {
      @Condition(propertyPath = "cond1", valueState = ConditionValueState.EMPTY),
      @Condition(propertyPath = "cond2", valueBoolean = true)})
  @SuppressWarnings("unused")
  private static class NotEmptyWhenMultipleConditionsBean {
    private String field = "";
    private String cond1 = "";
    private boolean cond2 = true;
  }
}
