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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jp.ecuacion.lib.core.util.ReflectionUtil.BeanProperty;
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

  /** The character that replaces the contents of {@code [...]} in masked propertyPaths. */
  private static final char MASK_CHAR = '_';

  /**
   * Returns a string of the same length as {@code propertyPath}
   *     in which the contents of every {@code [...]} part are replaced with {@code '_'}.
   *
   * <p>The contents of {@code [...]} are an index or the {@code toString()} value of a map key.
   *     The latter can be any string, so it may contain characters
   *     that have a meaning in the propertyPath syntax, like {@code "."}, {@code "["},
   *     {@code "]"} or {@code "<K>"}
   *     (e.g. {@code map[1.5].<map value>} or {@code map<K>[[a, b]].<map key>}).<br>
   *     Searching for the positions of such characters in the returned string
   *     instead of the original one prevents them from being misinterpreted.
   *     Substrings are to be taken from the original string at the found positions.</p>
   *
   * <p>The closing {@code "]"} of a {@code [...]} part is the first {@code "]"}
   *     at which the bracket depth reaches 0 or less 
   *     and which is followed by the end of the string,
   *     {@code "."}, {@code "["} or {@code "<"}.
   *     If no {@code "]"} satisfies it, the first {@code "]"} followed by one of them is used.
   *     Thus balanced brackets in contents (like the {@code toString()} value of a {@code List})
   *     and a {@code "]"} not followed by these characters are handled correctly.
   *     Contents containing an unbalanced {@code "]"} immediately followed by
   *     one of these characters cannot be distinguished from the end of the contents.</p>
   *
   * @param propertyPath propertyPath
   * @return masked propertyPath
   */
  static String maskBracketContents(String propertyPath) {
    char[] chars = propertyPath.toCharArray();
    int i = 0;
    while (i < chars.length) {
      if (chars[i] != '[') {
        i++;
        continue;
      }

      int close = findClosingBracket(propertyPath, i);
      if (close < 0) {
        break;
      }

      Arrays.fill(chars, i + 1, close, MASK_CHAR);
      i = close + 1;
    }

    return new String(chars);
  }

  private static int findClosingBracket(String propertyPath, int openPos) {
    int depth = 1;
    int fallback = -1;
    for (int j = openPos + 1; j < propertyPath.length(); j++) {
      char ch = propertyPath.charAt(j);
      if (ch == '[') {
        depth++;

      } else if (ch == ']') {
        depth--;
        if (isEndOfBracketPart(propertyPath, j + 1)) {
          if (depth <= 0) {
            return j;
          }

          if (fallback < 0) {
            fallback = j;
          }
        }
      }
    }

    return fallback;
  }

  private static boolean isEndOfBracketPart(String propertyPath, int nextPos) {
    if (nextPos >= propertyPath.length()) {
      return true;
    }

    char next = propertyPath.charAt(nextPos);
    return next == '.' || next == '[' || next == '<';
  }

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
    String masked = maskBracketContents(propertyPath);
    while (true) {
      int lastDotPos = masked.lastIndexOf('.');
      String removingString = lastDotPos >= 0 ? pp.substring(lastDotPos + 1) : pp;
      final String maskedRemovingString =
          lastDotPos >= 0 ? masked.substring(lastDotPos + 1) : masked;
      pp = lastDotPos >= 0 ? pp.substring(0, lastDotPos) : "";
      masked = lastDotPos >= 0 ? masked.substring(0, lastDotPos) : "";

      rightMostNode = removingString + (rightMostNode.isEmpty() ? "" : ".") + rightMostNode;

      if (Arrays.stream(COLLECTION_ELS).anyMatch(maskedRemovingString::contains)) {
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
    if (maskBracketContents(propertyPath).contains(".")) {
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

  private static String toFieldPathFromNode(String node) {
    String masked = maskBracketContents(node);
    int keyPos = masked.indexOf("<K>");
    int bracketPos = masked.indexOf('[');
    int endPos = keyPos < 0 ? bracketPos : (bracketPos < 0 ? keyPos : Math.min(keyPos, bracketPos));

    return endPos < 0 ? node : node.substring(0, endPos);
  }

  /**
   * Converts a propertyPath to a plain field path by removing all collection notation
   * ({@code [index]}, {@code <list element>}, etc.).
   *
   * <p>Used to obtain the field path for {@code itemNameKey} resolution.</p>
   *
   * <table border="1">
   * <caption>Examples</caption>
   * <tr><th>argument</th><th>return</th></tr>
   * <tr><td>{@code field}</td><td>{@code field}</td></tr>
   * <tr><td>{@code bean.field}</td><td>{@code bean.field}</td></tr>
   * <tr><td>{@code strList[0].<list element>}</td><td>{@code strList}</td></tr>
   * <tr><td>{@code bean.strList[0].<list element>}</td><td>{@code bean.strList}</td></tr>
   * <tr><td>{@code userList[1].name}</td><td>{@code userList.name}</td></tr>
   * </table>
   */
  public static String toFieldPath(String propertyPath) {
    List<@NonNull String> nodeList =
        getNodeList(propertyPath).stream().map(PropertyPathUtil::toFieldPathFromNode).toList();
    return String.join(".", nodeList);
  }

  /**
   * Converts a propertyPath to an index-less canonical form by removing specific indices
   * and element-type markers while keeping the {@code []} bracket structure.
   *
   * <p>Used to normalize a propertyPath so that it can be matched against
   *     item definitions registered in {@code customizedItems()},
   *     which use {@code []} without specific indices.</p>
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
  public static String toIndexlessPath(String propertyPath) {
    StringBuilder sb = new StringBuilder();
    // Positions are searched in the masked string
    // so that "[" or "]" in the toString() value of a map key is not misinterpreted.
    String masked = maskBracketContents(propertyPath);

    // Remove characters between "[" and "]".
    int pos = 0;
    while (pos < propertyPath.length()) {
      int openPos = masked.indexOf('[', pos);
      int closePos = openPos < 0 ? -1 : masked.indexOf(']', openPos);
      if (closePos < 0) {
        sb.append(propertyPath.substring(pos));
        break;
      }

      sb.append(propertyPath, pos, openPos).append("[]");
      pos = closePos + 1;
    }

    // Remove element keywords.
    String tmpPropertyPath = sb.toString();
    List<@NonNull String> list = Arrays.asList(
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
   * Returns a {@link BeanProperty} by navigating the dot-separated {@code propertyPath}.
   *
   * <p>Each node is resolved to a field, or to a getter when no field of the name exists.
   *     See {@link ReflectionUtil#getBeanProperty(Class, String)}.</p>
   *
   * @param cls starting class
   * @param propertyPath dot-separated path (e.g. {@code "dept.name"}) or simple name
   * @return the resolved {@link BeanProperty}
   */
  public static BeanProperty getBeanProperty(Class<?> cls, String propertyPath) {
    if (propertyPath.contains(".")) {
      String leftMost = propertyPath.substring(0, propertyPath.indexOf("."));
      String theRest = propertyPath.substring(leftMost.length() + 1);
      return getBeanProperty(ReflectionUtil.getBeanProperty(cls, leftMost).getType(), theRest);
    }

    return ReflectionUtil.getBeanProperty(cls, propertyPath);
  }

  /**
   * Returns the Java class at the end of the given {@code propertyPath} starting from
   * {@code rootBeanClass}.
   *
   * <p>Supports collection notation (e.g. {@code "list[0]"}, {@code "map[key]"})
   *     and resolves generic type arguments for parameterized types.</p>
   *
   * <p>Each node is resolved to a field, or to a getter when no field of the name exists.
   *     See {@link ReflectionUtil#getBeanProperty(Class, String)}.</p>
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
      String nodeWithoutCollectionPart = toFieldPath(node);

      try {
        BeanProperty tmpProperty =
            ReflectionUtil.getBeanProperty(tmpClass, nodeWithoutCollectionPart);
        tmpClass = tmpProperty.getType();

        String maskedNode = maskBracketContents(node);
        if (!maskedNode.contains("[")) {
          continue;
        }

        Type type = tmpProperty.getGenericType();
        // Each "[" means an access to an element of an array, a collection or a map.
        for (int pos = maskedNode.indexOf('['); pos >= 0; pos = maskedNode.indexOf('[', pos + 1)) {
          type = upperBound(type);
          if (type instanceof Class<?> cls && cls.isArray()) {
            // Array: use component type (not null since cls is an array)
            type = Objects.requireNonNull(cls.getComponentType());

          } else if (type instanceof GenericArrayType gat) {
            // Array of a generic type (like List<String>[]): use component type
            type = gat.getGenericComponentType();

          } else {
            // Collection or Map: use type argument
            ParameterizedType pt = (ParameterizedType) type;
            Type[] typeArgs = pt.getActualTypeArguments();
            // Map value access ("map[key]", not "map<K>[key]"): use 2nd type arg; others use 1st
            boolean isMapValueAccess = !maskedNode.startsWith("<K>", pos - 3)
                && Map.class.isAssignableFrom((Class<?>) pt.getRawType());
            type = typeArgs[isMapValueAccess ? 1 : 0];
          }
        }

        tmpClass = toClass(type);

      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }

    return tmpClass;
  }

  /**
   * Returns the upper bound of a wildcard type (like {@code ? extends Number})
   *     or a type variable (like {@code T extends Number}), or {@code type} itself otherwise.
   */
  private static Type upperBound(Type type) {
    if (type instanceof WildcardType wt) {
      return upperBound(wt.getUpperBounds()[0]);
    }

    if (type instanceof TypeVariable<?> tv) {
      return upperBound(tv.getBounds()[0]);
    }

    return type;
  }

  /**
   * Returns the class of {@code type}.
   *
   * <p>The raw type is used for a parameterized type (like {@code List<String>}),
   *     and the upper bound is used for a wildcard type or a type variable.</p>
   */
  private static Class<?> toClass(Type type) {
    Type bound = upperBound(type);
    if (bound instanceof Class<?> cls) {
      return cls;
    }

    if (bound instanceof ParameterizedType pt) {
      return (Class<?>) pt.getRawType();
    }

    if (bound instanceof GenericArrayType gat) {
      return Array.newInstance(toClass(gat.getGenericComponentType()), 0).getClass();
    }

    throw new IllegalArgumentException("The class of the type cannot be obtained. (type: "
        + bound.getTypeName() + ")");
  }

  /**
   * Returns a property value by navigating {@code propertyPath} from {@code object}.
   *
   * <p>Supports dot-separated paths (e.g. {@code "dept.name"}) and the notation of collections
   *     used in the propertyPath of Jakarta Validation.</p>
   *
   * <table border="1">
   * <caption>Supported collections</caption>
   * <tr><th>collection</th><th>example of propertyPath</th><th>value</th></tr>
   * <tr><td>array, {@code List}</td>
   *     <td>{@code list[0]}, {@code list[0].<list element>}, {@code list[0].name}</td>
   *     <td>the element at the index</td></tr>
   * <tr><td>nested array, {@code List}</td>
   *     <td>{@code list[0][1]}, {@code list[0].<list element>[1].<list element>}</td>
   *     <td>the element at the indices</td></tr>
   * <tr><td>{@code Map} (value)</td>
   *     <td>{@code map[key]}, {@code map[key].<map value>}, {@code map[key].name}</td>
   *     <td>the value of the entry</td></tr>
   * <tr><td>{@code Map} (key)</td><td>{@code map<K>[key].<map key>}</td>
   *     <td>the key of the entry</td></tr>
   * </table>
   *
   * <p>The key of a {@code Map} in the propertyPath is compared with
   *     {@code String.valueOf(key)} of each entry,
   *     since Jakarta Validation puts the {@code toString()} value of the key in the propertyPath.
   *     {@code null} is returned when no key matches.
   *     {@link ElementOfCollectionCannotBeObtainedException} is thrown
   *     when two or more keys match.</p>
   *
   * <p>{@link ElementOfCollectionCannotBeObtainedException} is also thrown for {@code Set}
   *     and other collections, since the propertyPath (like {@code set[].<iterable element>})
   *     does not identify the element.</p>
   *
   * <p>Each node is resolved to a field, or to a getter when no field of the name exists.
   *     See {@link ReflectionUtil#getBeanProperty(Class, String)}.</p>
   *
   * <p>When an intermediate value of the path is {@code null}
   *     (e.g. {@code dept} of {@code "dept.name"}, or {@code list} of {@code "list[0]"}),
   *     {@code null} is returned, since the target property does not have a value either.</p>
   *
   * <p>When {@code propertyPath} is empty, {@code object} itself is returned.</p>
   *
   * @param object root object
   * @param propertyPath path from root object to the target property
   * @return property value, or {@code null} if the property or an intermediate value of the path
   *     holds {@code null}
   */
  public static @Nullable Object getValue(Object object, String propertyPath) {
    Object current = object;
    for (String node : getNodeList(propertyPath)) {
      if (current == null) {
        return null;
      }

      current = getValueOfNode(current, node);
    }

    return current;
  }

  /**
   * Returns the value of a node (like {@code name}, {@code list[0].<list element>}
   *     or {@code map<K>[key].<map key>}) of {@code object}.
   */
  private static @Nullable Object getValueOfNode(Object object, String node) {
    Object value = ReflectionUtil.getBeanProperty(object.getClass(), toFieldPathFromNode(node))
        .getValue(object);

    // Positions are searched in the masked string
    // so that "[" or "]" in the toString() value of a map key is not misinterpreted.
    String masked = maskBracketContents(node);
    int pos = 0;
    while (true) {
      int openPos = masked.indexOf('[', pos);
      if (openPos < 0) {
        return value;
      }

      if (value == null) {
        return null;
      }

      int closePos = masked.indexOf(']', openPos);
      String indexOrKey = node.substring(openPos + 1, closePos);
      boolean isMapKey = masked.startsWith("<K>", openPos - 3);
      value = getElement(value, indexOrKey, isMapKey);
      pos = closePos + 1;
    }
  }

  private static @Nullable Object getElement(Object collection, String indexOrKey,
      boolean isMapKey) {
    if (collection instanceof Map<?, ?> map) {
      Map.Entry<?, ?> entry = findMapEntry(map, indexOrKey);
      return entry == null ? null : (isMapKey ? entry.getKey() : entry.getValue());
    }

    if (!isMapKey && collection.getClass().isArray()) {
      return Array.get(collection, Integer.parseInt(indexOrKey));
    }

    if (!isMapKey && collection instanceof List<?> list) {
      return list.get(Integer.parseInt(indexOrKey));
    }

    // Set and other collections: the element cannot be identified
    // since the propertyPath is like "set[].<iterable element>".
    throw new ElementOfCollectionCannotBeObtainedException(
        "Collections other than array, List and Map are not supported "
            + "since the element cannot be identified by the propertyPath. The type of value: "
            + collection.getClass().getCanonicalName());
  }

  private static Map.@Nullable Entry<?, ?> findMapEntry(Map<?, ?> map, String keyString) {
    List<Map.Entry<?, ?>> matched = new ArrayList<>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (String.valueOf(entry.getKey()).equals(keyString)) {
        matched.add(entry);
      }
    }

    if (matched.size() > 1) {
      throw new ElementOfCollectionCannotBeObtainedException(
          "The entry of the map cannot be identified since two or more keys have the same "
              + "string expression. (key: " + keyString + ")");
    }

    return matched.isEmpty() ? null : matched.get(0);
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
   * Is thrown when the element of a collection cannot be identified by the propertyPath
   *     in {@code getValue}: elements of {@code Set} and other collections
   *     other than array, {@code List} and {@code Map},
   *     or entries of {@code Map} whose keys have the same string expression.
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
