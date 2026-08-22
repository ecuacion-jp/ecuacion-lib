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
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Checks if the {@code String}, {@code java.io.File} or {@code java.nio.file.Path}
 *     annotated points to an existing directory.
 *
 * <p>It's invalid when the path points to a regular file, or to nothing.</p>
 *
 * @see DirExistsValidator
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {DirExistsValidator.class})
public @interface DirExists {

  /**
   * Returns message ID.
   *
   * @return message ID
   */
  String message() default "{jp.ecuacion.lib.validation.constraints.DirExists.message}";

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
