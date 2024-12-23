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
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.util.ObjectsUtil;

/**
 * Holds fields related to exceptions.
 * 
 * <p>Since a set of message parameters is also a message-related {@code String} array, 
 * this needs to be objectized to be easily distinguished from message arguments.</p>
 */
public class AppExceptionFields {

  private String[] fields;

  /**
   * Constructs a new instance with {@code fields}.
   * 
   * @param fields the fields related to an exception.
   *     An array of length zero is acceptable.
   */
  public AppExceptionFields(@RequireNonnull String... fields) {
    this.fields = fields;

    ObjectsUtil.paramRequireNonNull(fields);
  }

  /**
   * Gets fields.
   * 
   * @return the fields which holds in this instance
   */
  public @Nonnull String[] getFields() {
    return fields;
  }
}
