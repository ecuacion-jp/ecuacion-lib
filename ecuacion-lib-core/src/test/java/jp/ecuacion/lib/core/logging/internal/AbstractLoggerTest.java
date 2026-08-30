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

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/** Tests for {@link AbstractLogger#log} / {@link AbstractLogger#logWithoutSanitizing}. */
class AbstractLoggerTest {

  private static class TestLogger extends AbstractLogger {
    TestLogger(Logger internalLogger) {
      super("test-logger");
      try {
        Field field = AbstractLogger.class.getDeclaredField("internalLogger");
        field.setAccessible(true);
        field.set(this, internalLogger);
      } catch (ReflectiveOperationException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  /** Captures every single-{@code String}-argument call made on it (error/warn/info/...). */
  private static Logger capturingLogger(List<String> captured) {
    return (Logger) Proxy.newProxyInstance(Logger.class.getClassLoader(),
        new Class<?>[] {Logger.class}, (proxy, method, args) -> {
          if (args != null && args.length == 1 && args[0] instanceof String) {
            captured.add((String) args[0]);
          }
          return method.getReturnType() == boolean.class ? false : null;
        });
  }

  @Test
  void logPassesThroughNormalCharactersUnchanged() {
    List<String> captured = new ArrayList<>();
    TestLogger logger = new TestLogger(capturingLogger(captured));

    logger.log(Level.INFO, "GET /api/key/foo?x=1");

    assertThat(captured).containsExactly("GET /api/key/foo?x=1");
  }

  @Test
  void logEscapesCrAndLfToLiteralBackslashRBackslashN() {
    List<String> captured = new ArrayList<>();
    TestLogger logger = new TestLogger(capturingLogger(captured));

    logger.log(Level.WARN, "line1\r\nline2");

    assertThat(captured).containsExactly("line1\\r\\nline2");
  }

  @Test
  void logNeutralizesALogForgingAttemptViaLf() {
    List<String> captured = new ArrayList<>();
    TestLogger logger = new TestLogger(capturingLogger(captured));

    logger.log(Level.WARN, "/api/key/foo\n2026-01-01 ERROR fake log line");

    assertThat(captured).containsExactly(
        "/api/key/foo\\n2026-01-01 ERROR fake log line");
  }

  @Test
  void logPreservesTab() {
    List<String> captured = new ArrayList<>();
    TestLogger logger = new TestLogger(capturingLogger(captured));

    logger.log(Level.INFO, "a\tb");

    assertThat(captured).containsExactly("a\tb");
  }

  @Test
  void logRemovesOtherAsciiControlCharacters() {
    List<String> captured = new ArrayList<>();
    TestLogger logger = new TestLogger(capturingLogger(captured));

    logger.log(Level.INFO, "a" + '\u0001' + "bcd");

    assertThat(captured).containsExactly("abcd");
  }

  @Test
  void logPassesThroughNullMessageAsNull() {
    List<String> captured = new ArrayList<>();
    TestLogger logger = new TestLogger(capturingLogger(captured));

    logger.log(Level.INFO, null);

    // The proxy only records String-argument calls, so a null message means nothing gets
    // captured; the assertion below just confirms log() doesn't throw for a null message.
    assertThat(captured).isEmpty();
  }

  @Test
  void logWithoutSanitizingPreservesLineBreaks() {
    List<String> captured = new ArrayList<>();
    TestLogger logger = new TestLogger(capturingLogger(captured));

    logger.logWithoutSanitizing(Level.ERROR, "line1\nline2\nline3");

    assertThat(captured).containsExactly("line1\nline2\nline3");
  }
}
