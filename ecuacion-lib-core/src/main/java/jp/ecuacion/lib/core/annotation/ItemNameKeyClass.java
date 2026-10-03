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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies an alternate {@code itemNameKey} for validation.
 *
 * <p>When validating some object, some itemNameKey is set by default.
 *     But sometimes it's not proper, so this annotation provides the way to change it.</p>
 *
 * <p>It can be placed at a class, a field, or a getter.</p>
 *
 * <ul>
 * <li>At a class: replaces the class part of {@code itemNameKey} (which defaults to the class
 *     name) for fields directly owned by that class,
 *     when its itemPropertyPath is not nested (like {@code "name"}).</li>
 * <li>At a field holding a bean, or a collection, an array or a map of beans:
 *     replaces the class part of {@code itemNameKey} (which defaults to the field name)
 *     for fields of that bean reached through the annotated field.
 *     For example, with {@code @ItemNameKeyClass("dept") List<Dept> deptList},
 *     the itemNameKey of {@code deptList[0].name} becomes {@code dept.name}
 *     instead of {@code deptList.name}.</li>
 * <li>At a getter: works the same as at a field,
 *     when the getter is used for the property because no field of the same name exists
 *     (like {@code getDeptList()} without the field {@code deptList}).
 *     When the field exists, the annotation at the field is used
 *     and the one at the getter is ignored.</li>
 * </ul>
 *
 * <p>Either is overridden by the class part explicitly specified
 *     by {@code Item#itemNameKey(String)}.</p>
 *
 * @see <a href="URL">https://github.com/ecuacion-jp/ecuacion-jp.github.io/blob/main/documentation/common/naming-convention.md</a>
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RUNTIME)
@Documented
public @interface ItemNameKeyClass {

  /**
   * Specifies {@code ItemNameKeyClass}.
   */
  String value();
}
