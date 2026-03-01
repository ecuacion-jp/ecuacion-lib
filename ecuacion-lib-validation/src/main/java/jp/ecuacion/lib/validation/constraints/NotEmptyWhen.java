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

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jp.ecuacion.lib.core.jakartavalidation.annotation.PlacedAtClass;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.NotEmptyWhen.NotEmptyWhenList;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;

/**
 * Checks if specified {@code propertyPath} is not empty only when condition is satisfied.
 */
@PlacedAtClass
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(NotEmptyWhenList.class)
@Documented
@Constraint(validatedBy = {NotEmptyWhenValidator.class})
public @interface NotEmptyWhen {

  /** 
   * Is a validated field.
   * 
   * <p>Its name is {@code propertyPath}, not {@code field} 
   *     because basically you set field names (like 'name'), 
   *     but you can also set a field in a bean (like 'dept.name').</p>
   * 
   * <p>The datatype is an array in order to validate multiple values at once.</p>
   * 
   * @return propertyPath
   */
  String[] propertyPath();

  /**
   * Is a field, whose value determines whether the validation is executed or not.
   * 
   * <p>Its name is {@code conditionPropertyPath}, not {@code conditionField} 
   *     because basically you set field names (like 'name'), 
   *     but you can also set a field in a bean (like 'dept.name').</p>
   *     
   * @return conditionPropertyPath
   */
  String conditionPropertyPath();

  /**
   * Specifies a value used for determination whether validation is executed or not.
   * 
   * <p>ConditionValue has several types of values.<br><br>
   *     Values which designate fixed values (such as EMPTY, TRUE, FALSE) means 
   *     the same as empty (null for all datatypes and "" for String)
   *     and boolean true / false.<br>
   *     Values such as STRING, PATTERN and VALUE_OF_PROPERTY_PATH does not 
   *     directly express fixed values. So they need additional parameters to specify
   *     a variety of values.
   *     </p>
   * 
   * @return ConditionPattern
   */
  ConditionValue conditionValue();

  /**
   * Specifies the operator applied between the value of a condition field and the condition value
   *     To decide whether the condition is satisfied.
   * 
   * @return ConditionOperator
   */
  ConditionOperator conditionOperator() default ConditionOperator.EQUAL_TO;

  /**
   * Specifies condition value string.
   * 
   * <p>This is used when {@code ConditionValue} is {@code STRING}. Otherwise it must be unset.</p>
   * 
   * <p>The condition is satisfied when {@code ConditionOperator} is {@code EQUAL_TO} and
   *     one of the values of this field is equal to the value of conditionPropertyPath.<br>
   *     Otherwise when {@code ConditionOperator} is {@code NOT_EQUAL_TO}.</p>
   * 
   * <p>You can use {@code ConditionValue == STRING} 
   *     only when the datatype of conditionField is string. 
   *     Otherwise you need to choose other choice.</p>
   * 
   * @return an array of string values
   */
  String[] conditionValueString() default EclibValidationConstants.VALIDATOR_PARAMETER_NULL;

  /**
   * Specifies condition regular expression.
   * 
   * <p>This is used when {@code ConditionValue} is {@code PATTERN}. Otherwise it must be unset.</p>
   * 
   * <p>The condition is satisfied when {@code ConditionOperator} is {@code EQUAL_TO} and
   *     the value of conditionPropertyPath satisfies the regular expression.<br>
   *     Otherwise when {@code ConditionOperator} is {@code NOT_EQUAL_TO}.</p>
   *     
   * <p>You can use {@code ConditionValue == Pattern} 
   *     only when the datatype of conditionField is string. 
   *     Otherwise you need to choose other choice.</p>
   * 
   * @return regular expression
   */
  String conditionValueRegexp() default EclibValidationConstants.VALIDATOR_PARAMETER_NULL;

  /**
   * Specifies description for condition regular expression.
   */
  //@formatter:off  
  String conditionValuePatternDescription() 
      default EclibValidationConstants.VALIDATOR_PARAMETER_NULL;
  //@formatter:on

  /**
   * Specifies condition value field.
   * 
   * <p>This is used when {@code ConditionValue} is {@code VALUE_OF_PROPERTY_PATH}.
   *     Otherwise it must be unset.</p>
   * 
   * <p>The datatype of {@code conditionValuePropertyPath} can be the same as 
   *     that of {@code conditionPropertyPath}, or its array. 
   *     ({@code Collection} not supported)</p>
   * 
   * <p>The condition is satisfied when {@code ConditionOperator} is {@code EQUAL_TO} and
   *     the value of {@code conditionPropertyPath} is equal to the value [one of values] of 
   *     {@code conditionValuePropertyPath}.<br>
   *     Otherwise when {@code ConditionOperator} is {@code NOT_EQUAL_TO}.</p>
   * 
   * @return an array of string values
   */
  String conditionValuePropertyPath() default EclibValidationConstants.VALIDATOR_PARAMETER_NULL;

  /**
   * Specifies the the display string of condition value.
   * 
   * <p>It can be an array datatype which has multiple values.<br>
   *     When the value is new String[] {""}, the condionValue specified is displayed
   *     as a part of an error message.</p>
   * 
   * @return String
   */
  String displayStringOfConditionValue() default "";

  /**
   * Decides whether validation check is executed when the condition is not satisfied.
   * 
   * @return boolean
   */
  boolean emptyWhenConditionNotSatisfied() default false;

  /** 
   * Returns message ID.
   * 
   * @return message ID
   */
  String message() default "{jp.ecuacion.lib.validation.constraints.NotEmptyWhen.message}";

  /** 
   * Returns groups.
   * 
   * @return groups
   */
  Class<?>[] groups() default {};

  /** 
   * Returns payload.
   * 
   * @return payload
   */
  Class<? extends Payload>[] payload() default {};

  /**
   * Defines several {@link NotEmptyWhen} annotations on the same element.
   */
  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface NotEmptyWhenList {

    /**
     * Returns an array of NotEmptyWhen.
     * 
     * @return an array of NotEmptyWhen
     */
    NotEmptyWhen[] value();
  }
}
