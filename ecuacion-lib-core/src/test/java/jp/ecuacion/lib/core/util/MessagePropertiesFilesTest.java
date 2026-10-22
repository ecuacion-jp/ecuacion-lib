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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests the message properties files of all the ecuacion-lib modules for each language.
 *
 * <p>Translations are maintained for many languages, and it is easy to forget to add a key
 *     or a whole file to some of them. This test reads the properties files in
 *     {@code src/main/resources} of the ecuacion-lib modules next to this module
 *     and checks that every language has the same files, keys and placeholders
 *     as the default (English) files.</p>
 */
@DisplayName("message properties files of all languages")
public class MessagePropertiesFilesTest {

  /** Files which are not message files. */
  private static final Set<String> EXCLUDED_BASE_NAMES = Set.of("version_ecuacion-lib");

  /**
   * Base names which have only language-specific files, mapped to the base name whose keys
   *     those files must have.
   *
   * <p>{@code ValidationMessages_lib_core_xx.properties} exists only for the languages
   *     Hibernate Validator does not support. It has the same Jakarta standard constraint keys
   *     as {@code ValidationMessagesWithItemNames_lib_core.properties}.</p>
   */
  private static final Map<String, String> BASE_NAMES_WITHOUT_DEFAULT_FILE =
      Map.of("ValidationMessages_lib_core", "ValidationMessagesWithItemNames_lib_core");

  /** Matches the locale part at the end of a file name like {@code _ja} or {@code _zh_Hant}. */
  private static final Pattern LOCALE_SUFFIX =
      Pattern.compile("^(.+?)_([a-z]{2,3}(?:_[A-Z][a-z]{3})?(?:_[A-Z]{2})?)$");

  private static final Pattern EL_EXPRESSION = Pattern.compile("\\$\\{[^}]*}");
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{[A-Za-z0-9_.]+}");
  private static final Pattern SYMBOL_KEY = Pattern.compile("\\.(prepend|append)Symbol(\\.|$)");

  /** base name -> (locale -> properties). The default file has the locale {@code ""}. */
  private static final Map<String, Map<String, Properties>> BUNDLES = new TreeMap<>();

  @BeforeAll
  public static void beforeAll() throws IOException {
    List<Path> resourceDirs;
    try (Stream<Path> modules = Files.list(Path.of(".."))) {
      resourceDirs = modules
          .filter(p -> p.getFileName().toString().startsWith("ecuacion-lib-"))
          .map(p -> p.resolve("src/main/resources")).filter(Files::isDirectory).toList();
    }

    for (Path dir : resourceDirs) {
      try (Stream<Path> files = Files.list(dir)) {
        for (Path file : files.filter(p -> p.toString().endsWith(".properties")).toList()) {
          String name = file.getFileName().toString().replaceFirst("\\.properties$", "");
          Matcher m = LOCALE_SUFFIX.matcher(name);
          String baseName = m.matches() ? m.group(1) : name;
          String locale = m.matches() ? m.group(2) : "";
          if (EXCLUDED_BASE_NAMES.contains(baseName)) {
            continue;
          }

          Properties props = new Properties();
          try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            props.load(reader);
          }
          Objects.requireNonNull(BUNDLES.computeIfAbsent(baseName, k -> new TreeMap<>()))
              .put(locale, props);
        }
      }
    }
  }

  @Test
  @DisplayName("message files are found")
  void filesFound() {
    assertThat(BUNDLES).containsKeys("messages_lib_core", "ValidationMessages_lib_validation");
  }

  @Test
  @DisplayName("every language has all the message files")
  void everyLanguageHasAllFiles() {
    Set<String> languages =
        new TreeSet<>(Objects.requireNonNull(BUNDLES.get("messages_lib_core")).keySet());
    languages.remove("");

    List<String> missing = new ArrayList<>();
    for (Map.Entry<String, Map<String, Properties>> bundle : BUNDLES.entrySet()) {
      if (BASE_NAMES_WITHOUT_DEFAULT_FILE.containsKey(bundle.getKey())) {
        continue;
      }
      for (String language : languages) {
        if (!bundle.getValue().containsKey(language)) {
          missing.add(bundle.getKey() + "_" + language + ".properties");
        }
      }
    }

    assertThat(missing).as("missing files").isEmpty();
  }

  @Test
  @DisplayName("every language file has the same keys as the default file")
  void sameKeys() {
    List<String> errors = new ArrayList<>();
    for (Map.Entry<String, Map<String, Properties>> bundle : BUNDLES.entrySet()) {
      Properties defaultProps = getDefaultProps(bundle.getKey());
      if (defaultProps == null) {
        errors.add(bundle.getKey() + ": the default file does not exist");
        continue;
      }

      for (Map.Entry<String, Properties> entry : bundle.getValue().entrySet()) {
        Set<String> expected = defaultProps.stringPropertyNames();
        Set<String> actual = entry.getValue().stringPropertyNames();
        String file = bundle.getKey() + "_" + entry.getKey();

        Set<String> missingKeys = new TreeSet<>(expected);
        missingKeys.removeAll(actual);
        missingKeys.forEach(k -> errors.add(file + ": missing key " + k));

        Set<String> extraKeys = new TreeSet<>(actual);
        extraKeys.removeAll(expected);
        extraKeys.forEach(k -> errors.add(file + ": unknown key " + k));
      }
    }

    assertThat(errors).as("key differences").isEmpty();
  }

  @Test
  @DisplayName("every message keeps the placeholders of the default message")
  void samePlaceholders() {
    List<String> errors = new ArrayList<>();
    for (Map.Entry<String, Map<String, Properties>> bundle : BUNDLES.entrySet()) {
      Properties defaultProps = getDefaultProps(bundle.getKey());
      if (defaultProps == null) {
        continue;
      }

      for (Map.Entry<String, Properties> entry : bundle.getValue().entrySet()) {
        for (String key : entry.getValue().stringPropertyNames()) {
          String defaultValue = defaultProps.getProperty(key);
          if (defaultValue == null) {
            continue;
          }

          List<String> expected = placeholders(defaultValue);
          if (BASE_NAMES_WITHOUT_DEFAULT_FILE.containsKey(bundle.getKey())) {
            // Messages without item names have neither the item name nor the input value.
            expected.removeAll(List.of("{0}", "{invalidValue}"));
          }
          List<String> actual =
              placeholders(Objects.requireNonNull(entry.getValue().getProperty(key)));
          if (!expected.equals(actual)) {
            errors.add(bundle.getKey() + "_" + entry.getKey() + ": " + key + " has " + actual
                + " but " + expected + " is expected");
          }
        }
      }
    }

    assertThat(errors).as("placeholder differences").isEmpty();
  }

  @Test
  @DisplayName("message files do not use ASCII apostrophes outside EL expressions")
  void noAsciiApostrophes() {
    // Messages with item names are formatted by MessageFormat, which treats "'" as a quote,
    // so a placeholder after "'" is not replaced. Use "’" instead.
    // Item name / value symbols are excluded since they are passed to MessageFormat
    // as arguments, not as a part of the pattern.
    List<String> errors = new ArrayList<>();
    for (Map.Entry<String, Map<String, Properties>> bundle : BUNDLES.entrySet()) {
      for (Map.Entry<String, Properties> entry : bundle.getValue().entrySet()) {
        for (String key : entry.getValue().stringPropertyNames()) {
          String value = Objects.requireNonNull(entry.getValue().getProperty(key));
          if (!SYMBOL_KEY.matcher(key).find()
              && EL_EXPRESSION.matcher(value).replaceAll("").contains("'")) {
            errors.add(bundle.getKey() + (entry.getKey().isEmpty() ? "" : "_" + entry.getKey())
                + ": " + key);
          }
        }
      }
    }

    assertThat(errors).as("messages with ASCII apostrophes").isEmpty();
  }

  /**
   * Returns the default properties of the base name. For base names without the default file,
   *     the default file of the base name they refer to is returned.
   */
  private static @Nullable Properties getDefaultProps(String baseName) {
    String defaultBaseName = BASE_NAMES_WITHOUT_DEFAULT_FILE.getOrDefault(baseName, baseName);
    Map<String, Properties> map = BUNDLES.get(defaultBaseName);
    return map == null ? null : map.get("");
  }

  /**
   * Returns placeholders like {@code {0}}, {@code {min}} and EL expressions in sorted order.
   *     Strings in EL expressions are translated, so EL expressions are compared
   *     only by the expression before {@code ?}.
   */
  private static List<String> placeholders(String value) {
    List<String> list = new ArrayList<>();
    Matcher el = EL_EXPRESSION.matcher(value);
    while (el.find()) {
      list.add(el.group().replaceFirst("\\?.*", "?"));
    }

    Matcher m = PLACEHOLDER.matcher(EL_EXPRESSION.matcher(value).replaceAll(""));
    while (m.find()) {
      list.add(m.group());
    }

    list.sort(Comparator.naturalOrder());
    return list;
  }
}
