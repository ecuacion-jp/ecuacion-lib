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
package jp.ecuacion.lib.core.violation;

import static org.assertj.core.api.Assertions.assertThat;
import jp.ecuacion.lib.core.exception.ViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link Violations}. */
@DisplayName("Violations")
public class ViolationsTest {

  @Test
  @DisplayName("empty violations does not throw")
  public void emptyViolationsDoesNotThrow() {
    new Violations().throwIfAny();
  }

  @Test
  @DisplayName("throwIfAny throws ViolationException when a violation is present")
  public void throwIfAny() {
    Violations violations = new Violations().add(new BusinessViolation("TEST_KEY"));
    Assertions.assertThrows(ViolationException.class, violations::throwIfAny);
  }

  @Test
  @DisplayName("added violations are stored in order")
  public void violationsStoredCorrectly() {
    Violations violations = new Violations()
        .add(new BusinessViolation("KEY_1"))
        .add(new BusinessViolation("KEY_2"))
        .add(new BusinessViolation("KEY_3"));

    assertThat(violations.getBusinessViolations()).hasSize(3);
    assertThat(violations.getBusinessViolations().get(0).getMessageId()).isEqualTo("KEY_1");
    assertThat(violations.getBusinessViolations().get(1).getMessageId()).isEqualTo("KEY_2");
    assertThat(violations.getBusinessViolations().get(2).getMessageId()).isEqualTo("KEY_3");
  }

  @Test
  @DisplayName("thrown ViolationException holds the violations")
  public void thrownExceptionHoldsViolations() {
    Violations violations = new Violations().add(new BusinessViolation("TEST_KEY"));

    @SuppressWarnings("null")
    ViolationException ex =
        Assertions.assertThrows(ViolationException.class, violations::throwIfAny);
    assertThat(ex.getViolations()).isNotNull();
    assertThat(ex.getViolations().getBusinessViolations()).hasSize(1);
  }
}
