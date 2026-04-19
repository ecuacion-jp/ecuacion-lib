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

import java.util.Arrays;
import java.util.Locale;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Is used for business logic exceptions.
 *
 * <p>Messages are designated by messageId, which is defined in messages[_xxx].properties.<br>
 */
public class BizLogicAppException extends SingleAppException {
  private static final long serialVersionUID = 1L;

  /**
   * message ID.
   *
   * <p>It cannot be {@code null} because business logic exceptions with no message ID
   *     cannot display error messages to users.</p>
   */
  private String messageId;

  /**
   * message Arguments.
   *
   * <p>{@code null} is not allowed for the value of each element
   *     because if you want to hold {@code null}, put it in {@code Arg}, 
   *     {@code Arg} doesn't have to be {@code null}.
   *     And an array itself cannot be {@code null}.</p>
   */
  private @NonNull Arg[] messageArgs;

  /**
   * An array of item propertyPath.
   * 
   * <p>Elements of the array cannot be {@code null} because they are usually
   *     set with direct string value (like "name") and accepting {@code null}
   *     means nothing.<br>
   *     The array itself also cannot be {@code null} because if you want to 
   *     express there's no {@code itemPropertyPath}, just put {@code new String[] {}}.</p>
   */
  private @NonNull String[] itemPropertyPaths;

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   *
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public BizLogicAppException(String messageId, @Nullable String... messageArgs) {
    this(new String[] {}, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemPropertyPaths the itemPropertyPaths related to the exception
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public BizLogicAppException(String[] itemPropertyPaths, String messageId,
      @Nullable String... messageArgs) {

    this(itemPropertyPaths, messageId, Arrays.asList(messageArgs).stream()
        .map(arg -> Arg.string(arg)).toList().toArray(new Arg[messageArgs.length]));
  }

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   *
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public BizLogicAppException(String messageId, @NonNull Arg[] messageArgs) {
    this(new @NonNull String[] {}, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemPropertyPaths the itemPropertyPaths related to the exception
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public BizLogicAppException(@NonNull String[] itemPropertyPaths, String messageId,
      @NonNull Arg[] messageArgs) {
    this.itemPropertyPaths = itemPropertyPaths;
    this.messageId = ObjectsUtil.requireNonNull(messageId);
    this.messageArgs = messageArgs;
  }

  @Override
  public String getMessage() {
    return PropertiesFileUtil.getMessage(Locale.ENGLISH, messageId, messageArgs);
  }

  @Override
  public @NonNull String[] getItemPropertyPaths() {
    return itemPropertyPaths;
  }

  /**
   * Gets messageId.
   *
   * @return messageId
   */
  public String getMessageId() {
    return messageId;
  }

  /**
   * Gets messageArgs.
   *
   * @return messageArgs
   */
  public @NonNull Arg[] getMessageArgs() {
    return messageArgs;
  }

  /**
   * Provides {@link Exception#initCause(Throwable)} with method chain.
   *
   * @param th throwable
   * @return BizLogicAppException for method chain
   */
  public BizLogicAppException cause(Throwable th) {
    super.initCause(th);
    return this;
  }
}
