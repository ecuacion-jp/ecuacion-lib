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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MailUtilEmail {
  private HashMap<String, String> detailLogMap = new HashMap<>();
  private MailUtilEmailServer serverInfo;
  private MailUtilEmailContent contentInfo;
  private MailUtilEmailSettings settingInfo;

  public MailUtilEmail() {

  }

  public MailUtilEmail(MailUtilEmailServer emailServer, MailUtilEmailContent emailContent,
      MailUtilEmailSettings emailSettings) {
    this.serverInfo = emailServer;
    this.contentInfo = emailContent;
    this.settingInfo = emailSettings;
  }

  public MailUtilEmail(MailUtilEmailServer emailServer, MailUtilEmailContent emailContent,
      MailUtilEmailSettings emailSettings, HashMap<String, String> detailLogMap) {
    this.serverInfo = emailServer;
    this.contentInfo = emailContent;
    this.settingInfo = emailSettings;
    this.detailLogMap = new HashMap<>(detailLogMap);
  }

  public Properties getProperties() {
    Properties props = new Properties();
    props.setProperty("mail.smtp.host", serverInfo.getSmtpServer());
    props.setProperty("mail.smtp.port", serverInfo.getPort());
    props.setProperty("mail.smtp.auth",
        Boolean.valueOf(serverInfo.isNeedsAuthentication()).toString());
    String bounce = contentInfo.getBounceMailAddress();
    if (bounce != null && !bounce.equals("")) {
      props.setProperty("mail.smtp.from", bounce);
    }

    props.setProperty("mail.debug", Boolean.valueOf(settingInfo.getOutputsDebugLog()).toString());

    // タイムアウト設定
    props.setProperty("mail.smtp.connectiontimeout", "5000");
    props.setProperty("mail.smtp.timeout", "5000");

    if (serverInfo.isSslEnabled()) {
      props.setProperty("mail.smtp.starttls.enable", "true");
      props.setProperty("mail.smtp.socketFactory.fallback", "false");
      props.setProperty("mail.smtp.socketFactory.port", serverInfo.getPort());

      if (serverInfo.isChecksCertificate()) {
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      } else {
        // 証明書のチェックをしないSSLSocketFactoryを設定
        props.setProperty("mail.smtp.socketFactory.class",
            "jp.ecuacion.framework.common.common.util.AuthUtilDummySSLSocketFactory");
      }
    }

    return props;
  }

  public String getDebugLogMessage() {
    // detailLogMapに値がない場合は終了
    if (detailLogMap == null || detailLogMap.size() == 0) {
      return ". (parameter none)";
    }

    StringBuilder sb = new StringBuilder();
    sb.append(": ");
    boolean is1st = true;
    for (Map.Entry<String, String> entry : detailLogMap.entrySet()) {
      if (is1st) {
        is1st = false;

      } else {
        sb.append(", ");
      }

      sb.append(entry.getKey() + " = " + entry.getValue());
    }

    return sb.toString();
  }

  public MailUtilEmailServer getServerInfo() {
    return serverInfo;
  }

  public void setServerInfo(MailUtilEmailServer serverInfo) {
    this.serverInfo = serverInfo;
  }

  public MailUtilEmailContent getContentInfo() {
    return contentInfo;
  }

  public void setContentInfo(MailUtilEmailContent contentInfo) {
    this.contentInfo = contentInfo;
  }

  public MailUtilEmailSettings getSettingInfo() {
    return settingInfo;
  }

  public void setSettingInfo(MailUtilEmailSettings settingInfo) {
    this.settingInfo = settingInfo;
  }

  public HashMap<String, String> getDetailLogMap() {
    return new HashMap<>(detailLogMap);
  }

  public void setDetailLogMap(HashMap<String, String> detailLogMap) {
    this.detailLogMap = new HashMap<>(detailLogMap);
  }
}
