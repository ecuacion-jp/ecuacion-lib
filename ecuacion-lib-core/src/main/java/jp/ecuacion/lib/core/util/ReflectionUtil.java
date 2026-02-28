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
   * Returns new instance constructed with an constructor with no arguments.
   * 
   * @param className className with package (like "java.lang.Object")
   * @return new instance
   */
  public static Object newInstance(String className) {
    try {
      Class<?> cls = Class.forName(className);
      return cls.getConstructor().newInstance();

    } catch (Exception ex) {
      throw new EclibRuntimeException(ex);
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
  protected static Object getValue(Object object, String propertyPath) {
    try {

      while (true) {
        if (propertyPath.contains(".")) {
          String leftMostOfPropertyPath = propertyPath.substring(0, propertyPath.indexOf("."));
          String theRestOfPropertyPath = propertyPath.substring(propertyPath.indexOf(".") + 1);

          return getValue(getValue(object, leftMostOfPropertyPath), theRestOfPropertyPath);

        } else {
          if (propertyPath.contains("[")) {
            String propertyPathWithoutIndex = propertyPath.substring(0, propertyPath.indexOf("["));
            String tmpSerial = propertyPath.substring(propertyPath.indexOf("[") + 1);
            // It's string because it can be non-number value when the validated object is Map.
            String index = tmpSerial.substring(0, tmpSerial.indexOf("]"));
            Field rootField = getField(object.getClass(), propertyPathWithoutIndex);
            rootField.setAccessible(true);
            Object objs = rootField.get(object);

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
            Field rootField = getField(object.getClass(), propertyPath);
            rootField.setAccessible(true);
            return rootField.get(object);
          }
        }
      }

    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new EclibRuntimeException(
          "Field value cannot be obtained " + "from the field name '" + propertyPath + "'", ex);
    }
  }

  /**
   * Returns leafBean from rootBean and propertyPath from rootBean.
   */
  public static Object getLeafBean(Object rootBean, String propertyPath) {
    String leafBeanItemPropertyPath =
        propertyPath.contains(".") ? propertyPath.substring(0, propertyPath.lastIndexOf("."))
            : null;

    return leafBeanItemPropertyPath == null ? rootBean
        : ReflectionUtil.getValue(rootBean, leafBeanItemPropertyPath);
  }

  /**
   * Obtains a field with any scopes and also searches fields in super classes.
   * 
   * @param propertyPath fieldName
   * @param cls classOfTargetInstance
   * @return {@code Pair<Field, Object>} left-hand side is the obtained field, 
   *     right-hand side is its instance.
   *     When you set "dept.name" to fieldName, instance would be "dept".
   */
  @Nonnull
  public static Field getField(Class<?> cls, String propertyPath) {
    Field validationTargetField;

    // store first exception
    Exception ex = null;

    if (propertyPath.contains(".")) {
      String leftMost = propertyPath.substring(0, propertyPath.indexOf("."));
      String theLeft = propertyPath.substring(leftMost.length() + 1);

      return getField(getField(cls, leftMost).getType(), theLeft);
    }

    // fieldName with arrays or Collections not acceptable.
    if (propertyPath.contains("[")) {
      throw new EclibRuntimeException(
          "fieldName with index (like value[0]) not acceptable. fieldName: " + propertyPath);
    }

    // loop for finding fields in parent's class.
    while (true) {
      if (cls.equals(Object.class)) {
        break;
      }

      try {
        validationTargetField = cls.getDeclaredField(propertyPath);
        return validationTargetField;

      } catch (Exception exception) {
        if (ex == null) {
          ex = exception;
        }
      }

      cls = cls.getSuperclass();
    }

    throw new RuntimeException(ex);
  }
}
