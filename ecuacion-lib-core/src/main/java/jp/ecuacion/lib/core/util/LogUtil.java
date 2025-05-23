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
package jp.ecuacion.lib.core.util;

import jakarta.annotation.Nullable;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.logging.ErrorLogger;

/**
 * Provides multiple-logger-used fixed procedures.
 */
public class LogUtil {
  private static ErrorLogger errLog = new ErrorLogger();

  /**
   * Prevents other classes from instantiating it.
   */
  private LogUtil() {}

  /**
  * Logs throwable.
  *
  * @param detailLog DetailLogger instance
  * @param throwable throwable
  */
  public static void logSystemError(
      @RequireNonnull DetailLogger detailLog, @RequireNonnull Throwable throwable) {
    logSystemError(detailLog, throwable, null);
  }

  /**
   * Logs throwable.
   * 
   * @param detailLog DetailLogger instance
   * @param throwable throwable
   * @param additionalMessage additionalMessage,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case o {@code null} no additional message is output.
   */
  public static void logSystemError(@RequireNonnull DetailLogger detailLog,
      @RequireNonnull Throwable throwable, @Nullable String additionalMessage) {
    ObjectsUtil.requireNonNull(detailLog, throwable);
    
    errLog.logSystemError(throwable, additionalMessage);
    detailLog.logSystemError(throwable, additionalMessage);
  }
}
