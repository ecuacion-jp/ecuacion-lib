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

import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jp.ecuacion.lib.core.util.PropertyPathUtil;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.internal.ValidateWhenValidator;
import org.jspecify.annotations.Nullable;

/**
 * Provides the validation logic for {@code NotValueOfPropertyPathWhen}.
 */
public class NotValueOfPropertyPathWhenValidator
    extends ValidateWhenValidator<NotValueOfPropertyPathWhen, Object> {

  private String valuePropertyPath = "";

  /**
   * Initializes an instance.
   *
   * <p>{@code @Nullable} on the parameter is for Eclipse null analysis compatibility;
   *     see package {@link jp.ecuacion.lib.validation.constraints} for details.</p>
   */
  @Override
  public void initialize(@Nullable NotValueOfPropertyPathWhen annotation) {
    Objects.requireNonNull(annotation);
    super.initialize(annotation.message(), annotation.propertyPath(),
        annotation.conditionPropertyPath(), annotation.conditionValue(),
        annotation.conditionOperator(), annotation.conditionValueString(),
        annotation.conditionValuePatternRegexp(), annotation.conditionValuePropertyPath(),
        annotation.valueOfPropertyPathWhenConditionNotSatisfied());

    this.valuePropertyPath = annotation.valuePropertyPath();
  }

  /**
   * Overrides {@code internalIsValid} instead of {@code isValid(Object)} because this validator
   * needs {@code propertyValues}, which is derived from {@code instance} and therefore cannot be
   * threaded through the single-value {@code isValid(Object)} hook without caching it in an
   * instance field (unsafe: the validator instance is cached and reused across concurrent
   * validations by the Jakarta Validation runtime).
   */
  @Override
  public boolean internalIsValid(Object instance, Object[] valuesOfPropertyPaths,
      @Nullable ConstraintValidatorContext context) {
    boolean satisfiesCondition = getSatisfiesCondition(instance);
    List<Object> propertyValues = computePropertyValues(instance);

    for (int i = 0; i < propertyPaths.length; i++) {
      boolean matches = !isValueInPropertyValues(valuesOfPropertyPaths[i], propertyValues);
      boolean result = satisfiesCondition ? matches
          : (validatesWhenConditionNotSatisfied ? !matches : true);

      if (!result) {
        return false;
      }
    }

    return true;
  }

  private List<Object> computePropertyValues(Object instance) {
    Object valueOfPropertyValuePath = PropertyPathUtil.getValue(instance, valuePropertyPath);

    List<Object> propertyValues = new ArrayList<>();
    if (valueOfPropertyValuePath instanceof Object[] arr) {
      for (Object val : arr) {
        propertyValues.add(val);
      }
    } else {
      propertyValues.add(valueOfPropertyValuePath);
    }

    propertyValues.replaceAll(
        x -> x == null ? EclibValidationConstants.VALIDATOR_PARAMETER_NULL : x);
    return propertyValues;
  }

  private boolean isValueInPropertyValues(@Nullable Object valueOfField,
      List<Object> propertyValues) {
    return (valueOfField == null
        && propertyValues.contains(EclibValidationConstants.VALIDATOR_PARAMETER_NULL))
        || (valueOfField != null && propertyValues.contains(valueOfField));
  }

  @Override
  protected boolean isValid(Object valueOfField) {
    throw new UnsupportedOperationException(
        "internalIsValid is overridden directly; this hook is not used.");
  }

}
