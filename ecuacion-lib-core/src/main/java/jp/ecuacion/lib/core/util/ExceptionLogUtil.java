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

import java.util.Locale;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.logging.internal.AbstractLogger;
import org.jspecify.annotations.Nullable;

/**
 * Handles Exception log strings.
 */
public class ExceptionLogUtil {

  private static final String RT = "\n";

  /**
   * Returns strings or error log.
   * 
   * @param throwable throwable. {@code null} acceptable to avoid system error on logging procedure.
   * @param additionalMessage additional message,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case of {@code null} no additional message is output.
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @return error log string
   */
  public static String getErrLogString(@Nullable Throwable throwable,
      @Nullable String additionalMessage, @Nullable Locale locale) {
    return getErrLogString(throwable, additionalMessage, locale, null);
  }

  /**
   * Returns strings or error log.
   * 
   * @param throwable throwable
   * @param additionalMessage additional message,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case of {@code null} no additional message is output.
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param packagesShown packages shown in the stack traces.
   *     This is used when the log displaying area is small.
   * @return error log string
   */
  public static String getErrLogString(@Nullable Throwable throwable,
      @Nullable String additionalMessage, @Nullable Locale locale,
      @Nullable Integer packagesShown) {
    ObjectsUtil.requireNonNull(throwable);
    locale = (locale == null) ? Locale.ENGLISH : locale;

    StringBuilder sb = new StringBuilder();

    sb.append(ExceptionUtil.SYSTEM_ERROR_OCCURED_SIGN + RT);
    if (additionalMessage != null) {
      sb.append(additionalMessage + RT);
      sb.append(RT);
    }

    sb.append(RT);

    getMessageAndStackTraceStringRecursively(sb, throwable, locale, packagesShown);

    String rtn = sb.toString();
    return rtn;
  }

  /**
   * Adds Throwable message and stackTrace string to argument stringBuilder 
   *     for a throwable and its causes.
   * 
   * @param sb StringBuilder
   * @param th throwable
   * @param locale locale, may be null 
   * @param packagesShown null means all package of a class is shown 
   *     like "at jp.ecuacion.lib.core.util.ExceptionUtil.main(ExceptionUtil.java:468)".
   *     "0" shows no packages like "at ..main(ExceptionUtil.java:468)".
   *     "1" shows 1 package part like "at jp...main(ExceptionUtil.java:468)".
   */
  public static void getMessageAndStackTraceStringRecursively(StringBuilder sb,
      @Nullable Throwable th, @Nullable Locale locale, @Nullable Integer packagesShown) {

    getMessageAndStackTraceString(sb, th, locale, packagesShown);

    // Also outputs for getCause().
    if (th != null && th.getCause() != null) {
      getMessageAndStackTraceStringRecursively(sb, th.getCause(), locale, packagesShown);
    }
  }

  /**
   * Adds Throwable message and stackTrace string to argument stringBuilder 
   *     for one throwable. (getCause() ignored)
   * 
   * @param sb StringBuilder
   * @param th throwable
   * @param locale locale, may be null 
   * @param packagesShown see getMessageAndStackTraceStringRecursively
   */
  @SuppressWarnings({"removal"})
  private static void getMessageAndStackTraceString(StringBuilder sb, @Nullable Throwable th,
      @Nullable Locale locale, @Nullable Integer packagesShown) {
    locale = (locale == null) ? Locale.ENGLISH : locale;

    // Call MultipleAppException#getMessage() explicitly
    // since getExceptionMessage skips the exception.
    String errMsg;
    if (th == null) {
      errMsg = AbstractLogger.NULL_THROWABLE_MESSAGE;

    } else if (th instanceof ViolationException) {
      errMsg = th.getClass().getCanonicalName()
          + ExceptionUtil.getMessageList((ViolationException) th, locale, true).toString();

    } else {
      // Legacy: remove MultipleAppException branch when MultipleAppException is retired.
      String legacyMsg = (th instanceof MultipleAppException)
          ? ((MultipleAppException) th).getMessage()
          : ExceptionUtil.getMessageList(th, locale, true).toString();
      errMsg = th.getClass().getCanonicalName() + legacyMsg;
    }

    sb.append(errMsg + RT);

    // Output stackTrace string
    getStackTraceString(sb, th, packagesShown);
  }

  /**
   * get stackTrace string for one throwable. (getCause() ignored)
   * 
   * @param sb StringBuilder
   * @param th throwable
   * @param packagesShown see getMessageAndStackTraceStringRecursively
   * @return stackTrace string
   */
  private static void getStackTraceString(StringBuilder sb, @Nullable Throwable th,
      @Nullable Integer packagesShown) {
    if (th == null) {
      return;
    }

    for (StackTraceElement ste : th.getStackTrace()) {
      String[] spl = ste.getClassName().split("\\.");
      String packageAndClass = ste.getClassName();
      if (packagesShown != null) {
        String packages = "";
        for (int i = 0; i < packagesShown; i++) {
          if (spl.length > i) {
            packages = packages + spl[i] + (spl.length - 1 == i ? "" : ".");
          }
        }

        packageAndClass = packages + (spl.length > packagesShown ? "." : "");
      }

      sb.append("\tat " + packageAndClass + "." + ste.getMethodName() + "(" + ste.getFileName()
          + ":" + ste.getLineNumber() + ")" + RT);
    }
  }

}
