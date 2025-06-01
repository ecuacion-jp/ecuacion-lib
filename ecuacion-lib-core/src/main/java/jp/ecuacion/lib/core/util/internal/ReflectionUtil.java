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
package jp.ecuacion.lib.core.util.internal;

import jakarta.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;

/**
 * Provides utility methods for {@code java.lang.reflect} and other checks.
 */
public class ReflectionUtil {

  /**
   * Searches for a class annotation in the argument instance and its superClasses.
   * 
   * <p>The search starts at the argument instance, and if it doesn't have the annotation, 
   *     It searches the superClass of the instance next.<br>
   *     And if it continues to search the annotation and it reaches to Object.class,
   *     it stops to search and returns empty Optional.</p>
   *     
   * <p>Search ends when it founds the first annotation.
   *     Even if there is another anntation of same class, 
   *     it ignores and it returns first-found annotation.
   */
  protected static <A extends Annotation> Optional<A> searchAnnotationPlacedAtClass(Object instance,
      Class<A> annotation) {
    Class<?> cls = instance.getClass();
    while (true) {
      // No more ancestors
      if (cls == Object.class) {
        return Optional.empty();
      }

      A an = (A) cls.getAnnotation(annotation);
      if (an != null) {
        return Optional.of(an);
      }

      cls = cls.getSuperclass();
    }
  }

  /**
   * Obtains a field value with any scopes and searches fields in super classes.
   * 
   * <p>Since Class#getDeclaredField is used in the method, 
   *     making its scope public causes a spotbugs error.<br>
   *     That's why its scope is protected
   *     and when you use it you need to extend this class.</p>
   * 
   * <code>
   * Public method 
   * jp.ecuacion.lib.core.util.internal.PrivateFieldReadUtil.getFieldValue(String, Object, String) 
   * uses reflection to modify a field it gets in its parameter 
   * which could increase the accessibility of any class. 
   * REFLF_REFLECTION_MAY_INCREASE_ACCESSIBILITY_OF_FIELD
   * </code>
   */
  protected static Object getFieldValue(String fieldName, Object instance) {
    Field field = getField(fieldName, instance);

    field.setAccessible(true);

    try {
      return field.get(instance);

    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throwRuntimeException(ex, fieldName, "Field value");
    }

    // throwRuntimeException always throws Exception so this will never executed.
    return null;
  }

  /**
   * Obtains a field with any scopes and searches fields in super classes.
   * 
   * <p>Since Class#getDeclaredField is used in the method, 
   *     making its scope public causes a spotbugs error.<br>
   *     That's why its scope is protected
   *     and when you use it you need to extend this class.</p>
   *     
   * <code>
   * Public method 
   * jp.ecuacion.lib.core.util.internal.PrivateFieldReadUtil.getFieldValue(String, Object, String) 
   * uses reflection to modify a field it gets in its parameter 
   * which could increase the accessibility of any class. 
   * REFLF_REFLECTION_MAY_INCREASE_ACCESSIBILITY_OF_FIELD
   * </code>
   * 
   * @param fieldName fieldName
   * @param instance instance
   * @return Field
   */
  @Nonnull
  protected static Field getField(String fieldName, Object instance) {
    Field validationTargetField;

    // loop for finding fields in parent's class.
    Class<?> cls = instance.getClass();
    // store first exception
    Exception ex = null;

    while (true) {
      if (cls.equals(Object.class)) {
        break;
      }

      try {
        validationTargetField = cls.getDeclaredField(fieldName);
        return validationTargetField;

      } catch (Exception exception) {
        if (ex == null) {
          ex = exception;
        }
      }

      cls = cls.getSuperclass();
    }

    throwRuntimeException(ex, fieldName, "Field");

    // throwRuntimeException always throws Exception so this will never executed.
    return null;
  }

  private static void throwRuntimeException(Exception ex, String fieldName,
      String whatIsTriedToObtain) {
    throw new EclibRuntimeException(
        whatIsTriedToObtain + "cannot be obtained " + "from the field name '" + fieldName + "'",
        ex);
  }
}
