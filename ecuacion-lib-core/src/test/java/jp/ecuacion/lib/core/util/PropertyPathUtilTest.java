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
import org.junit.jupiter.api.DisplayName;
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
}
