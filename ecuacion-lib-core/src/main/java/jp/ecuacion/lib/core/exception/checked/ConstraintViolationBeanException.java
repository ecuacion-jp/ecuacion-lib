package jp.ecuacion.lib.core.exception.checked;

import jakarta.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;

/**
 * Is a throwable with ConstraintViolationBean stored.
 */
public class ConstraintViolationBeanException extends AppException {

  private static final long serialVersionUID = 1L;

  /**
   * The name is not constraintViolationBeanSet but constraintViolationBeans
   * because ConstraintViolationException stores ConstraintViolations.
   */
  private Set<ConstraintViolationBean<?>> constraintViolationBeans;

  /**
   * Constructs a new instance.
   */
  @SuppressWarnings("unchecked")
  public <T> ConstraintViolationBeanException(Set<T> constraintViolations) {
    if (constraintViolations == null || constraintViolations.size() == 0) {
      throw new EclibRuntimeException("ConstraintViolation required.");
    }

    T obj = constraintViolations.stream().toList().get(0);
    if (obj instanceof ConstraintViolation) {
      this.constraintViolationBeans = new HashSet<>(constraintViolations.stream()
          .map(cv -> new ConstraintViolationBean<>((ConstraintViolation<?>) cv)).toList());

    } else if (obj instanceof ConstraintViolationBean) {
      this.constraintViolationBeans = (Set<ConstraintViolationBean<?>>) constraintViolations;
    }
  }

  public Set<ConstraintViolationBean<?>> getConstraintViolationBeans() {
    return constraintViolationBeans;
  }
}
