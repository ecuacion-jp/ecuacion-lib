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

  @Nullable
  private AppExceptionFields fields;

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   * 
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(@Nonnull String messageId, @Nonnull String... messageArgs) {
    this(null, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code locale},  {@code fields},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param fields the fields related to the exception
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(@Nullable AppExceptionFields fields, @Nonnull String messageId,
      @Nonnull String... messageArgs) {
    
    this(fields, ObjectsUtil.paramRequireNonNull(messageId),
        Arrays.asList(ObjectsUtil.paramRequireNonNull(messageArgs)).stream()
            .map(arg -> new Arg(arg)).toList().toArray(new Arg[messageArgs.length]));
  }

  /**
   * Constructs a new instance with {@code fields},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(@RequireNonnull String messageId, @RequireNonnull Arg[] messageArgs) {
    this(null, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code fields},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param fields the fields related to the exeception
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(@Nullable AppExceptionFields fields, @RequireNonnull String messageId,
      @RequireNonnull Arg[] messageArgs) {

    // set message with default locale to show the message in stack trace
    // for users who don't use ecuacion-xxlib exception handler.
    super(PropertyFileUtil.getMsg(messageId));

    this.fields = fields;
    this.messageId = ObjectsUtil.paramRequireNonNull(messageId);
    this.messageArgs = messageArgs;
  }

  /**
   * Gets fields. 
   * 
   * @return fields
   */
  public @Nullable AppExceptionFields getErrorFields() {
    return fields;
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
}
