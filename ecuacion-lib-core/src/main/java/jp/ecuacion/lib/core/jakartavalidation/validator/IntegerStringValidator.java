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
package jp.ecuacion.lib.core.jakartavalidation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides the validation logic for {@code IntegerString}.
 */
public class IntegerStringValidator implements ConstraintValidator<IntegerString, String> {

  /**
   * Constructs a new instance.
   */
  public IntegerStringValidator() {

  }

  /** Initializes an instance. */
  @Override
  public void initialize(IntegerString constraintAnnotation) {}

  /**
   * Checks if a string is convertible to {@code Integer}.
   * 
   * <p>a string is valid if the value is blank or Integer.valueOf() does not throw exception.</p>
   * 
   * <p>comma-separated value is acceptable. This validator removes comma before check.
   * This does not check the positions of the commas are correct.</p>
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
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {

    // true if value is null or blank
    if (StringUtils.isEmpty(value)) {
      return true;
    }

    Objects.requireNonNull(value);

    // Remove commas. Don't have to check whether the position of commas is right.
    value = value.replaceAll(",", "");

    try {
      Integer.valueOf(value);
      return true;

    } catch (Exception e) {
      return false;
    }
  }
}
