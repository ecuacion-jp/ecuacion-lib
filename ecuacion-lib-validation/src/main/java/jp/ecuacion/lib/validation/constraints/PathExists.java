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
 *     annotated points to an existing regular file or directory.
 *
 * <p>It's invalid only when nothing exists at the path.</p>
 *
 * <p><b>Do not annotate a field fed by untrusted (e.g. end-user) input.</b> Since the pass/fail
 *     result is observable per request, an attacker could use it as an oracle to enumerate which
 *     paths exist on the server's file system (e.g. probing {@code /etc/passwd} or internal
 *     application paths). Use it only for trusted input such as configuration values or
 *     administrator-entered paths.</p>
 *
 * @see PathExistsValidator
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {PathExistsValidator.class})
public @interface PathExists {

  /**
   * Returns message ID.
   *
   * @return message ID
   */
  String message() default "{jp.ecuacion.lib.validation.constraints.PathExists.message}";

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
