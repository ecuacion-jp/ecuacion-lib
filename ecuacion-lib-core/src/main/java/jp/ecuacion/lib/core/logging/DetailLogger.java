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
package jp.ecuacion.lib.core.logging;

import jp.ecuacion.lib.core.logging.internal.AbstractLogger;
import jp.ecuacion.lib.core.util.ExceptionLogUtil;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.Level;

/**
 * Logs anything you want to log.
 * 
 * <p>Logs output by other libraries or frameworks like spring are processed with this logger, 
 * so the root logger is used.</p>
 * 
 * <p>All the loglevels (trace, debug, info, warn, error) can be used.</p>
 */
public class DetailLogger extends AbstractLogger {

  /** 
   * Constructs a new instance with a caller instance.
   *
   * @param object caller object
   */
  public DetailLogger(Object object) {
    this(object.getClass());
  }

  /** 
   * Constructs a new instance with a caller class. 
   * Used when logging is executed from static method.
   *
   * @param cls caller class.
   */
  public DetailLogger(Class<?> cls) {
    super(ObjectsUtil.requireNonNull(cls));
  }

  /** 
   * Logs message with "trace" loglevel.
   *
   * @param message message to log
   */
  public void trace(@Nullable String message) {
    log(Level.TRACE, message);
  }

  /** 
   * Logs message with "debug" loglevel.
   *
   * @param message message to log
   */
  public void debug(@Nullable String message) {
    log(Level.DEBUG, message);
  }

  /**
   * Logs message with "info" loglevel.
   *
   * @param message message to log
   */
  public void info(@Nullable String message) {
    log(Level.INFO, message);
  }

  /**
   * Logs message with "warn" loglevel.
   *
   * @param message message to log
   */
  public void warn(@Nullable String message) {
    log(Level.WARN, message);
  }

  /**
   * Logs exception message with "error" loglevel.
   *
   * @param th exception to log
   */
  public void warn(@Nullable Throwable th) {
    ObjectsUtil.requireNonNull(th);
    log(Level.WARN, th);
  }

  /**
   * Logs message with "error" loglevel.
   *
   * @param message message to log
   */
  public void error(@Nullable String message) {
    log(Level.ERROR, message);
  }

  /**
   * Logs error.
   * 
   * @param throwable throwable
   */
  public void error(@Nullable Throwable throwable) {
    error(throwable, ExceptionUtil.SYSTEM_ERROR_OCCURED_SIGN);
  }

  /**
   * Logs error.
   * 
   * @param throwable throwable
   * @param additionalMessage additionalMessage
   */
  public void error(@Nullable Throwable throwable, @Nullable String additionalMessage) {
    // Output to detailLog
    log(Level.ERROR, throwable, ExceptionUtil.SYSTEM_ERROR_OCCURED_SIGN, additionalMessage);
  }

  /** 
   * Logs message and throwable with logLevel.
   * 
   * @param logLevel logLevel
   * @param throwable throwable
   */
  private void log(Level logLevel, @Nullable Throwable throwable) {
    log(logLevel, throwable, new @Nullable String[] {});
  }

  /** 
   * Logs message and throwable with logLevel.
   * 
   * @param logLevel logLevel. Cannot be {@code null}.
   * @param throwable throwable. It can be {@code null}. 
   *     See {@code message} parameter part of {@link AbstractLogger#log(Level, String)}.
   * @param additionalMessage message. Elements can be {@code null}. 
   *     See {@code message} parameter part of {@link AbstractLogger#log(Level, String)}.
   */
  private void log(Level logLevel, @Nullable Throwable throwable,
      @Nullable String... additionalMessages) {

    String throwableMessage = null;
    if (throwable == null) {
      throwableMessage = NULL_THROWABLE_MESSAGE;

    } else {
      StringBuilder sb = new StringBuilder();
      ExceptionLogUtil.getMessageAndStackTraceStringRecursively(sb, throwable, null, null);
      throwableMessage = sb.toString();
    }

    log(logLevel, throwableMessage);

    for (String additionalMessage : additionalMessages) {
      if (!StringUtils.isEmpty(additionalMessage)) {
        log(logLevel, additionalMessage);
      }
    }
  }
}
