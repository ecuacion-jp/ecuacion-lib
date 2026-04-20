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
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.violation.Violations;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
   * Validates and throws {@link jp.ecuacion.lib.core.exception.ViolationException}
   *     if validation errors exist.
   *
   * @param <T> any class
   * @param object object to validate
   */
  public static <T> void validateThenThrow(@Nullable T object) {
    validateThenThrow(object, new Class<?>[] {});
  }

  /**
   * Validates and throws {@link jp.ecuacion.lib.core.exception.ViolationException}
   *     if validation errors exist.
   *
   * @param <T> any class
   * @param object object to validate
   * @param groups validation groups
   */
  public static <T> void validateThenThrow(T object, Class<?>... groups) {
    validateThenThrow(object, new MessageParameters(), groups);
  }

  /**
   * Validates and throws {@link jp.ecuacion.lib.core.exception.ViolationException}
   *     if validation errors exist.
   *
   * @param <T> any class
   * @param object object to validate
   * @param messageParameters See {@link MessageParameters}.
   */
  public static <T> void validateThenThrow(T object, MessageParameters messageParameters) {
    validateThenThrow(object, messageParameters, new Class<?>[] {});
  }

  /**
   * Validates and throws {@link jp.ecuacion.lib.core.exception.ViolationException}
   *     if validation errors exist.
   *
   * @param <T> any class
   * @param object object to validate
   * @param messageParameters See {@link MessageParameters}.
   * @param groups validation groups
   */
  public static <T> void validateThenThrow(T object, MessageParameters messageParameters,
      Class<?>... groups) {

    Optional<@NonNull Violations> opt = validateThenReturn(object, messageParameters, groups);

    if (opt.isPresent()) {
      Objects.requireNonNull(opt.get()).throwIfAny();
    }
  }

  /**
   * Validates and returns {@link Violations} if validation errors exist.
   *
   * @param <T> any class
   * @param object object to validate
   * @return {@link Violations}, empty when no validation errors exist.
   */
  public static <T> Optional<@NonNull Violations> validateThenReturn(T object) {
    return validateThenReturn(object, new Class<?>[] {});
  }

  /**
   * Validates and returns {@link Violations} if validation errors exist.
   *
   * @param <T> any class
   * @param object object to validate
   * @param groups validation groups
   * @return {@link Violations}, empty when no validation errors exist.
   */
  public static <T> Optional<@NonNull Violations> validateThenReturn(T object,
      Class<?>... groups) {
    return validateThenReturn(object, ValidationUtil.messageParameters(), groups);
  }

  /**
   * Validates and returns {@link Violations} if validation errors exist.
   *
   * @param <T> any class
   * @param object object to validate
   * @param messageParameters See {@link MessageParameters}.
   * @return {@link Violations}, empty when no validation errors exist.
   */
  public static <T> Optional<@NonNull Violations> validateThenReturn(T object,
      MessageParameters messageParameters) {
    return validateThenReturn(object, messageParameters, new Class<?>[] {});
  }

  /**
   * Validates and returns {@link Violations} if validation errors exist.
   *
   * @param <T> any class
   * @param object object to validate
   * @param messageParameters See {@link MessageParameters}.
   * @param groups validation groups
   * @return {@link Violations}, empty when no validation errors exist.
   */
  public static <T> Optional<@NonNull Violations> validateThenReturn(
      T object, MessageParameters messageParameters, Class<?>... groups) {
    Set<ConstraintViolation<T>> set =
        groups == null || groups.length == 0 ? validator.validate(object)
            : validator.validate(object, groups);

    Optional<@NonNull Violations> rtn = set.size() == 0 ? Optional.empty()
        : Optional.of(new Violations().add(set).messageParameters(messageParameters));

    return Objects.requireNonNull(rtn);
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

    private @Nullable Boolean isMessageWithItemName;
    private boolean showsItemNamePath = false;
    private @Nullable Arg messagePrefix;
    private @Nullable Arg messagePostfix;

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
    public @Nullable Boolean isMessageWithItemName() {
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

    public @Nullable Arg getMessagePrefix() {
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

    public @Nullable Arg getMessagePostfix() {
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
