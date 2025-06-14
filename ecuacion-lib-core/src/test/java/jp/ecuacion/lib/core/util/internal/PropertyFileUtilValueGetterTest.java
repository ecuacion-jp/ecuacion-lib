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

import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.APPLICATION;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.MESSAGES;
import java.util.Locale;
import jp.ecuacion.lib.core.TestTools;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireNonNullException;
import jp.ecuacion.lib.core.util.internal.PropertyFileUtilValueGetter.KeyDupliccatedException;
import jp.ecuacion.lib.core.util.internal.PropertyFileUtilValueGetter.NoKeyInPropertiesFileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PropertyFileUtilValueGetterTest extends TestTools {

  private PropertyFileUtilValueGetter obj;

  private static final PropertyFileUtilValueGetter OBJ_APP =
      new PropertyFileUtilValueGetter(APPLICATION);
  private static final PropertyFileUtilValueGetter OBJ_MSG =
      new PropertyFileUtilValueGetter(MESSAGES);
  private static final Class<NoKeyInPropertiesFileException> NO_KEY_EX =
      NoKeyInPropertiesFileException.class;

  @BeforeAll
  public static void beforeAll() {
    PropertyFileUtilValueGetter.addToDynamicPostfixList("test");
  }

  @Test
  public void constructor_test() {

    // argument: PropertyFileUtilFileKindEnum

    // null
    Assertions.assertThrows(RequireNonNullException.class,
        () -> new PropertyFileUtilValueGetter((PropertyFileUtilFileKindEnum) null));

    // nonnull
    Assertions.assertEquals("TEST_VALUE", OBJ_MSG.getProp(Locale.CANADA, "TEST_KEY", null));

    // argument: String[][]

    // null
    Assertions.assertThrows(RequireNonNullException.class,
        () -> new PropertyFileUtilValueGetter((String[][]) null));

    // nonnull
    obj = new PropertyFileUtilValueGetter(new String[][] {new String[] {"messages"}});
    Assertions.assertEquals("TEST_VALUE", obj.getProp(Locale.CANADA, "TEST_KEY", null));
  }

  @Test
  public void hasProp_basicTest() {
    // key is null
    Assertions.assertThrows(NullPointerException.class, () -> OBJ_MSG.hasProp(null));

    // file not exist
    obj = new PropertyFileUtilValueGetter(new String[][] {new String[] {"non-exist-file"}});
    Assertions.assertFalse(obj.hasProp("testkey"));

    // key not exist
    Assertions.assertFalse(OBJ_MSG.hasProp("non-exist-key"));

    // key exists
    Assertions.assertTrue(OBJ_MSG.hasProp("TEST_KEY"));
  }

  @Test
  public void getProp_basicTest() {

    // # argument: key

    // key is null
    Assertions.assertThrows(RequireNonNullException.class, () -> OBJ_APP.getProp(null, null));

    // file not exist
    obj = new PropertyFileUtilValueGetter(new String[][] {new String[] {"non-exist-file"}});
    Assertions.assertThrows(NO_KEY_EX, () -> obj.getProp("testkey", null));

    // key not exist : throwsExceptionWhenKeyDoesNotExist = true (APP)
    Assertions.assertThrows(NO_KEY_EX, () -> OBJ_APP.getProp("non-exist-key", null));

    // key exist
    Assertions.assertEquals("TEST_APP", OBJ_APP.getProp("TEST_KEY", null));

    // # argument: locale, key

    // locale is null
    Assertions.assertEquals("TEST_APP", OBJ_APP.getProp(null, "TEST_KEY", null));

    // locale is not null
    Assertions.assertEquals("TEST_APP", OBJ_APP.getProp(Locale.JAPAN, "TEST_KEY", null));
  }

  public void duplicatedKeyTest() {
    // By the specification of "ResourceBundle", it's not an error.
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(
        new String[][] {new String[] {"test92-duplicate-in-one-file"}});
    store.getProp("KEY2", null);
  }

  @Test
  public void localeTest() {

    // # properties file: non-locale only

    PropertyFileUtilValueGetter none =
        new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none"}});
    // argument: no-locale
    Assertions.assertEquals("TEST_VALUE", none.getProp(Locale.ROOT, "TEST_KEY", null));
    // argument: lang
    Assertions.assertEquals("TEST_VALUE", none.getProp(Locale.JAPANESE, "TEST_KEY", null));
    // argument: lang and country
    Assertions.assertEquals("TEST_VALUE", none.getProp(Locale.JAPAN, "TEST_KEY", null));

    // # properties file: lang

    PropertyFileUtilValueGetter lang =
        new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-lang"}});
    // argument: lang
    Assertions.assertEquals("TEST_VALUE", none.getProp(Locale.ENGLISH, "TEST_KEY", null));
    // argument: other lang
    Assertions.assertThrows(NO_KEY_EX, () -> lang.getProp(Locale.JAPAN, "TEST_KEY", null));

    // # properties file: none-and-lang

    PropertyFileUtilValueGetter noneAndLang =
        new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang"}});
    // argument: lang
    Assertions.assertEquals("it", noneAndLang.getProp(Locale.ITALY, "FILE_LOCALE", null));
    // argument: lang-country
    Assertions.assertEquals("it", noneAndLang.getProp(Locale.ITALIAN, "FILE_LOCALE", null));
    // argument: other lang
    Assertions.assertEquals("none", noneAndLang.getProp(Locale.JAPANESE, "FILE_LOCALE", null));
    // argument: other lang-country
    Assertions.assertEquals("none", noneAndLang.getProp(Locale.JAPAN, "FILE_LOCALE", null));

    // # properties file: none-and-langCountry

    PropertyFileUtilValueGetter noneAndLangCountry = new PropertyFileUtilValueGetter(
        new String[][] {new String[] {"test92-none-and-lang-country"}});
    // argument: lang
    Assertions.assertEquals("none", noneAndLangCountry.getProp(Locale.FRENCH, "FILE_LOCALE", null));
    // argument: lang (other lang)
    Assertions.assertEquals("none", noneAndLangCountry.getProp(Locale.JAPANESE, "FILE_LOCALE", null));
    // argument: langCountry
    Assertions.assertEquals("fr_CA",
        noneAndLangCountry.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE", null));
    // argument: langCountry (other country)
    Assertions.assertEquals("none", noneAndLangCountry.getProp(Locale.FRANCE, "FILE_LOCALE", null));
    // argument: langCountry (other lang)
    Assertions.assertEquals("none", noneAndLangCountry.getProp(Locale.CANADA, "FILE_LOCALE", null));
    // argument: langCountry (other lang, other country)
    Assertions.assertEquals("none", noneAndLangCountry.getProp(Locale.JAPAN, "FILE_LOCALE", null));

    // # properties file: none-and-lang-and-langCountry

    PropertyFileUtilValueGetter noneAndLangAndLangCountry = new PropertyFileUtilValueGetter(
        new String[][] {new String[] {"test92-none-and-lang-and-lang-country"}});
    // argument: lang
    Assertions.assertEquals("fr", noneAndLangAndLangCountry.getProp(Locale.FRENCH, "FILE_LOCALE", null));
    // argument: lang (other lang)
    Assertions.assertEquals("none",
        noneAndLangAndLangCountry.getProp(Locale.JAPANESE, "FILE_LOCALE", null));
    // argument: langCountry
    Assertions.assertEquals("fr_CA",
        noneAndLangAndLangCountry.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE", null));
    // argument: langCountry (other country)
    Assertions.assertEquals("fr", noneAndLangAndLangCountry.getProp(Locale.FRANCE, "FILE_LOCALE", null));
    // argument: langCountry (other lang)
    Assertions.assertEquals("none",
        noneAndLangAndLangCountry.getProp(Locale.CANADA, "FILE_LOCALE", null));
    // argument: langCountry (other lang, other country)
    Assertions.assertEquals("none", noneAndLangAndLangCountry.getProp(Locale.JAPAN, "FILE_LOCALE", null));
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
    obj = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true, obj.getPostfixes().contains("lib_core"));

    // ecuacion_splib_xxx
    obj = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true, obj.getPostfixes().contains("splib_web"));

    // app
    obj = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true, obj.getPostfixes().contains(""));

    // app-base
    obj = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true, obj.getPostfixes().contains("base"));

    // app_profile
    obj = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true, obj.getPostfixes().contains("profile"));

    // app_core_profile
    PropertyFileUtilValueGetter store =
        new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertEquals(true, store.getPostfixes().contains("core_profile"));

    // inter-file key duplication

    PropertyFileUtilValueGetter dup = new PropertyFileUtilValueGetter(
        new String[][] {new String[] {"test92-duplicate-in-multiple-files"}});
    Assertions.assertThrows(KeyDupliccatedException.class, () -> dup.getProp("KEY1", null));
  }

  // To remove default locale from candidate locales
  public void defaultLocaleRemovedFromLocaleCandidateTest() {
    Locale.setDefault(Locale.ITALY);
    PropertyFileUtilValueGetter noneAndLang =
        new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang"}});
    Assertions.assertEquals("none", noneAndLang.getProp("FILE_LOCALE", null));
  }

  // To avoid throwing an exception exen if message Keys do not exist
  @Test
  public void throwsExceptionWhenKeyDoesNotExistIsFalseTest() {
    // key not exist : throwsExceptionWhenKeyDoesNotExist = false (MSG)

    // hasProp : false
    Assertions.assertEquals(false, OBJ_MSG.hasProp("non-exist-key"));

    // getProp : No exception occurs and return key plus alpha string
    Assertions.assertEquals("[ non-exist-key ]", OBJ_MSG.getProp("non-exist-key", null));
  }

  // To use "default" message by putting the postfix of the message ID ".default"

  // (no test)

  // To have the override function by java launch parameter (-D) or System.setProperty(...)

  // (no test)

  // To resolve property keys in the obtained value

  @Test
  public void resolvePropertyKeysInObtainedValueTest() {
    Assertions.assertEquals("Hi, John.", OBJ_MSG.getProp("KEY_IN_MSG", null));


  }

  // To resolve property keys in arguments</li>

  // (no test)

}
