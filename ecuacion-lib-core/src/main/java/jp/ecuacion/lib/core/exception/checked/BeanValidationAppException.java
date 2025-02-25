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

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintViolation;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.beanvalidation.bean.BeanValidationErrorInfoBean;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.PropertyFileUtil.Arg;

/**
 * Holds a bean validations violation.
 */
public class BeanValidationAppException extends SingleAppException {
  private static final long serialVersionUID = 1L;

  private BeanValidationErrorInfoBean bean;
  
  private boolean isMessageWithItemName;

  private Arg messagePrefix;
  
  private Arg messagePostfix;

  /**
   * Constructs a new instance with bean validation violation.
   *
   * @param violation violation result
   */
  public BeanValidationAppException(@RequireNonnull ConstraintViolation<?> violation) {
    super();
    this.bean = new BeanValidationErrorInfoBean(ObjectsUtil.paramRequireNonNull(violation));
  }

  /**
   * Constructs a new instance with {@code BeanValidationErrorInfoBean}.
   * 
   * <p>This makes possible to treat exceptions
   *     wchich are not created from {@code ConstraintViolation}
   *     as {@code BeanValidationAppException}.</p>
   * 
   * @param bean BeanValidationErrorInfoBean
   */
  public BeanValidationAppException(@RequireNonnull BeanValidationErrorInfoBean bean) {
    this.bean = ObjectsUtil.paramRequireNonNull(bean);
  }

  /**
  * Gets BeanValidationErrorInfoBean.
  *
  * @return BeanValidationErrorInfoBean
  */
  public BeanValidationErrorInfoBean getBeanValidationErrorInfoBean() {
    return bean;
  }

  /** 
   * Outputs a string for logs. 
   * 
  * @return String
   */
  @Override
  public @Nonnull String toString() {
    return "message:" + bean.getMessage() + "\n" + "annotation:" + bean.getAnnotation() + "\n"
        + "rootClassName:" + bean.getRootClassName() + "\n" + "leafClassName:"
        + bean.getLeafClassName() + "\n" + "propertyPath:" + bean.getPropertyPath() + "\n"
        + "invalidValue:" + bean.getInvalidValue();
  }

  /**
   * Obtains {@code isMessageWithItemName}.
   * 
   * @return boolean
   */
  public boolean isMessageWithItemName() {
    return isMessageWithItemName;
  }

  /**
   * Sets {@code isMessageWithItemName} and returns this for method chain.
   * 
   * @return BeanValidationErrorInfoBean;
   */
  public BeanValidationAppException setMessageWithItemName(boolean isMessageWithItemName) {
    this.isMessageWithItemName = isMessageWithItemName;
    return this;
  }
  
  public Arg getMessagePrefix() {
    return messagePrefix;
  }

  /**
   * Sets {@code messagePrefix} and returns this for method chain.
   * 
   * @return BeanValidationErrorInfoBean;
   */
  public BeanValidationAppException setMessagePrefix(Arg messagePrefix) {
    this.messagePrefix = messagePrefix;
    return this;
  }

  /**
   * Sets {@code messagePostfix} and returns this for method chain.
   * 
   * @return BeanValidationErrorInfoBean;
   */

  public Arg getMessagePostfix() {
    return messagePostfix;
  }

  public void setMessagePostfix(Arg messagePostfix) {
    this.messagePostfix = messagePostfix;
  }
}
