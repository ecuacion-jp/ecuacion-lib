package jp.ecuacion.lib.core.exception.unchecked;

/**
 * Designates non-empty is required.
 */
public class RequireNonEmptyException extends EclibRuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Construct a new instance.
   */
  public RequireNonEmptyException() {
    super("Non-empty required.");
  }
}
