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

import jakarta.validation.ConstraintViolation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ValidatorMessageParameterCreator;
import jp.ecuacion.lib.core.util.ItemUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Is a LocalizedMessageParameter creator for Comparison validators.
 */
public class ComparisonValidatorMessageParameterCreator
    implements ValidatorMessageParameterCreator {

  @Override
  public Map<@NonNull String, @Nullable Object> create(ConstraintViolation<?> cv,
      Map<@NonNull String, @Nullable Object> paramMap) {

    Map<@NonNull String, @Nullable Object> result = new HashMap<>();

    // This constraint (e.g. @LessThanOrEqualTo) is always class-level (@Target(TYPE)), so
    // cv.getLeafBean() is always the exact bean instance the annotation is placed on, and
    // baselinePropertyPath is always a direct property of that same bean. Resolving relative
    // to it directly (rather than concatenating cv.getPropertyPath() onto cv.getRootBean())
    // keeps the item name based on the bean's own identity (@ItemNameKeyClass or class name)
    // regardless of how deeply that bean is nested under the root.
    String bpp = Objects.requireNonNull((String) paramMap.get("baselinePropertyPath"));
    Item item = ItemUtil.resolveItem(bpp, cv.getLeafBean());
    result.put("baselinePropertyPathItemName",
        new ItemNameParam(List.of(item), cv.getRootBean()));

    return result;
  }
}
