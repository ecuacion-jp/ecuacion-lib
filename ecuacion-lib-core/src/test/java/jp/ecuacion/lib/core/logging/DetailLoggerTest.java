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

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.logging.internal.AbstractLogger;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

/** Tests for {@link DetailLogger}. */
class DetailLoggerTest {

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

  private static DetailLogger newDetailLoggerCapturingInto(List<String> captured)
      throws ReflectiveOperationException {
    DetailLogger detailLog = new DetailLogger(DetailLoggerTest.class);
    Field field = AbstractLogger.class.getDeclaredField("internalLogger");
    field.setAccessible(true);
    field.set(detailLog, capturingLogger(captured));
    return detailLog;
  }

  @Test
  void warnSanitizesAMessageBuiltFromUntrustedInput() throws Exception {
    List<String> captured = new ArrayList<>();
    DetailLogger detailLog = newDetailLoggerCapturingInto(captured);

    detailLog.warn("Request to /api/key/foo\n2026-01-01 ERROR fake log line is missing the key.");

    assertThat(captured).containsExactly(
        "Request to /api/key/foo\\n2026-01-01 ERROR fake log line is missing the key.");
  }

  @Test
  void errorWithThrowablePreservesTheStackTraceLineBreaks() throws Exception {
    List<String> captured = new ArrayList<>();
    DetailLogger detailLog = newDetailLoggerCapturingInto(captured);

    detailLog.error(new RuntimeException("boom"));

    assertThat(captured).isNotEmpty();
    // The rendered stack trace is deliberately multi-line and must survive un-flattened.
    assertThat(captured.get(0)).contains("\n");
    assertThat(captured.get(0)).doesNotContain("\\n");
  }
}
