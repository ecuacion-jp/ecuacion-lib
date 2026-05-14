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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Handles message formatting for {@link jp.ecuacion.lib.core.util.PropertiesFileUtil}.
 *
 * <p>Provides two kinds of placeholder substitution:</p>
 * <ul>
 * <li>Positional ({@code {0}}, {@code {1}}, ...) via {@link java.text.MessageFormat}</li>
 * <li>Named ({@code {min}}, {@code {max}}, ...) for Jakarta Validation messages</li>
 * </ul>
 */
public class PropertiesFileUtilFormatter {

  private PropertiesFileUtilFormatter() {}

  /**
   * Formats {@code template} by substituting positional placeholders with {@code args}
   * using {@link java.text.MessageFormat}.
   * Returns {@code template} as-is when {@code args} is empty.
   *
   * @param locale locale used for formatting; {@code null} is treated as {@link Locale#ROOT}
   * @param template message template
   * @param args arguments to substitute
   * @return formatted string
   */
  public static String formatWithArgs(@Nullable Locale locale, String template, Object[] args) {
    if (args.length == 0) {
      return template;
    }
    Locale effectiveLocale = locale != null ? locale : Locale.ROOT;
    return new MessageFormat(template, effectiveLocale).format(args);
  }

  /**
   * Formats {@code template} by substituting named placeholders (e.g., {@code {min}},
   * {@code {max}}) with the corresponding values from {@code argMap}.
   *
   * @param template the template containing named placeholders
   * @param argMap map of placeholder name to value
   * @return the template with all named placeholders replaced
   */
  public static String formatWithArgs(String template,
      Map<@NonNull String, @Nullable Object> argMap) {
    String result = template;

    for (Entry<@NonNull String, @Nullable Object> entry : argMap.entrySet()) {
      // Escape "{" and "}" to prevent errors at MessageFormat.
      result = result.replace("{" + entry.getKey() + "}",
          entry.getValue() == null ? "''"
              : Objects.requireNonNull(entry.getValue()).toString().replace("{", "'{'").replace("}",
                  "'}'"));
    }

    return result;
  }
}
