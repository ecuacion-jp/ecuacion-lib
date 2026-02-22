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
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.util.ObjectsUtil;

/**
 * Holds a Jakarta Validations violation.
 * 
 * <p>Normally {@code ConstraintViolationBeanException} should be used Since it's alike 
 *     to jakarta validation standard {@code ConstraintViolationException}.<br>
 *     But sometimes you can use it 
 *     when you want to treat {@code ConstraintViolationBean} as one exception
 *     (mainly in libraries or frameworks).</p>
 */
public class ValidationAppException extends SingleAppException {
  private static final long serialVersionUID = 1L;

  private ConstraintViolationBean<?> bean;

  /**
   * Constructs a new instance with Jakarta Validation violation.
   *
   * @param violation violation result
   */
  public <T> ValidationAppException(@RequireNonnull ConstraintViolation<T> violation) {
    super(violation.getMessage());
    this.bean = new ConstraintViolationBean<T>(ObjectsUtil.requireNonNull(violation));
  }

  /**
   * Constructs a new instance with {@code BeanValidationErrorInfoBean}.
   * 
   * @param bean ConstraintViolationBean
   */
  public <T> ValidationAppException(@RequireNonnull ConstraintViolationBean<T> bean) {
    this.bean = ObjectsUtil.requireNonNull(bean);
  }

  /**
  * Gets BeanValidationErrorInfoBean.
  *
  * @return BeanValidationErrorInfoBean
  */
  public ConstraintViolationBean<?> getConstraintViolationBean() {
    return bean;
  }

  @Override
  public String[] getItemPropertyPaths() {
    return null;
  }
}
