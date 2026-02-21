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
 * Enumerates how to determine condition is valid.
 */
public enum ConditionValue {

  // null (for any data type) or String blank.
  // no additional value selection is needed.
  EMPTY,

  // boolean.
  // no additional value selection is needed.
  TRUE, FALSE,

  // Setting value of conditionValueString is needed.
  STRING,

  // Setting value of conditionValuePattern is needed.
  PATTERN,

  // Setting value of conditionValuePropertyPath is needed.
  VALUE_OF_PROPERTY_PATH;
}
