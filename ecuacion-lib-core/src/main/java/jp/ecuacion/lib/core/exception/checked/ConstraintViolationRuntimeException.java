package jp.ecuacion.lib.core.exception.checked;

import jakarta.validation.ConstraintViolation;
import java.util.Set;

/**
 * Is used as ConstraintViolationException but unchecked one.
 */
public class ConstraintViolationRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private Set<ConstraintViolation<?>> constraintViolations;

  /**
   * Constructs a new instance.
   */
  public ConstraintViolationRuntimeException(Set<ConstraintViolation<?>> constraintViolations) {
    this.constraintViolations = constraintViolations;
  }

  public Set<ConstraintViolation<?>> getConstraintViolations() {
    return constraintViolations;
  }
}
