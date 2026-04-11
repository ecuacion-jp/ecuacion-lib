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
import java.util.Arrays;
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
 * </tr>
 * <tr>
 *     <td rowspan="4">List</td><td rowspan="2">String</td>
 *     <td>{@code List<String>}</td><td>{@code stringList[1].<list element>}</td></tr>
 * <tr><td>{@code List<List<String>>}</td>
 *     <td>{@code stringList[1].<list element>[2].<list element>}</td></tr>
 * <tr>
 *     <td rowspan="2">Book</td>
 *     <td>{@code List<Book>}</td><td>{@code bookList[1].title}</td></tr>
 * <tr><td>{@code List<List<String>>}</td>
 *     <td>{@code stringList[1].<list element>[2].<list element>}</td></tr>
 * <tr>
 *     <td>String</td><td>{@code List<String>}</td>
 *     <td>{@code stringList[1].<list element>}</td>
 * </tr>
 * 
 * </table>
 */
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
   *     or {@code beanList[0]} (specifies {@code List<Bean> beanList}).<br>
   *     This method considers the collection parts as a part of a node.</p>
   *     
   * <table>
   * <caption>propertyPath for lists</caption>
   *    <tr><th>argument propertyPath</th><th>return</th><th>note</th></tr>
   *    <tr><td>{@code bean.strList[0].<list element>}</td>
   *        <td>{@code strList[0].<list element>}</td><td>{@code List<Strig> strList}</td></tr>
   *    <tr><td>{@code bean.strList[0].<list element>[0].<list element>}</td>
   *        <td>{@code strList[0].<list element>[0].<list element>}</td>
   *        <td>{@code List<List<Strig>> strList}</td>
   *    <tr><td>{@code bean.bookList[0]}</td>
   *        <td>{@code bookList[0]}</td><td>{@code List<Book> bookList}</td></tr>
   *    <tr><td>{@code bean.bookList[0].<list element>[0]}</td>
   *        <td>{@code bookList[0].<list element>[0]}</td>
   *        <td>{@code List<List<Book>> bookList}</td></tr>
   * </table>
   * 
   * <p>When argument string is {@code value} or {@code strList[0].<list element>}
   *     (no parent nodes exist), return the same as the argument value is returned.</p>
   */
  public static String getRightMostNode(String propertyPath) {
    String rightMostNode = "";

    String pp = propertyPath;
    while (true) {
      String removingString = pp.contains(".") ? pp.substring(pp.lastIndexOf(".") + 1) : pp;
      pp = pp.contains(".") ? pp.substring(0, pp.lastIndexOf(".")) : "";

      rightMostNode = removingString + (rightMostNode.equals("") ? "" : ".") + rightMostNode;

      if (Arrays.stream(COLLECTION_ELS).anyMatch(removingString::contains)) {
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

  /**
   * Remove index from propertyPath with list and key string from propertyPath with Map.
   * 
   * <p>As an implementation, it removes string enclosed in "[" and "]", 
   *     and also removes string expresses an element, like {@code <list element>}.</p>
   * 
   * <table border="1">
   * <caption>Applying the method to list</caption>
   * <tr>
   *    <th>argument</th>
   *    <th>return</th>
   * </tr>
   * <tr>
   *    <td>{@code stringList[1].<list element>}</td>
   *    <td>{@code stringList[]}</td>
   * </tr>
   * <tr>
   *    <td>{@code stringList[1].<list element>[2].<list element>}</td>
   *    <td>{@code stringList[][]}</td>
   * </tr>
   * <tr>
   *    <td>{@code userList[1].name}</td>
   *    <td>{@code userList[].name}</td>
   * </tr>
   * <tr>
   *    <td>{@code userList[1].<list element>[2].name}</td>
   *    <td>{@code userList[][].name}</td>
   * </tr>
   * </table>
   * 
   * <p>It can be used for collections other than List.</p>
   * 
   * <table border="1">
   * <caption>Applying the method to list</caption>
   * <tr>
   *    <th>argument</th>
   *    <th>return</th>
   * </tr>
   * <tr>
   *    <td>{@code stringSet[].<iterable element>}</td>
   *    <td>{@code stringSet[]}</td>
   * </tr>
   * <tr>
   *    <td>{@code stringSet[].<iterable element>}</td>
   *    <td>{@code stringSet[]}</td>
   * </tr>
   * <tr>
   *    <td>{@code stringSet[].<iterable element>}</td>
   *    <td>{@code stringSet[]}</td>
   * </tr>
   * </table>
   */
  public static String removeIndex(String propertyPath) {
    StringBuilder sb = new StringBuilder();
    String tmpPropertyPath = propertyPath;

    // Remove characters between "[" and "]".
    while (true) {
      if (tmpPropertyPath.equals("")) {
        break;

      } else if (!tmpPropertyPath.contains("]")) {
        sb.append(tmpPropertyPath);
        break;
      }

      sb.append(tmpPropertyPath.substring(0, tmpPropertyPath.indexOf("[")) + "[]");
      tmpPropertyPath = tmpPropertyPath.substring(tmpPropertyPath.indexOf("]") + 1);
    }

    // Remove element keywords.
    tmpPropertyPath = sb.toString();
    List<String> list =
        Arrays.asList(new String[] {".<list element>", ".<iterable element>", ".<map value>"});
    for (String keyword : list) {
      while (true) {
        if (tmpPropertyPath.contains(keyword)) {
          tmpPropertyPath = tmpPropertyPath.replace(keyword, "");

        } else {
          break;
        }
      }
    }

    return tmpPropertyPath;
  }
}
