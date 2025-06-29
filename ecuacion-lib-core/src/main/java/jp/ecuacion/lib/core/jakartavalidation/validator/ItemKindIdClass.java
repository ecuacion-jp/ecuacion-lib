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
package jp.ecuacion.lib.core.jakartavalidation.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * THIS IS NOT A VALIDTOR. 
 *     This specifies {@code itemKindIdClass} on validated by {@code ValidationUtil}.
 * 
 * <p>When {@code ValidationUtil} validates some object, itemKindId is set by default.
 *     But sometimes it's not proper, so this annotation provides the way to change it.</p>
 * 
 * @see <a href="URL">https://github.com/ecuacion-jp/ecuacion-jp.github.io/blob/main/documentation/common/naming-convention.md</a>
 */
@Target({FIELD, ElementType.TYPE})
@Retention(RUNTIME)
@Documented
public @interface ItemKindIdClass {

  /**
   * Specifies itemKindIdClass.
   */
  String value();
}
