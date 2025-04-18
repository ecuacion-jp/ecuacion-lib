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
 * provides jpa-related classes.
 */
module jp.ecuacion.lib.jpa {
  exports jp.ecuacion.lib.jpa.entity;

  requires jakarta.annotation;
  requires transitive jakarta.persistence;

  requires transitive jp.ecuacion.lib.core;
  
  requires org.apache.commons.lang3;
}
