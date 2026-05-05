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
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.NULL;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.TRUE;

import jakarta.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import org.jspecify.annotations.Nullable;

public abstract class ValidateWhenValidator<A extends Annotation, T> extends ClassValidator<A, T> {
  private String conditionPropertyPath = "";
  // Put anything to avoid null error.
  private ConditionValue conditionPattern = ConditionValue.EMPTY;
  // Put anything to avoid null error.
  private ConditionOperator conditionOperator = ConditionOperator.EQUAL_TO;
  private String[] conditionValueString = new String[] {};
  private String conditionValueRegexp = "";
  private String conditionValuePropertyPath = "";
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
  public static final String CONDITION_VALUE_PROPERTY_PATH_DISPLAY_STRING_PROPERTY_PATH =
      "conditionValueDisplayStringPropertyPath";
  public static final String VALIDATES_WHEN_CONDITION_NOT_SATISFIED =
      "validatesWhenConditionNotSatisfied";

  public void initialize(String message, String[] propertyPath, String conditionPropertyPath,
      ConditionValue conditionPattern, ConditionOperator conditionOperator,
      String[] conditionValueString, String conditionValuePattern,
      String conditionValuePropertyPath, boolean validatesWhenConditionNotSatisfied) {
    super.initialize(message, propertyPath);

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
  public boolean internalIsValid(Object instance, @Nullable ConstraintValidatorContext context) {

    procedureBeforeLoopForEachPropertyPath(instance);

    for (int i = 0; i < propertyPaths.length; i++) {
      boolean result = isValidForSinglePropertyPath(propertyPaths[i], valuesOfPropertyPaths[i]);

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
    Object valueOfConditionPropertyPath = ReflectionUtil.getValue(instance, conditionPropertyPath);

    return switch (conditionPattern) {
      case NULL, NOT_NULL -> checkNull(valueOfConditionPropertyPath);
      case EMPTY, NOT_EMPTY -> checkEmpty(valueOfConditionPropertyPath);
      case TRUE, FALSE -> checkBoolean(valueOfConditionPropertyPath);
      case STRING -> checkString(valueOfConditionPropertyPath);
      case PATTERN -> checkPattern(valueOfConditionPropertyPath);
      case VALUE_OF_PROPERTY_PATH -> checkValueOfPropertyPath(instance,
          valueOfConditionPropertyPath);
    };
  }

  private boolean checkNull(@Nullable Object valueOfConditionPropertyPath) {
    conditionValueStringMustNotSet();
    conditionValueRegexpMustNotSet();
    conditionValuePropertyPathMustNotSet();

    boolean isNull = valueOfConditionPropertyPath == null;

    // conditionPattern NULL means "null", NOT_NULL means "not null".
    // conditionOperator then further modifies the direction.
    boolean patternMatchesNull = conditionPattern == NULL;
    boolean conditionSatisfied = patternMatchesNull ? isNull : !isNull;
    return (conditionSatisfied && conditionOperator == EQUAL_TO)
        || (!conditionSatisfied && conditionOperator == NOT_EQUAL_TO);
  }

  private boolean checkEmpty(@Nullable Object valueOfConditionPropertyPath) {
    conditionValueStringMustNotSet();
    conditionValueRegexpMustNotSet();
    conditionValuePropertyPathMustNotSet();

    boolean isEmpty = StringUtil.isObjectNullOrEmpty(valueOfConditionPropertyPath);

    return (isEmpty && conditionOperator == EQUAL_TO)
        || (!isEmpty && conditionOperator == NOT_EQUAL_TO);
  }

  private boolean checkBoolean(@Nullable Object valueOfConditionPropertyPath) {
    conditionValueStringMustNotSet();
    conditionValueRegexpMustNotSet();
    conditionValuePropertyPathMustNotSet();

    if (valueOfConditionPropertyPath != null
        && !(valueOfConditionPropertyPath instanceof Boolean)) {
      throw new RuntimeException("The data type of conditionPropertyPath must be boolean");
    }

    Boolean bl = (Boolean) valueOfConditionPropertyPath;

    boolean validWhenBooleanTrue = (conditionOperator == EQUAL_TO && bl != null && bl)
        || (conditionOperator == NOT_EQUAL_TO && (bl == null || !bl));
    boolean validWhenBooleanFalse = (conditionOperator == EQUAL_TO && bl != null && !bl)
        || (conditionOperator == NOT_EQUAL_TO && (bl == null || bl));

    return conditionPattern == TRUE ? validWhenBooleanTrue : validWhenBooleanFalse;
  }

  private boolean checkString(@Nullable Object valueOfConditionPropertyPath) {
    conditionValueRegexpMustNotSet();
    conditionValuePropertyPathMustNotSet();

    Object conditionValue =
        valueOfConditionPropertyPath == null ? EclibValidationConstants.VALIDATOR_PARAMETER_NULL
            : valueOfConditionPropertyPath;

    // datatype of valueOfConditionField must be String.
    if (!(conditionValue instanceof String)) {
      throw new RuntimeException("'valueOfConditionPropertyPath' must be String.");
    }

    boolean contains = Arrays.asList(conditionValueString).contains(conditionValue);
    return (contains && conditionOperator == EQUAL_TO)
        || (!contains && conditionOperator == NOT_EQUAL_TO);
  }

  private boolean checkPattern(@Nullable Object valueOfConditionPropertyPath) {
    conditionValueStringMustNotSet();
    conditionValuePropertyPathMustNotSet();

    // Condition is considered not to be satisfied when valueOfConditionPropertyPath is null or
    // blank.
    // If you want the condition to be satisfied, add one more validator with conditionValue ==
    // EMPTY.
    if (StringUtil.isObjectNullOrEmpty(valueOfConditionPropertyPath)) {
      return false;
    }

    // datatype of valueOfConditionField must be String.
    if (!(valueOfConditionPropertyPath instanceof String s)) {
      throw new RuntimeException("'valueOfConditionPropertyPath' must be String.");
    }

    // Pattern must be set.
    if (conditionValueRegexp.isEmpty()) {
      throw new RuntimeException("'conditionValuePattern' must be set.");
    }

    Pattern p = Pattern.compile(conditionValueRegexp);
    Matcher m = p.matcher(s);

    boolean satisfies = m.find();
    return (satisfies && conditionOperator == EQUAL_TO)
        || (!satisfies && conditionOperator == NOT_EQUAL_TO);
  }

  private boolean checkValueOfPropertyPath(Object instance,
      @Nullable Object valueOfConditionPropertyPath) {
    conditionValueStringMustNotSet();
    conditionValueRegexpMustNotSet();

    Object valueOfConditionValueField =
        ReflectionUtil.getValue(instance, conditionValuePropertyPath);

    List<Object> valueListOfConditionValueField;
    if (valueOfConditionValueField instanceof Object[] arr) {
      valueListOfConditionValueField = new ArrayList<>(Arrays.asList(arr));
    } else {
      valueListOfConditionValueField = new ArrayList<>();
      valueListOfConditionValueField.add(valueOfConditionValueField);
    }

    // dataType difference check
    List<Object> nonnullList =
        valueListOfConditionValueField.stream().filter(v -> v != null).toList();
    Object firstValueOfConditionValueField = nonnullList.isEmpty() ? null : nonnullList.get(0);
    // if either of 2 values is null you cant check difference of datatype. So both is not null.
    if (valueOfConditionPropertyPath != null && firstValueOfConditionValueField != null) {
      Class<?> valueOfCf = valueOfConditionPropertyPath.getClass();
      Class<?> firstValueOfCvfList = firstValueOfConditionValueField.getClass();
      if (!firstValueOfCvfList.isAssignableFrom(valueOfCf)) {
        throw new RuntimeException(
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

    return (contains && conditionOperator == EQUAL_TO)
        || (!contains && conditionOperator == NOT_EQUAL_TO);
  }

  /**
   * Is called when {@code validatesWhenConditionNotSatisfied} is {@code true}.
   *
   * <p>The default implementation returns {@code !isValid(valueOfField)},
   *     which validates the inverse condition.
   *     Override this method if different behavior is needed.</p>
   *
   * @param valueOfField valueOfField
   * @return boolean
   */
  protected boolean isValidWhenConditionNotSatisfied(Object valueOfField) {
    return !isValid(valueOfField);
  }

  private void conditionValuePropertyPathMustNotSet() {
    // when prerequisite is satisfied, fieldHoldingConditionValue must be null
    if (!conditionValuePropertyPath.isEmpty()) {
      throw new RuntimeException("You cannot set 'conditionValuePropertyPath' when "
          + "'conditionValue' is not 'VALUE_OF_PROPERTY_PATH'.");
    }
  }

  private void conditionValueStringMustNotSet() {
    // when prerequisite is satisfied, conditionValueIsNotEmpty must be false
    if (!Arrays.asList(conditionValueString)
        .contains(EclibValidationConstants.VALIDATOR_PARAMETER_NULL)) {
      throw new RuntimeException(
          "You cannot set 'conditionValueString' when conditionValue is not 'STRING'.");
    }
  }

  private void conditionValueRegexpMustNotSet() {
    if (!conditionValueRegexp.isEmpty()) {
      throw new RuntimeException(
          "You cannot set 'conditionValuePattern' when conditionValue is not 'PATTERN'.");
    }
  }
}
