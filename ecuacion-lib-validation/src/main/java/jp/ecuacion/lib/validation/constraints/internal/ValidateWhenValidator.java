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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import jp.ecuacion.lib.core.util.PropertyPathUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValueState;
import org.jspecify.annotations.Nullable;

public abstract class ValidateWhenValidator<A extends Annotation, T> extends ClassValidator<A, T> {
  private String conditionPropertyPath = "";
  // Put anything to avoid null error.
  private ConditionValue conditionPattern = ConditionValue.EMPTY;
  // Put anything to avoid null error.
  private ConditionOperator conditionOperator = ConditionOperator.EQUAL_TO;
  private String[] conditionValueString = new String[] {};
  private String conditionValueRegexp = "";
  // Compiled once here rather than per isValid() call: the regexp itself is annotation
  // (developer) input, immutable after initialize(), so caching it is safe (see ClassValidator's
  // javadoc on why per-call state must not be cached in a field, which does not apply here).
  private @Nullable Pattern compiledConditionValueRegexp;
  private String conditionValuePropertyPath = "";
  private boolean[] conditionValueBoolean = new boolean[] {};
  private ConditionValueState conditionValueState = ConditionValueState.UNSPECIFIED;
  protected boolean validatesWhenConditionNotSatisfied;

  public static final String CONDITION_PROPERTY_PATH = "conditionPropertyPath";
  public static final String CONDITION_PROPERTY_PATH_ITEM_NAME_KEY =
      "conditionPropertyPathItemNameKey";
  public static final String CONDITION_PROPERTY_PATH_ITEM_NAME = "conditionPropertyPathItemName";
  public static final String CONDITION_VALUE = "conditionValue";
  public static final String CONDITION_OPERATOR = "conditionOperator";
  public static final String CONDITION_VALUE_STRING = "conditionValueString";
  public static final String CONDITION_VALUE_PROPERTY_PATH = "conditionValuePropertyPath";
  public static final String CONDITION_VALUE_BOOLEAN = "conditionValueBoolean";
  public static final String CONDITION_VALUE_STATE = "conditionValueState";

  public static final String DISPLAY_STRING_OF_CONDITION_VALUE = "displayStringOfConditionValue";
  public static final String CONDITION_VALUE_PROPERTY_PATH_DISPLAY_STRING_PROPERTY_PATH =
      "conditionValueDisplayStringPropertyPath";
  public static final String VALIDATES_WHEN_CONDITION_NOT_SATISFIED =
      "validatesWhenConditionNotSatisfied";

  public void initialize(String message, String[] propertyPath, String conditionPropertyPath,
      ConditionValue conditionPattern, ConditionOperator conditionOperator,
      String[] conditionValueString, String conditionValuePattern,
      String conditionValuePropertyPath, boolean[] conditionValueBoolean,
      ConditionValueState conditionValueState, boolean validatesWhenConditionNotSatisfied) {
    super.initialize(message, propertyPath);

    this.conditionPropertyPath = conditionPropertyPath;
    this.conditionPattern = resolveConditionValue(conditionPattern, conditionValueString,
        conditionValuePattern, conditionValuePropertyPath, conditionValueBoolean,
        conditionValueState);
    this.conditionOperator = conditionOperator;
    this.conditionValueString = conditionValueString;
    this.conditionValueRegexp = conditionValuePattern;
    this.compiledConditionValueRegexp =
        conditionValuePattern.isEmpty() ? null : Pattern.compile(conditionValuePattern);
    this.conditionValuePropertyPath = conditionValuePropertyPath;
    this.conditionValueBoolean = conditionValueBoolean;
    this.conditionValueState = conditionValueState;
    this.validatesWhenConditionNotSatisfied = validatesWhenConditionNotSatisfied;
  }

  /**
   * Resolves the effective {@code ConditionValue}, inferring it from whichever of
   * {@code conditionValueString} / {@code conditionValuePatternRegexp} /
   * {@code conditionValuePropertyPath} / {@code conditionValueBoolean} /
   * {@code conditionValueState} is set when the annotation left {@code conditionValue}
   * unspecified.
   *
   * <p>Also used by {@link ValidateWhenValidatorMessageParameterCreator} so that error messages
   *     reflect the same resolution, since {@code ConstraintViolation} attributes carry the
   *     raw (unresolved) annotation value rather than this validator's resolved field.</p>
   *
   * @param conditionValue the raw {@code conditionValue} annotation element
   * @param conditionValueString the raw {@code conditionValueString} annotation element
   * @param conditionValuePatternRegexp the raw {@code conditionValuePatternRegexp} annotation
   *     element
   * @param conditionValuePropertyPath the raw {@code conditionValuePropertyPath} annotation
   *     element
   * @param conditionValueBoolean the raw {@code conditionValueBoolean} annotation element
   * @param conditionValueState the raw {@code conditionValueState} annotation element
   * @return the resolved, concrete {@code ConditionValue}
   */
  public static ConditionValue resolveConditionValue(ConditionValue conditionValue,
      String[] conditionValueString, String conditionValuePatternRegexp,
      String conditionValuePropertyPath, boolean[] conditionValueBoolean,
      ConditionValueState conditionValueState) {
    if (conditionValue != ConditionValue.UNSPECIFIED) {
      return conditionValue;
    }

    boolean stringSet = !Arrays.asList(conditionValueString)
        .contains(EclibValidationConstants.VALIDATOR_PARAMETER_NULL);
    boolean patternSet = !conditionValuePatternRegexp.isEmpty();
    boolean propertyPathSet = !conditionValuePropertyPath.isEmpty();
    boolean booleanSet = conditionValueBoolean.length > 0;
    boolean stateSet = conditionValueState != ConditionValueState.UNSPECIFIED;

    int numSet = (stringSet ? 1 : 0) + (patternSet ? 1 : 0) + (propertyPathSet ? 1 : 0)
        + (booleanSet ? 1 : 0) + (stateSet ? 1 : 0);

    if (numSet > 1) {
      throw new RuntimeException(
          "You cannot set more than one of 'conditionValueString', "
              + "'conditionValuePatternRegexp', 'conditionValuePropertyPath', "
              + "'conditionValueBoolean' and 'conditionValueState' at the same time.");
    }

    if (numSet == 0) {
      throw new RuntimeException(
          "'conditionValue' must be set when none of 'conditionValueString', "
              + "'conditionValuePatternRegexp', 'conditionValuePropertyPath', "
              + "'conditionValueBoolean' or 'conditionValueState' is set.");
    }

    if (stringSet) {
      return ConditionValue.STRING;
    } else if (patternSet) {
      return ConditionValue.PATTERN;
    } else if (propertyPathSet) {
      return ConditionValue.VALUE_OF_PROPERTY_PATH;
    } else if (booleanSet) {
      // When both true and false happen to be set, the first element wins.
      return conditionValueBoolean[0] ? ConditionValue.TRUE : ConditionValue.FALSE;
    } else {
      // ConditionValueState constant names are kept identical to their ConditionValue
      // counterparts (NULL / NOT_NULL / EMPTY / NOT_EMPTY) so this mapping is safe.
      return ConditionValue.valueOf(conditionValueState.name());
    }
  }

  protected abstract boolean isValid(Object valueOfField);

  /**
   * Executes validation check.
   */
  @Override
  public boolean internalIsValid(Object instance, Object[] valuesOfPropertyPaths,
      @Nullable ConstraintValidatorContext context) {

    boolean satisfiesCondition = getSatisfiesCondition(instance);

    for (int i = 0; i < propertyPaths.length; i++) {
      boolean result =
          isValidForSinglePropertyPath(satisfiesCondition, valuesOfPropertyPaths[i]);

      if (!result) {
        return false;
      }
    }

    return true;
  }

  protected boolean isValidForSinglePropertyPath(boolean satisfiesCondition,
      Object valueOfField) {
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

  protected boolean getSatisfiesCondition(Object instance) {
    Object valueOfConditionPropertyPath =
        PropertyPathUtil.getValue(instance, conditionPropertyPath);

    return switch (conditionPattern) {
      case NULL, NOT_NULL -> checkNull(valueOfConditionPropertyPath);
      case EMPTY, NOT_EMPTY -> checkEmpty(valueOfConditionPropertyPath);
      case TRUE, FALSE -> checkBoolean(valueOfConditionPropertyPath);
      case STRING -> checkString(valueOfConditionPropertyPath);
      case PATTERN -> checkPattern(valueOfConditionPropertyPath);
      case VALUE_OF_PROPERTY_PATH -> checkValueOfPropertyPath(instance,
          valueOfConditionPropertyPath);
      // Unreachable: resolveConditionValue() replaces UNSPECIFIED before it is ever stored
      // in conditionPattern. Kept only because the switch must be exhaustive.
      case ConditionValue.UNSPECIFIED -> throw new AssertionError(
          "conditionPattern must have been resolved to a concrete value in initialize().");
    };
  }

  private boolean checkNull(@Nullable Object valueOfConditionPropertyPath) {
    conditionValueStringMustNotSet();
    conditionValueRegexpMustNotSet();
    conditionValuePropertyPathMustNotSet();
    conditionValueBooleanMustNotSet();
    conditionValueStateMustMatchOrNotSet();

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
    conditionValueBooleanMustNotSet();
    conditionValueStateMustMatchOrNotSet();

    boolean isEmpty = StringUtil.isObjectNullOrEmpty(valueOfConditionPropertyPath);

    // conditionPattern EMPTY means "empty", NOT_EMPTY means "not empty".
    // conditionOperator then further modifies the direction.
    boolean patternMatchesEmpty = conditionPattern == ConditionValue.EMPTY;
    boolean conditionSatisfied = patternMatchesEmpty ? isEmpty : !isEmpty;
    return (conditionSatisfied && conditionOperator == EQUAL_TO)
        || (!conditionSatisfied && conditionOperator == NOT_EQUAL_TO);
  }

  private boolean checkBoolean(@Nullable Object valueOfConditionPropertyPath) {
    conditionValueStringMustNotSet();
    conditionValueRegexpMustNotSet();
    conditionValuePropertyPathMustNotSet();
    conditionValueStateMustNotSet();
    conditionValueBooleanMustMatchOrNotSet();

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
    conditionValueBooleanMustNotSet();
    conditionValueStateMustNotSet();

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
    conditionValueBooleanMustNotSet();
    conditionValueStateMustNotSet();

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

    Matcher m = Objects.requireNonNull(compiledConditionValueRegexp).matcher(s);

    boolean satisfies = m.find();
    return (satisfies && conditionOperator == EQUAL_TO)
        || (!satisfies && conditionOperator == NOT_EQUAL_TO);
  }

  private boolean checkValueOfPropertyPath(Object instance,
      @Nullable Object valueOfConditionPropertyPath) {
    conditionValueStringMustNotSet();
    conditionValueRegexpMustNotSet();
    conditionValueBooleanMustNotSet();
    conditionValueStateMustNotSet();

    Object valueOfConditionValueField =
        PropertyPathUtil.getValue(instance, conditionValuePropertyPath);

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

  private void conditionValueBooleanMustNotSet() {
    if (conditionValueBoolean.length > 0) {
      throw new RuntimeException(
          "You cannot set 'conditionValueBoolean' when conditionValue is not 'TRUE' or 'FALSE'.");
    }
  }

  private void conditionValueStateMustNotSet() {
    if (conditionValueState != ConditionValueState.UNSPECIFIED) {
      throw new RuntimeException("You cannot set 'conditionValueState' when conditionValue is "
          + "'STRING', 'PATTERN', 'VALUE_OF_PROPERTY_PATH', 'TRUE' or 'FALSE'.");
    }
  }

  /**
   * Checked from {@code checkBoolean()}: {@code conditionValueBoolean} corresponds to both
   * {@code TRUE} and {@code FALSE}, so unlike the other {@code MustNotSet} guards it is allowed
   * to be set here — it just must agree with the already-resolved {@code conditionPattern}
   * when it is.
   */
  private void conditionValueBooleanMustMatchOrNotSet() {
    if (conditionValueBoolean.length > 0
        && conditionValueBoolean[0] != (conditionPattern == TRUE)) {
      throw new RuntimeException("'conditionValueBoolean' (" + conditionValueBoolean[0]
          + ") conflicts with 'conditionValue' (" + conditionPattern + ").");
    }
  }

  /**
   * Checked from {@code checkNull()} / {@code checkEmpty()}: {@code conditionValueState}
   * corresponds to all of {@code NULL} / {@code NOT_NULL} / {@code EMPTY} / {@code NOT_EMPTY},
   * so unlike the other {@code MustNotSet} guards it is allowed to be set here — it just must
   * agree with the already-resolved {@code conditionPattern} when it is.
   */
  private void conditionValueStateMustMatchOrNotSet() {
    if (conditionValueState != ConditionValueState.UNSPECIFIED
        && conditionValueState != ConditionValueState.valueOf(conditionPattern.name())) {
      throw new RuntimeException("'conditionValueState' (" + conditionValueState
          + ") conflicts with 'conditionValue' (" + conditionPattern + ").");
    }
  }
}
