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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.exception.checked.ConstraintViolationExceptionWithParameters;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.ValidationAppException;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ValidatorMessageParameterCreator;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.ValidationUtil.MessageParameters;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
  public static <T> List<String> getMessageList(Set<ConstraintViolation<T>> constraintViolations) {
    return getMessageList(constraintViolations, null, false, ValidationUtil.messageParameters());
  }

  /**
   * Returns Exception message list.
   */
  public static <T> List<String> getMessageList(Set<ConstraintViolation<T>> constraintViolations,
      @Nullable Locale locale) {
    return getMessageList(constraintViolations, locale, false, ValidationUtil.messageParameters());
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
  public static <T> List<String> getMessageList(Set<ConstraintViolation<T>> constraintViolationSet,
      boolean isMessagesWithItemNamesAsDefault) {
    return getMessageList(constraintViolationSet, null, isMessagesWithItemNamesAsDefault);
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
  public static <T> List<String> getMessageList(Set<ConstraintViolation<T>> constraintViolationSet,
      @Nullable Locale locale, boolean isMessagesWithItemNamesAsDefault) {

    return getMessageList(constraintViolationSet, locale, isMessagesWithItemNamesAsDefault,
        ValidationUtil.messageParameters());
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
  public static <T> List<String> getMessageList(Set<ConstraintViolation<T>> constraintViolations,
      boolean isMessagesWithItemNamesAsDefault, MessageParameters messageParameters) {

    return getMessageList(constraintViolations, null, isMessagesWithItemNamesAsDefault,
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
  public static <T> List<String> getMessageList(Set<ConstraintViolation<T>> constraintViolations,
      @Nullable Locale locale, boolean isMessagesWithItemNamesAsDefault,
      MessageParameters messageParameters) {

    if (constraintViolations.size() == 0) {
      throw new RuntimeException("Size of ConstraintViolation is zero.");
    }

    Locale nonNullLocale = locale == null ? Locale.getDefault() : locale;
    List<String> result = new ArrayList<>();
    for (ConstraintViolation<T> cv : constraintViolations) {
      result
          .add(buildMessageFromConstraintViolation(nonNullLocale, isMessagesWithItemNamesAsDefault,
              ConstraintViolationBean.createConstraintViolationBean(cv), messageParameters));
    }
    return result;
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
  public static List<String> getMessageList(Throwable throwable) {
    return getMessageList(throwable, null, false);
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
  public static List<String> getMessageList(Throwable throwable, @Nullable Locale locale) {
    return getMessageList(throwable, locale, false);
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
   * @param isMessagesWithItemNamesAsDefault 
   *     isValidationMessagesWithItemNames, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @return a list of messages
   */
  public static List<String> getMessageList(Throwable throwable,
      boolean isMessagesWithItemNamesAsDefault) {
    return getMessageList(throwable, null, isMessagesWithItemNamesAsDefault);
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
   * @param isMessagesWithItemNamesAsDefault true 
   *     when itemName needed for ValidationAppException messages.
   *     
   * @return a list of messages
   */
  @SuppressWarnings({"removal"})
  public static List<String> getMessageList(Throwable throwable, @Nullable Locale locale,
      boolean isMessagesWithItemNamesAsDefault) {
    ObjectsUtil.requireNonNull(throwable);
    Locale nonNullLocale = locale == null ? Locale.getDefault() : locale;

    // Handle ViolationException first.
    if (throwable instanceof ViolationException) {
      return getMessageList(((ViolationException) throwable).getViolations(), locale,
          isMessagesWithItemNamesAsDefault);
    }

    List<Throwable> exList = new ArrayList<>();
    List<String> rtnList = new ArrayList<>();

    // jakarta.validation.ConstraintViolationException can be thrown from unassumed locations.
    // In that case messages are built directly from ConstraintViolation.
    if (throwable instanceof ConstraintViolationException) {
      ConstraintViolationException cve = (ConstraintViolationException) throwable;

      MessageParameters params = cve instanceof ConstraintViolationExceptionWithParameters
          ? ((ConstraintViolationExceptionWithParameters) cve).getMessageParameters()
          : ValidationUtil.messageParameters();

      for (ConstraintViolation<?> cv : cve.getConstraintViolations()) {
        rtnList.add(
            buildMessageFromConstraintViolation(nonNullLocale, isMessagesWithItemNamesAsDefault,
                ConstraintViolationBean.createConstraintViolationBean(cv), params));
      }
      return rtnList;

    } else {
      exList.add(throwable);
    }

    for (Throwable th : exList) {
      if (th instanceof MultipleAppException) {
        // Legacy: remove this branch when MultipleAppException is retired.
        continue;

      } else if (th instanceof BizLogicAppException) {
        // Legacy: remove this branch when BizLogicAppException is retired.
        rtnList.add(getMessageFromBusinessViolation(nonNullLocale, isMessagesWithItemNamesAsDefault,
            ((BizLogicAppException) th).getBusinessViolation(),
            ValidationUtil.messageParameters()));

      } else if (th instanceof ValidationAppException) {
        // Legacy: remove this branch when ValidationAppException is retired.
        ValidationAppException ex = (ValidationAppException) th;
        rtnList.add(
            buildMessageFromConstraintViolation(nonNullLocale, isMessagesWithItemNamesAsDefault,
                ex.getConstraintViolationBean(), ex.getMessageParameters()));

      } else {
        rtnList.add(th.getMessage());
      }
    }

    return rtnList;
  }

  /**
   * Returns message list from {@link Violations}.
   *
   * @param violations violations
   * @return a list of messages
   */
  public static List<String> getMessageList(Violations violations) {
    return getMessageList(violations, null, false);
  }

  /**
   * Returns message list from {@link Violations}.
   *
   * @param violations violations
   * @param locale locale, may be {@code null} which is treated as {@code Locale.getDefault()}.
   * @return a list of messages
   */
  public static List<String> getMessageList(Violations violations, @Nullable Locale locale) {
    return getMessageList(violations, locale, false);
  }

  /**
   * Returns message list from {@link Violations}.
   *
   * @param violations violations
   * @param isMessagesWithItemNamesAsDefault true when item names are shown in messages by default.
   * @return a list of messages
   */
  public static List<String> getMessageList(Violations violations,
      boolean isMessagesWithItemNamesAsDefault) {
    return getMessageList(violations, null, isMessagesWithItemNamesAsDefault);
  }

  /**
   * Returns message list from {@link Violations}.
   *
   * <p>{@link MessageParameters} stored in {@code violations} is used
   *     for {@link ConstraintViolation} message resolution.</p>
   *
   * @param violations violations
   * @param locale locale, may be {@code null} which is treated as {@code Locale.getDefault()}.
   * @param isMessagesWithItemNamesAsDefault true when item names are shown in messages by default.
   * @return a list of messages
   */
  public static List<String> getMessageList(Violations violations, @Nullable Locale locale,
      boolean isMessagesWithItemNamesAsDefault) {
    List<String> result = new ArrayList<>();
    Locale nonNullLocale = locale == null ? Locale.getDefault() : locale;

    for (ConstraintViolation<?> cv : violations.getConstraintViolations()) {
      result
          .add(buildMessageFromConstraintViolation(nonNullLocale, isMessagesWithItemNamesAsDefault,
              ConstraintViolationBean.createConstraintViolationBean(cv),
              violations.getMessageParameters()));
    }

    for (BusinessViolation bv : violations.getBusinessViolations()) {
      result.add(getMessageFromBusinessViolation(nonNullLocale, isMessagesWithItemNamesAsDefault,
          bv, violations.getMessageParameters()));
    }

    return result;
  }

  /**
   * Returns message list from {@link ViolationException}.
   *
   * @param ex violation exception
   * @return a list of messages
   */
  public static List<String> getMessageList(ViolationException ex) {
    return getMessageList(ex.getViolations());
  }

  /**
   * Returns message list from {@link ViolationException}.
   *
   * @param ex violation exception
   * @param locale locale, may be {@code null} which is treated as {@code Locale.getDefault()}.
   * @return a list of messages
   */
  public static List<String> getMessageList(ViolationException ex, @Nullable Locale locale) {
    return getMessageList(ex.getViolations(), locale);
  }

  /**
   * Returns message list from {@link ViolationException}.
   *
   * @param ex violation exception
   * @param isMessagesWithItemNamesAsDefault true when item names are shown in messages by default.
   * @return a list of messages
   */
  public static List<String> getMessageList(ViolationException ex,
      boolean isMessagesWithItemNamesAsDefault) {
    return getMessageList(ex.getViolations(), isMessagesWithItemNamesAsDefault);
  }

  /**
   * Returns message list from {@link ViolationException}.
   *
   * @param ex violation exception
   * @param locale locale, may be {@code null} which is treated as {@code Locale.getDefault()}.
   * @param isMessagesWithItemNamesAsDefault true when item names are shown in messages by default.
   * @return a list of messages
   */
  public static List<String> getMessageList(ViolationException ex, @Nullable Locale locale,
      boolean isMessagesWithItemNamesAsDefault) {
    return getMessageList(ex.getViolations(), locale, isMessagesWithItemNamesAsDefault);
  }

  private static String buildMessageFromConstraintViolation(Locale locale,
      boolean isMessagesWithItemNamesAsDefault, ConstraintViolationBean<?> bean,
      MessageParameters messageParameters) {
    String message = null;
    try {
      final Map<String, Object> map = new HashMap<>(bean.getEmbeddedParamMap());

      // Get localize-needed message embedded parameters
      Set<LocalizedEmbeddedParameter> embeddedParameterSet = getMessageParameterSet(bean);

      // Add parameters from messageParameterSet.
      putMesageParameterSetToParamMap(locale, map, embeddedParameterSet,
          messageParameters.showsItemNamePath());

      // If messageParameters.isMessageWithItemName() is not null (= explicitly specified),
      // it's prioritized over isMessagesWithItemNamesAsDefault.
      Boolean isMessageWithItemName = messageParameters.isMessageWithItemName() != null
          ? Objects.requireNonNull(messageParameters.isMessageWithItemName())
          : isMessagesWithItemNamesAsDefault;

      String messageKey = bean.getMessageTemplate().replace("{", "").replace("}", "");
      boolean isMessageDefined = isMessageWithItemName
          ? PropertiesFileUtil.hasValidationMessageWithItemName(locale, messageKey)
          : PropertiesFileUtil.hasValidationMessage(locale, messageKey);
      if (isMessageDefined) {
        message = isMessageWithItemName
            ? PropertiesFileUtil.getValidationMessageWithItemName(locale, messageKey, map)
            : PropertiesFileUtil.getValidationMessage(locale, messageKey, map);
      } else {
        message = bean.getMessageTemplate();
      }

      // Replace {0} to itemName.
      if (message.contains("{0}")) {
        message = MessageFormat.format(message, MessageUtil.getItemNames(locale, bean.getItemList(),
            messageParameters.showsItemNamePath(), bean.getRootBean()));
      }

      // add prefix and postfix messages.
      if (messageParameters.getMessagePrefix() != null) {
        message = PropertiesFileUtil.getStringFromArg(locale,
            Objects.requireNonNull(messageParameters.getMessagePrefix())) + message;
      }

      if (messageParameters.getMessagePostfix() != null) {
        message = message + PropertiesFileUtil.getStringFromArg(locale,
            Objects.requireNonNull(messageParameters.getMessagePostfix()));
      }

    } catch (MissingResourceException ignored) {
      message = bean.getMessage();
    }
    return message;
  }

  private static Set<LocalizedEmbeddedParameter> getMessageParameterSet(
      ConstraintViolationBean<?> cvBean) {
    Set<LocalizedEmbeddedParameter> rtnSet = new HashSet<>();
    List<Item> beanList = cvBean.getItemList();

    // invalidValue
    if (!beanList.get(0).getShowsValue()) {
      String key = "jp.ecuacion.lib.core.jakartavalidation.validator.displayStringForHiddenValue";
      rtnSet.add(new LocalizedEmbeddedParameter("invalidValue",
          new PropertiesFileUtilFileKindEnum[] {PropertiesFileUtilFileKindEnum.MESSAGES}, key));
      // argMap.put(, PropertiesFileUtil.getMessage(locale, key));
    }

    // Obtain and put additional parameters for its violation message to messageParameterSet.
    String className = cvBean.getValidatorClass() + "MessageParameterCreator";
    if (ReflectionUtil.classExists(className)) {
      rtnSet.addAll(((ValidatorMessageParameterCreator) ReflectionUtil.newInstance(className))
          .create(cvBean, cvBean.getEmbeddedParamMap()));
    }

    return rtnSet;
  }

  private static String getMessageFromBusinessViolation(Locale locale,
      boolean isMessagesWithItemNamesAsDefault, BusinessViolation violation,
      MessageParameters messageParameters) {
    // return isMessagesWithItemNamesAsDefault
    // ? PropertiesFileUtil.getMessageWithItemName(locale, violation.getMessageId(),
    // violation.getMessageArgs())
    // : PropertiesFileUtil.getMessage(locale, violation.getMessageId(),
    // violation.getMessageArgs());

    // If messageParameters.isMessageWithItemName() is not null (= explicitly specified),
    // it's prioritized over isMessagesWithItemNamesAsDefault.
    Boolean isMessageWithItemName = messageParameters.isMessageWithItemName() != null
        ? Objects.requireNonNull(messageParameters.isMessageWithItemName())
        : isMessagesWithItemNamesAsDefault;

    String message = null;
    String messageKey = violation.getMessageId();
    if (isMessageWithItemName) {
      message = PropertiesFileUtil.hasMessageWithItemName(messageKey)
          ? PropertiesFileUtil.getMessageWithItemName(locale, messageKey,
              violation.getMessageArgs())
          : PropertiesFileUtil.getValidationMessageWithItemName(locale, messageKey,
              new HashMap<>());
    } else {
      message = PropertiesFileUtil.hasMessage(messageKey)
          ? PropertiesFileUtil.getMessage(locale, messageKey, violation.getMessageArgs())
          : PropertiesFileUtil.getValidationMessage(locale, messageKey, new HashMap<>());
    }

    // Replace {0} to itemName.
    if (message.contains("{0}") && violation.getRootBean() != null) {
      Object rootBean = Objects.requireNonNull(violation.getRootBean());
      List<@NonNull Item> itemList = Arrays.asList(violation.getItemPropertyPaths()).stream()
          .map(path -> MessageUtil.getItem(path, rootBean, rootBean)).toList();

      message = MessageFormat.format(message,
          MessageUtil.getItemNames(locale, itemList, false, rootBean));
    }

    // add prefix and postfix messages.
    if (messageParameters.getMessagePrefix() != null) {
      message = PropertiesFileUtil.getStringFromArg(locale,
          Objects.requireNonNull(messageParameters.getMessagePrefix())) + message;
    }

    if (messageParameters.getMessagePostfix() != null) {
      message = message + PropertiesFileUtil.getStringFromArg(locale,
          Objects.requireNonNull(messageParameters.getMessagePostfix()));
    }

    return message;
  }

  private static void putMesageParameterSetToParamMap(Locale locale,
      final Map<@NonNull String, Object> map, Set<LocalizedEmbeddedParameter> embeddedParameterSet,
      boolean showsItemNamePath) {
    for (LocalizedEmbeddedParameter paramBean : embeddedParameterSet) {

      // Put propertyFileKey as value when paramBean.fileKinds().length == 0.
      if (paramBean.fileKinds().length == 0) {
        map.put(paramBean.parameterKey(), paramBean.propertyFileKey());
        continue;
      }

      String value = "";
      for (PropertiesFileUtilFileKindEnum fileKind : paramBean.fileKinds()) {
        if (paramBean.isItemName()) {
          value =
              MessageUtil.getItemNames(locale, Arrays.asList(paramBean.items()), showsItemNamePath,
                  // always nonnull when paramBean.isItemName() == true
                  Objects.requireNonNull(paramBean.rootBean()));

        } else {
          // Put return value of PropertiesFileUtil.get() even when key does not exist.
          value = PropertiesFileUtil.get(fileKind.toString(), locale, paramBean.propertyFileKey(),
              paramBean.args());

        }

        if (PropertiesFileUtil.has(fileKind.toString(), paramBean.propertyFileKey())) {
          break;
        }
      }

      map.put(paramBean.parameterKey(), value);
    }
  }

  /**
   * Stores parameters of information on a message for ValidationAppException.
   * 
   * <p>It is resolved to message value at ExceptionHandler
   *     Because there is a locale there.</p>
   *     
   * <p>When you designate fileKinds = new PropertiesFileUtilFileKindEnum[] {} (length is zero),
   *     propertyPathKey is set as the value.</p>
   */
  public static record LocalizedEmbeddedParameter(String parameterKey,
      PropertiesFileUtilFileKindEnum[] fileKinds, boolean isItemName, Item @Nullable [] items,
      @Nullable Object rootBean, String propertyFileKey, Arg... args) {

    /**
     * Constructs a new instance without itemName info.
     */
    public LocalizedEmbeddedParameter(String parameterKey,
        PropertiesFileUtilFileKindEnum[] fileKinds, String propertyFileKey, Arg... args) {
      this(parameterKey, fileKinds, false, null, null, propertyFileKey, args);
    }
  }

}
