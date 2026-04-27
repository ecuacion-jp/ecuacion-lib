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
import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilValueGetter.KeyDupliccatedException;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilValueGetter.NoKeyInPropertiesFileException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link PropertiesFileUtilValueGetter}. */
@DisplayName("PropertiesFileUtilValueGetter")
public class PropertiesFileUtilValueGetterTest {

  private @Nullable PropertiesFileUtilValueGetter obj;

  private static final PropertiesFileUtilValueGetter OBJ_APP =
      new PropertiesFileUtilValueGetter(APPLICATION);
  private static final PropertiesFileUtilValueGetter OBJ_MSG =
      new PropertiesFileUtilValueGetter(MESSAGES);
  @SuppressWarnings("null")
  private static final Class<NoKeyInPropertiesFileException> NO_KEY_EX =
      NoKeyInPropertiesFileException.class;

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtilValueGetter.addToDynamicPostfixList("lib-core-test");
    PropertiesFileUtilValueGetter.addToDynamicPostfixList("lib-core-2nd-test");
  }

  @Test
  @DisplayName("constructor accepts FileKindEnum and String[][] argument")
  public void constructor_test() {
    // argument: PropertiesFileUtilFileKindEnum
    assertThat(OBJ_MSG.getProp(Locale.CANADA, "TEST_KEY", new HashMap<>()))
        .isEqualTo("TEST_VALUE");

    // argument: String[][]
    obj = new PropertiesFileUtilValueGetter(new String[][]{new String[]{"messages"}});
    assertThat(Objects.requireNonNull(obj).getProp(Locale.CANADA, "TEST_KEY", new HashMap<>()))
        .isEqualTo("TEST_VALUE");
  }

  @Test
  @DisplayName("hasProp returns false for missing file or key, true when key exists")
  public void hasProp_basicTest() {
    // file not exist
    obj = new PropertiesFileUtilValueGetter(new String[][]{new String[]{"non-exist-file"}});
    assertThat(Objects.requireNonNull(obj).hasProp("testkey")).isFalse();
    // key not exist
    assertThat(OBJ_MSG.hasProp("non-exist-key")).isFalse();
    // key exists
    assertThat(OBJ_MSG.hasProp("TEST_KEY")).isTrue();
  }

  @Test
  @DisplayName("getProp returns value or throws when key/file is missing")
  public void getProp_basicTest() {
    assertThat(OBJ_APP.getProp("TEST_KEY", new HashMap<>())).isEqualTo("TEST_APP");

    // file not exist
    obj = new PropertiesFileUtilValueGetter(new String[][]{new String[]{"non-exist-file"}});
    Assertions.assertThrows(NO_KEY_EX, () ->
        Objects.requireNonNull(obj).getProp("testkey", new HashMap<>()));

    // key not exist : throwsExceptionWhenKeyDoesNotExist = true (APP)
    Assertions.assertThrows(NO_KEY_EX, () -> OBJ_APP.getProp("non-exist-key", new HashMap<>()));

    // key exist
    assertThat(OBJ_APP.getProp("TEST_KEY", new HashMap<>())).isEqualTo("TEST_APP");

    // locale is null
    assertThat(OBJ_APP.getProp(null, "TEST_KEY", new HashMap<>())).isEqualTo("TEST_APP");
    // locale is not null
    assertThat(OBJ_APP.getProp(Locale.JAPAN, "TEST_KEY", new HashMap<>())).isEqualTo("TEST_APP");
  }

  public void duplicatedKeyTest() {
    // By the specification of "ResourceBundle", it's not an error.
    PropertiesFileUtilValueGetter store = new PropertiesFileUtilValueGetter(
        new String[][]{new String[]{"test92-duplicate-in-one-file"}});
    store.getProp("KEY2", new HashMap<>());
  }

  @Test
  @DisplayName("locale resolution falls back correctly through file hierarchy")
  public void localeTest() {

    // properties file: non-locale only

    PropertiesFileUtilValueGetter none =
        new PropertiesFileUtilValueGetter(new String[][]{new String[]{"test92-none"}});
    assertThat(none.getProp(Locale.ROOT, "TEST_KEY", new HashMap<>())).isEqualTo("TEST_VALUE");
    assertThat(none.getProp(Locale.JAPANESE, "TEST_KEY", new HashMap<>())).isEqualTo("TEST_VALUE");
    assertThat(none.getProp(Locale.JAPAN, "TEST_KEY", new HashMap<>())).isEqualTo("TEST_VALUE");

    // properties file: lang

    PropertiesFileUtilValueGetter lang =
        new PropertiesFileUtilValueGetter(new String[][]{new String[]{"test92-lang"}});
    assertThat(none.getProp(Locale.ENGLISH, "TEST_KEY", new HashMap<>())).isEqualTo("TEST_VALUE");
    Assertions.assertThrows(NO_KEY_EX,
        () -> lang.getProp(Locale.JAPAN, "TEST_KEY", new HashMap<>()));

    // properties file: none-and-lang

    PropertiesFileUtilValueGetter noneAndLang =
        new PropertiesFileUtilValueGetter(new String[][]{new String[]{"test92-none-and-lang"}});
    assertThat(noneAndLang.getProp(Locale.ITALY, "FILE_LOCALE", new HashMap<>())).isEqualTo("it");
    assertThat(noneAndLang.getProp(Locale.ITALIAN, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("it");
    assertThat(noneAndLang.getProp(Locale.JAPANESE, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("none");
    assertThat(noneAndLang.getProp(Locale.JAPAN, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("none");

    // properties file: none-and-langCountry

    PropertiesFileUtilValueGetter noneAndLangCountry = new PropertiesFileUtilValueGetter(
        new String[][]{new String[]{"test92-none-and-lang-country_lib-core-test"}});
    assertThat(noneAndLangCountry.getProp(Locale.FRENCH, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("none");
    assertThat(noneAndLangCountry.getProp(Locale.JAPANESE, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("none");
    assertThat(noneAndLangCountry.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("fr_CA");
    assertThat(noneAndLangCountry.getProp(Locale.FRANCE, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("none");
    assertThat(noneAndLangCountry.getProp(Locale.CANADA, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("none");
    assertThat(noneAndLangCountry.getProp(Locale.JAPAN, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("none");

    // properties file: none-and-lang-and-langCountry

    PropertiesFileUtilValueGetter noneAndLangAndLangCountry = new PropertiesFileUtilValueGetter(
        new String[][]{new String[]{"test92-none-and-lang-and-lang-country"}});
    assertThat(noneAndLangAndLangCountry.getProp(Locale.FRENCH, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("fr");
    assertThat(noneAndLangAndLangCountry.getProp(Locale.JAPANESE, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("none");
    assertThat(
        noneAndLangAndLangCountry.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("fr_CA");
    assertThat(noneAndLangAndLangCountry.getProp(Locale.FRANCE, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("fr");
    assertThat(noneAndLangAndLangCountry.getProp(Locale.CANADA, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("none");
    assertThat(noneAndLangAndLangCountry.getProp(Locale.JAPAN, "FILE_LOCALE", new HashMap<>()))
        .isEqualTo("none");
  }

  @Test
  @DisplayName("fileKind postfix detection covers all expected patterns")
  public void fileKindTest() {
    obj = new PropertiesFileUtilValueGetter(new String[][]{new String[]{"test92-12-11"}});
    assertThat(Objects.requireNonNull(obj).getPostfixes()).contains("_lib_core");

    obj = new PropertiesFileUtilValueGetter(new String[][]{new String[]{"test92-12-11"}});
    assertThat(Objects.requireNonNull(obj).getPostfixes()).contains("_splib_web");

    obj = new PropertiesFileUtilValueGetter(new String[][]{new String[]{"test92-12-11"}});
    assertThat(Objects.requireNonNull(obj).getPostfixes()).contains("");

    obj = new PropertiesFileUtilValueGetter(new String[][]{new String[]{"test92-12-11"}});
    assertThat(Objects.requireNonNull(obj).getPostfixes()).contains("_base");

    obj = new PropertiesFileUtilValueGetter(new String[][]{new String[]{"test92-12-11"}});
    assertThat(Objects.requireNonNull(obj).getPostfixes()).contains("-profile");

    PropertiesFileUtilValueGetter store =
        new PropertiesFileUtilValueGetter(new String[][]{new String[]{"test92-12-11"}});
    assertThat(store.getPostfixes()).contains("_core-profile");

    // inter-file key duplication throws
    PropertiesFileUtilValueGetter dup = new PropertiesFileUtilValueGetter(
        new String[][]{new String[]{"test92-duplicate-in-multiple-files"}});
    Assertions.assertThrows(KeyDupliccatedException.class,
        () -> dup.getProp("KEY1", new HashMap<>()));
  }

  @Test
  @DisplayName("throwsExceptionWhenKeyDoesNotExist=false returns key string when key not found")
  public void throwsExceptionWhenKeyDoesNotExistIsFalseTest() {
    assertThat(OBJ_MSG.hasProp("non-exist-key")).isFalse();
    assertThat(OBJ_MSG.getProp("non-exist-key", new HashMap<>())).isEqualTo("non-exist-key");
  }

  @Test
  @DisplayName("property keys in obtained value are resolved recursively")
  public void resolvePropertyKeysInObtainedValueTest() {
    assertThat(OBJ_MSG.getProp("KEY_IN_MSG", new HashMap<>())).isEqualTo("Hi, John.");
  }
}
