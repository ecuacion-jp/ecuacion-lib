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
package jp.ecuacion.lib.core.beanvalidation.bean;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintViolation;
import java.util.Map;

/** 
 * Stores {@code ConstraintViolation} info.
 */
public class BeanValidationErrorInfoBean {
  private String message;
  private String propertyPath;
  private String validatorClass;
  private String annotationDescriptionString;

  private String annotation;
  private Map<String, Object> annotationAttributes;
  private String messageTemplate;
  private String rootClassName;
  private String leafClassName;
  private String invalidValue;
  private Object instance;
  
  private Map<String, Object> paramMap;

  /**
   * Constructs a new instance with {@code ConstraintViolation}.
   * 
   * @param cv ConstraintViolation
   */
  public BeanValidationErrorInfoBean(ConstraintViolation<?> cv) {
    this.message = cv.getMessage();
    this.propertyPath = cv.getPropertyPath().toString();
    this.validatorClass = cv.getConstraintDescriptor().getAnnotation().annotationType().getName();
    this.annotationDescriptionString = cv.getConstraintDescriptor().getAnnotation().toString();

    this.annotation =
        cv.getConstraintDescriptor().getAnnotation().annotationType().getCanonicalName();
    this.annotationAttributes = cv.getConstraintDescriptor().getAttributes();
    messageTemplate = cv.getMessageTemplate();
    // 値が{jakarta.validation.constraints.Pattern.message} のように{}に囲まれているので外す
    if (messageTemplate.startsWith("{")) {
      messageTemplate = messageTemplate.replace("{", "").replace("}", "");
    }
    this.rootClassName = cv.getRootBeanClass().getName();
    this.leafClassName = cv.getLeafBean().getClass().getName();
    this.invalidValue = (cv.getInvalidValue() == null) ? "null" : cv.getInvalidValue().toString();
    this.instance = cv.getLeafBean();
    
    this.paramMap = cv.getConstraintDescriptor().getAttributes();
  }

  /**
   * Constructs a new instance 
   * with {@code message}, {@code propertyPath} and {@code validatorClass}.
   * 
   * <p>This is used for {@code NotEmpty} validation logic.</p>
   * 
   * @param message message
   * @param propertyPath propertyPath
   * @param validatorClass validatorClass
   */
  public BeanValidationErrorInfoBean(String message, String propertyPath, String validatorClass,
      String rootClassName) {
    this.message = message;
    this.propertyPath = propertyPath;
    this.validatorClass = validatorClass;
    this.rootClassName = rootClassName;

    // これは@Pattern用なので実質使用はしないのだが、nullだとcompareの際におかしくなると嫌なので空白にしておく
    annotationDescriptionString = "";
  }

  /**
   * Gets message.
   * 
   * @return message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Gets propertyPath.
   * 
   * @return propertyPath
   */
  public String getPropertyPath() {
    return propertyPath;
  }

  /**
   * Gets validatorClass.
   * 
   * @return validatorClass
   */
  public String getValidatorClass() {
    return validatorClass;
  }

  /**
   * Gets annotationDescriptionString.
   * 
   * @return annotationDescriptionString
   */
  public String getAnnotationDescriptionString() {
    return annotationDescriptionString;
  }

  /**
   * Gets annotation.
   * 
   * @return annotation
   */
  public String getAnnotation() {
    return annotation;
  }

  /**
   * Gets annotationAttributes.
   * 
   * @return annotationAttributes
   */
  public Map<String, Object> getAnnotationAttributes() {
    return annotationAttributes;
  }

  /**
   * Gets messageTemplate.
   * 
   * @return messageTemplate
   */
  public String getMessageTemplate() {
    return messageTemplate;
  }

  /**
   * Gets rootClassName.
   * 
   * @return rootClassName
   */
  public String getRootClassName() {
    return rootClassName;
  }

  /**
   * Gets leafClassName.
   * 
   * @return leafClassName
   */
  public String getLeafClassName() {
    return leafClassName;
  }

  /**
   * Gets invalidValue.
   * 
   * @return invalidValue
   */
  public String getInvalidValue() {
    return invalidValue;
  }

  /**
   * Gets messageId.
   * 
   * @return annotation
   */
  public @Nonnull String getMessageId() {
    return annotation;
  }

  /**
   * Gets instance.
   * 
   * @return instance
   */
  public Object getInstance() {
    return instance;
  }
  
  public Map<String, Object> getParamMap() {
    return paramMap;
  }
}
