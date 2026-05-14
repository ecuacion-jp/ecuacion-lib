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
package jp.ecuacion.lib.core.violation;

import java.util.Arrays;
import java.util.Locale;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Describes a single business logic violation.
 *
 * <p>Use this when you want to collect violations without immediately throwing,
 *     then call {@link Violations#throwIfAny()} after all checks are done.</p>
 */
public class BusinessViolation {

  /**
   * message ID.
   *
   * <p>It cannot be {@code null} because business logic violations with no message ID
   *     cannot display error messages to users.</p>
   */
  private String messageId;

  /**
   * message Arguments. Elements may be plain {@code Object}s or {@link Arg} instances,
   *     and each element can be {@code null}.
   */
  private @Nullable Object[] messageArgs;

  /**
   * Keys used to look up item display names from {@code item_names.properties}.
   *
   * <p>These keys are used to resolve {@code {item_name}} placeholders in
   *     {@code messages_with_item_names.properties} at message rendering time.</p>
   */
  private @NonNull String[] itemNameKeys;

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
   * @param messageArgs message Arguments; {@link Arg} instances and plain {@code Object}s
   *     may be mixed. Each element can be {@code null}.
   */
  public BusinessViolation(String messageId, @Nullable Object... messageArgs) {
    this(new String[] {}, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemPropertyPaths the itemPropertyPaths related to the violation
   * @param messageId message ID
   * @param messageArgs message Arguments; {@link Arg} instances and plain {@code Object}s
   *     may be mixed. Each element can be {@code null}.
   */
  public BusinessViolation(@NonNull String[] itemPropertyPaths, String messageId,
      @Nullable Object... messageArgs) {
    this(new String[] {}, itemPropertyPaths, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemNameKeys}, {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemNameKeys keys to look up item display names from {@code item_names.properties};
   *     used to resolve {@code {item_name}} in messages
   * @param itemPropertyPaths the itemPropertyPaths related to the violation
   * @param messageId message ID
   * @param messageArgs message Arguments; {@link Arg} instances and plain {@code Object}s
   *     may be mixed. Each element can be {@code null}.
   */
  public BusinessViolation(@NonNull String[] itemNameKeys, @NonNull String[] itemPropertyPaths,
      String messageId, @Nullable Object... messageArgs) {
    this.itemNameKeys = itemNameKeys;
    this.itemPropertyPaths = itemPropertyPaths;
    this.messageId = ObjectsUtil.requireNonNull(messageId);
    this.messageArgs = messageArgs;
  }

  @Override
  public String toString() {
    return "{" + "message : \""
        + PropertiesFileUtil.getMessage(Locale.ROOT, messageId, messageArgs) + "\", "
        + "messageId : \"" + messageId + "\", " + "messageArgs : \"" + Arrays.toString(messageArgs)
        + "\", " + "itemPropertyPaths : \"" + Arrays.toString(itemPropertyPaths) + "\"}";
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
  public @Nullable Object[] getMessageArgs() {
    return messageArgs;
  }

  /**
   * Gets itemNameKeys.
   *
   * @return itemNameKeys
   */
  public @NonNull String[] getItemNameKeys() {
    return itemNameKeys;
  }

  /**
   * Gets itemPropertyPaths.
   *
   * @return itemPropertyPaths
   */
  public @NonNull String[] getItemPropertyPaths() {
    return itemPropertyPaths;
  }
}
