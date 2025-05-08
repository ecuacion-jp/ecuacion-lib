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

import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Has common methods for concrete loggers.
 */
public abstract class EclibLogger {

  protected static ExceptionUtil exUtil = new ExceptionUtil();

  /** internalLogger. */
  protected Logger internalLogger;

  /** 
   * Constructs a new instance with a logger name.
   * 
   * @param loggerName loggerName. Cannot be {@code null}.
   */
  public EclibLogger(String loggerName) {
    internalLogger = LoggerFactory.getLogger(loggerName);
  }

  /** 
   * Constructs a new instance with a caller class. 
   * Used when logging is executed from static method.
   *
   * @param cls class. Cannot be {@code null}.
   */
  public EclibLogger(Class<?> cls) {
    internalLogger = LoggerFactory.getLogger(cls.getName());
  }

  /** 
   * Logs message with logLevel.
   * 
   * @param message message. Cannot be {@code null}.
   * @param logLevel logLevel. Cannot be {@code null}.
   */
  public void log(Level logLevel, String message) {
    ObjectsUtil.paramRequireNonNull(message);
    ObjectsUtil.paramRequireNonNull(logLevel);

    switch (logLevel) {
      case Level.ERROR -> internalLogger.error(message);
      case Level.WARN -> internalLogger.warn(message);
      case Level.INFO -> internalLogger.info(message);
      case Level.DEBUG -> internalLogger.debug(message);
      case Level.TRACE -> internalLogger.trace(message);
      default -> throw new IllegalArgumentException("Unexpected value: " + logLevel);
    }
  }
}
