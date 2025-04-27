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
package jp.ecuacion.lib.core.jakartavalidation.bean;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintViolation;
import java.util.HashMap;
import java.util.Map;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.jakartavalidation.util.internal.PrivateFieldReader;
import jp.ecuacion.lib.core.jakartavalidation.validator.internal.ConditionalValidator;

/** 
 * Stores {@code ConstraintViolation} info.
 * 
 * <p>The reason of the existence of the class is that the violations 
 *     which are not created by {@code Jakarata Validation} can also be treated 
 *     just like the one created by {@code Jakarata Validation}.</p>
 */
public class ConstraintViolationBean extends PrivateFieldReader {
  private ConstraintViolation<?> cv;
  private String message;
  private String propertyPath;
  private String validatorClass;
  private String rootClassName;
  private String messageTemplate;
  private String annotationDescriptionString;

  @Nonnull
  private Map<String, Object> paramMap;

  /**
   * Constructs a new instance with {@code ConstraintViolation}.
   * 
   * @param cv ConstraintViolation
   */
  public ConstraintViolationBean(ConstraintViolation<?> cv) {
    this.cv = cv;

    this.message = cv.getMessage();
    this.propertyPath = cv.getPropertyPath().toString();
    this.validatorClass = cv.getConstraintDescriptor().getAnnotation().annotationType().getName();
    this.rootClassName = cv.getRootBeanClass().getName();
    this.annotationDescriptionString = cv.getConstraintDescriptor().getAnnotation().toString();
    messageTemplate = cv.getMessageTemplate();
    // 値が{jakarta.validation.constraints.Pattern.message} のように{}に囲まれているので外す
    if (messageTemplate.startsWith("{")) {
      messageTemplate = messageTemplate.replace("{", "").replace("}", "");
    }

    this.paramMap = cv.getConstraintDescriptor().getAttributes() == null ? new HashMap<>()
        : new HashMap<>(cv.getConstraintDescriptor().getAttributes());

    // put additional params to paramMap
    putAdditionalParamsToParamMap(cv);
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
  public ConstraintViolationBean(String message, String propertyPath, String validatorClass,
      String rootClassName) {
    this.message = message;
    this.propertyPath = propertyPath;
    this.validatorClass = validatorClass;
    this.rootClassName = rootClassName;
    this.messageTemplate = validatorClass + ".message";
    this.paramMap = new HashMap<>();

    // これは@Pattern用なので実質使用はしないのだが、nullだとcompareの際におかしくなると嫌なので空白にしておく
    annotationDescriptionString = "";
  }

  private void putAdditionalParamsToParamMap(ConstraintViolation<?> cv) {

    // When localized messages are created, paramMap is an only parameter.
    // So sume value are needed to put into the map.
    paramMap.put("leafClassName", getLeafClassName());
    paramMap.put("invalidValue", getInvalidValue());
    paramMap.put("annotation", getAnnotation());

    // In the case of ConditionalXxx validator
    if (getAnnotation()
        .startsWith("jp.ecuacion.lib.core.jakartavalidation.validator.Conditional")) {
      String conditionValueKind;
      String valuesOfConditionFieldToValidate = null;
      if ((Boolean) paramMap.get(ConditionalValidator.CONDITION_VALUE_IS_EMPTY)) {
        conditionValueKind = ConditionalValidator.CONDITION_VALUE_IS_EMPTY;

      } else if ((Boolean) paramMap.get(ConditionalValidator.CONDITION_VALUE_IS_NOT_EMPTY)) {
        conditionValueKind = ConditionalValidator.CONDITION_VALUE_IS_NOT_EMPTY;

      } else if (!((String) paramMap.get(ConditionalValidator.FIELD_HOLDING_CONDITOION_VALUE))
          .equals(EclibCoreConstants.VALIDATOR_PARAMETER_NULL)) {
        conditionValueKind = ConditionalValidator.FIELD_HOLDING_CONDITOION_VALUE;
        valuesOfConditionFieldToValidate = (String) getFieldValue(
            (String) paramMap.get(ConditionalValidator.FIELD_HOLDING_CONDITOION_VALUE),
            getInstance(), conditionValueKind);

      } else {
        // conditionValue is used
        conditionValueKind = ConditionalValidator.CONDITION_VALUE;

        String[] strs = (String[]) paramMap.get(conditionValueKind);
        StringBuilder csv = new StringBuilder();
        boolean is1st = true;
        for (String str : strs) {
          if (is1st) {
            is1st = false;
          } else {
            csv.append(", ");
          }

          csv.append(str);
        }

        valuesOfConditionFieldToValidate = csv.toString();
      }

      paramMap.put(ConditionalValidator.CONDITION_VALUE_KIND, conditionValueKind);
      paramMap.put(ConditionalValidator.VALUE_OF_CONDITION_FIELD_TO_VALIDATE,
          valuesOfConditionFieldToValidate);
    }
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
    return cv.getConstraintDescriptor().getAnnotation().annotationType().getCanonicalName();
  }

  /**
   * Gets annotationAttributes.
   * 
   * @return annotationAttributes
   */
  public Map<String, Object> getAnnotationAttributes() {
    return cv.getConstraintDescriptor().getAttributes();
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
    return cv.getLeafBean().getClass().getName();
  }

  /**
   * Gets invalidValue.
   * 
   * @return invalidValue
   */
  public String getInvalidValue() {
    return (cv.getInvalidValue() == null) ? "null" : cv.getInvalidValue().toString();
  }

  /**
   * Gets messageId.
   * 
   * @return annotation
   */
  public @Nonnull String getMessageId() {
    return getAnnotation();
  }

  /**
   * Gets instance.
   * 
   * @return instance
   */
  public Object getInstance() {
    return cv.getLeafBean();
  }

  @Nonnull
  public Map<String, Object> getParamMap() {
    return paramMap;
  }
}
