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
 * Enumerates the state-based subset of {@link ConditionValue} that has no value of its own
 * to distinguish it (unlike {@code STRING}, {@code PATTERN} or {@code VALUE_OF_PROPERTY_PATH}).
 *
 * <p>Used as the type of the {@code conditionValueState} annotation element, which lets
 *     {@code conditionValue} be inferred as one of these 4 values without having to set
 *     {@code conditionValue} itself. Constant names are kept identical to their
 *     {@link ConditionValue} counterparts so the two can be mapped by name.</p>
 */
public enum ConditionValueState {

  NULL, NOT_NULL, EMPTY, NOT_EMPTY,

  // Used only as the default value of the {@code conditionValueState} annotation element,
  // meaning "this element is not set". Mirrors ConditionValue.UNSPECIFIED.
  UNSPECIFIED;
}
