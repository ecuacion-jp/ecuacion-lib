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
import java.util.HashMap;
import java.util.Map;
import jp.ecuacion.lib.core.util.ObjectsUtil;

/**
 * Holds a bean validations violation.
 */
public class BeanValidationAppException extends SingleAppException {
  private static final long serialVersionUID = 1L;

  private String annotation;
  private Map<String, Object> annotationAttributes;
  private String message;
  private String messageTemplate;
  private String rootClassName;
  private String leafClassName;
  private String propertyPath;
  private String invalidValue;
  private Object instance;

  /**
   * Constructs a new instance with bean validation violation.
   *
   * @param <T> The class applying validation.
   * @param violation violation result
   */
  public <T> BeanValidationAppException(@Nonnull ConstraintViolation<T> violation) {
    super();

    ObjectsUtil.paramRequireNonNull(violation);
    
    // annotationをそのままmeeesageIdとして使用する
    annotation =
        violation.getConstraintDescriptor().getAnnotation().annotationType().getCanonicalName();
    annotationAttributes = violation.getConstraintDescriptor().getAttributes();
    message = violation.getMessage();
    messageTemplate = violation.getMessageTemplate();
    // 値が{jakarta.validation.constraints.Pattern.message} のように{}に囲まれているので外す
    if (messageTemplate.startsWith("{")) {
      messageTemplate = messageTemplate.replace("{", "").replace("}", "");
    }

    rootClassName = violation.getRootBeanClass().getName();
    leafClassName = violation.getLeafBean().getClass().getName();
    propertyPath = violation.getPropertyPath().toString();
    invalidValue =
        (violation.getInvalidValue() == null) ? "null" : violation.getInvalidValue().toString();
    instance = violation.getLeafBean();
  }

  /**
   * Gets annotationAttributes.
   * 
   * @return annotationAttributes
   */
  public Map<String, Object> getAnnotationAttributes() {
    return new HashMap<>(annotationAttributes);
  }

  /**
   * Gets annotation.
   * 
   * @return annotation
   */
  public @Nonnull String getAnnotation() {
    return annotation;
  }

  /**
   * Gets message.
   * 
   * @return message
   */
  public @Nonnull String getMessage() {
    return message;
  }

  /**
   * Gets messageTemplate.
   * 
   * @return messageTemplate
   */
  public @Nonnull String getMessageTemplate() {
    return messageTemplate;
  }

  /**
   * Gets rootClassName.
   * 
   * @return rootClassName
   */
  public @Nonnull String getRootClassName() {
    return rootClassName;
  }

  /**
   * Gets leafClassName.
   * 
   * @return leafClassName
   */
  public @Nonnull String getLeafClassName() {
    return leafClassName;
  }

  /**
   * Gets propertyPath.
   * 
   * @return propertyPath
   */
  public @Nonnull String getPropertyPath() {
    return propertyPath;
  }

  /**
   * Gets invalidValue.
   * 
   * @return invalidValue
   */
  public @Nonnull String getInvalidValue() {
    return invalidValue;
  }

  /**
   * Gets instance.
   * 
   * @return instance
   */
  public @Nonnull Object getInstance() {
    return instance;
  }

  /**
   * Gets annotation.
   * 
   * @return annotation
   */
  public @Nonnull String getMessageId() {
    return annotation;
  }

  /** Outputs a string for logs. */
  @Override
  public @Nonnull String toString() {
    return "message:" + message + "\n" + "annotation:" + annotation + "\n" + "rootClassName:"
        + rootClassName + "\n" + "leafClassName:" + leafClassName + "\n" + "propertyPath:"
        + propertyPath + "\n" + "invalidValue:" + invalidValue;
  }
}
