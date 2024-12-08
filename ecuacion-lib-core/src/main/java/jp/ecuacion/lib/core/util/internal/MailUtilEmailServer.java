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

public final class MailUtilEmailServer {

  private String smtpServer;
  private String port;
  private boolean isSslEnabled;
  private boolean needsAuthentication;
  private boolean checksCertificate;

  /** 必須項目のみのコンストラクタ。 */
  public MailUtilEmailServer(String smtpServer, String port, boolean isSslEnabled,
      boolean needsAuthentication, boolean checksCertificate) {
    this.smtpServer = smtpServer;
    this.port = port;
    this.isSslEnabled = isSslEnabled;
    this.needsAuthentication = needsAuthentication;
    this.checksCertificate = checksCertificate;
  }

  /** 全項目のコンストラクタ。 */
  public MailUtilEmailServer(String smtpServer, String port, boolean isSslEnabled,
      boolean needsAuthentication, boolean checksCertificate, String bounceMailAddress) {
    this.smtpServer = smtpServer;
    this.port = port;
    this.isSslEnabled = isSslEnabled;
    this.needsAuthentication = needsAuthentication;
    this.checksCertificate = checksCertificate;
  }

  public String getSmtpServer() {
    return smtpServer;
  }

  public String getPort() {
    return port;
  }

  public boolean isSslEnabled() {
    return isSslEnabled;
  }

  public boolean isNeedsAuthentication() {
    return needsAuthentication;
  }

  public boolean isChecksCertificate() {
    return checksCertificate;
  }
}
