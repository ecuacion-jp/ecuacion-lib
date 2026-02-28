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
package jp.ecuacion.lib.core.jakartavalidation.bean;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jp.ecuacion.lib.core.jakartavalidation.annotation.PlacedAtClass;

/**
 * Checks if specified {@code itemPropertyPath} is empty only when condition is satisfied.
 */
@PlacedAtClass
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {AlwaysFalseValidator.class})
public @interface AlwaysFalse {


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
   * Returns message ID.
   * 
   * @return message ID
   */
  String message() default "jp.ecuacion.lib.validation.constraints.AlwaysFalse.message";

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
}
