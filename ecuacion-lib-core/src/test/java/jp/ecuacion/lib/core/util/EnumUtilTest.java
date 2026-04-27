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
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link EnumUtil}. */
@DisplayName("EnumUtil")
public class EnumUtilTest {

  // -------------------------------------------------------------------------
  // getEnumFromCode
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getEnumFromCode")
  class GetEnumFromCode {

    @Test
    @DisplayName("existing code returns the corresponding enum value")
    void existingCodeReturnsEnum() {
      assertThat(EnumUtil.getEnumFromCode(TestEnum.class, "A")).isEqualTo(TestEnum.VALUE_A);
      assertThat(EnumUtil.getEnumFromCode(TestEnum.class, "B")).isEqualTo(TestEnum.VALUE_B);
    }

    @Test
    @DisplayName("non-existent code throws RuntimeException")
    void nonExistentCodeThrows() {
      assertThatThrownBy(() -> EnumUtil.getEnumFromCode(TestEnum.class, "Z"))
          .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("non-enum class throws IllegalArgumentException")
    void nonEnumClassThrows() {
      assertThatThrownBy(() -> EnumUtil.getEnumFromCode(String.class, "A"))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  // -------------------------------------------------------------------------
  // hasEnumFromCode
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("hasEnumFromCode")
  class HasEnumFromCode {

    @Test
    @DisplayName("existing code returns true")
    void existingCodeReturnsTrue() {
      assertThat(EnumUtil.hasEnumFromCode(TestEnum.class, "A")).isTrue();
      assertThat(EnumUtil.hasEnumFromCode(TestEnum.class, "B")).isTrue();
    }

    @Test
    @DisplayName("non-existent code returns false")
    void nonExistentCodeReturnsFalse() {
      assertThat(EnumUtil.hasEnumFromCode(TestEnum.class, "Z")).isFalse();
    }
  }

  // -------------------------------------------------------------------------
  // Test enum (requires getCode() and getDisplayName(Locale) by EnumUtil contract)
  // -------------------------------------------------------------------------

  public enum TestEnum {
    VALUE_A("A"), VALUE_B("B");

    private final String code;

    TestEnum(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }

    public String getDisplayName(Locale locale) {
      return name();
    }
  }
}
