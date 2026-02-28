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
package jp.ecuacion.lib.validation.constraints.internal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import org.apache.commons.lang3.StringUtils;

public abstract class AllAnyValidator<A extends Annotation, T> extends ClassValidator<A, T> {

  protected int numberOfNonEmptyValues(Object object) {
    List<Object> list = new ArrayList<>();

    for (Object fieldValue : valuesOfPropertyPaths) {
      if (fieldValue instanceof String) {
        if (StringUtils.isNotEmpty((String) fieldValue)) {
          list.add(fieldValue);
        }

      } else {
        if (fieldValue != null) {
          list.add(fieldValue);
        }
      }
    }

    return list.size();
  }
}
