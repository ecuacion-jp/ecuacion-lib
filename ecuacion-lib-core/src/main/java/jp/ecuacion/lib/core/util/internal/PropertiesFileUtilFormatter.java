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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.util.LocaleUtil;
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
   * @param locale locale used for formatting; {@code null} is treated as the fallback locale
   *     (see {@link jp.ecuacion.lib.core.util.LocaleUtil#getFallbackLocale()})
   * @param template message template
   * @param args arguments to substitute
   * @return formatted string
   */
  public static String formatWithArgs(@Nullable Locale locale, String template, Object[] args) {
    if (args.length == 0) {
      return template;
    }
    Locale effectiveLocale = locale != null ? locale : LocaleUtil.getFallbackLocale();
    return new MessageFormat(template, effectiveLocale).format(args);
  }

  private static final Pattern NAMED_PLACEHOLDER = Pattern.compile("\\{([^{}]+)\\}");

  /**
   * Formats {@code template} by substituting named placeholders (e.g., {@code {min}},
   * {@code {max}}) with the corresponding values from {@code argMap}.
   *
   * <p>The template is scanned once, so placeholders contained in the substituted values
   *     are not substituted again. Placeholders not in {@code argMap}
   *     (like {@code {0}}) are left unchanged. A {@code null} value is substituted
   *     with an empty string.</p>
   *
   * <p>When the result is formatted by {@link java.text.MessageFormat} afterwards,
   *     specify {@code true} to {@code escapesForMessageFormat}.
   *     Then each value is quoted so that {@code '}, {@code {} and {@code }} in it
   *     are displayed as they are after {@code MessageFormat}.
   *     Otherwise the values are substituted as they are.</p>
   *
   * @param template the template containing named placeholders
   * @param argMap map of placeholder name to value
   * @param escapesForMessageFormat whether the result is formatted by {@code MessageFormat}
   * @return the template with all named placeholders replaced
   */
  public static String formatWithNamedArgs(String template,
      Map<@NonNull String, @Nullable Object> argMap, boolean escapesForMessageFormat) {
    Matcher matcher = NAMED_PLACEHOLDER.matcher(template);
    StringBuilder sb = new StringBuilder();

    while (matcher.find()) {
      String name = matcher.group(1);
      if (!argMap.containsKey(name)) {
        matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
        continue;
      }

      Object value = argMap.get(name);
      String str = value == null ? "" : value.toString();
      matcher.appendReplacement(sb,
          Matcher.quoteReplacement(escapesForMessageFormat ? escapeForMessageFormat(str) : str));
    }

    matcher.appendTail(sb);
    return sb.toString();
  }

  /**
   * Escapes {@code value} so that {@link java.text.MessageFormat} outputs it as it is.
   */
  private static String escapeForMessageFormat(String value) {
    if (value.indexOf('\'') < 0 && value.indexOf('{') < 0 && value.indexOf('}') < 0) {
      return value;
    }

    return "'" + value.replace("'", "''") + "'";
  }
}
