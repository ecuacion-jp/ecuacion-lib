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

import java.util.Arrays;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.internal.ValidateWhenValidator;

/**
 * Provides the validation logic for {@code NotStringWhen}.
 */
public class NotStringWhenValidator extends ValidateWhenValidator<NotStringWhen, Object> {

  private String[] propertyValueString;

  /** Initializes an instance. */
  @Override
  public void initialize(NotStringWhen annotation) {
    super.initialize(annotation.message(), annotation.propertyPath(),
        annotation.conditionPropertyPath(), annotation.conditionValue(),
        annotation.conditionOperator(), annotation.conditionValueString(),
        annotation.conditionValuePatternRegexp(), annotation.conditionValuePropertyPath(),
        annotation.stringWhenConditionNotSatisfied());

    this.propertyValueString = annotation.propertyValueString();
  }

  @Override
  protected boolean isValid(Object valueOfField) {
    if (valueOfField == null) {
      valueOfField = EclibValidationConstants.VALIDATOR_PARAMETER_NULL;
    }

    if (!(valueOfField instanceof String)) {
      throw new EclibRuntimeException("The data type of propertyPath must be String.");
    }

    return !Arrays.asList(propertyValueString).contains(valueOfField);
  }

}
