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
package jp.ecuacion.lib.core.jakartavalidation.constraints;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests ClassValidator.
 *
 * <p>Tests for {0} messages is executed in {@code ExceptionUtil}.</p>
 */
@DisplayName("ClassValidator")
public class ClassValidatorTest {

  @BeforeAll
  public static void beforeAll() {}

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  /**
   * Tests irregulars on propertyPath.
   */
  @Test
  @SuppressWarnings("unused")
  public void irregular() {

    // Cases where initialize() itself throws are tested by calling initialize(String, String[])
    // directly, without going through validator.validate(). Routing through the BV framework
    // when initialize() throws can leave Hibernate Validator's internal constraint-metadata
    // cache in an inconsistent state and corrupt unrelated tests.

    // propertyPath is empty string
    assertThatThrownBy(
        () -> new ClassAlwaysFalseValidator().initialize("msg", new String[]{""}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("propertyPath must not contain empty strings. Specify a valid field name.");

    // propertyPath contains an empty string element
    assertThatThrownBy(
        () -> new ClassAlwaysFalseValidator().initialize("msg", new String[]{"propertyPath", ""}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("propertyPath must not contain empty strings. Specify a valid field name.");

    // propertyPath length zero
    assertThatThrownBy(
        () -> new ClassAlwaysFalseValidator().initialize("msg", new String[]{}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Length of propertyPath is zero.");

    // propertyPath not found in the bean — exception comes from isValid(), so validator.validate()
    // is used here. initialize() succeeds, so no metadata-cache corruption occurs.
    try {
      var unused = validator.validate(new PropertyPathNotFound(null)).size();
      Assertions.fail();

    } catch (Exception ex) {
      assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(Objects.requireNonNull(ex.getCause()).getCause())
          .isInstanceOf(NoSuchFieldException.class);
    }
  }

  @ClassAlwaysFalse(propertyPath = {"a"})
  public static record PropertyPathNotFound(@Nullable String propertyPath) {

  }
}
