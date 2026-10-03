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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jp.ecuacion.lib.core.util.PropertyPathUtil.ElementOfCollectionCannotBeObtainedException;
import jp.ecuacion.lib.core.util.ReflectionUtil.BeanProperty;
import jp.ecuacion.lib.core.util.ReflectionUtilTest.getFieldTest.SecondExtendedClass;
import jp.ecuacion.lib.core.util.ReflectionUtilTest.getFieldTest.SimpleClass;
import jp.ecuacion.lib.core.util.ReflectionUtilTest.getFieldValueTest.FieldValueRoot;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link ReflectionUtil} and related {@link PropertyPathUtil} bean-navigation methods. */
@DisplayName("ReflectionUtil")
@SuppressWarnings("EmptyCatch")
public class ReflectionUtilTest {

  @Test
  public void getFieldValueTest() {
    // fieldName without dot
    Object o = PropertyPathUtil.getValue(new FieldValueRoot(), "value");
    assertThat(o).isInstanceOf(String.class);
    assertThat((String) o).isEqualTo("root");

    // fieldName with dot
    o = PropertyPathUtil.getValue(new FieldValueRoot(), "child.value");
    assertThat(o).isInstanceOf(String.class);
    assertThat((String) o).isEqualTo("child");

    // fieldName with array
    o = PropertyPathUtil.getValue(new FieldValueRoot(), "childs[0].value");
    assertThat(o).isInstanceOf(String.class);
    assertThat((String) o).isEqualTo("child");

    // fieldName with List
    o = PropertyPathUtil.getValue(new FieldValueRoot(), "childList[0].value");
    assertThat(o).isInstanceOf(String.class);
    assertThat((String) o).isEqualTo("child");

    // fieldName with Set
    try {
      o = PropertyPathUtil.getValue(new FieldValueRoot(), "childSet[0].value");
      Assertions.fail();

    } catch (ElementOfCollectionCannotBeObtainedException ex) {

    }

    // non-existent field throws RuntimeException wrapping NoSuchFieldException
    try {
      PropertyPathUtil.getValue(new FieldValueRoot(), "nonExistent");
      Assertions.fail();
    } catch (RuntimeException ex) {
      assertThat(ex.getCause()).isInstanceOf(NoSuchFieldException.class);
    }
  }

  @SuppressWarnings("unused")
  public static class getFieldValueTest {
    public static class FieldValueRoot {
      private String value = "root";

      private FieldValueChild child = new FieldValueChild();
      private FieldValueChild[] childs = new FieldValueChild[] {new FieldValueChild()};
      private List<FieldValueChild> childList =
          Arrays.asList(new FieldValueChild[] {new FieldValueChild()});
      private Set<FieldValueChild> childSet = new HashSet<>(childList);
    }

    public static class FieldValueChild {
      private String value = "child";
    }
  }

  //@formatter:off
  ///
  /// Tests getDeclaredField() (simple field lookup) and PropertyPathUtil.getField() (path lookup).
  ///
  //@formatter:on
  @Test
  public void getFieldTest() {
    Field f;

    // getDeclaredField: fieldName with "["
    try {
      f = ReflectionUtil.getDeclaredField(SimpleClass.class, "values[]");
      Assertions.fail();
    } catch (RuntimeException ignored) {
      // OK
    }

    // getDeclaredField: normal fields
    f = ReflectionUtil.getDeclaredField(SimpleClass.class, "value");
    assertThat(f.getType().getSimpleName()).isEqualTo("String");

    f = ReflectionUtil.getDeclaredField(SimpleClass.class, "object");
    assertThat(f.getType().getSimpleName()).isEqualTo("ChildClass");

    f = ReflectionUtil.getDeclaredField(SimpleClass.class, "values");
    assertThat(f.getType().getSimpleName()).isEqualTo("String[]");

    f = ReflectionUtil.getDeclaredField(SimpleClass.class, "objectList");
    assertThat(f.getType().getSimpleName()).isEqualTo("List");

    // getDeclaredField: fieldName in superClass
    f = ReflectionUtil.getDeclaredField(SecondExtendedClass.class, "value");
    assertThat(f.getType().getSimpleName()).isEqualTo("String");

    // PropertyPathUtil.getField: fieldName with dot — non-existent throws RuntimeException
    try {
      f = PropertyPathUtil.getField(SimpleClass.class, "a.b");
      Assertions.fail();
    } catch (RuntimeException ex) {
      assertThat(ex.getCause()).isInstanceOf(NoSuchFieldException.class);
    }
  }

  public static class getFieldTest {

    @SuppressWarnings("unused")
    public static class SimpleClass {
      private @Nullable String value;
      private @Nullable ChildClass object;
      private String @Nullable [] values;
      private @Nullable List<ChildClass> objectList;

      public static class ChildClass {
      }
    }

    public static class ExtendedClass extends SimpleClass {

    }

    public static class SecondExtendedClass extends ExtendedClass {

    }
  }

  @Test
  public void getClassTest() {
    Class<?> cls;

    // 1.list with generic type of basic object
    cls = PropertyPathUtil.getClass(GetClass.class, "strList[0].<list element>");
    assertThat(String.class.isAssignableFrom(cls)).isTrue();

    // 2.lists with generic type of basic object
    cls = PropertyPathUtil.getClass(
        GetClass.class, "strListList[0].<list element>[0].<list element>");
    assertThat(String.class.isAssignableFrom(cls)).isTrue();

    // 3.list with generic type of customized object
    cls = PropertyPathUtil.getClass(GetClass.class, "childList[0]");
    assertThat(GetClass.Child.class.isAssignableFrom(cls)).isTrue();

    // 4.lists with generic type of customized object
    cls = PropertyPathUtil.getClass(GetClass.class, "childListList[0].<list element>[0]");
    assertThat(GetClass.Child.class.isAssignableFrom(cls)).isTrue();
  }

  @SuppressWarnings("unused")
  public static class GetClass {
    private @Nullable List<String> strList;
    private @Nullable List<List<String>> strListList;

    private @Nullable List<Child> childList;
    private @Nullable List<List<Child>> childListList;

    private static class Child {

    }
  }

  @Test
  public void classExistsTest() {
    assertThat(ReflectionUtil.classExists("java.lang.String")).isTrue();
    assertThat(ReflectionUtil.classExists("no.such.Class")).isFalse();
  }

  @Test
  public void newInstanceTest() {
    Object obj = ReflectionUtil.newInstance("java.util.ArrayList");
    assertThat(obj).isInstanceOf(java.util.ArrayList.class);
  }

  @Test
  @DisplayName("newInstance throws RuntimeException for non-existent class")
  public void newInstance_nonExistentClass_throws() {
    try {
      ReflectionUtil.newInstance("no.such.Class");
      Assertions.fail();
    } catch (RuntimeException ex) {
      assertThat(ex.getCause()).isInstanceOf(ClassNotFoundException.class);
    }
  }

  @Test
  public void searchAnnotationPlacedAtClassTest() {
    // annotation on the class itself
    Optional<@NonNull SampleAnnotation> found =
        ReflectionUtil.searchAnnotationPlacedAtClass(AnnotatedClass.class, SampleAnnotation.class);
    assertThat(found).isPresent();

    // annotation inherited from superclass
    found = ReflectionUtil.searchAnnotationPlacedAtClass(
        AnnotatedSubClass.class, SampleAnnotation.class);
    assertThat(found).isPresent();

    // annotation not present anywhere in hierarchy
    found = ReflectionUtil.searchAnnotationPlacedAtClass(
        UnannotatedClass.class, SampleAnnotation.class);
    assertThat(found).isEmpty();
  }

  @Test
  public void getLeafBeanTest() {
    FieldValueRoot root = new FieldValueRoot();

    // no dot: returns root itself
    Object leaf = PropertyPathUtil.getLeafBean(root, "value");
    assertThat(leaf).isSameAs(root);

    // one dot: returns the parent object (child bean)
    leaf = PropertyPathUtil.getLeafBean(root, "child.value");
    assertThat(leaf).isInstanceOf(getFieldValueTest.FieldValueChild.class);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface SampleAnnotation {}

  @SampleAnnotation
  public static class AnnotatedClass {}

  public static class AnnotatedSubClass extends AnnotatedClass {}

  public static class UnannotatedClass {}

  @Nested
  @DisplayName("getBeanProperty: field first, getter when no field exists")
  class GetBeanProperty {

    @Test
    @DisplayName("field is used when it exists")
    void field() {
      BeanProperty p = ReflectionUtil.getBeanProperty(PropertyBean.class, "fieldOnly");
      assertThat(p.getType()).isEqualTo(String.class);
      assertThat(p.getValue(new PropertyBean())).isEqualTo("field");
    }

    @Test
    @DisplayName("field takes priority over getter of the same property")
    void fieldTakesPriorityOverGetter() {
      assertThat(ReflectionUtil.getBeanProperty(PropertyBean.class, "both")
          .getValue(new PropertyBean())).isEqualTo("fieldValue");
    }

    @Test
    @DisplayName("getXxx() is used when no field exists")
    void getter() {
      BeanProperty p = ReflectionUtil.getBeanProperty(PropertyBean.class, "getterOnly");
      assertThat(p.getType()).isEqualTo(String.class);
      assertThat(p.getValue(new PropertyBean())).isEqualTo("getter");
    }

    @Test
    @DisplayName("isXxx() is used when it returns boolean")
    void isGetter() {
      BeanProperty p = ReflectionUtil.getBeanProperty(PropertyBean.class, "active");
      assertThat(p.getType()).isEqualTo(boolean.class);
      assertThat(p.getValue(new PropertyBean())).isEqualTo(true);
    }

    @Test
    @DisplayName("private getter and getter in superclass are found")
    void privateAndInheritedGetter() {
      assertThat(ReflectionUtil.getBeanProperty(PropertyBean.class, "privateGetter")
          .getValue(new PropertyBean())).isEqualTo("private");
      assertThat(ReflectionUtil.getBeanProperty(PropertySubBean.class, "getterOnly")
          .getValue(new PropertySubBean())).isEqualTo("getter");
    }

    @Test
    @DisplayName("generic return type and annotation of getter are obtained")
    void genericTypeAndAnnotation() {
      BeanProperty p = ReflectionUtil.getBeanProperty(PropertyBean.class, "strList");
      assertThat(p.getGenericType().getTypeName()).isEqualTo("java.util.List<java.lang.String>");
      assertThat(p.getAnnotation(SampleMethodAnnotation.class)).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrapperBoolean", "voidReturn", "staticValue", "withArg", "none"})
    @DisplayName("not found: isXxx() not returning boolean, void, static, with argument, none")
    void notFound(String propertyName) {
      assertThatThrownBy(() -> ReflectionUtil.getBeanProperty(PropertyBean.class, propertyName))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Neither a field nor a getter")
          .hasMessageContaining("'" + propertyName + "'")
          .cause().isInstanceOf(NoSuchFieldException.class).hasMessage(propertyName);
    }

    @Test
    @DisplayName("propertyName with index is not acceptable")
    void propertyNameWithIndex() {
      assertThatThrownBy(() -> ReflectionUtil.getBeanProperty(PropertyBean.class, "values[0]"))
          .isInstanceOf(RuntimeException.class).hasMessageContaining("not acceptable");
    }

    @Test
    @DisplayName("not found: interface (no superclass to traverse)")
    void notFoundInInterface() {
      assertThatThrownBy(() -> ReflectionUtil.getBeanProperty(Runnable.class, "value"))
          .isInstanceOf(RuntimeException.class)
          .cause().isInstanceOf(NoSuchFieldException.class);
    }

    @Test
    @DisplayName("getter invoked on an object of another class throws RuntimeException")
    void getterInvokedOnWrongObject() {
      BeanProperty p = ReflectionUtil.getBeanProperty(PropertyBean.class, "getterOnly");
      assertThatThrownBy(() -> p.getValue(new Object()))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining(PropertyBean.class.getName() + "#getGetterOnly()")
          .cause().isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("exception thrown by getter is wrapped with the getter name in the message")
    void getterThrows() {
      BeanProperty p = ReflectionUtil.getBeanProperty(PropertyBean.class, "throwing");
      assertThatThrownBy(() -> p.getValue(new PropertyBean()))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining(PropertyBean.class.getName() + "#getThrowing()")
          .cause().isInstanceOf(IllegalStateException.class).hasMessage("thrown by getter");
    }

    @Test
    @DisplayName("PropertyPathUtil.getValue resolves getters at every node")
    void getValueThroughGetters() {
      assertThat(PropertyPathUtil.getValue(new PropertyBean(), "child.value")).isEqualTo("child");
      assertThat(PropertyPathUtil.getValue(new PropertyBean(), "childList[0].value"))
          .isEqualTo("child");
      assertThat(PropertyPathUtil.getValue(new PropertyBean(), "childArray[0].value"))
          .isEqualTo("child");
    }

    @Test
    @DisplayName("PropertyPathUtil.getClass and getBeanProperty resolve getters at every node")
    void getClassThroughGetters() {
      assertThat(PropertyPathUtil.getClass(PropertyBean.class, "childList[0]"))
          .isEqualTo(GetterChild.class);
      assertThat(PropertyPathUtil.getBeanProperty(PropertyBean.class, "child.value").getType())
          .isEqualTo(String.class);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface SampleMethodAnnotation {}

  @SuppressWarnings("unused")
  public static class PropertyBean {
    private String fieldOnly = "field";
    private String both = "fieldValue";

    public String getBoth() {
      return "getterValue";
    }

    public String getGetterOnly() {
      return "getter";
    }

    public boolean isActive() {
      return true;
    }

    private String getPrivateGetter() {
      return "private";
    }

    @SampleMethodAnnotation
    public List<String> getStrList() {
      return List.of("a");
    }

    public Boolean isWrapperBoolean() {
      return true;
    }

    public void getVoidReturn() {}

    public static String getStaticValue() {
      return "static";
    }

    public String getWithArg(String arg) {
      return arg;
    }

    public String getThrowing() {
      throw new IllegalStateException("thrown by getter");
    }

    public GetterChild getChild() {
      return new GetterChild();
    }

    public List<GetterChild> getChildList() {
      return List.of(new GetterChild());
    }

    public GetterChild[] getChildArray() {
      return new GetterChild[] {new GetterChild()};
    }
  }

  public static class PropertySubBean extends PropertyBean {}

  public static class GetterChild {
    public String getValue() {
      return "child";
    }
  }
}
