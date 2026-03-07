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
package jp.ecuacion.lib.core.exception.checked;

import jakarta.validation.ConstraintViolation;
import java.util.Set;

/**
 * Is used as ConstraintViolationException but unchecked one.
 */
public class ConstraintViolationRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private Set<ConstraintViolation<?>> constraintViolations;

  /**
   * Constructs a new instance.
   */
  public ConstraintViolationRuntimeException(Set<ConstraintViolation<?>> constraintViolations) {
    this.constraintViolations = constraintViolations;
  }

  public Set<ConstraintViolation<?>> getConstraintViolations() {
    return constraintViolations;
  }
}
