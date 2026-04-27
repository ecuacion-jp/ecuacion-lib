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
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@code @NotEmptyWhen} validation logic. */
@DisplayName("@NotEmptyWhen")
public class NotEmptyWhenTest {

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtil.addResourceBundlePostfix("lib-validation-test");
  }

  /**
   * Tests validation logic for {@code conditionValue = NULL} and {@code NOT_NULL}.
   *
   * <p>Key difference from {@code EMPTY}/{@code NOT_EMPTY}: blank strings do NOT satisfy
   *     the {@code NULL} condition.</p>
   */
  @Test
  @DisplayName("NULL / NOT_NULL: blank string does not satisfy the NULL condition")
  public void validationOnConditionIsNullNotNull() {
    // NULL: condition satisfied only when condValue is strictly null (not blank)
    // null condValue -> condition satisfied -> value=null -> fail
    assertThat(validator.validate(new NullCondBean(null, null))).hasSize(1);
    // null condValue -> condition satisfied -> value="a" -> pass
    assertThat(validator.validate(new NullCondBean("a", null))).isEmpty();
    // blank condValue -> condition NOT satisfied (blank != null) -> pass
    assertThat(validator.validate(new NullCondBean(null, ""))).isEmpty();
    // non-null condValue -> condition NOT satisfied -> pass
    assertThat(validator.validate(new NullCondBean(null, "a"))).isEmpty();

    // NOT_NULL: condition satisfied when condValue is not null (including blank strings)
    // non-null condValue -> condition satisfied -> value=null -> fail
    assertThat(validator.validate(new NotNullCondBean(null, "a"))).hasSize(1);
    // blank condValue -> condition satisfied (blank is not null) -> value=null -> fail
    assertThat(validator.validate(new NotNullCondBean(null, ""))).hasSize(1);
    // null condValue -> condition NOT satisfied -> pass
    assertThat(validator.validate(new NotNullCondBean(null, null))).isEmpty();
    // non-null condValue -> condition satisfied -> value="a" -> pass
    assertThat(validator.validate(new NotNullCondBean("a", "x"))).isEmpty();
  }

  @Test
  @DisplayName("EMPTY condition: validates String and non-String fields")
  public void validationOnConditionIsTrue() {
    // String
    // null
    assertThat(validator.validate(new StrBean(null, null))).hasSize(1);
    // empty string
    assertThat(validator.validate(new StrBean("", null))).hasSize(1);
    // non-empty value
    assertThat(validator.validate(new StrBean("a", null))).isEmpty();

    // non-String (Integer)
    // null value
    assertThat(validator.validate(new IntBean(null, null))).hasSize(1);
    // non-null value
    assertThat(validator.validate(new IntBean(0, null))).isEmpty();
  }

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.NULL)
  public static record NullCondBean(@Nullable String value, @Nullable String condValue) {}

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.NOT_NULL)
  public static record NotNullCondBean(@Nullable String value, @Nullable String condValue) {}

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY)
  public static record StrBean(@Nullable String value, @Nullable String condValue) {}

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY)
  public static record IntBean(@Nullable Integer value, @Nullable String condValue) {}
}
