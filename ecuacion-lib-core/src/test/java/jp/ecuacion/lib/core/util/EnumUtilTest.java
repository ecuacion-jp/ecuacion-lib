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
  // getListForHtmlSelect
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getListForHtmlSelect")
  class GetListForHtmlSelect {

    @Test
    @DisplayName("no options returns all elements")
    void noOptions() {
      var list = EnumUtil.getListForHtmlSelect(TestEnum.class, null, null);
      assertThat(list).hasSize(3);
      assertThat(list.get(0)[0]).isEqualTo("A");
      assertThat(list.get(1)[0]).isEqualTo("B");
      assertThat(list.get(2)[0]).isEqualTo("C");
    }

    @Test
    @DisplayName("including filters to specified names")
    void including() {
      var list = EnumUtil.getListForHtmlSelect(TestEnum.class, null, "including=VALUE_A|VALUE_C");
      assertThat(list).hasSize(2);
      assertThat(list.get(0)[0]).isEqualTo("A");
      assertThat(list.get(1)[0]).isEqualTo("C");
    }

    @Test
    @DisplayName("excluding filters out specified names")
    void excluding() {
      var list = EnumUtil.getListForHtmlSelect(TestEnum.class, null, "excluding=VALUE_B");
      assertThat(list).hasSize(2);
      assertThat(list.get(0)[0]).isEqualTo("A");
      assertThat(list.get(1)[0]).isEqualTo("C");
    }

    @Test
    @DisplayName("firstCharOfCodeEqualTo filters by first char of code")
    void firstCharOfCodeEqualTo() {
      var list =
          EnumUtil.getListForHtmlSelect(TestEnum.class, null, "firstCharOfCodeEqualTo=A|C");
      assertThat(list).hasSize(2);
      assertThat(list.get(0)[0]).isEqualTo("A");
      assertThat(list.get(1)[0]).isEqualTo("C");
    }

    @Test
    @DisplayName("firstCharOfCodeLessThanOrEqualTo filters correctly")
    void firstCharOfCodeLessThanOrEqualTo() {
      var list = EnumUtil.getListForHtmlSelect(
          TestEnum.class, null, "firstCharOfCodeLessThanOrEqualTo=B");
      assertThat(list).hasSize(2);
      assertThat(list.get(0)[0]).isEqualTo("A");
      assertThat(list.get(1)[0]).isEqualTo("B");
    }

    @Test
    @DisplayName("firstCharOfCodeGreaterThanOrEqualTo filters correctly")
    void firstCharOfCodeGreaterThanOrEqualTo() {
      var list = EnumUtil.getListForHtmlSelect(
          TestEnum.class, null, "firstCharOfCodeGreaterThanOrEqualTo=B");
      assertThat(list).hasSize(2);
      assertThat(list.get(0)[0]).isEqualTo("B");
      assertThat(list.get(1)[0]).isEqualTo("C");
    }

    @Test
    @DisplayName("multiple options throws RuntimeException")
    void multipleOptionsThrows() {
      assertThatThrownBy(() -> EnumUtil.getListForHtmlSelect(
          TestEnum.class, null, "including=VALUE_A,excluding=VALUE_B"))
          .isInstanceOf(RuntimeException.class);
    }
  }

  // -------------------------------------------------------------------------
  // Test enum (requires getCode() and getDisplayName(Locale) by EnumUtil contract)
  // -------------------------------------------------------------------------

  public enum TestEnum {
    VALUE_A("A"), VALUE_B("B"), VALUE_C("C");

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
