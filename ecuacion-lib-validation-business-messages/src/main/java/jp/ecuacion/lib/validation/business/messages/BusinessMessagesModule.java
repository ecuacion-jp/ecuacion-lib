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

package jp.ecuacion.lib.validation.business.messages;

/**
 * Marker class for the {@code ecuacion-lib-validation-business-messages} module.
 *
 * <p>This module contains only resource bundles
 * ({@code ValidationMessages*.properties}) and intentionally provides no public
 * API. This marker class exists solely so that the Javadoc tool can generate
 * output for the module — on some JDK distributions, {@code javadoc} fails with
 * "No public or protected classes found to document" if a module has no public
 * type, and the resulting empty Javadoc jar is rejected by Maven Central
 * publishing validation.
 *
 * <p>See ADR-0005 for the architectural rationale of this module, and ADR-0006
 * for the rationale of this marker class.
 */
public final class BusinessMessagesModule {

  private BusinessMessagesModule() {
  }
}
