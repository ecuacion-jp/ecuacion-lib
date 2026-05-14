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
import java.util.Locale;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilBundleReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link MessageUtil}. */
@DisplayName("MessageUtil")
public class MessageUtilTest {

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtilBundleReader.addToDynamicPostfixList("lib-core-test");
  }

  @Nested
  @DisplayName("formatValues")
  class GetValuesOfFormattedString {

    @Test
    @DisplayName("array: returns Arg that resolves to string containing each value")
    void array() {
      Arg result = MessageUtil.formatValues(new String[]{"hello", "world"});
      String resolved = result.resolveAsString(Locale.ENGLISH);
      assertThat(resolved).contains("hello");
      assertThat(resolved).contains("world");
    }

  }

  @Nested
  @DisplayName("formatValuesWithResolution")
  class GetValuesArg {

    @Test
    @DisplayName("array: returns FORMATTED_STRING Arg that resolves to value with symbols")
    void array() {
      Arg arg = MessageUtil.formatValuesWithResolution(new String[]{"MSG1"});
      assertThat(arg).isNotNull();
      assertThat(arg.getArgKind()).isEqualTo(PropertiesFileUtil.Arg.ArgKind.FORMATTED_STRING);
      String resolved = arg.resolveAsString(Locale.ENGLISH);
      assertThat(resolved).contains("message 1.");
    }

  }
}
