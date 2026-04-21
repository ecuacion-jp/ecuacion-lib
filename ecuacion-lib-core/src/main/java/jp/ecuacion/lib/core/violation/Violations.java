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
import java.util.List;
import java.util.Set;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.util.ValidationUtil.MessageParameters;
import org.jspecify.annotations.NonNull;

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
   * Gets the {@link MessageParameters}.
   *
   * @return messageParameters
   */
  public MessageParameters getMessageParameters() {
    return messageParameters;
  }
}
