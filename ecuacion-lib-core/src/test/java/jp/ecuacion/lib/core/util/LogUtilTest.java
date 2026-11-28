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
package jp.ecuacion.lib.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.logging.ErrorLogger;
import jp.ecuacion.lib.core.logging.internal.AbstractLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

/** Tests for {@link LogUtil}. */
@DisplayName("LogUtil")
public class LogUtilTest {

  private final List<String> errorLogs = new ArrayList<>();
  private final List<String> detailLogs = new ArrayList<>();
  private DetailLogger detailLog = new DetailLogger(LogUtilTest.class);
  private Object originalErrorLogger = new Object();

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

  private static Field internalLoggerField() throws ReflectiveOperationException {
    Field field = AbstractLogger.class.getDeclaredField("internalLogger");
    field.setAccessible(true);
    return field;
  }

  private static ErrorLogger errorLoggerOfLogUtil() throws ReflectiveOperationException {
    Field field = LogUtil.class.getDeclaredField("errLog");
    field.setAccessible(true);
    return (ErrorLogger) Objects.requireNonNull(field.get(null));
  }

  @BeforeEach
  void setUp() throws ReflectiveOperationException {
    internalLoggerField().set(detailLog, capturingLogger(detailLogs));

    ErrorLogger errLog = errorLoggerOfLogUtil();
    originalErrorLogger = Objects.requireNonNull(internalLoggerField().get(errLog));
    internalLoggerField().set(errLog, capturingLogger(errorLogs));
  }

  @AfterEach
  void tearDown() throws ReflectiveOperationException {
    internalLoggerField().set(errorLoggerOfLogUtil(), originalErrorLogger);
  }

  @Test
  @DisplayName("additional message is output to both loggers")
  void additionalMessage() {
    LogUtil.logSystemError(detailLog, new RuntimeException("boom"), "orderId=12345");

    assertThat(errorLogs).hasSize(1);
    assertThat(errorLogs.get(0)).endsWith(" (orderId=12345)");
    assertThat(detailLogs).hasSize(3);
    assertThat(detailLogs.get(2)).isEqualTo("orderId=12345");
  }

  @Test
  @DisplayName("null additional message is not output")
  void nullAdditionalMessage() {
    LogUtil.logSystemError(detailLog, new RuntimeException("boom"), null);

    assertThat(errorLogs).hasSize(1);
    assertThat(errorLogs.get(0)).doesNotContain("(").endsWith("[boom]");
    assertThat(detailLogs).hasSize(2);
  }

  @Test
  @DisplayName("empty additional message is not output, the same as null")
  void emptyAdditionalMessage() {
    LogUtil.logSystemError(detailLog, new RuntimeException("boom"), "");

    assertThat(errorLogs).hasSize(1);
    assertThat(errorLogs.get(0)).doesNotContain("(").endsWith("[boom]");
    assertThat(detailLogs).hasSize(2);
  }

  @Test
  @DisplayName("null throwable is logged without throwing an exception")
  void nullThrowable() {
    LogUtil.logSystemError(detailLog, null, "orderId=12345");

    assertThat(errorLogs).hasSize(1);
    assertThat(errorLogs.get(0)).contains(ExceptionLogUtil.NULL_THROWABLE_MESSAGE);
    assertThat(detailLogs.get(0)).isEqualTo(ExceptionLogUtil.NULL_THROWABLE_MESSAGE);
  }
}
