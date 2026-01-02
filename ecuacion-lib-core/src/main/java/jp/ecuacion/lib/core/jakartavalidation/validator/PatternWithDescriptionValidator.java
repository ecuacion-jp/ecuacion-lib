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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Checks if a string matches specified regular expression.
 */
public class PatternWithDescriptionValidator
    implements ConstraintValidator<PatternWithDescription, String> {

  private String regExp;
  
  /**
   * Constructs a new instance.
   */
  public PatternWithDescriptionValidator() {

  }

  /** Initializes an instance. */
  @Override
  public void initialize(PatternWithDescription constraintAnnotation) {
    regExp = constraintAnnotation.regexp();
  }

  /**
   * Checks if a string matches specified standard expression.
   * 
   * <p>{@code null} is valid following to the specification of Jakarta EE.<br>
   * {@code empty ("")} is invalid if it doesn't match the standard expression.</p>
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {

    // true if value is null or blank
    if (StringUtils.isEmpty(value)) {
      return true;
    }

    Objects.requireNonNull(value);
    Matcher m = Pattern.compile(regExp).matcher(value);
    return m.matches();
  }
}
