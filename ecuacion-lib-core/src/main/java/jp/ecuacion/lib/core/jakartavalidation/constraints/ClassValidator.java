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
import java.util.List;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import org.jspecify.annotations.Nullable;

/**
 * Is a ConstraintValidator implemented class for class-level validator.
 * This is accepted by ConstraintViolationBean.
 * 
 * <p>The field "propertyPath" is always needed as the field for validation.</p>
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
   * It's {@code @NonNull} 
   *     but it cannot be initialized at Constructor so initial value is substituted.
   */
  protected Object[] valuesOfPropertyPaths = new Object[] {};

  @Override
  public boolean isValid(T value, @Nullable ConstraintValidatorContext context) {
    valuesOfPropertyPaths = setValuesOfPropertyPaths(value);

    return isValidCommon(value, context);
  }

  private Object[] setValuesOfPropertyPaths(T object) {
    List<Object> list =
        Arrays.stream(propertyPaths).map(path -> ReflectionUtil.getValue(object, path)).toList();

    Object[] rtn = list.toArray(Object[]::new);
    return rtn;
  }
}
