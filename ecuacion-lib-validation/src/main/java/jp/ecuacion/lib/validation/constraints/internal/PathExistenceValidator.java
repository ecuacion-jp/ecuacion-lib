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
package jp.ecuacion.lib.validation.constraints.internal;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Objects;
import jp.ecuacion.lib.core.util.StringUtil;
import org.jspecify.annotations.Nullable;

/**
 * Provides the common validation logic for path-existence validators
 *     ({@code FileExists}, {@code DirExists}, {@code PathExists}), which accept
 *     {@code String}, {@code java.io.File} and {@code java.nio.file.Path} valued fields.
 */
public abstract class PathExistenceValidator<A extends Annotation>
    implements ConstraintValidator<A, Object> {

  /**
   * Initializes an instance.
   *
   * <p>{@code @Nullable} on the parameter is for Eclipse null analysis compatibility;
   *     see package {@link jp.ecuacion.lib.validation.constraints} for details.</p>
   */
  @Override
  public void initialize(@Nullable A constraintAnnotation) {}

  /**
   * Checks the existence of the path represented by {@code value}.
   *
   * <p>{@code null} and empty string are valid following to the specification
   *     of Jakarta EE.</p>
   */
  @Override
  public boolean isValid(@Nullable Object value, @Nullable ConstraintValidatorContext context) {
    if (StringUtil.isObjectNullOrEmpty(value)) {
      return true;
    }

    return isValidPath(PathConversionUtil.toPath(Objects.requireNonNull(value)));
  }

  /**
   * Checks the existence condition specific to each subclass.
   *
   * @param path the path converted from the annotated value.
   * @return {@code true} if {@code path} satisfies the condition.
   */
  protected abstract boolean isValidPath(Path path);
}
