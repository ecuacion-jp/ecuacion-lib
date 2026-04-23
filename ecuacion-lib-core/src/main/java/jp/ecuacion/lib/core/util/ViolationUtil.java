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

/**
 * Provides validation-related utilities.
 */
public class ViolationUtil {

  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  /**
   * Validates and returns {@link Violations} if validation errors exist.
   *
   * @param <T> any class
   * @param object object to validate
   * @return {@link Violations}, empty when no validation errors exist.
   */
  public static <T> Violations validate(T object) {
    return validate(object, new Class<?>[] {});
  }

  /**
   * Validates and returns {@link Violations} if validation errors exist.
   *
   * @param <T> any class
   * @param object object to validate
   * @param groups validation groups
   * @return {@link Violations}, empty when no validation errors exist.
   */
  public static <T> Violations validate(T object, Class<?>... groups) {
    Set<ConstraintViolation<T>> set =
        groups == null || groups.length == 0 ? validator.validate(object)
            : validator.validate(object, groups);

    return new Violations().addAll(set);
  }
}
