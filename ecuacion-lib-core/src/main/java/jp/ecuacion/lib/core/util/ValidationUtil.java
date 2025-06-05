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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.SingleAppException;
import jp.ecuacion.lib.core.exception.checked.ValidationAppException;
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
  * Validates and returns {@code ConstraintViolation} if validation errors exist.
  *
  * @param <T> any class
  * @param object object to validate
  * @return a Set of ConstraintViolation, may be null when no validation errors exist.
  *
  * @see jakarta.validation.Validator
  */
  @Nonnull
  public static <T> Set<ConstraintViolation<T>> validate(@RequireNonnull T object) {
    ObjectsUtil.requireNonNull(object);
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    // validator never returns null
    return validator.validate(object);
  }

  /**
   * Validates and throws {@code MultipleAppException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @throws MultipleAppException MultipleAppException
   */
  public static <T> void validateThenThrow(@RequireNonnull T object) throws MultipleAppException {
    validateThenThrow(object, null, null, null);
  }

  /**
   * Validates and throws {@code MultipleAppException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @throws MultipleAppException MultipleAppException
   */
  public static <T> void validateThenThrow(@RequireNonnull T object,
      @Nullable Boolean addsItemNameToMessage, @Nullable Arg messagePrefix,
      @Nullable Arg messagePostfix) throws MultipleAppException {
    Optional<MultipleAppException> exOpt =
        validateThenReturn(object, addsItemNameToMessage, messagePrefix, messagePostfix);

    if (exOpt.isPresent()) {
      throw exOpt.get();
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
  public static <T> Optional<MultipleAppException> validateThenReturn(@RequireNonnull T object) {
    return validateThenReturn(object, null, null, null);
  }

  /**
   * Validates and returns {@code MultipleAppException} if validation errors exist.
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
  public static <T> Optional<MultipleAppException> validateThenReturn(@RequireNonnull T object,
      @Nullable Boolean addsItemNameToMessage, @Nullable Arg messagePrefix,
      @Nullable Arg messagePostfix) {
    Set<ConstraintViolation<T>> set = ValidationUtil.validate(object);

    MultipleAppException exList = null;
    if (set != null && set.size() > 0) {
      List<SingleAppException> list = new ArrayList<>();
      for (ConstraintViolation<T> v : set) {
        ValidationAppException bvex = new ValidationAppException(v)
            .setMessageWithItemName(addsItemNameToMessage == null ? false : addsItemNameToMessage);

        if (messagePrefix != null) {
          bvex.setMessagePrefix(messagePrefix);
        }

        if (messagePostfix != null) {
          bvex.setMessagePostfix(messagePostfix);
        }

        list.add(bvex);
      }

      exList = new MultipleAppException(list);

    } else {
      exList = null;
    }

    return Optional.ofNullable(exList);
  }
}
