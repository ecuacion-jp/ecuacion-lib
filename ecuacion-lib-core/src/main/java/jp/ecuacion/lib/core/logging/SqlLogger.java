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

import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.logging.internal.EclibLogger;

/**
 * Logs sqls and related info like sql parameters. 
 * 
 * <p>Spring framework has it's own sql-logging structure, so this is not used.<br>
 * this is mainly used by ecuacion-util-jpa.</p>
 * 
 * <p>Available loglevels are as follows:</p>
 * <ul>
 * <li>debug: uses for sql log</li>
 * <li>trace: uses for sql-related info</li>
 * </ul>
 */
public class SqlLogger extends EclibLogger {

  /** Constructs a new instance with a fixed logger name. */
  public SqlLogger() {
    super("sql-logger");
  }

  /** 
   * Logs message with "trace" loglevel.
   *
   * @param message message to log. Cannot be {@code null}.
   */
  public void trace(@RequireNonnull String message) {
    log(message, LogLevel.trace);
  }

  /** 
   * Logs message with "debug" loglevel.
   *
   * @param message message to log. Cannot be {@code null}.
   */
  public void debug(@RequireNonnull String message) {
    log(message, LogLevel.debug);
  }
}
