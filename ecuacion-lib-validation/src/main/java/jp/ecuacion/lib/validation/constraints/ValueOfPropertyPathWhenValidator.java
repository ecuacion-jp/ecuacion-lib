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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.internal.ValidateWhenValidator;
import org.jspecify.annotations.Nullable;

/**
 * Provides the validation logic for {@code ValueOfPropertyPathWhen}.
 */
public class ValueOfPropertyPathWhenValidator
    extends ValidateWhenValidator<ValueOfPropertyPathWhen, Object> {

  private String valuePropertyPath = "";
  private List<Object> propertyValues = new ArrayList<>();

  /** Initializes an instance. */
  @Override
  public void initialize(@Nullable ValueOfPropertyPathWhen annotation) {
    Objects.requireNonNull(annotation);
    super.initialize(annotation.message(), annotation.propertyPath(),
        annotation.conditionPropertyPath(), annotation.conditionValue(),
        annotation.conditionOperator(), annotation.conditionValueString(),
        annotation.conditionValuePatternRegexp(), annotation.conditionValuePropertyPath(),
        annotation.notValueOfPropertyPathWhenConditionNotSatisfied());

    this.valuePropertyPath = annotation.valuePropertyPath();
  }

  @Override
  public void procedureBeforeLoopForEachPropertyPath(Object instance) {
    super.procedureBeforeLoopForEachPropertyPath(instance);

    Object valueOfPropertyValuePath = getValue(instance, valuePropertyPath);

    propertyValues = new ArrayList<>();
    if (valueOfPropertyValuePath instanceof Object[]) {
      for (Object val : (Object[]) valueOfPropertyValuePath) {
        propertyValues.add(val);
      }
    } else {
      propertyValues.add(valueOfPropertyValuePath);
    }

    propertyValues.replaceAll(
        x -> x == null ? EclibValidationConstants.VALIDATOR_PARAMETER_NULL : x);
  }

  @Override
  protected boolean isValid(Object valueOfField) {
    return (valueOfField == null
        && propertyValues.contains(EclibValidationConstants.VALIDATOR_PARAMETER_NULL))
        || (valueOfField != null && propertyValues.contains(valueOfField));
  }

}
