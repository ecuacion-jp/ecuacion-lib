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
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;
import jp.ecuacion.lib.core.exception.ConstraintViolationExceptionWithParameters;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ValidatorMessageParameterCreator;
import jp.ecuacion.lib.core.jakartavalidation.internal.ConstraintViolationBean;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.lib.core.violation.Violations.MessageParameters;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides available utilities for Exceptions including AppExceptions.
 */
public class ExceptionUtil {

  public static final String SYSTEM_ERROR_OCCURED_SIGN =
      "=============== system error occurred ===============";

  private static volatile @Nullable MessageInterpolator defaultInterpolator;

  /**
   * Prevents other classes from instantiating it.
   */
  private ExceptionUtil() {}

  private static MessageInterpolator getDefaultInterpolator() {
    if (defaultInterpolator == null) {
      synchronized (ExceptionUtil.class) {
        if (defaultInterpolator == null) {
          defaultInterpolator = Validation.buildDefaultValidatorFactory().getMessageInterpolator();
        }
      }
    }
    return Objects.requireNonNull(defaultInterpolator);
  }

  private static class DefaultMessageContext implements MessageInterpolator.Context {
    private final ConstraintDescriptor<?> descriptor;
    private final @Nullable Object value;

    DefaultMessageContext(ConstraintDescriptor<?> descriptor, @Nullable Object value) {
      this.descriptor = descriptor;
      this.value = value;
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
      return descriptor;
    }

    @Override
    public @Nullable Object getValidatedValue() {
      return value;
    }

    @Override
    public <T> T unwrap(@Nullable Class<T> type) {
      throw new jakarta.validation.ValidationException("Unwrapping is not supported.");
    }
  }

  /**
   * Returns Exception message list.
   */
  public static <T> List<@NonNull String> getMessageList(
      Set<ConstraintViolation<T>> constraintViolations) {
    return getMessageList(constraintViolations, null, false, Violations.newMessageParameters());
  }

  /**
   * Returns Exception message list.
   */
  public static <T> List<@NonNull String> getMessageList(
      Set<ConstraintViolation<T>> constraintViolations, @Nullable Locale locale) {
    return getMessageList(constraintViolations, locale, false, Violations.newMessageParameters());
  }

  /**
   * Returns Exception message list.
   * 
   * <p>Even if {@code isMessagesWithItemNamesAsDefault} is non-null value,
   *     messageParameters.isMessageWithItemName value is adopted
   *     when messageParameters.isMessageWithItemName is not null
   *     (= explicitly specified by the caller).<br>
   *     {@code isMessagesWithItemNamesAsDefault} is assumed to a system default value
   *     so messageParameters.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   */
  public static <T> List<@NonNull String> getMessageList(
      Set<ConstraintViolation<T>> constraintViolationSet,
      boolean isMessagesWithItemNamesAsDefault) {
    return getMessageList(constraintViolationSet, null, isMessagesWithItemNamesAsDefault);
  }

  /**
   * Returns Exception message list.
   * 
   * <p>Even if {@code isMessagesWithItemNamesAsDefault} is non-null value,
   *     messageParameters.isMessageWithItemName value is adopted
   *     when messageParameters.isMessageWithItemName is not null
   *     (= explicitly specified by the caller).<br>
   *     {@code isMessagesWithItemNamesAsDefault} is assumed to a system default value
   *     so messageParameters.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   */
  public static <T> List<@NonNull String> getMessageList(
      Set<ConstraintViolation<T>> constraintViolationSet, @Nullable Locale locale,
      boolean isMessagesWithItemNamesAsDefault) {

    return getMessageList(constraintViolationSet, locale, isMessagesWithItemNamesAsDefault,
        Violations.newMessageParameters());
  }

  /**
   * Returns Exception message list.
   * 
   * <p>Even if {@code isMessagesWithItemNamesAsDefault} is non-null value,
   *     messageParameters.isMessageWithItemName value is adopted
   *     when messageParameters.isMessageWithItemName is not null
   *     (= explicitly specified by the caller).<br>
   *     {@code isMessagesWithItemNamesAsDefault} is assumed to a system default value
   *     so messageParameters.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   */
  public static <T> List<@NonNull String> getMessageList(
      Set<ConstraintViolation<T>> constraintViolations, boolean isMessagesWithItemNamesAsDefault,
      MessageParameters messageParameters) {

    return getMessageList(constraintViolations, null, isMessagesWithItemNamesAsDefault,
        messageParameters);
  }

  /**
   * Returns Exception message list.
   * 
   * <p>Even if {@code isMessagesWithItemNamesAsDefault} is non-null value,
   *     messageParameters.isMessageWithItemName value is adopted
   *     when messageParameters.isMessageWithItemName is not null
   *     (= explicitly specified by the caller).<br>
   *     {@code isMessagesWithItemNamesAsDefault} is assumed to a system default value
   *     so messageParameters.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   */
  public static <T> List<@NonNull String> getMessageList(
      Set<ConstraintViolation<T>> constraintViolations, @Nullable Locale locale,
      boolean isMessagesWithItemNamesAsDefault, MessageParameters messageParameters) {

    if (constraintViolations.isEmpty()) {
      throw new RuntimeException("Size of ConstraintViolation is zero.");
    }

    Locale nonNullLocale = locale == null ? Locale.ROOT : locale;
    List<@NonNull String> result = new ArrayList<>();
    for (ConstraintViolation<T> cv : constraintViolations) {
      result
          .add(buildMessageFromConstraintViolation(nonNullLocale, isMessagesWithItemNamesAsDefault,
              cv, ConstraintViolationBean.createConstraintViolationBean(cv), messageParameters));
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
   * so the return type is not a {@code String}, but a {@code List<@NonNull String>}.</p>
   *
   * @param throwable throwable
   * @return a list of messages
   */
  public static List<@NonNull String> getMessageList(Throwable throwable) {
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
   * so the return type is not a {@code String}, but a {@code List<@NonNull String>}.</p>
   *
   * @param throwable throwable
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.ROOT}.
   * @return a list of messages
   */
  public static List<@NonNull String> getMessageList(Throwable throwable, @Nullable Locale locale) {
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
   * so the return type is not a {@code String}, but a {@code List<@NonNull String>}.</p>
   * 
   * <p>Even if {@code isValidationMessagesWithItemNames} is non-null value, 
   *     ConstraintViolationBean.isMessageWithItemName value is adopted 
   *     when ConstraintViolationBean.isMessageWithItemName is not null 
   *     (= explicitly specified by the caller).<br>
   *     {@code isValidationMessagesWithItemNames} is assumed to a system default value
   *     so ConstraintViolationBean.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   *
   * @param throwable throwable
   * @param isMessagesWithItemNamesAsDefault 
   *     isValidationMessagesWithItemNames, may be {@code null} 
   *     which is treated as {@code Locale.ROOT}.
   * @return a list of messages
   */
  public static List<@NonNull String> getMessageList(Throwable throwable,
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
   * so the return type is not a {@code String}, but a {@code List<@NonNull String>}.</p>
   * 
   * <p>Even if {@code isValidationMessagesWithItemNames} is non-null value, 
   *     ConstraintViolationBean.isMessageWithItemName value is adopted 
   *     when ConstraintViolationBean.isMessageWithItemName is not null 
   *     (= explicitly specified by the caller).<br>
   *     {@code isValidationMessagesWithItemNames} is assumed to a system default value
   *     so ConstraintViolationBean.isMessageWithItemName, specified for each validation
   *     overcomes it.</p>
   *
   * @param throwable throwable
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.ROOT}.
   * @param isMessagesWithItemNamesAsDefault true 
   *     when itemName needed for messages.
   *     
   * @return a list of messages
   */
  public static List<@NonNull String> getMessageList(Throwable throwable, @Nullable Locale locale,
      boolean isMessagesWithItemNamesAsDefault) {
    ObjectsUtil.requireNonNull(throwable);
    Locale nonNullLocale = locale == null ? Locale.ROOT : locale;

    // Handle ViolationException first.
    if (throwable instanceof ViolationException ve) {
      return getMessageList(ve.getViolations(), locale, isMessagesWithItemNamesAsDefault);
    }

    List<@NonNull String> rtnList = new ArrayList<>();

    // jakarta.validation.ConstraintViolationException can be thrown from unassumed locations.
    // In that case messages are built directly from ConstraintViolation.
    if (throwable instanceof ConstraintViolationException cve) {
      MessageParameters params = cve instanceof ConstraintViolationExceptionWithParameters cvewp
          ? cvewp.getMessageParameters()
          : Violations.newMessageParameters();

      for (ConstraintViolation<?> cv : cve.getConstraintViolations()) {
        rtnList.add(
            buildMessageFromConstraintViolation(nonNullLocale, isMessagesWithItemNamesAsDefault, cv,
                ConstraintViolationBean.createConstraintViolationBean(cv), params));
      }
      return rtnList;
    }

    if (throwable.getMessage() != null) {
      rtnList.add(Objects.requireNonNull(throwable.getMessage()));
    }

    return rtnList;
  }

  /**
   * Returns message list from {@link Violations}.
   *
   * @param violations violations
   * @return a list of messages
   */
  public static List<@NonNull String> getMessageList(Violations violations) {
    return getMessageList(violations, null, false);
  }

  /**
   * Returns message list from {@link Violations}.
   *
   * @param violations violations
   * @param locale locale, may be {@code null} which is treated as {@code Locale.ROOT}.
   * @return a list of messages
   */
  public static List<@NonNull String> getMessageList(Violations violations,
      @Nullable Locale locale) {
    return getMessageList(violations, locale, false);
  }

  /**
   * Returns message list from {@link Violations}.
   *
   * @param violations violations
   * @param isMessagesWithItemNamesAsDefault true when item names are shown in messages by default.
   * @return a list of messages
   */
  public static List<@NonNull String> getMessageList(Violations violations,
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
   * @param locale locale, may be {@code null} which is treated as {@code Locale.ROOT}.
   * @param isMessagesWithItemNamesAsDefault true when item names are shown in messages by default.
   * @return a list of messages
   */
  public static List<@NonNull String> getMessageList(Violations violations, @Nullable Locale locale,
      boolean isMessagesWithItemNamesAsDefault) {
    List<@NonNull String> result = new ArrayList<>();
    Locale nonNullLocale = locale == null ? Locale.ROOT : locale;

    for (ConstraintViolation<?> cv : violations.getConstraintViolations()) {
      result
          .add(buildMessageFromConstraintViolation(nonNullLocale, isMessagesWithItemNamesAsDefault,
              cv, ConstraintViolationBean.createConstraintViolationBean(cv),
              violations.messageParameters()));
    }

    for (BusinessViolation bv : violations.getBusinessViolations()) {
      result.add(getMessageFromBusinessViolation(nonNullLocale, isMessagesWithItemNamesAsDefault,
          bv, violations.messageParameters()));
    }

    return result;
  }

  private static String buildMessageFromConstraintViolation(Locale locale,
      boolean isMessagesWithItemNamesAsDefault, ConstraintViolation<?> cv,
      ConstraintViolationBean<?> bean, MessageParameters messageParameters) {
    String message = null;
    try {
      final Map<@NonNull String, @Nullable Object> map = new HashMap<>(bean.getEmbeddedParamMap());

      // Put Arg-based parameters directly into the map (resolved by getValidationMessage).
      addArgBasedParamsToMap(bean, map);

      // Merge external validator params (Arg and ItemNameParam values).
      map.putAll(getExternalMessageParams(cv, bean));

      // Resolve ItemNameParam (item names) before formatWithArgs.
      resolveItemNameParams(locale, map, messageParameters.showsItemNamePath());

      // If messageParameters.isMessageWithItemName() is not null (= explicitly specified),
      // it's prioritized over isMessagesWithItemNamesAsDefault.
      Boolean isMessageWithItemName = messageParameters.isMessageWithItemName() != null
          ? Objects.requireNonNull(messageParameters.isMessageWithItemName())
          : isMessagesWithItemNamesAsDefault;

      String messageKey = bean.getMessageTemplate().replace("{", "").replace("}", "");
      boolean isMessageDefined =
          isMessageWithItemName ? PropertiesFileUtil.hasValidationMessageWithItemName(messageKey)
              : PropertiesFileUtil.hasValidationMessage(locale, messageKey);
      if (isMessageDefined) {
        message = isMessageWithItemName
            ? PropertiesFileUtil.getValidationMessageWithItemName(locale, messageKey, map)
            : PropertiesFileUtil.getValidationMessage(locale, messageKey, map);
      } else {
        // No entry in ecuacion-lib properties files; re-interpolate for the target locale.
        message = getDefaultInterpolator().interpolate(bean.getMessageTemplate(),
            new DefaultMessageContext(bean.getConstraintDescriptor(), bean.getInvalidValueObject()),
            locale != null ? locale : Locale.getDefault());
      }

      // Replace {0} to itemName.
      if (message.contains("{0}")) {
        message = MessageFormat.format(message, MessageUtil.getItemNames(locale, bean.getItemList(),
            messageParameters.showsItemNamePath(), bean.getRootBean()));
      }

      // add prefix and postfix messages.
      if (messageParameters.getMessagePrefix() != null) {
        message =
            Objects.requireNonNull(messageParameters.getMessagePrefix()).resolveAsString(locale)
                + message;
      }

      if (messageParameters.getMessagePostfix() != null) {
        message = message
            + Objects.requireNonNull(messageParameters.getMessagePostfix()).resolveAsString(locale);
      }

    } catch (MissingResourceException ignored) {
      message = bean.getMessage();
    }
    return message;
  }

  private static void addArgBasedParamsToMap(ConstraintViolationBean<?> cvBean,
      Map<@NonNull String, @Nullable Object> map) {
    List<Item> beanList = cvBean.getItemList();

    if (!beanList.get(0).getShowsValue()) {
      String key = "jp.ecuacion.lib.core.jakartavalidation.validator.displayStringForHiddenValue";
      map.put("invalidValue", Arg.message(key));
    }
  }

  private static Map<@NonNull String, @Nullable Object> getExternalMessageParams(
      ConstraintViolation<?> cv, ConstraintViolationBean<?> cvBean) {
    Map<@NonNull String, @Nullable Object> rtnMap = new HashMap<>();

    String className = cvBean.getValidatorClass() + "MessageParameterCreator";
    if (ReflectionUtil.classExists(className)) {
      rtnMap.putAll(((ValidatorMessageParameterCreator) ReflectionUtil.newInstance(className))
          .create(cv, cvBean.getEmbeddedParamMap()));
    }

    return rtnMap;
  }

  private static void resolveItemNameParams(@Nullable Locale locale,
      final Map<@NonNull String, @Nullable Object> map, boolean showsItemNamePath) {
    Map<@NonNull String, @Nullable Object> updates = new HashMap<>();
    for (Map.Entry<@NonNull String, @Nullable Object> entry : map.entrySet()) {
      if (entry.getValue() instanceof ValidatorMessageParameterCreator.ItemNameParam lep) {
        updates.put(entry.getKey(), MessageUtil.getItemNames(locale, lep.items(),
            showsItemNamePath, Objects.requireNonNull(lep.rootBean())));
      }
    }
    map.putAll(updates);
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
      Map<@NonNull String, @Nullable Object> namedArgs = new HashMap<>();
      String[] itemNameKeys = violation.getItemNameKeys();
      if (itemNameKeys.length > 0) {
        List<@NonNull Item> itemList =
            Arrays.stream(itemNameKeys).map(key -> new Item(key).itemNameKey(key)).toList();
        String itemName = MessageUtil.getItemNames(locale, itemList, false, new Object());
        namedArgs.put("item_name", itemName);
        namedArgs.put("0", itemName);
      }
      message = PropertiesFileUtil.hasMessageWithItemName(messageKey)
          ? PropertiesFileUtil.getMessageWithItemName(locale, messageKey, namedArgs,
              (Object[]) violation.getMessageArgs())
          : PropertiesFileUtil.getValidationMessageWithItemName(locale, messageKey, namedArgs);
    } else {
      message = PropertiesFileUtil.hasMessage(messageKey)
          ? PropertiesFileUtil.getMessage(locale, messageKey, (Object[]) violation.getMessageArgs())
          : PropertiesFileUtil.getValidationMessage(locale, messageKey, new HashMap<>());
    }

    // add prefix and postfix messages.
    if (messageParameters.getMessagePrefix() != null) {
      message = Objects.requireNonNull(messageParameters.getMessagePrefix()).resolveAsString(locale)
          + message;
    }

    if (messageParameters.getMessagePostfix() != null) {
      message = message
          + Objects.requireNonNull(messageParameters.getMessagePostfix()).resolveAsString(locale);
    }

    return message;
  }
}
