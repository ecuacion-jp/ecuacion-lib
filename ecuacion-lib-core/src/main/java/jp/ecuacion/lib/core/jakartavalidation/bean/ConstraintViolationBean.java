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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import jp.ecuacion.lib.core.jakartavalidation.constraints.MultiplePropertyPathsValidator;
import jp.ecuacion.lib.core.util.MessageUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Stores {@code ConstraintViolation} info normalized for the message-building pipeline.
 *
 * <p>This is an internal DTO used by {@code ExceptionUtil}
 *     and {@code ValidatorMessageParameterCreator} implementations.
 *     It augments {@link jakarta.validation.ConstraintViolation} with information
 *     that is not directly available from the Jakarta API:</p>
 *
 * <ul>
 *   <li>{@code itemList} expands a single violation into multiple property paths
 *       for {@code MultiplePropertyPathsValidator} (class- / method-level validators).</li>
 *   <li>{@code embeddedParamMap} is the constraint descriptor's attributes
 *       cleaned of {@code groups}, {@code message}, {@code payload},
 *       and augmented with {@code annotation}, {@code itemAttributes},
 *       and {@code invalidValue} for use as message-template parameters.</li>
 * </ul>
 *
 * <p>Instances are created via {@link #createConstraintViolationBean(ConstraintViolation)}.</p>
 */
public class ConstraintViolationBean<T> extends ReflectionUtil {

  // properties in ConstraintViolation

  /**
   * RootBean, which cannot be {@code null} because validation cannot be executed if it is.
   */
  private T rootBean;
  /**
   * LeafBean, which cannot be {@code null} because validation cannot be executed if it is.
   */
  private Object leafBean;
  /**
   * Validator class. There must be one if validation is executed so it cannot be {@code null}.
   */
  private String validatorClass;
  /** 
   * The propertyPath ConstraintViolation stores.<br>
   * It is different from item.propertyPath 
   * when classValidators or methodValidators are used.
   */
  private String constraintViolationPropertyPath;
  private @Nullable Object invalidValue;
  private String messageTemplate;
  private String message;
  /** Message already resolved by the Jakarta Validation implementation 
   * ({@code cv.getMessage()}). */
  private String resolvedMessage;
  /** Original constraint descriptor, used for locale-aware re-interpolation. */
  private ConstraintDescriptor<?> constraintDescriptor;
  /**
   * The value of the map is {@code @Nullable} 
   *     because this map stores {@code invalidValue} parameter 
   *     and its value (validated value) can be {@code null}.
   */
  private Map<@NonNull String, @Nullable Object> embeddedParamMap = new HashMap<>();

  // values needed for all the patterns

  private List<Item> itemList = new ArrayList<>();

  private ConstraintViolationBean(String validatorClassName,
      @NonNull T rootBean, Object leafBean, @Nullable Object invalidValue, String messageTemplate,
      Map<@NonNull String, @Nullable Object> embeddedParameterMap,
      String constraintViolationPropertyPath, String resolvedMessage,
      ConstraintDescriptor<?> constraintDescriptor, String... propertyPaths) {

    this.rootBean = rootBean;
    this.leafBean = leafBean;
    this.validatorClass = validatorClassName;
    this.constraintViolationPropertyPath = constraintViolationPropertyPath;
    this.invalidValue = invalidValue;
    this.constraintDescriptor = constraintDescriptor;
    String msg = PropertiesFileUtil.getValidationMessage(Locale.ENGLISH,
        messageTemplate.replace("{", "").replace("}", ""), new HashMap<>());
    this.message = msg;
    this.messageTemplate = messageTemplate;
    this.resolvedMessage = resolvedMessage;

    List<@NonNull String> propertyPathList = List.of(propertyPaths);
    for (String fullPropertyPath : propertyPathList) {
      itemList.add(MessageUtil.getItem(fullPropertyPath, rootBean, leafBean));
    }

    embeddedParamMap.put("invalidValue", invalidValue);

    // Put field in this instance to paramMap
    embeddedParamMap.put("annotation", validatorClass);
    embeddedParamMap.put("itemAttributes", itemList.toArray(Item[]::new));

    if (embeddedParameterMap != null) {
      this.embeddedParamMap.putAll(embeddedParameterMap);
    }
  }

  /**
   * Creates a new ConstraintViolationBean instance from {@code ConstraintViolation}.
   * 
   * <p>Developers don't need to use this. It is used only in ecuacion libraries.</p>
   * 
   * @param cv ConstraintViolation
   */
  public static <U> ConstraintViolationBean<U> createConstraintViolationBean(
      ConstraintViolation<U> cv) {

    boolean isParamNull = cv.getConstraintDescriptor().getAttributes() == null;

    // embeddedParamMap
    Map<@NonNull String, @Nullable Object> embeddedParamMap =
        isParamNull ? new HashMap<>() : new HashMap<>(cv.getConstraintDescriptor().getAttributes());
    // Remove keys which are not used as message parameters.
    embeddedParamMap.remove("groups");
    embeddedParamMap.remove("message");
    embeddedParamMap.remove("payload");

    // validatorClass
    Class<?> validatorClass = cv.getConstraintDescriptor().getConstraintValidatorClasses().get(0);

    boolean isMultiplePropertyPathsValidator =
        MultiplePropertyPathsValidator.class.isAssignableFrom(validatorClass);
    boolean isClassValidator = ClassValidator.class.isAssignableFrom(validatorClass);

    // propertyPathList
    String cvPp = cv.getPropertyPath() == null ? "" : cv.getPropertyPath().toString();
    List<@NonNull String> ppList = null;
    if (isMultiplePropertyPathsValidator) {
      // Base differs class from method.
      String cvPpBase = isClassValidator ? cvPp
          : (cvPp.contains(".") ? cvPp.substring(0, cvPp.lastIndexOf(".")) : "");
      String cvPpPrefix = (StringUtils.isEmpty(cvPpBase) ? "" : cvPpBase + ".");

      ppList = Arrays.stream((String[]) embeddedParamMap.get("propertyPath"))
          .map(p -> cvPpPrefix + p).toList();

    } else {
      ppList = new ArrayList<>();
      ppList.add(cvPp);
    }

    ConstraintViolationBean<U> rtnCv = new ConstraintViolationBean<>(
        Objects.requireNonNull(Objects.requireNonNull(cv.getConstraintDescriptor()).getAnnotation())
            .annotationType().getName(),
        cv.getRootBean(), cv.getLeafBean(), cv.getInvalidValue(), cv.getMessageTemplate(),
        embeddedParamMap, cv.getPropertyPath().toString(), cv.getMessage(),
        cv.getConstraintDescriptor(), ppList.toArray(String[]::new));

    return rtnCv;
  }

  /** 
   * Outputs a string for logs. 
   * 
   * @return String
   */
  @SuppressWarnings("null")
  @Override
  public String toString() {
    return "message:" + getMessage() + "\n" + "annotation:" + getValidatorClass() + "\n"
        + "rootClassName:" + Objects.requireNonNull(getRootBean()).getClass().getName() + "\n"
        + "leafClassName:" + getLeafBean().getClass().getName() + "\n" + "propertyPath:"
        + StringUtil.getCsv(getItemList().stream().map(Item::getPropertyPath).toList()) + "\n"
        + "invalidValue:" + getInvalidValue();
  }

  public T getRootBean() {
    return rootBean;
  }

  public Object getLeafBean() {
    return leafBean;
  }

  /**
   * Returns constraintViolationPropertyPath.
   */
  public String getConstraintViolationPropertyPath() {
    return constraintViolationPropertyPath;
  }

  /**
   * Returns message.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns messageTemplate.
   */
  public String getMessageTemplate() {
    return messageTemplate;
  }

  /**
   * Returns the message already resolved by the Jakarta Validation implementation
   *     ({@code cv.getMessage()}).
   *
   * <p>Used as fallback when no entry is found in ecuacion-lib properties files.</p>
   */
  public String getResolvedMessage() {
    return resolvedMessage;
  }

  /**
   * Returns the constraint descriptor, used for locale-aware re-interpolation.
   */
  public ConstraintDescriptor<?> getConstraintDescriptor() {
    return constraintDescriptor;
  }

  /**
   * Returns the raw invalid value object (may be {@code null}).
   */
  public @Nullable Object getInvalidValueObject() {
    return invalidValue;
  }

  public String getValidatorClass() {
    return validatorClass;
  }

  /**
   * Returns {@code invalidValue} as a string.
   *
   * <p>Returns {@code "null"} string when the value is {@code null}.</p>
   */
  public String getInvalidValue() {
    Object iv = invalidValue;
    return iv == null ? "null" : iv.toString();
  }

  public List<Item> getItemList() {
    return itemList;
  }

  public Item[] getItems() {
    return itemList.toArray(Item[]::new);
  }

  public Map<@NonNull String, @Nullable Object> getEmbeddedParamMap() {
    return embeddedParamMap;
  }

}
