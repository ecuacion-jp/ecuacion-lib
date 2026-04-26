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
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import jp.ecuacion.lib.core.TestTools;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilValueGetter.KeyDupliccatedException;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilValueGetter.NoKeyInPropertiesFileException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PropertiesFileUtilValueGetterTest extends TestTools {

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
  public void constructor_test() {

    // argument: PropertiesFileUtilFileKindEnu

    // nonnull
    Assertions.assertEquals("TEST_VALUE",
        OBJ_MSG.getProp(Locale.CANADA, "TEST_KEY", new HashMap<>()));

    // argument: String[][]

    // nonnull
    obj = new PropertiesFileUtilValueGetter(new String[][] {new String[] {"messages"}});
    Assertions.assertEquals("TEST_VALUE",
        Objects.requireNonNull(obj).getProp(Locale.CANADA, "TEST_KEY", new HashMap<>()));
  }

  @Test
  public void hasProp_basicTest() {
    // file not exist
    obj = new PropertiesFileUtilValueGetter(new String[][] {new String[] {"non-exist-file"}});
    Assertions.assertFalse(Objects.requireNonNull(obj).hasProp("testkey"));

    // key not exist
    Assertions.assertFalse(OBJ_MSG.hasProp("non-exist-key"));

    // key exists
    Assertions.assertTrue(OBJ_MSG.hasProp("TEST_KEY"));
  }

  @Test
  public void getProp_basicTest() {
    Assertions.assertEquals("TEST_APP", OBJ_APP.getProp("TEST_KEY", new HashMap<>()));

    // # argument: key

    // file not exist
    obj = new PropertiesFileUtilValueGetter(new String[][] {new String[] {"non-exist-file"}});
    Assertions.assertThrows(NO_KEY_EX, () -> Objects.requireNonNull(obj).getProp("testkey", new HashMap<>()));

    // key not exist : throwsExceptionWhenKeyDoesNotExist = true (APP)
    Assertions.assertThrows(NO_KEY_EX, () -> OBJ_APP.getProp("non-exist-key", new HashMap<>()));

    // key exist
    Assertions.assertEquals("TEST_APP", OBJ_APP.getProp("TEST_KEY", new HashMap<>()));

    // # argument: locale, key

    // locale is null
    Assertions.assertEquals("TEST_APP", OBJ_APP.getProp(null, "TEST_KEY", new HashMap<>()));

    // locale is not null
    Assertions.assertEquals("TEST_APP", OBJ_APP.getProp(Locale.JAPAN, "TEST_KEY", new HashMap<>()));
  }

  public void duplicatedKeyTest() {
    // By the specification of "ResourceBundle", it's not an error.
    PropertiesFileUtilValueGetter store = new PropertiesFileUtilValueGetter(
        new String[][] {new String[] {"test92-duplicate-in-one-file"}});
    store.getProp("KEY2", new HashMap<>());
  }

  @Test
  public void localeTest() {

    // # properties file: non-locale only

    PropertiesFileUtilValueGetter none =
        new PropertiesFileUtilValueGetter(new String[][] {new String[] {"test92-none"}});
    // argument: no-locale
    Assertions.assertEquals("TEST_VALUE", none.getProp(Locale.ROOT, "TEST_KEY", new HashMap<>()));
    // argument: lang
    Assertions.assertEquals("TEST_VALUE", none.getProp(Locale.JAPANESE, "TEST_KEY", new HashMap<>()));
    // argument: lang and country
    Assertions.assertEquals("TEST_VALUE", none.getProp(Locale.JAPAN, "TEST_KEY", new HashMap<>()));

    // # properties file: lang

    PropertiesFileUtilValueGetter lang =
        new PropertiesFileUtilValueGetter(new String[][] {new String[] {"test92-lang"}});
    // argument: lang
    Assertions.assertEquals("TEST_VALUE", none.getProp(Locale.ENGLISH, "TEST_KEY", new HashMap<>()));
    // argument: other lang
    Assertions.assertThrows(NO_KEY_EX, () -> lang.getProp(Locale.JAPAN, "TEST_KEY", new HashMap<>()));

    // # properties file: none-and-lang

    PropertiesFileUtilValueGetter noneAndLang =
        new PropertiesFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang"}});
    // argument: lang
    Assertions.assertEquals("it", noneAndLang.getProp(Locale.ITALY, "FILE_LOCALE", new HashMap<>()));
    // argument: lang-country
    Assertions.assertEquals("it", noneAndLang.getProp(Locale.ITALIAN, "FILE_LOCALE", new HashMap<>()));
    // argument: other lang
    Assertions.assertEquals("none", noneAndLang.getProp(Locale.JAPANESE, "FILE_LOCALE", new HashMap<>()));
    // argument: other lang-country
    Assertions.assertEquals("none", noneAndLang.getProp(Locale.JAPAN, "FILE_LOCALE", new HashMap<>()));

    // # properties file: none-and-langCountry

    PropertiesFileUtilValueGetter noneAndLangCountry = new PropertiesFileUtilValueGetter(
        new String[][] {new String[] {"test92-none-and-lang-country_lib-core-test"}});
    // argument: lang
    Assertions.assertEquals("none", noneAndLangCountry.getProp(Locale.FRENCH, "FILE_LOCALE", new HashMap<>()));
    // argument: lang (other lang)
    Assertions.assertEquals("none",
        noneAndLangCountry.getProp(Locale.JAPANESE, "FILE_LOCALE", new HashMap<>()));
    // argument: langCountry
    Assertions.assertEquals("fr_CA",
        noneAndLangCountry.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE", new HashMap<>()));
    // argument: langCountry (other country)
    Assertions.assertEquals("none", noneAndLangCountry.getProp(Locale.FRANCE, "FILE_LOCALE", new HashMap<>()));
    // argument: langCountry (other lang)
    Assertions.assertEquals("none", noneAndLangCountry.getProp(Locale.CANADA, "FILE_LOCALE", new HashMap<>()));
    // argument: langCountry (other lang, other country)
    Assertions.assertEquals("none", noneAndLangCountry.getProp(Locale.JAPAN, "FILE_LOCALE", new HashMap<>()));

    // # properties file: none-and-lang-and-langCountry

    PropertiesFileUtilValueGetter noneAndLangAndLangCountry = new PropertiesFileUtilValueGetter(
        new String[][] {new String[] {"test92-none-and-lang-and-lang-country"}});
    // argument: lang
    Assertions.assertEquals("fr",
        noneAndLangAndLangCountry.getProp(Locale.FRENCH, "FILE_LOCALE", new HashMap<>()));
    // argument: lang (other lang)
    Assertions.assertEquals("none",
        noneAndLangAndLangCountry.getProp(Locale.JAPANESE, "FILE_LOCALE", new HashMap<>()));
    // argument: langCountry
    Assertions.assertEquals("fr_CA",
        noneAndLangAndLangCountry.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE", new HashMap<>()));
    // argument: langCountry (other country)
    Assertions.assertEquals("fr",
        noneAndLangAndLangCountry.getProp(Locale.FRANCE, "FILE_LOCALE", new HashMap<>()));
    // argument: langCountry (other lang)
    Assertions.assertEquals("none",
        noneAndLangAndLangCountry.getProp(Locale.CANADA, "FILE_LOCALE", new HashMap<>()));
    // argument: langCountry (other lang, other country)
    Assertions.assertEquals("none",
        noneAndLangAndLangCountry.getProp(Locale.JAPAN, "FILE_LOCALE", new HashMap<>()));
  }

  // To read multiple kinds of ".properties"
  // ({@code application, messages, enum_names, item_names, ValidationMessages,
  // ValidationMessagesWithItemNames})</li>

  // (no test)

  // To read all the ".properties" files in library modules and multiple modules
  // in projects of an app

  @Test
  public void fileKindTest() {

    // ecuacion_lib_xxx
    obj = new PropertiesFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true, Objects.requireNonNull(obj).getPostfixes().contains("_lib_core"));

    // ecuacion_splib_xxx
    obj = new PropertiesFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true,
        Objects.requireNonNull(obj).getPostfixes().contains("_splib_web"));

    // app
    obj = new PropertiesFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true, Objects.requireNonNull(obj).getPostfixes().contains(""));

    // app-base
    obj = new PropertiesFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true, Objects.requireNonNull(obj).getPostfixes().contains("_base"));

    // app_profile
    obj = new PropertiesFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true, Objects.requireNonNull(obj).getPostfixes().contains("-profile"));

    // app_core_profile
    PropertiesFileUtilValueGetter store =
        new PropertiesFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true, store.getPostfixes().contains("_core-profile"));

    // inter-file key duplication

    PropertiesFileUtilValueGetter dup = new PropertiesFileUtilValueGetter(
        new String[][] {new String[] {"test92-duplicate-in-multiple-files"}});
    Assertions.assertThrows(KeyDupliccatedException.class, () -> dup.getProp("KEY1", new HashMap<>()));
  }

  // To remove default locale from candidate locales
  public void defaultLocaleRemovedFromLocaleCandidateTest() {
    Locale.setDefault(Locale.ITALY);
    PropertiesFileUtilValueGetter noneAndLang = new PropertiesFileUtilValueGetter(
        new String[][] {new String[] {"test92-none-and-lang_test"}});
    Assertions.assertEquals("none", noneAndLang.getProp("FILE_LOCALE", new HashMap<>()));
  }

  // To avoid throwing an exception exen if message Keys do not exist
  @Test
  public void throwsExceptionWhenKeyDoesNotExistIsFalseTest() {
    // key not exist : throwsExceptionWhenKeyDoesNotExist = false (MSG)

    // hasProp : false
    Assertions.assertEquals(false, OBJ_MSG.hasProp("non-exist-key"));

    // getProp : No exception occurs and return key plus alpha string
    Assertions.assertEquals("non-exist-key", OBJ_MSG.getProp("non-exist-key", new HashMap<>()));
  }

  // To use "default" message by putting the postfix of the message ID ".default"

  // (no test)

  // To have the override function by java launch parameter (-D) or System.setProperty(...)

  // (no test)

  // To resolve property keys in the obtained value

  @Test
  public void resolvePropertyKeysInObtainedValueTest() {
    Assertions.assertEquals("Hi, John.", OBJ_MSG.getProp("KEY_IN_MSG", new HashMap<>()));


  }

  // To resolve property keys in arguments</li>

  // (no test)

}
