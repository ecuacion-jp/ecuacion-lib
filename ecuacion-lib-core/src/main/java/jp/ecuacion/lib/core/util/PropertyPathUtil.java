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

import java.util.ArrayList;
import java.util.List;

/**
 * Provides utilities on propertyPaths.
 * 
 * <p>ProperyPath for collections:</p>
 *
 * <table border="1">
 * <caption>PropertyPath for collections</caption>
 * <tr>
 *  <th>Class</th>
 *  <th>Parameter type</th>
 *  <th>Type</th>
 *  <th>propertyPath</th>
 *  <th>Type (double)</th>
 *  <th>propertyPath (double)</th>
 * </tr>
 * <tr>
 *     <td rowspan="2">List</td><td>String</td><td>{@code List<String>}</td>
 *     <td>{@code stringList[1].<list element>}</td>
 *     <td>{@code List<List<String>>}</td><td>{@code List<String>}</td>
 * </tr>
 * <tr>
 *     <td>String</td><td>{@code List<String>}</td>
 *     <td>{@code stringList[1].<list element>}</td>
 * </tr>
 * 
 * </table>
 */


// // list : propertyPath=targetList[0].<list element>[0].field
// // set : propertyPath=targetList[].<iterable element>[].field
// // map key: propertyPath=targetList[].<map value><K>[TargetCls[field=null]].field
// // map val: propertyPath=targetList[key1].<map value>[key2].field
public class PropertyPathUtil {

  /**
   * PropertyPath is {@code stringList[1].<list element>}.
   */
  public static final String EL_LIST = "<list element>";

  /**
   * PropertyPath is {@code integerSet[].<iterable element>}.
   */
  public static final String EL_SET = "<iterable element>";

  /**
   * PropertyPath is {@code strDateMap<K>[].<map key>}.
   */
  public static final String EL_MAP_KEY = "<map key>";

  /**
   * PropertyPath is {@code strDateMap[test].<map value>}.
   */
  public static final String EL_MAP_VAL = "<map value>";

  public static final String[] COLLECTION_ELS =
      new String[] {EL_LIST, EL_SET, EL_MAP_KEY, EL_MAP_VAL};

  /**
   * Returns right most node from propertyPath.
   * 
   * <p>Nodes contains collection parts.
   *     For example, {@code strList[0].<list element>} 
   *     (specifies {@code List<String> strList})
   *     or {@code obj[0]} (specifies {@code List<Obje> objList})<br>
   *     This method removes collection related part, which menas
   *     it changes {@code strList[0].<list element>} to {@code strList}
   *     and {@code obj[0]} to {@code obj}.</p>
   */
  public static String getRightMostNode(String propertyPath) {
    String rightMostNode = "";

    String pp = propertyPath;
    while (true) {
      String removingString = pp.contains(".") ? pp.substring(pp.lastIndexOf(".") + 1) : pp;
      pp = pp.contains(".") ? pp.substring(0, pp.lastIndexOf(".")) : "";

      rightMostNode = removingString + (rightMostNode.equals("") ? "" : ".") + rightMostNode;

      if (removingString.contains(EL_LIST) || removingString.contains(EL_SET)
          || removingString.contains(EL_MAP_KEY) || removingString.contains(EL_MAP_VAL)) {
        continue;
      }

      return rightMostNode;
    }
  }

  /**
   * Returns leafBean propertyPath.
   * 
   * <ul>
   * <li>Collections considered. <br>
   *     When propertyPath is {@code "bean.field[1].<list element>"}, 
   *     return value is "bean", not bean.field[1].</li>
   * <li>ClassValidators considered.<br>
   *     When propertyPath obtained from ConstraintViolation is "bean1.bean2"
   *     and propertyPath obtained from attribute of annotation is "bean3.field",
   *     the resultant leafBean becomes "bean1.bean2.bean3".</li>
   * </ul>
   */
  public static String getPropertyPathWithoutRightMostNode(String propertyPath) {
    if (propertyPath.contains(".")) {
      String rtn = propertyPath.substring(0,
          propertyPath.length() - getRightMostNode(propertyPath).length());

      return rtn.endsWith(".") ? rtn.substring(0, rtn.length() - 1) : rtn;

    } else {
      return "";
    }
  }

  /**
   * Returns node list from propertyPath.
   * 
   * <p>About {@code node}, {@see getRightMostNode(String}}.</p>
   */
  public static List<String> getNodeList(String propertyPath) {
    List<String> rtnList = new ArrayList<>();

    String pp = propertyPath;
    while (true) {
      if (pp.equals("")) {
        return rtnList.reversed();
      }

      String rightMostNode = getRightMostNode(pp);
      rtnList.add(rightMostNode);

      pp = pp.equals(rightMostNode) ? ""
          : pp.substring(0, pp.length() - (rightMostNode.length() + 1));
    }
  }

  private static String removeCollectionPartFromNode(String node) {
    node = node.contains("<K>") ? node.substring(0, node.indexOf("<K>")) : node;
    node = node.contains("[") ? node.substring(0, node.indexOf("[")) : node;

    return node;
  }

  /**
   * Removes Collection related part from the given propertyPath node.
   */
  public static String removeCollectionPart(String propertyPath) {
    List<String> nodeList = new ArrayList<>();
    for (String node : getNodeList(propertyPath)) {
      nodeList.add(removeCollectionPartFromNode(node));
    }

    StringBuilder sb = new StringBuilder();
    nodeList.stream().forEach(node -> sb.append("." + node));
    return sb.toString().length() < 1 ? "" : sb.toString().substring(1);
  }
}
