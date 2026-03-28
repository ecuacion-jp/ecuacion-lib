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

import jp.ecuacion.lib.validation.constraints.internal.AbstractNumberStringValidator;

/**
 * Provides the validation logic for {@code IntegerString}.
 *
 * <p>A string is valid if the value is blank or {@code Integer.valueOf()} does not throw
 *     an exception.</p>
 *
 * <p>Comma-separated values are acceptable. This validator removes commas before checking.
 *     This does not check the positions of the commas are correct.</p>
 *
 * <p>Valid strings are: "{@code 123}", "{@code 123,456}", "{@code 12,3,4,56}"</p>
 *
 * <p>{@code null} is valid following to the specification of Jakarta EE.</p>
 *
 * <p>{@code empty ("")} is also valid.<br>
 *     Blank is allowed because it's supposed to be used for fields in record,
 *     which accepts values from external.
 *     HTML (and maybe others) cannot tell difference between blank and null for number values.
 *     Otherwise we also want the information whether the empty value is submitted (=blank)
 *     or not (=null).</p>
 */
public class IntegerStringValidator extends AbstractNumberStringValidator<IntegerString> {

  @Override
  protected void parseNumber(String value) {
    Integer.valueOf(value);
  }
}
