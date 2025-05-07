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
import java.util.Locale;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.unchecked.UncheckedAppException;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.logging.ErrorLogger;
import org.slf4j.event.Level;

/**
 * Provides logging-related utility methods, espacially log {@code Throable}.
 */
public class LogUtil {

  private DetailLogger detailLog;
  private ErrorLogger errLog = new ErrorLogger();
  private ExceptionUtil exUtil = new ExceptionUtil();

  /**
   * is a large partition or separator which separates log lines.
   */
  public static final String PARTITION_LARGE = "===============";

  /**
   * is a medium partition or separator which separates log lines.
   */
  public static final String PARTITION_MEDIUM = "----------";

  /**
   * Constructs a new instance with an instance.
   * 
   * @param instance instance which want to log.
   */
  public LogUtil(@Nonnull Object instance) {
    detailLog = new DetailLogger(ObjectsUtil.paramRequireNonNull(instance));
  }

  /**
   * Constructs a new instance with an instance.
   * 
   * @param cls class which want to log.
   */
  public LogUtil(@Nonnull Class<?> cls) {
    detailLog = new DetailLogger(ObjectsUtil.paramRequireNonNull(cls));
  }

  /**
   * Logs throwable.
   * 
   * @param throwable throwable
   */
  public void logError(@RequireNonnull Throwable throwable) {
    logError(throwable, (String) null);
  }

  /**
   * Logs throwable.
   * 
   * @param throwable throwable
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   */
  public void logError(@RequireNonnull Throwable throwable, @Nullable Locale locale) {
    logError(throwable, null, locale);
  }

  /**
   * Logs throwable.
   * 
   * @param throwable throwable
   * @param additionalMessage additionalMessage,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case o {@code null} no additional message is output.
   */
  public void logError(@RequireNonnull Throwable throwable, @Nullable String additionalMessage) {
    logError(throwable, additionalMessage, null);
  }

  /**
   * Logs throwable.
   * 
   * @param throwable throwable
   * @param additionalMessage additionalMessage,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case o {@code null} no additional message is output.
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   */
  public void logError(@RequireNonnull Throwable throwable, @Nullable String additionalMessage,
      @Nullable Locale locale) {
    logException(Level.ERROR, throwable, additionalMessage, locale);
  }
  
  /**
   * Logs Exception details with DEBUG loglevel.
   * 
   * <p>It's intended to use to show the cause of {@code BizLogicAppException} 
   * in case that where it's thrown from or see what the original Exception is.</p>
   * 
   * @param throwable throwable
   */
  public void logDebug(@RequireNonnull Throwable throwable) {
    logException(Level.DEBUG, throwable, null, null);
  }

  /**
   * Logs throwable.
   * 
   * @param throwable throwable
   * @param additionalMessage additionalMessage,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case o {@code null} no additional message is output.
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   */
  private void logException(@Nonnull Level logLevel, @RequireNonnull Throwable throwable,
      @Nullable String additionalMessage, @Nullable Locale locale) {
    ObjectsUtil.paramRequireNonNull(throwable);

    // errorLogへの出力
    String msg = (throwable.getMessage() == null) ? ""
        : " - " + exUtil.getExceptionMessage(throwable, locale, true).toString().replace("\n", " ");
    errLog.error("A system error has occured: " + throwable.getClass().getName() + msg + " ("
        + additionalMessage + ")");

    // additionalMessageへの追加
    if (throwable instanceof AppException || throwable instanceof UncheckedAppException) {
      // ビジネスエラーとしてクライアントに返すのみ。本サービスでのエラー発生関連処理はない。
      StringBuilder sb = new StringBuilder();
      AppException ae = throwable instanceof UncheckedAppException
          ? (AppException) ((UncheckedAppException) throwable).getCause()
          : (AppException) throwable;
      new ExceptionUtil().getAppExceptionMessageList(ae, Locale.getDefault())
          .forEach(tmpMsg -> sb.append(tmpMsg + "\n"));

      additionalMessage += "\n" + sb.toString();
    }

    // Output to detailLog
    log(logLevel, exUtil.getErrLogString(throwable, additionalMessage, locale));
  }

  private void log(@Nonnull Level logLevel, String message) {
    switch (logLevel) {
      case Level.ERROR -> detailLog.error(message);
      case Level.WARN -> detailLog.warn(message);
      case Level.INFO -> detailLog.info(message);
      case Level.DEBUG -> detailLog.debug(message);
      case Level.TRACE -> detailLog.trace(message);
      default -> throw new IllegalArgumentException("Unexpected value: " + logLevel);
    }
  }
}
