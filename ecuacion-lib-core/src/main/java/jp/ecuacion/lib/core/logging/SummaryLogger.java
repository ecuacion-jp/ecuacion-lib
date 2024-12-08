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
import jp.ecuacion.lib.core.logging.internal.AbstractLogger;

/**
 * Logs start and end time of the timer-triggered exections, 
 * espacially used in batch programs. Not used for web or rest.
 * 
 * <p>This is mainly expected to use in the ecuacion-lib or other library, 
 * but not in specific apps.<br>
 * Although you can use it in the case that you need to skip the execution in some condition
 * or things like this.</p>
 * 
 * <p>You are also allowed to log what is as important as the start and end status, 
 * but in most cases it's better to log with {@code DetailLogger}.</p>
 * 
 * <p>Available loglevels are as follows:</p>
 * <ul>
 * <li>info : uses for start and normal end</li>
 * <li>warn : uses for skip and any other special way of enging executions</li>
 * <li>error: uses for abnormal end</li>
 * </ul>
 */
public class SummaryLogger extends AbstractLogger {

  /** Constructs a new instance with a fixed logger name. */
  public SummaryLogger() {
    super("summary-logger");
  }
  
  /** 
   * Logs message with "info" loglevel.
   *
   * @param message message to log. Cannot be {@code null}.
   */
  public void info(@Nonnull String message) {
    log(message, LogLevel.info);
  }

  /** 
   * Logs message with "warn" loglevel.
   *
   * @param message message to log. Cannot be {@code null}.
   */
  public void warn(@Nonnull String message) {
    log(message, LogLevel.warn);
  }

  /** 
   * Logs message with "error" loglevel.
   *
   * @param message message to log. Cannot be {@code null}.
   */
  public void error(@Nonnull String message) {
    log(message, LogLevel.error);
  }
}
