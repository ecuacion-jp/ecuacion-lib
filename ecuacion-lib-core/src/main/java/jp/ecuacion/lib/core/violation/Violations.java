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
package jp.ecuacion.lib.core.violation;

import jakarta.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.exception.ViolationWarningException;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Collects {@link ConstraintViolation}s and {@link BusinessViolation}s
 *     and throws {@link ViolationException} at once when any are present.
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * Violations violations = new Violations();
 * violations.add(constraintViolationSet);
 * violations.add(new BusinessViolation("msg.id"));
 * violations.throwIfAny();
 * }</pre>
 */
public class Violations {

  private List<@NonNull ConstraintViolation<?>> constraintViolations = new ArrayList<>();
  private List<@NonNull BusinessViolation> businessViolations = new ArrayList<>();
  private MessageParameters messageParameters = new MessageParameters();

  /**
   * Adds a set of {@link ConstraintViolation}s.
   *
   * @param violation set of constraint violations to add
   * @return this instance for method chaining
   */
  public Violations add(ConstraintViolation<?> violation) {
    constraintViolations.add(violation);
    return this;
  }

  /**
   * Adds a {@link BusinessViolation}.
   *
   * @param violation business violation to add
   * @return this instance for method chaining
   */
  public Violations add(BusinessViolation violation) {
    businessViolations.add(violation);
    return this;
  }

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   *
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public Violations add(String messageId, @Nullable String... messageArgs) {
    return add(new String[] {}, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemPropertyPaths the itemPropertyPaths related to the violation
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public Violations add(@NonNull String[] itemPropertyPaths, String messageId,
      @Nullable String... messageArgs) {
    return add(null, itemPropertyPaths, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param rootBean rootBean
   * @param itemPropertyPaths the itemPropertyPaths related to the violation
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public Violations add(@Nullable Object rootBean, @NonNull String[] itemPropertyPaths,
      String messageId, @Nullable String... messageArgs) {
    return add(rootBean, itemPropertyPaths, messageId,
        Arrays.stream(messageArgs).map(arg -> Arg.string(arg)).toArray(Arg[]::new));
  }

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   *
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public Violations add(String messageId, @NonNull Arg[] messageArgs) {
    return add(new @NonNull String[] {}, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemPropertyPaths the itemPropertyPaths related to the violation
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public Violations add(@NonNull String[] itemPropertyPaths, String messageId,
      @NonNull Arg[] messageArgs) {
    return add(null, itemPropertyPaths, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param rootBean rootBean
   * @param itemPropertyPaths the itemPropertyPaths related to the violation
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public Violations add(@Nullable Object rootBean, @NonNull String[] itemPropertyPaths,
      String messageId, @NonNull Arg[] messageArgs) {
    return add(new BusinessViolation(rootBean, itemPropertyPaths, messageId, messageArgs));
  }

  /**
   * Adds a set of {@link ConstraintViolation}s.
   *
   * @param violations set of constraint violations to add
   * @return this instance for method chaining
   */
  public Violations addAll(Set<? extends ConstraintViolation<?>> violations) {
    constraintViolations.addAll(violations);
    return this;
  }

  /**
   * Adds a {@link BusinessViolation}.
   *
   * @param violationList business violation list to add
   * @return this instance for method chaining
   */
  public Violations addAll(List<BusinessViolation> violationList) {
    businessViolations.addAll(violationList);
    return this;
  }

  /**
   * Sets {@link MessageParameters} to apply when constructing messages from
   *     {@link ConstraintViolation}s.
   *
   * @param messageParameters message parameters
   * @return this instance for method chaining
   */
  public Violations messageParameters(MessageParameters messageParameters) {
    this.messageParameters = messageParameters;
    return this;
  }

  /**
   * Sets {@link MessageParameters} to apply when constructing messages from
   *     {@link ConstraintViolation}s.
   */
  public MessageParameters messageParameters() {
    return messageParameters;
  }

  /**
   * Returns new MessagepPrameters.
   */
  public Violations withMessageParameters(Consumer<MessageParameters> action) {
    action.accept(messageParameters);
    return this;
  }

  /**
   * Throws {@link ViolationException} if any violations have been added.
   *
   * @throws ViolationException when one or more violations are present
   */
  public void throwIfAny() {
    if (!constraintViolations.isEmpty() || !businessViolations.isEmpty()) {
      throw new ViolationException(this);
    }
  }

  /**
   * Instantiates {@code violationExceptionClass} and throws it if any violations have been added.
   *
   * <p>{@code violationExceptionClass} must have a constructor
   *     that takes a single {@link Violations} argument,
   *     the same as {@link ViolationException#ViolationException(Violations)}.
   *     If no such constructor exists, {@link RuntimeException} is thrown instead.</p>
   *
   * @param <T> exception type extending {@link ViolationException}
   * @param violationExceptionClass the exception class to instantiate and throw
   * @throws T when one or more violations are present
   * @throws RuntimeException when {@code violationExceptionClass}
   *     has no constructor with a {@link Violations} argument
   */
  public <T extends ViolationException> void throwIfAny(Class<T> violationExceptionClass) {
    if (!constraintViolations.isEmpty() || !businessViolations.isEmpty()) {
      try {
        throw violationExceptionClass.getConstructor(Violations.class).newInstance(this);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(violationExceptionClass.getName()
            + " must have a constructor with a Violations argument.", e);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(
            "Failed to instantiate " + violationExceptionClass.getName() + ".", e);
      }
    }
  }

  /**
   * Throws {@link ViolationException} if any violations have been added.
   *
   * @throws ViolationException when one or more violations are present
   */
  public void throwWarningIfAny() {
    if (!constraintViolations.isEmpty() || !businessViolations.isEmpty()) {
      throw new ViolationWarningException(this);
    }
  }

  /**
   * Returns a copy of the collected {@link ConstraintViolation}s.
   *
   * @return list of constraint violations
   */
  public List<@NonNull ConstraintViolation<?>> getConstraintViolations() {
    return new ArrayList<>(constraintViolations);
  }

  /**
   * Returns a copy of the collected {@link BusinessViolation}s.
   *
   * @return list of business violations
   */
  public List<@NonNull BusinessViolation> getBusinessViolations() {
    return new ArrayList<>(businessViolations);
  }

  /**
   * Returns new MessagepPrameters.
   */
  public static MessageParameters newMessageParameters() {
    return new Violations.MessageParameters();
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
    public MessageParameters() {}

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
      this.showsItemNamePath = showsItemNamePath;
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
