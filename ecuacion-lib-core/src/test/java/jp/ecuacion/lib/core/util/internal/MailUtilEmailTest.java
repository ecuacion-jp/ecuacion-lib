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
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link MailUtilEmail}. */
@DisplayName("MailUtilEmail")
public class MailUtilEmailTest {

  // -------------------------------------------------------------------------
  // getProperties
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getProperties")
  class GetProperties {

    @Test
    @DisplayName("sslEnabled=true (port 465): sets a direct-SSL socket factory, "
        + "not STARTTLS")
    void sslEnabled_setsSocketFactory() {
      MailUtilEmailServer server = new MailUtilEmailServer("smtp.test.com", "465", true, true, true);
      MailUtilEmail email = new MailUtilEmail(server,
          new MailUtilEmailContent("sender@test.com", null), new MailUtilEmailSettings(false));

      Properties props = email.getProperties();

      assertThat(props.getProperty("mail.smtp.socketFactory.class"))
          .isEqualTo("javax.net.ssl.SSLSocketFactory");
      assertThat(props.getProperty("mail.smtp.socketFactory.fallback")).isEqualTo("false");
      assertThat(props.getProperty("mail.smtp.socketFactory.port")).isEqualTo("465");
      assertThat(props.getProperty("mail.smtp.starttls.enable")).isNull();
      assertThat(props.getProperty("mail.smtp.starttls.required")).isNull();
    }

    @Test
    @DisplayName("sslEnabled=false (port 587): enables STARTTLS with starttls.required "
        + "following starttlsRequired, not the SSL socket factory")
    void sslDisabled_setsStarttls() {
      MailUtilEmailServer server =
          new MailUtilEmailServer("smtp.test.com", "587", false, true, true);
      MailUtilEmail email = new MailUtilEmail(server,
          new MailUtilEmailContent("sender@test.com", null), new MailUtilEmailSettings(false));

      Properties props = email.getProperties();

      assertThat(props.getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
      assertThat(props.getProperty("mail.smtp.starttls.required")).isEqualTo("true");
      assertThat(props.getProperty("mail.smtp.socketFactory.class")).isNull();
    }

    @Test
    @DisplayName("sslEnabled=false, starttlsRequired=false: starttls.required "
        + "reflects the opt-out")
    void sslDisabled_starttlsNotRequired() {
      MailUtilEmailServer server =
          new MailUtilEmailServer("smtp.test.com", "587", false, true, false);
      MailUtilEmail email = new MailUtilEmail(server,
          new MailUtilEmailContent("sender@test.com", null), new MailUtilEmailSettings(false));

      Properties props = email.getProperties();

      assertThat(props.getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
      assertThat(props.getProperty("mail.smtp.starttls.required")).isEqualTo("false");
    }

    @Test
    @DisplayName("always verifies the server's certificate matches its hostname, "
        + "regardless of sslEnabled")
    void alwaysChecksServerIdentity() {
      MailUtilEmailServer server =
          new MailUtilEmailServer("smtp.test.com", "587", false, true, true);
      MailUtilEmail email = new MailUtilEmail(server,
          new MailUtilEmailContent("sender@test.com", null), new MailUtilEmailSettings(false));

      Properties props = email.getProperties();

      assertThat(props.getProperty("mail.smtp.ssl.checkserveridentity")).isEqualTo("true");
    }

    @Test
    @DisplayName("mail.smtp.from is set from the content's (possibly fallback) bounce address")
    void setsBounceAddress() {
      MailUtilEmailServer server =
          new MailUtilEmailServer("smtp.test.com", "587", false, true, true);
      MailUtilEmail email = new MailUtilEmail(server,
          new MailUtilEmailContent("sender@test.com", "bounce@test.com"),
          new MailUtilEmailSettings(false));

      Properties props = email.getProperties();

      assertThat(props.getProperty("mail.smtp.from")).isEqualTo("bounce@test.com");
    }

    @Test
    @DisplayName("basic connection properties (host, port, auth, debug) are set")
    void setsBasicConnectionProperties() {
      MailUtilEmailServer server =
          new MailUtilEmailServer("smtp.test.com", "587", false, true, true);
      MailUtilEmail email = new MailUtilEmail(server,
          new MailUtilEmailContent("sender@test.com", null), new MailUtilEmailSettings(true));

      Properties props = email.getProperties();

      assertThat(props.getProperty("mail.smtp.host")).isEqualTo("smtp.test.com");
      assertThat(props.getProperty("mail.smtp.port")).isEqualTo("587");
      assertThat(props.getProperty("mail.smtp.auth")).isEqualTo("true");
      assertThat(props.getProperty("mail.debug")).isEqualTo("true");
    }
  }

  // -------------------------------------------------------------------------
  // getDebugLogMessage
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getDebugLogMessage")
  class GetDebugLogMessage {

    @Test
    @DisplayName("returns a no-parameter message when detailLogMap is empty")
    void empty() {
      MailUtilEmail email = new MailUtilEmail(
          new MailUtilEmailServer("smtp.test.com", "587", false, true, true),
          new MailUtilEmailContent("sender@test.com", null), new MailUtilEmailSettings(false));

      assertThat(email.getDebugLogMessage()).isEqualTo(". (parameter none)");
    }

    @Test
    @DisplayName("joins detailLogMap entries as \"key = value\" pairs")
    void nonEmpty() {
      MailUtilEmail email = new MailUtilEmail(
          new MailUtilEmailServer("smtp.test.com", "587", false, true, true),
          new MailUtilEmailContent("sender@test.com", null), new MailUtilEmailSettings(false),
          Map.of("key1", "value1"));

      assertThat(email.getDebugLogMessage()).isEqualTo(": key1 = value1");
    }
  }
}
