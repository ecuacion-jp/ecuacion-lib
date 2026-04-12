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
package jp.ecuacion.lib.validation.constraints;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.jakartavalidation.bean.ValidatorMessageParameterCreator;
import jp.ecuacion.lib.core.util.ExceptionUtil.LocalizedEmbeddedParameter;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * Is a LocalizedMessageParameter creator for PatternWithDescription.
 */
public class PatternWithDescriptionMessageParameterCreator extends ReflectionUtil
    implements ValidatorMessageParameterCreator {

  @Override
  public Set<LocalizedEmbeddedParameter> create(ConstraintViolationBean<?> cv,
      Map<String, Object> paramMap) {

    Set<LocalizedEmbeddedParameter> messageParameterSet = new HashSet<>();

    final String key = "description";
    String description = (String) paramMap.get(key);

    if (StringUtils.isEmpty(description)) {
      throw new EclibRuntimeException("@PatternWithDescription needs " + key + " value.");
    }

    messageParameterSet.add(new LocalizedEmbeddedParameter("description",
        new PropertiesFileUtilFileKindEnum[] {
            PropertiesFileUtilFileKindEnum.VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS},
        (String) paramMap.get("description"), new Arg[] {}));

    return messageParameterSet;
  }
}
