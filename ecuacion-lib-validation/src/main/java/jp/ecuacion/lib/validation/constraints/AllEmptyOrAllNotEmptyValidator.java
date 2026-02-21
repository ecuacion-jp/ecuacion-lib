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
package jp.ecuacion.lib.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.ecuacion.lib.validation.constraints.internal.AllAnyValidator;

/**
 * Provides the validation logic for {@code AllEmptyOrAllNotEmpty}.
 */
public class AllEmptyOrAllNotEmptyValidator extends AllAnyValidator
    implements ConstraintValidator<AllEmptyOrAllNotEmpty, Object> {

  /** Initializes an instance. */
  @Override
  public void initialize(AllEmptyOrAllNotEmpty annotation) {
    super.initialize(annotation.propertyPath());
  }

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext context) {
    int numberOfNonEmptyValues = numberOfNonEmptyValues(object, propertyPaths);
    return numberOfNonEmptyValues == propertyPaths.length || numberOfNonEmptyValues == 0;
  }
}
