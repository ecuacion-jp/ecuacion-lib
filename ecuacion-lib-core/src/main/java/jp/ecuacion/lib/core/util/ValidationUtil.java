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
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.checked.ConstraintViolationBeanException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.ValidationAppException;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.util.PropertyFileUtil.Arg;

/**
 * Provides validation-related utilities.
 */
public class ValidationUtil {

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
   * @param parameterBean See {@link MessageParameterBean}.
   * @return a set of ConstraintViolationBean, may be empty set when no validation errors exist.
   */
  @Nonnull
  public static <T> Set<ConstraintViolationBean<T>> validate(@RequireNonnull T object,
      @Nullable MessageParameterBean parameterBean) {
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
   * @param parameterBean See {@link MessageParameterBean}.
   * @param groups validation groups
   * @return a set of ConstraintViolationBean, may be empty set when no validation errors exist.
   */
  @Nonnull
  public static <T> Set<ConstraintViolationBean<T>> validate(@RequireNonnull T object,
      @Nullable MessageParameterBean parameterBean, Class<?>... groups) {
    Validator v = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<T>> set =
        (groups == null || groups.length == 0) ? v.validate(object) : v.validate(object, groups);

    MessageParameterBean param = parameterBean == null ? new MessageParameterBean() : parameterBean;

    List<ConstraintViolationBean<T>> list =
        set.stream().map(cv -> new ConstraintViolationBean<T>(cv))
            .peek(cv -> cv.setMessageWithItemName(param.isMessageWithItemNames))
            .peek(cv -> cv.setMessagePrefix(param.getMessagePrefix()))
            .peek(cv -> cv.setMessagePostfix(param.getMessagePostfix())).toList();

    return new HashSet<>(list);
  }

  /**
   * Validates and throws {@code ConstraintViolationBeanException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @throws ConstraintViolationBeanException ConstraintViolationBeanException
   */
  public static <T> void validateThenThrow(@RequireNonnull T object)
      throws ConstraintViolationBeanException {
    validateThenThrow(object, (Class<?>[]) null);
  }

  /**
   * Validates and throws {@code ConstraintViolationBeanException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @param groups validation groups
   * @throws ConstraintViolationBeanException ConstraintViolationBeanException
   */
  public static <T> void validateThenThrow(@RequireNonnull T object, Class<?>... groups)
      throws ConstraintViolationBeanException {
    validateThenThrow(object, null, null, null, groups);
  }

  /**
   * Validates and throws {@code ConstraintViolationBeanException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @throws ConstraintViolationBeanException ConstraintViolationBeanException
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
   * @param parameterBean See {@link MessageParameterBean}.
   * @throws ConstraintViolationBeanException ConstraintViolationBeanException
   */
  public static <T> void validateThenThrow(@RequireNonnull T object,
      @Nullable MessageParameterBean parameterBean) throws ConstraintViolationBeanException {
    validateThenThrow(object, parameterBean, (Class<?>[]) null);
  }

  /**
   * Validates and throws {@code ConstraintViolationBeanException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @throws ConstraintViolationBeanException ConstraintViolationBeanException
   */
  @Deprecated(since = "14.26.0")
  public static <T> void validateThenThrow(@RequireNonnull T object,
      @Nullable Boolean addsItemNameToMessage, @Nullable Arg messagePrefix,
      @Nullable Arg messagePostfix, Class<?>... groups) throws ConstraintViolationBeanException {

    Set<ConstraintViolationBean<T>> set = validate(object,
        new MessageParameterBean(
            addsItemNameToMessage == null ? Boolean.FALSE : addsItemNameToMessage, messagePrefix,
            messagePostfix),
        groups);
    if (set.size() > 0) {
      throw new ConstraintViolationBeanException(set);
    }
  }

  /**
   * Validates and throws {@code ConstraintViolationBeanException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @param parameterBean See {@link MessageParameterBean}.
   * @param groups validation groups
   * @throws ConstraintViolationBeanException ConstraintViolationBeanException
   */
  public static <T> void validateThenThrow(@RequireNonnull T object,
      @Nullable MessageParameterBean parameterBean, Class<?>... groups)
      throws ConstraintViolationBeanException {

    Set<ConstraintViolationBean<T>> set = validate(object, parameterBean, groups);
    if (set.size() > 0) {
      throw new ConstraintViolationBeanException(set);
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
  @Deprecated(since = "14.26.0")
  public static <T> Optional<MultipleAppException> validateThenReturn(@RequireNonnull T object) {
    return validateThenReturn(object, false, null, null, (Class<?>[]) null);
  }

  /**
   * Validates and returns {@code MultipleAppException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @return MultipleAppException, may be null when no validation errors exist.
   */
  @Nonnull
  @Deprecated(since = "14.26.0")
  public static <T> Optional<MultipleAppException> validateThenReturn(@RequireNonnull T object,
      Class<?>... groups) {
    return validateThenReturn(object, false, null, null, groups);
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
        new MessageParameterBean()
            .isMessageWithItemNames(
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
  public static MessageParameterBean messageParameters() {
    return new MessageParameterBean();
  }

  /**
   * Stores validation parameters.
   * 
   * <p>Parameters are meant to show
   *     understandable error messages especially for non-display-value-validations 
   *     (like validation errors for uploaded excel files).</p>
   * 
   * <p>addsItemNameToMessage you'll get message with itemName when {@code true} is specified.
   *        Default value is {@code false}.</p>
   * 
   * <p>messagePrefix Used when you want to put an additional message 
   *     before the original message like "About the uploaded excel file, ". 
   *     It may be {@code null}, which means no messages added.</p>
   * 
   * <p>messagePostfix Used when you want to put an additional message 
   *     after the original message. It may be {@code null}, which means no messages added.</p>
   */
  public static class MessageParameterBean {

    private Boolean isMessageWithItemNames;
    private Arg messagePrefix;
    private Arg messagePostfix;

    /**
     * Construct a new instance. Construction from outside not allowed.
     */
    MessageParameterBean() {

    }

    /**
     * Construct a new instance. Construction from outside not allowed.
     */
    MessageParameterBean(Boolean isMessageWithItemNames, Arg messagePrefix, Arg messagePostfix) {
      this.isMessageWithItemNames = isMessageWithItemNames;
      this.messagePrefix = messagePrefix;
      this.messagePostfix = messagePostfix;
    }

    /**
     * Returns addsItemNameToMessage.
     */
    public Boolean isMessageWithItemNames() {
      return isMessageWithItemNames;
    }

    /**
     * Sets messagePrefix and returns this.
     */
    public MessageParameterBean isMessageWithItemNames(Boolean isMessageWithItemNames) {
      this.isMessageWithItemNames = isMessageWithItemNames;
      return this;
    }

    public Arg getMessagePrefix() {
      return messagePrefix;
    }

    /**
     * Sets messagePrefix and returns this.
     */
    public MessageParameterBean messagePrefix(Arg messagePrefix) {
      this.messagePrefix = messagePrefix;
      return this;
    }

    /**
     * Sets messagePrefix and returns this.
     */
    public MessageParameterBean messagePrefix(String messagePrefix) {
      this.messagePrefix = Arg.string(messagePrefix);
      return this;
    }

    public Arg getMessagePostfix() {
      return messagePostfix;
    }

    /**
     * Sets messagePostfix and returns this.
     */
    public MessageParameterBean messagePostfix(String messagePostfix) {
      this.messagePostfix = Arg.string(messagePostfix);
      return this;
    }

    /**
     * Sets messagePostfix and returns this.
     */
    public MessageParameterBean messagePostfix(Arg messagePostfix) {
      this.messagePostfix = messagePostfix;
      return this;
    }
  }
}
