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
import jp.ecuacion.lib.validation.constraints.LessThanOrEqualTo.LessThanOrEqualToList;
import jp.ecuacion.lib.validation.constraints.enums.TypeConversionFromString;

/**
 * Is valid when the value of {@code propertyPath} is 
 *     less than or equal to the value of {@code basisPropertyPath}.
 * 
 * <p>It returns valid when the value of {@code propertyPath} 
 *     or {@code basisPropertyPath} is {@code null} or blank string.</p>
 */
@PlacedAtClass
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(LessThanOrEqualToList.class)
@Documented
@Constraint(validatedBy = {LessThanOrEqualToValidator.class})
public @interface LessThanOrEqualTo {

  /** Is the propertyPath being compared. */
  String[] propertyPath();

  /** Is the propertyPath of the basis for comparison. */
  String basisPropertyPath();

  /** 
   * Offers conversion to designated type from string value before comparing values.
   * 
   * <p>Default value is {@code NONE}, which means no conversions executed.</p>
   */
  TypeConversionFromString typeConversionFromString() default TypeConversionFromString.NONE;

  /**
   * Stores DateTimeFormatter format for type conversion from string to Date.
   * It's referred only when typeConversionFromString == DATE.
   * 
   * <p>Default value is "yyyy-MM-dd".</p>
   */
  String typeConversionDateFormat() default "yyyy-MM-dd";

  /** 
   * Returns message ID.
   * 
   * @return message ID
   */
  String message() default "{jp.ecuacion.lib.validation.constraints.LessThanOrEqualTo.message}";

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
   * Defines several {@link LessThanOrEqualTo} annotations on the same element.
   */
  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface LessThanOrEqualToList {

    /**
     * Returns an array of Comparison.
     */
    LessThanOrEqualTo[] value();
  }
}
