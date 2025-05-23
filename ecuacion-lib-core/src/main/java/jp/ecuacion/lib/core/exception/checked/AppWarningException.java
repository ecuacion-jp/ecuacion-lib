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
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.util.ObjectsUtil;

/**
 * Provides an exception that indicates a warning.
 * 
 * <p>This is used in web apps. When some business logic throws this warning exception, 
 * the app shows the warning popup window on screen and proceed when a user presses "OK".</p>
 * 
 * <p>It is not a child of {@code AppException} 
 *     because {@code AppException} means an error occurred in a business logic 
 *     but {@code AppWarningException} doesn't mean it.<br>
 *     Localized message is needed for this, 
 *     so the class is the same constructure as {@code BizLogicAppException}.</p>
 * 
 * <p>{@code MultipleAppException} for warning does not seem to be needed so it does not exist.</p>
 */
public class AppWarningException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * message ID.
   */
  protected String messageId;

  /**
   * message Arguments.
   */
  protected String[] messageArgs;

  /**
   * locale.
   */
  protected Locale locale;

  /**
   * itemIds.
   */
  protected AppExceptionItemIds itemIds;

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
   * Constructs a new instance with warn itemIds, messageId and message Arguments.
   *
   * @param locale locale. May be null, which means default locale is used.
   *        designated.
   * @param messageId messageId. Cannot be {@code null}.
   * @param messageArgs message Arguments. May be null, which means no message arguments designated.
   */
  public AppWarningException(@RequireNonnull Locale locale, @Nonnull String messageId,
      @Nonnull String... messageArgs) {
    super();

    this.locale = ObjectsUtil.requireNonNull(locale);
    this.messageId = ObjectsUtil.requireNonNull(messageId);
    this.messageArgs = ObjectsUtil.requireNonNull(messageArgs);
    
    this.itemIds = new AppExceptionItemIds();
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
   * Returns itemIds.
  
   * @return itemIds
   */
  public @Nonnull AppExceptionItemIds itemIds() {
    return itemIds;
  }

  /**
   * Sets itemIds and return this instance to realize the method chain.
   *
   * @return AppWarningException
   */
  public @Nonnull AppWarningException itemIds(@Nullable AppExceptionItemIds itemIds) {
    if (itemIds != null) {
      this.itemIds = itemIds;

    } else {
      this.itemIds = new AppExceptionItemIds();
    }
    
    return this;
  }
}
