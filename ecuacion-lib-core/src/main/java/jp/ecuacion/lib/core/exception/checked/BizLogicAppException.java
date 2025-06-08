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
import java.util.Arrays;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.PropertyFileUtil;
import jp.ecuacion.lib.core.util.PropertyFileUtil.Arg;

/**
 * Is used for buziness logic exceptions.
 * 
 * <p>Messages are designated by messageId, which is defined in messages[_xxx].properties.<br>
 */
public class BizLogicAppException extends SingleAppException {
  private static final long serialVersionUID = 1L;

  @Nonnull
  private String messageId;

  @Nonnull
  private Arg[] messageArgs;

  @Nonnull
  private String[] itemPropertyPaths;

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   * 
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(@RequireNonnull String messageId,
      @RequireNonnull String... messageArgs) {
    this(null, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPath}s,
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemPropertyPaths the itemPropertyPaths related to the exception
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(@Nullable String[] itemPropertyPaths,
      @RequireNonnull String messageId, @RequireNonnull String... messageArgs) {

    this(itemPropertyPaths, messageId, Arrays.asList(ObjectsUtil.requireNonNull(messageArgs))
        .stream().map(arg -> Arg.string(arg)).toList().toArray(new Arg[messageArgs.length]));
  }

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   *
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(@RequireNonnull String messageId, @RequireNonnull Arg[] messageArgs) {
    this(null, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemPropertyPaths the itemPropertyPaths related to the exception
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(@Nullable String[] itemPropertyPaths,
      @RequireNonnull String messageId, @Nullable Arg[] messageArgs) {
    this.itemPropertyPaths = itemPropertyPaths == null ? new String[] {} : itemPropertyPaths;
    this.messageId = ObjectsUtil.requireNonNull(messageId);
    this.messageArgs = messageArgs == null ? new Arg[] {} : messageArgs;
  }

  @Override
  public String getMessage() {
    return PropertyFileUtil.getMessage(messageId, messageArgs);
  }

  @Override
  public @Nonnull String[] getItemPropertyPaths() {
    return itemPropertyPaths;
  }

  /**
   * Sets propertyPath and returns self for method chain.
   * 
   * @param itemPropertyPaths propertyPaths
   * @return BizLogicAppException
   */
  public BizLogicAppException itemPropertyPaths(String... itemPropertyPaths) {
    this.itemPropertyPaths = itemPropertyPaths;

    return this;
  }

  /**
   * Gets messageId. 
   * 
   * @return messageId
   */
  public @Nonnull String getMessageId() {
    return messageId;
  }

  /**
   * Gets messageArgs. 
   * 
   * @return messageArgs
   */
  @Nonnull
  public Arg[] getMessageArgs() {
    return messageArgs == null ? new Arg[] {} : messageArgs;
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
