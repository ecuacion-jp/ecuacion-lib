package jp.ecuacion.lib.core.jakartavalidation.validator.internal;

import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Arrays;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import org.apache.commons.lang3.StringUtils;

public abstract class ConditionalValidator {
  private String field;
  private String conditionField;
  private String[] conditionValue;
  private boolean conditionValueIsEmpty;
  private boolean conditionValueIsNotEmpty;
  private String fieldWhichHoldsConditionValue;

  public static final String CONDITION_FIELD = "conditionField";
  public static final String CONDITION_VALUE = "conditionValue";
  public static final String CONDITION_VALUE_IS_EMPTY = "conditionValueIsEmpty";
  public static final String CONDITION_VALUE_IS_NOT_EMPTY = "conditionValueIsNotEmpty";
  public static final String FIELD_WHICH_HOLDS_CONDITOION_VALUE = "fieldWhichHoldsConditionValue";

  // Used to create messages from conditional validators.
  public static final String CONDITION_VALUE_KIND = "conditionValueKind";
  public static final String VALUE_OF_CONDITION_FIELD_TO_VALIDATE =
      "valuesOfConditionFieldToValidate";

  public void initialize(String field, String conditionField, String[] conditionValue,
      boolean conditionValueIsEmpty, boolean conditionValueIsNotEmpty,
      String fieldWhichHoldsConditionValue) {
    this.field = field;
    this.conditionField = conditionField;
    this.conditionValue = conditionValue;
    this.conditionValueIsEmpty = conditionValueIsEmpty;
    this.conditionValueIsNotEmpty = conditionValueIsNotEmpty;
    this.fieldWhichHoldsConditionValue = fieldWhichHoldsConditionValue;
  }

  protected abstract boolean isValid(Object valueOfField);

  /**
   * Executes validation check.
   */
  public boolean isValid(Object instance, ConstraintValidatorContext context) {

    Object valueOfField = getFieldValue(field, instance, "validationTargetField");
    Object valueOfConditionField = getFieldValue(conditionField, instance, CONDITION_FIELD);

    if (!(conditionValue.length == 1
        && conditionValue[0].equals(EclibCoreConstants.VALIDATOR_PARAMETER_NULL))) {

      conditionValueIsEmptyMustBeFalse(CONDITION_VALUE);
      conditionValueIsNotEmptyMustBeFalse(CONDITION_VALUE);
      fieldWhichHoldsConditionalValueMustBeNull(CONDITION_VALUE);

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
      fieldWhichHoldsConditionalValueMustBeNull(CONDITION_VALUE_IS_EMPTY + " = true");

      if ((valueOfConditionField instanceof String
          && StringUtils.isEmpty((String) valueOfConditionField))
          || valueOfConditionField == null) {
        return isValid(valueOfField);
      }

    } else if (conditionValueIsNotEmpty) {

      fieldWhichHoldsConditionalValueMustBeNull(CONDITION_VALUE_IS_NOT_EMPTY + " = true");

      if ((valueOfConditionField instanceof String
          && !StringUtils.isEmpty((String) valueOfConditionField))
          || valueOfConditionField != null) {
        return isValid(valueOfField);
      }

    } else {
      // conditionValue == null && conditionValueIsNotEmpty == false

      if (fieldWhichHoldsConditionValue.equals(EclibCoreConstants.VALIDATOR_PARAMETER_NULL)) {
        // This case means validation check should be executed
        // when the value of conditionField is null
        if (valueOfConditionField == null) {
          return isValid(valueOfField);
        }

      } else {
        Object valueOfFieldWhichHoldsConditionalValue = getFieldValue(fieldWhichHoldsConditionValue,
            instance, "constFieldWhichHoldsConditionalValue");

        if ((valueOfConditionField == null && valueOfFieldWhichHoldsConditionalValue == null)
            || (valueOfConditionField != null
                && valueOfFieldWhichHoldsConditionalValue.equals(valueOfConditionField))) {
          return isValid(valueOfField);
        }
      }
    }

    // Reaching here means valueOfConditionField is not equal to conditionValue. skipped.
    return true;
  }

  public static Object getFieldValue(String fieldName, Object instance, String fieldKindName) {
    Field validationTargetField;

    try {
      validationTargetField = instance.getClass().getDeclaredField(fieldName);
      validationTargetField.setAccessible(true);

    } catch (Exception ex) {
      throw new EclibRuntimeException("'" + fieldKindName + "' field instance cannot be obtained "
          + "from the field name '" + fieldName + "'", ex);
    }

    try {
      return validationTargetField.get(instance);

    } catch (Exception ex) {
      throw new EclibRuntimeException(
          "The value of '" + fieldName + "' instance cannot be obtained.", ex);
    }
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

  private void fieldWhichHoldsConditionalValueMustBeNull(String prerequisite) {
    // when prerequisite is satisfied, fieldWhichHoldsConditionalValue must be null
    if (!fieldWhichHoldsConditionValue.equals(EclibCoreConstants.VALIDATOR_PARAMETER_NULL)) {
      throw new EclibRuntimeException("When you set '" + prerequisite + "', you cannot set '"
          + FIELD_WHICH_HOLDS_CONDITOION_VALUE + "'.");
    }
  }
}
