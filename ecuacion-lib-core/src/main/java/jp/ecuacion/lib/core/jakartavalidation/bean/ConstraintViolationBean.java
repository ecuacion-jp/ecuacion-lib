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

import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern.stringValueOfConditionPropertyPathIsEqualTo;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern.stringValueOfConditionPropertyPathIsNotEqualTo;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern.valueOfConditionPropertyPathIsEqualToValueOf;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern.valueOfConditionPropertyPathIsNotEqualToValueOf;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintViolation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.jakartavalidation.validator.ItemKindIdClass;
import jp.ecuacion.lib.core.jakartavalidation.validator.PlacedAtClass;
import jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern;
import jp.ecuacion.lib.core.jakartavalidation.validator.internal.ConditionalValidator;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.core.util.internal.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;

/** 
 * Stores {@code ConstraintViolation} info.
 * 
 * <p>The reason of the existence of the class is that the violations 
 *     which are not created by {@code Jakarata Validation} can also be treated 
 *     just like the one created by {@code Jakarata Validation}.</p>
 */
public class ConstraintViolationBean extends ReflectionUtil {
  private ConstraintViolation<?> cv;
  private String message;
  private String propertyPath;
  private String[] fieldPropertyPaths;

  private String validatorClass;
  private String rootClassName;
  private String messageTemplate;
  private String annotationDescriptionString;

  private String[] itemKindIds;

  @Nonnull
  private Map<String, Object> paramMap;

  private static final String CONDITIONAL_VALIDATOR_PREFIX =
      "jp.ecuacion.lib.core.jakartavalidation.validator.Conditional";

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
   * @param validatorClass validatorClass
   */
  public ConstraintViolationBean(String message, String validatorClass, String rootClassName,
      String itemKindId) {
    this.message = message;
    this.validatorClass = validatorClass;
    this.rootClassName = rootClassName;
    this.messageTemplate = validatorClass + ".message";

    this.itemKindIds = new String[] {itemKindId};
    // in this case itemKindId == propertyPath holds true.
    this.propertyPath = itemKindId;
    this.fieldPropertyPaths = new String[] {itemKindId};

    this.paramMap = new HashMap<>();

    // これは@Pattern用なので実質使用はしないのだが、nullだとcompareの際におかしくなると嫌なので空白にしておく
    annotationDescriptionString = "";
  }

  private Optional<String> getItemKindIdClassFromAnnotation(boolean isClassValidator,
      Object leafBean, String propertyPath) {

    Object modifiedLeafBean = leafBean;
    try {
      // search for @ItemKindIdClass at field
      if (!isClassValidator) {
        String fieldName = propertyPath.split("\\.")[propertyPath.split("\\.").length - 1];
        Field field = getField(fieldName, modifiedLeafBean).getLeft();
        ItemKindIdClass an = field.getAnnotation(ItemKindIdClass.class);
        String value = an == null ? null : an.value();

        if (value != null) {
          return Optional.of(value);
        }

      } else {
        // If propertyPath is not empty, Obtain the real leaf bean
        String tmpPp = propertyPath;
        while (true) {
          if (StringUtils.isEmpty(tmpPp)) {
            break;
          }

          String fieldName = tmpPp.contains(".") ? tmpPp.substring(0, tmpPp.indexOf(".")) : tmpPp;
          tmpPp = tmpPp.contains(".") ? tmpPp.substring(tmpPp.indexOf(".") + 1) : "";

          modifiedLeafBean = getFieldValue(fieldName, modifiedLeafBean);
        }
      }

      Optional<ItemKindIdClass> an =
          searchAnnotationPlacedAtClass(modifiedLeafBean, ItemKindIdClass.class);
      return Optional.ofNullable(an.isPresent() ? an.get().value() : null);

    } catch (Exception ex) {
      throw new EclibRuntimeException(ex);
    }
  }

  private void putAdditionalParamsToParamMap(ConstraintViolation<?> cv) {

    // When localized messages are created, paramMap is an only parameter.
    // So some values should be put into the map.
    paramMap.put("leafClassName", getLeafClassName());
    paramMap.put("invalidValue", getInvalidValue());
    paramMap.put("annotation", getAnnotation());



    // checks if the validator is for class or field.
    boolean isClassValidator = cv.getConstraintDescriptor().getAnnotation().annotationType()
        .getAnnotation(PlacedAtClass.class) != null;

    // itemKindIdClass
    String defaultItemKindIdClass = null;

    // itemKindIdField
    String[] propertyPaths = null;
    List<String> itemKindIdList = new ArrayList<>();
    if (isClassValidator) {
      // Reaching here means the annotation is added to class, not field.
      propertyPaths = (String[]) paramMap.get("propertyPath");

      for (String fieldPropertyPath : propertyPaths) {
        Optional<String> itemKindIdClassFromAnnotation =
            getItemKindIdClassFromAnnotation(isClassValidator, cv.getLeafBean(),
                fieldPropertyPath.contains(".")
                    ? fieldPropertyPath.substring(0, fieldPropertyPath.lastIndexOf("."))
                    : "");

        defaultItemKindIdClass = StringUtils.isEmpty(propertyPath)
            ? rootClassName.split("\\.")[rootClassName.split("\\.").length - 1]
            : propertyPath.split("\\.")[propertyPath.split("\\.").length - 1];

        String itemKindId =
            getFinalItemKindIdClass(itemKindIdClassFromAnnotation, defaultItemKindIdClass) + "."
                + (fieldPropertyPath.contains(".")
                    ? (fieldPropertyPath.substring(fieldPropertyPath.lastIndexOf(".") + 1))
                    : fieldPropertyPath);
        itemKindIdList.add(itemKindId);
      }

      // conditionFieldItemKindId
      String conditionPropertyPath =
          (String) paramMap.get(ConditionalValidator.CONDITION_PROPERTY_PATH);
      Optional<String> itemKindIdClassFromAnnotation = getItemKindIdClassFromAnnotation(
          isClassValidator, cv.getLeafBean(), conditionPropertyPath);
      String newValue =
          getFinalItemKindIdClass(itemKindIdClassFromAnnotation, defaultItemKindIdClass) + "."
              + conditionPropertyPath;
      paramMap.put(ConditionalValidator.CONDITION_PROPERTY_PATH_ITEM_KIND_ID, newValue);

      // fieldPropertyPaths
      List<String> list =
          Arrays.asList(propertyPaths).stream().map(field -> propertyPath + "." + field).toList();
      fieldPropertyPaths = list.toArray(new String[list.size()]);

    } else {
      // Reaching here means the annotation is added to field.
      // In that case propertyPath cannot be empty and the last part is always itemKindIdField.

      // itemKindId
      Optional<String> itemKindIdClassFromAnnotation = getItemKindIdClassFromAnnotation(
          isClassValidator, cv.getLeafBean(), cv.getPropertyPath().toString());
      defaultItemKindIdClass = propertyPath.split("\\.").length > 1
          ? propertyPath.split("\\.")[propertyPath.split("\\.").length - 2]
          : rootClassName.split("\\.")[rootClassName.split("\\.").length - 1];
      String field = propertyPath.split("\\.")[propertyPath.split("\\.").length - 1];
      String itemKindId =
          getFinalItemKindIdClass(itemKindIdClassFromAnnotation, defaultItemKindIdClass) + "."
              + field;
      itemKindIdList.add(itemKindId);

      // fieldPropertyPaths
      fieldPropertyPaths = new String[] {propertyPath};
    }

    // itemKindIds
    itemKindIds = itemKindIdList.toArray(new String[itemKindIdList.size()]);
    paramMap.put("itemKindIds", itemKindIds);

    // In the case of ConditionalXxx validator
    if (getAnnotation().startsWith(CONDITIONAL_VALIDATOR_PREFIX)) {

      ConditionPattern conditionPtn =
          (ConditionPattern) paramMap.get(ConditionalValidator.CONDITION_PATTERN);

      paramMap.put(ConditionalValidator.CONDITION_PATTERN, conditionPtn);

      String valuesOfConditionPropertyPathToValidate = null;
      if (conditionPtn == valueOfConditionPropertyPathIsEqualToValueOf
          || conditionPtn == valueOfConditionPropertyPathIsNotEqualToValueOf) {

        Object obj =
            getFieldValue((String) paramMap.get(ConditionalValidator.CONDITION_VALUE_PROPERTY_PATH),
                getInstance());
        if (obj instanceof Object[]) {
          List<String> strList = Arrays.asList(obj).stream().map(o -> o.toString()).toList();
          valuesOfConditionPropertyPathToValidate =
              StringUtil.getCsvWithSpace((String[]) strList.toArray(new String[strList.size()]));

        } else {
          // String
          valuesOfConditionPropertyPathToValidate = String.valueOf(obj);
        }

      } else if (conditionPtn == stringValueOfConditionPropertyPathIsEqualTo
          || conditionPtn == stringValueOfConditionPropertyPathIsNotEqualTo) {
        // conditionValue is used

        String[] strs = (String[]) paramMap.get(ConditionalValidator.CONDITION_VALUE_STRING);
        valuesOfConditionPropertyPathToValidate = StringUtil.getCsvWithSpace(strs);
      }

      // when fieldHoldingConditionValueDisplayName is not blank,
      // valuesOfConditionFieldToValidate is overrided by its value.
      String valueOfConditionValuePropertyPathForDisplay = (String) paramMap
          .get(ConditionalValidator.VALUE_OF_CONDITION_VALUE_PROPERTY_PATH_FOR_DISPLAY);
      if (!valueOfConditionValuePropertyPathForDisplay.equals("")) {
        String[] strs =
            (String[]) getFieldValue(valueOfConditionValuePropertyPathForDisplay, getInstance());
        valuesOfConditionPropertyPathToValidate = StringUtil.getCsvWithSpace(strs);
      }

      paramMap.put(ConditionalValidator.VALUE_OF_CONDITION_FIELD_TO_VALIDATE,
          valuesOfConditionPropertyPathToValidate);

      // validatesWhenConditionNotSatisfied
      boolean bl = getAnnotation().endsWith("ConditionalEmpty")
          && (Boolean) paramMap
              .get(ConditionalValidator.VALIDATES_WHEN_CONDITION_NOT_SATISFIED_EMPTY)
          || getAnnotation().endsWith("ConditionalNotEmpty") && (Boolean) paramMap
              .get(ConditionalValidator.VALIDATES_WHEN_CONDITION_NOT_SATISFIED_NOT_EMPTY);
      paramMap.put(ConditionalValidator.VALIDATES_WHEN_CONDITION_NOT_SATISFIED, bl);
    }
  }

  private String getFinalItemKindIdClass(Optional<String> itemKindIdClassFromAnnotation,
      String defaultItemKindIdClass) {
    String itemKindIdClass = itemKindIdClassFromAnnotation.orElse(defaultItemKindIdClass);

    // Remove "aClass$" from "aClass$bClass" when itemKindIdClass is an internal class.
    return itemKindIdClass.split("\\$")[itemKindIdClass.split("\\$").length - 1];
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

  public String[] getItemKindIds() {
    return itemKindIds;
  }

  @Nonnull
  public Map<String, Object> getParamMap() {
    return paramMap;
  }

  public String[] getFieldPropertyPaths() {
    return fieldPropertyPaths;
  }

}
