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
import jp.ecuacion.lib.validation.constraints.internal.PatternDescriptionParameter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Is a LocalizedMessageParameter creator for PatternWithDescription.
 *
 * <p>See {@link PatternDescriptionParameter} for how {@code description} is created.</p>
 */
public class PatternWithDescriptionMessageParameterCreator
    implements ValidatorMessageParameterCreator {

  @Override
  public Map<@NonNull String, @Nullable Object> create(ConstraintViolation<?> cv,
      Map<@NonNull String, @Nullable Object> paramMap) {

    Map<@NonNull String, @Nullable Object> result = new HashMap<>();

    result.put("description", PatternDescriptionParameter.of(
        Objects.requireNonNull((String) paramMap.get("regexp")),
        Objects.requireNonNullElse((String) paramMap.get("description"), "")));

    return result;
  }
}
