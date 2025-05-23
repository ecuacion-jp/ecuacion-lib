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

import jakarta.annotation.Nullable;
import java.util.Locale;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.unchecked.UncheckedAppException;
import jp.ecuacion.lib.core.logging.internal.EclibLogger;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import org.slf4j.event.Level;

/**
 * Logs anything you want to log.
 * 
 * <p>Logs output by other libraries or frameworks like spring are processed with this logger, 
 * so the root logger is used.</p>
 * 
 * <p>All the loglevels (trace, debug, info, warn, error) can be used.</p>
 */
public class DetailLogger extends EclibLogger {

  /** 
   * Constructs a new instance with a caller instance.
   *
   * @param object caller object
   */
  public DetailLogger(@RequireNonnull Object object) {
    this(object.getClass());

    ObjectsUtil.requireNonNull(object);
  }

  /** 
   * Constructs a new instance with a caller class. 
   * Used when logging is executed from static method.
   *
   * @param cls caller class.
   */
  public DetailLogger(@RequireNonnull Class<?> cls) {
    super(cls);

    ObjectsUtil.requireNonNull(cls);
  }

  /** 
   * Logs message with "trace" loglevel.
   *
   * @param message message to log
   */
  public void trace(@RequireNonnull String message) {
    log(Level.TRACE, message);
  }

  /** 
   * Logs message with "debug" loglevel.
   *
   * @param message message to log
   */
  public void debug(@RequireNonnull String message) {
    log(Level.DEBUG, message);
  }

  /**
   * Logs message with "info" loglevel.
   *
   * @param message message to log
   */
  public void info(@RequireNonnull String message) {
    log(Level.INFO, message);
  }

  /**
   * Logs message with "warn" loglevel.
   *
   * @param message message to log
   */
  public void warn(@RequireNonnull String message) {
    log(Level.WARN, message);
  }

  /**
   * Logs message with "error" loglevel.
   *
   * @param message message to log
   */
  public void error(@RequireNonnull String message) {
    log(Level.ERROR, message);
  }

  /**
   * Logs exception message with "error" loglevel.
   *
   * @param th exception to log
   */
  public void error(@RequireNonnull Throwable th) {
    ObjectsUtil.requireNonNull(th);
    log(Level.ERROR, "A system error has occurred: ", th);
  }

  /**
   * Logs system error.
   * 
   * @param throwable throwable
   */
  public void logSystemError(@RequireNonnull Throwable throwable) {
    logSystemError(throwable, null);
  }

  /**
   * Logs system error.
   * 
   * @param throwable throwable
   * @param additionalMessage additionalMessage
   */
  public void logSystemError(@RequireNonnull Throwable throwable,
      @Nullable String additionalMessage) {
    ObjectsUtil.requireNonNull(throwable);

    // additionalMessageへの追加
    if (throwable instanceof AppException || throwable instanceof UncheckedAppException) {
      // ビジネスエラーとしてクライアントに返すのみ。本サービスでのエラー発生関連処理はない。
      StringBuilder sb = new StringBuilder();
      AppException ae = throwable instanceof UncheckedAppException
          ? (AppException) ((UncheckedAppException) throwable).getCause()
          : (AppException) throwable;
      ExceptionUtil.getAppExceptionMessageList(ae, Locale.getDefault())
          .forEach(tmpMsg -> sb.append(tmpMsg + "\n"));

      additionalMessage += "\n" + sb.toString();
    }

    // Output to detailLog
    log(Level.ERROR, ExceptionUtil.getErrLogString(throwable, additionalMessage, Locale.ENGLISH));
  }

  /**
   * Logs Exception info for reference.
   * 
   * <p>It's used when you want to know where an {@code AppException} is thrown from with webapps
   * (Usually AppException shows business error messages only, 
   * no stack trace is recorded anywhere.).</p>
   * 
   * @param throwable throwable
   */
  public void logExceptionAsDebugInfo(@RequireNonnull Throwable throwable) {
    ObjectsUtil.requireNonNull(throwable);
    
    internalLogger.debug(EclibCoreConstants.ECLIB_PREFIX
        + "The following is only for reference. (No system error will occur)");

  }

  /** 
   * Logs message and throwable with logLevel.
   * 
   * @param message message. Cannot be {@code null}.
   * @param logLevel logLevel. Cannot be {@code null}.
   */
  private void log(@RequireNonnull Level logLevel, @RequireNonnull String message,
      @RequireNonnull Throwable throwable) {
    ObjectsUtil.requireNonNull(message, logLevel, throwable);

    switch (logLevel) {
      case Level.ERROR -> internalLogger.error(message, throwable);
      case Level.WARN -> internalLogger.warn(message, throwable);
      case Level.INFO -> internalLogger.info(message, throwable);
      case Level.DEBUG -> internalLogger.debug(message, throwable);
      case Level.TRACE -> internalLogger.trace(message, throwable);
      default -> throw new IllegalArgumentException("Unexpected value: " + logLevel);
    }
  }
}
