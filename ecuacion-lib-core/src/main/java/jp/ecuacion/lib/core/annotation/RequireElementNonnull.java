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
package jp.ecuacion.lib.core.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

/**
 * Designates that the method throws RequireNonEmptyException 
 *     when the annotated arguments is empty. (= null or blank(""))
 * 
 * <p>This annnotation does nothing. 
 *     Just for the explanation of the implementation of the method to developers.</p>
 *
 * @see jakarta.annotation.Nonnull
 * @see java.util.Objects
 */
@Documented
@Retention(RUNTIME)
public @interface RequireElementNonnull {

}
