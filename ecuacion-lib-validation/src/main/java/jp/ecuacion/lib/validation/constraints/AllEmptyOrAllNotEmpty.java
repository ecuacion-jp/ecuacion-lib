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
import jp.ecuacion.lib.validation.constraints.AllEmptyOrAllNotEmpty.AllEmptyOrAllNotEmptyList;

/**
 * Is valid when all of the values of {@code propertyPath} is empty or not empty.
 */
@PlacedAtClass
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AllEmptyOrAllNotEmptyList.class)
@Documented
@Constraint(validatedBy = {AllEmptyOrAllNotEmptyValidator.class})
public @interface AllEmptyOrAllNotEmpty {

  /** 
   * Is the array of propertyPath. 
   * The validation result is true when all of the values are empty or not empty.
   */
  String[] propertyPath();

  /** 
   * Returns message ID.
   */
  String message() default "{jp.ecuacion.lib.validation.constraints.AllEmptyOrAllNotEmpty.message}";

  /** 
   * Returns groups.
   */
  Class<?>[] groups() default {};

  /** 
   * Returns payload.
   */
  Class<? extends Payload>[] payload() default {};

  /**
   * Defines several {@link AllEmptyOrAllNotEmpty} annotations on the same element.
   */
  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface AllEmptyOrAllNotEmptyList {

    /**
     * Returns an array of Comparison.
     */
    AllEmptyOrAllNotEmpty[] value();
  }
}
