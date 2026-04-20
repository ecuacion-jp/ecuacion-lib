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

import java.util.Locale;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Is used for business logic exceptions.
 *
 * <p>Messages are designated by messageId, which is defined in messages[_xxx].properties.<br>
 * 
 * @deprecated Use BusinessViolation instead.
 */
@Deprecated(since = "15.1", forRemoval = true)
public class BizLogicAppException extends SingleAppException {
  private static final long serialVersionUID = 1L;

  private BusinessViolation violation;

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   *
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  @Deprecated(since = "15.1", forRemoval = true)
  public BizLogicAppException(String messageId, @Nullable String... messageArgs) {
    violation = new BusinessViolation(messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemPropertyPaths the itemPropertyPaths related to the exception
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  @Deprecated(since = "15.1", forRemoval = true)
  public BizLogicAppException(String[] itemPropertyPaths, String messageId,
      @Nullable String... messageArgs) {
    violation = new BusinessViolation(itemPropertyPaths, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   *
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  @Deprecated(since = "15.1", forRemoval = true)
  public BizLogicAppException(String messageId, @NonNull Arg[] messageArgs) {
    violation = new BusinessViolation(messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemPropertyPaths the itemPropertyPaths related to the exception
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  @Deprecated(since = "15.1", forRemoval = true)
  public BizLogicAppException(@NonNull String[] itemPropertyPaths, String messageId,
      @NonNull Arg[] messageArgs) {
    violation = new BusinessViolation(itemPropertyPaths, messageId, messageArgs);
  }

  @Override
  @Deprecated(since = "15.1", forRemoval = true)
  public String getMessage() {
    return PropertiesFileUtil.getMessage(Locale.ENGLISH, violation.getMessageId(),
        violation.getMessageArgs());
  }

  @Override
  @Deprecated(since = "15.1", forRemoval = true)
  public @NonNull String[] getItemPropertyPaths() {
    return violation.getItemPropertyPaths();
  }

  /**
   * Gets messageId.
   *
   * @return messageId
   */
  @Deprecated(since = "15.1", forRemoval = true)
  public String getMessageId() {
    return violation.getMessageId();
  }

  /**
   * Gets messageArgs.
   *
   * @return messageArgs
   */
  @Deprecated(since = "15.1", forRemoval = true)
  public @NonNull Arg[] getMessageArgs() {
    return violation.getMessageArgs();
  }

  /**
   * Gets the {@link BusinessViolation} held internally.
   *
   * @return businessViolation
   */
  @Deprecated(since = "15.1", forRemoval = true)
  public BusinessViolation getBusinessViolation() {
    return violation;
  }

  /**
   * Provides {@link Exception#initCause(Throwable)} with method chain.
   *
   * @param th throwable
   * @return BizLogicAppException for method chain
   */
  @Deprecated(since = "15.1", forRemoval = true)
  public BizLogicAppException cause(Throwable th) {
    super.initCause(th);
    return this;
  }
}
