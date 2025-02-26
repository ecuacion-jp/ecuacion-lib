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

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.util.ObjectsUtil;

/**
 * Conveys multiple SingleAppExceptions at once.
 * 
 * <p>This has a list of {@code SingleAppException} inside 
 * and by throwing {@code MultipleAppException} multiple
 * error messages can be shown on screen in web apps.</p>
 */
public class MultipleAppException extends AppException {
  private static final long serialVersionUID = 1L;

  private List<SingleAppException> exceptionList;

  /**
   * Constructs a new instance with a list of {@code AppException}.
   * 
   * @param list a list of {@code AppException}. {@code size()} cannot be zero. 
   */
  public MultipleAppException(@Nonnull List<? extends SingleAppException> list) {
    super();

    ObjectsUtil.paramRequireNonNull(list);
    ObjectsUtil.paramSizeNonZero(list);

    // 本メソッドの引数としてはAppException、つまりMultipleAppExceptionも許しているが、
    // 内部的にはSingleAppExceptionのListとして保持するため、MultipleAppExceptionは分解する。
    List<SingleAppException> internalList = new ArrayList<>();
    for (SingleAppException ae : list) {
      internalList.add((SingleAppException) ae);
    }

    this.exceptionList = internalList;
  }

  /**
   * Constructs a new instance with {@code MultipleAppException}.
   * 
   * @param ex {@code NultipleAppException}
   */
  public MultipleAppException(@Nonnull MultipleAppException ex) {
    List<SingleAppException> internalList = new ArrayList<>();
    internalList.addAll(ex.getList());

    this.exceptionList = internalList;
  }

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    exceptionList.stream().forEach(ex -> sb.append(ex.getMessage()));

    return sb.toString();
  }

  /** 
   * Returns a list of {@code SingleAppException}.
   * 
   * @return exceptionList
   */
  @Nonnull
  public List<SingleAppException> getList() {
    return new ArrayList<>(exceptionList);
  }
}
