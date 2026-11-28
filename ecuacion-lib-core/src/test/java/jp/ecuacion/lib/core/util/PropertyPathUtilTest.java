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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link PropertyPathUtil#getRightMostNode}. */
@DisplayName("PropertyPathUtil - getRightMostNode")
public class PropertyPathUtilTest {

  private String getRightMostNode(String propertyPath) {
    return PropertyPathUtil.getRightMostNode(propertyPath);
  }

  // short aliases for collection element suffixes
  private static final String L = PropertyPathUtil.EL_LIST;
  private static final String S = PropertyPathUtil.EL_SET;
  private static final String MK = PropertyPathUtil.EL_MAP_KEY;
  private static final String MV = PropertyPathUtil.EL_MAP_VAL;

  @Test
  public void getRightMostNodeTest_normalPattern() {
    // no "."
    assertThat(getRightMostNode("field")).isEqualTo("field");

    // with "."
    assertThat(getRightMostNode("bean.field")).isEqualTo("field");
    assertThat(getRightMostNode("bean1.bean2.field")).isEqualTo("field");
  }

  @Test
  public void getRightMostNodeTest_array() {
    // array: index notation only, no collection-element marker
    assertThat(getRightMostNode("field[0]")).isEqualTo("field[0]");
    assertThat(getRightMostNode("bean.field[0]")).isEqualTo("field[0]");
  }

  @Test
  public void getRightMostNodeTest_list() {
    // List: strList[0].<list element>
    assertThat(getRightMostNode("strList[0]." + L)).isEqualTo("strList[0]." + L);
    assertThat(getRightMostNode("bean.strList[0]." + L)).isEqualTo("strList[0]." + L);
  }

  @Test
  public void getRightMostNodeTest_set() {
    // Set: intSet[].<iterable element>
    assertThat(getRightMostNode("intSet[]." + S)).isEqualTo("intSet[]." + S);
    assertThat(getRightMostNode("bean.intSet[]." + S)).isEqualTo("intSet[]." + S);
  }

  @Test
  public void getRightMostNodeTest_mapKey() {
    // Map key: strMap<K>[].<map key>
    assertThat(getRightMostNode("strMap<K>[]." + MK)).isEqualTo("strMap<K>[]." + MK);
    assertThat(getRightMostNode("bean.strMap<K>[]." + MK)).isEqualTo("strMap<K>[]." + MK);
  }

  @Test
  public void getRightMostNodeTest_mapValue() {
    // Map value: strMap[key1].<map value>
    assertThat(getRightMostNode("strMap[key1]." + MV)).isEqualTo("strMap[key1]." + MV);
    assertThat(getRightMostNode("bean.strMap[key1]." + MV)).isEqualTo("strMap[key1]." + MV);
  }

  @Test
  public void getRightMostNodeTest_pairRight() {
    // Pair.right is exposed as map value node type (no key qualifier)
    assertThat(getRightMostNode("pairField." + MV)).isEqualTo("pairField." + MV);
    assertThat(getRightMostNode("bean.pairField." + MV)).isEqualTo("pairField." + MV);
  }

  @Test
  public void getRightMostNodeTest_nestedList() {
    // List<List<String>>: strListList[0].<list element>[0].<list element>
    String path = "strListList[0]." + L + "[0]." + L;
    assertThat(getRightMostNode(path)).isEqualTo(path);
    assertThat(getRightMostNode("bean." + path)).isEqualTo(path);
  }

  @Test
  public void getRightMostNodeTest_nestedSet() {
    // Set<Set<Integer>>: intSetSet[].<iterable element>[].<iterable element>
    String path = "intSetSet[]." + S + "[]." + S;
    assertThat(getRightMostNode(path)).isEqualTo(path);
    assertThat(getRightMostNode("bean." + path)).isEqualTo(path);
  }

  @Test
  public void getRightMostNodeTest_nestedMapValue() {
    // Map<V, Map<V>>: mapMap[outer].<map value>[inner].<map value>
    String path = "mapMap[outer]." + MV + "[inner]." + MV;
    assertThat(getRightMostNode(path)).isEqualTo(path);
    assertThat(getRightMostNode("bean." + path)).isEqualTo(path);
  }

  @Test
  public void getRightMostNodeTest_nestedMix() {
    // List<Set<Map<String, Integer>>>:
    // listSetMap[0].<list element>[].<iterable element>[key].<map value>
    String path = "listSetMap[0]." + L + "[]." + S + "[key]." + MV;
    assertThat(getRightMostNode(path)).isEqualTo(path);
    assertThat(getRightMostNode("bean." + path)).isEqualTo(path);
  }

  // -------------------------------------------------------------------------
  // getPropertyPathWithoutRightMostNode
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getPropertyPathWithoutRightMostNode")
  class GetPropertyPathWithoutRightMostNode {

    @Test
    @DisplayName("no dot returns empty string")
    void noDot() {
      assertThat(PropertyPathUtil.getPropertyPathWithoutRightMostNode("field")).isEqualTo("");
    }

    @Test
    @DisplayName("one dot returns the parent segment")
    void oneDot() {
      assertThat(PropertyPathUtil.getPropertyPathWithoutRightMostNode("bean.field"))
          .isEqualTo("bean");
    }

    @Test
    @DisplayName("two dots returns everything except rightmost node")
    void twoDots() {
      assertThat(PropertyPathUtil.getPropertyPathWithoutRightMostNode("bean1.bean2.field"))
          .isEqualTo("bean1.bean2");
    }

    @Test
    @DisplayName("collection node is the rightmost - parent bean returned")
    void collectionNode() {
      assertThat(PropertyPathUtil
          .getPropertyPathWithoutRightMostNode("bean.strList[0]." + L)).isEqualTo("bean");
      assertThat(PropertyPathUtil
          .getPropertyPathWithoutRightMostNode("strList[0]." + L)).isEqualTo("");
    }
  }

  // -------------------------------------------------------------------------
  // getNodeList
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getNodeList")
  class GetNodeList {

    @Test
    @DisplayName("empty string returns empty list")
    void emptyString() {
      assertThat(PropertyPathUtil.getNodeList("")).isEmpty();
    }

    @Test
    @DisplayName("single field returns one-element list")
    void singleField() {
      assertThat(PropertyPathUtil.getNodeList("field")).containsExactly("field");
    }

    @Test
    @DisplayName("dot-separated path returns nodes in order")
    void dotSeparated() {
      assertThat(PropertyPathUtil.getNodeList("bean.field")).containsExactly("bean", "field");
      assertThat(PropertyPathUtil.getNodeList("bean1.bean2.field"))
          .containsExactly("bean1", "bean2", "field");
    }

    @Test
    @DisplayName("collection node is treated as a single node")
    void collectionNode() {
      List<@NonNull String> nodes = PropertyPathUtil.getNodeList("strList[0]." + L);
      assertThat(nodes).containsExactly("strList[0]." + L);

      nodes = PropertyPathUtil.getNodeList("bean.strList[0]." + L);
      assertThat(nodes).containsExactly("bean", "strList[0]." + L);
    }
  }

  // -------------------------------------------------------------------------
  // toFieldPath
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("toFieldPath")
  class RemoveCollectionPart {

    @Test
    @DisplayName("plain field is unchanged")
    void plainField() {
      assertThat(PropertyPathUtil.toFieldPath("field")).isEqualTo("field");
      assertThat(PropertyPathUtil.toFieldPath("bean.field")).isEqualTo("bean.field");
    }

    @Test
    @DisplayName("list node strips index and element marker")
    void listNode() {
      assertThat(PropertyPathUtil.toFieldPath("strList[0]." + L)).isEqualTo("strList");
      assertThat(PropertyPathUtil.toFieldPath("bean.strList[0]." + L))
          .isEqualTo("bean.strList");
    }

    @Test
    @DisplayName("bean list node strips only the index")
    void beanListNode() {
      assertThat(PropertyPathUtil.toFieldPath("userList[1].name"))
          .isEqualTo("userList.name");
    }

    @Test
    @DisplayName("map key node strips the key qualifier")
    void mapKeyNode() {
      assertThat(PropertyPathUtil.toFieldPath("strMap<K>[]." + MK)).isEqualTo("strMap");
    }
  }

  // -------------------------------------------------------------------------
  // toIndexlessPath
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("toIndexlessPath")
  class RemoveIndex {

    @Test
    @DisplayName("plain field is unchanged")
    void plainField() {
      assertThat(PropertyPathUtil.toIndexlessPath("field")).isEqualTo("field");
    }

    @Test
    @DisplayName("list: index removed and element marker stripped")
    void list() {
      assertThat(PropertyPathUtil.toIndexlessPath("stringList[1]." + L)).isEqualTo("stringList[]");
      assertThat(PropertyPathUtil.toIndexlessPath("stringList[1]." + L + "[2]." + L))
          .isEqualTo("stringList[][]");
      assertThat(PropertyPathUtil.toIndexlessPath("userList[1].name")).isEqualTo("userList[].name");
    }

    @Test
    @DisplayName("set: element marker stripped")
    void set() {
      assertThat(PropertyPathUtil.toIndexlessPath("stringSet[]." + S)).isEqualTo("stringSet[]");
    }

    @Test
    @DisplayName("map value: index removed and element marker stripped")
    void mapValue() {
      assertThat(PropertyPathUtil.toIndexlessPath("strMap[key1]." + MV)).isEqualTo("strMap[]");
    }

    @Test
    @DisplayName("map key: element marker stripped")
    void mapKey() {
      assertThat(PropertyPathUtil.toIndexlessPath("strMap<K>[]." + MK)).isEqualTo("strMap<K>[]");
    }

    @Test
    @DisplayName("already normalized shorthand path is unchanged (idempotent)")
    void idempotent() {
      assertThat(PropertyPathUtil.toIndexlessPath("nestedList[][]")).isEqualTo("nestedList[][]");
      assertThat(PropertyPathUtil.toIndexlessPath("nestedMap<K>[]<K>[].name"))
          .isEqualTo("nestedMap<K>[]<K>[].name");
      assertThat(PropertyPathUtil.toIndexlessPath("list[]<K>[]")).isEqualTo("list[]<K>[]");
    }
  }

  // -------------------------------------------------------------------------
  // map keys whose toString() contains characters of the propertyPath syntax
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("map key containing '.', '[', ']' or '<' in its toString()")
  class MapKeyWithSyntaxCharacters {

    @Test
    @DisplayName("maskBracketContents replaces only the contents of [...] with '_'")
    void maskBracketContents() {
      assertThat(PropertyPathUtil.maskBracketContents("map[1.5]." + MV))
          .isEqualTo("map[___]." + MV);
      // balanced brackets in the contents (toString() of a List key)
      assertThat(PropertyPathUtil.maskBracketContents("map<K>[[a, b]]." + MK))
          .isEqualTo("map<K>[______]." + MK);
      // "]" not followed by ".", "[", "<" or the end is treated as the contents
      assertThat(PropertyPathUtil.maskBracketContents("map[a]b]." + MV))
          .isEqualTo("map[___]." + MV);
      // unbalanced "[" in the contents
      assertThat(PropertyPathUtil.maskBracketContents("map[a[b]." + MV))
          .isEqualTo("map[___]." + MV);
      // "<" in the contents
      assertThat(PropertyPathUtil.maskBracketContents("map[a<K>b]." + MV))
          .isEqualTo("map[_____]." + MV);
      // empty brackets and paths without brackets are unchanged
      assertThat(PropertyPathUtil.maskBracketContents("list[][].name"))
          .isEqualTo("list[][].name");
      assertThat(PropertyPathUtil.maskBracketContents("bean.field")).isEqualTo("bean.field");
    }

    @Test
    @DisplayName("toIndexlessPath removes the whole key")
    void toIndexlessPath() {
      assertThat(PropertyPathUtil.toIndexlessPath("map[1.5]." + MV)).isEqualTo("map[]");
      assertThat(PropertyPathUtil.toIndexlessPath("map[a]b]." + MV)).isEqualTo("map[]");
      assertThat(PropertyPathUtil.toIndexlessPath("map[a[b]." + MV)).isEqualTo("map[]");
      // Map<List<String>, ?>: key is a List
      assertThat(PropertyPathUtil.toIndexlessPath("map<K>[[x, null]]." + MK + "[1]." + L))
          .isEqualTo("map<K>[][]");
      // key is a bean with the default toString() (package name contains ".")
      assertThat(PropertyPathUtil.toIndexlessPath("map<K>[jp.example.User@1a2b].name"))
          .isEqualTo("map<K>[].name");
    }

    @Test
    @DisplayName("getRightMostNode and getNodeList do not split at '.' in a key")
    void nodes() {
      assertThat(getRightMostNode("bean.map[1.5]." + MV)).isEqualTo("map[1.5]." + MV);
      assertThat(getRightMostNode("bean.map<K>[jp.example.User@1a2b].name")).isEqualTo("name");
      assertThat(PropertyPathUtil.getNodeList("bean.map<K>[jp.example.User@1a2b].name"))
          .containsExactly("bean", "map<K>[jp.example.User@1a2b]", "name");
      assertThat(PropertyPathUtil.getPropertyPathWithoutRightMostNode("map[1.5].name"))
          .isEqualTo("map[1.5]");
    }

    @Test
    @DisplayName("toFieldPath removes the whole key")
    void toFieldPath() {
      assertThat(PropertyPathUtil.toFieldPath("bean.map[1.5]." + MV)).isEqualTo("bean.map");
      assertThat(PropertyPathUtil.toFieldPath("map<K>[jp.example.User@1a2b].name"))
          .isEqualTo("map.name");
    }
  }

  @Nested
  @DisplayName("getValue with a null intermediate value")
  @SuppressWarnings("unused") // fields are read via reflection
  class GetValueWithNullIntermediateValue {

    public static class Dept {
      private String name = "dept1";
    }

    public static class Emp {
      private @Nullable Dept dept;
      private @Nullable List<Dept> deptList;
      private @Nullable Dept @Nullable [] deptArray;
    }

    @Test
    @DisplayName("intermediate bean is null: returns null")
    void intermediateBeanIsNull() {
      assertThat(PropertyPathUtil.getValue(new Emp(), "dept.name")).isNull();
    }

    @Test
    @DisplayName("List or array is null: returns null")
    void collectionIsNull() {
      assertThat(PropertyPathUtil.getValue(new Emp(), "deptList[0]")).isNull();
      assertThat(PropertyPathUtil.getValue(new Emp(), "deptList[0].name")).isNull();
      assertThat(PropertyPathUtil.getValue(new Emp(), "deptArray[0].name")).isNull();
    }

    @Test
    @DisplayName("element of List is null: returns null")
    void elementIsNull() {
      Emp emp = new Emp();
      List<Dept> list = new java.util.ArrayList<>();
      list.add(null);
      emp.deptList = list;
      assertThat(PropertyPathUtil.getValue(emp, "deptList[0].name")).isNull();
    }

    @Test
    @DisplayName("no null in the path: returns the value")
    void noNull() {
      Emp emp = new Emp();
      emp.dept = new Dept();
      emp.deptList = List.of(new Dept());
      assertThat(PropertyPathUtil.getValue(emp, "dept.name")).isEqualTo("dept1");
      assertThat(PropertyPathUtil.getValue(emp, "deptList[0].name")).isEqualTo("dept1");
    }
  }

  @Nested
  @DisplayName("getValue with the notation of collections")
  @SuppressWarnings("unused") // fields are read via reflection
  class GetValueWithCollections {

    public static class Dept {
      private String name;

      Dept(String name) {
        this.name = name;
      }
    }

    public static class Holder {
      private List<String> strList = List.of("a", "b");
      private List<List<String>> nestedList = List.of(List.of("x0", "x1"), List.of("y0"));
      private int[] intArray = {10, 20};
      private List<Dept> deptList = List.of(new Dept("d0"));
      private Map<String, String> strMap = Map.of("k1", "v1");
      private Map<Integer, Dept> intKeyMap = Map.of(5, new Dept("d5"));
      private Map<String, List<String>> listMap = Map.of("k", List.of("e0", "e1"));
      private Set<String> strSet = Set.of("s");
      private Map<Object, String> ambiguousMap = new java.util.HashMap<>();
    }

    private final Holder holder = new Holder();

    @Test
    @DisplayName("List: <list element> and nested indices")
    void list() {
      assertThat(PropertyPathUtil.getValue(holder, "strList[1]")).isEqualTo("b");
      assertThat(PropertyPathUtil.getValue(holder, "strList[1].<list element>")).isEqualTo("b");
      assertThat(PropertyPathUtil.getValue(holder, "nestedList[0][1]")).isEqualTo("x1");
      assertThat(PropertyPathUtil.getValue(holder,
          "nestedList[0].<list element>[1].<list element>")).isEqualTo("x1");
      assertThat(PropertyPathUtil.getValue(holder, "deptList[0].name")).isEqualTo("d0");
    }

    @Test
    @DisplayName("array including primitive array")
    void array() {
      assertThat(PropertyPathUtil.getValue(holder, "intArray[1]")).isEqualTo(20);
      assertThat(PropertyPathUtil.getValue(holder, "intArray[1].<list element>")).isEqualTo(20);
    }

    @Test
    @DisplayName("Map value: obtained by the string expression of the key")
    void mapValue() {
      assertThat(PropertyPathUtil.getValue(holder, "strMap[k1]")).isEqualTo("v1");
      assertThat(PropertyPathUtil.getValue(holder, "strMap[k1].<map value>")).isEqualTo("v1");
      assertThat(PropertyPathUtil.getValue(holder, "intKeyMap[5].name")).isEqualTo("d5");
      assertThat(PropertyPathUtil.getValue(holder, "listMap[k].<map value>[1].<list element>"))
          .isEqualTo("e1");
    }

    @Test
    @DisplayName("Map key: obtained by <K>")
    void mapKey() {
      assertThat(PropertyPathUtil.getValue(holder, "strMap<K>[k1].<map key>")).isEqualTo("k1");
      assertThat(PropertyPathUtil.getValue(holder, "intKeyMap<K>[5].<map key>")).isEqualTo(5);
    }

    @Test
    @DisplayName("Map: null when no key matches")
    void mapKeyNotFound() {
      assertThat(PropertyPathUtil.getValue(holder, "strMap[none].<map value>")).isNull();
      assertThat(PropertyPathUtil.getValue(holder, "strMap<K>[none].<map key>")).isNull();
    }

    @Test
    @DisplayName("Map: exception when two or more keys have the same string expression")
    void mapAmbiguousKey() {
      holder.ambiguousMap.put(1, "int");
      holder.ambiguousMap.put("1", "string");
      assertThatThrownBy(() -> PropertyPathUtil.getValue(holder, "ambiguousMap[1].<map value>"))
          .isInstanceOf(PropertyPathUtil.ElementOfCollectionCannotBeObtainedException.class);
    }

    @Test
    @DisplayName("Set: exception since the element cannot be identified")
    void set() {
      assertThatThrownBy(() -> PropertyPathUtil.getValue(holder, "strSet[].<iterable element>"))
          .isInstanceOf(PropertyPathUtil.ElementOfCollectionCannotBeObtainedException.class);
    }

    @Test
    @DisplayName("empty propertyPath returns the object itself")
    void emptyPath() {
      assertThat(PropertyPathUtil.getValue(holder, "")).isSameAs(holder);
    }
  }

  @Nested
  @DisplayName("getClass with generic element types")
  @SuppressWarnings("unused") // fields are read via reflection
  class GetClassWithGenericElementTypes {

    // Only the declared types are used, so the fields are not initialized.
    public static class Holder<T extends Number> {
      private @Nullable List<String> strList;
      private @Nullable List<List<String>> nestedList;
      private @Nullable Map<String, List<Integer>> listMap;
      private @Nullable Map<String, Map<Integer, String>> nestedMap;
      private @Nullable List<? extends Number> wildcardList;
      private @Nullable List<T> typeVariableList;
      private List<String> @Nullable [] genericArray;
    }

    @Test
    @DisplayName("element type which is not parameterized (works as before)")
    void simple() {
      assertThat(PropertyPathUtil.getClass(Holder.class, "strList[0].<list element>"))
          .isEqualTo(String.class);
      assertThat(PropertyPathUtil.getClass(Holder.class,
          "nestedList[0].<list element>[1].<list element>")).isEqualTo(String.class);
    }

    @Test
    @DisplayName("element type which is parameterized: the raw type is returned")
    void parameterized() {
      assertThat(PropertyPathUtil.getClass(Holder.class, "nestedList[0]")).isEqualTo(List.class);
      assertThat(PropertyPathUtil.getClass(Holder.class, "listMap[k]")).isEqualTo(List.class);
      assertThat(PropertyPathUtil.getClass(Holder.class, "listMap[k].<map value>[0]"))
          .isEqualTo(Integer.class);
    }

    @Test
    @DisplayName("map key and value of nested maps")
    void nestedMap() {
      assertThat(PropertyPathUtil.getClass(Holder.class, "nestedMap<K>[a].<map key>"))
          .isEqualTo(String.class);
      assertThat(PropertyPathUtil.getClass(Holder.class, "nestedMap[a].<map value>"))
          .isEqualTo(Map.class);
      assertThat(PropertyPathUtil.getClass(Holder.class, "nestedMap[a].<map value>[1]"))
          .isEqualTo(String.class);
      assertThat(PropertyPathUtil.getClass(Holder.class,
          "nestedMap[a].<map value><K>[1].<map key>")).isEqualTo(Integer.class);
    }

    @Test
    @DisplayName("wildcard type and type variable: the upper bound is returned")
    void wildcardAndTypeVariable() {
      assertThat(PropertyPathUtil.getClass(Holder.class, "wildcardList[0]"))
          .isEqualTo(Number.class);
      assertThat(PropertyPathUtil.getClass(Holder.class, "typeVariableList[0]"))
          .isEqualTo(Number.class);
    }

    @Test
    @DisplayName("array of a generic type")
    void genericArray() {
      assertThat(PropertyPathUtil.getClass(Holder.class, "genericArray[0]"))
          .isEqualTo(List.class);
      assertThat(PropertyPathUtil.getClass(Holder.class, "genericArray[0][1]"))
          .isEqualTo(String.class);
    }
  }
}
