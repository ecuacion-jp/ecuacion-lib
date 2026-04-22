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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import jp.ecuacion.lib.core.util.ValidationUtil.MessageParameters;
import org.jspecify.annotations.Nullable;

/**
 * Provides {@code ConstraintViolationException} with meta data.
 */
public class ConstraintViolationExceptionWithParameters extends ConstraintViolationException {

  private static final long serialVersionUID = 1L;

  private MessageParameters messageParameters;

  public MessageParameters getMessageParameters() {
    return messageParameters;
  }

  /**
   * Construct a new instance.
   * 
   * @param constraintViolations can be {@code null} and its elements also can be {@code null}
   *     because the variable is stored in the parent class, the standard jakarta EE class.
   */
  public ConstraintViolationExceptionWithParameters(
      @Nullable Set<? extends ConstraintViolation<?>> constraintViolations,
      MessageParameters messageParameters) {
    super(constraintViolations);

    this.messageParameters = messageParameters;
  }
}
