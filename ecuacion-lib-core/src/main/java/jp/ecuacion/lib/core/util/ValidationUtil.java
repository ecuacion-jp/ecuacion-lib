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
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.checked.ConstraintViolationBeanException;
import jp.ecuacion.lib.core.exception.checked.ConstraintViolationExceptionWithParameters;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.ValidationAppException;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.util.PropertyFileUtil.Arg;

/**
 * Provides validation-related utilities.
 */
public class ValidationUtil {

  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  /**
   * Prevents other classes from instantiating it.
   */
  private ValidationUtil() {}

  /**
   * Validates and returns {@code Set<ConstraintViolationBean<T>>}.
   * 
   * <p>It returns an empty set when no errors occur 
   *     according to the specification of jakarta validation.</p>
   * 
   * @param <T> Any class 
   * @param object object to validate
   * @return a set of ConstraintViolationBean, may be empty set when no validation errors exist.
   */
  @Deprecated
  @Nonnull
  public static <T> Set<ConstraintViolationBean<T>> validate(@RequireNonnull T object) {
    return validate(object, new Class<?>[] {});
  }

  /**
   * Validates and returns {@code Set<ConstraintViolationBean<T>>}.
   * 
   * <p>It returns an empty set when no errors occur 
   *     according to the specification of jakarta validation.</p>
   * 
   * @param <T> Any class 
   * @param object object to validate
   * @param groups validation groups
   * @return a set of ConstraintViolationBean, may be empty set when no validation errors exist.
   */
  @Deprecated
  @Nonnull
  public static <T> Set<ConstraintViolationBean<T>> validate(@RequireNonnull T object,
      Class<?>... groups) {
    return validate(object, null, groups);
  }

  /**
   * Validates and returns {@code Set<ConstraintViolationBean<T>>}.
   * 
   * <p>It returns an empty set when no errors occur 
   *     according to the specification of jakarta validation.</p>
   * 
   * @param <T> Any class 
   * @param object object to validate
   * @param parameterBean See {@link MessageParameters}.
   * @return a set of ConstraintViolationBean, may be empty set when no validation errors exist.
   */
  @Deprecated
  @Nonnull
  public static <T> Set<ConstraintViolationBean<T>> validate(@RequireNonnull T object,
      @Nullable MessageParameters parameterBean) {
    return validate(object, parameterBean, new Class<?>[] {});
  }

  /**
   * Validates and returns {@code Set<ConstraintViolationBean<T>>}.
   * 
   * <p>It returns an empty set when no errors occur 
   *     according to the specification of jakarta validation.</p>
   * 
   * @param <T> Any class 
   * @param object object to validate
   * @param parameterBean See {@link MessageParameters}.
   * @param groups validation groups
   * @return a set of ConstraintViolationBean, may be empty set when no validation errors exist.
   */
  @Deprecated
  @Nonnull
  public static <T> Set<ConstraintViolationBean<T>> validate(@RequireNonnull T object,
      @Nullable MessageParameters parameterBean, Class<?>... groups) {
    Validator v = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<T>> set =
        (groups == null || groups.length == 0) ? v.validate(object) : v.validate(object, groups);

    MessageParameters param = parameterBean == null ? new MessageParameters() : parameterBean;

    List<ConstraintViolationBean<T>> list =
        set.stream().map(cv -> new ConstraintViolationBean<T>(cv))
            .peek(cv -> cv.setMessageWithItemName(param.isMessageWithItemName))
            .peek(cv -> cv.setMessagePrefix(param.getMessagePrefix()))
            .peek(cv -> cv.setMessagePostfix(param.getMessagePostfix())).toList();

    return new HashSet<>(list);
  }

  /**
   * Validates and throws {@code ConstraintViolationException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @throws ConstraintViolationException ConstraintViolationException
   */
  public static <T> void validateThenThrow(@RequireNonnull T object)
      throws ConstraintViolationException {
    validateThenThrow(object, (Class<?>[]) null);
  }

  /**
   * Validates and throws {@code ConstraintViolationException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @param groups validation groups
   * @throws ConstraintViolationException ConstraintViolationException
   */
  public static <T> void validateThenThrow(@RequireNonnull T object, Class<?>... groups)
      throws ConstraintViolationException {
    validateThenThrow(object, new MessageParameters(), groups);
  }

  /**
   * Validates and throws {@code ConstraintViolationException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @param messageParameters See {@link MessageParameters}.
   * @throws ConstraintViolationException ConstraintViolationException
   */
  public static <T> void validateThenThrow(@RequireNonnull T object,
      @Nullable MessageParameters messageParameters) throws ConstraintViolationException {
    validateThenThrow(object, messageParameters, (Class<?>[]) null);
  }

  /**
   * Validates and throws {@code ConstraintViolationException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @param messageParameters See {@link MessageParameters}.
   * @param groups validation groups
   * @throws ConstraintViolationException ConstraintViolationException
   */
  public static <T> void validateThenThrow(@RequireNonnull T object,
      @Nullable MessageParameters messageParameters, Class<?>... groups)
      throws ConstraintViolationException {

    Optional<ConstraintViolationException> opt =
        validateThenReturn(object, messageParameters, groups);

    if (opt.isPresent()) {
      throw opt.get();
    }
  }

  /**
   * Validates and throws {@code ConstraintViolationBeanException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @throws ConstraintViolationException ConstraintViolationException
   */
  @Deprecated(since = "14.26.0")
  public static <T> void validateThenThrow(@RequireNonnull T object,
      @Nullable Boolean addsItemNameToMessage, @Nullable Arg messagePrefix,
      @Nullable Arg messagePostfix) throws ConstraintViolationBeanException {
    validateThenThrow(object, addsItemNameToMessage, messagePrefix, messagePostfix,
        (Class<?>[]) null);
  }

  /**
   * Validates and throws {@code ConstraintViolationBeanException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @throws ConstraintViolationException ConstraintViolationException
   */
  @Deprecated(since = "14.26.0")
  public static <T> void validateThenThrow(@RequireNonnull T object,
      @Nullable Boolean addsItemNameToMessage, @Nullable Arg messagePrefix,
      @Nullable Arg messagePostfix, Class<?>... groups) throws ConstraintViolationException {
    MessageParameters params =
        new MessageParameters(addsItemNameToMessage == null ? Boolean.FALSE : addsItemNameToMessage,
            false, messagePrefix, messagePostfix);

    Set<ConstraintViolation<T>> set = validator.validate(object, groups);
    if (set.size() > 0) {
      throw new ConstraintViolationExceptionWithParameters(set, params);
    }
  }

  /**
   * Validates and returns {@code MultipleAppException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @return MultipleAppException, may be null when no validation errors exist.
   */
  @Nonnull
  public static <T> Optional<ConstraintViolationException> validateThenReturn(
      @RequireNonnull T object) {
    return validateThenReturn(object, (Class<?>[]) null);
  }

  /**
   * Validates and returns {@code MultipleAppException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @return MultipleAppException, may be null when no validation errors exist.
   */
  @Nonnull
  public static <T> Optional<ConstraintViolationException> validateThenReturn(
      @RequireNonnull T object, Class<?>... groups) {
    return validateThenReturn(object, null, groups);
  }

  /**
   * Validates and returns {@code MultipleAppException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @return MultipleAppException, may be null when no validation errors exist.
   */
  @Nonnull
  public static <T> Optional<ConstraintViolationException> validateThenReturn(
      @RequireNonnull T object, MessageParameters messageParameters) {
    return validateThenReturn(object, messageParameters, (Class<?>[]) null);
  }

  /**
   * Validates and returns {@code MultipleAppException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @return MultipleAppException, may be null when no validation errors exist.
   */
  @Nonnull
  public static <T> Optional<ConstraintViolationException> validateThenReturn(
      @RequireNonnull T object, MessageParameters messageParameters, Class<?>... groups) {
    Set<ConstraintViolation<T>> set =
        groups == null || groups.length == 0 ? validator.validate(object)
            : validator.validate(object, groups);

    return set.size() == 0 ? Optional.empty()
        : Optional.of(new ConstraintViolationExceptionWithParameters(set, messageParameters));
  }

  /**
   * Validates and returns {@code MultipleAppException} if validation errors exist.
   * 
   * <p>3 parameters are added to arguments in addition to object, which are meant to show
   *     understandable error messages for non-display-value-validations 
   *     (like validations to uploaded excel files) 
   *     when the message displaying setting designates messages are to be shown 
   *     at the bottom of each item.<br>
   *     Prefix and postfix are used to additional explanation for error messages, 
   *     like "About the uploaded excel file, ".</p>
   * 
   * @param <T> any class
   * @param object object to validate
   * @param addsItemNameToMessage you'll get message with itemName when {@code true} is specified.
   *        It may be {@code null}, which is equal to {@code false}. 
   * @param messagePrefix Used when you want to put an additional message 
   *     before the original message. It may be {@code null}, which means no messages added.
   * @param messagePostfix Used when you want to put an additional message 
   *     after the original message. It may be {@code null}, which means no messages added.
   * @return MultipleAppException, may be null when no validation errors exist.
   */
  @Nonnull
  @Deprecated(since = "14.26.0")
  public static <T> Optional<MultipleAppException> validateThenReturn(@RequireNonnull T object,
      @Nullable Boolean addsItemNameToMessage, @Nullable Arg messagePrefix,
      @Nullable Arg messagePostfix) {
    return validateThenReturn(object, addsItemNameToMessage, messagePrefix, messagePostfix,
        (Class<?>[]) null);
  }

  /**
   * Validates and returns {@code MultipleAppException} if validation errors exist.
   * 
   * <p>3 parameters are added to arguments in addition to object, which are meant to show
   *     understandable error messages for non-display-value-validations 
   *     (like validations to uploaded excel files) 
   *     when the message displaying setting designates messages are to be shown 
   *     at the bottom of each item.<br>
   *     Prefix and postfix are used to additional explanation for error messages, 
   *     like "About the uploaded excel file, ".</p>
   * 
   * @param <T> any class
   * @param object object to validate
   * @param addsItemNameToMessage you'll get message with itemName when {@code true} is specified.
   *        It may be {@code null}, which is equal to {@code false}. 
   * @param messagePrefix Used when you want to put an additional message 
   *     before the original message. It may be {@code null}, which means no messages added.
   * @param messagePostfix Used when you want to put an additional message 
   *     after the original message. It may be {@code null}, which means no messages added.
   * @return MultipleAppException, may be null when no validation errors exist.
   */
  @Nonnull
  @Deprecated(since = "14.26.0")
  public static <T> Optional<MultipleAppException> validateThenReturn(@RequireNonnull T object,
      Boolean addsItemNameToMessage, @Nullable Arg messagePrefix, @Nullable Arg messagePostfix,
      Class<?>... groups) {
    Set<ConstraintViolationBean<T>> set = ValidationUtil.validate(object,
        new MessageParameters()
            .isMessageWithItemName(
                addsItemNameToMessage == null ? Boolean.FALSE : addsItemNameToMessage)
            .messagePrefix(messagePrefix).messagePostfix(messagePostfix),
        groups);

    if (set.size() == 0) {
      return Optional.empty();
    }

    MultipleAppException listEx = new MultipleAppException(
        set.stream().map(bean -> new ValidationAppException(bean)).toList());

    return Optional.of(listEx);
  }

  /**
   * Constructs and returns ParameterBean.
   */
  public static MessageParameters messageParameters() {
    return new MessageParameters();
  }

  /**
   * Stores validation parameters.
   * 
   * <p>Parameters are meant to show
   *     understandable error messages especially for non-display-value validations 
   *     (like validation errors for uploaded excel files).</p>
   * 
   * <p>{@code isMessageWithItemName}: You'll get message with itemName 
   *     when {@code true} is specified.
   *     Default value is {@code false}.</p>
   * 
   * <p>{@code messagePrefix} and {@code messagePostfix}: Used 
   *     when you want to put an additional message 
   *     before the original message like "About the uploaded excel file, ". 
   *     It may be {@code null}, which means no messages added.</p>
   */
  public static class MessageParameters {

    private Boolean isMessageWithItemName;
    private boolean showsItemNamePath;
    private Arg messagePrefix;
    private Arg messagePostfix;

    /**
     * Construct a new instance.
     */
    public MessageParameters() {

    }

    /**
     * Construct a new instance.
     */
    public MessageParameters(Boolean isMessageWithItemName, String messagePrefix,
        String messagePostfix, boolean showsItemNamePath) {
      this.isMessageWithItemName = isMessageWithItemName;
      this.showsItemNamePath = showsItemNamePath;
      this.messagePrefix = messagePrefix == null ? null : Arg.string(messagePrefix);
      this.messagePostfix = messagePostfix == null ? null : Arg.string(messagePostfix);
    }

    /**
     * Construct a new instance.
     */
    public MessageParameters(Boolean isMessageWithItemName, boolean showsItemNamePath,
        Arg messagePrefix, Arg messagePostfix) {
      this.isMessageWithItemName = isMessageWithItemName;
      this.messagePrefix = messagePrefix;
      this.messagePostfix = messagePostfix;
    }

    /**
     * Returns addsItemNameToMessage.
     */
    public Boolean isMessageWithItemName() {
      return isMessageWithItemName;
    }

    /**
     * Sets messagePrefix and returns this.
     */
    public MessageParameters isMessageWithItemName(Boolean isMessageWithItemName) {
      this.isMessageWithItemName = isMessageWithItemName;
      return this;
    }

    /**
     * Returns the value of showsItemNamePath.
     */
    public boolean showsItemNamePath() {
      return showsItemNamePath;
    }

    /**
     * Sets showsItemNamePath.
     */
    public MessageParameters showsItemNamePath(boolean showsItemNamePath) {
      this.showsItemNamePath = showsItemNamePath;
      return this;
    }

    public Arg getMessagePrefix() {
      return messagePrefix;
    }

    /**
     * Sets messagePrefix and returns this.
     */
    public MessageParameters messagePrefix(Arg messagePrefix) {
      this.messagePrefix = messagePrefix;
      return this;
    }

    /**
     * Sets messagePrefix and returns this.
     * 
     * <p>The argument string is treated as a message key 
     *     and the value is adopted when the message key is found 
     *     in {@code messages.properties}.
     *     When not found, the argument string itself is used as the prefix message.</p>
     */
    public MessageParameters messagePrefix(String messagePrefix) {
      this.messagePrefix = Arg.message(messagePrefix);
      return this;
    }

    public Arg getMessagePostfix() {
      return messagePostfix;
    }

    /**
     * Sets messagePostfix and returns this.
     */
    public MessageParameters messagePostfix(Arg messagePostfix) {
      this.messagePostfix = messagePostfix;
      return this;
    }

    /**
     * Sets messagePostfix and returns this.
     * 
     * <p>The argument string is treated as a message key 
     *     and the value is adopted when the message key is found 
     *     in {@code messages.properties}.
     *     When not found, the argument string itself is used as the postfix message.</p>
     */
    public MessageParameters messagePostfix(String messagePostfix) {
      this.messagePostfix = Arg.message(messagePostfix);
      return this;
    }
  }
}
