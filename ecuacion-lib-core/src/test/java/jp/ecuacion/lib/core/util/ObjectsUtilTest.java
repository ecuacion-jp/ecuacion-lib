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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireElementNonEmptyException;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireElementNonNullException;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireElementsNonDuplicatedException;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireNonEmptyException;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireNonNullException;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireSizeNonZeroException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link ObjectsUtil}. */
@DisplayName("ObjectsUtil")
public class ObjectsUtilTest {

  // -------------------------------------------------------------------------
  // requireNonNull (single)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("requireNonNull (single)")
  class RequireNonNullSingle {

    @Test
    @DisplayName("non-null value is returned as-is")
    void nonNullReturns() {
      assertThat(ObjectsUtil.requireNonNull("value")).isEqualTo("value");
      assertThat(ObjectsUtil.requireNonNull(42)).isEqualTo(42);
    }

    @Test
    @DisplayName("null throws RequireNonNullException")
    void nullThrows() {
      assertThatThrownBy(() -> ObjectsUtil.requireNonNull(null))
          .isInstanceOf(RequireNonNullException.class);
    }
  }

  // -------------------------------------------------------------------------
  // requireNonNull (multiple)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("requireNonNull (multiple)")
  class RequireNonNullMultiple {

    @Test
    @DisplayName("all non-null passes without throwing")
    void allNonNullPasses() {
      ObjectsUtil.requireNonNull("a", "b", "c");
    }

    @Test
    @DisplayName("any null throws RequireNonNullException")
    void anyNullThrows() {
      assertThatThrownBy(() -> ObjectsUtil.requireNonNull("a", null, "c"))
          .isInstanceOf(RequireNonNullException.class);
      assertThatThrownBy(() -> ObjectsUtil.requireNonNull(null, "b", "c"))
          .isInstanceOf(RequireNonNullException.class);
    }
  }

  // -------------------------------------------------------------------------
  // requireNonEmpty (single)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("requireNonEmpty (single)")
  class RequireNonEmptySingle {

    @Test
    @DisplayName("non-empty string is returned as-is")
    void nonEmptyReturns() {
      assertThat(ObjectsUtil.requireNonEmpty("value")).isEqualTo("value");
    }

    @Test
    @DisplayName("null throws RequireNonEmptyException")
    void nullThrows() {
      assertThatThrownBy(() -> ObjectsUtil.requireNonEmpty(null))
          .isInstanceOf(RequireNonEmptyException.class);
    }

    @Test
    @DisplayName("empty string throws RequireNonEmptyException")
    void emptyThrows() {
      assertThatThrownBy(() -> ObjectsUtil.requireNonEmpty(""))
          .isInstanceOf(RequireNonEmptyException.class);
    }
  }

  // -------------------------------------------------------------------------
  // requireNonEmpty (multiple)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("requireNonEmpty (multiple)")
  class RequireNonEmptyMultiple {

    @Test
    @DisplayName("all non-empty passes without throwing")
    void allNonEmptyPasses() {
      ObjectsUtil.requireNonEmpty("a", "b", "c");
    }

    @Test
    @DisplayName("any null or empty throws RequireNonEmptyException")
    void anyNullOrEmptyThrows() {
      assertThatThrownBy(() -> ObjectsUtil.requireNonEmpty("a", null, "c"))
          .isInstanceOf(RequireNonEmptyException.class);
      assertThatThrownBy(() -> ObjectsUtil.requireNonEmpty("a", "", "c"))
          .isInstanceOf(RequireNonEmptyException.class);
    }
  }

  // -------------------------------------------------------------------------
  // requireSizeNonZero
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("requireSizeNonZero")
  class RequireSizeNonZero {

    @Test
    @DisplayName("non-empty array/collection is returned as-is")
    void nonEmptyPasses() {
      String[] arr = {"a", "b"};
      assertThat(ObjectsUtil.requireSizeNonZero(arr)).isEqualTo(arr);

      List<String> list = List.of("a");
      assertThat(ObjectsUtil.requireSizeNonZero(list)).isEqualTo(list);
    }

    @Test
    @DisplayName("empty array throws RequireSizeNonZeroException")
    void emptyArrayThrows() {
      assertThatThrownBy(() -> ObjectsUtil.requireSizeNonZero(new String[]{}))
          .isInstanceOf(RequireSizeNonZeroException.class);
    }

    @Test
    @DisplayName("empty collection throws RequireSizeNonZeroException")
    void emptyCollectionThrows() {
      assertThatThrownBy(() -> ObjectsUtil.requireSizeNonZero(List.of()))
          .isInstanceOf(RequireSizeNonZeroException.class);
    }
  }

  // -------------------------------------------------------------------------
  // requireElementNonNull
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("requireElementNonNull")
  class RequireElementNonNull {

    @Test
    @DisplayName("array with no null elements is returned as-is")
    void noNullElementPasses() {
      String[] arr = {"a", "b"};
      assertThat(ObjectsUtil.requireElementNonNull(arr)).isEqualTo(arr);
    }

    @Test
    @DisplayName("array containing null element throws RequireElementNonNullException")
    void nullElementThrows() {
      assertThatThrownBy(() -> ObjectsUtil.requireElementNonNull(new String[]{"a", null}))
          .isInstanceOf(RequireElementNonNullException.class);
    }

    @Test
    @DisplayName("collection containing null element throws RequireElementNonNullException")
    void nullElementInCollectionThrows() {
      assertThatThrownBy(
          () -> ObjectsUtil.requireElementNonNull(Arrays.asList("a", null)))
          .isInstanceOf(RequireElementNonNullException.class);
    }
  }

  // -------------------------------------------------------------------------
  // requireElementNonEmpty
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("requireElementNonEmpty")
  class RequireElementNonEmpty {

    @Test
    @DisplayName("array with no empty elements is returned as-is")
    void noEmptyElementPasses() {
      String[] arr = {"a", "b"};
      assertThat(ObjectsUtil.requireElementNonEmpty(arr)).isEqualTo(arr);
    }

    @Test
    @DisplayName("array containing null element throws RequireElementNonEmptyException")
    void nullElementThrows() {
      assertThatThrownBy(() -> ObjectsUtil.requireElementNonEmpty(new String[]{"a", null}))
          .isInstanceOf(RequireElementNonEmptyException.class);
    }

    @Test
    @DisplayName("array containing empty string throws RequireElementNonEmptyException")
    void emptyStringElementThrows() {
      assertThatThrownBy(() -> ObjectsUtil.requireElementNonEmpty(new String[]{"a", ""}))
          .isInstanceOf(RequireElementNonEmptyException.class);
    }
  }

  // -------------------------------------------------------------------------
  // requireElementsNonDuplicated
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("requireElementsNonDuplicated")
  class RequireElementsNonDuplicated {

    @Test
    @DisplayName("array with no duplicates is returned as-is")
    void noDuplicatePasses() {
      String[] arr = {"a", "b", "c"};
      assertThat(ObjectsUtil.requireElementsNonDuplicated(arr)).isEqualTo(arr);
    }

    @Test
    @DisplayName("array containing duplicate elements throws RequireElementsNonDuplicatedException")
    void duplicateThrows() {
      assertThatThrownBy(() -> ObjectsUtil.requireElementsNonDuplicated(new String[]{"a", "b", "a"}))
          .isInstanceOf(RequireElementsNonDuplicatedException.class);
    }

    @Test
    @DisplayName("collection containing duplicate throws RequireElementsNonDuplicatedException")
    void duplicateInCollectionThrows() {
      assertThatThrownBy(
          () -> ObjectsUtil.requireElementsNonDuplicated(List.of("a", "b", "a")))
          .isInstanceOf(RequireElementsNonDuplicatedException.class);
    }
  }
}
