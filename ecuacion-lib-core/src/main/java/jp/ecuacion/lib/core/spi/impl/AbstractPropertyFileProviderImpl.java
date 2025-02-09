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

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.spi.AbstractResourceBundleProvider;
import jp.ecuacion.lib.core.util.internal.PropertyFileUtilValueGetter;

/**
 * Provides an implementation of {@code ResourceBundleProvider}.
 * 
 * <p>It's used when the app is in java 9 module system.</p>
 * 
 * <p>This removes the default locale, which is the locale of OS,
 *      to avoid the result depends on the execution environment.</p>
 */
public abstract class AbstractPropertyFileProviderImpl extends AbstractResourceBundleProvider {

  /**
   * Provides {@code ResourceBundle}.
   */
  public ResourceBundle getBundle(String baseName, Locale locale) {
    Locale specifiedLocale = PropertyFileUtilValueGetter.specifiedLocale.get();
    
    // remove default locale if not specified.
    if (!locale.getLanguage().equals("")
        && !specifiedLocale.getLanguage().equals(Locale.getDefault().getLanguage())
        && locale.getLanguage().equals(Locale.getDefault().getLanguage())) {
      return null;
    }

    return super.getBundle(PropertyFileUtilValueGetter.bundleNameForModule.get(), locale);
  }
}
