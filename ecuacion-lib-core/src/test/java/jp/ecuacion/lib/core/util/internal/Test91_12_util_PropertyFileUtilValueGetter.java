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

import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.APP;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilFileKindEnum.MSG;
import java.util.Locale;
import jp.ecuacion.lib.core.TestTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test91_12_util_PropertyFileUtilValueGetter extends TestTools {

  @Test
  public void test01_constructor_01_PropertyFileUtilPropFileKindEnum_01_引数がnull() {
    try {
      new PropertyFileUtilValueGetter((PropertyFileUtilFileKindEnum) null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test01_constructor_01_PropertyFileUtilPropFileKindEnum_02_引数がnull以外() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(MSG);
    Assertions.assertThat(store.getProp(Locale.CANADA, "TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test01_constructor_02_filePrefix_01_引数がnull() {
    try {
      new PropertyFileUtilValueGetter((String[][]) null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test01_constructor_02_filePrefix_02_引数がnull以外() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"messages"}});
    Assertions.assertThat(store.getProp(Locale.CANADA, "TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test11_読み込みファイル種類_01_ecuacion_lib_xxx() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertThat(store.getPostfixes().contains("lib_core")).isEqualTo(true);
  }

  @Test
  public void test11_読み込みファイル種類_02_ecuacion_splib_xxx() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertThat(store.getPostfixes().contains("splib_web")).isEqualTo(true);
  }

  @Test
  public void test11_読み込みファイル種類_03_個別プロジェクト用標準ファイル() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertThat(store.getPostfixes().contains("")).isEqualTo(true);
  }

  @Test
  public void test11_読み込みファイル種類_04_個別プロジェクト用base() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertThat(store.getPostfixes().contains("base")).isEqualTo(true);
  }

  @Test
  public void test11_読み込みファイル種類_05_個別プロジェクト用_profile() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertThat(store.getPostfixes().contains("profile")).isEqualTo(true);
  }

  @Test
  public void test11_読み込みファイル種類_06_個別プロジェクト用_core_profile() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-12-11"}});
    Assertions.assertThat(store.getPostfixes().contains("core_profile")).isEqualTo(true);
  }

  @Test
  public void test21_hasProp_01_key_01_keyがnull() {
    try {
      PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(APP);
      store.hasProp(null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test21_hasProp_01_key_11_ファイルが存在しない場合() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"non-exist-file"}});

    assertFalse(store.hasProp("testkey"));
  }

  @Test
  public void test21_hasProp_01_key_12_キーが存在しない場合() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(MSG);

    assertFalse(store.hasProp("non-exist-key"));
  }

  @Test
  public void test21_hasProp_01_key_21_キーが存在する場合() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(MSG);

    assertTrue(store.hasProp("TEST_KEY"));
  }

  @Test
  public void test22_getProp_01_key_01_keyがnull() {
    try {
      PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(APP);
      store.getProp(null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_01_key_11_ファイルが存在しない場合() {
    try {
      PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"non-exist-file"}});
      store.getProp("testkey");
      fail();

    } catch (RuntimeException rte) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_01_key_12_キーが存在しない場合() {
    try {
      PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(MSG);
      store.getProp("non-exist-key");
      fail();

    } catch (RuntimeException rte) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_01_key_21_キーが存在する場合() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(MSG);

    Assertions.assertThat(store.getProp("TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test22_getProp_02_locale_key_01_localeがnull() {
    try {
      PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(MSG);
      store.getProp(null, "TEST_KEY");

    } catch (NullPointerException npe) {
      fail();
    }
  }

  @Test
  public void test22_getProp_02_locale_key_02_keyがnull() {
    try {
      PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(MSG);
      store.getProp(Locale.JAPAN, null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_02_locale_key_11_ファイルが存在しない場合() {
    try {
      PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"non-exist-file"}});
      store.getProp(Locale.JAPAN, "testkey");
      fail();

    } catch (RuntimeException re) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_02_locale_key_12_キーが存在しない場合() {
    try {
      PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(MSG);
      store.getProp(Locale.JAPAN, "non-exist-key");
      fail();

    } catch (RuntimeException re) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_02_locale_key_21_キーが存在する場合() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(MSG);

    Assertions.assertThat(store.getProp(Locale.JAPAN, "TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test31_複数locale_01_ファイル_localeなしのみ_01_client_locale_なし() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none"}});

    Assertions.assertThat(store.getProp(Locale.ROOT, "TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test31_複数locale_01_ファイル_localeなしのみ_02_client_locale_言語() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none"}});

    Assertions.assertThat(store.getProp(Locale.JAPANESE, "TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test31_複数locale_01_ファイル_localeなしのみ_03_client_locale_言語and国() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none"}});

    Assertions.assertThat(store.getProp(Locale.JAPAN, "TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test31_複数locale_02_ファイル_locale_言語_01_client_locale_別言語() {
    try {
      PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-lang"}});

      store.getProp(Locale.JAPAN, "TEST_KEY");
      fail();

    } catch (RuntimeException re) {
      assertTrue(true);
    }
  }

  @Test
  public void test31_複数locale_11_ファイル_localeなし_言語_01_client_locale_同一言語and国() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang"}});
    Assertions.assertThat(store.getProp(Locale.US, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_11_ファイル_localeなし_言語_02_client_locale_同一言語() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang"}});
    Assertions.assertThat(store.getProp(Locale.ENGLISH, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_11_ファイル_localeなし_言語_03_client_locale_別言語and国() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang"}});
    Assertions.assertThat(store.getProp(Locale.JAPAN, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_11_ファイル_localeなし_言語_04_client_locale_別言語() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang"}});
    Assertions.assertThat(store.getProp(Locale.JAPANESE, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_01_client_locale_同一言語and同一国() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE")).isEqualTo("fr_CA");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_02_client_locale_同一言語and別国() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.FRANCE, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_03_client_locale_同一言語() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.FRENCH, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_04_client_locale_別言語and同一国() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.CANADA, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_05_client_locale_別言語and別国() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.JAPAN, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_06_client_locale_別言語() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.JAPANESE, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_01_client_locale_同一言語and同一国() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE")).isEqualTo("fr_CA");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_02_client_locale_同一言語and別国() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.FRANCE, "FILE_LOCALE")).isEqualTo("fr");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_03_client_locale_同一言語() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.FRENCH, "FILE_LOCALE")).isEqualTo("fr");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_04_client_locale_別言語and同一国() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.CANADA, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_05_client_locale_別言語and別国() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.JAPAN, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_06_client_locale_別言語() {
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang-and-lang-country"}});
    Assertions.assertThat(store.getProp(Locale.JAPANESE, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test41_キー重複_01_同一ファイル内() {
    // この場合、ResourceBundleの仕様上エラーにならないので注意。残念・・・
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-duplicate-in-one-file"}});
    store.getProp("KEY2");
    assertTrue(true);
  }

  @Test
  public void test41_キー重複_02_複数ファイル間() {
    try {
      PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-duplicate-in-multiple-files"}});
      store.getProp("KEY1");
      fail();

    } catch (RuntimeException re) {
      assertTrue(true);
    }
  }

  @Test
  public void test41_キー重複_11_別locale() {
    // これはエラーにしてはいけない
    // この場合、ResourceBundleの仕様上エラーにならないので注意。残念・・・
    PropertyFileUtilValueGetter store = new PropertyFileUtilValueGetter(new String[][] {new String[] {"test92-none-and-lang"}});
    store.getProp("FILE_LOCALE");
    assertTrue(true);
  }
}
