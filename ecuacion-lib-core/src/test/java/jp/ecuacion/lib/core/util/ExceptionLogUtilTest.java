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
import java.util.Locale;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.logging.internal.EclibLogger;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilValueGetter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link ExceptionLogUtil}. */
@DisplayName("ExceptionLogUtil")
public class ExceptionLogUtilTest {

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtilValueGetter.addToDynamicPostfixList("lib-core-test");
  }

  @Nested
  @DisplayName("getErrLogString")
  class GetErrLogString {

    @Test
    @DisplayName("null throwable: contains sign and null-throwable message")
    void nullThrowable() {
      String result = ExceptionLogUtil.getErrLogString(null, null, null);
      assertThat(result).contains(ExceptionUtil.SYSTEM_ERROR_OCCURED_SIGN);
      assertThat(result).contains(EclibLogger.NULL_THROWABLE_MESSAGE);
    }

    @Test
    @DisplayName("RuntimeException: contains sign, class name, and exception message")
    void runtimeException() {
      String result =
          ExceptionLogUtil.getErrLogString(new RuntimeException("err-msg"), null, Locale.ENGLISH);
      assertThat(result).contains(ExceptionUtil.SYSTEM_ERROR_OCCURED_SIGN);
      assertThat(result).contains("RuntimeException");
      assertThat(result).contains("err-msg");
      assertThat(result).contains("\tat ");
    }

    @Test
    @DisplayName("ViolationException: shows resolved message text")
    void violationException() {
      ViolationException ve =
          new ViolationException(new Violations().add(new BusinessViolation("MSG1")));
      String result = ExceptionLogUtil.getErrLogString(ve, null, Locale.ENGLISH);
      assertThat(result).contains("ViolationException");
      assertThat(result).contains("message 1.");
    }

    @Test
    @DisplayName("additional message is included in output")
    void withAdditionalMessage() {
      String result = ExceptionLogUtil.getErrLogString(
          new RuntimeException("x"), "extra info", Locale.ENGLISH);
      assertThat(result).contains("extra info");
    }

    @Test
    @DisplayName("packagesShown=0: full package name not shown in stack trace")
    void packagesShownZero() {
      String result =
          ExceptionLogUtil.getErrLogString(new RuntimeException("x"), null, null, 0);
      assertThat(result).contains(ExceptionUtil.SYSTEM_ERROR_OCCURED_SIGN);
      assertThat(result).doesNotContain("jp.ecuacion.lib.core");
    }

    @Test
    @DisplayName("packagesShown=1: first package segment shown in stack trace")
    void packagesShownOne() {
      String result =
          ExceptionLogUtil.getErrLogString(new RuntimeException("x"), null, null, 1);
      assertThat(result).contains("jp.");
    }
  }

  @Nested
  @DisplayName("getMessageAndStackTraceStringRecursively")
  class GetMessageAndStackTraceStringRecursively {

    @Test
    @DisplayName("root exception and its cause are both included")
    void causeChainIncluded() {
      Exception cause = new RuntimeException("the cause");
      Exception root = new RuntimeException("the root", cause);
      StringBuilder sb = new StringBuilder();
      ExceptionLogUtil.getMessageAndStackTraceStringRecursively(sb, root, Locale.ENGLISH);
      String result = sb.toString();
      assertThat(result).contains("the root");
      assertThat(result).contains("the cause");
    }

    @Test
    @DisplayName("null throwable outputs null-throwable message")
    void nullThrowable() {
      StringBuilder sb = new StringBuilder();
      ExceptionLogUtil.getMessageAndStackTraceStringRecursively(sb, null, null);
      assertThat(sb.toString()).contains(EclibLogger.NULL_THROWABLE_MESSAGE);
    }

    @Test
    @DisplayName("packagesShown overload: both root and cause are included")
    void withPackagesShownAndCause() {
      Exception cause = new RuntimeException("cause msg");
      Exception root = new RuntimeException("root msg", cause);
      StringBuilder sb = new StringBuilder();
      ExceptionLogUtil.getMessageAndStackTraceStringRecursively(sb, root, Locale.ENGLISH, 1);
      String result = sb.toString();
      assertThat(result).contains("root msg");
      assertThat(result).contains("cause msg");
    }
  }
}
