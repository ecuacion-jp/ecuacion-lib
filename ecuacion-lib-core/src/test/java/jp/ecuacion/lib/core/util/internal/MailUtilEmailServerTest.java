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

/** Tests for {@link MailUtilEmailServer}. */
@DisplayName("MailUtilEmailServer")
public class MailUtilEmailServerTest {

  @Test
  @DisplayName("constructor and getters: all fields round-trip correctly")
  void constructorAndGetters() {
    MailUtilEmailServer server =
        new MailUtilEmailServer("smtp.test.com", "587", false, true, true);

    assertThat(server.getSmtpServer()).isEqualTo("smtp.test.com");
    assertThat(server.getPort()).isEqualTo("587");
    assertThat(server.isSslEnabled()).isFalse();
    assertThat(server.isNeedsAuthentication()).isTrue();
    assertThat(server.isStarttlsRequired()).isTrue();
  }

  @Test
  @DisplayName("constructor and getters: sslEnabled and starttlsRequired can both be false")
  void constructorAndGetters_flagsFalse() {
    MailUtilEmailServer server =
        new MailUtilEmailServer("smtp.test.com", "465", true, false, false);

    assertThat(server.isSslEnabled()).isTrue();
    assertThat(server.isNeedsAuthentication()).isFalse();
    assertThat(server.isStarttlsRequired()).isFalse();
  }
}
