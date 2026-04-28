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
import java.util.List;
import org.jspecify.annotations.NonNull;
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
  // removeCollectionPart
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("removeCollectionPart")
  class RemoveCollectionPart {

    @Test
    @DisplayName("plain field is unchanged")
    void plainField() {
      assertThat(PropertyPathUtil.removeCollectionPart("field")).isEqualTo("field");
      assertThat(PropertyPathUtil.removeCollectionPart("bean.field")).isEqualTo("bean.field");
    }

    @Test
    @DisplayName("list node strips index and element marker")
    void listNode() {
      assertThat(PropertyPathUtil.removeCollectionPart("strList[0]." + L)).isEqualTo("strList");
      assertThat(PropertyPathUtil.removeCollectionPart("bean.strList[0]." + L))
          .isEqualTo("bean.strList");
    }

    @Test
    @DisplayName("bean list node strips only the index")
    void beanListNode() {
      assertThat(PropertyPathUtil.removeCollectionPart("userList[1].name"))
          .isEqualTo("userList.name");
    }

    @Test
    @DisplayName("map key node strips the key qualifier")
    void mapKeyNode() {
      assertThat(PropertyPathUtil.removeCollectionPart("strMap<K>[]." + MK)).isEqualTo("strMap");
    }
  }

  // -------------------------------------------------------------------------
  // removeIndex
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("removeIndex")
  class RemoveIndex {

    @Test
    @DisplayName("plain field is unchanged")
    void plainField() {
      assertThat(PropertyPathUtil.removeIndex("field")).isEqualTo("field");
    }

    @Test
    @DisplayName("list: index removed and element marker stripped")
    void list() {
      assertThat(PropertyPathUtil.removeIndex("stringList[1]." + L)).isEqualTo("stringList[]");
      assertThat(PropertyPathUtil.removeIndex("stringList[1]." + L + "[2]." + L))
          .isEqualTo("stringList[][]");
      assertThat(PropertyPathUtil.removeIndex("userList[1].name")).isEqualTo("userList[].name");
    }

    @Test
    @DisplayName("set: element marker stripped")
    void set() {
      assertThat(PropertyPathUtil.removeIndex("stringSet[]." + S)).isEqualTo("stringSet[]");
    }

    @Test
    @DisplayName("map value: index removed and element marker stripped")
    void mapValue() {
      assertThat(PropertyPathUtil.removeIndex("strMap[key1]." + MV)).isEqualTo("strMap[]");
    }

    @Test
    @DisplayName("map key: element marker stripped")
    void mapKey() {
      assertThat(PropertyPathUtil.removeIndex("strMap<K>[]." + MK)).isEqualTo("strMap<K>[]");
    }
  }
}
