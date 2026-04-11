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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
    Assertions.assertEquals("field", getRightMostNode("field"));

    // with "."
    Assertions.assertEquals("field", getRightMostNode("bean.field"));
    Assertions.assertEquals("field", getRightMostNode("bean1.bean2.field"));
  }

  @Test
  public void getRightMostNodeTest_array() {
    // array: index notation only, no collection-element marker
    Assertions.assertEquals("field[0]", getRightMostNode("field[0]"));
    Assertions.assertEquals("field[0]", getRightMostNode("bean.field[0]"));
  }

  @Test
  public void getRightMostNodeTest_list() {
    // List: strList[0].<list element>
    Assertions.assertEquals("strList[0]." + L, getRightMostNode("strList[0]." + L));
    Assertions.assertEquals("strList[0]." + L, getRightMostNode("bean.strList[0]." + L));
  }

  @Test
  public void getRightMostNodeTest_set() {
    // Set: intSet[].<iterable element>
    Assertions.assertEquals("intSet[]." + S, getRightMostNode("intSet[]." + S));
    Assertions.assertEquals("intSet[]." + S, getRightMostNode("bean.intSet[]." + S));
  }

  @Test
  public void getRightMostNodeTest_mapKey() {
    // Map key: strMap<K>[].<map key>
    Assertions.assertEquals("strMap<K>[]." + MK, getRightMostNode("strMap<K>[]." + MK));
    Assertions.assertEquals("strMap<K>[]." + MK, getRightMostNode("bean.strMap<K>[]." + MK));
  }

  @Test
  public void getRightMostNodeTest_mapValue() {
    // Map value: strMap[key1].<map value>
    Assertions.assertEquals("strMap[key1]." + MV, getRightMostNode("strMap[key1]." + MV));
    Assertions.assertEquals("strMap[key1]." + MV, getRightMostNode("bean.strMap[key1]." + MV));
  }

  @Test
  public void getRightMostNodeTest_pairRight() {
    // Pair.right is exposed as map value node type (no key qualifier)
    Assertions.assertEquals("pairField." + MV, getRightMostNode("pairField." + MV));
    Assertions.assertEquals("pairField." + MV, getRightMostNode("bean.pairField." + MV));
  }

  @Test
  public void getRightMostNodeTest_nestedList() {
    // List<List<String>>: strListList[0].<list element>[0].<list element>
    String path = "strListList[0]." + L + "[0]." + L;
    Assertions.assertEquals(path, getRightMostNode(path));
    Assertions.assertEquals(path, getRightMostNode("bean." + path));
  }

  @Test
  public void getRightMostNodeTest_nestedSet() {
    // Set<Set<Integer>>: intSetSet[].<iterable element>[].<iterable element>
    String path = "intSetSet[]." + S + "[]." + S;
    Assertions.assertEquals(path, getRightMostNode(path));
    Assertions.assertEquals(path, getRightMostNode("bean." + path));
  }

  @Test
  public void getRightMostNodeTest_nestedMapValue() {
    // Map<V, Map<V>>: mapMap[outer].<map value>[inner].<map value>
    String path = "mapMap[outer]." + MV + "[inner]." + MV;
    Assertions.assertEquals(path, getRightMostNode(path));
    Assertions.assertEquals(path, getRightMostNode("bean." + path));
  }

  @Test
  public void getRightMostNodeTest_nestedMix() {
    // List<Set<Map<String, Integer>>>:
    // listSetMap[0].<list element>[].<iterable element>[key].<map value>
    String path = "listSetMap[0]." + L + "[]." + S + "[key]." + MV;
    Assertions.assertEquals(path, getRightMostNode(path));
    Assertions.assertEquals(path, getRightMostNode("bean." + path));
  }
}
