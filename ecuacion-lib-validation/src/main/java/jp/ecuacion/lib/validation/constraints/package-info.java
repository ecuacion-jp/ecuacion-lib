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

/**
 * Provides {@code jakarta validation} validators.
 *
 * <p>Note on {@code @Nullable} on {@code ConstraintValidator} method parameters: some
 *     parameters of methods overriding {@code jakarta.validation.ConstraintValidator}
 *     are annotated {@code @Nullable} even though they are never {@code null} at
 *     runtime. This is required to avoid an "Illegal redefinition of parameter" error
 *     in Eclipse null analysis, which reports a violation when {@code @NullMarked} code
 *     strengthens the nullness constraint on a parameter inherited from an unannotated
 *     method. The affected parameters are: the annotation parameter of
 *     {@code initialize()}, and the {@code ConstraintValidatorContext} parameter of
 *     {@code isValid()}.</p>
 */
@NullMarked
package jp.ecuacion.lib.validation.constraints;

import org.jspecify.annotations.NullMarked;
