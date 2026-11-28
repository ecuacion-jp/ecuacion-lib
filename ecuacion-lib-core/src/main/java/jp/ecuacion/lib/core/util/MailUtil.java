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

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.util.internal.MailUtilEmail;
import jp.ecuacion.lib.core.util.internal.MailUtilEmailContent;
import jp.ecuacion.lib.core.util.internal.MailUtilEmailServer;
import jp.ecuacion.lib.core.util.internal.MailUtilEmailSettings;
import jp.ecuacion.lib.core.util.internal.MailUtilLogOutputStream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides mail-related utility methods.
 */
public class MailUtil {
  private static final DetailLogger dtlLog = new DetailLogger(MailUtil.class);
  private static final String APP_PREFIX = "jp.ecuacion.lib.core.mail.";

  /**
   * Holds all parameters needed to send mail via {@link MailUtil}.
   *
   * <p>Use this when you want to supply SMTP settings and application-level mail settings
   *     programmatically instead of reading them from {@code application.properties}.</p>
   *
   * @param smtpServer SMTP server hostname
   * @param port SMTP port (e.g. 587 for STARTTLS, 465 for SSL)
   * @param sslEnabled {@code true} to use SSL (port 465), {@code false} for STARTTLS (port 587)
   * @param needsAuthentication {@code true} if SMTP authentication is required
   * @param starttlsRequired when {@code sslEnabled} is {@code false}, {@code true} makes the
   *     connection fail rather than fall back to plaintext when the server doesn't support
   *     STARTTLS. Defaults to {@code true} everywhere this record is built by ecuacion
   *     modules; set to {@code false} only for a server that is known not to support
   *     STARTTLS (e.g. a local test relay) — doing so lets SMTP authentication happen over
   *     an unencrypted connection, exposing the password on the wire. Ignored when
   *     {@code sslEnabled} is {@code true}.
   * @param sender From address
   * @param password SMTP password
   * @param bounceAddress bounce/delivery-failure notification address, may be {@code null}
   * @param debug {@code true} to enable JavaMail debug output. This logs the full SMTP
   *     protocol exchange, including the Base64-encoded credentials sent during
   *     authentication — do not enable in production.
   * @param titlePrefix prefix prepended to the mail subject
   * @param addressCsvOnSystemError comma-separated To addresses for system-error notifications
   */
  public record MailUtilConfig(String smtpServer, int port, boolean sslEnabled,
      boolean needsAuthentication, boolean starttlsRequired, String sender, String password,
      @Nullable String bounceAddress, boolean debug, String titlePrefix,
      String addressCsvOnSystemError) {
  }

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
   * <p>Exceptions occurred while sending the mail are logged and not thrown
   *     so as not to get in the way of the error handling.
   *     Exceptions caused by the settings (like a missing key in
   *     {@code application.properties}) are thrown as {@code RuntimeException}.</p>
   *
   * @param throwable throwable. Can be {@code null},
   *     since error handling should not throw an exception even in that case.
   */
  public static void sendErrorMail(@Nullable Throwable throwable) {
    sendErrorMail(throwable, (String) null);
  }

  /**
   * Sends an error mail using the supplied {@link MailUtilConfig} instead of reading settings
   * from {@code application.properties}.
   *
   * @param throwable throwable. Can be {@code null}.
   * @param config mail configuration
   * @see #sendErrorMail(Throwable)
   */
  public static void sendErrorMail(@Nullable Throwable throwable, MailUtilConfig config) {
    sendErrorMail(throwable, null, config);
  }

  /**
   * Sends an error mail adding an additional message to it,
   *     using the supplied {@link MailUtilConfig} instead of reading settings
   *     from {@code application.properties}.
   *
   * @param throwable throwable. Can be {@code null}.
   * @param additionalMessage additional message,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case of {@code null} no additional message is output.
   * @param config mail configuration
   * @see #sendErrorMail(Throwable)
   */
  public static void sendErrorMail(@Nullable Throwable throwable,
      @Nullable String additionalMessage, MailUtilConfig config) {
    Objects.requireNonNull(config);

    List<@NonNull String> errorMailAddressList =
        splitAddressCsv(config.addressCsvOnSystemError());

    if (errorMailAddressList.isEmpty()) {
      return;
    }

    String mailTitle = config.titlePrefix() + "A system error has occurred.";

    try {
      sendMailCommon(errorMailAddressList, (List<@NonNull String>) null, false, mailTitle,
          getErrorMailContent(throwable, additionalMessage), false, config);

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Sends an error mail adding an additional message to it.
   *
   * @param throwable throwable. Can be {@code null}.
   * @param additionalMessage additional message,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case of {@code null} no additional message is output.
   * @see #sendErrorMail(Throwable)
   */
  public static void sendErrorMail(@Nullable Throwable throwable,
      @Nullable String additionalMessage) {
    List<@NonNull String> errorMailAddressList =
        splitAddressCsv(getApp("address-csv-on-system-error"));

    if (errorMailAddressList.isEmpty()) {
      return;
    }

    String mailTitle = getApp("title-prefix") + "A system error has occurred.";

    try {
      sendMailCommon(errorMailAddressList, (List<@NonNull String>) null, false, mailTitle,
          getErrorMailContent(throwable, additionalMessage), false);

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /*
   * Splits comma-separated addresses.
   * Each address is trimmed, and empty ones (like "" or "a@example.com,,b@example.com") are
   * ignored.
   */
  private static List<@NonNull String> splitAddressCsv(String csv) {
    return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
  }

  private static String getErrorMailContent(@Nullable Throwable e,
      @Nullable String additionalMessage) {
    return ExceptionLogUtil.getErrLogString(e, additionalMessage, Locale.getDefault()) + "\n";
  }

  /**
   * Sends a warn mail.
   *
   * <p>{@code mailToList} is passed as-is to {@link jakarta.mail.internet.InternetAddress}
   *     without sanitization; do not pass untrusted (e.g. end-user-supplied) values.</p>
   *
   * <p>Exceptions occurred while sending the mail are logged and not thrown.
   *     Exceptions caused by the settings (like a missing key in
   *     {@code application.properties}) are thrown as {@code RuntimeException}.</p>
   *
   * @param content content, may be {@code null} if no mailbody content needed.
   * @param mailToList list of mailadresses used for "TO" address
   */
  public static void sendWarnMail(@Nullable String content, List<@NonNull String> mailToList) {
    ObjectsUtil.requireSizeNonZero(mailToList);

    String envSpecStr = getApp("title-prefix");

    try {
      sendMailCommon(mailToList, (List<@NonNull String>) null, false, envSpecStr + "Warn Message",
          content, false);

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Sends a warn mail using the supplied {@link MailUtilConfig} instead of reading settings
   * from {@code application.properties}.
   *
   * <p>{@code mailToList} is passed as-is to {@link jakarta.mail.internet.InternetAddress}
   *     without sanitization; do not pass untrusted (e.g. end-user-supplied) values.</p>
   *
   * <p>Exceptions occurred while sending the mail are logged and not thrown.</p>
   *
   * @param content content, may be {@code null} if no mailbody content needed.
   * @param mailToList list of mailadresses used for "TO" address
   * @param config mail configuration
   */
  public static void sendWarnMail(@Nullable String content, List<@NonNull String> mailToList,
      MailUtilConfig config) {
    ObjectsUtil.requireSizeNonZero(mailToList);
    Objects.requireNonNull(config);

    try {
      sendMailCommon(mailToList, (List<@NonNull String>) null, false,
          config.titlePrefix() + "Warn Message", content, false, config);

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Provides sending text-format mail function.
   *
   * <p>The following settings are needed to application.properties
   *     to send mails with this object.</p>
   *
   * <pre>
   * jp.ecuacion.lib.core.mail.smtp.server=smtp.gmail.com
   * jp.ecuacion.lib.core.mail.smtp.port=465
   * jp.ecuacion.lib.core.mail.smtp.sender=info@ecuacion.jp
   * jp.ecuacion.lib.core.mail.smtp.password=(password)
   * # true or false
   * jp.ecuacion.lib.core.mail.smtp.authentication=true
   * # Normally "true" if the port number is 465, "false" if 587
   * jp.ecuacion.lib.core.mail.smtp.ssl-enabled=true
   * jp.ecuacion.lib.core.mail.title-prefix=[app-name: staging environment]
   * jp.ecuacion.lib.core.mail.address-csv-on-system-error=info@ecuacion.jp
   * # true or false. Logs the full SMTP protocol exchange, including the Base64-encoded
   * # credentials sent during authentication -- do not enable in production.
   * jp.ecuacion.lib.core.mail.debug=false
   * # true or false. Applies only when smtp.ssl-enabled is false (STARTTLS on port 587).
   * # true (default) fails the connection rather than falling back to plaintext when the
   * # server doesn't support STARTTLS. Setting this to false lets SMTP authentication
   * # happen over an unencrypted connection if the server lacks STARTTLS support --
   * # only do so for a server known not to support it (e.g. a local test relay), never
   * # in production.
   * jp.ecuacion.lib.core.mail.smtp.starttls-required=true
   * </pre>
   *
   * <p>{@code mailToList}, {@code mailCcList}, and {@code title} are passed as-is to
   *     the underlying {@link jakarta.mail.internet.MimeMessage} (addresses via
   *     {@link jakarta.mail.internet.InternetAddress}) without sanitization; do not pass
   *     untrusted (e.g. end-user-supplied) values without validating or escaping them
   *     first.</p>
   *
   * @param mailToList mailToList.
   *     Either mailToList or mailCcList need to have at least one element.
   * @param mailCcList mailCcList.
   *     Either mailToList or mailCcList need to have at least one element.
   * @param title title
   * @param content content, may be {@code null} if no content needed.
   * @throws Exception Exception
   */
  public static void sendTextMail(@Nullable List<@NonNull String> mailToList,
      @Nullable List<@NonNull String> mailCcList, String title, @Nullable String content)
      throws Exception {

    sendMailCommon(mailToList, mailCcList, false, title, content, true);
  }

  /**
   * Provides sending text-format mail function using the supplied {@link MailUtilConfig}
   *     instead of reading settings from {@code application.properties}.
   *
   * <p>{@code mailToList}, {@code mailCcList}, and {@code title} are passed as-is to
   *     the underlying {@link jakarta.mail.internet.MimeMessage} without sanitization;
   *     do not pass untrusted (e.g. end-user-supplied) values without validating or escaping
   *     them first.</p>
   *
   * @param mailToList mailToList.
   *     Either mailToList or mailCcList need to have at least one element.
   * @param mailCcList mailCcList.
   *     Either mailToList or mailCcList need to have at least one element.
   * @param title title
   * @param content content, may be {@code null} if no content needed.
   * @param config mail configuration
   * @throws Exception Exception
   */
  public static void sendTextMail(@Nullable List<@NonNull String> mailToList,
      @Nullable List<@NonNull String> mailCcList, String title, @Nullable String content,
      MailUtilConfig config) throws Exception {

    sendMailCommon(mailToList, mailCcList, false, title, content, true, config);
  }

  /**
   * Provides sending html-format mail function.
   *
   * @see #sendTextMail(List, List, String, String)
   */
  public static void sendHtmlMail(@Nullable List<@NonNull String> mailToList,
      @Nullable List<@NonNull String> mailCcList, String title, @Nullable String content)
      throws Exception {

    sendMailCommon(mailToList, mailCcList, true, title, content, true);
  }

  /**
   * Provides sending html-format mail function using the supplied {@link MailUtilConfig}
   *     instead of reading settings from {@code application.properties}.
   *
   * @see #sendTextMail(List, List, String, String, MailUtilConfig)
   */
  public static void sendHtmlMail(@Nullable List<@NonNull String> mailToList,
      @Nullable List<@NonNull String> mailCcList, String title, @Nullable String content,
      MailUtilConfig config) throws Exception {

    sendMailCommon(mailToList, mailCcList, true, title, content, true, config);
  }

  private static void sendMailCommon(@Nullable List<@NonNull String> mailToList,
      @Nullable List<@NonNull String> mailCcList, boolean isHtmlFormat, String title,
      @Nullable String content, boolean throwsException, MailUtilConfig config)
      throws Exception {
    Objects.requireNonNull(title);
    Objects.requireNonNull(config);
    requireRecipients(mailToList, mailCcList);
    String body = content == null ? "" : content;

    MailSender sender = mailSender;
    if (sender != null) {
      sendSafely(() -> sender.send(mailToList, mailCcList, isHtmlFormat, title, body),
          throwsException);
      return;
    }

    MailUtilEmailServer serverInfo =
        new MailUtilEmailServer(config.smtpServer(), String.valueOf(config.port()),
            config.sslEnabled(), config.needsAuthentication(), config.starttlsRequired());

    MailUtilEmail emailInfo = new MailUtilEmail(serverInfo,
        new MailUtilEmailContent(config.sender(), config.bounceAddress()),
        new MailUtilEmailSettings(config.debug()));

    sendSafely(() -> sendMailInternal(config.sender(), config.password(), mailToList, mailCcList,
        isHtmlFormat, title, body, emailInfo), throwsException);
  }

  private static void sendMailCommon(@Nullable List<@NonNull String> mailToList,
      @Nullable List<@NonNull String> mailCcList, boolean isHtmlFormat, String title,
      @Nullable String content, boolean throwsException) throws Exception {
    Objects.requireNonNull(title);
    requireRecipients(mailToList, mailCcList);
    String body = content == null ? "" : content;

    MailSender sender = mailSender;
    if (sender != null) {
      sendSafely(() -> sender.send(mailToList, mailCcList, isHtmlFormat, title, body),
          throwsException);
      return;
    }

    String mailFrom = getApp("smtp.sender");
    String pass = getApp("smtp.password");
    String bounceAddress = hasApp("smtp.bounce-address") ? getApp("smtp.bounce-address") : null;

    // Server connection settings
    MailUtilEmailServer serverInfo =
        new MailUtilEmailServer(getApp("smtp.server"), getApp("smtp.port"),
            hasApp("smtp.ssl-enabled") ? Boolean.parseBoolean(getApp("smtp.ssl-enabled")) : false,
            Boolean.parseBoolean(getApp("smtp.authentication")),
            hasApp("smtp.starttls-required")
                ? Boolean.parseBoolean(getApp("smtp.starttls-required"))
                : true);

    // javaMail settings
    boolean debug = hasApp("debug") && Boolean.parseBoolean(getApp("debug"));

    MailUtilEmail emailInfo = new MailUtilEmail(serverInfo,
        new MailUtilEmailContent(mailFrom, bounceAddress), new MailUtilEmailSettings(debug));

    sendSafely(() -> sendMailInternal(mailFrom, pass, mailToList, mailCcList, isHtmlFormat, title,
        body, emailInfo), throwsException);
  }

  /* Either mailToList or mailCcList need to have one element at least. */
  private static void requireRecipients(@Nullable List<@NonNull String> mailToList,
      @Nullable List<@NonNull String> mailCcList) {
    if ((mailToList == null || mailToList.isEmpty())
        && (mailCcList == null || mailCcList.isEmpty())) {
      throw new RuntimeException(
          "Either mailToList or mailCcList need to have at least one element.");
    }
  }

  private static String getApp(String postfix) {
    return PropertiesFileUtil.getApplication(APP_PREFIX + postfix);
  }

  private static boolean hasApp(String postfix) {
    return PropertiesFileUtil.hasApplication(APP_PREFIX + postfix);
  }

  /** Mail sending procedure, which may throw an exception. */
  @FunctionalInterface
  private interface SendingProcedure {
    void send() throws Exception;
  }

  /**
   * Executes the mail sending procedure.
   *
   * <p>Since mail sending procedure is an additional function
   *     when sending system-error mails for administrators,
   *     it has the option that all the exceptions can be caught
   *     not to get in the way of business logic.<br>
   *     In both cases the exception is logged.</p>
   */
  private static void sendSafely(SendingProcedure procedure, boolean throwsException)
      throws Exception {
    try {
      procedure.send();

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

  /** Mail sending internal procedure. */
  private static void sendMailInternal(String mailFrom, String pass,
      @Nullable List<@NonNull String> mailToList, @Nullable List<@NonNull String> mailCcList,
      boolean isHtmlFormat, String title, String content, MailUtilEmail emailInfo)
      throws Exception {

    Session session = null;
    // Change procedure whether an authentication is needed or not.
    // Both branches use getInstance (not getDefaultInstance), which always creates a
    // fresh Session from the given Properties. getDefaultInstance would instead return
    // a JVM-wide shared Session created on the first call, silently ignoring the
    // Properties passed on every subsequent call.
    if (emailInfo.getServerInfo().isNeedsAuthentication()) {
      session = Session.getInstance(emailInfo.getProperties(), new MyAuth(mailFrom, pass));
    } else {
      session = Session.getInstance(emailInfo.getProperties());
    }

    session.setDebug(emailInfo.getSettingInfo().getOutputsDebugLog());
    // Pass log output to DetailLogger.
    session.setDebugOut(
        new PrintStream(new MailUtilLogOutputStream(), true, StandardCharsets.UTF_8));

    Message msg =
        createMessage(session, mailFrom, mailToList, mailCcList, isHtmlFormat, title, content);

    // Retry when an exception occurs.
    for (int i = 0; i < 3; i++) {
      try {
        // Connect and send to smtp server.
        sendMailToSmtp(emailInfo.getServerInfo().isNeedsAuthentication(), msg,
            emailInfo.getServerInfo().getSmtpServer(), mailFrom, pass, session);
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
  }

  /* Creates a message. Package-private for tests. */
  static MimeMessage createMessage(Session session, String mailFrom,
      @Nullable List<@NonNull String> mailToList, @Nullable List<@NonNull String> mailCcList,
      boolean isHtmlFormat, String title, String content) throws MessagingException {
    MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(mailFrom));
    @SuppressWarnings("JavaUtilDate")
    var sentDate = new Date();
    msg.setSentDate(sentDate);
    if (mailToList != null && !mailToList.isEmpty()) {
      msg.setRecipients(Message.RecipientType.TO, toAddresses(mailToList));
    }
    if (mailCcList != null && !mailCcList.isEmpty()) {
      msg.setRecipients(Message.RecipientType.CC, toAddresses(mailCcList));
    }
    msg.setSubject(title, "UTF-8");
    if (isHtmlFormat) {
      msg.setContent(content, "text/html; charset=\"UTF-8\"");
    } else {
      msg.setText(content, "UTF-8");
    }

    return msg;
  }

  private static InternetAddress[] toAddresses(List<@NonNull String> addressList)
      throws AddressException {
    InternetAddress[] addresses = new InternetAddress[addressList.size()];
    for (int i = 0; i < addressList.size(); i++) {
      addresses[i] = new InternetAddress(addressList.get(i));
    }

    return addresses;
  }

  /** Strategy interface for sending mail; package-private to allow substitution in tests. */
  @FunctionalInterface
  interface MailSender {
    void send(@Nullable List<@NonNull String> mailToList,
        @Nullable List<@NonNull String> mailCcList, boolean isHtmlFormat, String title,
        String content) throws Exception;
  }

  static @Nullable MailSender mailSender = null;

  private static void sendMailToSmtp(boolean doesNeedAuthentication, Message msg, String smtpSrv,
      String mailFrom, String pass, Session session) throws MessagingException {
    // Connect and send to smtp server.
    // The recipients are both TO and CC.
    if (doesNeedAuthentication) {
      try (Transport tp = session.getTransport("smtp")) {
        tp.connect(smtpSrv, mailFrom, pass);
        tp.sendMessage(msg, msg.getAllRecipients());
      }

    } else {
      Transport.send(msg);
    }
  }

  static class MyAuth extends Authenticator {

    private final String mailFrom;
    private final String pass;

    public MyAuth(String mailFrom, String pass) {
      this.mailFrom = mailFrom;
      this.pass = pass;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(mailFrom, pass);
    }
  }
}
