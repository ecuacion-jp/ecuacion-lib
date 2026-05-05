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
 * Provides business-friendly Japanese validation messages that override the
 * neutral fallbacks defined in {@code ecuacion-lib-core} /
 * {@code ecuacion-lib-validation}.
 *
 * <p>This module contains only resource bundles
 * ({@code ValidationMessages*.properties}); adding it as a runtime dependency
 * activates {@code .default} key fallback for Jakarta Validation messages.
 * See ADR-0005 for the architectural rationale.
 */
package jp.ecuacion.lib.validation.business.messages;
