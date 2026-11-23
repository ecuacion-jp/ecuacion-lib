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

import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;

/**
 * Creates the message parameter that describes a regular expression.
 *
 * <p>It is used for {@code description} of {@code @PatternWithDescription},
 *     {@code @PatternWhen} and {@code @NotPatternWhen}.
 *     When {@code description} is empty, the regular expression itself is used.
 *     Otherwise it is resolved from {@code ValidationMessagesPatternDescriptions.properties},
 *     and the specified string itself is used when the key does not exist.</p>
 */
public class PatternDescriptionParameter {

  private PatternDescriptionParameter() {}

  /**
   * Returns the message parameter that describes {@code regexp}.
   *
   * @param regexp regular expression
   * @param description description of the regular expression, may be empty
   * @return {@code regexp} when {@code description} is empty, otherwise an {@code Arg}
   *     resolved from {@code ValidationMessagesPatternDescriptions.properties}
   */
  public static Object of(String regexp, String description) {
    if (description.isEmpty()) {
      return regexp;
    }

    return Arg.fromFileKinds(
        new PropertiesFileUtilFileKindEnum[] {
            PropertiesFileUtilFileKindEnum.VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS},
        description);
  }
}
