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
  // formatWithArgs
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("formatWithArgs")
  class SubstituteNamedPlaceholders {

    @Test
    @DisplayName("replaces {key} with corresponding value from map")
    void basicSubstitution() {
      Map<@NonNull String, @Nullable Object> map = Map.of("min", 3, "max", 20);
      assertThat(PropertiesFileUtilFormatter.formatWithArgs(
          "{min} 以上 {max} 以下", map))
          .isEqualTo("3 以上 20 以下");
    }

    @Test
    @DisplayName("null value in map: replaced with empty string literal ''")
    void nullValue() {
      Map<@NonNull String, @Nullable Object> map = new HashMap<>();
      map.put("val", null);
      assertThat(PropertiesFileUtilFormatter.formatWithArgs("{val}", map))
          .isEqualTo("''");
    }

    @Test
    @DisplayName("braces in value are escaped to prevent MessageFormat errors")
    void bracesInValueAreEscaped() {
      Map<@NonNull String, @Nullable Object> map = Map.of("key", "{escaped}");
      assertThat(PropertiesFileUtilFormatter.formatWithArgs("{key}", map))
          .isEqualTo("'{'escaped'}'");
    }

    @Test
    @DisplayName("placeholder not in map: left unchanged")
    void placeholderNotInMap() {
      Map<@NonNull String, @Nullable Object> map = Map.of("other", "value");
      assertThat(PropertiesFileUtilFormatter.formatWithArgs("{missing}", map))
          .isEqualTo("{missing}");
    }

    @Test
    @DisplayName("no placeholders: returns message unchanged")
    void noPlaceholders() {
      assertThat(PropertiesFileUtilFormatter.formatWithArgs(
          "no placeholders here", Map.of()))
          .isEqualTo("no placeholders here");
    }
  }
}
