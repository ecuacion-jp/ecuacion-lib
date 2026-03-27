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
package jp.ecuacion.lib.validation.constraints.internal;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides common validation logic for number-string validators
 *     such as {@code IntegerStringValidator} and {@code LongStringValidator}.
 *
 * <p>A string is valid if the value is blank or {@link #parseNumber(String)} does not throw
 *     an exception. Comma-separated values are acceptable; commas are removed before checking.</p>
 *
 * <p>{@code null} is valid following to the specification of Jakarta EE.</p>
 *
 * @param <A> the constraint annotation type
 */
public abstract class AbstractNumberStringValidator<A extends Annotation>
    implements ConstraintValidator<A, String> {

  /** Initializes an instance. */
  @Override
  public void initialize(A constraintAnnotation) {}

  /**
   * Checks if a string is convertible to the target number type.
   *
   * <p>{@code null} and empty strings are considered valid.</p>
   *
   * @param value the value to validate
   * @param context context in which the constraint is evaluated
   * @return {@code true} if the value is valid
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
      parseNumber(value);
      return true;

    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Parses the given string as the target number type.
   *
   * <p>Implementations should throw an exception if the string cannot be parsed.</p>
   *
   * @param value the string to parse, never {@code null} or empty
   */
  protected abstract void parseNumber(String value);
}
