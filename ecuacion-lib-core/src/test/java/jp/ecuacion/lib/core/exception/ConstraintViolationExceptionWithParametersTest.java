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
package jp.ecuacion.lib.core.exception;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Set;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.lib.core.violation.Violations.MessageParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link ConstraintViolationExceptionWithParameters}. */
@DisplayName("ConstraintViolationExceptionWithParameters")
public class ConstraintViolationExceptionWithParametersTest {

  @Test
  @DisplayName("constructor stores MessageParameters; getMessageParameters returns it")
  void constructorAndGetter() {
    MessageParameters params = Violations.newMessageParameters();
    ConstraintViolationExceptionWithParameters ex =
        new ConstraintViolationExceptionWithParameters(Set.of(), params);
    assertThat(ex.getMessageParameters()).isSameAs(params);
  }
}
