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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.util.internal.ReflectionUtilTest.getFieldTest.SecondExtendedClass;
import jp.ecuacion.lib.core.util.internal.ReflectionUtilTest.getFieldTest.SimpleClass;
import jp.ecuacion.lib.core.util.internal.ReflectionUtilTest.getFieldValueTest.FieldValueRoot;
import org.assertj.core.util.Arrays;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReflectionUtilTest {

  @Test
  public void getFieldValueTest() {
    // fieldName without dot
    Object o = ReflectionUtil.getFieldValue("value", new FieldValueRoot());
    Assertions.assertTrue(o instanceof String);
    Assertions.assertEquals("root", (String) o);

    // fieldName with dot
    o = ReflectionUtil.getFieldValue("child.value", new FieldValueRoot());
    Assertions.assertTrue(o instanceof String);
    Assertions.assertEquals("child", (String) o);

    // fieldName with array
    o = ReflectionUtil.getFieldValue("childs[0].value", new FieldValueRoot());
    Assertions.assertTrue(o instanceof String);
    Assertions.assertEquals("child", (String) o);

    // fieldName with List
    o = ReflectionUtil.getFieldValue("childList[0].value", new FieldValueRoot());
    Assertions.assertTrue(o instanceof String);
    Assertions.assertEquals("child", (String) o);

    // fieldName with Set
    try {
      o = ReflectionUtil.getFieldValue("childSet[0].value", new FieldValueRoot());
      Assertions.fail();

    } catch (EclibRuntimeException ex) {
      
    }
  }

  @SuppressWarnings("unused")
  public static class getFieldValueTest {
    public static class FieldValueRoot {
      private String value = "root";

      private FieldValueChild child = new FieldValueChild();
      private FieldValueChild[] childs = new FieldValueChild[] {new FieldValueChild()};
      private List<Object> childList = Arrays.asList(new FieldValueChild[] {new FieldValueChild()});
      private Set<Object> childSet = Sets.newHashSet(childList) ;
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
      f = ReflectionUtil.getField("a.b", SimpleClass.class);
      Assertions.fail();

    } catch (EclibRuntimeException ex) {
      // OK
    }

    // fieldName with "["
    try {
      f = ReflectionUtil.getField("values[]", SimpleClass.class);
      Assertions.fail();

    } catch (EclibRuntimeException ex) {
      // OK
    }

    // normal fields
    f = ReflectionUtil.getField("value", SimpleClass.class);
    Assertions.assertEquals("String", f.getType().getSimpleName());

    f = ReflectionUtil.getField("object", SimpleClass.class);
    Assertions.assertEquals("ChildClass", f.getType().getSimpleName());

    f = ReflectionUtil.getField("values", SimpleClass.class);
    Assertions.assertEquals("String[]", f.getType().getSimpleName());

    f = ReflectionUtil.getField("objectList", SimpleClass.class);
    Assertions.assertEquals("List", f.getType().getSimpleName());

    // fieldName in superClass
    f = ReflectionUtil.getField("value", SecondExtendedClass.class);
    Assertions.assertEquals("String", f.getType().getSimpleName());
  }

  public static class getFieldTest {

    @SuppressWarnings("unused")
    public static class SimpleClass {
      private String value;
      private ChildClass object;
      private String[] values;
      private List<ChildClass> objectList;

      public static class ChildClass {
      }
    }

    public static class ExtendedClass extends SimpleClass {

    }

    public static class SecondExtendedClass extends ExtendedClass {

    }
  }
}
