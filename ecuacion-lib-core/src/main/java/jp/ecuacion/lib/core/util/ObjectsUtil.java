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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import jp.ecuacion.lib.core.annotation.RequireElementNonEmpty;
import jp.ecuacion.lib.core.annotation.RequireElementNonNull;
import jp.ecuacion.lib.core.annotation.RequireNonEmpty;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides utility methods for {@code Objects.requireNonnull} and other checks.
 * 
 * <p>{@code Objects.requireNonnull} throws NullPointerException and we can't see the difference
 * whether it happens because of the parameter or return value check, or other reasons.<br>
 * By using these methods you can see it by additional messages.</p>
 * 
 * <p>Several other methods provided to clarify an exception is thrown from ecuacion apps.</p>
 */
public class ObjectsUtil {

  /**
   * Prevents other classes from instantiating it.
   */
  private ObjectsUtil() {}

  /**
   * Validates that the argument is not {@code null}
   *     and throws {@code RequireNonNullException} if it is.
   * 
   * @param <T> The class of the argument
   * @param object Any object
   * @return the argument
   */
  public static <T> @NonNull T requireNonNull(@Nullable T object) {
    if (object == null) {
      throw new RequireNonNullException();
    }

    return object;
  }

  /**
   * Validates that multiple arguments are not {@code null}
   *     and throws {@code RequireNonNullException} if any of them is.
   * 
   * <p>This is used to validate multiple arguments at one time.</p>
   * 
   * @param object1 Any object
   * @param object2 Any object
   * @param objects Any objects
   */
  public static void requireNonNull(@Nullable Object object1, @Nullable Object object2,
      @Nullable Object... objects) {

    Object[] allObjects = ArrayUtils.addAll(objects, object1, object2);

    for (Object object : allObjects) {
      requireNonNull(object);
    }
  }

  /**
   * Validates that the argument is not {@code null} or {@code blank("")}
   *     and throws {@code RequireNonEmptyException} if it is.
   *     
   * @param string Any string 
   * @return the argument
   */
  public static String requireNonEmpty(@RequireNonEmpty @Nullable String string) {
    if (StringUtils.isEmpty(string)) {
      throw new RequireNonEmptyException();
    }

    return Objects.requireNonNull(string);
  }

  /**
   * Validates that multiple arguments are not {@code null} or {@code blank("")}
   *     and throws {@code RequireNonEmptyException} if any of them is.
   *     
   * @param string1 Any string
   * @param string2 Any string
   * @param strings Any strings
   */
  public static void requireNonEmpty(@RequireNonEmpty @Nullable String string1,
      @RequireNonEmpty @Nullable String string2,
      @RequireNonEmpty @Nullable String @Nullable... strings) {

    String[] allStrings = ArrayUtils.addAll(strings, string1, string2);

    for (String string : allStrings) {
      requireNonEmpty(string);
    }
  }

  /**
   * Validates the length of an array is not zero 
   *     and throws {@code RequireSizeNonZeroException} 
   *     if the argument value does not match the condition.
   * 
   * @param <T> The class of the argument array
   * @param objects Any object, {@code null} is acceptable.
   * @return the argument
   */
  public static <T extends @Nullable Object> T[] requireSizeNonZero(T[] objects) {
    requireSizeNonZero(Objects.requireNonNull(Arrays.asList(objects)));

    return objects;
  }

  /**
   * Validates the length of a collection is not zero 
   *     and throws {@code RequireSizeNonZeroException} 
   *     if the argument value does not match the condition.
   * 
   * @param <T> The class of the argument collection
   * @param collection Any collection, {@code null} is acceptable.
   * @return the argument
   */
  public static <T extends @Nullable Object> Collection<T> requireSizeNonZero(
      Collection<T> collection) {

    if (collection != null && collection.isEmpty()) {
      throw new RequireSizeNonZeroException();
    }

    return (Collection<T>) collection;
  }

  /**
   * Validates that elements of an array are not {@code null}
   *     and throws {@code RequireElementNonNullException} if any element is.
   *
   * @param <T> The class of the argument array
   * @param objects Any object, {@code null} is acceptable.
   * @return the argument
   */
  public static <T extends @Nullable Object> T[] requireElementNonNull(T[] objects) {
    List<T> objList = Arrays.asList(objects);
    requireElementNonNull(objList);

    T[] nonNullObjs = objects;
    return nonNullObjs;
  }

  /**
   * Validates that elements of a collection are not {@code null}
   *     and throws {@code RequireElementNonNullException} if any element is.
   *
   * @param <T> The class of the argument collection
   * @param collection Any collection, {@code null} is acceptable.
   * @return the argument
   */
  public static <T extends @Nullable Object> Collection<T> requireElementNonNull(
      Collection<T> collection) {

    if (collection != null) {
      for (T object : collection) {
        if (object == null) {
          throw new RequireElementNonNullException();
        }
      }
    }

    Collection<T> nonNullCol = collection;
    return nonNullCol;
  }

  /**
   * Validates that elements of an array are not {@code null} or empty
   *     and throws {@code RequireElementNonEmptyException} if any element is.
   *
   * @param strings Any strings, {@code null} is acceptable.
   * @return the argument
   */
  public static String[] requireElementNonEmpty(@RequireElementNonEmpty String[] strings) {

    if (strings != null) {
      for (String string : strings) {
        if (StringUtils.isEmpty(string)) {
          throw new RequireElementNonEmptyException();
        }
      }
    }

    return strings;
  }

  /**
   * Validates that elements of a collection are not {@code null} or empty
   *     and throws {@code RequireElementNonEmptyException} if any element is.
   *
   * @param collection Any collection, {@code null} is acceptable.
   * @return the argument
   */
  public static Collection<String> requireElementNonEmpty(
      @RequireElementNonEmpty Collection<String> collection) {
    requireElementNonEmpty(collection.toArray(String[]::new));

    return collection;
  }

  /**
   * Validates that elements of an array are not duplicated
   *     and throws {@code RequireElementsNonDuplicatedException} if any duplicate is found.
   *
   * @param <T> The class of the argument array
   * @param objects Any object, {@code null} is acceptable.
   * @return the argument
   */
  public static <T> T[] requireElementsNonDuplicated(@RequireElementNonNull T[] objects) {
    requireElementsNonDuplicated(Arrays.asList(objects));

    return objects;
  }

  /**
   * Validates that elements of a collection are not duplicated
   *     and throws {@code RequireElementsNonDuplicatedException} if any duplicate is found.
   *
   * @param <T> The class of the argument collection
   * @param collection Any collection, {@code null} is acceptable.
   * @return the argument
   */
  public static <T> Collection<T> requireElementsNonDuplicated(
      @RequireElementNonNull Collection<T> collection) {

    Set<T> set = new HashSet<>();
    if (collection != null) {
      for (T object : collection) {
        if (set.contains(object)) {
          throw new RequireElementsNonDuplicatedException();
        }
      }
    }

    return collection;
  }

  /**
   * Is an abstract exception class.
   */
  public abstract static class ObjectsUtilException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new instance.
     */
    public ObjectsUtilException(String message) {
      super(message);
    }
  }

  /**
   * Designates non-null is required.
   */
  public static class RequireNonNullException extends ObjectsUtilException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new instance.
     */
    public RequireNonNullException() {
      super("Non-null required.");
    }
  }

  /**
   * Designates non-empty is required.
   */
  public static class RequireNonEmptyException extends ObjectsUtilException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new instance.
     */
    public RequireNonEmptyException() {
      super("Non-empty required.");
    }
  }

  /**
   * Designates size non-zero is required.
   */
  public static class RequireSizeNonZeroException extends ObjectsUtilException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new instance.
     */
    public RequireSizeNonZeroException() {
      super("Size non-zero required.");
    }
  }

  /**
   * Designates element non-null is required.
   */
  public static class RequireElementNonNullException extends ObjectsUtilException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new instance.
     */
    public RequireElementNonNullException() {
      super("Element non-null required.");
    }
  }

  /**
   * Designates element non-empty is required.
   */
  public static class RequireElementNonEmptyException extends ObjectsUtilException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new instance.
     */
    public RequireElementNonEmptyException() {
      super("Element non-empty required.");
    }
  }

  /**
   * Designates elements value non-duplicated required.
   */
  public static class RequireElementsNonDuplicatedException extends ObjectsUtilException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new instance.
     */
    public RequireElementsNonDuplicatedException() {
      super("Elements non-duplicated required.");
    }
  }
}
