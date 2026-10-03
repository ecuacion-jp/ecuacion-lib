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
import java.util.Locale;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests propertyPath attributes of class validators referring to getters.
 *
 * <p>The resolution itself (field first, getter when no field exists, {@code isXxx()},
 *     exceptions and so on) is covered by {@code ReflectionUtilTest} in ecuacion-lib-core.
 *     This class covers that each propertyPath attribute of the validators goes through it.</p>
 */
@DisplayName("Class validators - propertyPath referring to getters "
    + "※resolution details are covered by ReflectionUtilTest")
public class PropertyPathGetterTest {

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtil.addResourceBundlePostfix("lib-validation-test");
  }

  @Test
  @DisplayName("@NotEmptyWhen: propertyPath and conditionPropertyPath refer to getters")
  public void whenValidator() {
    // condition satisfied (isFlagged() == true) -> value empty -> fail
    assertThat(validator.validate(new WhenBean(true, null))).hasSize(1);
    // condition satisfied -> value not empty -> pass
    assertThat(validator.validate(new WhenBean(true, "a"))).isEmpty();
    // condition not satisfied -> pass
    assertThat(validator.validate(new WhenBean(false, null))).isEmpty();
  }

  @Test
  @DisplayName("@NotEmptyWhen: conditionPropertyPath refers to a getter of a nested bean "
      + "reached through a getter, and conditionValuePropertyPath refers to a getter")
  public void whenValidatorNestedGetter() {
    assertThat(validator.validate(new NestedWhenBean("X", null))).hasSize(1);
    assertThat(validator.validate(new NestedWhenBean("Y", null))).isEmpty();
  }

  @Test
  @DisplayName("@LessThan: propertyPath and baselinePropertyPath refer to getters")
  public void comparisonValidator() {
    assertThat(validator.validate(new ComparisonBean(1, 2))).isEmpty();
    assertThat(validator.validate(new ComparisonBean(2, 1))).hasSize(1);
  }

  @Test
  @DisplayName("itemNameKey of baselinePropertyPath referring to a getter, "
      + "in a nested bean reached through a getter")
  public void baselinePropertyPathItemName() {
    String msg = ExceptionUtil
        .getMessageList(validator.validate(new Outer()), Locale.ENGLISH).get(0);
    assertThat(msg).isEqualTo("must be less than or equal to the value of 'end date of inner'");
  }

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "flagged",
      conditionValueBoolean = true)
  public static class WhenBean {
    private final boolean flag;
    private final @Nullable String val;

    WhenBean(boolean flag, @Nullable String val) {
      this.flag = flag;
      this.val = val;
    }

    public boolean isFlagged() {
      return flag;
    }

    public @Nullable String getValue() {
      return val;
    }
  }

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "child.code",
      conditionValuePropertyPath = "expectedCode")
  public static class NestedWhenBean {
    private final String childCode;
    private final @Nullable String val;

    NestedWhenBean(String childCode, @Nullable String val) {
      this.childCode = childCode;
      this.val = val;
    }

    public Child getChild() {
      return new Child(childCode);
    }

    public String getExpectedCode() {
      return "X";
    }

    public @Nullable String getValue() {
      return val;
    }
  }

  public static class Child {
    private final String cd;

    Child(String cd) {
      this.cd = cd;
    }

    public String getCode() {
      return cd;
    }
  }

  @LessThan(propertyPath = "start", baselinePropertyPath = "end")
  public static class ComparisonBean {
    private final Integer from;
    private final Integer to;

    ComparisonBean(Integer from, Integer to) {
      this.from = from;
      this.to = to;
    }

    public Integer getStart() {
      return from;
    }

    public Integer getEnd() {
      return to;
    }
  }

  public static class Outer {
    @Valid
    public Inner getInner() {
      return new Inner();
    }
  }

  @LessThanOrEqualTo(propertyPath = "startDate", baselinePropertyPath = "endDate")
  public static class Inner {
    public String getStartDate() {
      return "2025-08-01";
    }

    public String getEndDate() {
      return "2025-07-01";
    }
  }
}
