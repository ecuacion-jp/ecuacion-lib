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

import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern.stringValueOfConditionFieldIsEqualTo;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern.stringValueOfConditionFieldIsNotEqualTo;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern.valueOfConditionFieldIsEmpty;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern.valueOfConditionFieldIsEqualToValueOf;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern.valueOfConditionFieldIsNotEmpty;
import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern.valueOfConditionFieldIsNotEqualToValueOf;

import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern;
import jp.ecuacion.lib.core.util.internal.ReflectionUtil;

public abstract class ConditionalValidator extends ReflectionUtil {
  private String[] field;
  private String conditionField;
  private ConditionPattern conditionPattern;
  private String[] conditionValueString;
  private String conditionValueField;
  private boolean validatesWhenConditionNotSatisfied;

  public static final String VALIDATION_TARGET_FIELD = "validationTargetField";
  public static final String CONDITION_FIELD = "conditionField";
  public static final String CONDITION_FIELD_ITEM_ID = "conditionFieldItemId";
  public static final String CONDITION_FIELD_DISPLAY_NAME = "conditionFieldDisplayName";
  public static final String CONDITION_PATTERN = "conditionPattern";
  public static final String CONDITION_VALUE_STRING = "conditionValueString";
  public static final String CONDITION_VALUE_FIELD = "conditionValueField";
  public static final String VALUE_OF_CONDITION_VALUE_FIELD_FOR_DISPLAY =
      "valueOfConditionValueFieldForDisplay";

  public static final String VALIDATES_WHEN_CONDITION_NOT_SATISFIED_EMPTY =
      "notEmptyWhenConditionNotSatisfied";
  public static final String VALIDATES_WHEN_CONDITION_NOT_SATISFIED_NOT_EMPTY =
      "emptyWhenConditionNotSatisfied";

  // Used to create messages from conditional validators.
  // public static final String CONDITION_VALUE_KIND = "conditionValueKind";
  public static final String VALUE_OF_CONDITION_FIELD_TO_VALIDATE =
      "valuesOfConditionFieldToValidate";
  public static final String VALIDATES_WHEN_CONDITION_NOT_SATISFIED =
      "validatesWhenConditionNotSatisfied";

  public void initialize(String[] field, String conditionField, ConditionPattern conditionPattern,
      String[] conditionValueString, String conditionValueField,
      boolean validatesWhenConditionNotSatisfied) {
    this.field = field;
    this.conditionField = conditionField;
    this.conditionPattern = conditionPattern;
    this.conditionValueString = conditionValueString;
    this.conditionValueField = conditionValueField;
    this.validatesWhenConditionNotSatisfied = validatesWhenConditionNotSatisfied;
  }

  protected abstract boolean isValid(Object valueOfField);

  /**
   * Executes validation check.
   */
  public boolean isValid(Object instance, ConstraintValidatorContext context) {

    boolean satisfiesCondition = getSatisfiesCondition(instance);

    List<Object> valueOfFieldList =
        Arrays.asList(field).stream().map(f -> getFieldValue(f, instance)).toList();

    for (Object valueOfField : valueOfFieldList) {
      boolean result = isValidForSingleValueOfField(valueOfField, satisfiesCondition);

      if (!result) {
        return false;
      }
    }

    return true;
  }

  private boolean isValidForSingleValueOfField(Object valueOfField, boolean satisfiesCondition) {
    if (satisfiesCondition) {
      return isValid(valueOfField);

    } else {
      if (validatesWhenConditionNotSatisfied) {
        return isValidWhenConditionNotSatisfied(valueOfField);

      } else {
        // Reaching here means valueOfConditionField is not equal to conditionValue. skipped.
        return true;
      }
    }
  }

  boolean getSatisfiesCondition(Object instance) {

    Object valueOfConditionField = getFieldValue(conditionField, instance);

    if (conditionPattern == valueOfConditionFieldIsEmpty
        || conditionPattern == valueOfConditionFieldIsNotEmpty) {

      conditionValueStringMustNotSet();
      conditionValueFieldMustNotSet();

      boolean isEmpty = valueOfConditionField == null || (valueOfConditionField instanceof String
          && ((String) valueOfConditionField).equals(""));

      if (isEmpty && conditionPattern == valueOfConditionFieldIsEmpty
          || !isEmpty && conditionPattern == valueOfConditionFieldIsNotEmpty) {
        return true;
      }

    } else if (conditionPattern == valueOfConditionFieldIsEqualToValueOf
        || conditionPattern == valueOfConditionFieldIsNotEqualToValueOf) {

      conditionValueStringMustNotSet();

      Object valueOfConditionValueField = getFieldValue(conditionValueField, instance);

      List<Object> valueListOfConditionValueField = new ArrayList<>();
      if (valueOfConditionValueField instanceof Object[]) {
        for (Object val : (Object[]) valueOfConditionValueField) {
          valueListOfConditionValueField.add(val);
        }

      } else {
        valueListOfConditionValueField.add(valueOfConditionValueField);
      }

      // dataType difference check
      List<Object> nonnullList =
          valueListOfConditionValueField.stream().filter(v -> v != null).toList();
      Object firstValueOfConditionValueField = nonnullList.size() == 0 ? null : nonnullList.get(0);
      // if either of 2 values is null you cant check difference of datatype. So both is not null.
      if (valueOfConditionField != null && firstValueOfConditionValueField != null) {
        Class<?> valueOfCf = valueOfConditionField.getClass();
        Class<?> firstValueOfCvfList = firstValueOfConditionValueField.getClass();
        if (!firstValueOfCvfList.isAssignableFrom(valueOfCf)) {
          throw new EclibRuntimeException(
              "Datatype not match. valueOfConditionField: " + valueOfConditionField
                  + ", valueListOfConditionValueField.get(0): " + firstValueOfConditionValueField);
        }
      }

      // contains(null) cannot be used for list so change it to VALIDATOR_PARAMETER_NULL in advance.
      valueListOfConditionValueField
          .replaceAll(x -> x == null ? EclibCoreConstants.VALIDATOR_PARAMETER_NULL : x);

      boolean contains = (valueOfConditionField == null
          && valueListOfConditionValueField.contains(EclibCoreConstants.VALIDATOR_PARAMETER_NULL))
          || (valueOfConditionField != null
              && valueListOfConditionValueField.contains(valueOfConditionField));

      if (contains && conditionPattern == valueOfConditionFieldIsEqualToValueOf
          || !contains && conditionPattern == valueOfConditionFieldIsNotEqualToValueOf) {
        return true;
      }

    } else {

      conditionValueFieldMustNotSet();

      if (valueOfConditionField == null) {
        valueOfConditionField = EclibCoreConstants.VALIDATOR_PARAMETER_NULL;
      }

      // When you use 'conditionValue', datatype of valueOfConditionField must be String.
      if (!(valueOfConditionField instanceof String)) {
        throw new EclibRuntimeException(
            "When 'conditionValue' is not null, 'validationConditionField' must be String.");
      }

      boolean contains = Arrays.asList(conditionValueString).contains(valueOfConditionField);
      if (contains && conditionPattern == stringValueOfConditionFieldIsEqualTo
          || !contains && conditionPattern == stringValueOfConditionFieldIsNotEqualTo) {
        return true;
      }
    }

    return false;
  }

  /**
   * Is called when {@code validatesWhenConditionNotSatisfied} is {@code true}.
   * 
   * <p>It's supposed to overrided by child classes.
   *     This method is default method, that's why it always returns {@code true}.</p>
   * 
   * @param valueOfField valueOfField
   * @return boolean
   */
  protected boolean isValidWhenConditionNotSatisfied(Object valueOfField) {
    return true;
  }

  private void conditionValueFieldMustNotSet() {
    // when prerequisite is satisfied, fieldHoldingConditionValue must be null
    if (!Arrays.asList(conditionValueField).contains(EclibCoreConstants.VALIDATOR_PARAMETER_NULL)) {
      throw new EclibRuntimeException("You cannot set 'conditionValueField' when "
          + "howToDetermineConditionIsValid is not either 'valueOfConditionFieldIsEqualToValueOf' "
          + "or 'valueOfConditionFieldIsNotEqualToValueOf'.");
    }
  }

  private void conditionValueStringMustNotSet() {
    // when prerequisite is satisfied, conditionValueIsNotEmpty must be false
    if (!Arrays.asList(conditionValueString)
        .contains(EclibCoreConstants.VALIDATOR_PARAMETER_NULL)) {
      throw new EclibRuntimeException("You cannot set 'conditionValueString' when "
          + "howToDetermineConditionIsValid is not either "
          + "'stringValueOfConditionFieldIsEqualTo' or 'stringValueOfConditionFieldIsNotEqualTo'.");
    }
  }
}
