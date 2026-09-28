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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValueState;

/**
 * Specifies one condition of a {@code When} validator (like {@link NotEmptyWhen}).
 *
 * <p>This annotation is used only as an element of the {@code conditions} parameter of
 *     {@code When} validators, which enables to specify multiple conditions.
 *     The validation is executed when <b>all</b> of the conditions are satisfied (AND).</p>
 *
 * <pre>
 * &#64;NotEmptyWhen(propertyPath = "remarks", conditions = {
 *     &#64;Condition(propertyPath = "type", valueString = "OTHER"),
 *     &#64;Condition(propertyPath = "status", valueState = ConditionValueState.NOT_EMPTY)})
 * </pre>
 *
 * <p>Each element corresponds to the {@code When} validator's parameter of the same name
 *     with the {@code condition} prefix (like {@code valueString} to
 *     {@code conditionValueString}) and has the same meaning.
 *     Exactly one of {@code valueString}, {@code valuePatternRegexp},
 *     {@code valuePropertyPath}, {@code valueBoolean} and {@code valueState} must be set.</p>
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Condition {

  /**
   * Is a field, whose value determines whether the condition is satisfied or not.
   *
   * <p>See {@link NotEmptyWhen#conditionPropertyPath()}.</p>
   *
   * @return propertyPath
   */
  String propertyPath();

  /**
   * Specifies the operator applied between the value of a condition field and the condition
   *     value to decide whether the condition is satisfied.
   *
   * <p>See {@link NotEmptyWhen#conditionOperator()}.</p>
   *
   * @return ConditionOperator
   */
  ConditionOperator operator() default ConditionOperator.EQUAL_TO;

  /**
   * Specifies condition value string.
   *
   * <p>See {@link NotEmptyWhen#conditionValueString()}.</p>
   *
   * @return an array of string values
   */
  String[] valueString() default EclibValidationConstants.VALIDATOR_PARAMETER_NULL;

  /**
   * Specifies condition regular expression.
   *
   * <p>See {@link NotEmptyWhen#conditionValuePatternRegexp()}.</p>
   *
   * @return regular expression
   */
  String valuePatternRegexp() default "";

  /**
   * Specifies description for condition regular expression.
   *
   * <p>See {@link NotEmptyWhen#conditionValuePatternDescription()}.</p>
   *
   * @return description
   */
  String valuePatternDescription() default "";

  /**
   * Specifies condition value field.
   *
   * <p>See {@link NotEmptyWhen#conditionValuePropertyPath()}.</p>
   *
   * @return propertyPath
   */
  String valuePropertyPath() default "";

  /**
   * Specifies condition value boolean.
   *
   * <p>See {@link NotEmptyWhen#conditionValueBoolean()}.</p>
   *
   * @return an array holding at most one boolean value
   */
  boolean[] valueBoolean() default {};

  /**
   * Specifies condition value state.
   *
   * <p>See {@link NotEmptyWhen#conditionValueState()}.</p>
   *
   * @return ConditionValueState
   */
  ConditionValueState valueState() default ConditionValueState.UNSPECIFIED;

  /**
   * Specifies the display string of the condition value.
   *
   * <p>See {@link NotEmptyWhen#conditionValueDisplayStringPropertyPath()}.</p>
   *
   * @return propertyPath
   */
  String valueDisplayStringPropertyPath() default "";
}
