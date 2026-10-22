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
package jp.ecuacion.lib.validation.business.messages;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.validation.constraints.AnyNotEmpty;
import jp.ecuacion.lib.validation.constraints.BooleanString;
import jp.ecuacion.lib.validation.constraints.Condition;
import jp.ecuacion.lib.validation.constraints.GreaterThan;
import jp.ecuacion.lib.validation.constraints.NotEmptyWhen;
import jp.ecuacion.lib.validation.constraints.PatternWhen;
import jp.ecuacion.lib.validation.constraints.SizeString;
import jp.ecuacion.lib.validation.constraints.StringWhen;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests that the business-friendly messages of all the supported languages are built without
 *     unresolved placeholders.
 */
@DisplayName("business-friendly messages of all the supported languages")
public class AllLanguagesBusinessMessagesTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  static Stream<Locale> locales() throws IOException {
    try (Stream<Path> files = Files.list(Path.of("../ecuacion-lib-core/src/main/resources"))) {
      List<Locale> list = files.map(p -> p.getFileName().toString())
          .filter(n -> n.matches("^messages_lib_core_.+\\.properties$"))
          .map(n -> n.replaceFirst("^messages_lib_core_(.+)\\.properties$", "$1"))
          .map(l -> Locale.forLanguageTag(l.replace('_', '-'))).toList();
      return Stream.concat(Stream.of(Locale.ENGLISH), list.stream());
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("locales")
  @DisplayName("messages have no unresolved placeholders")
  void noUnresolvedPlaceholders(Locale locale) {
    for (boolean withItemNames : new boolean[] {false, true}) {
      List<String> messages =
          ExceptionUtil.getMessageList(validator.validate(new Bean()), locale, withItemNames);

      assertThat(messages).hasSize(13).allSatisfy(m -> assertThat(m).isNotBlank()
          .doesNotContain("{").doesNotContain("}").doesNotContain("${"));
    }
  }

  @AnyNotEmpty(propertyPath = {"a", "b"})
  @GreaterThan(propertyPath = "small", baselinePropertyPath = "large")
  @NotEmptyWhen(propertyPath = "field", conditionPropertyPath = "cond",
      conditionValueBoolean = true, emptyWhenConditionNotSatisfied = true)
  @StringWhen(propertyPath = "str", string = {"x"}, conditions = {
      @Condition(propertyPath = "cond2", valueString = {"p", "q"}),
      @Condition(propertyPath = "cond", valueBoolean = true)})
  @PatternWhen(propertyPath = "pat", regexp = "^[0-9]+$", conditionPropertyPath = "cond3",
      conditionValuePatternRegexp = "^a.*$")
  @SuppressWarnings("unused")
  private static class Bean {
    @NotNull
    private @Nullable String nn = null;
    @Size(min = 2, max = 3)
    private String size = "x";
    @DecimalMax("10")
    private BigDecimal decMax = new BigDecimal("11");
    @DecimalMin(value = "10", inclusive = false)
    private BigDecimal decMin = new BigDecimal("1");
    @Pattern(regexp = "^[a-z]+$")
    private String pattern = "A";
    @Digits(integer = 2, fraction = 1)
    private BigDecimal digits = new BigDecimal("123.45");
    @SizeString(min = 5, max = 6)
    private String sizeString = "abc";
    @BooleanString
    private String booleanString = "maybe";
    private @Nullable String a = null;
    private @Nullable String b = null;
    private Integer small = 1;
    private Integer large = 5;
    private String field = "";
    private boolean cond = true;
    private String str = "z";
    private String cond2 = "p";
    private String pat = "abc";
    private String cond3 = "abc";
  }
}
