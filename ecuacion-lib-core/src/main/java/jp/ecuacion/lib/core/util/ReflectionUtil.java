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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
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
   * <p>{@code className} must not come from untrusted (e.g. end-user) input: it triggers
   *     loading and static initialization of the named class.</p>
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
   * <p>{@code className} must not come from untrusted (e.g. end-user) input: it triggers
   *     loading, static initialization, and no-argument construction of the named class.</p>
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

    Field field = findDeclaredField(cls, simpleFieldName);
    if (field == null) {
      throw new RuntimeException(new NoSuchFieldException(simpleFieldName));
    }

    return field;
  }

  /**
   * Searches for a bean property by simple name, traversing the class hierarchy.
   *
   * <p>A field named {@code simplePropertyName} is searched first.
   *     When no such field exists, a getter is searched next.
   *     It follows the JavaBeans naming convention, which Jakarta Validation also adopts
   *     for constraints placed at getters: a non-static, no-argument method named
   *     {@code getXxx()} with a non-void return type, or {@code isXxx()} returning
   *     primitive {@code boolean}. So a property without a field of the same name can also be
   *     referred to, and in that case the return value of the getter is the value of
   *     the property.</p>
   *
   * <p>Note that {@code isXxx()} returning {@code Boolean} (the wrapper type) is not treated as
   *     a getter, following the JavaBeans naming convention.
   *     Name it {@code getXxx()} instead.</p>
   *
   * <p>Each search traverses the class hierarchy up to (but excluding) {@code Object}.
   *     Non-public fields and getters are also searched.</p>
   *
   * <p>The argument {@code simplePropertyName} must not contain {@code "."} or {@code "["}
   *     (use {@link PropertyPathUtil#getBeanProperty(Class, String)} for path-based lookup).</p>
   *
   * @param cls starting class
   * @param simplePropertyName property name without path notation
   * @return {@link BeanProperty}
   * @throws RuntimeException with {@link NoSuchFieldException} as its cause
   *     when neither the field nor the getter is found
   */
  public static BeanProperty getBeanProperty(Class<?> cls, String simplePropertyName) {
    if (simplePropertyName.contains("[")) {
      throw new RuntimeException("propertyName with index (like value[0]) not acceptable. "
          + "propertyName: " + simplePropertyName);
    }

    Field field = findDeclaredField(cls, simplePropertyName);
    if (field != null) {
      return new BeanProperty(field);
    }

    Method getter = findGetter(cls, simplePropertyName);
    if (getter != null) {
      return new BeanProperty(getter);
    }

    throw new RuntimeException("Neither a field nor a getter (getXxx(), or isXxx() returning "
        + "primitive boolean; isXxx() returning Boolean is not a getter) found for the property '"
        + simplePropertyName + "' in the class '" + cls.getName() + "' and its superclasses.",
        new NoSuchFieldException(simplePropertyName));
  }

  private static @Nullable Field findDeclaredField(Class<?> cls, String simpleFieldName) {
    for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
      try {
        return c.getDeclaredField(simpleFieldName);
      } catch (NoSuchFieldException ex) {
        // Continue to search the superclass.
      }
    }

    return null;
  }

  private static @Nullable Method findGetter(Class<?> cls, String simplePropertyName) {
    String capitalized = StringUtils.capitalize(simplePropertyName);
    for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
      Method getter = findNoArgInstanceMethod(c, "get" + capitalized);
      if (getter != null && getter.getReturnType() != void.class) {
        return getter;
      }

      Method isGetter = findNoArgInstanceMethod(c, "is" + capitalized);
      if (isGetter != null && isGetter.getReturnType() == boolean.class) {
        return isGetter;
      }
    }

    return null;
  }

  private static @Nullable Method findNoArgInstanceMethod(Class<?> cls, String methodName) {
    try {
      Method method = cls.getDeclaredMethod(methodName);
      return Modifier.isStatic(method.getModifiers()) ? null : method;

    } catch (NoSuchMethodException ex) {
      return null;
    }
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

  /**
   * Provides a bean property, which is backed by either a field or a getter.
   *
   * <p>Obtained by {@link ReflectionUtil#getBeanProperty(Class, String)}.
   *     It hides the difference between a field and a getter
   *     so that the type, annotations and value of a property can be obtained in the same way.</p>
   */
  public static final class BeanProperty {

    /** Either a {@link Field} or a {@link Method} (getter). */
    private final AccessibleObject member;

    private BeanProperty(AccessibleObject member) {
      this.member = member;
    }

    /**
     * Gets the type of the property.
     *
     * @return the type of the field, or the return type of the getter
     */
    public Class<?> getType() {
      return member instanceof Field field ? field.getType()
          : ((Method) member).getReturnType();
    }

    /**
     * Gets the generic type of the property.
     *
     * @return the generic type of the field, or the generic return type of the getter
     */
    public Type getGenericType() {
      return member instanceof Field field ? field.getGenericType()
          : ((Method) member).getGenericReturnType();
    }

    /**
     * Gets the annotation placed at the field or the getter backing the property.
     *
     * @param <A> the type of the annotation
     * @param annotationClass the class of the annotation
     * @return the annotation, {@code null} if not present
     */
    public <A extends Annotation> @Nullable A getAnnotation(Class<A> annotationClass) {
      return member.getAnnotation(annotationClass);
    }

    /**
     * Gets the value of the property from an object, using {@code setAccessible(true)}.
     *
     * <p>When the property is backed by a getter, the getter is invoked.
     *     An exception thrown by the getter is wrapped in a {@code RuntimeException}
     *     whose message tells the getter.</p>
     *
     * @param object the object to read from
     * @return the property value, or {@code null} if the property holds {@code null}
     */
    public @Nullable Object getValue(Object object) {
      if (member instanceof Field field) {
        return getFieldValue(object, field);
      }

      Method getter = (Method) member;
      String getterName = getter.getDeclaringClass().getName() + "#" + getter.getName() + "()";
      try {
        getter.setAccessible(true);
        return getter.invoke(object);

      } catch (InvocationTargetException ex) {
        throw new RuntimeException("The getter '" + getterName + "' threw an exception.",
            ex.getCause());

      } catch (IllegalArgumentException | IllegalAccessException ex) {
        throw new RuntimeException(
            "Property value cannot be obtained from the getter '" + getterName + "'", ex);
      }
    }
  }
}
