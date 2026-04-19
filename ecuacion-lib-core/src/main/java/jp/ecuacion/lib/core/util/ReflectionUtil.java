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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
   * Returns class specified by propertyPath.
   */
  public static Class<?> getClass(Class<?> rootBeanClass, String propertyPath) {
    Class<?> tmpClass = rootBeanClass;
    for (String node : PropertyPathUtil.getNodeList(propertyPath)) {
      String nodeWithoutCollectionPart = PropertyPathUtil.removeCollectionPart(node);

      try {
        Field tmpField = getField(tmpClass, nodeWithoutCollectionPart);
        tmpClass = tmpField.getType();

        if (!node.contains("[")) {
          continue;
        }

        // Count the number of "[" in propertyPath.
        int count = node.length() - node.replaceAll("\\[", "").length();

        Type type = tmpField.getGenericType();
        for (int i = 0; i < count; i++) {
          if (type instanceof Class<?> cls && cls.isArray()) {
            // Array: use component type
            type = cls.getComponentType();
          } else {
            // Collection or Map: use type argument
            Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
            // Map value access (no <K> in node): use 2nd type arg; others use 1st
            boolean isMapValueAccess = !node.contains("<K>")
                && Map.class.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType());
            type = typeArgs[isMapValueAccess ? 1 : 0];
          }
        }

        tmpClass = type instanceof Class<?> c ? c
            : Class.forName(Objects.requireNonNull(type).getTypeName());

      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }

    return tmpClass;
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
   * Obtains a field with any scopes and also searches fields in super classes.
   * 
   * @param propertyPath fieldName
   * @param cls classOfTargetInstance
   * @return {@code Pair<Field, Object>} left-hand side is the obtained field, 
   *     right-hand side is its instance.
   *     When you set "dept.name" to fieldName, instance would be "dept".
   */
  public static Field getField(Class<?> cls, String propertyPath) {
    Field validationTargetField;

    // store first exception
    Exception ex = null;

    if (propertyPath.contains(".")) {
      String leftMost = propertyPath.substring(0, propertyPath.indexOf("."));
      @NonNull
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

      cls = Objects.requireNonNull(cls.getSuperclass());
    }

    throw new RuntimeException(ex);
  }

  /**
   * Obtains a field value with any scopes and searches fields in super classes.
   * 
   * <p>Since Class#getDeclaredField is used in the method, 
   *     making its scope public causes a SpotBugs error.<br>
   *     That's why its scope is protected
   *     and you need to extend this class when you use it .</p>
   * 
   * <code>
   * Public method 
   * jp.ecuacion.lib.core.util.internal.PrivateFieldReadUtil.getFieldValue(String, Object, String) 
   * uses reflection to modify a field it gets in its parameter 
   * which could increase the accessibility of any class. 
   * REFLF_REFLECTION_MAY_INCREASE_ACCESSIBILITY_OF_FIELD
   * </code>
   */
  protected static @Nullable Object getValue(Object object, String propertyPath) {
    try {

      while (true) {
        if (propertyPath.contains(".")) {
          @NonNull
          String leftMostOfPropertyPath = propertyPath.substring(0, propertyPath.indexOf("."));
          @NonNull
          String theRestOfPropertyPath = propertyPath.substring(propertyPath.indexOf(".") + 1);

          return getValue(Objects.requireNonNull(getValue(object, leftMostOfPropertyPath)),
              theRestOfPropertyPath);

        } else {
          if (propertyPath.contains("[")) {
            String propertyPathWithoutIndex = propertyPath.substring(0, propertyPath.indexOf("["));
            String tmpSerial = propertyPath.substring(propertyPath.indexOf("[") + 1);
            // It's string because it can be non-number value when the validated object is Map.
            String index = tmpSerial.substring(0, tmpSerial.indexOf("]"));

            // Handle Map key (propertyPath: field<K>[].<map key>)
            if (propertyPathWithoutIndex.contains("<")) {
              propertyPathWithoutIndex =
                  propertyPathWithoutIndex.substring(0, propertyPathWithoutIndex.indexOf("<"));
            }

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
              throw new ElementOfCollectionCannotBeObtainedException(
                  "Multiple value types other than array and List "
                      + "are not supported. The type of value: "
                      + Objects.requireNonNull(objs).getClass().getCanonicalName());
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
        PropertyPathUtil.getPropertyPathWithoutRightMostNode(propertyPath);

    return StringUtils.isEmpty(leafBeanItemPropertyPath) ? rootBean
        : Objects.requireNonNull(ReflectionUtil.getValue(rootBean, leafBeanItemPropertyPath));
  }

  /**
   * Is thrown when getValue method called for non-ordered collections: Sets and Map keys.
   */
  public static class ElementOfCollectionCannotBeObtainedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance.
     */
    public ElementOfCollectionCannotBeObtainedException() {}

    /**
     * Constructs a new instance.
     */
    public ElementOfCollectionCannotBeObtainedException(String message) {
      super(message);
    }
  }
}
