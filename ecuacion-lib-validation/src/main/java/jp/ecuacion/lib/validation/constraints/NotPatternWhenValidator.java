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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.validation.constraints.internal.ValidateWhenValidator;

/**
 * Provides the validation logic for {@code NotPatternWhen}.
 */
public class NotPatternWhenValidator extends ValidateWhenValidator<NotPatternWhen, Object> {

  private String propertyValuePatternRegexp;

  /** Initializes an instance. */
  @Override
  public void initialize(NotPatternWhen annotation) {
    super.initialize(annotation.message(), annotation.propertyPath(),
        annotation.conditionPropertyPath(), annotation.conditionValue(),
        annotation.conditionOperator(), annotation.conditionValueString(),
        annotation.conditionValuePatternRegexp(), annotation.conditionValuePropertyPath(),
        annotation.patternWhenConditionNotSatisfied());

    this.propertyValuePatternRegexp = annotation.regexp();
  }

  @Override
  protected boolean isValid(Object valueOfField) {
    if (StringUtil.isObjectNullOrEmpty(valueOfField)) {
      return true;
    }

    if (!(valueOfField instanceof String)) {
      throw new EclibRuntimeException("The data type of propertyPath must be String.");
    }

    Pattern p = Pattern.compile(propertyValuePatternRegexp);
    Matcher m = p.matcher((String) valueOfField);

    return !m.find();
  }

}
