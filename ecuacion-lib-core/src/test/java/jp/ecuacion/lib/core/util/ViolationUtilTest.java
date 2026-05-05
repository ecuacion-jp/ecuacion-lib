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
package jp.ecuacion.lib.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.constraints.NotNull;
import jp.ecuacion.lib.core.violation.Violations;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link ViolationUtil}. */
@DisplayName("ViolationUtil")
public class ViolationUtilTest {

  @Test
  @DisplayName("validate(T): returns empty violations for valid object")
  void validateValid() {
    Violations violations = ViolationUtil.validate(new SimpleBean("ok"));
    assertThat(violations.getConstraintViolations()).isEmpty();
  }

  @Test
  @DisplayName("validate(T): returns violations for invalid object")
  void validateInvalid() {
    Violations violations = ViolationUtil.validate(new SimpleBean(null));
    assertThat(violations.getConstraintViolations()).hasSize(1);
  }

  @Test
  @DisplayName("validate(T, Class<?>...): runs validation with specified group")
  void validateWithGroups() {
    Violations violations = ViolationUtil.validate(new GroupBean(null), GroupA.class);
    assertThat(violations.getConstraintViolations()).hasSize(1);
  }

  @SuppressWarnings("MultipleNullnessAnnotations")
  private static record SimpleBean(@NotNull @Nullable String value) {
  }

  @SuppressWarnings("MultipleNullnessAnnotations")
  private static record GroupBean(@NotNull(groups = GroupA.class) @Nullable String value) {
  }

  private interface GroupA {
  }
}
