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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides the validation logic for {@code SizeString}.
 */
public class SizeStringValidator implements ConstraintValidator<SizeString, String> {

  private int min;
  private int max;
  
  /**
   * Constructs a new instance.
   */
  public SizeStringValidator() {

  }

  /** Initializes an instance. */
  @Override
  public void initialize(SizeString constraintAnnotation) {
    min = constraintAnnotation.min();
    max = constraintAnnotation.max();
  }

  /**
   * Checks if a string is allowable size.
   * 
   * <p>a string is valid if the value is null or it's size is within min and max.</p>
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

    return value.length() >= min && value.length() <= max;
  }
}
