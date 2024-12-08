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
package jp.ecuacion.lib.core.exception.checked;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Locale;
import jp.ecuacion.lib.core.util.ObjectsUtil;

/**
 * Provides an exception that indicates a warning.
 * 
 * <p>This is used in web apps. When some business logic throws this warning exception, 
 * the app shows the warning popup window on screen and proceed when a user presses "OK".</p>
 * 
 * <p>It is not a child of {@code AppException} 
 *     because {@code AppException} means an error occured in a business logic 
 *     but {@code AppWarningException} doesn't mean it.<br>
 *     Localized message is needed for this, 
 *     so the class is the same constructure as {@code BizLogicAppException}.</p>
 * 
 * <p>{@code MultipleAppException} for warning does not seem to be needed so it does not exist.</p>
 */
public class AppWarningException extends Exception {
  private static final long serialVersionUID = 1L;

  private String messageId;

  private String[] messageArgs;

  private Locale locale;

  private AppExceptionFields fields;

  private String buttonId;

  /**
   * Constructs a new instance with {@code messageId}.
   *
   * @param messageId message ID
   */
  public AppWarningException(@Nonnull String messageId) {
    this(messageId, new String[] {});
  }

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   *
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public AppWarningException(@Nonnull String messageId, @Nonnull String... messageArgs) {
    this(Locale.getDefault(), messageId, messageArgs);
  }

  /**
   * Constructs a new instance with warn fields and messageId.
   *
   * @param warnFields fields related to the exeception. May be null, which means no fields
   *        designated.
   * @param messageId messageId. Cannot be {@code null}.
   */
  public AppWarningException(@Nonnull AppExceptionFields warnFields, @Nonnull String messageId) {
    this(warnFields, messageId, new String[] {});
  }

  /**
   * Constructs a new instance with warn fields, messageId and message Arguments.
   *
   * @param warnFields fields related to the exeception. May be null, which means no fields
   *        designated.
   * @param messageId messageId. Cannot be {@code null}.
   * @param messageArgs message Arguments. May be null, which means no message arguments designated.
   */
  public AppWarningException(@Nonnull AppExceptionFields warnFields, @Nonnull String messageId,
      @Nonnull String... messageArgs) {
    this(Locale.getDefault(), warnFields, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with locale and messageId.
   *
   * @param locale locale. May be null, which means default locale is used.
   * @param messageId messageId. Cannot be {@code null}.
   */
  public AppWarningException(@Nonnull Locale locale, @Nonnull String messageId) {
    this(locale, messageId, new String[] {});
  }

  /**
   * Constructs a new instance with locale, messageId and message Arguments.
   *
   * @param locale locale. May be null, which means default locale is used.
   * @param messageId messageId. Cannot be {@code null}.
   * @param messageArgs message Arguments. May be null, which means no message arguments designated.
   */
  public AppWarningException(@Nonnull Locale locale, @Nonnull String messageId,
      @Nonnull String... messageArgs) {
    this(locale, new AppExceptionFields(new String[] {}), messageId, messageArgs);
  }

  /**
   * Constructs a new instance with locale, warn fields and messageId.
   *
   * @param locale locale. May be null, which means default locale is used.
   * @param warnFields fields related to the exeception. May be null, which means no fields
   *        designated.
   * @param messageId messageId. Cannot be {@code null}.
   */
  public AppWarningException(@Nonnull Locale locale, @Nonnull AppExceptionFields warnFields,
      @Nonnull String messageId) {
    this(locale, warnFields, messageId, new String[] {});
  }

  /**
   * Constructs a new instance with locale, warn fields, messageId and message Arguments.
   *
   * @param locale locale. May be null, which means default locale is used.
   * @param warnFields fields related to the exeception. May be null, which means no fields
   *        designated.
   * @param messageId messageId. Cannot be {@code null}.
   * @param messageArgs message Arguments. May be null, which means no message arguments designated.
   */
  public AppWarningException(@Nonnull Locale locale, @Nonnull AppExceptionFields warnFields,
      @Nonnull String messageId, @Nonnull String... messageArgs) {
    this(locale, null, warnFields, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with warn fields, messageId and message Arguments.
   *
   * @param locale locale. May be null, which means default locale is used.
   * @param buttonId button ID.
   * @param warnFields fields related to the exeception. May be null, which means no fields
   *        designated.
   * @param messageId messageId. Cannot be {@code null}.
   * @param messageArgs message Arguments. May be null, which means no message arguments designated.
   */
  public AppWarningException(@Nonnull Locale locale, @Nullable String buttonId,
      @Nullable AppExceptionFields warnFields, @Nonnull String messageId,
      @Nonnull String... messageArgs) {

    // TODO: buttonIdがなんだかよくわからないので調べてjavadocに記載！！
    super();

    this.locale = ObjectsUtil.paramRequireNonNull(locale);
    this.buttonId = buttonId;
    this.fields = ObjectsUtil.paramRequireNonNull(warnFields);
    this.messageId = ObjectsUtil.paramRequireNonNull(messageId);
    this.messageArgs = ObjectsUtil.paramRequireNonNull(messageArgs);
  }

  /**
   * Returns locale.
   * 
   * @return locale
   */
  public @Nonnull Locale getLocale() {
    return locale;
  }

  /**
   * Returns fields.
  
   * @return warn fields
   */
  public @Nonnull AppExceptionFields getWarnFields() {
    return fields;
  }

  /**
   * Returns messageId.
   * 
   * @return message ID
   */
  public @Nonnull String getMessageId() {
    return messageId;
  }

  /**
   * Returns messageArgs.
   * 
   * @return message arguments
   */
  public @Nonnull String[] getMessageArgs() {
    return messageArgs.clone();
  }

  /**
   * Returns buttonId.
   * 
   * @return button　ID
   */
  public @Nullable String getButtonId() {
    return buttonId;
  }
}
