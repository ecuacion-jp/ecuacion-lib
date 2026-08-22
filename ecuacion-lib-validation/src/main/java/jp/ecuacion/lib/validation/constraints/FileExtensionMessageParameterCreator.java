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
package jp.ecuacion.lib.validation.constraints;

import jakarta.validation.ConstraintViolation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ValidatorMessageParameterCreator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Is a LocalizedMessageParameter creator for FileExtension.
 *
 * <p>Normalizes {@code value} (which may or may not have a leading dot) to always have
 *     a leading dot, so the message reads consistently regardless of how
 *     {@code @FileExtension} was declared.</p>
 */
public class FileExtensionMessageParameterCreator implements ValidatorMessageParameterCreator {

  @Override
  public Map<@NonNull String, @Nullable Object> create(ConstraintViolation<?> cv,
      Map<@NonNull String, @Nullable Object> paramMap) {

    Map<@NonNull String, @Nullable Object> result = new HashMap<>();

    String value = Objects.requireNonNull((String) paramMap.get("value"));
    result.put("extension", value.startsWith(".") ? value : "." + value);

    return result;
  }
}
