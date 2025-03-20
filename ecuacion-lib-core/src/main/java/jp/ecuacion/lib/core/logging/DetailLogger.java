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

import jakarta.annotation.Nonnull;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.logging.internal.LibLogger;
import jp.ecuacion.lib.core.util.ObjectsUtil;

/**
 * Logs anything you want to log.
 * 
 * <p>Logs output by other libraries or frameworks like spring are processed with this logger, 
 * so the root logger is used.</p>
 * 
 * <p>All the loglevels (trace, debug, info, warn, error) can be used.</p>
 */
public class DetailLogger extends LibLogger {

  /** 
   * Constructs a new instance with a caller instance.
   *
   * @param object caller object
   */
  public DetailLogger(@Nonnull Object object) {
    this(object.getClass());

    ObjectsUtil.paramRequireNonNull(object);
  }

  /** 
   * Constructs a new instance with a caller class. 
   * Used when logging is executed from static method.
   *
   * @param cls caller class.
   */
  public DetailLogger(@Nonnull Class<?> cls) {
    super(cls);

    ObjectsUtil.paramRequireNonNull(cls);
  }

  /** 
   * Logs message with "trace" loglevel.
   *
   * @param message message to log
   */
  public void trace(@RequireNonnull String message) {
    log(message, LogLevel.trace);
  }

  /** 
   * Logs message with "debug" loglevel.
   *
   * @param message message to log
   */
  public void debug(@RequireNonnull String message) {
    log(message, LogLevel.debug);
  }

  /**
   * Logs message with "info" loglevel.
   *
   * @param message message to log
   */
  public void info(@RequireNonnull String message) {
    log(message, LogLevel.info);
  }

  /**
   * Logs message with "warn" loglevel.
   *
   * @param message message to log
   */
  public void warn(@RequireNonnull String message) {
    log(message, LogLevel.warn);
  }

  /**
   * Logs message with "error" loglevel.
   *
   * @param message message to log
   */
  public void error(@RequireNonnull String message) {
    log(message, LogLevel.error);
  }

  /**
   * Logs exception message with "error" loglevel.
   *
   * @param th exception to log
   */
  public void error(@RequireNonnull Throwable th) {
    logThrowable(th);
  }
}
