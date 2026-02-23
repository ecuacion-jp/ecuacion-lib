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

    } else {
      throw new EclibRuntimeException(
          "T needs to be an instance of either ConstraintViolation or ConstraintViolationBean.");
    }
  }

  public Set<ConstraintViolationBean<?>> getConstraintViolationBeans() {
    return constraintViolationBeans;
  }
}
