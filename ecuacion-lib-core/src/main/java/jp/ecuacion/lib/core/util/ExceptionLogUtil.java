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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Handles Exception log strings.
 */
public class ExceptionLogUtil {

  private static final String RT = "\n";

  /** Message used when a throwable argument is {@code null}. */
  public static final String NULL_THROWABLE_MESSAGE = "(throwable argument is null)";

  /**
   * Returns strings or error log. All packages are shown in stack traces.
   *
   * @param throwable throwable. {@code null} is accepted because logging must not
   *     terminate with an error even for irregular input — if logging itself fails,
   *     we would lose information about what originally went wrong.
   * @param additionalMessage additional message,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case of {@code null} no additional message is output.
   * @param locale locale, may be {@code null}
   *     which is treated as {@code Locale.ROOT}.
   * @return error log string
   */
  public static String getErrLogString(@Nullable Throwable throwable,
      @Nullable String additionalMessage, @Nullable Locale locale) {
    return getErrLogStringCore(throwable, additionalMessage, locale, null);
  }

  /**
   * Returns strings or error log.
   *
   * @param throwable throwable. {@code null} is accepted because logging must not
   *     terminate with an error even for irregular input — if logging itself fails,
   *     we would lose information about what originally went wrong.
   * @param additionalMessage additional message,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case of {@code null} no additional message is output.
   * @param locale locale, may be {@code null}
   *     which is treated as {@code Locale.ROOT}.
   * @param packagesShown number of package levels shown in stack traces.
   *     This is used when the log displaying area is small.
   *     {@code 0} shows no packages like {@code "at ..main(ExceptionUtil.java:468)"}.
   *     {@code 1} shows 1 package part like {@code "at jp...main(ExceptionUtil.java:468)"}.
   * @return error log string
   */
  public static String getErrLogString(@Nullable Throwable throwable,
      @Nullable String additionalMessage, @Nullable Locale locale, int packagesShown) {
    return getErrLogStringCore(throwable, additionalMessage, locale, packagesShown);
  }

  private static String getErrLogStringCore(@Nullable Throwable throwable,
      @Nullable String additionalMessage, @Nullable Locale locale,
      @Nullable Integer packagesShown) {
    locale = (locale == null) ? Locale.ENGLISH : locale;

    StringBuilder sb = new StringBuilder();

    sb.append(ExceptionUtil.SYSTEM_ERROR_OCCURED_SIGN + RT);
    if (additionalMessage != null) {
      sb.append(additionalMessage + RT);
      sb.append(RT);
    }

    sb.append(RT);

    getMessageAndStackTraceStringRecursivelyCore(sb, throwable, locale, packagesShown);

    String rtn = sb.toString();
    return rtn;
  }

  /**
   * Returns the exception message list string for a throwable, built via
   * {@link ExceptionUtil#getMessageList(Throwable, Locale, boolean)}.
   *
   * <p>Message resolution can itself throw (e.g. a broken message resource), which would
   *     otherwise hide the original throwable from the log. When that happens, this method
   *     falls back to a message built only from the throwable's own class name and message
   *     (both of which cannot fail), and includes the build failure itself so the underlying
   *     issue stays visible. This method never throws.</p>
   *
   * @param th throwable. Cannot be {@code null}.
   * @param locale locale, may be {@code null} which is treated as the fallback locale.
   * @param isMessagesWithItemNamesAsDefault see
   *     {@link ExceptionUtil#getMessageList(Throwable, Locale, boolean)}.
   * @return the message string; never throws.
   */
  public static String getMessageListStringSafely(Throwable th, @Nullable Locale locale,
      boolean isMessagesWithItemNamesAsDefault) {
    try {
      return ExceptionUtil.getMessageList(th, locale, isMessagesWithItemNamesAsDefault)
          .toString();

    } catch (Throwable messageBuildFailure) {
      return "(failed to build detailed message: " + messageBuildFailure + ") "
          + th.getMessage();
    }
  }

  /**
   * Adds Throwable message and stackTrace string to argument stringBuilder
   *     for a throwable and its causes. All packages are shown in stack traces.
   *
   * @param sb StringBuilder
   * @param th throwable
   * @param locale locale, may be null
   */
  public static void getMessageAndStackTraceStringRecursively(StringBuilder sb,
      @Nullable Throwable th, @Nullable Locale locale) {
    getMessageAndStackTraceStringRecursivelyCore(sb, th, locale, null);
  }

  /**
   * Adds Throwable message and stackTrace string to argument stringBuilder
   *     for a throwable and its causes.
   *
   * @param sb StringBuilder
   * @param th throwable
   * @param locale locale, may be null
   * @param packagesShown number of package levels shown in stack traces.
   *     See {@link #getErrLogString(Throwable, String, Locale, int)} for details.
   */
  public static void getMessageAndStackTraceStringRecursively(StringBuilder sb,
      @Nullable Throwable th, @Nullable Locale locale, int packagesShown) {
    getMessageAndStackTraceStringRecursivelyCore(sb, th, locale, packagesShown);
  }

  private static void getMessageAndStackTraceStringRecursivelyCore(StringBuilder sb,
      @Nullable Throwable th, @Nullable Locale locale, @Nullable Integer packagesShown) {

    if (th == null) {
      getMessageAndStackTraceString(sb, null, locale, packagesShown);
      return;
    }

    // A cause chain must not be walked without a cycle check: unlike getCause() on a normal
    // exception, a crafted or buggy cause chain can point back to a throwable already visited
    // (e.g. a.initCause(b); b.initCause(a)), which would otherwise recurse forever and crash
    // the very logging path meant to report the original error. IdentityHashMap-backed set
    // mirrors the "dejavu" set the JDK's own printStackTrace uses for the same reason: identity
    // (not equals()) is what matters here, since two unrelated throwables may be equal.
    Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    Throwable current = th;
    while (current != null && visited.add(current)) {
      getMessageAndStackTraceString(sb, current, locale, packagesShown);
      current = current.getCause();
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
  private static void getMessageAndStackTraceString(StringBuilder sb, @Nullable Throwable th,
      @Nullable Locale locale, @Nullable Integer packagesShown) {
    locale = (locale == null) ? Locale.ENGLISH : locale;

    String errMsg;
    if (th == null) {
      errMsg = NULL_THROWABLE_MESSAGE;

    } else {
      errMsg = th.getClass().getCanonicalName() + getMessageListStringSafely(th, locale, true);
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
   */
  private static void getStackTraceString(StringBuilder sb, @Nullable Throwable th,
      @Nullable Integer packagesShown) {
    if (th == null) {
      return;
    }

    for (StackTraceElement ste : th.getStackTrace()) {
      String[] spl = ste.getClassName().split("\\.", -1);
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
