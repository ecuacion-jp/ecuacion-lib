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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jp.ecuacion.lib.core.item.EclibItem;
import jp.ecuacion.lib.core.item.EclibItemContainer;
import jp.ecuacion.lib.core.jakartavalidation.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.jakartavalidation.annotation.PlacedAtClass;
import jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionValuePattern;
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

  // properties in ConstraintViolation

  private Object rootBean;
  private Object leafBean;
  private String validatorClass;
  private String messageTemplate;
  private String originalMessage;

  // values needed for all the patterns

  private List<FieldInfoBean> fieldInfoBeanList;

  // values needed for validations for form

  private String rootRecordNameForForm;

  @Nonnull
  private Map<String, Object> paramMap = new HashMap<>();

  private static final String CONDITIONAL_VALIDATOR_PREFIX =
      "jp.ecuacion.lib.core.jakartavalidation.validator.Conditional";

  private void putArgsToFields(Object rootBean, Object leafBean, String validatorClass,
      String originalMessage, String messageTemplate, String rootRecordNameForForm,
      List<FieldInfoBean> beanList) {
    this.rootBean = rootBean;
    this.validatorClass = validatorClass;
    this.rootRecordNameForForm = rootRecordNameForForm;
    this.originalMessage = originalMessage;
    this.messageTemplate = messageTemplate;
    this.leafBean = leafBean;
    this.fieldInfoBeanList = beanList;

    getItemDependentValues(beanList);
  }

  private void putArgsToParamMap(Object invalidValue) {
    paramMap.put("invalidValue", invalidValue.toString());

    // Put field in this instance to paramMap
    paramMap.put("annotation", validatorClass);
    paramMap.put("itemAttributes",
        fieldInfoBeanList.toArray(new FieldInfoBean[fieldInfoBeanList.size()]));
  }

  /**
   * Constructs a new instance with parameters, not a ConstraintViolation.
   * 
   * <p>This is used for {@code NotEmpty} validation logic.</p>
   */
  public ConstraintViolationBean(Object rootBean, String message, String validatorClass,
      String rootRecordNameForForm, String itemPropertyPath) {

    List<FieldInfoBean> beanList = new ArrayList<>();
    beanList.add(new FieldInfoBean(rootRecordNameForForm + "." + itemPropertyPath));
    beanList.get(0).itemPropertyPathForForm = itemPropertyPath;

    putArgsToFields(rootBean,
        getLeafBeanFromPropertyPath(rootRecordNameForForm + "." + itemPropertyPath, rootBean),
        validatorClass, message, validatorClass + ".message", rootRecordNameForForm, beanList);

    putArgsToParamMap("(empty)");

    // "paramMap" is used to get strings to embed them to error messages,
    // but @NotEmpty does not need parameter strings because the message is like
    // "The input is empty." and that's all so params are not really needed.
    // this.paramMap = new HashMap<>();
  }

  /**
   * Constructs a new instance with {@code ConstraintViolation}.
   * 
   * @param cv ConstraintViolation
   */
  public ConstraintViolationBean(ConstraintViolation<?> cv) {
    this.cv = cv;

    // Initialize paramMap.
    if (cv.getConstraintDescriptor().getAttributes() != null) {
      this.paramMap = new HashMap<>(cv.getConstraintDescriptor().getAttributes());
      // Remove keys which are not used as message parameters.
      paramMap.remove("groups");
      paramMap.remove("message");
      paramMap.remove("payload");
    }

    // Check if the validator is for class or field.
    boolean isClassValidator = cv.getConstraintDescriptor().getAnnotation().annotationType()
        .getAnnotation(PlacedAtClass.class) != null;

    // propertyPath
    String cvPp = cv.getPropertyPath() == null ? "" : cv.getPropertyPath().toString();
    List<String> fullPpList = null;
    if (isClassValidator) {
      fullPpList = (Arrays.asList((String[]) paramMap.get("propertyPath")).stream()
          .map(p -> (StringUtils.isEmpty(cvPp) ? "" : cvPp + ".") + p).toList());

    } else {
      fullPpList = new ArrayList<>();
      fullPpList.add(cvPp);
    }

    List<FieldInfoBean> beanList = new ArrayList<>();
    fullPpList.stream().forEach(pp -> beanList.add(new FieldInfoBean(pp)));
    beanList.stream()
        .peek(bean -> bean.itemPropertyPathForForm = bean.fullPropertyPath.contains(".")
            ? bean.fullPropertyPath.substring(bean.fullPropertyPath.indexOf(".") + 1)
            : bean.fullPropertyPath)
        .toList();
    String fullPp0 = fullPpList.get(0);
    String rootClassName = StringUtils.uncapitalize(cv.getRootBean().getClass().getSimpleName());
    // remove "aClass$" from "aClass$bCLass" when the class is internal.
    rootClassName = rootClassName.split("\\$")[rootClassName.split("\\$").length - 1];
    String rootRecordNameForForm =
        fullPp0.contains(".") ? fullPp0.substring(0, fullPp0.indexOf(".")) : rootClassName;

    // Substitute to common fields.
    putArgsToFields(cv.getRootBean(), cv.getLeafBean(),
        cv.getConstraintDescriptor().getAnnotation().annotationType().getName(), cv.getMessage(),
        // Remove {} since the value is usually enclosed with {} like
        // {jakarta.validation.constraints.Pattern.message}.
        cv.getMessageTemplate().replace("{", "").replace("}", ""), rootRecordNameForForm, beanList);

    // Put params to paramMap.
    // When localized messages are created, parameters are refered from the map.

    putArgsToParamMap(getInvalidValue());

    // In the case of ConditionalXxx validator
    if (getValidatorClass().startsWith(CONDITIONAL_VALIDATOR_PREFIX)) {
      // conditionFieldItemNameKey
      String conditionPropertyPath = (StringUtils.isEmpty(cv.getPropertyPath().toString()) ? ""
          : cv.getPropertyPath().toString() + ".")
          + ((String) paramMap.get(ConditionalValidator.CONDITION_PROPERTY_PATH));
      FieldInfoBean bean = getItemDependentValues(conditionPropertyPath,
          getLeafBeanFromPropertyPath(conditionPropertyPath, rootBean).getClass());
      paramMap.put(ConditionalValidator.CONDITION_PROPERTY_PATH_ITEM_NAME_KEY, bean.itemNameKey);

      // displayStringOfConditionValue
      ConditionValuePattern conditionPtn =
          (ConditionValuePattern) paramMap.get(ConditionalValidator.CONDITION_PATTERN);
      String displayStringOfConditionValue = null;
      if (conditionPtn == valueOfPropertyPath) {
        Object obj =
            getFieldValue((String) paramMap.get(ConditionalValidator.CONDITION_VALUE_PROPERTY_PATH),
                cv.getLeafBean());
        if (obj instanceof Object[]) {
          List<String> strList = Arrays.asList(obj).stream().map(o -> o.toString()).toList();
          displayStringOfConditionValue =
              StringUtil.getCsvWithSpace((String[]) strList.toArray(new String[strList.size()]));

        } else {
          // String
          displayStringOfConditionValue = String.valueOf(obj);
        }

      } else if (conditionPtn == string) {
        // conditionValue is used
        String[] strs = (String[]) paramMap.get(ConditionalValidator.CONDITION_VALUE_STRING);
        displayStringOfConditionValue = StringUtil.getCsvWithSpace(strs);
      }

      // when fieldHoldingConditionValueDisplayName is not blank,
      // valuesOfConditionFieldToValidate is overrided by its value.
      String displayStringPropertyPathOfConditionValuePropertyPath = (String) paramMap
          .get(ConditionalValidator.DISPLAY_STRING_PROPERTY_PATH_OF_CONDITION_VALUE_PROPERTY_PATH);
      if (!displayStringPropertyPathOfConditionValuePropertyPath.equals("")) {
        Object obj =
            getFieldValue(displayStringPropertyPathOfConditionValuePropertyPath, cv.getLeafBean());

        String[] strs = obj instanceof String[] ? (String[]) obj : new String[] {((String) obj)};
        displayStringOfConditionValue = StringUtil.getCsvWithSpace(strs);
      }
      paramMap.put(ConditionalValidator.DISPLAY_STRING_OF_CONDITION_VALUE,
          displayStringOfConditionValue);

      // validatesWhenConditionNotSatisfied
      boolean bl = getValidatorClass().endsWith("ConditionalEmpty")
          && (Boolean) paramMap.get("notEmptyWhenConditionNotSatisfied")
          || getValidatorClass().endsWith("ConditionalNotEmpty")
              && (Boolean) paramMap.get("emptyWhenConditionNotSatisfied");
      paramMap.put(ConditionalValidator.VALIDATES_WHEN_CONDITION_NOT_SATISFIED, bl);
    }
  }

  private Object getLeafBeanFromPropertyPath(String propertyPath, Object rootBean) {
    String leafBeanItemPropertyPath =
        propertyPath.contains(".") ? propertyPath.substring(0, propertyPath.lastIndexOf("."))
            : null;

    return leafBeanItemPropertyPath == null ? rootBean
        : ReflectionUtil.getFieldValue(leafBeanItemPropertyPath, rootBean);
  }

  /**
   * Gets item dependent values.
   */
  private void getItemDependentValues(List<FieldInfoBean> beanList) {

    for (FieldInfoBean bean : beanList) {
      FieldInfoBean newBean = getItemDependentValues(bean.fullPropertyPath, leafBean.getClass());
      bean.itemNameKey = newBean.itemNameKey;
      bean.showsValue = newBean.showsValue;
    }
  }

  /**
   * Sets {@code itemNameKey} and {@code showsValue}.
   * 
   * <p>It does not consider {@code @ItemNameKeyClass}. In order to consider it,
   *     {@code getRootRecordNameConsideringItemNameKeyClass(itemPropertyPath)} 
   *     needs to be used together.</p>
   * 
   * @param fullPropertyPath itemPropertyPath
   * @param defaultItemNameKeyClass defaultItemNameKeyClass
   * @return itemNameKey
   */
  private FieldInfoBean getItemDependentValues(String fullPropertyPath, Class<?> leafBeanClass) {

    String fullPropertyPath1stPart = fullPropertyPath.contains(".")
        ? fullPropertyPath.substring(0, fullPropertyPath.indexOf("."))
        : null;
    Object firstChild = fullPropertyPath1stPart == null ? null
        : ReflectionUtil.getFieldValue(fullPropertyPath1stPart, rootBean);

    FieldInfoBean bean = new FieldInfoBean(fullPropertyPath);
    EclibItem item = null;
    boolean setsItemNameKeyClassExplicitly = false;

    // Get item if exists.
    if (rootBean instanceof EclibItemContainer) {
      // the case that rootBean is an EclibRecord
      item = ((EclibItemContainer) rootBean).getItem(fullPropertyPath);

    } else if (firstChild != null && firstChild instanceof EclibItemContainer) {
      // the case that EclibRecord is stored in form or something
      item = ((EclibItemContainer) firstChild)
          .getItem(fullPropertyPath.substring(fullPropertyPath1stPart.length() + 1));
    }

    if (item == null) {
      String itemNameKeyField = fullPropertyPath.contains(".")
          ? fullPropertyPath.substring(fullPropertyPath.lastIndexOf(".") + 1)
          : fullPropertyPath;

      String fullPropertyPathWithoutInkf = fullPropertyPath.contains(".")
          ? fullPropertyPath.substring(0, fullPropertyPath.length() - itemNameKeyField.length() - 1)
          : null;

      String itemNameKeyClass = fullPropertyPathWithoutInkf == null ? rootRecordNameForForm
          : (fullPropertyPathWithoutInkf.contains(".")
              ? fullPropertyPathWithoutInkf.substring(
                  fullPropertyPathWithoutInkf.lastIndexOf(".") + 1)
              : fullPropertyPathWithoutInkf);

      bean.itemNameKey = itemNameKeyClass + "." + itemNameKeyField;

    } else {
      bean.itemNameKey = item.getItemNameKey(rootRecordNameForForm);
      setsItemNameKeyClassExplicitly = item.setsItemNameKeyClassExplicitly();
      bean.showsValue = item.getShowsValue();
    }

    // Check existence of itemNameKeyClass to determine rootRecordName.
    Optional<ItemNameKeyClass> opItemNameKeyClassAn =
        searchAnnotationPlacedAtClass(leafBeanClass, ItemNameKeyClass.class);
    String itemNameKeyClass =
        opItemNameKeyClassAn.isPresent() ? opItemNameKeyClassAn.get().value() : null;
    if (itemNameKeyClass != null && !setsItemNameKeyClassExplicitly) {
      bean.itemNameKey =
          itemNameKeyClass + bean.itemNameKey.substring(bean.itemNameKey.indexOf("."));
    }

    return bean;
  }

  public Object getRootBean() {
    return rootBean;
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
  public String getOriginalMessage() {
    return originalMessage;
  }

  public String getValidatorClass() {
    return validatorClass;
  }

  public String getInvalidValue() {
    return (cv == null || cv.getInvalidValue() == null) ? "null" : cv.getInvalidValue().toString();
  }

  public @Nonnull String getMessageId() {
    return messageTemplate;
  }

  public String getRootRecordNameForForm() {
    return rootRecordNameForForm;
  }

  public List<FieldInfoBean> getFieldInfoBeanList() {
    return fieldInfoBeanList;
  }

  public FieldInfoBean[] getFieldInfoBeans() {
    return fieldInfoBeanList.toArray(new FieldInfoBean[fieldInfoBeanList.size()]);
  }

  @Nonnull
  public Map<String, Object> getParamMap() {
    return paramMap;
  }

  /**
   * Stores field-unit parameters.
   */
  public static class FieldInfoBean {

    /** 
     * The key of the bean.
     * It's a propertyPath which designate from rootBean to the violation-occurring field.
     */
    public String fullPropertyPath;

    /** 
     * When a validator added to a class detects a violation, 
     * it can be a combination of values between multiple items. 
     * In that case you want to set error multiple itemPropertyPaths for those items.
     */
    public String itemPropertyPathForForm;

    public String itemNameKey;

    public boolean showsValue = true;

    /**
     * Constructs a new instance.
     */
    public FieldInfoBean(String fullPropertyPath) {
      this.fullPropertyPath = fullPropertyPath;
    }
  }
}
