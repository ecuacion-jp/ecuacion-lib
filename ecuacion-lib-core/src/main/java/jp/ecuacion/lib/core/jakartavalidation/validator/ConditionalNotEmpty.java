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
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.jakartavalidation.validator.ConditionalNotEmpty.ConditionalNotEmptyList;

/**
 * Checks if specified {@code field} is not empty 
 *     when {@code conditionField} has the value 
 *     which is equal to {@code conditionValue}, 
 *     or the value {@code fieldWhichHoldsConditionalValue} has.
 * 
 * @see ConditionalNotEmptyValidator
 */
@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(ConditionalNotEmptyList.class)
@Documented
@Constraint(validatedBy = {ConditionalNotEmptyValidator.class})
public @interface ConditionalNotEmpty {

  /** 
   * Validated field.
   * 
   * @return field name
   */
  String field();

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
   * See {@code conditionField}.
   * 
   * <p>Originally 
   *     The datatype of a parameter of annotation can only be String and primitive datatypes.
   *     It cannot be {@code Object}. 
   * 
   * @return value
   */
  String fieldWhichHoldsConditionValue() default EclibCoreConstants.VALIDATOR_PARAMETER_NULL;

  /** 
   * Returns message ID.
   * 
   * @return message ID
   */
  String message() default "{jp.ecuacion.validation.constraints.ConditionalNotEmpty.message}";

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
   * Defines several {@link ConditionalNotEmpty} annotations on the same element.
   *
   * @see Pattern
   */
  @Target({TYPE})
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  public @interface ConditionalNotEmptyList {

    /**
     * Returns an array of ConditionalNotEmpty.
     * 
     * @return an array of ConditionalNotEmpty
     */
    ConditionalNotEmpty[] value();
  }
}
