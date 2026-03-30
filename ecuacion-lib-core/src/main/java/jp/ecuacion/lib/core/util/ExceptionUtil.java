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
package jp.ecuacion.lib.core.util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.exception.checked.ConstraintViolationExceptionWithParameters;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.ValidationAppException;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean.FieldInfoBean;
import jp.ecuacion.lib.core.jakartavalidation.bean.ValidatorMessageParameterCreator;
import jp.ecuacion.lib.core.util.PropertyFileUtil.Arg;
import jp.ecuacion.lib.core.util.ValidationUtil.MessageParameters;
import jp.ecuacion.lib.core.util.enums.PropertyFileUtilFileKindEnum;

/**
 * Provides available utilities for Exceptions including AppExceptions.
 */
public class ExceptionUtil {

  public static final String SYSTEM_ERROR_OCCURED_SIGN =
      "=============== system error occurred ===============";

  /**
   * Prevents other classes from instantiating it.
   */
  private ExceptionUtil() {}

  /**
   * Returns Exception message list.
   */
  @Nonnull
  public static <T> List<String> getMessageList(
      @RequireNonnull Set<ConstraintViolation<T>> constraintViolations) {
    return getMessageList(constraintViolations, null, null, null);
  }

  /**
   * Returns Exception message list.
   */
  @Nonnull
  public static <T> List<String> getMessageList(
      @RequireNonnull Set<ConstraintViolation<T>> constraintViolations, @Nullable Locale locale) {
    return getMessageList(constraintViolations, locale, null, null);
  }

  /**
   * Returns Exception message list.
   * 
   * <p>Even if {@code isValidationMessagesWithItemNamesAsDefault} is non-null value, 
   *     messageParameters.isMessageWithItemName value is adopted 
   *     when messageParameters.isMessageWithItemName is not null 
   *     (= explicitly specified in ValidationUtil.validate).<br>
   *     {@code isValidationMessagesWithItemNamesAsDefault} is assumed to a system default value
   *     so messageParameters.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   */
  @Nonnull
  public static <T> List<String> getMessageList(
      @RequireNonnull Set<ConstraintViolation<T>> constraintViolationSet,
      @Nullable Boolean isValidationMessagesWithItemNamesAsDefault) {
    return getMessageList(constraintViolationSet, null, isValidationMessagesWithItemNamesAsDefault);
  }

  /**
   * Returns Exception message list.
   * 
   * <p>Even if {@code isValidationMessagesWithItemNamesAsDefault} is non-null value, 
   *     messageParameters.isMessageWithItemName value is adopted 
   *     when messageParameters.isMessageWithItemName is not null 
   *     (= explicitly specified in ValidationUtil.validate).<br>
   *     {@code isValidationMessagesWithItemNamesAsDefault} is assumed to a system default value
   *     so messageParameters.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   */
  @Nonnull
  public static <T> List<String> getMessageList(
      @RequireNonnull Set<ConstraintViolation<T>> constraintViolationSet, @Nullable Locale locale,
      @Nullable Boolean isValidationMessagesWithItemNamesAsDefault) {

    return getMessageList(constraintViolationSet, locale,
        isValidationMessagesWithItemNamesAsDefault, ValidationUtil.messageParameters());
  }

  /**
   * Returns Exception message list.
   * 
   * <p>Even if {@code isValidationMessagesWithItemNamesAsDefault} is non-null value, 
   *     messageParameters.isMessageWithItemName value is adopted 
   *     when messageParameters.isMessageWithItemName is not null 
   *     (= explicitly specified in ValidationUtil.validate).<br>
   *     {@code isValidationMessagesWithItemNamesAsDefault} is assumed to a system default value
   *     so messageParameters.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   */
  @Nonnull
  public static <T> List<String> getMessageList(
      @RequireNonnull Set<ConstraintViolation<T>> constraintViolations,
      @Nullable Boolean isValidationMessagesWithItemNamesAsDefault,
      @Nullable MessageParameters messageParameters) {

    return getMessageList(constraintViolations, null, isValidationMessagesWithItemNamesAsDefault,
        messageParameters);
  }

  /**
   * Returns Exception message list.
   * 
   * <p>Even if {@code isValidationMessagesWithItemNamesAsDefault} is non-null value, 
   *     messageParameters.isMessageWithItemName value is adopted 
   *     when messageParameters.isMessageWithItemName is not null 
   *     (= explicitly specified in ValidationUtil.validate).<br>
   *     {@code isValidationMessagesWithItemNamesAsDefault} is assumed to a system default value
   *     so messageParameters.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   */
  @Nonnull
  public static <T> List<String> getMessageList(
      @RequireNonnull Set<ConstraintViolation<T>> constraintViolations, @Nullable Locale locale,
      @Nullable Boolean isValidationMessagesWithItemNamesAsDefault,
      @Nullable MessageParameters messageParameters) {

    if (constraintViolations == null || constraintViolations.size() == 0) {
      throw new EclibRuntimeException("Size of ConstraintViolation is zero.");
    }

    Exception ex =
        new ConstraintViolationExceptionWithParameters(constraintViolations, messageParameters);

    return getMessageList(ex, locale, isValidationMessagesWithItemNamesAsDefault);
  }

  /**
   * Returns exception message for 1 exception.
   * 
   * <p>This method covers all the exceptions including Java standard exceptions, 
   * ConstraintViolationException used in Jakarta Validation
   * and AppExceptions defined in this library.</p>
   * 
   * <p>One exception normally has one message, 
   * but one ConstraintViolationException can have multiple messages 
   * so the return type is not a {@code String}, but a {@code List<String>}.</p>
   *
   * @param throwable throwable
   * @return a list of messages
   */
  @Nonnull
  public static List<String> getMessageList(@RequireNonnull Throwable throwable) {
    return getMessageList(throwable, null, null);
  }

  /**
   * Returns exception message for 1 exception.
   * 
   * <p>This method covers all the exceptions including Java standard exceptions, 
   * ConstraintViolationException used in Jakarta Validation
   * and AppExceptions defined in this library.</p>
   * 
   * <p>One exception normally has one message, 
   * but one ConstraintViolationException can have multiple messages 
   * so the return type is not a {@code String}, but a {@code List<String>}.</p>
   *
   * @param throwable throwable
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @return a list of messages
   */
  @Nonnull
  public static List<String> getMessageList(@RequireNonnull Throwable throwable,
      @Nullable Locale locale) {
    return getMessageList(throwable, locale, null);
  }

  /**
   * Returns exception message for 1 exception.
   * 
   * <p>This method covers all the exceptions including Java standard exceptions, 
   * ConstraintViolationException used in Jakarta Validation
   * and AppExceptions defined in this library.</p>
   * 
   * <p>One exception normally has one message, 
   * but one ConstraintViolationException can have multiple messages 
   * so the return type is not a {@code String}, but a {@code List<String>}.</p>
   * 
   * <p>Even if {@code isValidationMessagesWithItemNames} is non-null value, 
   *     ConstraintViolationBean.isMessageWithItemName value is adopted 
   *     when ConstraintViolationBean.isMessageWithItemName is not null 
   *     (= explicitly specified in ValidationUtil.validate).<br>
   *     {@code isValidationMessagesWithItemNames} is assumed to a system default value
   *     so ConstraintViolationBean.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   *
   * @param throwable throwable
   * @param isValidationMessagesWithItemNamesAsDefault 
   *     isValidationMessagesWithItemNames, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @return a list of messages
   */
  @Nonnull
  public static List<String> getMessageList(@RequireNonnull Throwable throwable,
      @Nullable Boolean isValidationMessagesWithItemNamesAsDefault) {
    return getMessageList(throwable, null, isValidationMessagesWithItemNamesAsDefault);
  }

  /**
   * Returns exception message for 1 exception.
   * 
   * <p>This method covers all the exceptions including Java standard exceptions, 
   * ConstraintViolationException used in Jakarta Validation
   * and AppExceptions defined in this library.</p>
   * 
   * <p>One exception normally has one message, 
   * but one ConstraintViolationException can have multiple messages 
   * so the return type is not a {@code String}, but a {@code List<String>}.</p>
   * 
   * <p>Even if {@code isValidationMessagesWithItemNames} is non-null value, 
   *     ConstraintViolationBean.isMessageWithItemName value is adopted 
   *     when ConstraintViolationBean.isMessageWithItemName is not null 
   *     (= explicitly specified in ValidationUtil.validate).<br>
   *     {@code isValidationMessagesWithItemNames} is assumed to a system default value
   *     so ConstraintViolationBean.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   *
   * @param throwable throwable
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param isValidationMessagesWithItemNamesAsDefault true 
   *     when itemName needed for ValidationAppException messages.
   *     
   * @return a list of messages
   */
  @Nonnull
  public static List<String> getMessageList(@RequireNonnull Throwable throwable,
      @Nullable Locale locale, @Nullable Boolean isValidationMessagesWithItemNamesAsDefault) {
    ObjectsUtil.requireNonNull(throwable);
    locale = locale == null ? Locale.getDefault() : locale;
    isValidationMessagesWithItemNamesAsDefault =
        isValidationMessagesWithItemNamesAsDefault == null ? Boolean.FALSE
            : isValidationMessagesWithItemNamesAsDefault;

    List<Throwable> exList = new ArrayList<>();
    List<String> rtnList = new ArrayList<>();

    // jakarta.validation.ConstraintViolationException can be thrown from unassumed locations.
    // In that case it's not transformed to AppBeanValidationException,
    // So the transformation procedure is added here.
    if (throwable instanceof ConstraintViolationException) {

      ConstraintViolationException cve = (ConstraintViolationException) throwable;

      MessageParameters params = cve instanceof ConstraintViolationExceptionWithParameters
          ? ((ConstraintViolationExceptionWithParameters) cve).getMessageParameters()
          : null;

      for (ConstraintViolation<?> cv : cve.getConstraintViolations()) {
        exList.add(new ValidationAppException(cv, params));
      }

      // } else if (throwable instanceof ConstraintViolationBeanException) {
      // ConstraintViolationBeanException cve = (ConstraintViolationBeanException) throwable;
      // for (ConstraintViolationBean<?> cv : cve.getConstraintViolationBeans()) {
      // exList.add(new ValidationAppException(cv));
      // }

    } else {
      exList.add(throwable);
    }

    for (Throwable th : exList) {
      if (th instanceof MultipleAppException) {
        // Continue because this exception doesn't have a message.
        continue;

      } else if (th instanceof BizLogicAppException) {
        BizLogicAppException ex = (BizLogicAppException) th;
        String message = isValidationMessagesWithItemNamesAsDefault
            ? PropertyFileUtil.getMessageWithItemName(locale, ex.getMessageId(),
                ex.getMessageArgs())
            : PropertyFileUtil.getMessage(locale, ex.getMessageId(), ex.getMessageArgs());
        rtnList.add(message);

      } else if (th instanceof ValidationAppException) {
        ValidationAppException ex = (ValidationAppException) th;

        String message = null;
        try {
          ConstraintViolationBean<?> bean = ex.getConstraintViolationBean();
          final Map<String, Object> map = new HashMap<>(bean.getEmbeddedParamMap());

          // Get localize-needed message embedded parameters
          Set<LocalizedEmbeddedParameter> embeddedParameterSet = getMessageParameterSet(bean);

          // Add parameters from messageParameterSet.
          for (LocalizedEmbeddedParameter paramBean : embeddedParameterSet) {

            // Put propertyFileKey as value when paramBean.fileKinds().length == 0.
            if (paramBean.fileKinds() == null || paramBean.fileKinds().length == 0) {
              map.put(paramBean.parameterKey(), paramBean.propertyFileKey());
              continue;
            }

            String value = "";
            for (PropertyFileUtilFileKindEnum fileKind : paramBean.fileKinds()) {
              // Put return value of PropertyFileUtil.get() even when key does not exist.
              value = PropertyFileUtil.get(fileKind.toString(), locale, paramBean.propertyFileKey(),
                  paramBean.args());

              if (PropertyFileUtil.has(fileKind.toString(), paramBean.propertyFileKey())) {
                break;
              }
            }

            map.put(paramBean.parameterKey(), value);
          }

          MessageParameters messageParameters = ex.getMessageParameters();
          messageParameters = messageParameters == null ? new ValidationUtil.MessageParameters()
              : messageParameters;

          // If bean.isMessageWithItemName() is not null (= explicitly specified), it's prioritized
          // because it is specified for each validation,
          // and isValidationMessagesWithItemNames is assumed to be used as system default value.
          Boolean isMessageWithItemName = messageParameters.isMessageWithItemName() != null
              ? messageParameters.isMessageWithItemName()
              : isValidationMessagesWithItemNamesAsDefault;

          String messageKey = bean.getMessageTemplate().replace("{", "").replace("}", "");
          boolean isMessageDefined =
              isMessageWithItemName ? PropertyFileUtil.hasValidationMessageWithItemName(messageKey)
                  : PropertyFileUtil.hasValidationMessage(messageKey);
          if (isMessageDefined) {
            message = isMessageWithItemName
                ? PropertyFileUtil.getValidationMessageWithItemName(locale, messageKey, map)
                : PropertyFileUtil.getValidationMessage(locale, messageKey, map);

          } else {
            message = bean.getMessageTemplate();
          }

          // Replace {0} to itemName.
          if (message.contains("{0}")) {
            message = MessageFormat.format(message,
                MessageUtil.getItemNames(locale, bean.getFieldInfoBeanList(),
                    messageParameters.showsItemNamePath(), bean.getRootBean()));
          }

          // add prefix and postfix messages.
          if (messageParameters.getMessagePrefix() != null) {
            message =
                PropertyFileUtil.getStringFromArg(locale, messageParameters.getMessagePrefix())
                    + message;
          }

          if (messageParameters.getMessagePostfix() != null) {
            message = message
                + PropertyFileUtil.getStringFromArg(locale, messageParameters.getMessagePostfix());
          }

        } catch (MissingResourceException mre) {
          message = ex.getMessage();
        }

        rtnList.add(message);

      } else {
        rtnList.add(th.getMessage());
      }
    }

    return rtnList;

  }

  private static Set<LocalizedEmbeddedParameter> getMessageParameterSet(
      ConstraintViolationBean<?> cvBean) {
    Set<LocalizedEmbeddedParameter> rtnSet = new HashSet<>();
    List<FieldInfoBean> beanList = cvBean.getFieldInfoBeanList();

    // invalidValue
    if (!beanList.get(0).showsValue()) {
      String key = "jp.ecuacion.lib.core.jakartavalidation.validator.displayStringForHiddenValue";
      rtnSet.add(new LocalizedEmbeddedParameter("invalidValue",
          new PropertyFileUtilFileKindEnum[] {PropertyFileUtilFileKindEnum.MESSAGES}, key));
      // argMap.put(, PropertyFileUtil.getMessage(locale, key));
    }

    // Comparison validators
    if (cvBean.getEmbeddedParamMap().containsKey("baselinePropertyPath")) {
      String bpp = (String) cvBean.getEmbeddedParamMap().get("baselinePropertyPath");
      String itemNameKey =
          MessageUtil.getFieldInfoBean(bpp, cvBean.getRootBean(), cvBean.getLeafBean().getClass())
              .itemNameKey();
      rtnSet.add(new LocalizedEmbeddedParameter("baselinePropertyPathItemName",
          new PropertyFileUtilFileKindEnum[] {PropertyFileUtilFileKindEnum.ITEM_NAMES},
          itemNameKey));
    }

    // Obtain and put additional parameters for its violation message to messageParameterSet.
    String className = cvBean.getValidatorClass() + "MessageParameterCreator";
    if (ReflectionUtil.classExists(className)) {
      rtnSet.addAll(((ValidatorMessageParameterCreator) ReflectionUtil.newInstance(className))
          .create(cvBean, cvBean.getEmbeddedParamMap()));
    }

    return rtnSet;
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
  public static record LocalizedEmbeddedParameter(String parameterKey,
      PropertyFileUtilFileKindEnum[] fileKinds, String propertyFileKey, Arg... args) {
  }
}
