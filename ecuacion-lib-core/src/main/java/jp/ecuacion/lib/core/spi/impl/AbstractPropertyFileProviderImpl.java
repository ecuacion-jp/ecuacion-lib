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
package jp.ecuacion.lib.core.spi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.spi.AbstractResourceBundleProvider;
import jp.ecuacion.lib.core.util.internal.PropertyFileUtilValueGetter;

/**
 * Provides an implementation of {@code ResourceBundleProvider}.
 * 
 * <p>It's used when the app is in java 9 module system.</p>
 * 
 * <p>This removes the default locale, which is the locale of OS,
 *     to avoid the result depends on the execution environment.</p>
 */
public abstract class AbstractPropertyFileProviderImpl extends AbstractResourceBundleProvider {

  /**
   * Provides {@code ResourceBundle}.
   */
  @Override
  public ResourceBundle getBundle(String baseName, Locale locale) {
    Locale specifiedLocale = PropertyFileUtilValueGetter.specifiedLocale.get();

    // remove default locale if not specified.
    if (!locale.getLanguage().equals("")
        && !specifiedLocale.getLanguage().equals(Locale.getDefault().getLanguage())
        && locale.getLanguage().equals(Locale.getDefault().getLanguage())) {
      return null;
    }

    // Obtain resource from module.
    String baseFilename = PropertyFileUtilValueGetter.bundleNameForModule.get();
    String moduleName = getModuleName(baseFilename);
    Module module = ModuleLayer.boot().findModule(moduleName).orElse(null);
    if (module != null) {
      try {
        String bundleName = baseFilename + (locale.toString().equals("") ? "" : "_")
            + locale.toString() + ".properties";
        InputStream is = module.getResourceAsStream(bundleName);

        if (is != null) {
          return new PropertyResourceBundle(is);
        }

      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    return super.getBundle(PropertyFileUtilValueGetter.bundleNameForModule.get(), locale);
  }

  private String getModuleName(String filename) {
    String[] parts = filename.split("_");

    int len = parts.length;

    // for test
    if (filename.endsWith("test")) {
      // Filenames like messages_lib-core-test[_xx].properties are used in test.
      // ('lib-core-test' is needed to add in advance by PropertyFileUtil.addResourceBundlePostfix)
      // and you can see the module name from the 'lib-core' part.
      // If you want more postfixes, you can use 'lib-core-xxxtest'
      String[] moduleNameParts = parts[len - 1].split("-");
      return "jp.ecuacion." + moduleNameParts[0] + "." + moduleNameParts[1];
    }

    // for ecuacion lib/splib/utils
    if (len > 2) {
      String firstPart = parts[len - 2];
      String secondPart = parts[len - 1];

      return "jp.ecuacion." + firstPart + "." + secondPart;
    }

    return null;
  }
}
