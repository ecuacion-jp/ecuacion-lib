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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilBundleReader;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link MailUtil}. */
@DisplayName("MailUtil")
public class MailUtilTest {

  record CapturedMail(@Nullable List<String> to, @Nullable List<String> cc,
      boolean isHtml, String title, @Nullable String content) {}

  private final List<CapturedMail> captured = new ArrayList<>();

  @BeforeAll
  static void init() {
    PropertiesFileUtilBundleReader.addToDynamicPostfixList("lib-core-test");
  }

  @SuppressWarnings("null")
  @BeforeEach
  void setUp() {
    captured.clear();
    MailUtil.mailSender = (to, cc, html, title, content) ->
        captured.add(new CapturedMail(to, cc, html, title, content));
  }

  @AfterEach
  void tearDown() {
    MailUtil.mailSender = null;
  }

  @Test
  @DisplayName("sendTextMail: captured with correct title and plain-text flag")
  void sendTextMail() throws Exception {
    MailUtil.sendTextMail(List.of("to@test.com"), null, "Text Subject", "Text body");

    assertThat(captured).hasSize(1);
    assertThat(captured.get(0).title()).isEqualTo("Text Subject");
    assertThat(captured.get(0).isHtml()).isFalse();
  }

  @Test
  @DisplayName("sendHtmlMail: captured with HTML flag set")
  void sendHtmlMail() throws Exception {
    MailUtil.sendHtmlMail(List.of("to@test.com"), null, "HTML Subject", "<h1>Hello</h1>");

    assertThat(captured).hasSize(1);
    assertThat(captured.get(0).title()).isEqualTo("HTML Subject");
    assertThat(captured.get(0).isHtml()).isTrue();
  }

  @Test
  @DisplayName("sendTextMail with CC: TO and CC are both captured")
  void sendTextMail_withCc() throws Exception {
    MailUtil.sendTextMail(List.of("to@test.com"), List.of("cc@test.com"), "CC Subject", "body");

    assertThat(captured).hasSize(1);
    assertThat(captured.get(0).to()).containsExactly("to@test.com");
    assertThat(captured.get(0).cc()).containsExactly("cc@test.com");
  }

  @Test
  @DisplayName("sendErrorMail(Throwable): captured with error title from properties")
  void sendErrorMail_throwable() throws Exception {
    MailUtil.sendErrorMail(new RuntimeException("test error"));

    assertThat(captured).hasSize(1);
    assertThat(captured.get(0).title()).contains("system error");
  }

  @Test
  @DisplayName("sendErrorMail(Throwable, String): additional message variant also captured")
  void sendErrorMail_withAdditionalMessage() throws Exception {
    MailUtil.sendErrorMail(new RuntimeException("test error"), "additional info");

    assertThat(captured).hasSize(1);
    assertThat(captured.get(0).title()).contains("system error");
  }

  @Test
  @DisplayName("sendWarnMail: captured with warn title")
  void sendWarnMail() throws Exception {
    MailUtil.sendWarnMail("Warn content", List.of("warn@test.com"));

    assertThat(captured).hasSize(1);
    assertThat(captured.get(0).title()).contains("Warn Message");
  }

  @Test
  @DisplayName("sendTextMail: throws RuntimeException when both TO and CC are null")
  void sendTextMail_bothNull_throws() {
    assertThatThrownBy(() -> MailUtil.sendTextMail(null, null, "title", "body"))
        .isInstanceOf(RuntimeException.class);
  }
}
