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
package jp.ecuacion.lib.core.jakartavalidation.constraints;

import jakarta.validation.ConstraintViolation;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.item.Item;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides message parameters used in ValidationMessages.properties as "{param}".
 *
 * <p>Parameters defined in an annotation are automatically added to the parameter map,
 *     but this interface allows adding further parameters for user-friendly messages.</p>
 *
 * <p>Map values may be {@link jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg} instances
 *     (resolved at render time), {@link ItemNameParam} (for item-name resolution),
 *     or plain {@code Object}s.</p>
 */
public interface ValidatorMessageParameterCreator {

  /**
   * Creates and returns named message parameters.
   *
   * @param cv the constraint violation from Jakarta Validation
   * @param paramMap annotation attribute map (pre-cleaned: groups/message/payload removed;
   *     augmented with annotation, itemAttributes, invalidValue)
   * @return map of parameter name to value
   *     ({@link jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg},
   *     {@link ItemNameParam}, or plain object)
   */
  Map<@NonNull String, @Nullable Object> create(ConstraintViolation<?> cv,
      Map<@NonNull String, @Nullable Object> paramMap);

  /**
   * Represents an item-name parameter to be resolved at render time.
   *
   * <p>Place an instance of this class as a map value returned from {@link #create}
   *     when the named placeholder should be filled with the item's display name.
   *     {@link jp.ecuacion.lib.core.util.ExceptionUtil} will resolve it before the map
   *     is passed to the message formatter.</p>
   *
   * <p>For property-file-lookup parameters, put an
   *     {@link jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg} instance directly
   *     in the map instead.</p>
   */
  record ItemNameParam(List<@NonNull Item> items, Object rootBean) {
  }
}
