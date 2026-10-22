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
package jp.ecuacion.lib.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests locale resolution of properties files placed in a module
 *     other than {@code jp.ecuacion.lib.core}.
 *
 * <p>The {@code ResourceBundleProvider} implementations live in {@code jp.ecuacion.lib.core},
 *     so the properties files in this module are read by
 *     {@code AbstractPropertiesFileProviderImpl} through the module lookup,
 *     not by the fallback to {@code AbstractResourceBundleProvider}.</p>
 */
@DisplayName("PropertiesFileUtil locale resolution in other modules")
public class PropertiesFileLocaleTest {

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtil.addResourceBundlePostfix("lib-validation-test");
  }

  @Test
  @DisplayName("locale with script or extensions resolves files named by ResourceBundle rules")
  public void localeWithScriptAndExtensionsTest() {
    // script
    assertThat(getFileLocale(Locale.forLanguageTag("zh-Hant"))).isEqualTo("zh_Hant");
    assertThat(getFileLocale(Locale.forLanguageTag("zh-Hant-TW"))).isEqualTo("zh_Hant");
    assertThat(getFileLocale(Locale.forLanguageTag("zh-Hans"))).isEqualTo("zh");

    // country implying script (ResourceBundle.Control maps TW / HK / MO to Hant)
    assertThat(getFileLocale(Locale.TAIWAN)).isEqualTo("zh_Hant");
    assertThat(getFileLocale(Locale.forLanguageTag("zh-HK"))).isEqualTo("zh_Hant");
    assertThat(getFileLocale(Locale.CHINA)).isEqualTo("zh");
    assertThat(getFileLocale(Locale.CHINESE)).isEqualTo("zh");

    // extensions are not part of file names
    assertThat(getFileLocale(Locale.forLanguageTag("fr-CA-u-ca-gregory"))).isEqualTo("fr_CA");

    assertThat(getFileLocale(Locale.CANADA_FRENCH)).isEqualTo("fr_CA");
    assertThat(getFileLocale(Locale.ROOT)).isEqualTo("none");
  }

  private String getFileLocale(Locale locale) {
    return PropertiesFileUtil.getMessage(locale, "fileLocale");
  }
}
