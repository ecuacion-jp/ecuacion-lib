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
package jp.ecuacion.lib.core.jakartavalidation.constraintvalidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import java.util.Objects;
import jp.ecuacion.lib.core.jakartavalidation.constraints.MultiplePropertyPathsValidator;
import org.jspecify.annotations.Nullable;

/**
 * Creates multiple constraintViolations out of one validation.
 * It is realized by setting the value of createsMultipleConstraintViolations to true.
 */
public class CreateMultipleConstraintViolationsConstraintValidatorFactory
    implements ConstraintValidatorFactory {

  @Override
  public <T extends ConstraintValidator<?, ?>> T getInstance(@Nullable Class<T> key) {
    T validator;
    try {
      validator = Objects.requireNonNull(key).getDeclaredConstructor().newInstance();

      if (validator instanceof MultiplePropertyPathsValidator) {
        ((MultiplePropertyPathsValidator<?, ?>) validator)
            .setCreatesMultipleConstraintViolations(true);
      }

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    return validator;
  }

  @Override
  public void releaseInstance(@Nullable ConstraintValidator<?, ?> instance) {}
}
