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

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jp.ecuacion.lib.validation.constraints.PatternWithDescription.PatternWithDescriptionList;

/**
 * Checks if a string matches specified regular expression.
 * 
 * @see PatternWithDescriptionValidator
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(PatternWithDescriptionList.class)
@Documented
@Constraint(validatedBy = {PatternWithDescriptionValidator.class})
public @interface PatternWithDescription {

  /** 
   * Returns message ID.
   * 
   * @return message ID
   */
  String message() default 
      "{jp.ecuacion.lib.validation.constraints.PatternWithDescription.message}";

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
   * Stores a regular expression.
   * 
   * @return regular expression
   */
  String regexp();

  /**
   * Stores description ID to add description string to the message to users.
   * 
   * @return description ID
   */
  String descriptionId();

  /**
   * Defines several {@link Pattern} annotations on the same element.
   *
   * @see Pattern
   */
  @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  public @interface PatternWithDescriptionList {

    /**
     * Returns an array of PatternWithDescription.
     * 
     * @return an array of PatternWithDescription
     */
    PatternWithDescription[] value();
  }
}
