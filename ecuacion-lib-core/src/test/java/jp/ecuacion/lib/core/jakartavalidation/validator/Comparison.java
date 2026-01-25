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

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jp.ecuacion.lib.core.jakartavalidation.annotation.PlacedAtClass;
import jp.ecuacion.lib.core.jakartavalidation.validator.Comparison.ComparisonList;
import jp.ecuacion.lib.core.jakartavalidation.validator.enums.TypeConversionFromString;

/**
 * Checks if 2 specified {@code itemPropertyPaths} have assumed numrical comparison relation.
 */
@PlacedAtClass
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ComparisonList.class)
@Documented
@Constraint(validatedBy = {ConcreteComparisonValidator.class})
public @interface Comparison {

  /** Is the propertyPath being compared. */
  String[] propertyPath();

  /** Is the propertyPath of the basis for comparison. */
  String basisPropertyPath();

  /** 
   * Is true when you want to make valid 
   * in the case the value of {@code propertyPath} is less then {@code basisPropertyPath}. 
   */
  boolean isValidWhenLessThanBasis() default true;

  /** Is true when you want to make valid in the case two values are the same. */
  boolean allowsEqual() default true;

  /** 
   * Offers conversion to designated type from string value before comparing values.
   * 
   * <p>Default value is {@code NONE}, which means no conversions executed.</p>
   */
  TypeConversionFromString typeConversionFromString() default TypeConversionFromString.NONE;

  /** 
   * Returns message ID.
   * 
   * @return message ID
   */
  String message() default "{jp.ecuacion.validation.constraints.Comparison.message}";

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
   * Defines several {@link Comparison} annotations on the same element.
   */
  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface ComparisonList {

    /**
     * Returns an array of Comparison.
     */
    Comparison[] value();
  }
}
