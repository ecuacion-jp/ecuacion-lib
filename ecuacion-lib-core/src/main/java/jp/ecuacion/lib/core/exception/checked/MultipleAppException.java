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

import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.annotation.RequireSizeNonZero;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import org.jspecify.annotations.NonNull;

/**
 * Conveys multiple SingleAppExceptions at once.
 * 
 * <p>This has a list of {@code SingleAppException} inside 
 * and by throwing {@code MultipleAppException} multiple
 * error messages can be shown on screen in web apps at once.</p>
 */
public class MultipleAppException extends AppException {
  private static final long serialVersionUID = 1L;

  /**
   * List of {@code SingleAppException}.
   * 
   * <p>Each element is {@code @NonNull} 
   *     because all the elements are {@code null} there's no need to throw an exception.</p>
   */
  private List<@NonNull SingleAppException> exceptionList;

  /**
   * Constructs a new instance with a list of {@code AppException}.
   * 
   * @param list a list of {@code AppException}. {@code size()} cannot be zero. 
   */
  public MultipleAppException(@RequireSizeNonZero List<? extends AppException> list) {
    super();

    ObjectsUtil.requireSizeNonZero(list);

    // Although this method's parameter type is {@code AppException}
    // (which allows {@code MultipleAppException}), {@code MultipleAppException} is decomposed
    // into a list of {@code SingleAppException}.
    List<@NonNull SingleAppException> internalList = new ArrayList<>();

    for (AppException ae : ObjectsUtil.requireElementNonNull(list)) {
      if (ae instanceof SingleAppException) {
        internalList.add((SingleAppException) ae);

      } else if (ae instanceof MultipleAppException) {
        internalList.addAll(((MultipleAppException) ae).getList());

      } else {
        // In the case that {@code AppException} itself is passed as an element of the list.
        throw new RuntimeException(
            "An unassumed exception is thrown. Exception: " + ae.getClass().getCanonicalName());
      }
    }

    this.exceptionList = internalList;
  }

  /**
   * Constructs a new instance with {@code MultipleAppException}.
   * 
   * @param ex {@code NultipleAppException}
   */
  public MultipleAppException(MultipleAppException ex) {
    List<@NonNull SingleAppException> internalList = new ArrayList<>();
    internalList.addAll(ex.getList());

    this.exceptionList = internalList;
  }

  /**
   * Returns list of messages holding exceptions have with default locale.
   */
  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    exceptionList.stream().forEach(ex -> sb.append("- " + ex.getMessage() + "\n"));

    // remove last "\n"
    return sb.toString().substring(0, sb.toString().length() - 1);
  }

  /** 
   * Returns a list of {@code SingleAppException}.
   * 
   * @return exceptionList
   */
  public List<@NonNull SingleAppException> getList() {
    return new ArrayList<>(exceptionList);
  }
}
