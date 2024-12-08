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
import java.util.Locale;
import jp.ecuacion.lib.core.util.ObjectsUtil;

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
  private String[] messageArgs;

  @Nonnull
  private Locale locale;

  @Nonnull
  private AppExceptionFields fields;

  /**
   * Constructs a new instance with {@code messageId}.
   * 
   * @param messageId message ID
   */
  public BizLogicAppException(@Nonnull String messageId) {
    this(messageId, new String[] {});
  }

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   * 
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(@Nonnull String messageId, @Nonnull String... messageArgs) {
    this(Locale.getDefault(), messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code fields} and {@code messageId}.
   *
   * @param fields the fields related to the exeception
   * @param messageId message ID
   */
  public BizLogicAppException(@Nonnull AppExceptionFields fields, @Nonnull String messageId) {
    this(fields, messageId, new String[] {});
  }

  /**
   * Constructs a new instance with {@code fields}, {@code messageId} and {@code messageArgs}.
   *
   * @param fields the fields related to the exeception
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(@Nonnull AppExceptionFields fields, @Nonnull String messageId,
      @Nonnull String... messageArgs) {
    this(Locale.getDefault(), fields, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code locale} and {@code messageId}.
   *
   * @param locale locale
   * @param messageId message ID
   */
  public BizLogicAppException(@Nonnull Locale locale, @Nonnull String messageId) {
    this(locale, messageId, new String[] {});
  }

  /**
   * Constructs a new instance with {@code locale}, {@code messageId} and {@code messageArgs}.
   *
   * @param locale locale
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(@Nonnull Locale locale, @Nonnull String messageId,
      @Nonnull String... messageArgs) {
    this(locale, new AppExceptionFields(new String[] {}), messageId, messageArgs);
  }


  /**
   * Constructs a new instance with {@code locale}, {@code fields} and {@code messageId}.
   *
   * @param locale locale
   * @param fields the fields related to the exeception
   * @param messageId message ID
   */
  public BizLogicAppException(@Nonnull Locale locale, @Nonnull AppExceptionFields fields,
      @Nonnull String messageId) {
    this(locale, fields, messageId, new String[] {});
  }

  /**
   * Constructs a new instance with {@code locale},  {@code fields},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param locale locale
   * @param fields the fields related to the exeception
   * @param messageId message ID
   * @param messageArgs message Arguments
   */
  public BizLogicAppException(Locale locale, AppExceptionFields fields, String messageId,
      String... messageArgs) {
    super();
    
    this.locale = ObjectsUtil.paramRequireNonNull(locale);
    this.fields = ObjectsUtil.paramRequireNonNull(fields);
    this.messageId = ObjectsUtil.paramRequireNonNull(messageId);
    this.messageArgs = ObjectsUtil.paramRequireNonNull(messageArgs);
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
   * Gets fields. 
   * 
   * @return fields
   */
  public @Nonnull AppExceptionFields getErrorFields() {
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
  public @Nonnull String[] getMessageArgs() {
    return messageArgs == null ? new String[] {} : messageArgs.clone();
  }
}
