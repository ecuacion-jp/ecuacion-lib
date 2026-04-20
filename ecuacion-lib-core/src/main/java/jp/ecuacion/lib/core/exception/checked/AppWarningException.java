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

import org.jspecify.annotations.Nullable;

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
 *     so the class is the same structure as {@code BizLogicAppException}.</p>
 * 
 * <p>{@code MultipleAppException} for warning does not seem to be needed so it does not exist.</p>
 */
public class AppWarningException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * message ID.
   * 
   * <p>It cannot be {@code null} because warning popup windows with no message means 
   *     nothing.</p>
   */
  protected String messageId;

  /**
   * message Arguments.
   * 
   * <p>{@code null} is allowed for the value of each element 
   *     because messageArgs can be the user-input value and can be {@code null}.<br>
   *     But an array itself cannot be {@code null}.</p>
   */
  protected @Nullable String [] messageArgs;

  /**
   * Constructs a new instance 
   *     with {@code messageId} and {@code messageArgs}.
   *
   * @param messageId message ID
   * @param messageArgs message Arguments. It's {code @Nullable}. See {@code messageArgs}.
   */
  public AppWarningException(String messageId, @Nullable String... messageArgs) {
    super();

    this.messageId = messageId;
    this.messageArgs = messageArgs;
  }

  /**
   * Returns messageId.
   * 
   * @return message ID
   */
  public String getMessageId() {
    return messageId;
  }

  /**
   * Returns messageArgs.
   * 
   * @return message arguments
   */
  public @Nullable String[] getMessageArgs() {
    return messageArgs.clone();
  }
}
