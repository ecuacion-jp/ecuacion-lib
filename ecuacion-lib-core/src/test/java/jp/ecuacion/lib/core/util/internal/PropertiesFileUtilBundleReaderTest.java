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

import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.APPLICATION;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.MESSAGES;
import static jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum.VALIDATION_MESSAGES;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.Objects;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilBundleReader.KeyDuplicatedException;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilBundleReader.NoKeyInPropertiesFileException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link PropertiesFileUtilBundleReader}. */
@DisplayName("PropertiesFileUtilBundleReader")
public class PropertiesFileUtilBundleReaderTest {

  private @Nullable PropertiesFileUtilBundleReader obj;

  private static final PropertiesFileUtilBundleReader OBJ_APP =
      new PropertiesFileUtilBundleReader(APPLICATION);
  private static final PropertiesFileUtilBundleReader OBJ_MSG =
      new PropertiesFileUtilBundleReader(MESSAGES);
  @SuppressWarnings("null")
  private static final Class<NoKeyInPropertiesFileException> NO_KEY_EX =
      NoKeyInPropertiesFileException.class;

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtilBundleReader.addToDynamicPostfixList("lib-core-test");
    PropertiesFileUtilBundleReader.addToDynamicPostfixList("lib-core-2nd-test");
  }

  @Test
  @DisplayName("constructor accepts FileKindEnum and String[][] argument")
  public void constructor_test() {
    // argument: PropertiesFileUtilFileKindEnum
    assertThat(OBJ_MSG.getProp(Locale.CANADA, "TEST_KEY"))
        .isEqualTo("TEST_VALUE");

    // argument: String[][]
    obj = new PropertiesFileUtilBundleReader(new String[][]{new String[]{"messages"}});
    assertThat(Objects.requireNonNull(obj).getProp(Locale.CANADA, "TEST_KEY"))
        .isEqualTo("TEST_VALUE");
  }

  @Test
  @DisplayName("hasProp returns false for missing file or key, true when key exists")
  public void hasProp_basicTest() {
    // file not exist
    obj = new PropertiesFileUtilBundleReader(new String[][]{new String[]{"non-exist-file"}});
    assertThat(Objects.requireNonNull(obj).hasProp("testkey")).isFalse();
    // key not exist
    assertThat(OBJ_MSG.hasProp("non-exist-key")).isFalse();
    // key exists
    assertThat(OBJ_MSG.hasProp("TEST_KEY")).isTrue();
  }

  @Test
  @DisplayName("getProp returns value or throws when key/file is missing")
  public void getProp_basicTest() {
    assertThat(OBJ_APP.getProp("TEST_KEY")).isEqualTo("TEST_APP");

    // file not exist
    obj = new PropertiesFileUtilBundleReader(new String[][]{new String[]{"non-exist-file"}});
    Assertions.assertThrows(NO_KEY_EX, () ->
        Objects.requireNonNull(obj).getProp("testkey"));

    // key not exist : throwsExceptionWhenKeyDoesNotExist = true (APP)
    Assertions.assertThrows(NO_KEY_EX, () -> OBJ_APP.getProp("non-exist-key"));

    // key exist
    assertThat(OBJ_APP.getProp("TEST_KEY")).isEqualTo("TEST_APP");

    // locale is null
    assertThat(OBJ_APP.getProp(null, "TEST_KEY")).isEqualTo("TEST_APP");
    // locale is not null
    assertThat(OBJ_APP.getProp(Locale.JAPAN, "TEST_KEY")).isEqualTo("TEST_APP");
  }

  public void duplicatedKeyTest() {
    // By the specification of "ResourceBundle", it's not an error.
    PropertiesFileUtilBundleReader store = new PropertiesFileUtilBundleReader(
        new String[][]{new String[]{"propsLocale-duplicate-in-one-file"}});
    store.getProp("KEY2");
  }

  @Test
  @DisplayName("locale resolution falls back correctly through file hierarchy")
  public void localeTest() {

    // properties file: non-locale only

    PropertiesFileUtilBundleReader none =
        new PropertiesFileUtilBundleReader(new String[][]{new String[]{"propsLocale-none"}});
    assertThat(none.getProp(Locale.ROOT, "TEST_KEY")).isEqualTo("TEST_VALUE");
    assertThat(none.getProp(Locale.JAPANESE, "TEST_KEY")).isEqualTo("TEST_VALUE");
    assertThat(none.getProp(Locale.JAPAN, "TEST_KEY")).isEqualTo("TEST_VALUE");

    // properties file: lang

    PropertiesFileUtilBundleReader lang =
        new PropertiesFileUtilBundleReader(new String[][]{new String[]{"propsLocale-lang"}});
    assertThat(none.getProp(Locale.ENGLISH, "TEST_KEY")).isEqualTo("TEST_VALUE");
    Assertions.assertThrows(NO_KEY_EX,
        () -> lang.getProp(Locale.JAPAN, "TEST_KEY"));

    // properties file: none-and-lang

    PropertiesFileUtilBundleReader noneAndLang =
        new PropertiesFileUtilBundleReader(new String[][]{new String[]{"propsLocale-none-and-lang"}});
    assertThat(noneAndLang.getProp(Locale.ITALY, "FILE_LOCALE")).isEqualTo("it");
    assertThat(noneAndLang.getProp(Locale.ITALIAN, "FILE_LOCALE"))
        .isEqualTo("it");
    assertThat(noneAndLang.getProp(Locale.JAPANESE, "FILE_LOCALE"))
        .isEqualTo("none");
    assertThat(noneAndLang.getProp(Locale.JAPAN, "FILE_LOCALE"))
        .isEqualTo("none");

    // properties file: none-and-langCountry

    PropertiesFileUtilBundleReader noneAndLangCountry = new PropertiesFileUtilBundleReader(
        new String[][]{new String[]{"propsLocale-none-and-lang-country_lib-core-test"}});
    assertThat(noneAndLangCountry.getProp(Locale.FRENCH, "FILE_LOCALE"))
        .isEqualTo("none");
    assertThat(noneAndLangCountry.getProp(Locale.JAPANESE, "FILE_LOCALE"))
        .isEqualTo("none");
    assertThat(noneAndLangCountry.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE"))
        .isEqualTo("fr_CA");
    assertThat(noneAndLangCountry.getProp(Locale.FRANCE, "FILE_LOCALE"))
        .isEqualTo("none");
    assertThat(noneAndLangCountry.getProp(Locale.CANADA, "FILE_LOCALE"))
        .isEqualTo("none");
    assertThat(noneAndLangCountry.getProp(Locale.JAPAN, "FILE_LOCALE"))
        .isEqualTo("none");

    // properties file: none-and-lang-and-langCountry

    PropertiesFileUtilBundleReader noneAndLangAndLangCountry = new PropertiesFileUtilBundleReader(
        new String[][]{new String[]{"propsLocale-none-and-lang-and-lang-country"}});
    assertThat(noneAndLangAndLangCountry.getProp(Locale.FRENCH, "FILE_LOCALE"))
        .isEqualTo("fr");
    assertThat(noneAndLangAndLangCountry.getProp(Locale.JAPANESE, "FILE_LOCALE"))
        .isEqualTo("none");
    assertThat(
        noneAndLangAndLangCountry.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE"))
        .isEqualTo("fr_CA");
    assertThat(noneAndLangAndLangCountry.getProp(Locale.FRANCE, "FILE_LOCALE"))
        .isEqualTo("fr");
    assertThat(noneAndLangAndLangCountry.getProp(Locale.CANADA, "FILE_LOCALE"))
        .isEqualTo("none");
    assertThat(noneAndLangAndLangCountry.getProp(Locale.JAPAN, "FILE_LOCALE"))
        .isEqualTo("none");
  }

  @Test
  @DisplayName("fileKind postfix detection covers all expected patterns")
  public void fileKindTest() {
    obj = new PropertiesFileUtilBundleReader(new String[][]{new String[]{"propsLocale-fileKind"}});
    assertThat(Objects.requireNonNull(obj).getPostfixes()).contains("_lib_core");

    obj = new PropertiesFileUtilBundleReader(new String[][]{new String[]{"propsLocale-fileKind"}});
    assertThat(Objects.requireNonNull(obj).getPostfixes()).contains("_splib_web");

    obj = new PropertiesFileUtilBundleReader(new String[][]{new String[]{"propsLocale-fileKind"}});
    assertThat(Objects.requireNonNull(obj).getPostfixes()).contains("");

    obj = new PropertiesFileUtilBundleReader(new String[][]{new String[]{"propsLocale-fileKind"}});
    assertThat(Objects.requireNonNull(obj).getPostfixes()).contains("_base");

    obj = new PropertiesFileUtilBundleReader(new String[][]{new String[]{"propsLocale-fileKind"}});
    assertThat(Objects.requireNonNull(obj).getPostfixes()).contains("-profile");

    PropertiesFileUtilBundleReader store =
        new PropertiesFileUtilBundleReader(new String[][]{new String[]{"propsLocale-fileKind"}});
    assertThat(store.getPostfixes()).contains("_core-profile");

    // inter-file key duplication throws
    PropertiesFileUtilBundleReader dup = new PropertiesFileUtilBundleReader(
        new String[][]{new String[]{"propsLocale-duplicate-in-multiple-files"}});
    Assertions.assertThrows(KeyDuplicatedException.class,
        () -> dup.getProp("KEY1"));
  }

  @Test
  @DisplayName("throwsExceptionWhenKeyDoesNotExist=false returns key string when key not found")
  public void throwsExceptionWhenKeyDoesNotExistIsFalseTest() {
    assertThat(OBJ_MSG.hasProp("non-exist-key")).isFalse();
    assertThat(OBJ_MSG.getProp("non-exist-key")).isEqualTo("non-exist-key");
  }

  @Test
  @DisplayName("property keys in obtained value are resolved recursively")
  public void resolvePropertyKeysInObtainedValueTest() {
    assertThat(OBJ_MSG.getProp("KEY_IN_MSG")).isEqualTo("Hi, John.");
  }

  @Test
  @DisplayName("three-level priority: plain key > key.default > key.base")
  public void baseKeyFallbackPriorityTest() {
    PropertiesFileUtilBundleReader getter =
        new PropertiesFileUtilBundleReader(new String[][]{new String[]{"propsLocale-none"}});
    // .base is the fallback when neither plain key nor .default exists
    assertThat(getter.getProp("BASE_ONLY_KEY")).isEqualTo("base value");
    // .default takes precedence over .base
    assertThat(getter.getProp("DEFAULT_WINS_KEY")).isEqualTo("default value");
    // plain key takes precedence over both .default and .base
    assertThat(getter.getProp("KEY_WINS_KEY")).isEqualTo("key value");
  }

  @Test
  @DisplayName("ValidationMessages: standard Jakarta constraint key delegates to HV; unknown key also delegates")
  public void validationMessagesKeyResolutionTest() {
    PropertiesFileUtilBundleReader validationGetter =
        new PropertiesFileUtilBundleReader(VALIDATION_MESSAGES);

    // Standard Jakarta constraint key: not in any ValidationMessages file (ValidationMessages_lib_core
    // was removed so HV delegation handles these). getProp returns the key string.
    String knownKey = "jakarta.validation.constraints.NotNull.message";
    assertThat(validationGetter.hasProp(knownKey)).isFalse();
    assertThat(validationGetter.getProp(knownKey)).isEqualTo(knownKey);

    // Unknown key: not in any ValidationMessages file; getProp returns the key string so that
    // HV's MessageInterpolator can resolve it downstream (HV delegation).
    String unknownKey = "some.unknown.constraint.message";
    assertThat(validationGetter.hasProp(unknownKey)).isFalse();
    assertThat(validationGetter.getProp(unknownKey)).isEqualTo(unknownKey);
  }
}
