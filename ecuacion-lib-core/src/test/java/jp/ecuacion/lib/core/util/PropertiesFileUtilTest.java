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
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg.ArgKind;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilBundleReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link PropertiesFileUtil}. */
@DisplayName("PropertiesFileUtil")
public class PropertiesFileUtilTest {

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtilBundleReader.addToDynamicPostfixList("lib-core-test");
  }

  @Test
  @DisplayName("getMessage with number format arg formats the number correctly")
  public void getMessage_objectArgs_numberFormat() {
    String result = PropertiesFileUtil.getMessage(Locale.ENGLISH, "MSG_WITH_NUMBER_FORMAT",
        new Object[] {1234567});
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
    @DisplayName("plain Object substituted into {0} placeholder")
    void plainObject() {
      assertThat(
          PropertiesFileUtil.getMessage(Locale.ENGLISH, "MSG_WITH_STRING_ARG", "hello"))
              .isEqualTo("value=hello");
    }

    @Test
    @DisplayName("Arg.message resolves message ID then substitutes")
    void argMessage() {
      Arg[] args = new Arg[] {Arg.message("MSG1")};
      assertThat(
          PropertiesFileUtil.getMessage(Locale.ENGLISH, "MSG_WITH_STRING_ARG", (Object[]) args))
              .isEqualTo("value=message 1.");
    }
  }

  // -------------------------------------------------------------------------
  // Arg#resolveAsString
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Arg#resolveAsString")
  class ResolveAsString {

    @Test
    @DisplayName("MESSAGE_ID kind: resolves message from properties")
    void messageId() {
      assertThat(Arg.message("MSG1").resolveAsString(Locale.ENGLISH)).isEqualTo("message 1.");
    }

    @Test
    @DisplayName("FORMATTED_STRING kind: substitutes args into format string")
    void formattedString() {
      assertThat(Arg.formattedString("Hello {0}!", "world")
          .resolveAsString(Locale.ENGLISH)).isEqualTo("Hello world!");
    }

    @Test
    @DisplayName("FORMATTED_STRING with message arg: resolves inner message then formats")
    void formattedStringWithMessageArg() {
      assertThat(Arg.formattedString("value is {0}", Arg.message("MSG1"))
          .resolveAsString(Locale.ENGLISH)).isEqualTo("value is message 1.");
    }
  }

  // -------------------------------------------------------------------------
  // getConstant / hasConstant
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getConstant / hasConstant")
  class Constants {

    @Test
    @DisplayName("getConstant: returns value from constants.properties")
    void getConstant() {
      assertThat(PropertiesFileUtil.getConstant("TEST_CONSTANTS_KEY"))
          .isEqualTo("test constants value");
    }

    @Test
    @DisplayName("hasConstant: true for existing key")
    void hasConstant_existing() {
      assertThat(PropertiesFileUtil.hasConstant("TEST_CONSTANTS_KEY")).isTrue();
    }

    @Test
    @DisplayName("hasConstant: false for non-existing key")
    void hasConstant_missing() {
      assertThat(PropertiesFileUtil.hasConstant("NO_SUCH_CONSTANTS_KEY")).isFalse();
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
      assertThat(PropertiesFileUtil.getEnumName("TEST_ENUM.A")).isEqualTo("Enum Value A");
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
      assertThat(Arg.message("x").getArgKind()).isEqualTo(ArgKind.MESSAGE_ID);
      assertThat(Arg.formattedString("x").getArgKind()).isEqualTo(ArgKind.FORMATTED_STRING);
    }

    @Test
    @DisplayName("formattedString(String): no-arg version returns the string as-is")
    void formattedStringNoArgs() {
      Arg arg = Arg.formattedString("plain text");
      assertThat(arg.resolveAsString(Locale.ENGLISH))
          .isEqualTo("plain text");
    }

    @Test
    @DisplayName("message(String, String[]): resolves with string args")
    void messageWithStringArgs() {
      Arg arg = Arg.message("MSG_WITH_STRING_ARG", "hello");
      assertThat(arg.resolveAsString(Locale.ENGLISH))
          .isEqualTo("value=hello");
    }

    @Test
    @DisplayName("message(String, Object...): resolves with plain Object arg")
    void messageWithArgArray() {
      Arg arg = Arg.message("MSG_WITH_STRING_ARG", "world");
      assertThat(arg.resolveAsString(Locale.ENGLISH))
          .isEqualTo("value=world");
    }

    @Test
    @DisplayName("get(Enum[], String): resolves message from specified file kinds")
    void getWithFileKinds() {
      Arg arg = Arg.fromFileKinds(
          new PropertiesFileUtilFileKindEnum[] {PropertiesFileUtilFileKindEnum.MESSAGES}, "MSG1");
      assertThat(arg.resolveAsString(Locale.ENGLISH))
          .isEqualTo("message 1.");
    }

    @Test
    @DisplayName("get(Enum[], String, String[]): resolves with string args")
    void getWithFileKindsAndStringArgs() {
      Arg arg =
          Arg.fromFileKinds(new PropertiesFileUtilFileKindEnum[] {PropertiesFileUtilFileKindEnum.MESSAGES},
              "MSG_WITH_STRING_ARG", "test");
      assertThat(arg.resolveAsString(Locale.ENGLISH))
          .isEqualTo("value=test");
    }

    @Test
    @DisplayName("get(Enum[], String, Object...): resolves with plain Object arg")
    void getWithFileKindsAndArgArray() {
      Arg arg =
          Arg.fromFileKinds(new PropertiesFileUtilFileKindEnum[] {PropertiesFileUtilFileKindEnum.MESSAGES},
              "MSG_WITH_STRING_ARG", "ok");
      assertThat(arg.resolveAsString(Locale.ENGLISH)).isEqualTo("value=ok");
    }
  }
}
