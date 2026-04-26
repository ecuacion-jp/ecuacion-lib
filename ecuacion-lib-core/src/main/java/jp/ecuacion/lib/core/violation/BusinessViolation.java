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
   * message Arguments.
   *
   * <p>{@code null} is not allowed for the value of each element
   *     because if you want to hold {@code null}, put it in {@code Arg},
   *     {@code Arg} doesn't have to be {@code null}.
   *     And an array itself cannot be {@code null}.</p>
   */
  private @NonNull Arg[] messageArgs;

  /**
   * RootBean. Put the object field with {@code itemPropertyPath} has.
   */
  private @Nullable Object rootBean;

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
  public BusinessViolation(String messageId, @Nullable String... messageArgs) {
    this(new String[] {}, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemPropertyPaths the itemPropertyPaths related to the violation
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public BusinessViolation(@NonNull String[] itemPropertyPaths, String messageId,
      @Nullable String... messageArgs) {
    this(null, itemPropertyPaths, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param rootBean rootBean
   * @param itemPropertyPaths the itemPropertyPaths related to the violation
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public BusinessViolation(@Nullable Object rootBean, @NonNull String[] itemPropertyPaths,
      String messageId, @Nullable String... messageArgs) {
    this(rootBean, itemPropertyPaths, messageId,
        Arrays.stream(messageArgs).map(arg -> Arg.string(arg)).toArray(Arg[]::new));
  }

  /**
   * Constructs a new instance with {@code messageId} and {@code messageArgs}.
   *
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public BusinessViolation(String messageId, @NonNull Arg[] messageArgs) {
    this(new @NonNull String[] {}, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param itemPropertyPaths the itemPropertyPaths related to the violation
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public BusinessViolation(@NonNull String[] itemPropertyPaths, String messageId,
      @NonNull Arg[] messageArgs) {
    this(null, itemPropertyPaths, messageId, messageArgs);
  }

  /**
   * Constructs a new instance with {@code itemPropertyPaths},
   *     {@code messageId} and {@code messageArgs}.
   *
   * @param rootBean rootBean
   * @param itemPropertyPaths the itemPropertyPaths related to the violation
   * @param messageId message ID
   * @param messageArgs message Arguments. Each element can be {@code null}.
   */
  public BusinessViolation(@Nullable Object rootBean, @NonNull String[] itemPropertyPaths,
      String messageId, @NonNull Arg[] messageArgs) {
    this.rootBean = rootBean;
    this.itemPropertyPaths = itemPropertyPaths;
    this.messageId = ObjectsUtil.requireNonNull(messageId);
    this.messageArgs = messageArgs;
  }

  @Override
  public String toString() {
    return "{" + "message : \"" + PropertiesFileUtil.getMessage(Locale.ROOT, messageId, messageArgs)
        + "\", " + "messageId : \"" + messageId + "\", " + "messageArgs : \""
        + Arrays.toString(messageArgs) + "\", " + "itemPropertyPaths : \""
        + Arrays.toString(itemPropertyPaths) + "\"}";
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
   * Gets rootBean.
   * 
   * @return rootBean
   */
  public @Nullable Object getRootBean() {
    return rootBean;
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
