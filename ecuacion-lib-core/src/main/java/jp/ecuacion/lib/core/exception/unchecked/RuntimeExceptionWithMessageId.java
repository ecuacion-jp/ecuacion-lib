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
package jp.ecuacion.lib.core.exception.unchecked;

import jakarta.annotation.Nonnull;
import java.util.Locale;
import jp.ecuacion.lib.core.util.ObjectsUtil;

/**
 * Is used when you want to throw RuntimeException, but message is deifned in .properties.
 * 
 * <p>When you want throw {@code RuntimeException}, you can use {@code RuntimeSystemException}.
 * So it is not clear when to use {@code RuntimeExceptionWithMessageId}, 
 * but it is kept for the time being. May be deprecated in the future.
 */
public class RuntimeExceptionWithMessageId extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private String messageId;

  private @Nonnull String[] messageArgs;

  private @Nonnull Locale locale;

  /**
   * Constructs a new instance with {@code messageId}.
   * 
   * @param messageId message ID
   */
  public RuntimeExceptionWithMessageId(@Nonnull String messageId) {
    this(messageId, new String[] {});
  }

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   * 
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public RuntimeExceptionWithMessageId(@Nonnull String messageId, @Nonnull String... messageArgs) {
    this(Locale.getDefault(), messageId, messageArgs);
  }


  /**
   * Constructs a new instance with {@code locale} and {@code messageId}.
   * 
   * @param locale locale
   * @param messageId message ID
   */
  public RuntimeExceptionWithMessageId(@Nonnull Locale locale, @Nonnull String messageId) {
    this(locale, messageId, new String[] {});
  }

  /**
   * Constructs a new instance with {@code locale}, {@code messageId} and {@code messageArgs}.
   * 
   * @param locale locale
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public RuntimeExceptionWithMessageId(@Nonnull Locale locale, @Nonnull String messageId,
      @Nonnull String... messageArgs) {
    super();

    this.locale = ObjectsUtil.paramRequireNonNull(locale);
    this.messageId = ObjectsUtil.paramRequireNonNull(messageId);
    this.messageArgs = ObjectsUtil.paramRequireNonNull(messageArgs);
  }

  /**
   * Gets messageId.
   * 
   * @return message ID
   */
  public @Nonnull String getMessageId() {
    return messageId;
  }

  /**
   * Gets locale.
   * 
   * @return locale
   */
  public @Nonnull Locale getLocale() {
    return locale;
  }

  /**
   * Gets messageArgs.
   * 
   * @return message Arguments
   */
  public @Nonnull String[] getMessageArgs() {
    return messageArgs == null ? new String[] {} : messageArgs.clone();
  }
}
