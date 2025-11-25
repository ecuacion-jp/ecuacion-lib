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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.util.internal.MailUtilEmail;
import jp.ecuacion.lib.core.util.internal.MailUtilEmailContent;
import jp.ecuacion.lib.core.util.internal.MailUtilEmailServer;
import jp.ecuacion.lib.core.util.internal.MailUtilEmailSettings;
import jp.ecuacion.lib.core.util.internal.MailUtilLogOutputStream;

/**
 * Provides mail-related utility methods.
 */
public class MailUtil {
  private static DetailLogger dtlLog = new DetailLogger(MailUtil.class);
  private static final String APP_PREFIX = "jp.ecuacion.lib.core.mail.";

  /**
   * Prevents other classes from instantiating it.
   */
  private MailUtil() {}

  /**
   * Sends an error mail.
   * 
   * <p>This is used in exception handler 
   *     to notify system administrator the occurence of an error.</p>
   * 
   * <p>The mail is sent to the addresses defined in {@code application.properties}
   *     with key {@code jp.ecuacion.lib.core.mail.address-csv-on-system-error}.</p>
   *     
   * @param throwable throwable
   */
  public static void sendErrorMail(@RequireNonnull Throwable throwable) {
    sendErrorMail(throwable, null);
  }

  /**
   * Sends an error mail adding an additional message to it.
   * 
   * @param throwable throwable.
   * @param additionalMessage additional message,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case of {@code null} no additional message is output.
   */
  public static void sendErrorMail(@RequireNonnull Throwable throwable,
      @Nullable String additionalMessage) {
    ObjectsUtil.requireNonNull(throwable);

    List<String> errorMailAddressList = Arrays.asList(
        PropertyFileUtil.getApplication(APP_PREFIX + "address-csv-on-system-error").split(","));

    if (errorMailAddressList == null || errorMailAddressList.size() == 0) {
      return;
    }

    String mailTitle = PropertyFileUtil.getApplication(APP_PREFIX + "title-prefix")
        + "A system error has occurred.";

    try {
      sendMailCommon(errorMailAddressList, (List<String>) null, false, mailTitle,
          getErrorMailContent(throwable, additionalMessage), false);

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Nonnull
  private static String getErrorMailContent(@RequireNonnull Throwable e,
      @Nullable String additionalMessage) {
    StringBuffer msgSb = new StringBuffer();
    msgSb.append(ExceptionUtil.getErrLogString(e, additionalMessage, Locale.getDefault()) + "\n");

    String rtn = msgSb.toString();
    return rtn;
  }

  /**
   * Sends a warn mail.
   * 
   * @param content content, may be {@code null} if no mailbody content needed.
   * @param mailToList list of mailadresses used for "TO" address
   */
  @Nonnull
  public static void sendWarnMail(@Nullable String content,
      @RequireNonnull List<String> mailToList) {
    ObjectsUtil.requireNonNull(mailToList);
    ObjectsUtil.requireSizeNonZero(mailToList);

    String envSpecStr = PropertyFileUtil.getApplication(APP_PREFIX + "title-prefix");

    try {
      sendMailCommon(mailToList, (List<String>) null, false, envSpecStr + "Warn Message", content,
          true);
    } catch (Exception ex) {
      // do nothing.
    }
  }

  /**
   * Provides sending text-format mail function.
   * 
   * <p>The following settings ars needed to application.properties 
   *     to send mails with this object.</p>
   * 
   * <pre>
   * # true or false
   * SMTP_AUTHENTICATION=true
   * #Normally "true" if the port number is 465, "false" if 587
   * SMTP_SSL_ENABLED=true
   * jp.ecuacion.lib.core.mail.smtp.server=smtp.gmail.com
   * jp.ecuacion.lib.core.mail.smtp.port=465
   * jp.ecuacion.lib.core.mail.smtp.sender=info@ecuacion.jp
   * jp.ecuacion.lib.core.mail.smtp.password=(password)
   * jp.ecuacion.lib.core.mail.title-prefix=[app-name: staging environment]
   * jp.ecuacion.lib.core.mail.address-csv-on-system-error=info@ecuacion.jp
   * </pre>
   * 
   * @param mailToList mailToList. 
   *     Either mailToList or mailCcList need to have at least one element.
   * @param mailCcList mailCcList. 
   *     Either mailToList or mailCcList need to have at least one element.
   * @param title title
   * @param content content, may be {@code null} if no content needed.
   * @throws Exception Exception
   */
  public static void sendTextMail(@Nullable List<String> mailToList,
      @Nullable List<String> mailCcList, @RequireNonnull String title, @Nullable String content)
      throws Exception {

    sendMailCommon(mailToList, mailCcList, false, title, content, true);
  }

  /**
   * Provides sending html-format mail function.
   * 
   * @see sendTextMail
   */
  public static void sendHtmlMail(@Nullable List<String> mailToList,
      @Nullable List<String> mailCcList, @RequireNonnull String title, @Nullable String content)
      throws Exception {

    sendMailCommon(mailToList, mailCcList, true, title, content, true);
  }

  private static void sendMailCommon(@Nullable List<String> mailToList,
      @Nullable List<String> mailCcList, boolean isHtmlFormat, @RequireNonnull String title,
      @Nullable String content, boolean throwsException) throws Exception {
    ObjectsUtil.requireNonNull(title);

    // Either mailToList or mailCcList need to have one element at least
    if ((mailToList == null || mailToList.size() == 0)
        && (mailCcList == null || mailCcList.size() == 0)) {
      throw new EclibRuntimeException(
          "Either mailToList or mailCcList need to have at least one element.");
    }

    String mailFrom = PropertyFileUtil.getApplication(APP_PREFIX + "smtp.sender");
    String pass = PropertyFileUtil.getApplication(APP_PREFIX + "smtp.password");

    // Server connection settings
    MailUtilEmailServer serverInfo = new MailUtilEmailServer(
        PropertyFileUtil.getApplication(APP_PREFIX + "smtp.server"),
        PropertyFileUtil.getApplication(APP_PREFIX + "smtp.port"),
        (PropertyFileUtil.hasApplication(APP_PREFIX + "smtp.ssl-enabled"))
            ? Boolean.parseBoolean(PropertyFileUtil.getApplication(APP_PREFIX + "smtp.ssl-enabled"))
            : false,
        Boolean.parseBoolean(PropertyFileUtil.getApplication(APP_PREFIX + "smtp.authentication")),
        Boolean
            .parseBoolean(PropertyFileUtil.getApplication(APP_PREFIX + "smtp.checks-certificate")),
        (PropertyFileUtil.hasApplication(APP_PREFIX + "smtp.bounce-address"))
            ? PropertyFileUtil.getApplication(APP_PREFIX + "smtp.bounce-address")
            : null);

    // javaMail settings
    boolean debug = (PropertyFileUtil.hasApplication(APP_PREFIX + "debug"))
        ? Boolean.valueOf(PropertyFileUtil.getApplication(APP_PREFIX + "debug"))
        : false;

    sendMailInternal(mailFrom, pass, mailToList, mailCcList, isHtmlFormat, title, content,
        new MailUtilEmail(serverInfo, new MailUtilEmailContent(mailFrom),
            new MailUtilEmailSettings(debug)),
        throwsException);
  }

  /**
   * Mail sending internal rocedure.
   * 
   * <p>Since mail sending procedure is an additinal function 
   *     when sending system-error mails for administrators, 
   *     it has the option that all the execptions can be catched 
   *     not to get in the way of business logic</p>
   */
  private static void sendMailInternal(String mailFrom, String pass,
      @Nonnull List<String> mailToList, List<String> mailCcList, boolean isHtmlFormat, String title,
      String content, MailUtilEmail emailInfo, boolean throwsException) throws Exception {
    try {
      // Finish when the number of senders is zero.
      if ((mailToList == null || mailToList.size() == 0)
          && (mailCcList == null || mailCcList.size() == 0)) {
        System.out.println("mail:no TO or CC specified.");
        return;
      }

      Objects.requireNonNull(mailToList);

      Session session = null;
      // Change procedure whether an authentication is needed or not.
      if (emailInfo.getServerInfo().isNeedsAuthentication()) {
        session = Session.getInstance(emailInfo.getProperties(), new MyAuth(mailFrom, pass));
      } else {
        session = Session.getDefaultInstance(emailInfo.getProperties());
      }

      session.setDebug(emailInfo.getSettingInfo().getOutputsDebugLog());
      // Pass log output to DetailLogger.
      session.setDebugOut(new PrintStream(new MailUtilLogOutputStream(), true, "UTF-8"));

      InternetAddress[] addressTo = new InternetAddress[mailToList.size()];
      for (int i = 0; i < mailToList.size(); i++) {
        addressTo[i] = new InternetAddress(mailToList.get(i));
      }

      // Create message
      Message msg = null;
      msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(mailFrom));
      msg.setSentDate(new Date());
      msg.setRecipients(Message.RecipientType.TO, addressTo);
      ((MimeMessage) msg).setSubject(title, "UTF-8");
      if (isHtmlFormat) {
        ((MimeMessage) msg).setContent(content, "text/html; charset=\"UTF-8\"");
      } else {
        ((MimeMessage) msg).setText(content, "UTF-8");
      }

      // Retry when an exception occurs.
      for (int i = 0; i < 3; i++) {
        try {
          // Connect and send to smtp server.
          sendMailToSmtp(emailInfo.getServerInfo().isNeedsAuthentication(), msg,
              emailInfo.getServerInfo().getSmtpServer(), mailFrom, pass, session, addressTo);
          break;
        } catch (Exception e) {
          if (i < 2) {
            dtlLog.info("Mail sending failed. Retry in 1 second.");
            Thread.sleep(1000);

          } else {
            dtlLog.info(
                "Mail sending failed. Procedure finished since retry did not improve anything.");
            throw e;
          }
        }
      }

      dtlLog.trace("Mail successfully sent" + emailInfo.getDebugLogMessage());

    } catch (Throwable th1) {

      if (throwsException) {
        // Output error log
        LogUtil.logSystemError(dtlLog, th1);
        throw th1;

      } else {
        try {
          LogUtil.logSystemError(dtlLog, th1);

        } catch (Throwable th2) {
          // When even loogging causes a system error, ignore the error and just log to standard
          // output.
          // If that also causes an exception, Do nothing.
          try {
            th2.printStackTrace();

          } catch (Throwable th3) {
            // do nothing.
          }
        }
      }
    }
  }

  private static void sendMailToSmtp(boolean doesNeedAuthentication, Message msg, String smtpSrv,
      String mailFrom, String pass, Session session, InternetAddress[] addressTo)
      throws NoSuchProviderException, MessagingException {
    // Connect and send to smtp server.
    if (doesNeedAuthentication) {
      Transport tp = session.getTransport("smtp");
      tp.connect(smtpSrv, mailFrom, pass);
      tp.sendMessage(msg, addressTo);
    } else {
      Transport.send(msg);
    }
  }

  static class MyAuth extends Authenticator {

    private String mailFrom;
    private String pass;

    public MyAuth(String mailFrom, String pass) {
      this.mailFrom = mailFrom;
      this.pass = pass;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(mailFrom, pass);
    }
  }
}
