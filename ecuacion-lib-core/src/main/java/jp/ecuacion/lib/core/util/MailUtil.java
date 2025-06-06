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

    // 形式上Exceptionをcatchしているが、throwsException = falseで渡しているのでメール送信エラーによる例外は上がらない。
    // なので、もし上がったらRuntimeExceptionとしている。
    try {
      sendMailCommon(errorMailAddressList, (List<String>) null, mailTitle,
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
      sendMailCommon(mailToList, (List<String>) null, envSpecStr + "Warn Message", content, true);
    } catch (Exception ex) {
      // 何もしない
    }
  }

  /**
   * Provides the mail-sending function.
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
  public static void sendMail(@Nullable List<String> mailToList, @Nullable List<String> mailCcList,
      @RequireNonnull String title, @Nullable String content) throws Exception {

    sendMailCommon(mailToList, mailCcList, title, content, true);
  }

  private static void sendMailCommon(@Nullable List<String> mailToList,
      @Nullable List<String> mailCcList, @RequireNonnull String title, @Nullable String content,
      boolean throwsException) throws Exception {
    ObjectsUtil.requireNonNull(title);

    // Either mailToList or mailCcList need to have one element at least
    if ((mailToList == null || mailToList.size() == 0)
        && (mailCcList == null || mailCcList.size() == 0)) {
      throw new EclibRuntimeException(
          "Either mailToList or mailCcList need to have at least one element.");
    }

    String mailFrom = PropertyFileUtil.getApplication(APP_PREFIX + "smtp.sender");
    String pass = PropertyFileUtil.getApplication(APP_PREFIX + "smtp.password");

    // サーバ接続設定
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

    // javaMail設定
    boolean debug = (PropertyFileUtil.hasApplication(APP_PREFIX + "debug"))
        ? Boolean.valueOf(PropertyFileUtil.getApplication(APP_PREFIX + "debug"))
        : false;

    sendMailInternal(mailFrom, pass, mailToList, mailCcList, title, content,
        new MailUtilEmail(serverInfo, new MailUtilEmailContent(mailFrom),
            new MailUtilEmailSettings(debug)),
        throwsException);
  }

  /*
   * メール送信処理。 メールは業務的に付帯的な処理であることが多く、メール処理に失敗したために全体処理が落ちるようなことがないよう、 全ての例外をcatchする
   * また、エラー発生時のログ出力もこの中で行う。 多分これを呼び出す必要はないと思うのでprivateにしてある。必要があればpublicに変更してもよい。 throwsException =
   * trueの場合は、
   */
  private static void sendMailInternal(String mailFrom, String pass,
      @Nonnull List<String> mailToList, List<String> mailCcList, String title, String content,
      MailUtilEmail emailInfo, boolean throwsException) throws Exception {
    try {
      // 送信先が一人もいなければ終了
      if ((mailToList == null || mailToList.size() == 0)
          && (mailCcList == null || mailCcList.size() == 0)) {
        System.out.println("mail:no TO or CC specified.");
        return;
      }

      Objects.requireNonNull(mailToList);

      // 各種設定値を取得。SystemPropertyでoverride可能とする

      Session session = null;
      // 認証のあるなしで処理を変更
      if (emailInfo.getServerInfo().isNeedsAuthentication()) {
        session = Session.getInstance(emailInfo.getProperties(), new MyAuth(mailFrom, pass));
      } else {
        session = Session.getDefaultInstance(emailInfo.getProperties());
      }

      session.setDebug(emailInfo.getSettingInfo().getOutputsDebugLog());
      // ログ出力結果をDetailLogに流す
      session.setDebugOut(new PrintStream(new MailUtilLogOutputStream(), true, "UTF-8"));

      // mailToArrからInternetAddressインスタンスの配列を作成、格納
      InternetAddress[] addressTo = new InternetAddress[mailToList.size()];
      for (int i = 0; i < mailToList.size(); i++) {
        addressTo[i] = new InternetAddress(mailToList.get(i));
      }

      // messageの作成
      Message msg = null;
      msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(mailFrom));
      msg.setSentDate(new Date());
      msg.setRecipients(Message.RecipientType.TO, addressTo);
      ((MimeMessage) msg).setSubject(title, "UTF-8");
      ((MimeMessage) msg).setText(content, "UTF-8");

      // エラーが発生した場合はリトライ処理を行う
      for (int i = 0; i < 3; i++) {
        try {
          // smtpサーバへ接続・送信
          sendMailToSmtp(emailInfo.getServerInfo().isNeedsAuthentication(), msg,
              emailInfo.getServerInfo().getSmtpServer(), mailFrom, pass, session, addressTo);
          break;
        } catch (Exception e) {
          if (i < 2) {
            dtlLog.info("メール送信に失敗しました。リトライします。");

          } else {
            dtlLog.info("メール送信に失敗しました。リトライしてもエラー発生継続のため終了します。");
            throw e;
          }
        }
      }

      // 正常終了をログに残すためにログ出力
      dtlLog.trace("Mail successfully sent" + emailInfo.getDebugLogMessage());

    } catch (Throwable th1) {

      if (throwsException) {
        // エラーログ出力
        LogUtil.logSystemError(dtlLog, th1);
        throw th1;

      } else {
        try {
          LogUtil.logSystemError(dtlLog, th1);

        } catch (Throwable th2) {
          // ここまでくるとどうにもならないので標準出力に出力。それもダメなら何もしない
          try {
            th2.printStackTrace();

          } catch (Throwable th3) {
            // 何もしない
          }
        }
      }
    }
  }

  private static void sendMailToSmtp(boolean doesNeedAuthentication, Message msg, String smtpSrv,
      String mailFrom, String pass, Session session, InternetAddress[] addressTo)
      throws NoSuchProviderException, MessagingException {
    // smtpサーバへ接続・送信
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
