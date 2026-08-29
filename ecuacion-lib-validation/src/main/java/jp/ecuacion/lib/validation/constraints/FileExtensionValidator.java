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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.validation.constraints.internal.PathConversionUtil;
import org.jspecify.annotations.Nullable;

/**
 * Provides the validation logic for {@code FileExtension}.
 */
public class FileExtensionValidator implements ConstraintValidator<FileExtension, Object> {

  private String extensionWithoutDot = "";

  /**
   * Initializes an instance.
   *
   * <p>{@code @Nullable} on the parameter is for Eclipse null analysis compatibility;
   *     see package {@link jp.ecuacion.lib.validation.constraints} for details.</p>
   */
  @Override
  public void initialize(@Nullable FileExtension annotation) {
    Objects.requireNonNull(annotation);
    String value = annotation.value();
    extensionWithoutDot = value.startsWith(".") ? value.substring(1) : value;
  }

  /**
   * Checks if the file name extension of {@code value} matches the designated one.
   *
   * <p>{@code null} and empty string are valid following to the specification
   *     of Jakarta EE.</p>
   */
  @Override
  public boolean isValid(@Nullable Object value, @Nullable ConstraintValidatorContext context) {
    if (StringUtil.isObjectNullOrEmpty(value)) {
      return true;
    }

    Path path;
    try {
      path = PathConversionUtil.toPath(Objects.requireNonNull(value));
    } catch (InvalidPathException ex) {
      // A malformed path string (e.g. containing a NUL character) is not a valid file name,
      // so it fails validation rather than propagating as an unhandled exception.
      return false;
    }

    Path fileNamePath = path.getFileName();
    if (fileNamePath == null) {
      return false;
    }

    String fileName = fileNamePath.toString();
    int dotIndex = fileName.lastIndexOf('.');
    // No extension when there's no dot, or the dot is the first character (e.g. ".xlsx").
    if (dotIndex <= 0) {
      return false;
    }

    String actualExtension = fileName.substring(dotIndex + 1);
    return actualExtension.equalsIgnoreCase(extensionWithoutDot);
  }
}
