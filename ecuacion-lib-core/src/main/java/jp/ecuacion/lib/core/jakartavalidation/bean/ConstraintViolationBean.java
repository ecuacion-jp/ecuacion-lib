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

import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionValuePattern.string;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionValuePattern.valueOfPropertyPath;

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
import jp.ecuacion.lib.core.jakartavalidation.validator.ItemNameKeyClass;
import jp.ecuacion.lib.core.jakartavalidation.validator.PlacedAtClass;
import jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionValuePattern;
import jp.ecuacion.lib.core.jakartavalidation.validator.internal.ConditionalValidator;
import jp.ecuacion.lib.core.record.EclibRecord;
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

  private Object rootBean;
  private String message;

  /** 
   * When a validator added to a class detects a violation, 
   * it can be a combination of values between multiple items. 
   * In that case you want to set error multiple itemPropertyPaths for those items.
   */
  private String[] itemPropertyPaths;
  private String[] itemNameKeys;

  private String validatorClass;
  private String rootClassName;
  private String messageTemplate;
  private String annotationDescriptionString;

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

    this.rootBean = cv.getRootBean();
    this.message = cv.getMessage();
    this.validatorClass = cv.getConstraintDescriptor().getAnnotation().annotationType().getName();
    this.rootClassName = cv.getRootBeanClass().getName();
    this.annotationDescriptionString = cv.getConstraintDescriptor().getAnnotation().toString();
    messageTemplate = cv.getMessageTemplate();
    // Remove {} since the value is always enclosed with {} like
    // {jakarta.validation.constraints.Pattern.message}.
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
   */
  public ConstraintViolationBean(EclibRecord rootBean, String message, String validatorClass,
      String rootClassName, String itemPropertyPath) {
    this.rootBean = rootBean;
    this.message = message;
    this.validatorClass = validatorClass;
    this.rootClassName = rootClassName;
    this.messageTemplate = validatorClass + ".message";

    this.itemPropertyPaths = new String[] {itemPropertyPath};
    this.itemNameKeys =
        new String[] {rootBean.getItem(itemPropertyPath).getItemNameKey(rootClassName)};
    this.paramMap = new HashMap<>();

    // This is for @Pattern and not used when this constructor is called.
    // But null value causes an exception so let it set as blank.
    annotationDescriptionString = "";
  }

  private Optional<String> getItemNameKeyClassFromAnnotation(boolean isClassValidator,
      Object leafBean, String propertyPath) {

    Class<?> modifiedLeafBeanClass = leafBean.getClass();
    try {
      // search for @ItemNameKeyClass at field
      if (!isClassValidator) {
        String fieldName = propertyPath.split("\\.")[propertyPath.split("\\.").length - 1];
        Field field = getField(fieldName, leafBean.getClass());
        ItemNameKeyClass an = field.getAnnotation(ItemNameKeyClass.class);
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
          modifiedLeafBeanClass = getField(fieldName, modifiedLeafBeanClass).getType();

          // Change value for the next loop
          tmpPp = tmpPp.contains(".") ? tmpPp.substring(tmpPp.indexOf(".") + 1) : "";
        }
      }

      Optional<ItemNameKeyClass> an =
          searchAnnotationPlacedAtClass(modifiedLeafBeanClass, ItemNameKeyClass.class);
      return Optional.ofNullable(an.isPresent() ? an.get().value() : null);

    } catch (Exception ex) {
      throw new EclibRuntimeException(ex);
    }
  }

  private void putAdditionalParamsToParamMap(ConstraintViolation<?> cv) {

    final String propertyPath = cv.getPropertyPath().toString();

    // When localized messages are created, paramMap is an only parameter.
    // So some values should be put into the map.
    paramMap.put("leafClassName", getLeafClassName());
    paramMap.put("invalidValue", getInvalidValue());
    paramMap.put("annotation", getAnnotation());

    // checks if the validator is for class or field.
    boolean isClassValidator = cv.getConstraintDescriptor().getAnnotation().annotationType()
        .getAnnotation(PlacedAtClass.class) != null;

    // itemNameKeyClass
    String defaultItemNameKeyClass = null;

    // itemNameKeyField
    List<String> itemNameKeyList = new ArrayList<>();
    if (isClassValidator) {
      // Reaching here means the annotation is added to class, not field.
      itemPropertyPaths = (String[]) paramMap.get("propertyPath");

      for (String itemPropertyPath : itemPropertyPaths) {
        // String itemNameKey;
        // if (rootBean instanceof )
        // = rootBean instanceof EclibRecord ? ((EclibRecord)
        // rootBean).getItem(fieldPropertyPath).getItemNameKey(defaultItemNameKeyClass)
        Optional<String> itemNameKeyClassFromAnnotation =
            getItemNameKeyClassFromAnnotation(isClassValidator, cv.getLeafBean(),
                itemPropertyPath.contains(".")
                    ? itemPropertyPath.substring(0, itemPropertyPath.lastIndexOf("."))
                    : "");

        defaultItemNameKeyClass = StringUtils.isEmpty(propertyPath)
            ? rootClassName.split("\\.")[rootClassName.split("\\.").length - 1]
            : propertyPath.split("\\.")[propertyPath.split("\\.").length - 1];

        String itemNameKey =
            getFinalItemNameKeyClass(itemNameKeyClassFromAnnotation, defaultItemNameKeyClass) + "."
                + (itemPropertyPath.contains(".")
                    ? (itemPropertyPath.substring(itemPropertyPath.lastIndexOf(".") + 1))
                    : itemPropertyPath);

        itemNameKeyList.add(itemNameKey);
      }

      // conditionFieldItemNameKey
      String conditionPropertyPath =
          (String) paramMap.get(ConditionalValidator.CONDITION_PROPERTY_PATH);
      Optional<String> itemNameKeyClassFromAnnotation = getItemNameKeyClassFromAnnotation(
          isClassValidator, cv.getLeafBean(), conditionPropertyPath);
      String newValue =
          getFinalItemNameKeyClass(itemNameKeyClassFromAnnotation, defaultItemNameKeyClass) + "."
              + conditionPropertyPath;
      paramMap.put(ConditionalValidator.CONDITION_PROPERTY_PATH_ITEM_NAME_KEY, newValue);

    } else {
      // Reaching here means the annotation is added to field.
      // In that case propertyPath cannot be empty and the last part is always itemNameKeyField.

      // itemNameKey
      Optional<String> itemNameKeyClassFromAnnotation =
          getItemNameKeyClassFromAnnotation(isClassValidator, cv.getLeafBean(), propertyPath);
      defaultItemNameKeyClass = propertyPath.split("\\.").length > 1
          ? propertyPath.split("\\.")[propertyPath.split("\\.").length - 2]
          : rootClassName.split("\\.")[rootClassName.split("\\.").length - 1];
      String field = propertyPath.split("\\.")[propertyPath.split("\\.").length - 1];
      String itemNameKey =
          getFinalItemNameKeyClass(itemNameKeyClassFromAnnotation, defaultItemNameKeyClass) + "."
              + field;

      itemNameKeyList.add(itemNameKey);
    }

    // itemNameKeys
    itemNameKeys = itemNameKeyList.toArray(new String[itemNameKeyList.size()]);
    paramMap.put("itemNameKeys", itemNameKeys);

    // In the case of ConditionalXxx validator
    if (getAnnotation().startsWith(CONDITIONAL_VALIDATOR_PREFIX)) {

      ConditionValuePattern conditionPtn =
          (ConditionValuePattern) paramMap.get(ConditionalValidator.CONDITION_PATTERN);
      paramMap.put(ConditionalValidator.CONDITION_PATTERN, conditionPtn);

      String valuesOfConditionPropertyPathToValidate = null;
      if (conditionPtn == valueOfPropertyPath) {

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

      } else if (conditionPtn == string) {
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

  private String getFinalItemNameKeyClass(Optional<String> itemNameKeyClassFromAnnotation,
      String defaultItemNameKeyClass) {
    String itemNameKeyClass = itemNameKeyClassFromAnnotation.orElse(defaultItemNameKeyClass);

    // Remove "aClass$" from "aClass$bClass" when itemNameKeyClass is an internal class.
    return itemNameKeyClass.split("\\$")[itemNameKeyClass.split("\\$").length - 1];
  }

  public Object getRootBean() {
    return rootBean;
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

  /**
  * Obtains itemNameKeys.
  */
  public String[] getItemNameKeys() {
    return itemNameKeys;
  }

  @Nonnull
  public Map<String, Object> getParamMap() {
    return paramMap;
  }

  /**
   * Obtains itemPropertyPaths.
   */
  public String[] getItemPropertyPaths() {
    return itemPropertyPaths;
  }
}
