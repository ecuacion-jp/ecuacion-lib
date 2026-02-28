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
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.PATTERN;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.STRING;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.TRUE;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.VALUE_OF_PROPERTY_PATH;

import jakarta.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public abstract class ConditionalValidator<A extends Annotation, T> extends ClassValidator<A, T> {
  private String conditionPropertyPath;
  private ConditionValue conditionPattern;
  private ConditionOperator conditionOperator;
  private String[] conditionValueString;
  private String conditionValueRegexp;
  private String conditionValuePropertyPath;
  private boolean validatesWhenConditionNotSatisfied;

  private boolean satisfiesCondition = false;

  public static final String CONDITION_PROPERTY_PATH = "conditionPropertyPath";
  public static final String CONDITION_PROPERTY_PATH_ITEM_NAME_KEY =
      "conditionPropertyPathItemNameKey";
  public static final String CONDITION_PROPERTY_PATH_ITEM_NAME = "conditionPropertyPathItemName";
  public static final String CONDITION_VALUE = "conditionValue";
  public static final String CONDITION_OPERATOR = "conditionOperator";
  public static final String CONDITION_VALUE_STRING = "conditionValueString";
  public static final String CONDITION_VALUE_PROPERTY_PATH = "conditionValuePropertyPath";

  public static final String DISPLAY_STRING_OF_CONDITION_VALUE = "displayStringOfConditionValue";
  public static final String VALIDATES_WHEN_CONDITION_NOT_SATISFIED =
      "validatesWhenConditionNotSatisfied";

  public void initialize(String[] propertyPath, String conditionPropertyPath,
      ConditionValue conditionPattern, ConditionOperator conditionOperator,
      String[] conditionValueString, String conditionValuePattern,
      String conditionValuePropertyPath, boolean validatesWhenConditionNotSatisfied) {
    super.initialize(propertyPath);

    this.conditionPropertyPath = conditionPropertyPath;
    this.conditionPattern = conditionPattern;
    this.conditionOperator = conditionOperator;
    this.conditionValueString = conditionValueString;
    this.conditionValueRegexp = conditionValuePattern;
    this.conditionValuePropertyPath = conditionValuePropertyPath;
    this.validatesWhenConditionNotSatisfied = validatesWhenConditionNotSatisfied;
  }

  protected abstract boolean isValid(Object valueOfField);

  /**
   * Executes validation check.
   */
  @Override
  public boolean internalIsValid(Object instance, ConstraintValidatorContext context) {

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

    Object valueOfConditionPropertyPath = getValue(instance, conditionPropertyPath);

    if (conditionPattern == EMPTY) {

      conditionValueStringMustNotSet();
      conditionValueRegexpMustNotSet();
      conditionValueFieldMustNotSet();

      boolean isEmpty =
          valueOfConditionPropertyPath == null || (valueOfConditionPropertyPath instanceof String
              && ((String) valueOfConditionPropertyPath).equals(""));

      if (isEmpty && conditionOperator == EQUAL_TO
          || !isEmpty && conditionOperator == NOT_EQUAL_TO) {
        return true;
      }

    } else if (conditionPattern == TRUE || conditionPattern == FALSE) {

      conditionValueStringMustNotSet();
      conditionValueRegexpMustNotSet();
      conditionValueFieldMustNotSet();

      if (valueOfConditionPropertyPath != null
          && !(valueOfConditionPropertyPath instanceof Boolean)) {
        throw new EclibRuntimeException("The data type of conditionPropertyPath must be boolean");
      }

      Boolean bl = (Boolean) valueOfConditionPropertyPath;

      boolean validWhenBooleanTrue = (conditionOperator == EQUAL_TO && bl != null && bl)
          || (conditionOperator == NOT_EQUAL_TO && (bl == null || !bl));
      boolean validWhenBooleanFalse = (conditionOperator == EQUAL_TO && bl != null && !bl)
          || (conditionOperator == NOT_EQUAL_TO && (bl == null || bl));

      return conditionPattern == TRUE ? validWhenBooleanTrue : validWhenBooleanFalse;

    } else if (conditionPattern == STRING) {

      conditionValueRegexpMustNotSet();
      conditionValueFieldMustNotSet();

      if (valueOfConditionPropertyPath == null) {
        valueOfConditionPropertyPath = EclibValidationConstants.VALIDATOR_PARAMETER_NULL;
      }

      // datatype of valueOfConditionField must be String.
      if (!(valueOfConditionPropertyPath instanceof String)) {
        throw new EclibRuntimeException("'valueOfConditionPropertyPath' must be String.");
      }

      boolean contains = Arrays.asList(conditionValueString).contains(valueOfConditionPropertyPath);
      if (contains && conditionOperator == EQUAL_TO
          || !contains && conditionOperator == NOT_EQUAL_TO) {
        return true;
      }

    } else if (conditionPattern == PATTERN) {

      conditionValueStringMustNotSet();
      conditionValueFieldMustNotSet();

      // Condition is considered not to be satisfied when valueOfConditionPropertyPath is null or
      // blank.
      // If you want the condition to be satisfied, add one more validator with conditionValue ==
      // EMPTY.
      if (valueOfConditionPropertyPath == null || (valueOfConditionPropertyPath instanceof String
          && StringUtils.isEmpty((String) valueOfConditionPropertyPath))) {
        return false;
      }

      // datatype of valueOfConditionField must be String.
      if (!(valueOfConditionPropertyPath instanceof String)) {
        throw new EclibRuntimeException("'valueOfConditionPropertyPath' must be String.");
      }

      // Pattern must be set.
      if (conditionValueRegexp.equals(EclibValidationConstants.VALIDATOR_PARAMETER_NULL)) {
        throw new EclibRuntimeException("'conditionValuePattern' must be set.");
      }

      Pattern p = Pattern.compile(conditionValueRegexp);
      Matcher m = p.matcher((String) valueOfConditionPropertyPath);

      boolean satisfies = m.find();
      if (satisfies && conditionOperator == EQUAL_TO
          || !satisfies && conditionOperator == NOT_EQUAL_TO) {
        return true;
      }

    } else if (conditionPattern == VALUE_OF_PROPERTY_PATH) {

      conditionValueStringMustNotSet();
      conditionValueRegexpMustNotSet();

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
      if (valueOfConditionPropertyPath != null && firstValueOfConditionValueField != null) {
        Class<?> valueOfCf = valueOfConditionPropertyPath.getClass();
        Class<?> firstValueOfCvfList = firstValueOfConditionValueField.getClass();
        if (!firstValueOfCvfList.isAssignableFrom(valueOfCf)) {
          throw new EclibRuntimeException(
              "Datatype not match. valueOfConditionField: " + valueOfConditionPropertyPath
                  + ", valueListOfConditionValueField.get(0): " + firstValueOfConditionValueField);
        }
      }

      // contains(null) cannot be used for list so change it to VALIDATOR_PARAMETER_NULL in advance.
      valueListOfConditionValueField
          .replaceAll(x -> x == null ? EclibValidationConstants.VALIDATOR_PARAMETER_NULL : x);

      boolean contains = (valueOfConditionPropertyPath == null && valueListOfConditionValueField
          .contains(EclibValidationConstants.VALIDATOR_PARAMETER_NULL))
          || (valueOfConditionPropertyPath != null
              && valueListOfConditionValueField.contains(valueOfConditionPropertyPath));

      if (contains && conditionOperator == EQUAL_TO
          || !contains && conditionOperator == NOT_EQUAL_TO) {
        return true;
      }

    } else {
      throw new EclibRuntimeException("Unexpected.");
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
    if (!conditionValuePropertyPath.equals(EclibValidationConstants.VALIDATOR_PARAMETER_NULL)) {
      throw new EclibRuntimeException("You cannot set 'conditionValuePropertyPath' when "
          + "'conditionValue' is not 'VALUE_OF_PROPERTY_PATH'.");
    }
  }

  private void conditionValueStringMustNotSet() {
    // when prerequisite is satisfied, conditionValueIsNotEmpty must be false
    if (!Arrays.asList(conditionValueString)
        .contains(EclibValidationConstants.VALIDATOR_PARAMETER_NULL)) {
      throw new EclibRuntimeException(
          "You cannot set 'conditionValueString' when conditionValue is not 'STRING'.");
    }
  }

  private void conditionValueRegexpMustNotSet() {
    if (!conditionValueRegexp.equals(EclibValidationConstants.VALIDATOR_PARAMETER_NULL)) {
      throw new EclibRuntimeException(
          "You cannot set 'conditionValuePattern' when conditionValue is not 'PATTERN'.");
    }
  }
}
