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
/*
 * Provides business-friendly validation messages that override the neutral defaults from
 * ecuacion-lib-core. Properties files must be accessible via the module layer, so this
 * module is declared open.
 */
open module jp.ecuacion.lib.validation.business.messages {
  requires transitive jp.ecuacion.lib.core;
}
