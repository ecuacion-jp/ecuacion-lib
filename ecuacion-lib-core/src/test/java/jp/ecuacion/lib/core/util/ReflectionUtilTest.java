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
import jp.ecuacion.lib.core.util.ReflectionUtil.ElementOfCollectionCannotBeObtainedException;
import jp.ecuacion.lib.core.util.ReflectionUtilTest.getFieldTest.SecondExtendedClass;
import jp.ecuacion.lib.core.util.ReflectionUtilTest.getFieldTest.SimpleClass;
import jp.ecuacion.lib.core.util.ReflectionUtilTest.getFieldValueTest.FieldValueRoot;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link ReflectionUtil}. */
@DisplayName("ReflectionUtil")
@SuppressWarnings("EmptyCatch")
public class ReflectionUtilTest {

  @Test
  public void getFieldValueTest() {
    // fieldName without dot
    Object o = ReflectionUtil.getValue(new FieldValueRoot(), "value");
    assertThat(o).isInstanceOf(String.class);
    assertThat((String) o).isEqualTo("root");

    // fieldName with dot
    o = ReflectionUtil.getValue(new FieldValueRoot(), "child.value");
    assertThat(o).isInstanceOf(String.class);
    assertThat((String) o).isEqualTo("child");

    // fieldName with array
    o = ReflectionUtil.getValue(new FieldValueRoot(), "childs[0].value");
    assertThat(o).isInstanceOf(String.class);
    assertThat((String) o).isEqualTo("child");

    // fieldName with List
    o = ReflectionUtil.getValue(new FieldValueRoot(), "childList[0].value");
    assertThat(o).isInstanceOf(String.class);
    assertThat((String) o).isEqualTo("child");

    // fieldName with Set
    try {
      o = ReflectionUtil.getValue(new FieldValueRoot(), "childSet[0].value");
      Assertions.fail();

    } catch (ElementOfCollectionCannotBeObtainedException ex) {

    }

    // non-existent field throws RuntimeException wrapping NoSuchFieldException
    try {
      ReflectionUtil.getValue(new FieldValueRoot(), "nonExistent");
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
  /// Tests getField().
  ///
  //@formatter:on
  @Test
  public void getFieldTest() {
    Field f;

    // fieldName with dot
    try {
      f = ReflectionUtil.getField(SimpleClass.class, "a.b");
      Assertions.fail();
    } catch (RuntimeException ex) {
      assertThat(ex.getCause()).isInstanceOf(NoSuchFieldException.class);
    }

    // fieldName with "["
    try {
      f = ReflectionUtil.getField(SimpleClass.class, "values[]");
      Assertions.fail();
    } catch (RuntimeException ignored) {
      // OK
    }

    // normal fields
    f = ReflectionUtil.getField(SimpleClass.class, "value");
    assertThat(f.getType().getSimpleName()).isEqualTo("String");

    f = ReflectionUtil.getField(SimpleClass.class, "object");
    assertThat(f.getType().getSimpleName()).isEqualTo("ChildClass");

    f = ReflectionUtil.getField(SimpleClass.class, "values");
    assertThat(f.getType().getSimpleName()).isEqualTo("String[]");

    f = ReflectionUtil.getField(SimpleClass.class, "objectList");
    assertThat(f.getType().getSimpleName()).isEqualTo("List");

    // fieldName in superClass
    f = ReflectionUtil.getField(SecondExtendedClass.class, "value");
    assertThat(f.getType().getSimpleName()).isEqualTo("String");
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
    cls = ReflectionUtil.getClass(GetClass.class, "strList[0].<list element>");
    assertThat(String.class.isAssignableFrom(cls)).isTrue();

    // 2.lists with generic type of basic object
    cls =
        ReflectionUtil.getClass(GetClass.class, "strListList[0].<list element>[0].<list element>");
    assertThat(String.class.isAssignableFrom(cls)).isTrue();

    // 3.list with generic type of customized object
    cls = ReflectionUtil.getClass(GetClass.class, "childList[0]");
    assertThat(GetClass.Child.class.isAssignableFrom(cls)).isTrue();

    // 4.lists with generic type of customized object
    cls = ReflectionUtil.getClass(GetClass.class, "childListList[0].<list element>[0]");
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
    Object leaf = ReflectionUtil.getLeafBean(root, "value");
    assertThat(leaf).isSameAs(root);

    // one dot: returns the parent object (child bean)
    leaf = ReflectionUtil.getLeafBean(root, "child.value");
    assertThat(leaf).isInstanceOf(getFieldValueTest.FieldValueChild.class);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface SampleAnnotation {}

  @SampleAnnotation
  public static class AnnotatedClass {}

  public static class AnnotatedSubClass extends AnnotatedClass {}

  public static class UnannotatedClass {}
}
