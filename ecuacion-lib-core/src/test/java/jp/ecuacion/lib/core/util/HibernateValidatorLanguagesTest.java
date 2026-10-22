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
import java.lang.module.ModuleReader;
import java.lang.module.ResolvedModule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests the languages of the messages Hibernate Validator provides.
 *
 * <p>For the languages Hibernate Validator does not support, ecuacion-lib provides
 *     the messages of Jakarta standard constraints in
 *     {@code ValidationMessages_lib_core_xx.properties}. When Hibernate Validator starts
 *     to support one of them, the file should be removed so that the messages of Hibernate
 *     Validator are used. This test detects any change in the languages of Hibernate
 *     Validator so that it is not overlooked when Hibernate Validator is upgraded.</p>
 */
@DisplayName("languages of Hibernate Validator messages")
public class HibernateValidatorLanguagesTest {

  /**
   * The languages Hibernate Validator supports.
   *
   * <p>When this test fails because Hibernate Validator changed its languages, update this list
   *     after the following.</p>
   * <ul>
   *   <li>If a language with {@code ValidationMessages_lib_core_xx.properties} was added,
   *       remove that file to use the messages of Hibernate Validator.</li>
   *   <li>If a new language was added, consider supporting it in ecuacion-lib.</li>
   * </ul>
   */
  private static final Set<String> KNOWN_LANGUAGES = Set.of("", "ar", "az", "cs", "da", "de",
      "en", "es", "fa", "fr", "hu", "it", "ja", "ko", "mn_MN", "nl", "pl", "pt", "pt_BR", "pt_PT",
      "ro", "ru", "sk", "tr", "uk", "zh", "zh_CN", "zh_TW");

  private static final Pattern HV_MESSAGES =
      Pattern.compile("^org/hibernate/validator/ValidationMessages(?:_(.+))?\\.properties$");
  private static final Pattern LIB_CORE_MESSAGES =
      Pattern.compile("^ValidationMessages_lib_core_(.+)\\.properties$");

  @Test
  @DisplayName("Hibernate Validator supports the known languages")
  void hibernateValidatorLanguages() throws IOException {
    assertThat(getHibernateValidatorLanguages()).as(
        "Hibernate Validator changed its languages. See the Javadoc of KNOWN_LANGUAGES.")
        .containsExactlyInAnyOrderElementsOf(KNOWN_LANGUAGES);
  }

  @Test
  @DisplayName("ValidationMessages_lib_core_xx exists only for languages Hibernate Validator"
      + " does not support")
  void libCoreMessagesOnlyForUnsupportedLanguages() throws IOException {
    Set<String> hvLanguages = getHibernateValidatorLanguages().stream()
        .map(l -> l.replaceFirst("_.*", "")).collect(Collectors.toSet());

    Set<String> libCoreLanguages;
    try (Stream<Path> files = Files.list(Path.of("src/main/resources"))) {
      libCoreLanguages = files.map(p -> LIB_CORE_MESSAGES.matcher(p.getFileName().toString()))
          .filter(Matcher::matches).map(m -> Objects.requireNonNull(m.group(1)))
          .collect(Collectors.toCollection(TreeSet::new));
    }

    assertThat(libCoreLanguages).as("ValidationMessages_lib_core_xx.properties should be removed"
        + " for the languages Hibernate Validator supports")
        .noneMatch(l -> hvLanguages.contains(l.replaceFirst("_.*", "")));
  }

  /**
   * Returns the languages of {@code org/hibernate/validator/ValidationMessages*.properties}.
   *
   * <p>The package is not opened by the module {@code org.hibernate.validator},
   *     so {@code Class#getResource} cannot read the files from other modules.
   *     {@code ModuleReader} is used to list them instead.</p>
   */
  private static Set<String> getHibernateValidatorLanguages() throws IOException {
    Optional<ResolvedModule> module =
        Objects.requireNonNull(ModuleLayer.boot()).configuration()
            .findModule("org.hibernate.validator");
    Assumptions.assumeTrue(module.isPresent(), "Hibernate Validator is not on the module path.");

    try (ModuleReader reader = module.get().reference().open();
        Stream<String> names = reader.list()) {
      return names.map(HV_MESSAGES::matcher).filter(Matcher::matches)
          .map(m -> m.group(1) == null ? "" : m.group(1))
          .collect(Collectors.toCollection(TreeSet::new));
    }
  }
}
