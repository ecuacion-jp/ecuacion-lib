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
package jp.ecuacion.lib.core.jakartavalidation.validator;

import jakarta.validation.ConstraintValidator;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.jakartavalidation.validator.internal.ConditionalValidator;

/**
 * Provides the validation logic for {@code EnumElement}.
 */
public class ConditionalEmptyValidator extends ConditionalValidator
    implements ConstraintValidator<ConditionalEmpty, Object> {

  /** Initializes an instance. */
  @Override
  public void initialize(ConditionalEmpty annotation) {
    super.initialize(annotation.propertyPath(), annotation.conditionPropertyPath(),
        annotation.conditionPattern(),
        annotation.conditionValueString(),
        annotation.conditionValuePropertyPath(), annotation.notEmptyWhenConditionNotSatisfied());
  }

  @Override
  protected boolean isValid(Object valueOfField) {
    return valueOfField == null || valueOfField.equals("")
        || valueOfField.equals(EclibCoreConstants.VALIDATOR_PARAMETER_NULL);
  }

  @Override
  protected boolean isValidWhenConditionNotSatisfied(Object valueOfField) {
    return !isValid(valueOfField);
  }
}
