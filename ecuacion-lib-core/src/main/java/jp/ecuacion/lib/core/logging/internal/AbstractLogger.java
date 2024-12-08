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
package jp.ecuacion.lib.core.logging.internal;

import jp.ecuacion.lib.core.exception.unchecked.RuntimeSystemException;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Has common methods for concrete loggers.
 */
public abstract class AbstractLogger {

  /** internalLogger. */
  private Logger internalLogger;

  /** 
   * Constructs a new instance with a logger name.
   * 
   * @param loggerName loggerName. Cannot be {@code null}.
   */
  public AbstractLogger(String loggerName) {
    internalLogger = LoggerFactory.getLogger(loggerName);
  }

  /** 
   * Constructs a new instance with a caller class. 
   * Used when logging is executed from static method.
   *
   * @param cls class. Cannot be {@code null}.
   */
  public AbstractLogger(Class<?> cls) {
    internalLogger = LoggerFactory.getLogger(cls.getName());
  }

  /** 
   * Logs message with logLevel.
   * 
   * @param message message. Cannot be {@code null}.
   * @param logLevel logLevel. Cannot be {@code null}.
   */
  protected void log(String message, LogLevel logLevel) {
    ObjectsUtil.paramRequireNonNull(message);
    ObjectsUtil.paramRequireNonNull(logLevel);

    if (logLevel == LogLevel.error) {
      internalLogger.error(message);

    } else if (logLevel == LogLevel.warn) {
      internalLogger.warn(message);

    } else if (logLevel == LogLevel.info) {
      internalLogger.info(message);

    } else if (logLevel == LogLevel.debug) {
      internalLogger.debug(message);

    } else if (logLevel == LogLevel.trace) {
      internalLogger.trace(message);

    } else {
      throw new RuntimeSystemException("nonexistent Loglevel : " + logLevel);
    }
  }

  /** 
   * Logs {@code Throwable}.
   * 
   * @param th throwable. Cannot be {@code null}.
   */
  protected void logThrowable(Throwable th) {
    ObjectsUtil.paramRequireNonNull(th);
    internalLogger.error("A system error has occured: ", th);
  }

  /** Is used for describing loglevels.*/
  public static enum LogLevel {
    error, warn, info, debug, trace;
  }
}
