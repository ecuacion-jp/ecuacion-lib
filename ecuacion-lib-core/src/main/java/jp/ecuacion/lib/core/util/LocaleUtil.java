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
package jp.ecuacion.lib.core.util;

import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * Provides locale-related utility methods.
 *
 * <p>The fallback locale is used when no explicit locale is specified in ecuacion-lib APIs.
 * By default it is {@link Locale#getDefault()} (the JVM default locale).
 * Setting {@code jp.ecuacion.locale.use-root=true} in {@code application.properties}
 * changes the fallback to {@link Locale#ROOT}.</p>
 */
public class LocaleUtil {

  private static final boolean USE_ROOT;

  static {
    @Nullable
    String val = PropertiesFileUtil.getApplicationOrElse("jp.ecuacion.locale.use-root", null);
    USE_ROOT = "true".equalsIgnoreCase(val);
  }

  private LocaleUtil() {}

  /**
   * Returns the fallback locale used when no explicit locale is specified.
   *
   * <p>Returns {@link Locale#ROOT} when {@code jp.ecuacion.locale.use-root=true} is set
   * in {@code application.properties}; otherwise returns {@link Locale#getDefault()}.</p>
   *
   * @return the fallback locale
   */
  public static Locale getFallbackLocale() {
    return USE_ROOT ? Locale.ROOT : Locale.getDefault();
  }
}
