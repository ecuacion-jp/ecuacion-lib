package jp.ecuacion.lib.core.jakartavalidation.constraintvalidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jp.ecuacion.lib.core.jakartavalidation.constraints.MultiplePropertyPathsValidator;

/**
 * Creates multiple constraintViolations out of one validation.
 * It is realized by setting the value of createsMultipleConstraintViolations to true.
 */
public class CreateMultipleConstraintViolationsConstraintValidatorFactory
    implements ConstraintValidatorFactory {

  @Override
  public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
    T validator = null;
    try {
      validator = key.getDeclaredConstructor().newInstance();

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
  public void releaseInstance(ConstraintValidator<?, ?> instance) {}
}
