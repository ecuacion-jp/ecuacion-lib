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
package jp.ecuacion.lib.validation.constraints.internal;

import jakarta.validation.ConstraintViolation;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Is a ValidatorMessageParameterCreator for {@code @PatternWhen} and {@code @NotPatternWhen}.
 *
 * <p>In addition to the parameters for the condition, it creates {@code description},
 *     which describes the regular expression that the validated value must (or must not) match.
 *     See {@link PatternDescriptionParameter} for how it is created.</p>
 */
public class PatternWhenValidatorMessageParameterCreator
    extends ValidateWhenValidatorMessageParameterCreator {

  private static final String DESCRIPTION = "description";

  @Override
  public Map<@NonNull String, @Nullable Object> create(ConstraintViolation<?> cv,
      Map<@NonNull String, @Nullable Object> paramMap) {
    Map<@NonNull String, @Nullable Object> result = super.create(cv, paramMap);

    result.put(DESCRIPTION, PatternDescriptionParameter.of(
        Objects.requireNonNull((String) paramMap.get("regexp")),
        Objects.requireNonNullElse((String) paramMap.get(DESCRIPTION), "")));

    return result;
  }
}
