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
import java.util.Set;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.lib.core.violation.Violations.MessageParameters;
import org.jspecify.annotations.Nullable;

/**
 * Provides validation-related utilities.
 */
@Deprecated
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
  @Deprecated
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
  @Deprecated
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
  @Deprecated
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
  @Deprecated
  public static <T> void validateThenThrow(T object, MessageParameters messageParameters,
      Class<?>... groups) {

    validateThenReturn(object, messageParameters, groups).throwIfAny();
  }

  /**
   * Validates and returns {@link Violations} if validation errors exist.
   *
   * @param <T> any class
   * @param object object to validate
   * @return {@link Violations}, empty when no validation errors exist.
   */
  @Deprecated
  public static <T> Violations validateThenReturn(T object) {
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
  @Deprecated
  public static <T> Violations validateThenReturn(T object, Class<?>... groups) {
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
  @Deprecated
  public static <T> Violations validateThenReturn(T object, MessageParameters messageParameters) {
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
  @Deprecated
  public static <T> Violations validateThenReturn(T object, MessageParameters messageParameters,
      Class<?>... groups) {
    Set<ConstraintViolation<T>> set =
        groups == null || groups.length == 0 ? validator.validate(object)
            : validator.validate(object, groups);

    return new Violations().addAll(set).messageParameters(messageParameters);
  }

  /**
   * Constructs and returns ParameterBean.
   */
  @Deprecated
  public static MessageParameters messageParameters() {
    return new Violations.MessageParameters();
  }
}
