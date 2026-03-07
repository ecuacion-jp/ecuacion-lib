package jp.ecuacion.lib.core.exception.checked;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import jp.ecuacion.lib.core.util.ValidationUtil.MessageParameters;

/**
 * Provides {@code ConstraintViolationException} with meta data.
 */
public class ConstraintViolationExceptionWithParameters extends ConstraintViolationException {

  private static final long serialVersionUID = 1L;

  private MessageParameters messageParameters;

  public MessageParameters getMessageParameters() {
    return messageParameters;
  }

  /**
   * Construct a new instance.
   */
  public ConstraintViolationExceptionWithParameters(
      Set<? extends ConstraintViolation<?>> constraintViolations,
      MessageParameters messageParameters) {
    super(constraintViolations);

    this.messageParameters = messageParameters;
  }
}
