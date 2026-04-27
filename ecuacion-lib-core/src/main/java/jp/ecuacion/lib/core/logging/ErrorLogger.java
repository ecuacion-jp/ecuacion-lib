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

import jakarta.validation.ConstraintViolation;
import java.util.Locale;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.logging.internal.EclibLogger;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.Level;

/**
 * Logs messages which are surveilled by surveillance service 
 * and alert error occurrence to administrators.
 * 
 * <p>It doesn't log details, but just one-line messages. Details should be logged by
 *     {@code DetailLogger} and others.</p>
 * 
 * <p>Available loglevels are as follows:</p>
 * <ul>
 * <li>error: uses for error occurrence</li>
 * <li>warn : uses for warning</li>
 * <li>info : uses for recover from warn state</li>
 * </ul>
 */
public class ErrorLogger extends EclibLogger {

  /** Constructs a new instance with a fixed logger name. */
  public ErrorLogger() {
    super("error-logger");
  }

  /**
   * Logs message with "info" loglevel.
   *
   * @param message message to log. Can be {@code null}.
   */
  public void info(@Nullable String message) {
    log(Level.INFO, message);
  }

  /**
   * Logs message with "warn" loglevel.
   *
   * @param message message to log. Can be {@code null}.
   */
  public void warn(@Nullable String message) {
    log(Level.WARN, message);
  }


  /**
   * Logs message with "error" loglevel.
   *
   * @param message message to log. Can be {@code null}.
   */
  public void error(@Nullable String message) {
    log(Level.ERROR, message);
  }

  /**
   * Logs system error.
   * 
   * @param throwable throwable
   */
  public void logSystemError(@Nullable Throwable throwable) {
    logSystemError(throwable, null);
  }

  /**
   * Logs system error.
   * 
   * @param throwable throwable
   * @param additionalMessage additionalMessage
   */
  public void logSystemError(@Nullable Throwable throwable, @Nullable String additionalMessage) {

    String throwableMessage;

    if (throwable == null) {
      throwableMessage = NULL_THROWABLE_MESSAGE;

    } else {
      throwableMessage = throwable.getClass().getName() + " - "
          + ExceptionUtil.getMessageList(throwable, Locale.ENGLISH).toString().replace("\n", " ");

      if (throwable instanceof ViolationException ve) {
        StringBuilder sb = new StringBuilder(throwableMessage);
        for (ConstraintViolation<?> cv
            : ve.getViolations().getConstraintViolations()) {
          sb.append("\n").append(ConstraintViolationBean.createConstraintViolationBean(cv));
        }
        throwableMessage = sb.toString();
      }
    }

    String additionalMsg = additionalMessage == null ? "" : " (" + additionalMessage + ")";
    internalLogger.error("A system error has occurred: " + throwableMessage + additionalMsg);
  }
}
