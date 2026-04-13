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
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import jp.ecuacion.lib.core.jakartavalidation.constraints.MultiplePropertyPathsValidator;
import jp.ecuacion.lib.core.util.MessageUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

/** 
 * Stores {@code ConstraintViolation} info.
 * 
 * <p>The reason of the existence of the class is that the violations 
 *     which are not created by {@code Jakarta Validation} can also be treated
 *     just as the one created by {@code Jakarta Validation}.</p>
 */
public class ConstraintViolationBean<T> extends ReflectionUtil implements ConstraintViolation<T> {

  // properties in ConstraintViolation

  private T rootBean;
  private Object leafBean;
  private String validatorClass;
  /** 
   * The propertyPath ConstraintViolation stores.<br>
   * It is different from item.propertyPath 
   * when classValidators or methodValidators are used.
   */
  private String constraintViolationPropertyPath;
  private Object invalidValue;
  private String messageTemplate;
  private String message;

  // values needed for all the patterns

  private ValidatorKindEnum validatorKind;
  private List<Item> itemList = new ArrayList<>();
  @Nonnull
  private Map<String, Object> embeddedParamMap = new HashMap<>();

  private void putArgsToFields(ValidatorKindEnum validatorKind, T rootBean, Object leafBean,
      String validatorClass, String message, String messageTemplate,
      String constraintViolationPropertyPath, List<String> propertyPathList, Object invalidValue) {
    this.validatorKind = validatorKind;
    this.rootBean = rootBean;
    this.leafBean = leafBean;
    this.validatorClass = validatorClass;
    this.constraintViolationPropertyPath = constraintViolationPropertyPath;
    this.invalidValue = invalidValue;
    this.message = message;
    this.messageTemplate = messageTemplate;

    for (int i = 0; i < propertyPathList.size(); i++) {
      String fullPropertyPath = propertyPathList.get(i);
      itemList.add(MessageUtil.getItem(fullPropertyPath, rootBean, leafBean));
    }

    embeddedParamMap.put("invalidValue", invalidValue);

    // Put field in this instance to paramMap
    embeddedParamMap.put("annotation", validatorClass);
    embeddedParamMap.put("itemAttributes",
        itemList.toArray(new Item[itemList.size()]));
  }

  /**
   * Constructs a new instance.
   */
  public ConstraintViolationBean(String validatorClassName, T rootBean, String messageTemplate,
      String propertyPath) {
    this(validatorClassName, rootBean, null, messageTemplate, propertyPath);
  }

  /**
   * Constructs a new instance.
   */
  public ConstraintViolationBean(String validatorClassName, T rootBean, Object invalidValue,
      String messageTemplate, String propertyPath) {
    this(ValidatorKindEnum.FIELD, validatorClassName, rootBean, rootBean, invalidValue,
        messageTemplate, null, propertyPath, propertyPath);
  }

  /**
   * Constructs a new instance with parameters, not a ConstraintViolation.
   */
  public ConstraintViolationBean(ValidatorKindEnum validatorKind, String validatorClassName,
      T rootBean, Object leafBean, Object invalidValue, String messageTemplate,
      Map<String, Object> embeddedParameterMap, String constraintViolationPropertyPath,
      String... propertyPaths) {

    putArgsToFields(validatorKind, rootBean, leafBean, validatorClassName,
        PropertiesFileUtil.getValidationMessage(Locale.ENGLISH,
            messageTemplate.replace("{", "").replace("}", ""), new HashMap<>()),
        messageTemplate, constraintViolationPropertyPath, List.of(propertyPaths),
        invalidValue == null ? null : invalidValue.toString());

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
    Map<String, Object> embeddedParamMap =
        isParamNull ? new HashMap<>() : new HashMap<>(cv.getConstraintDescriptor().getAttributes());
    // Remove keys which are not used as message parameters.
    embeddedParamMap.remove("groups");
    embeddedParamMap.remove("message");
    embeddedParamMap.remove("payload");

    // validatorClass
    Class<?> validatorClass = cv.getConstraintDescriptor().getConstraintValidatorClasses().get(0);

    // validatorKind
    boolean isMultiplePropertyPathsValidator =
        MultiplePropertyPathsValidator.class.isAssignableFrom(validatorClass);
    boolean isClassValidator = ClassValidator.class.isAssignableFrom(validatorClass);
    ValidatorKindEnum validatorKind = isMultiplePropertyPathsValidator
        ? (isClassValidator ? ValidatorKindEnum.CLASS : ValidatorKindEnum.METHOD)
        : ValidatorKindEnum.FIELD;

    // propertyPathList
    String cvPp = cv.getPropertyPath() == null ? "" : cv.getPropertyPath().toString();
    List<String> ppList = null;
    if (isMultiplePropertyPathsValidator) {
      // Base differs class from method.
      String cvPpBase = isClassValidator ? cvPp
          : (cvPp.contains(".") ? cvPp.substring(0, cvPp.lastIndexOf(".")) : "");
      String cvPpPrefix = (StringUtils.isEmpty(cvPpBase) ? "" : cvPpBase + ".");

      ppList = (Arrays.asList((String[]) embeddedParamMap.get("propertyPath")).stream()
          .map(p -> cvPpPrefix + p).toList());

    } else {
      ppList = new ArrayList<>();
      ppList.add(cvPp);
    }

    return new ConstraintViolationBean<>(validatorKind,
        cv.getConstraintDescriptor().getAnnotation().annotationType().getName(), cv.getRootBean(),
        cv.getLeafBean(), cv.getInvalidValue(), cv.getMessageTemplate(), embeddedParamMap,
        cv.getPropertyPath().toString(), ppList.toArray(new String[ppList.size()]));
  }

  /** 
   * Outputs a string for logs. 
   * 
   * @return String
   */
  @Override
  public @Nonnull String toString() {
    return "message:" + getMessage() + "\n" + "annotation:" + getValidatorClass() + "\n"
        + "rootClassName:" + getRootBean().getClass().getName() + "\n" + "leafClassName:"
        + getLeafBean().getClass().getName() + "\n" + "propertyPath:"
        + StringUtil.getCsv(getItemList().stream().map(b -> b.getPropertyPath()).toList())
        + "\n" + "invalidValue:" + getInvalidValue();
  }

  public T getRootBean() {
    return rootBean;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<T> getRootBeanClass() {
    return (Class<T>) rootBean.getClass();
  }

  public Object getLeafBean() {
    return leafBean;
  }

  @Override
  public Path getPropertyPath() {
    return new Path() {

      public String toString() {
        return constraintViolationPropertyPath;
      }

      @Override
      public Iterator<Node> iterator() {
        return null;
      }
    };
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public @Nonnull String getMessageTemplate() {
    return messageTemplate;
  }

  public String getValidatorClass() {
    return validatorClass;
  }

  public String getInvalidValue() {
    return invalidValue == null ? "null" : invalidValue.toString();
  }

  public ValidatorKindEnum getValidatorKind() {
    return validatorKind;
  }

  public List<Item> getItemList() {
    return itemList;
  }

  public Item[] getItems() {
    return itemList.toArray(new Item[itemList.size()]);
  }

  @Nonnull
  public Map<String, Object> getEmbeddedParamMap() {
    return embeddedParamMap;
  }

  @Override
  public Object[] getExecutableParameters() {
    throw new RuntimeException("Not assumed to call.");
  }

  @Override
  public Object getExecutableReturnValue() {
    throw new RuntimeException("Not assumed to call.");
  }

  @Override
  public ConstraintDescriptor<?> getConstraintDescriptor() {
    throw new RuntimeException("Not assumed to call.");
  }

  @Override
  public <U> U unwrap(Class<U> type) {
    throw new RuntimeException("Not assumed to call.");
  }

  /**
   * Stores validation kind.
   */
  public static enum ValidatorKindEnum {
    CLASS, METHOD, FIELD;
  }
}
