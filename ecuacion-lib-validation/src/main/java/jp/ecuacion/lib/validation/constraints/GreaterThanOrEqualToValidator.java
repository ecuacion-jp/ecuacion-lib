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

import java.util.Objects;
import jp.ecuacion.lib.validation.constraints.internal.ComparisonValidator;
import org.jspecify.annotations.Nullable;

/**
 * Provides the validation logic for {@code GreaterThan}.
 */
public class GreaterThanOrEqualToValidator
    extends ComparisonValidator<GreaterThanOrEqualTo, Object> {

  /**
   * Initializes an instance.
   *
   * <p>{@code @Nullable} on the parameter is for Eclipse null analysis compatibility;
   *     see package {@link jp.ecuacion.lib.validation.constraints} for details.</p>
   */
  @Override
  public void initialize(@Nullable GreaterThanOrEqualTo annotation) {
    Objects.requireNonNull(annotation);
    super.initialize(annotation.message(), annotation.propertyPath(),
        annotation.baselinePropertyPath(), false, true, annotation.typeConversionFromString(),
        annotation.typeConversionDateFormat());
  }
}
