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
import java.util.List;
import java.util.Optional;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;

/**
 * Provides utility methods for {@code java.lang.reflect} and other checks.
 */
public class ReflectionUtil {

  /**
   * Searches for a class annotation in the argument class and its superClasses.
   * 
   * <p>The search starts at the argument instance, and if it doesn't have the annotation, 
   *     It searches the superClass of the instance next.<br>
   *     And if it continues to search the annotation and it reaches to Object.class,
   *     it stops to search and returns empty Optional.</p>
   *     
   * <p>Search ends when it founds the first annotation.
   *     Even if there is another anntation of same class, 
   *     it ignores and it returns first-found annotation.</p>
   */
  public static <A extends Annotation> Optional<A> searchAnnotationPlacedAtClass(
      Class<?> classOfTargetInstance, Class<A> annotation) {
    while (true) {
      // No more ancestors
      if (classOfTargetInstance == Object.class) {
        return Optional.empty();
      }

      A an = (A) classOfTargetInstance.getAnnotation(annotation);
      if (an != null) {
        return Optional.of(an);
      }

      classOfTargetInstance = classOfTargetInstance.getSuperclass();
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
  protected static Object getFieldValue(String propertyPath, Object instance) {
    try {
      String chileItemPropertyPath = propertyPath.substring(propertyPath.indexOf(".") + 1);

      while (true) {
        if (propertyPath.contains(".")) {
          String rootFieldName = propertyPath.substring(0, propertyPath.indexOf("."));
          return getFieldValue(chileItemPropertyPath, getFieldValue(rootFieldName, instance));

        } else {
          if (propertyPath.contains("[")) {
            String propertyPathWithoutIndex = propertyPath.substring(0, propertyPath.indexOf("["));
            String tmpSerial = propertyPath.substring(propertyPath.indexOf("[") + 1);
            // It's string because it can be non-number value when the validated object is Map.
            String index = tmpSerial.substring(0, tmpSerial.indexOf("]"));
            Field rootField = getField(propertyPathWithoutIndex, instance.getClass());
            rootField.setAccessible(true);
            Object objs = rootField.get(instance);

            // Resolve the field for array or List.
            // Occur an exception for any other collections
            // because it's impossible to speciry the element in Set
            // (since the propertyPath is like "childSet[]")
            // and Map (since the propertyPath is like "childMap[test]"
            // where test is the key of them map entry. Any type can be key and
            // it's impossible to resolve it).
            if (objs instanceof Object[]) {
              return ((Object[]) objs)[Integer.parseInt(index)];

            } else if (objs instanceof List<?>) {
              return ((List<?>) objs).get(Integer.parseInt(index));

            } else {
              throw new EclibRuntimeException("Multiple value types other than array and List "
                  + "are not supported. The type of value: " + objs.getClass().getCanonicalName());
            }

          } else {
            Field rootField = getField(propertyPath, instance.getClass());
            rootField.setAccessible(true);
            return rootField.get(instance);
          }
        }
      }

    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throwRuntimeException(ex, propertyPath, "Field value");
    }

    // throwRuntimeException always throws Exception so this will never executed.
    return null;
  }

  /**
   * Obtains a field with any scopes and also searches fields in super classes.
   * 
   * @param fieldName fieldName
   * @param classOfTargetInstance classOfTargetInstance
   * @return {@code Pair<Field, Object>} left-hand side is the obtained field, 
   *     right-hand side is its instance.
   *     When you set "dept.name" to fieldName, instance would be "dept".
   */
  @Nonnull
  static Field getField(String fieldName, Class<?> classOfTargetInstance) {
    Field validationTargetField;

    // store first exception
    Exception ex = null;

    if (fieldName.contains(".")) {
      throw new EclibRuntimeException("fieldName cannot contain '.'.");
    }

    // fieldName with arrays or Collections not acceptable.
    if (fieldName.contains("[")) {
      throw new EclibRuntimeException(
          "fieldName with index (like value[0]) not acceptable. fieldName: " + fieldName);
    }

    // loop for finding fields in parent's class.
    while (true) {
      if (classOfTargetInstance.equals(Object.class)) {
        break;
      }

      try {
        validationTargetField = classOfTargetInstance.getDeclaredField(fieldName);
        return validationTargetField;

      } catch (Exception exception) {
        if (ex == null) {
          ex = exception;
        }
      }

      classOfTargetInstance = classOfTargetInstance.getSuperclass();
    }

    throwRuntimeException(ex, fieldName, "Field");

    // throwRuntimeException always throws Exception so this will never executed.
    return null;
  }

  private static void throwRuntimeException(Exception ex, String fieldName,
      String whatIsTriedToObtain) {
    throw new EclibRuntimeException(
        whatIsTriedToObtain + " cannot be obtained " + "from the field name '" + fieldName + "'",
        ex);
  }
}
