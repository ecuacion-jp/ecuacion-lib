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
import java.util.List;
import java.util.Locale;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilValueGetter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link MessageUtil}. */
@DisplayName("MessageUtil")
public class MessageUtilTest {

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtilValueGetter.addToDynamicPostfixList("lib-core-test");
  }

  @Nested
  @DisplayName("getValuesOfFormattedString")
  class GetValuesOfFormattedString {

    @Test
    @DisplayName("array: returns string containing each value")
    void array() {
      String result = MessageUtil.getValuesOfFormattedString(new String[]{"hello", "world"});
      assertThat(result).contains("hello");
      assertThat(result).contains("world");
    }

    @Test
    @DisplayName("list: delegates to array variant and returns same result")
    void list() {
      String arrayResult = MessageUtil.getValuesOfFormattedString(new String[]{"a", "b"});
      String listResult = MessageUtil.getValuesOfFormattedString(List.of("a", "b"));
      assertThat(listResult).isEqualTo(arrayResult);
    }
  }

  @Nested
  @DisplayName("getValuesArg")
  class GetValuesArg {

    @Test
    @DisplayName("array: returns FORMATTED_STRING Arg that resolves to value with symbols")
    void array() {
      Arg arg = MessageUtil.getValuesArg(new String[]{"MSG1"});
      assertThat(arg).isNotNull();
      assertThat(arg.getArgKind()).isEqualTo(PropertiesFileUtil.ArgKind.FORMATTED_STRING);
      String resolved = PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, arg);
      assertThat(resolved).contains("message 1.");
    }

    @Test
    @DisplayName("list: delegates to array variant and produces same result")
    void list() {
      Arg arrayArg = MessageUtil.getValuesArg(new String[]{"MSG1"});
      Arg listArg = MessageUtil.getValuesArg(List.of("MSG1"));
      String arrayResolved = PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, arrayArg);
      String listResolved = PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, listArg);
      assertThat(listResolved).isEqualTo(arrayResolved);
    }
  }
}
