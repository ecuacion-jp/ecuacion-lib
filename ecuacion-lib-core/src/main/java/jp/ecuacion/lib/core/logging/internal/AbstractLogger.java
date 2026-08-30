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

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

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
   * Logs message with logLevel, after sanitizing control characters (CR/LF etc., via
   * {@link #sanitize}) so that a message built by concatenating untrusted input (a request URI,
   * a header value, ...) cannot forge additional log lines or entries. This is the method every
   * {@code trace}/{@code debug}/{@code info}/{@code warn}/{@code error} overload across the
   * concrete logger classes ends up calling for a plain, single message — callers do not need to
   * sanitize such fragments themselves before concatenating them.
   *
   * <p>Not used for content that is deliberately multi-line, such as a rendered stack trace —
   *     see {@link #logWithoutSanitizing}, which those callers use instead so their line breaks
   *     survive.</p>
   *
   * @param logLevel logLevel. Cannot be {@code null}.
   * @param message message.
   *     Log messages are usually not {@code null},
   *     but when someone wants to log the value of some variable and its value is {@code null},
   *     it's not good for the logging procedure to throw an exception.
   *     It seems that loggers are supposed to log whatever the logged string is.
   *     So {@code message} is {@code @Nullable}.
   */
  public void log(Level logLevel, @Nullable String message) {
    logInternal(logLevel, sanitize(message));
  }

  /**
   * Logs message with logLevel, exactly as given — no sanitization.
   *
   * <p>Reserved for a subclass that has already assembled multi-line content it fully controls
   *     the structure of (e.g. {@code DetailLogger}'s rendered stack trace output), where
   *     sanitizing would flatten the line breaks into an unreadable single line. Never pass a
   *     value built by concatenating untrusted input to this method — use {@link #log} instead,
   *     which sanitizes.</p>
   *
   * @param logLevel logLevel. Cannot be {@code null}.
   * @param message message. See {@link #log}'s {@code message} parameter for the {@code null}
   *     rationale.
   */
  protected void logWithoutSanitizing(Level logLevel, @Nullable String message) {
    logInternal(logLevel, message);
  }

  private void logInternal(Level logLevel, @Nullable String message) {
    Objects.requireNonNull(logLevel);

    switch (logLevel) {
      case Level.ERROR -> internalLogger.error(message);
      case Level.WARN -> internalLogger.warn(message);
      case Level.INFO -> internalLogger.info(message);
      case Level.DEBUG -> internalLogger.debug(message);
      case Level.TRACE -> internalLogger.trace(message);
      default -> throw new IllegalArgumentException("Unexpected value: " + logLevel);
    }
  }

  /**
   * Escapes CR / LF and removes other ASCII control characters in {@code value}, so it is safe
   * to concatenate into a single log line without letting an attacker forge extra log lines or
   * fake log entries (CRLF / log injection) via a value that originates from untrusted input
   * (e.g. a request URI or header value).
   *
   * @param value value to sanitize, or {@code null}
   * @return {@code value} with {@code CR} / {@code LF} replaced by the literal two-character
   *     sequences {@code \r} / {@code \n}, and other ASCII control characters
   *     ({@code 0x00}-{@code 0x1F} excluding tab, and {@code 0x7F}) removed; {@code null} if
   *     {@code value} is {@code null}
   */
  private static @Nullable String sanitize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '\r') {
        sb.append("\\r");
      } else if (c == '\n') {
        sb.append("\\n");
      } else if (c == '\t' || (c >= 0x20 && c != 0x7f)) {
        sb.append(c);
      }
      // else: drop other control characters
    }

    return sb.toString();
  }
}
