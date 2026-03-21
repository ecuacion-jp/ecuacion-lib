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
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import jp.ecuacion.lib.core.jakartavalidation.constraints.MultiplePropertyPathsValidator;
import jp.ecuacion.lib.core.util.MessageUtil;
import jp.ecuacion.lib.core.util.PropertyFileUtil;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

/** 
 * Stores {@code ConstraintViolation} info.
 * 
 * <p>The reason of the existence of the class is that the violations 
 *     which are not created by {@code Jakarata Validation} can also be treated 
 *     just like the one created by {@code Jakarata Validation}.</p>
 */
public class ConstraintViolationBean<T> extends ReflectionUtil implements ConstraintViolation<T> {
  private ConstraintViolation<T> cv;

  // properties in ConstraintViolation

  private T rootBean;
  private Object leafBean;
  private String validatorClass;
  private String messageTemplate;
  private String originalMessage;

  // values needed for all the patterns

  private List<FieldInfoBean> fieldInfoBeanList = new ArrayList<>();

  @Nonnull
  private Map<String, Object> embeddedParamMap = new HashMap<>();

  private void putArgsToFields(T rootBean, Object leafBean, String validatorClass,
      String originalMessage, String messageTemplate, List<String> fullPropertyPathList,
      String invalidValue) {
    this.rootBean = rootBean;
    this.leafBean = leafBean;
    this.validatorClass = validatorClass;
    this.originalMessage = originalMessage;
    this.messageTemplate = messageTemplate;

    for (int i = 0; i < fullPropertyPathList.size(); i++) {
      String fullPropertyPath = fullPropertyPathList.get(i);
      fieldInfoBeanList
          .add(MessageUtil.getFieldInfoBean(fullPropertyPath, rootBean, leafBean.getClass()));
    }

    embeddedParamMap.put("invalidValue", invalidValue);

    // Put field in this instance to paramMap
    embeddedParamMap.put("annotation", validatorClass);
    embeddedParamMap.put("itemAttributes",
        fieldInfoBeanList.toArray(new FieldInfoBean[fieldInfoBeanList.size()]));
  }

  /**
   * Constructs a new instance with parameters, not a ConstraintViolation.
   * 
   * <p>This is used for {@code NotEmpty} validation logic.</p>
   */
  public ConstraintViolationBean(T rootBean, String message, String validatorClass,
      String propertyPath) {
    List<String> ppList = List.of(propertyPath);
    putArgsToFields(rootBean, getLeafBean(rootBean, propertyPath), validatorClass,
        PropertyFileUtil.getMessage(message), message, ppList, "(empty)");
  }

  /**
   * Constructs a new instance with {@code ConstraintViolation}.
   * 
   * @param cv ConstraintViolation
   */
  public ConstraintViolationBean(ConstraintViolation<T> cv) {
    this.cv = cv;

    // Initialize paramMap.
    if (cv.getConstraintDescriptor().getAttributes() != null) {
      this.embeddedParamMap = new HashMap<>(cv.getConstraintDescriptor().getAttributes());
      // Remove keys which are not used as message parameters.
      embeddedParamMap.remove("groups");
      embeddedParamMap.remove("message");
      embeddedParamMap.remove("payload");
    }

    Class<?> validatorClass = cv.getConstraintDescriptor().getConstraintValidatorClasses().get(0);
    boolean isMultiplePropertyPathsValidator =
        MultiplePropertyPathsValidator.class.isAssignableFrom(validatorClass);
    boolean isClassValidator = ClassValidator.class.isAssignableFrom(validatorClass);

    // propertyPath
    String cvPp = cv.getPropertyPath() == null ? "" : cv.getPropertyPath().toString();
    List<String> fullPpList = null;
    if (isMultiplePropertyPathsValidator) {
      // Base differs class from method.
      String cvPpBase = isClassValidator ? cvPp
          : (cvPp.contains(".") ? cvPp.substring(0, cvPp.lastIndexOf(".")) : "");
      String cvPpPrefix = (StringUtils.isEmpty(cvPpBase) ? "" : cvPpBase + ".");

      fullPpList = (Arrays.asList((String[]) embeddedParamMap.get("propertyPath")).stream()
          .map(p -> cvPpPrefix + p).toList());

    } else {
      fullPpList = new ArrayList<>();
      fullPpList.add(cvPp);
    }

    String rootClassName = StringUtils.uncapitalize(cv.getRootBean().getClass().getSimpleName());
    // remove "aClass$" from "aClass$bCLass" when the class is internal.
    rootClassName = rootClassName.split("\\$")[rootClassName.split("\\$").length - 1];

    // Substitute to common fields.
    putArgsToFields(cv.getRootBean(), cv.getLeafBean(),
        cv.getConstraintDescriptor().getAnnotation().annotationType().getName(), cv.getMessage(),
        // Remove {} since the value is usually enclosed with {} like
        // {jakarta.validation.constraints.Pattern.message}.
        cv.getMessageTemplate().replace("{", "").replace("}", ""), fullPpList, getInvalidValue());
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
        + StringUtil.getCsv(getFieldInfoBeanList().stream().map(b -> b.propertyPath).toList())
        + "\n" + "invalidValue:" + getInvalidValue();
  }

  public ConstraintViolation<T> getConstraintViolation() {
    return cv;
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

  /**
   * Gets message created by jakarta validation.
   * 
   * <p>DO NOT USE for user interface. Use the message obtained by ExceptionUtil instead.</p>
   * 
   * @return original message
   */
  @Override
  public String getMessage() {
    return originalMessage;
  }

  @Override
  public @Nonnull String getMessageTemplate() {
    return messageTemplate;
  }

  public String getValidatorClass() {
    return validatorClass;
  }

  public String getInvalidValue() {
    return (cv == null || cv.getInvalidValue() == null) ? "null" : cv.getInvalidValue().toString();
  }

  public List<FieldInfoBean> getFieldInfoBeanList() {
    return fieldInfoBeanList;
  }

  public FieldInfoBean[] getFieldInfoBeans() {
    return fieldInfoBeanList.toArray(new FieldInfoBean[fieldInfoBeanList.size()]);
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
  public Path getPropertyPath() {
    throw new RuntimeException("Not assumed to call. Use 'getFieldInfoBeanList'.");
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
   * Stores field-unit parameters.
   * 
   * @param propertyPath The key of the bean.
   *     It's a propertyPath which designate from rootBean to the violation-occurring field.
   */
  public static record FieldInfoBean(String propertyPath, String itemNameKey, boolean showsValue) {

  }
}
