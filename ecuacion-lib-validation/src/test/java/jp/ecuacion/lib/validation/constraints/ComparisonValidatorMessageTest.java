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
package jp.ecuacion.lib.validation.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Locale;
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests messages for Comparison validators (e.g. {@code @LessThanOrEqualTo}).
 *
 * <p>Covers the case where the annotation is placed at the class level of a bean
 * that is itself nested under a root bean (e.g. referenced via a {@code @Valid} field).
 * In that case the itemNameKey of {@code baselinePropertyPathItemName} must be resolved
 * in the same way as {@code propertyPath}, that is, its class part comes from the field
 * the nested bean is reached through (or {@code @ItemNameKeyClass} placed at that field).</p>
 */
@DisplayName("Comparison validators - message content")
public class ComparisonValidatorMessageTest {

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtil.addResourceBundlePostfix("lib-validation-test");
  }

  @Test
  @DisplayName("class part of baselinePropertyPathItemName is the field name "
      + "the nested bean is reached through")
  public void baselinePropertyPathItemNameUsesFieldName() {
    String msg = ExceptionUtil
        .getMessageList(validator.validate(new Outer()), Locale.ENGLISH).get(0);
    assertThat(msg).isEqualTo("must be less than or equal to the value of 'end date of inner'");
  }

  @Test
  @DisplayName("class part of baselinePropertyPathItemName is @ItemNameKeyClass "
      + "placed at the field the nested bean is reached through")
  public void baselinePropertyPathItemNameUsesItemNameKeyClassAtField() {
    String msg = ExceptionUtil
        .getMessageList(validator.validate(new OuterWithAnnotatedField()), Locale.ENGLISH)
        .get(0);
    assertThat(msg).isEqualTo("must be less than or equal to the value of 'end date'");
  }

  @ItemNameKeyClass("outerBean")
  private static class Outer {
    @SuppressWarnings("unused")
    @Valid
    private Inner inner = new Inner();
  }

  @ItemNameKeyClass("outerBean")
  private static class OuterWithAnnotatedField {
    @SuppressWarnings("unused")
    @ItemNameKeyClass("innerBean")
    @Valid
    private List<Inner> innerList = List.of(new Inner());
  }

  @ItemNameKeyClass("innerBean")
  @LessThanOrEqualTo(propertyPath = "startDate", baselinePropertyPath = "endDate")
  @SuppressWarnings("unused")
  private static class Inner {
    private String startDate = "2025-08-01";
    private String endDate = "2025-07-01";
  }
}
