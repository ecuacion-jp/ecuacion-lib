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
import jp.ecuacion.lib.core.constant.LibCoreConstants;
import jp.ecuacion.lib.core.util.ObjectsUtil;

/** 
 * Is thorwn just like {@code RuntimeException} whose message contains "ecuacion" 
 * so that you can see the Ezxception is thrown from the library.
 */
public class LibRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new instance with {@code massage}.
   * 
   * @param message message. May be null, which meaans it has no messages.
   */
  public LibRuntimeException(@Nonnull String message) {
    super(message);

    ObjectsUtil.paramRequireNonNull(message);
  }

  /**
   * Constructs a new instance with {@code cause}.
   * 
   * @param cause cause
   */
  public LibRuntimeException(@Nonnull Throwable cause) {
    super(cause);

    ObjectsUtil.paramRequireNonNull(cause);
  }

  /**
   * Constructs a new instance with {@code massage} and {@code cause}.
   * 
   * @param message message
   * @param cause cause
   */
  public LibRuntimeException(@Nonnull String message, @Nonnull Throwable cause) {
    super(message, cause);

    ObjectsUtil.paramRequireNonNull(message);
    ObjectsUtil.paramRequireNonNull(cause);
  }

  /**
   * Returns message with a prefix added by the library.
   * 
   * @return message message. May be null, which means message is null.
   */
  @Override
  public String getMessage() {
    return super.getMessage() == null ? null
        : LibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX + super.getMessage();
  }
}
