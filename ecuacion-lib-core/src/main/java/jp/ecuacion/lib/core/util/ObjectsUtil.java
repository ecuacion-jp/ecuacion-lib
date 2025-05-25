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

import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import jp.ecuacion.lib.core.annotation.RequireElementNonempty;
import jp.ecuacion.lib.core.annotation.RequireElementNonnull;
import jp.ecuacion.lib.core.annotation.RequireNonempty;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.annotation.RequireSizeNonzero;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

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
   * Validates the argument is not {@code null}  
   *     and throws {@code RequireNonNullException} 
   *     if the argument value does not match the condition.
   * 
   * @param <T> The class of the argument
   * @param object Any object
   * @return the argument
   */
  @Nonnull
  public static <T> T requireNonNull(@RequireNonnull T object) {
    if (object == null) {
      throw new RequireNonNullException();
    }

    return object;
  }

  /**
   * Validates multiple arguments are not {@code null}  
   *     and throws {@code RequireNonNullException} 
   *     if arguments value do not match the condition.
   * 
   * <p>This is used to validate multiple arguments at one time.</p>
   * 
   * @param object1 Any object
   * @param object2 Any object
   * @param objects Any objects
   */
  @Nonnull
  public static void requireNonNull(@RequireNonnull Object object1, @RequireNonnull Object object2,
      @RequireNonnull Object... objects) {

    Object[] allObjects = ArrayUtils.addAll(objects, object1, object2);

    for (Object object : allObjects) {
      requireNonNull(object);
    }
  }

  /**
   * Validates the argument is not {@code null} or {@code blank("")}  
   *     and throws {@code RequireNonEmptyException} 
   *     if the argument value does not match the condition.
   *     
   * @param string Any string 
   * @return the argument
   */
  @Nonnull
  public static String requireNonEmpty(@RequireNonempty String string) {

    if (string == null || string.equals("")) {
      throw new RequireNonEmptyException();
    }

    return string;
  }

  /**
   * Validates multiple arguments are not {@code null} or {@code blank("")}  
   *     and throws {@code RequireNonEmptyException} 
   *     if arguments value do not match the condition.
   *     
   * @param string1 Any string
   * @param string2 Any string
   * @param strings Any strings
   */
  @Nonnull
  public static void requireNonEmpty(@RequireNonempty String string1,
      @RequireNonempty String string2, @RequireNonempty String... strings) {

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
  @Nonnull
  public static <T> T[] requireSizeNonZero(@RequireSizeNonzero T[] objects) {
    requireSizeNonZero(Arrays.asList(objects));

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
  @Nonnull
  public static <T> Collection<T> requireSizeNonZero(@RequireSizeNonzero Collection<T> collection) {

    if (collection != null && collection.size() == 0) {
      throw new RequireSizeNonZeroException();
    }

    return collection;
  }

  /**
   * Validates elements of an array is not {@code null} 
   *     and throws {@code RequireElementNonNullException} 
   *     if the argument value does not match the condition.
   * 
   * @param <T> The class of the argument array
   * @param objects Any object, {@code null} is acceptable.
   * @return the argument
   */
  @Nonnull
  public static <T> T[] requireElementNonNull(@RequireElementNonnull T[] objects) {
    requireElementNonNull(Arrays.asList(objects));

    return objects;
  }

  /**
   * Validates elements of a collection is not {@code null} 
   *     and throws {@code RequireElementNonNullException} 
   *     if the argument value does not match the condition.
   * 
   * @param <T> The class of the argument collection
   * @param collection Any collection, {@code null} is acceptable.
   * @return the argument
   */
  @Nonnull
  public static <T> Collection<T> requireElementNonNull(
      @RequireElementNonnull Collection<T> collection) {

    if (collection != null) {
      for (T object : collection) {
        if (object == null) {
          throw new RequireElementNonNullException();
        }
      }
    }

    return collection;
  }

  /**
   * Validates elements of an array is not {@code null} 
   *     and throws {@code RequireElementNonEmptyException} 
   *     if the argument value does not match the condition.
   * 
   * @param strings Any strings, {@code null} is acceptable.
   * @return the argument
   */
  @Nonnull
  public static String[] requireElementNonEmpty(@RequireElementNonempty String[] strings) {

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
   * Validates elements of a collection is not {@code null} 
   *     and throws {@code RequireElementNonEmptyException} 
   *     if the argument value does not match the condition.
   * 
   * @param collection Any collection, {@code null} is acceptable.
   * @return the argument
   */
  @Nonnull
  public static Collection<String> requireElementNonEmpty(
      @RequireElementNonempty Collection<String> collection) {
    requireElementNonEmpty(collection.toArray(new String[collection.size()]));

    return collection;
  }

  /**
   * Validates elements of an array is not {@code null} 
   *     and throws {@code RequireElementNonNullException} 
   *     if the argument value does not match the condition.
   * 
   * @param <T> The class of the argument array
   * @param objects Any object, {@code null} is acceptable.
   * @return the argument
   */
  @Nonnull
  public static <T> T[] requireElementsNonDuplicated(@RequireElementNonnull T[] objects) {
    requireElementsNonDuplicated(Arrays.asList(objects));

    return objects;
  }

  /**
   * Validates elements of a collection is not {@code null} 
   *     and throws {@code RequireElementNonNullException} 
   *     if the argument value does not match the condition.
   * 
   * @param <T> The class of the argument collection
   * @param collection Any collection, {@code null} is acceptable.
   * @return the argument
   */
  @Nonnull
  public static <T> Collection<T> requireElementsNonDuplicated(
      @RequireElementNonnull Collection<T> collection) {

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
  public abstract static class ObjectsUtilException extends EclibRuntimeException {

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
