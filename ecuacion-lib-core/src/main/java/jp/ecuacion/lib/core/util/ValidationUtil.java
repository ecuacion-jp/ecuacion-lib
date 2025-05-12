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
    ObjectsUtil.paramRequireNonNull(object);
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    // validator never returns null
    return validator.validate(object);
  }

  /**
   * Constructs a new instance.
   * 
   * @return ValidationExecutorBuilder
   */
  public static ValidationExecutorBuilder builder() {
    return new ValidationExecutorBuilder();
  }

  /**
   * Provides a builder of {@code ValidationExecutor}.
   */
  public static class ValidationExecutorBuilder {
    private boolean isMessageWithItemName;
    private Arg messagePrefix;
    private Arg messagePostfix;

    /**
     * Sets {@code messageWithItemName} and returns this for method chain.
     * 
     * @param isMessageWithItemName isMessageWithItemName
     * @return BeanValidationUtil
     */
    @Nonnull
    public ValidationExecutorBuilder messageWithItemName(boolean isMessageWithItemName) {
      this.isMessageWithItemName = isMessageWithItemName;
      return this;
    }

    /**
     * Sets {@code messagePrefix} and returns this for method chain.
     * 
     * @param messagePrefix messagePrefix
     * @return BeanValidationUtil
     */
    @Nonnull
    public ValidationExecutorBuilder messagePrefix(@RequireNonnull Arg messagePrefix) {
      this.messagePrefix = messagePrefix;
      return this;
    }

    /**
     * Sets {@code messagePostfix} and returns this for method chain.
     * 
     * @param messagePostfix messagePostfix
     * @return BeanValidationUtil
     */
    @Nonnull
    public ValidationExecutorBuilder messagePostfix(@RequireNonnull Arg messagePostfix) {
      this.messagePostfix = messagePostfix;
      return this;
    }

    /**
     * Builds an executor.
     * 
     * @return ValidationExecutor
     */
    @Nonnull
    public ValidationExecutor build() {
      return new ValidationExecutor(isMessageWithItemName, messagePrefix, messagePostfix);
    }
  }

  /**
   * Executes validation.
   */
  public static class ValidationExecutor {

    private boolean isMessageWithItemName;
    private Arg messagePrefix;
    private Arg messagePostfix;

    /**
     * Construct a new instance.
     * 
     * @param isMessageWithItemName isMessageWithItemName
     * @param messagePrefix messagePrefix
     * @param messagePostfix messagePostfix
     */
    public ValidationExecutor(boolean isMessageWithItemName, @Nullable Arg messagePrefix,
        @Nullable Arg messagePostfix) {
      this.isMessageWithItemName = isMessageWithItemName;
      this.messagePrefix = messagePrefix;
      this.messagePostfix = messagePostfix;
    }

    /**
     * Validates and throws {@code MultipleAppException} if validation errors exist.
     * 
     * @param <T> any class
     * @param object object to validate
     * @throws MultipleAppException MultipleAppException
     */
    public <T> void validateThenThrow(@RequireNonnull T object) throws MultipleAppException {
      MultipleAppException exList = validateThenReturn(object);
      if (exList != null && exList.getList().size() > 0) {
        throw exList;
      }
    }

    /**
     * Validates and returns {@code MultipleAppException} if validation errors exist.
     * 
     * @param <T> any class
     * @param object object
     *     which is treated as {@code Locale.getDefault()}.
     * @return MultipleAppException, may be null when no validation errors exist.
     */
    @Nullable
    public <T> MultipleAppException validateThenReturn(@RequireNonnull T object) {
      Set<ConstraintViolation<T>> set = ValidationUtil.validate(object);

      MultipleAppException exList = null;
      if (set != null && set.size() > 0) {
        List<SingleAppException> list = new ArrayList<>();
        for (ConstraintViolation<T> v : set) {
          ValidationAppException bvex =
              new ValidationAppException(v).setMessageWithItemName(isMessageWithItemName);

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

      return exList;
    }
  }
}
