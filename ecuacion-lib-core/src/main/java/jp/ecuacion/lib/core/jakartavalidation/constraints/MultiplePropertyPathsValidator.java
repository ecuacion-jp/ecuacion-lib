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
import jakarta.validation.ValidationException;
import java.lang.annotation.Annotation;
import jp.ecuacion.lib.core.util.ReflectionUtil;

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
public abstract class MultiplePropertyPathsValidator<A extends Annotation, T> extends ReflectionUtil
    implements ConstraintValidator<A, T> {

  protected String message;
  protected String[] propertyPaths;

  private boolean createsMultipleConstraintViolations = false;

  /**
   * is {@code isValid} method for each class-level validators.
   */
  protected abstract boolean internalIsValid(T value, ConstraintValidatorContext context);

  /**
   * Constructs a new instance.
   */
  public void initialize(String message, String[] propertyPath) {
    this.message = message;
    this.propertyPaths = propertyPath;

    if (propertyPaths.length == 0) {
      throw new ValidationException("Length of propertyPath is zero.");
    }
  }

  @Override
  public boolean isValid(T value, ConstraintValidatorContext context) {
    return isValidCommon(value, context);
  }

  /**
   * Is a common procedure of {@code isValid}.
   */
  protected boolean isValidCommon(T value, ConstraintValidatorContext context) {
    boolean result = internalIsValid(value, context);

    if (createsMultipleConstraintViolations) {
      context.disableDefaultConstraintViolation();

      for (String propertyPath : propertyPaths) {
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(propertyPath)
            .addConstraintViolation();
      }
    }

    return result;
  }

  public void setCreatesMultipleConstraintViolations(boolean createsMultipleConstraintViolations) {
    this.createsMultipleConstraintViolations = createsMultipleConstraintViolations;
  }

}
