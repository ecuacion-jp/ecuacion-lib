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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
 *     <td>{@code List<@NonNull String>}</td><td>{@code stringList[1].<list element>}</td></tr>
 * <tr><td>{@code List<List<@NonNull String>>}</td>
 *     <td>{@code stringList[1].<list element>[2].<list element>}</td></tr>
 * <tr>
 *     <td rowspan="2">Book</td>
 *     <td>{@code List<Book>}</td><td>{@code bookList[1].title}</td></tr>
 * <tr><td>{@code List<List<@NonNull String>>}</td>
 *     <td>{@code stringList[1].<list element>[2].<list element>}</td></tr>
 * <tr>
 *     <td>String</td><td>{@code List<@NonNull String>}</td>
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

  @SuppressWarnings("MutablePublicArray")
  public static final String[] COLLECTION_ELS =
      new String[] {EL_LIST, EL_SET, EL_MAP_KEY, EL_MAP_VAL};

  /**
   * Returns right most node from propertyPath.
   *
   * <p>Nodes contains collection parts.
   *     For example, {@code strList[0].<list element>}
   *     (specifies {@code List<@NonNull String> strList})
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

      rightMostNode = removingString + (rightMostNode.isEmpty() ? "" : ".") + rightMostNode;

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
   * <p>About {@code node}, see {@link #getRightMostNode(String)}.</p>
   */
  public static List<@NonNull String> getNodeList(String propertyPath) {
    List<@NonNull String> rtnList = new ArrayList<>();

    String pp = propertyPath;
    while (true) {
      if (pp.isEmpty()) {
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
    List<@NonNull String> nodeList = getNodeList(propertyPath).stream()
        .map(PropertyPathUtil::removeCollectionPartFromNode).toList();
    return String.join(".", nodeList);
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
   *
   * <p>This method is idempotent: applying it to an already-normalized path
   *     (e.g., {@code bookList[].title}) returns the path unchanged.</p>
   */
  public static String removeIndex(String propertyPath) {
    StringBuilder sb = new StringBuilder();
    String tmpPropertyPath = propertyPath;

    // Remove characters between "[" and "]".
    while (true) {
      if (tmpPropertyPath.isEmpty()) {
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
    List<@NonNull String> list =
        Arrays.asList(
            new String[] {".<list element>", ".<iterable element>", ".<map key>", ".<map value>"});
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

  /**
   * Returns a {@link Field} by navigating the dot-separated {@code propertyPath}.
   *
   * <p>Delegates simple (non-dot) field lookups to
   *     {@link ReflectionUtil#getDeclaredField(Class, String)}.</p>
   *
   * @param cls starting class
   * @param propertyPath dot-separated path (e.g. {@code "dept.name"}) or simple name
   * @return the resolved {@link Field}
   */
  public static Field getField(Class<?> cls, String propertyPath) {
    if (propertyPath.contains(".")) {
      String leftMost = propertyPath.substring(0, propertyPath.indexOf("."));
      String theRest = propertyPath.substring(leftMost.length() + 1);
      return getField(ReflectionUtil.getDeclaredField(cls, leftMost).getType(), theRest);
    }

    return ReflectionUtil.getDeclaredField(cls, propertyPath);
  }

  /**
   * Returns the Java class at the end of the given {@code propertyPath} starting from
   * {@code rootBeanClass}.
   *
   * <p>Supports collection notation (e.g. {@code "list[0]"}, {@code "map[key]"})
   *     and resolves generic type arguments for parameterized types.</p>
   *
   * <p>When {@code propertyPath} is empty, {@code rootBeanClass} itself is returned.</p>
   *
   * @param rootBeanClass starting class
   * @param propertyPath propertyPath relative to {@code rootBeanClass}
   * @return resolved class
   */
  public static Class<?> getClass(Class<?> rootBeanClass, String propertyPath) {
    Class<?> tmpClass = rootBeanClass;
    for (String node : getNodeList(propertyPath)) {
      String nodeWithoutCollectionPart = removeCollectionPart(node);

      try {
        Field tmpField = ReflectionUtil.getDeclaredField(tmpClass, nodeWithoutCollectionPart);
        tmpClass = tmpField.getType();

        if (!node.contains("[")) {
          continue;
        }

        // Count the number of "[" in propertyPath.
        int count = node.length() - node.replaceAll("\\[", "").length();

        Type type = tmpField.getGenericType();
        for (int i = 0; i < count; i++) {
          if (type instanceof Class<?> cls && cls.isArray()) {
            // Array: use component type
            type = cls.getComponentType();
          } else {
            // Collection or Map: use type argument
            Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
            // Map value access (no <K> in node): use 2nd type arg; others use 1st
            boolean isMapValueAccess = !node.contains("<K>")
                && Map.class.isAssignableFrom(
                    (Class<?>) ((ParameterizedType) type).getRawType());
            type = typeArgs[isMapValueAccess ? 1 : 0];
          }
        }

        tmpClass = type instanceof Class<?> c ? c
            : Class.forName(Objects.requireNonNull(type).getTypeName());

      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }

    return tmpClass;
  }

  /**
   * Returns a field value by navigating {@code propertyPath} from {@code object}.
   *
   * <p>Supports dot-separated paths (e.g. {@code "dept.name"}) and collection index
   *     notation (e.g. {@code "list[0]"}).</p>
   *
   * @param object root object
   * @param propertyPath path from root object to the target field
   * @return field value, or {@code null} if the field holds {@code null}
   */
  public static @Nullable Object getValue(Object object, String propertyPath) {
    while (true) {
      if (propertyPath.contains(".")) {
        @NonNull
        String leftMostOfPropertyPath = propertyPath.substring(0, propertyPath.indexOf("."));
        @NonNull
        String theRestOfPropertyPath = propertyPath.substring(propertyPath.indexOf(".") + 1);

        return getValue(Objects.requireNonNull(getValue(object, leftMostOfPropertyPath)),
            theRestOfPropertyPath);

      } else {
        if (propertyPath.contains("[")) {
          String propertyPathWithoutIndex =
              propertyPath.substring(0, propertyPath.indexOf("["));
          String tmpSerial = propertyPath.substring(propertyPath.indexOf("[") + 1);
          // It's string because it can be non-number value when the validated object is Map.
          String index = tmpSerial.substring(0, tmpSerial.indexOf("]"));

          // Handle Map key (propertyPath: field<K>[].<map key>)
          if (propertyPathWithoutIndex.contains("<")) {
            propertyPathWithoutIndex =
                propertyPathWithoutIndex.substring(0, propertyPathWithoutIndex.indexOf("<"));
          }

          Field rootField =
              ReflectionUtil.getDeclaredField(object.getClass(), propertyPathWithoutIndex);
          Object objs = ReflectionUtil.getFieldValue(object, rootField);

          // Resolve the field for array or List.
          // Occur an exception for any other collections
          // because it's impossible to specify the element in Set
          // (since the propertyPath is like "childSet[]")
          // and Map (since the propertyPath is like "childMap[test]"
          // where test is the key of them map entry. Any type can be key and
          // it's impossible to resolve it).
          if (objs instanceof Object[] arr) {
            return arr[Integer.parseInt(index)];

          } else if (objs instanceof List<?> list) {
            return list.get(Integer.parseInt(index));

          } else {
            throw new ElementOfCollectionCannotBeObtainedException(
                "Multiple value types other than array and List "
                    + "are not supported. The type of value: "
                    + Objects.requireNonNull(objs).getClass().getCanonicalName());
          }

        } else {
          Field rootField = ReflectionUtil.getDeclaredField(object.getClass(), propertyPath);
          return ReflectionUtil.getFieldValue(object, rootField);
        }
      }
    }
  }

  /**
   * Returns leafBean from rootBean and propertyPath from rootBean.
   *
   * @param rootBean root bean
   * @param propertyPath propertyPath from rootBean to the target field
   * @return leafBean
   */
  public static Object getLeafBean(Object rootBean, String propertyPath) {
    String leafBeanItemPropertyPath = getPropertyPathWithoutRightMostNode(propertyPath);

    return StringUtils.isEmpty(leafBeanItemPropertyPath) ? rootBean
        : Objects.requireNonNull(getValue(rootBean, leafBeanItemPropertyPath));
  }

  /**
   * Is thrown when getValue method called for non-ordered collections: Sets and Map keys.
   */
  public static class ElementOfCollectionCannotBeObtainedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance.
     */
    public ElementOfCollectionCannotBeObtainedException() {}

    /**
     * Constructs a new instance.
     */
    public ElementOfCollectionCannotBeObtainedException(String message) {
      super(message);
    }
  }
}
