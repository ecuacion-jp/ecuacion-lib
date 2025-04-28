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
@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(ConditionalEmptyList.class)
@Documented
@Constraint(validatedBy = {ConditionalEmptyValidator.class})
public @interface ConditionalEmpty {

  /** 
   * Validated field.
   * 
   * @return field name
   */
  String[] field();

  /**
   * Conditional field. Validation check is executed 
   * only when the value of this field is equal to the value 
   * which {@code fieldWhichHoldsConditionalValue} holds.
   * 
   * @return condition field name
   */
  String conditionField();

  /**
   * Validation check is executed 
   *     only when the value {@code conditionField} holds is equal to to it.
   * 
   * <p>You can use this value when the datatype of {@code conditionField} is {@code String}.
   *     Otherwise you should use {@code fieldWhichHoldsConditionalValue}.</p>
   * 
   * <p>Multiple values can be set to the parameter. In that case validation check is executed
   *     when one of the values are equal to the value of conditionField.</p>
   * 
   * @return value
   */
  String[] conditionValue() default EclibCoreConstants.VALIDATOR_PARAMETER_NULL;

  /**
   * Validation check is executed
   *     only when it's true and the value {@code conditionField} holds is empty.
   *      
   * @return boolean
   */
  boolean conditionValueIsEmpty() default false;

  /**
   * Validation check is executed
   *     only when it's true and the value {@code conditionField} holds is not empty.
   *      
   * @return boolean
   */
  boolean conditionValueIsNotEmpty() default false;

  /**
   * Validation check is executed
   *     only when the value of the field specified by this parameter holds
   *     is equal to the value of {@code conditionField}.
   * 
   * @return value
   */
  String fieldHoldingConditionValue() default EclibCoreConstants.VALIDATOR_PARAMETER_NULL;

  /**
   * Decides whether validation check is executed 
   *     when the value of {@code conditionField} is not equal to the specified value.
   * 
   * @return boolean
   */
  boolean notEmptyForOtherValues() default false;

  /**
   * Specifies class part (= left part) of item ID.
   * 
   * @return String
   */
  String itemIdClass() default "";

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
