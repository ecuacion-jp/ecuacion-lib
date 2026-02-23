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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.jakartavalidation.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.jakartavalidation.annotation.PlacedAtClass;
import jp.ecuacion.lib.core.util.PropertyFileUtil.Arg;
import jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum;
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
public class ConstraintViolationBean<T> extends ReflectionUtil {
  private ConstraintViolation<T> cv;

  // properties in ConstraintViolation

  private Object rootBean;
  private Object leafBean;
  private String validatorClass;
  private String messageTemplate;
  private String originalMessage;

  // values needed for all the patterns

  private List<FieldInfoBean> fieldInfoBeanList;

  private Boolean isMessageWithItemName;
  private Arg messagePrefix;
  private Arg messagePostfix;

  // values needed for validations for form

  private String rootRecordNameForForm;

  @Nonnull
  private Map<String, Object> paramMap = new HashMap<>();
  private Set<LocalizedMessageParameter> messageParameterSet = new HashSet<>();

  private void putArgsToFields(Object rootBean, Object leafBean, String validatorClass,
      String originalMessage, String messageTemplate, String rootRecordNameForForm,
      List<FieldInfoBean> beanList) {
    this.rootBean = rootBean;
    this.leafBean = leafBean;
    this.validatorClass = validatorClass;
    this.originalMessage = originalMessage;
    this.messageTemplate = messageTemplate;
    this.rootRecordNameForForm = rootRecordNameForForm;
    this.fieldInfoBeanList = beanList;

    getItemDependentValues(beanList, rootBean, leafBean, rootRecordNameForForm);
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
  public ConstraintViolationBean(T rootBean, String message, String validatorClass,
      String rootRecordNameForForm, String itemPropertyPath) {

    List<FieldInfoBean> beanList = new ArrayList<>();
    beanList.add(new FieldInfoBean(rootRecordNameForForm + "." + itemPropertyPath));
    beanList.get(0).itemPropertyPathForForm = itemPropertyPath;

    putArgsToFields(rootBean, getLeafBean(rootBean, rootRecordNameForForm + "." + itemPropertyPath),
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
  public ConstraintViolationBean(ConstraintViolation<T> cv) {
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

    // invalidValue
    if (!beanList.get(0).showsValue) {
      String key = "jp.ecuacion.lib.validation.constraints.displayStringForHiddenValue";
      messageParameterSet.add(new LocalizedMessageParameter("invalidValue",
          new PropertyFileUtilFileKindEnum[] {PropertyFileUtilFileKindEnum.MESSAGES}, key));
      // argMap.put(, PropertyFileUtil.getMessage(locale, key));
    }

    // Comparison validators
    if (paramMap.containsKey("basisPropertyPath")) {
      String bpp = (String) paramMap.get("basisPropertyPath");
      String itemNameKey = getItemDependentValues(bpp, leafBean.getClass(), rootBean,
          rootRecordNameForForm).itemNameKey;
      messageParameterSet.add(new LocalizedMessageParameter("basisPropertyPathItemName",
          new PropertyFileUtilFileKindEnum[] {PropertyFileUtilFileKindEnum.ITEM_NAMES},
          itemNameKey));
    }

    // Obtain and put additional parameters for its violation message to messageParameterSet.
    String className = getValidatorClass() + "MessageParameterCreator";
    if (classExists(className)) {
      messageParameterSet.addAll(((ValidatorMessageParameterCreator) newInstance(className))
          .create(cv, paramMap, rootRecordNameForForm));
    }
  }

  /** 
   * Outputs a string for logs. 
   * 
   * @return String
   */
  @Override
  public @Nonnull String toString() {
    return "message:" + getOriginalMessage() + "\n" + "annotation:" + getValidatorClass()
        + "\n" + "rootClassName:" + getRootBean().getClass().getName() + "\n"
        + "leafClassName:" + getLeafBean().getClass().getName() + "\n" + "propertyPath:"
        + StringUtil.getCsv(
            getFieldInfoBeanList().stream().map(b -> b.itemPropertyPathForForm).toList())
        + "\n" + "invalidValue:" + getInvalidValue();
  }

  /**
   * Gets item dependent values.
   */
  public static void getItemDependentValues(List<FieldInfoBean> beanList, Object rootBean,
      Object leafBean, String rootRecordNameForForm) {

    for (FieldInfoBean bean : beanList) {
      FieldInfoBean newBean = getItemDependentValues(bean.fullPropertyPath, leafBean.getClass(),
          rootBean, rootRecordNameForForm);
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
   * @return itemNameKey
   */
  public static FieldInfoBean getItemDependentValues(String fullPropertyPath,
      Class<?> leafBeanClass, Object rootBean, String rootRecordNameForForm) {

    String fullPropertyPath1stPart = fullPropertyPath.contains(".")
        ? fullPropertyPath.substring(0, fullPropertyPath.indexOf("."))
        : null;
    Object firstChild = fullPropertyPath1stPart == null ? null
        : ReflectionUtil.getValue(rootBean, fullPropertyPath1stPart);

    FieldInfoBean bean = new FieldInfoBean(fullPropertyPath);
    Item item = null;
    boolean setsItemNameKeyClassExplicitly = false;

    // Get item if exists.
    if (rootBean instanceof ItemContainer) {
      // the case that rootBean is an EclibRecord
      item = ((ItemContainer) rootBean).getItem(fullPropertyPath);

    } else if (firstChild != null && firstChild instanceof ItemContainer) {
      // the case that EclibRecord is stored in form or something
      item = ((ItemContainer) firstChild)
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

  public Boolean isMessageWithItemName() {
    return isMessageWithItemName;
  }

  public void setMessageWithItemName(Boolean isMessageWithItemName) {
    this.isMessageWithItemName = isMessageWithItemName;
  }

  public Arg getMessagePrefix() {
    return messagePrefix;
  }
  
  public void setMessagePrefix(Arg messagePrefix) {
    this.messagePrefix = messagePrefix;
  }

  public Arg getMessagePostfix() {
    return messagePostfix;
  }

  public void setMessagePostfix(Arg messagePostfix) {
    this.messagePostfix = messagePostfix;
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

  public Set<LocalizedMessageParameter> getMessageParameterSet() {
    return messageParameterSet;
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

  /**
   * Stores parameters of information on a message for ValidationAppException.
   * 
   * <p>It is resolved to message value at ExceptionHandler
   *     Because there is a locale there.</p>
   *     
   * <p>When you designate fileKinds = new PropertyFileUtilFileKindEnum[] {} (length is zero),
   *     propertyPathKey is set as the value.</p>
   */
  public static record LocalizedMessageParameter(String parameterKey,
      PropertyFileUtilFileKindEnum[] fileKinds, String propertyFileKey, Arg... args) {
  }
}
