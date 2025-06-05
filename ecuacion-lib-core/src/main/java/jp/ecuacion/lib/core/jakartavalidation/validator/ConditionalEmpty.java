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
package jp.ecuacion.lib.core.jakartavalidation.validator;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.jakartavalidation.validator.ConditionalEmpty.ConditionalEmptyList;
import jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern;

/**
 * Checks if specified {@code field} is empty 
 *     only when {@code conditionField} has specified value.
 *     
 * <p>There are 4 ways to specify the value of {@code conditionField}.</p>
 * 
 * <ol>
 * <li>{@code conditionValue}: You can set the specified value directly 
 *     to {@code conditionValue} parameter.
 *     This can be used only when the datatype of {@code conditionField} is String.</li>
 * <li>{@code conditionValueIsEmpty}: You can set {@code true} to this parameter 
 *     means that the value of {@code conditionField} is empty.</li>
 * <li>{@code conditionValueIsNotEmpty}: You can set {@code true} to this parameter 
 *     means that the value of {@code conditionField} is not empty.</li>
 * <li>{@code fieldHoldingConditionValue}: You can set the field name to this parameter 
 *     means that the value of {@code fieldHoldingConditionValue} is the specified value.<br>
 *     (the datatype of {@code fieldHoldingConditionValue} is always an array)</li>
 * </ol>
 */
@PlacedAtClass
@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(ConditionalEmptyList.class)
@Documented
@Constraint(validatedBy = {ConditionalEmptyValidator.class})
public @interface ConditionalEmpty {

  /** 
   * Validated field.
   * 
   * <p>Its name is {@code propertyPath}, not {@code field} 
   *     because basically you set field names (like 'name'), 
   *     but you can also set a field in a bean (like 'dept.name').</p>
   * 
   * @return propertyPath
   */
  String[] propertyPath();

  /**
   * Conditional field. Validation check is executed 
   * only when the value of this field is equal to the value 
   * which {@code fieldWhichHoldsConditionalValue} holds.
   * 
   * <p>Its name is {@code conditionPropertyPath}, not {@code conditionField} 
   *     because basically you set field names (like 'name'), 
   *     but you can also set a field in a bean (like 'dept.name').</p>
   *     
   * @return condition field name
   */
  String conditionPropertyPath();

  /**
   * Specifies how to determine condition is valid.
   * 
   * <p>For example, we can say condition is satisfied 
   *     when the value of conditionField is not empty, 
   *     or the value of conditionField is equal to "a".<br>
   *     This field specifies how to determine that the condition is satisfied.</p>
   * 
   * @return ConditionPattern
   */
  ConditionPattern conditionPattern();

  /**
   * Specifies condition value string.
   * 
   * <p>This is used when {@code HowToDetermineConditionIsValid} is either 
   *     {@code stringValueOfConditionFieldIsEqualTo} 
   *     or {@code stringValueOfConditionFieldIsNotEqualTo}.
   *     Otherwise it must be unset.</p>
   * 
   * <p>When {@code stringValueOfConditionFieldIsEqualTo} is selected,
   *     a condition is considered to be satisfied 
   *     if one of the values of this field is equal to the value of conditionField.<br>
   *     When {@code stringValueOfConditionFieldIsNotEqualTo} is selected,
   *     a condition is considered to be satisfied 
   *     if all of the values of this field is NOT equal to the value of conditionField.<br>
   * 
   * <p>You can use {@code stringValueOfConditionFieldIsEqualTo}
   *     and {@code stringValueOfConditionFieldIsNotEqualTo} 
   *     only when the datatype of conditionField is string. 
   *     Otherwise you need to choose other choice.</p>
   * 
   * @return an array of string values
   */
  String[] conditionValueString() default EclibCoreConstants.VALIDATOR_PARAMETER_NULL;

  /**
   * Specifies condition value field.
   * 
   * <p>This is used when {@code HowToDetermineConditionIsValid} is either 
   *     {@code valueOfConditionFieldIsEqualToValueOf} 
   *     or {@code valueOfConditionFieldIsNotEqualToValueOf}.
   *     Otherwise it must be unset.</p>
   * 
   * <p>The datatype of the field specifies {@code conditionValueField} can be an array.</p>
   * 
   * <p>When {@code valueOfConditionFieldIsEqualToValueOf} is selected,
   *     a condition is considered to be satisfied 
   *     if one of the values of the field {@code conditionValueField} specifies 
   *     is equal to the value of conditionField.<br>
   *     When {@code valueOfConditionFieldIsNotEqualToValueOf} is selected,
   *     a condition is considered to be satisfied 
   *     if all of the values of the field {@code conditionValueField} specifies 
   *     is NOT equal to the value of conditionField.<br>
   * 
   * <p>Its name is {@code conditionValuePropertyPath}, not {@code conditionValueField} 
   *     because basically you set field names (like 'name'), 
   *     but you can also set a field in a bean (like 'dept.name').</p>
   *     
   * @return an array of string values
   */
  String conditionValuePropertyPath() default EclibCoreConstants.VALIDATOR_PARAMETER_NULL;

  /**
   * Specifies a field which holds the display name of condition value.
   * 
   * <p>It can be an array datatype which has multiple values.<br>
   *     When the value is new String[] {""}, the condionValue specified is displayed
   *     as a part of an error message.</p>
   *     
   * <p>Its name is {@code valueOfConditionValuePropertyPathForDisplay}, 
   * not {@code valueOfConditionValueFieldForDisplay} 
   *     because basically you set field names (like 'name'), 
   *     but you can also set a field in a bean (like 'dept.name').</p>     
   * 
   * @return String
   */
  String valueOfConditionValuePropertyPathForDisplay() default "";

  /**
   * Decides whether validation check is executed 
   *     when the value of {@code conditionField} is not equal to the specified value.
   * 
   * @return boolean
   */
  boolean notEmptyWhenConditionNotSatisfied() default false;

  /** 
   * Returns message ID.
   * 
   * @return message ID
   */
  String message() default "{jp.ecuacion.validation.constraints.ConditionalEmpty.message}";

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
   * Defines several {@link ConditionalEmpty} annotations on the same element.
   */
  @Target({TYPE})
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  public @interface ConditionalEmptyList {

    /**
     * Returns an array of ConditionalEmpty.
     * 
     * @return an array of ConditionalEmpty
     */
    ConditionalEmpty[] value();
  }
}
