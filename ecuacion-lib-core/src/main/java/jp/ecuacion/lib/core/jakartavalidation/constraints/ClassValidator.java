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
package jp.ecuacion.lib.core.jakartavalidation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import jp.ecuacion.lib.core.util.PropertyPathUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Is a ConstraintValidator implemented class for class-level validator.
 * This is accepted by ConstraintViolationBean.
 * 
 * <p>The annotation for implementations of this class must have a {@code propertyPath}
 *     attribute whose elements are non-empty strings identifying the fields associated
 *     with the constraint. An empty array or an element that is an empty string is
 *     rejected at initialization time by {@link MultiplePropertyPathsValidator#initialize}.</p>
 * 
 * <p>Jakarta Validation has the feature to create multiple ConstraintViolations 
 *     out of one validator using 
 *     {@code ConstraintValidatorContext.disableDefaultConstraintViolation()} 
 *     and {@code ConstraintViolationBuilder#addConstraintViolation()}, 
 *     but its feature is not used because it makes difficult to manage the number of 
 *     messages with the same string and so on. One ConstraintViolation 
 *     for one validator is easier to manipulate.</p>
 */
public abstract class ClassValidator<A extends Annotation, T>
    extends MultiplePropertyPathsValidator<A, T> implements ConstraintValidator<A, T> {

  /**
   * Computes the value of each {@code propertyPath} once per validation call, then delegates to
   * {@link #internalIsValid(Object, Object[], ConstraintValidatorContext)}.
   *
   * <p>{@code ConstraintValidator} instances are cached and reused by the Jakarta Validation
   *     runtime across concurrent validations of different objects, so the computed values are
   *     passed down as a method argument rather than kept in an instance field.</p>
   */
  @Override
  protected final boolean internalIsValid(@NonNull T value,
      @Nullable ConstraintValidatorContext context) {
    return internalIsValid(value, computeValuesOfPropertyPaths(value), context);
  }

  /**
   * Is {@code internalIsValid} for class-level validators, additionally given the value of each
   * {@code propertyPath}, computed once per call from {@code value}.
   */
  protected abstract boolean internalIsValid(@NonNull T value, Object[] valuesOfPropertyPaths,
      @Nullable ConstraintValidatorContext context);

  private Object[] computeValuesOfPropertyPaths(Object object) {
    return Arrays.stream(propertyPaths).map(path -> PropertyPathUtil.getValue(object, path))
        .toArray(Object[]::new);
  }
}
