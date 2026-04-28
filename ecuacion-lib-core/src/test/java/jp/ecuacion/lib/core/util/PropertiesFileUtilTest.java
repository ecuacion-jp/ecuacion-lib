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
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.ArgKind;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilValueGetter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link PropertiesFileUtil}. */
@DisplayName("PropertiesFileUtil")
public class PropertiesFileUtilTest {

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtilValueGetter.addToDynamicPostfixList("lib-core-test");
  }

  @Test
  @DisplayName("getMessage with number format arg formats the number correctly")
  public void getMessage_objectArgs_numberFormat() {
    String result = PropertiesFileUtil.getMessage(Locale.ENGLISH, "MSG_WITH_NUMBER_FORMAT",
        new Object[]{1234567});
    assertThat(result).isEqualTo("formatted: 1,234,567");
  }

  // -------------------------------------------------------------------------
  // application.properties
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getApplication / hasApplication / getApplicationOrElse")
  class Application {

    @Test
    @DisplayName("getApplication: returns value for existing key")
    void getApplication_existingKey() {
      assertThat(PropertiesFileUtil.getApplication("TEST_KEY")).isEqualTo("TEST_APP");
    }

    @Test
    @DisplayName("getApplication: throws when key does not exist")
    void getApplication_missingKey() {
      assertThatThrownBy(() -> PropertiesFileUtil.getApplication("NO_SUCH_KEY"))
          .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("hasApplication: true for existing key")
    void hasApplication_existing() {
      assertThat(PropertiesFileUtil.hasApplication("TEST_KEY")).isTrue();
    }

    @Test
    @DisplayName("hasApplication: false for non-existing key")
    void hasApplication_missing() {
      assertThat(PropertiesFileUtil.hasApplication("NO_SUCH_KEY")).isFalse();
    }

    @Test
    @DisplayName("getApplicationOrElse: returns value when key exists")
    void getApplicationOrElse_existing() {
      assertThat(PropertiesFileUtil.getApplicationOrElse("TEST_KEY", "default"))
          .isEqualTo("TEST_APP");
    }

    @Test
    @DisplayName("getApplicationOrElse: returns default when key does not exist")
    void getApplicationOrElse_missing() {
      assertThat(PropertiesFileUtil.getApplicationOrElse("NO_SUCH_KEY", "default"))
          .isEqualTo("default");
    }

  }

  // -------------------------------------------------------------------------
  // getMessage with Arg[]
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getMessage with Arg[]")
  class GetMessageWithArgArray {

    @Test
    @DisplayName("Arg.string substituted into {0} placeholder")
    void argString() {
      Arg[] args = new Arg[]{Arg.string("hello")};
      assertThat(PropertiesFileUtil.getMessage(Locale.ENGLISH, "MSG_WITH_STRING_ARG", args))
          .isEqualTo("value=hello");
    }

    @Test
    @DisplayName("Arg.message resolves message ID then substitutes")
    void argMessage() {
      Arg[] args = new Arg[]{Arg.message("MSG1")};
      assertThat(PropertiesFileUtil.getMessage(Locale.ENGLISH, "MSG_WITH_STRING_ARG", args))
          .isEqualTo("value=message 1.");
    }
  }

  // -------------------------------------------------------------------------
  // getStringFromArg
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getStringFromArg")
  class GetStringFromArg {

    @Test
    @DisplayName("STRING kind: returns the string as-is")
    void string() {
      assertThat(PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, Arg.string("hello")))
          .isEqualTo("hello");
    }

    @Test
    @DisplayName("STRING kind: null input stored as literal string 'null'")
    void stringNull() {
      assertThat(PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, Arg.string(null)))
          .isEqualTo("null");
    }

    @Test
    @DisplayName("MESSAGE_ID kind: resolves message from properties")
    void messageId() {
      assertThat(PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, Arg.message("MSG1")))
          .isEqualTo("message 1.");
    }

    @Test
    @DisplayName("FORMATTED_STRING kind: substitutes args into format string")
    void formattedString() {
      Arg formatted = Arg.formattedString("Hello {0}!", Arg.string("world"));
      assertThat(PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, formatted))
          .isEqualTo("Hello world!");
    }

    @Test
    @DisplayName("FORMATTED_STRING with message arg: resolves inner message then formats")
    void formattedStringWithMessageArg() {
      Arg formatted = Arg.formattedString("value is {0}", Arg.message("MSG1"));
      assertThat(PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, formatted))
          .isEqualTo("value is message 1.");
    }
  }

  // -------------------------------------------------------------------------
  // getString / hasString
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getString / hasString")
  class Strings {

    @Test
    @DisplayName("getString: returns value from strings.properties")
    void getString() {
      assertThat(PropertiesFileUtil.getString("TEST_STRINGS_KEY"))
          .isEqualTo("test strings value");
    }

    @Test
    @DisplayName("hasString: true for existing key")
    void hasString_existing() {
      assertThat(PropertiesFileUtil.hasString("TEST_STRINGS_KEY")).isTrue();
    }

    @Test
    @DisplayName("hasString: false for non-existing key")
    void hasString_missing() {
      assertThat(PropertiesFileUtil.hasString("NO_SUCH_STRINGS_KEY")).isFalse();
    }
  }

  // -------------------------------------------------------------------------
  // getItemName / hasItemName
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getItemName / hasItemName")
  class ItemNames {

    @Test
    @DisplayName("getItemName: finds key falling back to messages.properties")
    void getItemName() {
      assertThat(PropertiesFileUtil.getItemName(Locale.ENGLISH, "singleLayer.field"))
          .isEqualTo("test field");
    }

    @Test
    @DisplayName("hasItemName: true for key existing in messages fallback")
    void hasItemName_existing() {
      assertThat(PropertiesFileUtil.hasItemName("singleLayer.field")).isTrue();
    }

    @Test
    @DisplayName("hasItemName: false for non-existing key")
    void hasItemName_missing() {
      assertThat(PropertiesFileUtil.hasItemName("NO_SUCH_ITEM_KEY")).isFalse();
    }
  }

  // -------------------------------------------------------------------------
  // getEnumName / hasEnumName
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getEnumName / hasEnumName")
  class EnumNames {

    @Test
    @DisplayName("getEnumName: returns value from enum_names.properties")
    void getEnumName() {
      assertThat(PropertiesFileUtil.getEnumName(null, "TEST_ENUM.A"))
          .isEqualTo("Enum Value A");
    }

    @Test
    @DisplayName("hasEnumName: true for existing key")
    void hasEnumName_existing() {
      assertThat(PropertiesFileUtil.hasEnumName("TEST_ENUM.A")).isTrue();
    }

    @Test
    @DisplayName("hasEnumName: false for non-existing key")
    void hasEnumName_missing() {
      assertThat(PropertiesFileUtil.hasEnumName("NO_SUCH_ENUM_KEY")).isFalse();
    }
  }

  // -------------------------------------------------------------------------
  // get(fileKind, ...) / has(fileKind, ...)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("get and has with fileKind string")
  class AbstractProperty {

    @Test
    @DisplayName("get(fileKind, key): returns value for messages kind")
    void get_messages() {
      assertThat(PropertiesFileUtil.get("messages", "MSG1")).isEqualTo("message 1.");
    }

    @Test
    @DisplayName("get(fileKind, locale, key): returns localized value")
    void get_messages_withLocale() {
      assertThat(PropertiesFileUtil.get("messages", Locale.ENGLISH, "MSG1"))
          .isEqualTo("message 1.");
    }

    @Test
    @DisplayName("has(fileKind, key): true for existing key")
    void has_existing() {
      assertThat(PropertiesFileUtil.has("messages", "MSG1")).isTrue();
    }

    @Test
    @DisplayName("has(fileKind, key): false for non-existing key")
    void has_missing() {
      assertThat(PropertiesFileUtil.has("messages", "NO_SUCH_KEY")).isFalse();
    }
  }

  // -------------------------------------------------------------------------
  // hasMessage
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("hasMessage")
  class HasMessage {

    @Test
    @DisplayName("true for existing key")
    void existing() {
      assertThat(PropertiesFileUtil.hasMessage("MSG1")).isTrue();
    }

    @Test
    @DisplayName("false for non-existing key")
    void missing() {
      assertThat(PropertiesFileUtil.hasMessage("NO_SUCH_KEY")).isFalse();
    }

    @Test
    @DisplayName("with locale: true for existing key")
    void existingWithLocale() {
      assertThat(PropertiesFileUtil.hasMessage(Locale.ENGLISH, "MSG1")).isTrue();
    }
  }

  // -------------------------------------------------------------------------
  // Arg factory methods
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Arg factory methods and getArgKind")
  class ArgFactories {

    @Test
    @DisplayName("getArgKind: returns correct kind for each factory")
    void getArgKind() {
      assertThat(Arg.string("x").getArgKind()).isEqualTo(ArgKind.STRING);
      assertThat(Arg.message("x").getArgKind()).isEqualTo(ArgKind.MESSAGE_ID);
      assertThat(Arg.formattedString("x").getArgKind()).isEqualTo(ArgKind.FORMATTED_STRING);
    }

    @Test
    @DisplayName("strings(String...): creates array of STRING args")
    void strings() {
      Arg[] args = Arg.strings("hello", "world");
      assertThat(args).hasSize(2);
      assertThat(args[0].getArgString()).isEqualTo("hello");
      assertThat(args[1].getArgString()).isEqualTo("world");
    }

    @Test
    @DisplayName("formattedString(String): no-arg version returns the string as-is")
    void formattedStringNoArgs() {
      Arg arg = Arg.formattedString("plain text");
      assertThat(PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, arg))
          .isEqualTo("plain text");
    }

    @Test
    @DisplayName("message(String, String[]): resolves with string args")
    void messageWithStringArgs() {
      Arg arg = Arg.message("MSG_WITH_STRING_ARG", "hello");
      assertThat(PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, arg))
          .isEqualTo("value=hello");
    }

    @Test
    @DisplayName("message(String, Arg[]): resolves with Arg args")
    void messageWithArgArray() {
      Arg arg = Arg.message("MSG_WITH_STRING_ARG", new Arg[]{Arg.string("world")});
      assertThat(PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, arg))
          .isEqualTo("value=world");
    }

    @Test
    @DisplayName("get(String[], String): resolves message from specified file kinds")
    void getWithFileKinds() {
      Arg arg = Arg.get(new String[]{"MESSAGES"}, "MSG1");
      assertThat(PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, arg))
          .isEqualTo("message 1.");
    }

    @Test
    @DisplayName("get(String[], String, String[]): resolves with string args")
    void getWithFileKindsAndStringArgs() {
      Arg arg = Arg.get(new String[]{"MESSAGES"}, "MSG_WITH_STRING_ARG", "test");
      assertThat(PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, arg))
          .isEqualTo("value=test");
    }

    @Test
    @DisplayName("get(String[], String, Arg[]): resolves with Arg args")
    void getWithFileKindsAndArgArray() {
      Arg arg =
          Arg.get(new String[]{"MESSAGES"}, "MSG_WITH_STRING_ARG", new Arg[]{Arg.string("ok")});
      assertThat(PropertiesFileUtil.getStringFromArg(Locale.ENGLISH, arg))
          .isEqualTo("value=ok");
    }
  }
}
