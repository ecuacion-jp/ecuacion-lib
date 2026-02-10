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
package jp.ecuacion.lib.core.jakartavalidation.validator.internal;

import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import org.apache.commons.lang3.tuple.Pair;

public abstract class ClassValidator extends ReflectionUtil {

  protected String[] propertyPaths;
  protected Object instance;
  protected ConstraintValidatorContext context;

  public void initialize(String[] propertyPath) {
    this.propertyPaths = propertyPath;
  }

  protected abstract void procedureBeforeLoopForEachPropertyPath();

  protected abstract boolean isValidForSinglePropertyPath(String propertyPath,
      Object valueOfPropertyPath);

  /**
   * Executes validation check.
   */
  public boolean isValid(Object instance, ConstraintValidatorContext context) {
    this.instance = instance;
    this.context = context;

    procedureBeforeLoopForEachPropertyPath();

    List<Pair<String, Object>> valueOfFieldList = Arrays.asList(propertyPaths).stream()
        .map(path -> Pair.of(path, getValue(instance, path))).toList();

    for (Pair<String, Object> pair : valueOfFieldList) {
      boolean result = isValidForSinglePropertyPath(pair.getLeft(), pair.getRight());

      if (!result) {
        return false;
      }
    }

    return true;
  }
}
