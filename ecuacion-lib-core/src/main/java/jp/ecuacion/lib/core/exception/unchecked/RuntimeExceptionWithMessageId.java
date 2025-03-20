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
import jp.ecuacion.lib.core.util.PropertyFileUtil;
import jp.ecuacion.lib.core.util.PropertyFileUtil.Arg;

/**
 * Is used when you want to throw RuntimeException, but message is deifned in .properties.
 * 
 * <p>When you want throw {@code RuntimeException}, you can use {@code RuntimeSystemException}.
 * So it is not clear when to use {@code RuntimeExceptionWithMessageId}, 
 * but it is kept for the time being. May be deprecated in the future.
 */
@Deprecated
public class RuntimeExceptionWithMessageId extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private String messageId;

  private @Nonnull Arg[] messageArgs;

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   * 
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public RuntimeExceptionWithMessageId(@Nonnull String messageId, @Nonnull String... messageArgs) {
    this(messageId, Arg.strings(messageArgs));
  }


  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   * 
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public RuntimeExceptionWithMessageId(@Nonnull String messageId, @Nonnull Arg[] messageArgs) {
    this.messageId = messageId;
    this.messageArgs = messageArgs;
  }
  
  @Override
  public String getMessage() {
    return PropertyFileUtil.getMessage(messageId, messageArgs);
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
   * Gets messageArgs.
   * 
   * @return message Arguments
   */
  public @Nonnull Arg[] getMessageArgs() {
    return messageArgs == null ? new Arg[] {} : messageArgs.clone();
  }
}
