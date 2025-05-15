package jp.ecuacion.lib.core.util;

import jp.ecuacion.lib.core.jakartavalidation.validator.ConditionalNotEmpty;

@ConditionalNotEmpty(field = "value", conditionField = "conditionValue", conditionValue = "abc")
public class Test91_01__ObjWithClassValidator {
  public String conditionValue = "abc";
  public String value = null;
}