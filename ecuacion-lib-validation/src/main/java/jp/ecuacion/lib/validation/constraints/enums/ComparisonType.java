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
package jp.ecuacion.lib.validation.constraints.enums;

/**
 * Describes the comparison type used in comparison validators.
 */
public enum ComparisonType {
  /** Checks that the value is greater than the baseline. */
  GREATER_THAN(false, false),

  /** Checks that the value is less than the baseline. */
  LESS_THAN(true, false),

  /** Checks that the value is greater than or equal to the baseline. */
  GREATER_THAN_OR_EQUAL_TO(false, true),

  /** Checks that the value is less than or equal to the baseline. */
  LESS_THAN_OR_EQUAL_TO(true, true);

  private final boolean validWhenLessThanBasis;
  private final boolean allowsEqual;

  ComparisonType(boolean validWhenLessThanBasis, boolean allowsEqual) {
    this.validWhenLessThanBasis = validWhenLessThanBasis;
    this.allowsEqual = allowsEqual;
  }

  /** Returns whether a value less than the baseline is valid. */
  public boolean isValidWhenLessThanBasis() {
    return validWhenLessThanBasis;
  }

  /** Returns whether equal values are considered valid. */
  public boolean allowsEqual() {
    return allowsEqual;
  }
}
