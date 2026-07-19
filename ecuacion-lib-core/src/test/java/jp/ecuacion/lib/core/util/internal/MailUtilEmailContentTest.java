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
package jp.ecuacion.lib.core.util.internal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link MailUtilEmailContent}. */
@DisplayName("MailUtilEmailContent")
public class MailUtilEmailContentTest {

  @Test
  @DisplayName("getBounceMailAddress: falls back to sender when bounceMailAddress is null")
  void getBounceMailAddress_fallsBackToSender() {
    MailUtilEmailContent content = new MailUtilEmailContent("sender@test.com", null);

    assertThat(content.getBounceMailAddress()).isEqualTo("sender@test.com");
  }

  @Test
  @DisplayName("getBounceMailAddress: uses the configured bounce address when set")
  void getBounceMailAddress_usesConfiguredValue() {
    MailUtilEmailContent content = new MailUtilEmailContent("sender@test.com", "bounce@test.com");

    assertThat(content.getBounceMailAddress()).isEqualTo("bounce@test.com");
  }
}
