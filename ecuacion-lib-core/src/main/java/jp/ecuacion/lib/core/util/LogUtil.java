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

import java.util.Objects;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.logging.ErrorLogger;
import org.jspecify.annotations.Nullable;

/**
 * Provides multiple-logger-used fixed procedures.
 */
public class LogUtil {
  private static final ErrorLogger errLog = new ErrorLogger();

  /**
   * Prevents other classes from instantiating it.
   */
  private LogUtil() {}

  /**
   * Logs a system error to both {@code ErrorLogger} and {@code DetailLogger}.
   *
   * <p>{@code ErrorLogger} logs a one-line message for surveillance,
   *     and {@code DetailLogger} logs the details including the stack trace.</p>
   *
   * @param detailLog DetailLogger instance
   * @param throwable throwable. Can be {@code null},
   *     since logging should not throw an exception even in that case.
   */
  public static void logSystemError(DetailLogger detailLog, @Nullable Throwable throwable) {
    logSystemError(detailLog, throwable, null);
  }

  /**
   * Logs a system error to both {@code ErrorLogger} and {@code DetailLogger}.
   *
   * <p>{@code ErrorLogger} logs a one-line message for surveillance,
   *     and {@code DetailLogger} logs the details including the stack trace.</p>
   * 
   * @param detailLog DetailLogger instance
   * @param throwable throwable. Can be {@code null},
   *     since logging should not throw an exception even in that case.
   * @param additionalMessage additionalMessage,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case of {@code null} or an empty string, no additional message is output.
   */
  public static void logSystemError(DetailLogger detailLog, @Nullable Throwable throwable,
      @Nullable String additionalMessage) {
    Objects.requireNonNull(detailLog);

    errLog.logSystemError(throwable, additionalMessage);
    detailLog.error(throwable, additionalMessage);
  }
}
