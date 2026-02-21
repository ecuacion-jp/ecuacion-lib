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
package jp.ecuacion.lib.validation.util;

import jp.ecuacion.lib.validation.constraints.NotEmptyWhen;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValuePattern;

@NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "conditionValue",
    conditionPattern = ConditionValuePattern.STRING,
    conditionValueString = "abc")
public class ValidationUtilTest_ObjWithClassValidator {
  public String conditionValue = "abc";
  public String value = null;
}
