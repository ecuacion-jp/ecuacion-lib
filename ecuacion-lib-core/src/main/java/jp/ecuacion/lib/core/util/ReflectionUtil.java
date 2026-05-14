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
package jp.ecuacion.lib.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides low-level utility methods for {@code java.lang.reflect}.
 *
 * <p>Methods that navigate object graphs using a {@code propertyPath} string
 *     (e.g. {@code "dept.name"}, {@code "list[0]"}) have been moved to
 *     {@link PropertyPathUtil}.</p>
 */
public class ReflectionUtil {

  /**
   * Returns true when designated class exists.
   *
   * @param className className with package (like "java.lang.Object")
   * @return boolean
   */
  public static boolean classExists(String className) {
    try {
      Class.forName(className);
      return true;

    } catch (ClassNotFoundException ex) {
      return false;
    }
  }

  /**
   * Returns new instance constructed with a no-argument constructor.
   *
   * @param className className with package (like "java.lang.Object")
   * @return new instance
   */
  public static Object newInstance(String className) {
    try {
      Class<?> cls = Class.forName(className);
      return cls.getConstructor().newInstance();

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Searches for a class annotation in the argument class and its superClasses.
   *
   * <p>The search starts at the argument instance, and if it doesn't have the annotation,
   *     It searches the superClass of the instance next.<br>
   *     And if it continues to search the annotation and it reaches to Object.class,
   *     it stops to search and returns empty Optional.</p>
   *
   * <p>Search ends when it finds the first annotation.
   *     Even if there is another annotation of the same class,
   *     it ignores it and returns the first found annotation.</p>
   */
  @SuppressWarnings("null")
  public static <A extends Annotation> @NonNull Optional<@NonNull A> searchAnnotationPlacedAtClass(
      Class<?> classOfTargetInstance, Class<A> annotationClass) {
    while (true) {
      // No more ancestors
      // Equals to null when it's an anonymous class created directly from Interface.
      if (classOfTargetInstance == null || classOfTargetInstance == Object.class) {
        return Optional.empty();
      }

      A an = (A) classOfTargetInstance.getAnnotation(annotationClass);
      if (an != null) {
        return Optional.of(an);
      }

      classOfTargetInstance = Objects.requireNonNull(classOfTargetInstance.getSuperclass());
    }
  }

  /**
   * Searches for a declared field by simple name, traversing the class hierarchy.
   *
   * <p>The argument {@code simpleFieldName} must not contain {@code "."} or {@code "["}
   *     (use {@link PropertyPathUtil#getField(Class, String)} for path-based lookup).</p>
   *
   * @param cls starting class
   * @param simpleFieldName field name without path notation
   * @return {@link Field}
   */
  public static Field getDeclaredField(Class<?> cls, String simpleFieldName) {
    if (simpleFieldName.contains("[")) {
      throw new RuntimeException(
          "fieldName with index (like value[0]) not acceptable. fieldName: " + simpleFieldName);
    }

    Exception ex = null;
    while (true) {
      if (cls.equals(Object.class)) {
        break;
      }
      try {
        return cls.getDeclaredField(simpleFieldName);
      } catch (Exception exception) {
        if (ex == null) {
          ex = exception;
        }
      }
      cls = Objects.requireNonNull(cls.getSuperclass());
    }

    throw new RuntimeException(ex);
  }

  /**
   * Returns the value of a field from an object, using {@code setAccessible(true)}.
   *
   * <p>Uses {@link Field#setAccessible(boolean)} internally to access private fields.
   *     This triggers SpotBugs' {@code REFLF_REFLECTION_MAY_INCREASE_ACCESSIBILITY_OF_FIELD},
   *     which is suppressed via the project-level SpotBugs exclude filter.</p>
   *
   * @param object the object to read from
   * @param field the field to read
   * @return the field value, or {@code null} if the field holds {@code null}
   */
  public static @Nullable Object getFieldValue(Object object, Field field) {
    try {
      field.setAccessible(true);
      return field.get(object);
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new RuntimeException(
          "Field value cannot be obtained from the field '" + field.getName() + "'", ex);
    }
  }
}
