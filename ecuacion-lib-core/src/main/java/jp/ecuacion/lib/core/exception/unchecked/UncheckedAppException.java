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
package jp.ecuacion.lib.core.exception.unchecked;

import jakarta.annotation.Nonnull;
import java.util.Objects;
import jp.ecuacion.lib.core.exception.checked.AppException;

/**
 * Wraps {@code AppException} and enables to throw AppException
 *  in overrided and no "throws AppException" signature method.
 *  
 *  <p>After throwed, 
 *  catched and procesed in library is exactly the same as {@code SingleAppException}.</p>
 */
public class UncheckedAppException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new instance with {@code AppException}.
   * 
   * @param ex appException
   */
  public UncheckedAppException(@Nonnull AppException ex) {
    super(ex);
    Objects.requireNonNull(ex);
  }
}
