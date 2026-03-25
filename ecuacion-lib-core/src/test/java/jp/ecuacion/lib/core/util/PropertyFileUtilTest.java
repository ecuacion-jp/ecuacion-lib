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
// package jp.ecuacion.lib.core.util;
//
// import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilPropFileKindEnum.APP;
//import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilPropFileKindEnum.ENUM_NAME;
//import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilPropFileKindEnum.FIELD_NAME;
//import static jp.ecuacion.lib.core.util.internal.PropertyFileUtilPropFileKindEnum.MSG;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.Locale;
//import java.util.ResourceBundle;
//import jp.ecuacion.lib.core.constant.ConstantsInLibCore;
//import jp.ecuacion.lib.core.util.internal.PropertyFileUtilMultiLangPropStore;
//import org.junit.jupiter.api.Test;
//
// public class Test21_01_util_PropertyFileUtil {
//
// @Before
// public void before() {
// // Since the default locale becomes the locale of the PC running the test, to avoid confusion,
// // we set it to the unlikely locale of Lebanon. (Maybe because it was right after Ghosn's escape there... (lol))
// Locale.setDefault(new Locale("ar_LB"));
// }
//
// /**
// * Method that gets an instance of MultiLangPropStore.<br>
// * Since it uses a private constructor for testing and requires reflection,
// * it is extracted as a method for readability.
// */
// @SuppressWarnings("rawtypes")
// private PropertyFileUtilMultiLangPropStore newInstanceOfMultiLangPropStore(String filePrefix)
// throws Exception {
// Class<?> clazz = Class.forName(
// ConstantsInLibCore.STR_LIB_PKG + ".core.util.internal.PropertyFileUtilMultiLangPropStore");
// Constructor con = clazz.getDeclaredConstructor(String.class);
// con.setAccessible(true);
// return (PropertyFileUtilMultiLangPropStore) con.newInstance(filePrefix);
// }
//
// /**
// * Method that returns the result of the getRb method. filePrefix can be any value.
// * For example, specifying "test" would require test.properties etc. to be prepared.
// */
// private ResourceBundle invokeGetRbOfMultiLangPropStore(String filePrefix, Locale locale)
// throws Exception {
// PropertyFileUtilMultiLangPropStore obj = newInstanceOfMultiLangPropStore(filePrefix);
// Method m = PropertyFileUtilMultiLangPropStore.class.getDeclaredMethod("getRb", String.class,
// Locale.class);
// m.setAccessible(true);
// ResourceBundle rb = (ResourceBundle) m.invoke(obj, filePrefix, locale);
// return rb;
// }
//
// /**
// * Method that executes the readPropFile method.
// * Unlike invokeGetRBOfMultiLangPropStore, this requires a MultiLangPropStore instance as an argument
// * (because the test would not be meaningful without holding the obj on the test code side).<br>
// */
// private void invokeReadPropFileOfMultiLangPropStore(PropertyFileUtilMultiLangPropStore obj,
// Locale locale) throws Exception {
// Method m =
// PropertyFileUtilMultiLangPropStore.class.getDeclaredMethod("readPropFile", Locale.class);
// m.setAccessible(true);
// m.invoke(obj, locale);
// }
//
// @Test
// public void test01_PropFileKindEnum_getFilePrefixFromEnum_valid() {
// assertThat(APP.getFilePrefix()).isEqualTo("application")));
// assertThat(MSG.getFilePrefix()).isEqualTo("messages")));
// assertThat(FIELD_NAME.getFilePrefix()).isEqualTo("field_names")));
// assertThat(ENUM_NAME.getFilePrefix()).isEqualTo("enum_names")));
// }
//
// @Test
// public void test11_MultiLangPropStore_getRb_valid() throws Exception {
// // Create a new MultiLangPropStore instance
// ResourceBundle rb = invokeGetRbOfMultiLangPropStore("test21_01-a", Locale.JAPANESE);
// assertThat(rb.getString("test11key")).isEqualTo("test11value")));
// }
//
// @Test
// public void test12_MultiLangPropStore_getRb_bundleNameIsNull() throws Exception {
// // Create a new MultiLangPropStore instance
// try {
// invokeGetRbOfMultiLangPropStore(null, Locale.JAPANESE);
// assertTrue(false);
// } catch (InvocationTargetException e) {
// if (e.getTargetException() instanceof RuntimeException) {
// assertTrue(true);
// } else {
// assertTrue(false);
// }
// }
// }
//
// @Test
// public void test13_MultiLangPropStore_getRb_localeIsNull() throws Exception {
// // Create a new MultiLangPropStore instance
// try {
// invokeGetRbOfMultiLangPropStore("", null);
// assertTrue(false);
// } catch (InvocationTargetException e) {
// if (e.getTargetException() instanceof RuntimeException) {
// assertTrue(true);
// } else {
// assertTrue(false);
// }
// }
// }
//
// @Test
// public void test21_MultiLangPropStore_getRb_3levels_lang_langCountry() throws Exception {
// ResourceBundle rb = invokeGetRbOfMultiLangPropStore("test21_01-b_3files", Locale.JAPAN);
// assertThat(rb.getString("keyWhichAllFilesHave")).isEqualTo("ja_JP!")));
// }
//
// @Test
// public void test22_MultiLangPropStore_getRb_3levels_lang() throws Exception {
// ResourceBundle rb = invokeGetRbOfMultiLangPropStore("test21_01-b_3files", Locale.JAPANESE);
// assertThat(rb.getString("keyWhichAllFilesHave")).isEqualTo("ja!")));
// }
//
// @Test
// public void test23_MultiLangPropStore_getRb_3levels_differentLang_langCountry() throws Exception {
// ResourceBundle rb = invokeGetRbOfMultiLangPropStore("test21_01-b_3files", Locale.CANADA_FRENCH);
// assertThat(rb.getString("keyWhichAllFilesHave")).isEqualTo("none!")));
// }
//
// @Test
// public void test24_MultiLangPropStore_getRb_3levels_differentLang() throws Exception {
// ResourceBundle rb = invokeGetRbOfMultiLangPropStore("test21_01-b_3files", Locale.ENGLISH);
// assertThat(rb.getString("keyWhichAllFilesHave")).isEqualTo("none!")));
// }
//
// @Test
// public void test25_MultiLangPropStore_getRb_2levels_lang_langCountry() throws Exception {
// ResourceBundle rb = invokeGetRbOfMultiLangPropStore("test21_01-c_2files", Locale.JAPAN);
// assertThat(rb.getString("keyWhichAllFilesHave")).isEqualTo("ja!")));
// }
//
// @Test
// public void test26_MultiLangPropStore_getRb_2levels_lang() throws Exception {
// ResourceBundle rb = invokeGetRbOfMultiLangPropStore("test21_01-c_2files", Locale.JAPANESE);
// assertThat(rb.getString("keyWhichAllFilesHave")).isEqualTo("ja!")));
// }
//
// @Test
// public void test27_MultiLangPropStore_getRb_2levels_differentLang_langCountry() throws Exception {
// ResourceBundle rb = invokeGetRbOfMultiLangPropStore("test21_01-c_2files", Locale.CANADA_FRENCH);
// assertThat(rb.getString("keyWhichAllFilesHave")).isEqualTo("none!")));
// }
//
// @Test
// public void test28_MultiLangPropStore_getRb_2levels_differentLang() throws Exception {
// ResourceBundle rb = invokeGetRbOfMultiLangPropStore("test21_01-c_2files", Locale.ENGLISH);
// assertThat(rb.getString("keyWhichAllFilesHave")).isEqualTo("none!")));
// }
//
// @Test
// public void test31_MultiLangPropStore_readPropFile_keyByModule() throws Exception {
// PropertyFileUtilMultiLangPropStore obj = newInstanceOfMultiLangPropStore("test21_01-d_mods");
// invokeReadPropFileOfMultiLangPropStore(obj, Locale.ENGLISH);
// assertThat(obj.getProp(Locale.ENGLISH, "key_none")).isEqualTo("value_none")));
// assertThat(obj.getProp(Locale.ENGLISH, "key_for_all_profiles"),
// isEqualTo("value_for_all_profiles")));
// assertThat(obj.getProp(Locale.ENGLISH, "key_cmn")).isEqualTo("value_common")));
// assertThat(obj.getProp(Locale.ENGLISH, "key_base")).isEqualTo("value_base")));
// assertThat(obj.getProp(Locale.ENGLISH, "key_fw_web")).isEqualTo("value_fw_web")));
// assertThat(obj.getProp(Locale.ENGLISH, "key_fw_batch")).isEqualTo("value_fw_batch")));
// assertThat(obj.getProp(Locale.ENGLISH, "key_fw_cmn")).isEqualTo("value_fw_cmn")));
// }
//
// // test32 is no longer needed
//
// @Test
// public void test33_MultiLangPropStore_readPropFile_duplicateKeyAcrossModules() throws Exception {
// PropertyFileUtilMultiLangPropStore obj =
// newInstanceOfMultiLangPropStore("test21_01-e_duplicate_key");
// try {
// invokeReadPropFileOfMultiLangPropStore(obj, Locale.ENGLISH);
// assertTrue(false);
// } catch (Exception e) {
// assertTrue(true);
// }
// }
//
//
//
// }
//


