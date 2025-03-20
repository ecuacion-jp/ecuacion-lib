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
import java.util.Collection;
import java.util.Objects;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.constant.LibCoreConstants;

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
   * Validates the argument is not {@code null} and throws {@code NullPointerException} 
   * if {@code null}.
   * 
   * <p>This is used for arguments of the method or constructor parameters 
   *     as you can see it from the name of the method.</p>
   * 
   * @param <T> The class of the argument
   * @param object Any object
   * @return the argument
   */
  @Nonnull
  public static <T> T paramRequireNonNull(@RequireNonnull T object) {
    return Objects.requireNonNull(object, LibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX
        + "ObjectsUtil#paramRequireNonNull(Object) : the argument is null.");
  }


  /**
   * Validates the return value is not {@code null} and throws {@code NullPointerException} 
   * if {@code null}.
   * 
   * <p>This is used for return values of the method or constructor parameters 
   *     as you can see it from the name of the method.</p>
   * 
   * @param <T> The class of the argument
   * @param object Any object
   * @return the argument
   */
  @Nonnull
  public static <T> T returnRequireNonNull(@Nonnull T object) {
    return Objects.requireNonNull(object, LibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX
        + "the return value: " + object.getClass().getName() + "is null.");
  }

  /**
   * Validates the variable is not {@code null} and throws {@code NullPointerException} 
   * if {@code null}.
   * 
   * @param <T> The class of the argument
   * @param object Any object
   * @return the argument
   */
  @Nonnull
  public static <T> T requireNonNull(@RequireNonnull T object) {
    return Objects.requireNonNull(object, LibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX
        + "the variable: " + object.getClass().getName() + "is null.");
  }

  /**
   * Validates the length of the param array is not zero 
   * and throws {@code IllegalArgumentException} if zero.
   * 
   * @param <T> The class of the argument array
   * @param objects Any object
   * @return the argument
   */
  @Nonnull
  public static <T> T[] paramSizeNonZero(@Nonnull T[] objects) {
    ObjectsUtil.paramRequireNonNull(objects);

    if (objects.length == 0) {
      throw new IllegalArgumentException(
          LibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX + "The length of the array is zero.");
    }

    return objects;
  }


  /**
   * Validates the size of the para collection is not zero 
   * and throws {@code IllegalArgumentException} if zero.
   * 
   * @param <T> The class of the argument collection
   * @param colleciton Any colleciton
   * @return the argument
   */
  @Nonnull
  public static <T> Collection<T> paramSizeNonZero(@Nonnull Collection<T> colleciton) {
    ObjectsUtil.paramRequireNonNull(colleciton);

    if (colleciton.size() == 0) {
      throw new IllegalArgumentException(
          LibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX + "The size of the collection is zero.");
    }

    return colleciton;
  }
}
