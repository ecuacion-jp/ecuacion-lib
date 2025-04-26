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
package jp.ecuacion.lib.core.jakartavalidation.validator.internal;

import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.jakartavalidation.util.internal.PrivateFieldReader;
import org.apache.commons.lang3.StringUtils;

public abstract class ConditionalValidator extends PrivateFieldReader {
  private String field;
  private String conditionField;
  private String[] conditionValue;
  private boolean conditionValueIsEmpty;
  private boolean conditionValueIsNotEmpty;
  private String fieldHoldingConditionValue;

  public static final String VALIDATION_TARGET_FIELD = "validationTargetField";

  public static final String CONDITION_FIELD = "conditionField";
  public static final String CONDITION_VALUE = "conditionValue";
  public static final String CONDITION_VALUE_IS_EMPTY = "conditionValueIsEmpty";
  public static final String CONDITION_VALUE_IS_NOT_EMPTY = "conditionValueIsNotEmpty";
  public static final String FIELD_HOLDING_CONDITOION_VALUE = "fieldHoldingConditionValue";

  // Used to create messages from conditional validators.
  public static final String CONDITION_VALUE_KIND = "conditionValueKind";
  public static final String VALUE_OF_CONDITION_FIELD_TO_VALIDATE =
      "valuesOfConditionFieldToValidate";

  public void initialize(String field, String conditionField, String[] conditionValue,
      boolean conditionValueIsEmpty, boolean conditionValueIsNotEmpty,
      String fieldHoldingConditionValue) {
    this.field = field;
    this.conditionField = conditionField;
    this.conditionValue = conditionValue;
    this.conditionValueIsEmpty = conditionValueIsEmpty;
    this.conditionValueIsNotEmpty = conditionValueIsNotEmpty;
    this.fieldHoldingConditionValue = fieldHoldingConditionValue;
  }

  protected abstract boolean isValid(Object valueOfField);

  /**
   * Executes validation check.
   */
  public boolean isValid(Object instance, ConstraintValidatorContext context) {

    Object valueOfField = getFieldValue(field, instance, VALIDATION_TARGET_FIELD);
    Object valueOfConditionField = getFieldValue(conditionField, instance, CONDITION_FIELD);

    if (!(conditionValue.length == 1
        && conditionValue[0].equals(EclibCoreConstants.VALIDATOR_PARAMETER_NULL))) {

      conditionValueIsEmptyMustBeFalse(CONDITION_VALUE);
      conditionValueIsNotEmptyMustBeFalse(CONDITION_VALUE);
      fieldHoldingConditionalValueMustBeNull(CONDITION_VALUE);

      if (valueOfConditionField != null) {
        // When you use 'conditionValue', datatype of valueOfConditionField must be String.
        if (!(valueOfConditionField instanceof String)) {
          throw new EclibRuntimeException(
              "When 'conditionValue' is not null, 'validationConditionField' must be String.");
        }

        if (Arrays.asList(conditionValue).contains(valueOfConditionField)) {
          return isValid(valueOfField);
        }
      }

    } else if (conditionValueIsEmpty) {

      conditionValueIsNotEmptyMustBeFalse(CONDITION_VALUE_IS_EMPTY + " = true");
      fieldHoldingConditionalValueMustBeNull(CONDITION_VALUE_IS_EMPTY + " = true");

      if ((valueOfConditionField instanceof String
          && StringUtils.isEmpty((String) valueOfConditionField))
          || valueOfConditionField == null) {
        return isValid(valueOfField);
      }

    } else if (conditionValueIsNotEmpty) {

      fieldHoldingConditionalValueMustBeNull(CONDITION_VALUE_IS_NOT_EMPTY + " = true");

      if ((valueOfConditionField instanceof String
          && !StringUtils.isEmpty((String) valueOfConditionField))
          || valueOfConditionField != null) {
        return isValid(valueOfField);
      }

    } else {
      // conditionValue == null && conditionValueIsNotEmpty == false

      if (fieldHoldingConditionValue.equals(EclibCoreConstants.VALIDATOR_PARAMETER_NULL)) {
        // This case means validation check should be executed
        // when the value of conditionField is null
        if (valueOfConditionField == null) {
          return isValid(valueOfField);
        }

      } else {
        Object valueOfFieldHoldingConditionalValue = getFieldValue(fieldHoldingConditionValue,
            instance, FIELD_HOLDING_CONDITOION_VALUE);

        if ((valueOfConditionField == null && valueOfFieldHoldingConditionalValue == null)
            || (valueOfConditionField != null
                && valueOfFieldHoldingConditionalValue.equals(valueOfConditionField))) {
          return isValid(valueOfField);
        }
      }
    }

    // Reaching here means valueOfConditionField is not equal to conditionValue. skipped.
    return true;
  }

  private void conditionValueIsEmptyMustBeFalse(String prerequisite) {
    // when prerequisite is satisfied, conditionValueIsNotEmpty must be false
    if (conditionValueIsEmpty) {
      throw new EclibRuntimeException("When you set '" + prerequisite + "', you cannot set '"
          + CONDITION_VALUE_IS_EMPTY + " = true'.");
    }
  }

  private void conditionValueIsNotEmptyMustBeFalse(String prerequisite) {
    // when prerequisite is satisfied, conditionValueIsNotEmpty must be false
    if (conditionValueIsNotEmpty) {
      throw new EclibRuntimeException("When you set '" + prerequisite + "', you cannot set '"
          + CONDITION_VALUE_IS_NOT_EMPTY + " = true'.");
    }
  }

  private void fieldHoldingConditionalValueMustBeNull(String prerequisite) {
    // when prerequisite is satisfied, fieldHoldingConditionValue must be null
    if (!fieldHoldingConditionValue.equals(EclibCoreConstants.VALIDATOR_PARAMETER_NULL)) {
      throw new EclibRuntimeException("When you set '" + prerequisite + "', you cannot set '"
          + FIELD_HOLDING_CONDITOION_VALUE + "'.");
    }
  }
}
