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

/**
 * Provides the validation logic for {@code BooleanString}.
 */
public class BooleanStringValidator implements ConstraintValidator<BooleanString, String> {

  /**
   * Constructs a new instance.
   */
  public BooleanStringValidator() {

  }

  /** Initializes an instance. */
  @Override
  public void initialize(BooleanString constraintAnnotation) {}

  /**
   * Checks if a string is convertable to {@code Boolean}.
   * 
   * <p>Valid strings are as follows. <br>
   * (case-insensitive, the specification follows to 
   * "apache-commons-lang:BooleanUtils.toBoolean(String str)", but "○" and "×" are added.))</p>
   * 
   * <ul>
   * <li>treated as {@code true} : 
   *     {@code true}, {@code t}, {@code on}, {@code yes}, {@code y}, {@code ○}</li>
   * <li>treated as {@code false}: 
   *     {@code false}, {@code f}, {@code off}, {@code no}, {@code n}, {@code ×}</li>
   * </ul>
   * 
   * <p>{@code null} is valid following to the specification of Jakarta EE.<br>
   * {@code empty ("")} is invalid.</p>
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {

    // true if value == null (which consists with the specification of jakarta validation)
    if (value == null) {
      return true;
    }

    Objects.requireNonNull(value);

    String[] allowedLowerCaseStrings =
        new String[] {"true", "false", "on", "off", "yes", "no", "t", "f", "y", "n", "○", "×"};

    for (String keyword : allowedLowerCaseStrings) {
      if (keyword.equals(value.toLowerCase())) {
        return true;
      }
    }

    return false;
  }
}
