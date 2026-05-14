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

import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.CONSTANTS;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.MESSAGES;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashMap;
import java.util.Locale;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link PropertiesFileUtilResolver}. */
@DisplayName("PropertiesFileUtilResolver")
public class PropertiesFileUtilResolverTest {

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtilBundleReader.addToDynamicPostfixList("lib-core-test");
  }

  // -------------------------------------------------------------------------
  // getProp
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getProp")
  class GetProcessedProp {

    @Test
    @DisplayName("basic key lookup returns value")
    void basicLookup() {
      assertThat(PropertiesFileUtilResolver.getProp(null, MESSAGES, "MSG1", new HashMap<>()))
          .isEqualTo("message 1.");
    }

    @Test
    @DisplayName("#{fileKind:key} cross-reference is resolved")
    void crossReferenceResolved() {
      // KEY_IN_MSG=Hi, #{messages:PERSON}. and PERSON=John
      assertThat(PropertiesFileUtilResolver.getProp(
          Locale.ENGLISH, MESSAGES, "KEY_IN_MSG", new HashMap<>()))
          .isEqualTo("Hi, John.");
    }

    @Test
    @DisplayName("missing key returns key itself (non-throwing kind)")
    void missingKeyReturnsKeyItself() {
      assertThat(PropertiesFileUtilResolver.getProp(
          null, MESSAGES, "NO_SUCH_KEY", new HashMap<>()))
          .isEqualTo("NO_SUCH_KEY");
    }

    @Test
    @DisplayName("locale affects value retrieval")
    void withLocale() {
      assertThat(PropertiesFileUtilResolver.getProp(
          Locale.ENGLISH, MESSAGES, "MSG1", new HashMap<>()))
          .isEqualTo("message 1.");
    }
  }

  // -------------------------------------------------------------------------
  // has
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("has")
  class Has {

    @Test
    @DisplayName("true for existing key")
    void existingKey() {
      assertThat(PropertiesFileUtilResolver.hasProp(MESSAGES, "MSG1")).isTrue();
    }

    @Test
    @DisplayName("false for non-existing key")
    void missingKey() {
      assertThat(PropertiesFileUtilResolver.hasProp(MESSAGES, "NO_SUCH_KEY")).isFalse();
    }

    @Test
    @DisplayName("works across different file kinds")
    void acrossFileKinds() {
      assertThat(PropertiesFileUtilResolver.hasProp(CONSTANTS, "TEST_CONSTANTS_KEY")).isTrue();
      assertThat(PropertiesFileUtilResolver.hasProp(MESSAGES, "TEST_CONSTANTS_KEY")).isFalse();
    }
  }

  // -------------------------------------------------------------------------
  // resolveArgAsObject
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("resolveArgAsObject")
  class ResolveArgAsObject {

    @Test
    @DisplayName("MESSAGE_ID kind: resolves message key to string")
    void messageIdKind() {
      assertThat(PropertiesFileUtilResolver.resolveArgAsObject(Locale.ENGLISH, Arg.message("MSG1")))
          .isEqualTo("message 1.");
    }

    @Test
    @DisplayName("MESSAGE_ID kind with multiple fileKinds: finds key in first matching kind")
    void messageIdKindMultipleFileKinds() {
      Arg arg = Arg.fromFileKinds(
          new PropertiesFileUtilFileKindEnum[] {MESSAGES, CONSTANTS}, "MSG1");
      assertThat(PropertiesFileUtilResolver.resolveArgAsObject(Locale.ENGLISH, arg))
          .isEqualTo("message 1.");
    }

    @Test
    @DisplayName("FORMATTED_STRING kind: formats with nested args")
    void formattedStringKind() {
      Arg arg = Arg.formattedString("{0} / {1}", "A", "B");
      assertThat(PropertiesFileUtilResolver.resolveArgAsObject(null, arg))
          .isEqualTo("A / B");
    }

    @Test
    @DisplayName("FORMATTED_STRING kind with MESSAGE_ID nested arg: resolves inner message")
    void formattedStringWithMessageId() {
      Arg arg = Arg.formattedString("value={0}", Arg.message("MSG1"));
      assertThat(PropertiesFileUtilResolver.resolveArgAsObject(Locale.ENGLISH, arg))
          .isEqualTo("value=message 1.");
    }
  }

  // -------------------------------------------------------------------------
  // resolveArgElements
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("resolveArgElements")
  class ResolveArgElements {

    @Test
    @DisplayName("plain Objects are passed through unchanged")
    void plainObjects() {
      Object[] result = PropertiesFileUtilResolver.resolveArgElements(
          null, new Object[] {"hello", 42});
      assertThat(result).containsExactly("hello", 42);
    }

    @Test
    @DisplayName("Arg instances are resolved, plain Objects unchanged")
    void mixedArray() {
      Object[] result = PropertiesFileUtilResolver.resolveArgElements(
          Locale.ENGLISH, new Object[] {Arg.message("MSG1"), "plain", 99});
      assertThat(result).containsExactly("message 1.", "plain", 99);
    }

    @Test
    @DisplayName("empty array returns empty array")
    void emptyArray() {
      assertThat(PropertiesFileUtilResolver.resolveArgElements(null, new Object[0])).isEmpty();
    }
  }

  // -------------------------------------------------------------------------
  // analyzedValueString
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("analyzedValueString")
  class AnalyzedValueString {

    @Test
    @DisplayName("string without #{} or ${}: returned as-is")
    void noSyntax() {
      assertThat(PropertiesFileUtilResolver.analyzedValueString(
          null, "plain string", new HashMap<>()))
          .isEqualTo("plain string");
    }

    @Test
    @DisplayName("#{fileKind:key} resolves to value from that file kind")
    void fileKindKeySyntax() {
      assertThat(PropertiesFileUtilResolver.analyzedValueString(
          Locale.ENGLISH, "Hello, #{messages:MSG1}", new HashMap<>()))
          .isEqualTo("Hello, message 1.");
    }

    @Test
    @DisplayName("#{key} searches across default file kinds")
    void keyOnlySyntax() {
      assertThat(PropertiesFileUtilResolver.analyzedValueString(
          Locale.ENGLISH, "Hello, #{MSG1}", new HashMap<>()))
          .isEqualTo("Hello, message 1.");
    }

    @Test
    @DisplayName("${expr} EL expression is evaluated")
    void elExpression() {
      assertThat(PropertiesFileUtilResolver.analyzedValueString(
          null, "result: ${1 + 1}", new HashMap<>()))
          .isEqualTo("result: 2");
    }
  }
}
