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
package jp.ecuacion.lib.core.util.internal;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link PropertiesFileUtilFormatter}. */
@DisplayName("PropertiesFileUtilFormatter")
public class PropertiesFileUtilFormatterTest {

  // -------------------------------------------------------------------------
  // formatWithArgs
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("formatWithArgs")
  class FormatWithArgs {

    @Test
    @DisplayName("no args: returns template as-is")
    void noArgs() {
      assertThat(PropertiesFileUtilFormatter.formatWithArgs(null, "hello world", new Object[0]))
          .isEqualTo("hello world");
    }

    @Test
    @DisplayName("positional args: substitutes {0}, {1} placeholders")
    void positionalArgs() {
      assertThat(PropertiesFileUtilFormatter.formatWithArgs(
          null, "{0} is {1}", new Object[] {"age", "30"}))
          .isEqualTo("age is 30");
    }

    @Test
    @DisplayName("number format with English locale: formats with comma separator")
    void numberFormatWithLocale() {
      assertThat(PropertiesFileUtilFormatter.formatWithArgs(
          Locale.ENGLISH, "{0,number,#,###}", new Object[] {1234567}))
          .isEqualTo("1,234,567");
    }

    @Test
    @DisplayName("null locale: treated as ROOT locale")
    void nullLocale() {
      assertThat(PropertiesFileUtilFormatter.formatWithArgs(null, "{0}", new Object[] {"test"}))
          .isEqualTo("test");
    }
  }

  // -------------------------------------------------------------------------
  // formatWithNamedArgs
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("formatWithNamedArgs")
  class FormatWithNamedArgs {

    @Test
    @DisplayName("replaces {key} with corresponding value from map")
    void basicSubstitution() {
      Map<@NonNull String, @Nullable Object> map = Map.of("min", 3, "max", 20);
      assertThat(PropertiesFileUtilFormatter.formatWithNamedArgs(
          "{min} 以上 {max} 以下", map, false))
          .isEqualTo("3 以上 20 以下");
    }

    @Test
    @DisplayName("null value in map: replaced with empty string")
    void nullValue() {
      Map<@NonNull String, @Nullable Object> map = new HashMap<>();
      map.put("val", null);
      assertThat(PropertiesFileUtilFormatter.formatWithNamedArgs("[{val}]", map, false))
          .isEqualTo("[]");
      assertThat(PropertiesFileUtilFormatter.formatWithNamedArgs("[{val}]", map, true))
          .isEqualTo("[]");
    }

    @Test
    @DisplayName("not escaped for MessageFormat: value is substituted as it is")
    void notEscaped() {
      Map<@NonNull String, @Nullable Object> map = Map.of("key", "it's {escaped}");
      assertThat(PropertiesFileUtilFormatter.formatWithNamedArgs("{key}", map, false))
          .isEqualTo("it's {escaped}");
    }

    @Test
    @DisplayName("escaped for MessageFormat: value is quoted and its ' is doubled")
    void escaped() {
      Map<@NonNull String, @Nullable Object> map = Map.of("key", "it's {escaped}");
      assertThat(PropertiesFileUtilFormatter.formatWithNamedArgs("{key}", map, true))
          .isEqualTo("'it''s {escaped}'");
    }

    @Test
    @DisplayName("escaped for MessageFormat: value without special characters is not quoted")
    void escapedWithoutSpecialCharacters() {
      Map<@NonNull String, @Nullable Object> map = Map.of("key", "plain");
      assertThat(PropertiesFileUtilFormatter.formatWithNamedArgs("{key}", map, true))
          .isEqualTo("plain");
    }

    @Test
    @DisplayName("escaped value is displayed as it is after MessageFormat")
    void escapedValueAfterMessageFormat() {
      Map<@NonNull String, @Nullable Object> map = Map.of("key", "it's {0} '{1}' ''");
      String formatted =
          PropertiesFileUtilFormatter.formatWithNamedArgs("{0}: {key}", map, true);
      assertThat(PropertiesFileUtilFormatter.formatWithArgs(null, formatted,
          new Object[] {"'item'"})).isEqualTo("'item': it's {0} '{1}' ''");
    }

    @Test
    @DisplayName("placeholder in a substituted value is not substituted again")
    void singlePass() {
      Map<@NonNull String, @Nullable Object> map = Map.of("a", "{b}", "b", "B");
      assertThat(PropertiesFileUtilFormatter.formatWithNamedArgs("{a} {b}", map, false))
          .isEqualTo("{b} B");
    }

    @Test
    @DisplayName("placeholder not in map: left unchanged")
    void placeholderNotInMap() {
      Map<@NonNull String, @Nullable Object> map = Map.of("other", "value");
      assertThat(PropertiesFileUtilFormatter.formatWithNamedArgs("{missing} {0}", map, true))
          .isEqualTo("{missing} {0}");
    }

    @Test
    @DisplayName("no placeholders: returns message unchanged")
    void noPlaceholders() {
      assertThat(PropertiesFileUtilFormatter.formatWithNamedArgs(
          "no placeholders here", Map.of(), false))
          .isEqualTo("no placeholders here");
    }
  }
}
