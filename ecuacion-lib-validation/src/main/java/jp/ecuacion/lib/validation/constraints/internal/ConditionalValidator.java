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
package jp.ecuacion.lib.validation.constraints.internal;

import static jp.ecuacion.lib.validation.constraints.enums.ConditionOperator.EQUAL_TO;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionOperator.NOT_EQUAL_TO;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.EMPTY;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.FALSE;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.TRUE;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.VALUE_OF_PROPERTY_PATH;

import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import org.apache.commons.lang3.tuple.Pair;

public abstract class ConditionalValidator extends ClassValidator {
  private String conditionPropertyPath;
  private ConditionValue conditionPattern;
  private ConditionOperator conditionOperator;
  private String[] conditionValueString;
  private String conditionValuePropertyPath;
  private boolean validatesWhenConditionNotSatisfied;

  private boolean satisfiesCondition = false;

  public static final String CONDITION_PROPERTY_PATH = "conditionPropertyPath";
  public static final String CONDITION_PROPERTY_PATH_ITEM_NAME_KEY =
      "conditionPropertyPathItemNameKey";
  public static final String CONDITION_PROPERTY_PATH_ITEM_NAME = "conditionPropertyPathItemName";
  public static final String CONDITION_PATTERN = "conditionPattern";
  public static final String CONDITION_OPERATOR = "conditionOperator";
  public static final String CONDITION_VALUE_STRING = "conditionValueString";
  public static final String CONDITION_VALUE_PROPERTY_PATH = "conditionValuePropertyPath";
  public static final String DISPLAY_STRING_PROPERTY_PATH_OF_CONDITION_VALUE_PROPERTY_PATH =
      "displayStringPropertyPathOfConditionValuePropertyPath";

  public static final String DISPLAY_STRING_OF_CONDITION_VALUE = "displayStringOfConditionValue";
  public static final String VALIDATES_WHEN_CONDITION_NOT_SATISFIED =
      "validatesWhenConditionNotSatisfied";

  public void initialize(String[] propertyPath, String conditionPropertyPath,
      ConditionValue conditionPattern, ConditionOperator conditionOperator,
      String[] conditionValueString, String conditionValuePropertyPath,
      boolean validatesWhenConditionNotSatisfied) {
    super.initialize(propertyPath);

    this.conditionPropertyPath = conditionPropertyPath;
    this.conditionPattern = conditionPattern;
    this.conditionOperator = conditionOperator;
    this.conditionValueString = conditionValueString;
    this.conditionValuePropertyPath = conditionValuePropertyPath;
    this.validatesWhenConditionNotSatisfied = validatesWhenConditionNotSatisfied;
  }

  protected abstract boolean isValid(Object valueOfField);

  /**
   * Executes validation check.
   */
  public boolean isValid(Object instance, ConstraintValidatorContext context) {

    procedureBeforeLoopForEachPropertyPath(instance);

    List<Pair<String, Object>> valueOfFieldList = Arrays.asList(propertyPaths).stream()
        .map(path -> Pair.of(path, getValue(instance, path))).toList();

    for (Pair<String, Object> pair : valueOfFieldList) {
      boolean result = isValidForSinglePropertyPath(pair.getLeft(), pair.getRight());

      if (!result) {
        return false;
      }
    }

    return true;
  }

  public void procedureBeforeLoopForEachPropertyPath(Object instance) {
    satisfiesCondition = getSatisfiesCondition(instance);
  }

  protected boolean isValidForSinglePropertyPath(String itemPropertyPath, Object valueOfField) {
    if (satisfiesCondition) {
      return isValid(valueOfField);

    } else {
      if (validatesWhenConditionNotSatisfied) {
        return isValidWhenConditionNotSatisfied(valueOfField);

      } else {
        return true;
      }
    }
  }

  boolean getSatisfiesCondition(Object instance) {

    Object valueOfConditionField = getValue(instance, conditionPropertyPath);

    if (conditionPattern == EMPTY) {

      conditionValueStringMustNotSet();
      conditionValueFieldMustNotSet();

      boolean isEmpty = valueOfConditionField == null || (valueOfConditionField instanceof String
          && ((String) valueOfConditionField).equals(""));

      if (isEmpty && conditionOperator == EQUAL_TO
          || !isEmpty && conditionOperator == NOT_EQUAL_TO) {
        return true;
      }

    } else if (conditionPattern == TRUE || conditionPattern == FALSE) {

      conditionValueStringMustNotSet();
      conditionValueFieldMustNotSet();

      if (valueOfConditionField != null && !(valueOfConditionField instanceof Boolean)) {
        throw new EclibRuntimeException("The data type of conditionPropertyPath must be boolean");
      }

      Boolean bl = (Boolean) valueOfConditionField;

      boolean validWhenBooleanTrue = (conditionOperator == EQUAL_TO && bl != null && bl)
          || (conditionOperator == NOT_EQUAL_TO && (bl == null || !bl));
      boolean validWhenBooleanFalse = (conditionOperator == EQUAL_TO && bl != null && !bl)
          || (conditionOperator == NOT_EQUAL_TO && (bl == null || bl));

      return conditionPattern == TRUE ? validWhenBooleanTrue : validWhenBooleanFalse;

    } else if (conditionPattern == VALUE_OF_PROPERTY_PATH) {

      conditionValueStringMustNotSet();

      Object valueOfConditionValueField = getValue(instance, conditionValuePropertyPath);

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

      if (contains && conditionOperator == EQUAL_TO
          || !contains && conditionOperator == NOT_EQUAL_TO) {
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
      if (contains && conditionOperator == EQUAL_TO
          || !contains && conditionOperator == NOT_EQUAL_TO) {
        return true;
      }
    }

    return false;
  }

  /**
   * Is called when {@code validatesWhenConditionNotSatisfied} is {@code true}.
   * 
   * <p>It's supposed to be overridden by child classes.
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
    if (!Arrays.asList(conditionValuePropertyPath)
        .contains(EclibCoreConstants.VALIDATOR_PARAMETER_NULL)) {
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
