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

/** 
 * Is an abstract class describing an occurrence of a single validation error.
 */
public abstract class SingleAppException extends AppException {
  
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new instance.
   */
  public SingleAppException() {
    
  }

  /**
   * Constructs a new instance with {@code message}.
   * 
   * @param message message
   */
  public SingleAppException(String message) {
    super(message);
  }
  
  /**
   * Provides the location of an item at which this error occurred.
   * 
   * <p>Its data-type is an array 
   *     because although standard jakarta validation (like {@code @NotEmpty} has only one property path,
   *     but BizLogicAppException which describes an error of some relation between multiple items
   *     may have multiple property paths.</p>
   * 
   * @return an array of propertyPath
   */
  @Nonnull
  public abstract String[] getItemPropertyPaths();
}
