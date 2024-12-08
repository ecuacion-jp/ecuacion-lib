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

import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilPropFileKindEnum.APP;
import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilPropFileKindEnum.MSG;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Locale;
import jp.ecuacion.lib.core.TestTools;
import org.junit.jupiter.api.Test;

public class Test91_12_util_PropertyFileUtilMultiLangPropStore extends TestTools {
  @Test
  public void test01_constructor_01_PropertyFileUtilPropFileKindEnum_01_引数がnull() {
    try {
      new PropertyFileUtilKeyGetterByFileKind((PropertyFileUtilPropFileKindEnum) null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test01_constructor_01_PropertyFileUtilPropFileKindEnum_02_引数がnull以外() {
    PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);
    assertThat(store.getProp(Locale.CANADA, "TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test01_constructor_02_filePrefix_01_引数がnull() {
    try {
      new PropertyFileUtilKeyGetterByFileKind((String) null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test01_constructor_02_filePrefix_02_引数がnull以外() {
    PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind("messages");
    assertThat(store.getProp(Locale.CANADA, "TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test01_constructor_11_default_locale_01_指定なし() {
    Locale locale = Locale.getDefault();
    new PropertyFileUtilKeyGetterByFileKind("messages");

    assertThat(locale).isEqualTo(Locale.getDefault());
  }

  @Test
  public void test01_constructor_11_default_locale_02_指定あり() {
    String origName = PropertyFileUtilKeyGetterByFileKind.bundleForPropertyFileUtil;

    try {
      PropertyFileUtilKeyGetterByFileKind.bundleForPropertyFileUtil =
          "application_for_property-file-util_base_test";
      new PropertyFileUtilKeyGetterByFileKind("messages");

      assertThat(Locale.getDefault()).isEqualTo(Locale.FRENCH);
    } finally {
      PropertyFileUtilKeyGetterByFileKind.bundleForPropertyFileUtil = origName;
    }
  }

  @Test
  public void test11_読み込みファイル種類_01_ecuacion_lib_xxx() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-11");

    assertThat(store.getProp("LIB_CORE_KEY")).isEqualTo("LIB_CORE_VALUE");
  }

  @Test
  public void test11_読み込みファイル種類_02_ecuacion_lib_jakartaee_xxx() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-11");
    assertThat(store.getProp("LIB_JAKARTAEE_BATCH_KEY")).isEqualTo("LIB_JAKARTAEE_BATCH_VALUE");
  }

  @Test
  public void test11_読み込みファイル種類_03_個別プロジェクト用標準ファイル() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-11");
    assertThat(store.getProp("KEY")).isEqualTo("VALUE");
  }

  @Test
  public void test11_読み込みファイル種類_04_個別プロジェクト用base() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-11");
    assertThat(store.getProp("BASE_KEY")).isEqualTo("BASE_VALUE");
  }

  @Test
  public void test11_読み込みファイル種類_05_個別プロジェクト用_profile() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-11");
    assertThat(store.getProp("FOR_ALL_PROFILES_KEY")).isEqualTo("FOR_ALL_PROFILES_VALUE");
  }

  @Test
  public void test11_読み込みファイル種類_06_個別プロジェクト用_core_profile() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-11");
    assertThat(store.getProp("COMMON_FOR_ALL_PROFILES_KEY"))
        .isEqualTo("COMMON_FOR_ALL_PROFILES_VALUE");
  }

  @Test
  public void test21_hasProp_01_key_01_keyがnull() {
    try {
      PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(APP);
      store.hasProp(null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test21_hasProp_01_key_11_ファイルが存在しない場合() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("non-exist-file");

    assertFalse(store.hasProp("testkey"));
  }

  @Test
  public void test21_hasProp_01_key_12_キーが存在しない場合() {
    PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);

    assertFalse(store.hasProp("non-exist-key"));
  }

  @Test
  public void test21_hasProp_01_key_21_キーが存在する場合() {
    PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);

    assertTrue(store.hasProp("TEST_KEY"));
  }

  @Test
  public void test21_hasProp_02_locale_key_01_localeがnull() {
    try {
      PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);
      store.hasProp(null, "TEST_KEY");
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test21_hasProp_02_locale_key_02_keyがnull() {
    try {
      PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);
      store.hasProp(Locale.JAPAN, null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test21_hasProp_02_locale_key_11_ファイルが存在しない場合() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("non-exist-file");

    assertFalse(store.hasProp(Locale.JAPAN, "testkey"));
  }

  @Test
  public void test21_hasProp_02_locale_key_12_キーが存在しない場合() {
    PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);

    assertFalse(store.hasProp(Locale.JAPAN, "non-exist-key"));
  }

  @Test
  public void test21_hasProp_02_locale_key_21_キーが存在する場合() {
    PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);

    assertTrue(store.hasProp(Locale.JAPAN, "TEST_KEY"));
  }

  @Test
  public void test22_getProp_01_key_01_keyがnull() {
    try {
      PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(APP);
      store.getProp(null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_01_key_11_ファイルが存在しない場合() {
    try {
      PropertyFileUtilKeyGetterByFileKind store =
          new PropertyFileUtilKeyGetterByFileKind("non-exist-file");
      store.getProp("testkey");
      fail();

    } catch (RuntimeException rte) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_01_key_12_キーが存在しない場合() {
    try {
      PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);
      store.getProp("non-exist-key");
      fail();

    } catch (RuntimeException rte) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_01_key_21_キーが存在する場合() {
    PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);

    assertThat(store.getProp("TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test22_getProp_02_locale_key_01_localeがnull() {
    try {
      PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);
      store.getProp(null, "TEST_KEY");
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_02_locale_key_02_keyがnull() {
    try {
      PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);
      store.getProp(Locale.JAPAN, null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_02_locale_key_11_ファイルが存在しない場合() {
    try {
      PropertyFileUtilKeyGetterByFileKind store =
          new PropertyFileUtilKeyGetterByFileKind("non-exist-file");
      store.getProp(Locale.JAPAN, "testkey");
      fail();

    } catch (RuntimeException re) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_02_locale_key_12_キーが存在しない場合() {
    try {
      PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);
      store.getProp(Locale.JAPAN, "non-exist-key");
      fail();

    } catch (RuntimeException re) {
      assertTrue(true);
    }
  }

  @Test
  public void test22_getProp_02_locale_key_21_キーが存在する場合() {
    PropertyFileUtilKeyGetterByFileKind store = new PropertyFileUtilKeyGetterByFileKind(MSG);

    assertThat(store.getProp(Locale.JAPAN, "TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test31_複数locale_01_ファイル_localeなしのみ_01_client_locale_言語() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-01");

    assertThat(store.getProp(Locale.JAPANESE, "TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test31_複数locale_01_ファイル_localeなしのみ_02_client_locale_言語and国() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-01");

    assertThat(store.getProp(Locale.JAPAN, "TEST_KEY")).isEqualTo("TEST_VALUE");
  }

  @Test
  public void test31_複数locale_02_ファイル_localeなしがない_01_client_locale_別言語() {
    try {
      PropertyFileUtilKeyGetterByFileKind store =
          new PropertyFileUtilKeyGetterByFileKind("test92-12-31-02");

      store.getProp(Locale.JAPAN, "TEST_KEY");
      fail();

    } catch (RuntimeException re) {
      assertTrue(true);
    }
  }

  @Test
  public void test31_複数locale_11_ファイル_localeなし_言語_01_client_locale_同一言語and国() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-11");
    assertThat(store.getProp(Locale.US, "FILE_LOCALE")).isEqualTo("en");
  }

  @Test
  public void test31_複数locale_11_ファイル_localeなし_言語_02_client_locale_同一言語() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-11");
    assertThat(store.getProp(Locale.ENGLISH, "FILE_LOCALE")).isEqualTo("en");
  }

  @Test
  public void test31_複数locale_11_ファイル_localeなし_言語_03_client_locale_別言語and国() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-11");
    assertThat(store.getProp(Locale.JAPAN, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_11_ファイル_localeなし_言語_04_client_locale_別言語() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-11");
    assertThat(store.getProp(Locale.JAPANESE, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_01_client_locale_同一言語and同一国() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-12");
    assertThat(store.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE")).isEqualTo("fr_CA");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_02_client_locale_同一言語and別国() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-12");
    assertThat(store.getProp(Locale.FRANCE, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_03_client_locale_同一言語() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-12");
    assertThat(store.getProp(Locale.FRENCH, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_04_client_locale_別言語and同一国() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-12");
    assertThat(store.getProp(Locale.CANADA, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_05_client_locale_別言語and別国() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-12");
    assertThat(store.getProp(Locale.JAPAN, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_12_ファイル_localeなし_言語and国_06_client_locale_別言語() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-12");
    assertThat(store.getProp(Locale.JAPANESE, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_01_client_locale_同一言語and同一国() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-13");
    assertThat(store.getProp(Locale.CANADA_FRENCH, "FILE_LOCALE")).isEqualTo("fr_CA");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_02_client_locale_同一言語and別国() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-13");
    assertThat(store.getProp(Locale.FRANCE, "FILE_LOCALE")).isEqualTo("fr");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_03_client_locale_同一言語() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-13");
    assertThat(store.getProp(Locale.FRENCH, "FILE_LOCALE")).isEqualTo("fr");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_04_client_locale_別言語and同一国() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-13");
    assertThat(store.getProp(Locale.CANADA, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_05_client_locale_別言語and別国() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-13");
    assertThat(store.getProp(Locale.JAPAN, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test31_複数locale_13_ファイル_localeなし_言語_言語and国_06_client_locale_別言語() {
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-31-13");
    assertThat(store.getProp(Locale.JAPANESE, "FILE_LOCALE")).isEqualTo("none");
  }

  @Test
  public void test41_キー重複_01_同一ファイル内() {
    // この場合、ResourceBundleの仕様上エラーにならないので注意。残念・・・
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-41-01");
    store.getProp("KEY2");
    assertTrue(true);
  }

  @Test
  public void test41_キー重複_02_複数ファイル間() {
    try {
      PropertyFileUtilKeyGetterByFileKind store =
          new PropertyFileUtilKeyGetterByFileKind("test92-12-41-02");
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
    PropertyFileUtilKeyGetterByFileKind store =
        new PropertyFileUtilKeyGetterByFileKind("test92-12-41-11");
    store.getProp("KEY2");
    assertTrue(true);
  }
}
