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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Is valid when the return value of the method is {@code true}.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {AssertTrueWithPropertyPathValidator.class})
public @interface AssertTrueWithPropertyPath {

  /** 
   * Is the array of propertyPath. 
   * The validation result is true when one of the values is not empty.
   */
  String[] propertyPath();

  /** 
   * Returns message ID.
   */
  // @formatter:off
  String message() 
      default "{jp.ecuacion.lib.validation.constraints.AssertTrueWithPropertyPath.message}";
  // @formatter:on

  /** 
   * Returns groups.
   */
  Class<?>[] groups() default {};

  /** 
   * Returns payload.
   */
  Class<? extends Payload>[] payload() default {};
}
