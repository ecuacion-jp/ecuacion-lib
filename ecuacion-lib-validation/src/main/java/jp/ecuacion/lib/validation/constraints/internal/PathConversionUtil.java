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

import java.io.File;
import java.nio.file.Path;

/**
 * Provides the conversion logic shared by validators which accept
 *     {@code String}, {@code java.io.File} and {@code java.nio.file.Path} valued fields
 *     representing a file system path.
 */
public final class PathConversionUtil {

  private PathConversionUtil() {}

  /**
   * Converts {@code value} to {@code java.nio.file.Path}.
   *
   * @param value a non-{@code null} value of type {@code String}, {@code java.io.File}
   *     or {@code java.nio.file.Path}.
   * @return the converted {@code Path}.
   */
  public static Path toPath(Object value) {
    if (value instanceof Path path) {
      return path;

    } else if (value instanceof File file) {
      return file.toPath();

    } else if (value instanceof String string) {
      return Path.of(string);
    }

    throw new RuntimeException(
        "The type of the annotated value is unexpected. It needs to be one of "
            + "String, java.io.File or java.nio.file.Path. type: " + value.getClass());
  }
}
