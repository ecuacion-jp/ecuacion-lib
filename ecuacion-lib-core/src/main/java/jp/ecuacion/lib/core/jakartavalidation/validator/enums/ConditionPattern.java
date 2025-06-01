package jp.ecuacion.lib.core.jakartavalidation.validator.enums;

/**
 * Enumerates how to determine condition is valid.
 */
public enum ConditionPattern {
  
  // Setting value of conditionValueString is needed.
  stringValueOfConditionFieldIsEqualTo, stringValueOfConditionFieldIsNotEqualTo, 

  // no additional value selection is needed.
  valueOfConditionFieldIsEmpty, valueOfConditionFieldIsNotEmpty, 

  // Setting value of conditionValueField is needed.
  valueOfConditionFieldIsEqualToValueOf, valueOfConditionFieldIsNotEqualToValueOf;
}
