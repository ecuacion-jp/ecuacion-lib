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
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.ValidationUtil.MessageParameters;
import org.jspecify.annotations.NonNull;

/**
 * Holds a Jakarta Validations violation.
 * 
 * <p>Normally {@code ConstraintViolationException} should be used Since it's  
 *     a jakarta validation standard.<br>
 *     But sometimes you can use it 
 *     when you want to treat one {@code ConstraintViolation} as one exception
 *     (mainly in libraries or frameworks)<br><br>
 *     It's not recommended to use in apps from the view of understandability.</p>
 */
public class ValidationAppException extends SingleAppException {
  private static final long serialVersionUID = 1L;

  private ConstraintViolationBean<?> bean;
  private MessageParameters messageParameters = new MessageParameters();

  /**
   * Constructs a new instance with Jakarta Validation violation.
   *
   * @param violation violation result
   */
  public <T> ValidationAppException(ConstraintViolation<T> violation) {
    this(violation, new MessageParameters());
  }

  /**
   * Constructs a new instance with Jakarta Validation violation.
   *
   * @param violation violation result
   */
  public <T> ValidationAppException(ConstraintViolation<T> violation,
      MessageParameters messageParameters) {
    super(violation.getMessage());
    this.bean =
        violation instanceof ConstraintViolationBean ? (ConstraintViolationBean<T>) violation
            : ConstraintViolationBean
                .createConstraintViolationBean(ObjectsUtil.requireNonNull(violation));

    this.messageParameters = messageParameters;
  }

  @Override
  public @NonNull String[] getItemPropertyPaths() {
    return new @NonNull String[] {};
  }

  /**
  * Gets BeanValidationErrorInfoBean.
  *
  * @return BeanValidationErrorInfoBean
  */
  public ConstraintViolationBean<?> getConstraintViolationBean() {
    return bean;
  }

  public MessageParameters getMessageParameters() {
    return messageParameters;
  }
}
