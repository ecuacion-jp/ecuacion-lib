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

import java.util.Locale;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.logging.internal.EclibLogger;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import org.slf4j.event.Level;

/**
 * Logs messages which are surveilled by survaillance service 
 * and alert error occurence to administrators.
 * 
 * <p>It doesn't log details, but just one-line messages. Details should be logged by
 * {@code DetailLogger}.
 * </p>
 * 
 * <p>Usually error loglevel is used, but it can be used for alert by warn
 *  and tell the warning is resolved by info loglevel.<br>
 *  So warn and info loglevel can be used.</p>
 *  
 * <p>Available loglevels are as follows:</p>
 * <ul>
 * <li>error: uses for error occurence</li>
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
   * @param message message to log. Cannot be {@code null}.
   */
  public void info(@RequireNonnull String message) {
    log(Level.INFO, message);
  }

  /**
   * Logs message with "warn" loglevel.
   *
   * @param message message to log. Cannot be {@code null}.
   */
  public void warn(@RequireNonnull String message) {
    log(Level.WARN, message);
  }


  /**
   * Logs message with "error" loglevel.
   *
   * @param message message to log. Cannot be {@code null}.
   */
  public void error(@RequireNonnull String message) {
    log(Level.ERROR, message);
  }

  /**
   * Logs system error.
   * 
   * @param throwable throwable
   */
  public void logSystemError(Throwable throwable) {
    logSystemError(throwable, null);
  }

  /**
   * Logs system error.
   * 
   * @param throwable throwable
   * @param additionalMessage additionalMessage
   */
  public void logSystemError(Throwable throwable, String additionalMessage) {

    ObjectsUtil.paramRequireNonNull(throwable);
    ObjectsUtil.paramRequireNonNull(additionalMessage);

    String msg = (throwable.getMessage() == null) ? ""
        : " - " + ExceptionUtil.getExceptionMessage(throwable, Locale.ENGLISH, true).toString()
            .replace("\n", " ");
    internalLogger.error("A system error has occurred: " + throwable.getClass().getName() + msg
        + " (" + additionalMessage + ")");
  }
}
