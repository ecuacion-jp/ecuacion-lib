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
 * Designates that the method throws NullPointerException when the annotated arguments is null.
 * 
 * <p>This is almost equal to {@code jakarta.annotation.Nonnull}, 
 *     but {@code @Nonnull} is analyzed by the IDE and IDE warns the nullable parameter.
 *     If you want to show that the method throws NullPointerException 
 *     when the annotated arguments is null, but NO IDE warning needed, use {@RequireNonnull}.</p>
 * 
 * <p>This annnotation does nothing. 
 *     Just for the explanation of the implementation of the method to developers.</p>
 *
 * @see jakarta.annotation.Nonnull
 * @see java.util.Objects
 */
@Documented
@Retention(RUNTIME)
public @interface RequireNonnull {

}
