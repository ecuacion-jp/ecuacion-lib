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
package jp.ecuacion.lib.core.jakartavalidation.validator.enums;

/**
 * Enumerates how to determine condition is valid.
 */
public enum ConditionValuePattern {

  // Setting value of conditionValueString is needed.
  string,

  // no additional value selection is needed.
  empty,

  // boolean.
  // By using variation of ConditionOperator (equalTo, notEqualTo),
  // one of booleanTrue or booleanFalse covers all the pattern.
  // But both of them are prepared for understandability.
  booleanTrue, booleanFalse,

  // Setting value of conditionValuePropertyPath is needed.
  valueOfPropertyPath;
}
