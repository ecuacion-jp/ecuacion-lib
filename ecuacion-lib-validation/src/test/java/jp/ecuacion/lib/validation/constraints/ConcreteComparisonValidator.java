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
import jp.ecuacion.lib.validation.constraints.enums.ComparisonType;
import jp.ecuacion.lib.validation.constraints.internal.ComparisonValidator;
import org.jspecify.annotations.Nullable;

/**
 * Provides the validation logic for {@code EnumElement}.
 */
public class ConcreteComparisonValidator extends ComparisonValidator<Comparison, Object> {

  /** Initializes an instance. */
  @Override
  public void initialize(@Nullable Comparison annotation) {
    Objects.requireNonNull(annotation);
    boolean isLess = annotation.isValidWhenLessThanBasis();
    boolean allowsEqual = annotation.allowsEqual();
    ComparisonType comparisonType = isLess
        ? (allowsEqual ? ComparisonType.LESS_THAN_OR_EQUAL_TO : ComparisonType.LESS_THAN)
        : (allowsEqual ? ComparisonType.GREATER_THAN_OR_EQUAL_TO : ComparisonType.GREATER_THAN);
    super.initialize(annotation.message(), annotation.propertyPath(),
        annotation.basisPropertyPath(), comparisonType,
        annotation.typeConversionFromString(), "yyyy-MM-dd");
  }
}
