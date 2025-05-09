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
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import org.apache.commons.lang3.ArrayUtils;

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
  * Prevents to create an instance.
  */
  private ObjectsUtil() {}

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
    return Objects.requireNonNull(object, EclibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX
        + "ObjectsUtil#paramRequireNonNull(Object) : the argument is null.");
  }

  /**
   * Validates multiple arguments are not {@code null} and throws {@code NullPointerException} 
   * if {@code null}.
   * 
   * <p>This is used to validate multiple arguments at one time.</p>
   * 
   * @param object1 Any object
   * @param object2 Any object
   * @param objects Any objects
   */
  @Nonnull
  public static void paramRequireNonNull(@RequireNonnull Object object1,
      @RequireNonnull Object object2, @RequireNonnull Object... objects) {

    Object[] allObjects = ArrayUtils.addAll(objects, object1, object2);

    for (Object object : allObjects) {
      Objects.requireNonNull(object, EclibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX
          + "ObjectsUtil#paramRequireNonNull(Object) : the argument is null.");
    }
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
  public static <T> T returnRequireNonNull(@RequireNonnull T object) {
    return Objects.requireNonNull(object,
        EclibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX
            + "ObjectsUtil#returnRequireNonNull(Object) : the return value: "
            + object.getClass().getName() + "is null.");
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
    return Objects.requireNonNull(object,
        EclibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX
            + "ObjectsUtil#requireNonNull(Object) : the variable: " + object.getClass().getName()
            + "is null.");
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
  public static <T> T[] paramSizeNonZero(@RequireNonnull T[] objects) {
    ObjectsUtil.paramRequireNonNull(objects);

    if (objects.length == 0) {
      throw new IllegalArgumentException(EclibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX
          + "ObjectsUtil#paramSizeNonZero(T[]) : The length of the array is zero.");
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
  public static <T> Collection<T> paramSizeNonZero(@RequireNonnull Collection<T> colleciton) {
    ObjectsUtil.paramRequireNonNull(colleciton);

    if (colleciton.size() == 0) {
      throw new IllegalArgumentException(EclibCoreConstants.MSG_RUNTIME_EXCEPTION_PREFIX
          + "ObjectsUtil#paramSizeNonZero(Collection<T>) : The size of the collection is zero.");
    }

    return colleciton;
  }
}
