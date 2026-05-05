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

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ValidatorMessageParameterCreator;
import jp.ecuacion.lib.core.util.ExceptionUtil.LocalizedEmbeddedParameter;
import jp.ecuacion.lib.core.util.MessageUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Is a LocalizedMessageParameter creator for PatternWithDescription.
 */
public class ComparisonValidatorMessageParameterCreator
    implements ValidatorMessageParameterCreator {

  @Override
  public Set<LocalizedEmbeddedParameter> create(ConstraintViolationBean<?> cv,
      Map<@NonNull String, @Nullable Object> paramMap) {

    Set<LocalizedEmbeddedParameter> messageParameterSet = new HashSet<>();

    // Comparison validators
    String bpp =
        Objects.requireNonNull((String) cv.getEmbeddedParamMap().get("baselinePropertyPath"));
    Item item = MessageUtil.getItem(bpp, cv.getRootBean(), cv.getLeafBean());
    messageParameterSet.add(new LocalizedEmbeddedParameter("baselinePropertyPathItemName",
        new PropertiesFileUtilFileKindEnum[] {PropertiesFileUtilFileKindEnum.ITEM_NAMES}, true,
        new Item[] {item}, cv.getRootBean(), item.getItemNameKey(), new Arg[] {}));

    return messageParameterSet;
  }
}
