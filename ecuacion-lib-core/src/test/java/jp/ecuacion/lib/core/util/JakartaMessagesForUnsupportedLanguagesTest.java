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

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests the messages of Jakarta standard constraints without item names.
 *
 * <p>For the languages Hibernate Validator does not support, the messages in
 *     {@code ValidationMessages_lib_core_xx.properties} are used. For the other languages,
 *     the messages of Hibernate Validator are used.</p>
 */
@DisplayName("messages of Jakarta standard constraints without item names")
public class JakartaMessagesForUnsupportedLanguagesTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  @DisplayName("ecuacion-lib messages are used for languages Hibernate Validator does not support")
  void unsupportedLanguage() {
    assertThat(getMessage(Locale.forLanguageTag("id"))).isEqualTo("tidak boleh null");
  }

  @Test
  @DisplayName("Hibernate Validator messages are used for languages it supports")
  void supportedLanguage() {
    assertThat(getMessage(Locale.ENGLISH)).isEqualTo("must not be null");
    assertThat(getMessage(Locale.JAPANESE)).isEqualTo("null は許可されていません");
  }

  private String getMessage(Locale locale) {
    return ExceptionUtil.getMessageList(validator.validate(new Bean()), locale).get(0);
  }

  @SuppressWarnings("unused")
  private static class Bean {
    @NotNull
    private @Nullable String field = null;
  }
}
