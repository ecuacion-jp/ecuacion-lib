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
import jp.ecuacion.lib.validation.constraints.enums.ConditionValueState;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests class validators whose propertyPath has a {@code null} intermediate value
 *     (like {@code address} of {@code "address.zip"}).
 *
 * <p>The value of such a propertyPath is treated as {@code null}.</p>
 */
@DisplayName("propertyPath with a null intermediate value")
@SuppressWarnings({"SameNameButDifferent", "unused"})
public class NullIntermediateValueOfPropertyPathTest {

  private static final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  public static class Address {
    private @Nullable String zip;
    private @Nullable String type;
    private @Nullable Integer max;
  }

  @NotEmptyWhen(propertyPath = "address.zip", conditionPropertyPath = "flag",
      conditionValueState = ConditionValueState.NOT_EMPTY)
  public static class NotEmptyWhenBean {
    private String flag = "x";
    private @Nullable Address address = null;
  }

  @NullWhen(propertyPath = "address.zip", conditionPropertyPath = "flag",
      conditionValueState = ConditionValueState.NOT_EMPTY)
  public static class NullWhenBean {
    private String flag = "x";
    private @Nullable Address address = null;
  }

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "address.type",
      conditionValueState = ConditionValueState.NOT_EMPTY)
  public static class ConditionSideBean {
    private @Nullable String value = null;
    private @Nullable Address address = null;
  }

  @LessThanOrEqualTo(propertyPath = "value", baselinePropertyPath = "address.max")
  public static class ComparisonBean {
    private Integer value = 10;
    private @Nullable Address address = null;
  }

  @Test
  @DisplayName("@NotEmptyWhen: the value is treated as null, so it is invalid")
  void notEmptyWhen() {
    assertThat(validator.validate(new NotEmptyWhenBean())).hasSize(1);
  }

  @Test
  @DisplayName("@NullWhen: the value is treated as null, so it is valid")
  void nullWhen() {
    assertThat(validator.validate(new NullWhenBean())).isEmpty();
  }

  @Test
  @DisplayName("conditionPropertyPath: the condition value is treated as null")
  void conditionSide() {
    // The condition (address.type is not empty) is not satisfied, so value can be empty.
    assertThat(validator.validate(new ConditionSideBean())).isEmpty();
  }

  @Test
  @DisplayName("baselinePropertyPath: the baseline is treated as null, so comparison is skipped")
  void comparison() {
    assertThat(validator.validate(new ComparisonBean())).isEmpty();
  }
}
