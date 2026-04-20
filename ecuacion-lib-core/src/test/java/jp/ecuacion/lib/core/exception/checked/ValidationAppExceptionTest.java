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
package jp.ecuacion.lib.core.exception.checked;

import static org.junit.jupiter.api.Assertions.assertEquals;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.violation.Violations;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ValidationAppExceptionTest {

  private @Nullable ConstraintViolation<SampleObj> violation;

  @BeforeEach
  public void before() {
    Locale.setDefault(Locale.JAPANESE);

    SampleObj obj = new SampleObj();
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<SampleObj>> violationSet = validator.validate(obj);
    violation = violationSet.iterator().next();
  }

  @Test
  public void test01_storingValues_01_constraintViolationAddedToViolations() {
    Set<ConstraintViolation<SampleObj>> set = new HashSet<>();
    set.add(ObjectsUtil.requireNonNull(violation));

    Violations violations = new Violations().addAll(set);
    assertEquals(1, violations.getConstraintViolations().size());
  }

  @Test
  public void test02_obtaining_values() {
    final String className =
        "jp.ecuacion.lib.core.exception.checked." + "ValidationAppExceptionTest$SampleObj";

    ConstraintViolationBean<?> bean =
        ConstraintViolationBean.createConstraintViolationBean(ObjectsUtil.requireNonNull(violation));
    assertEquals("jakarta.validation.constraints.NotNull", bean.getValidatorClass());
    assertEquals("must not be null", bean.getMessage());
    assertEquals("{jakarta.validation.constraints.NotNull.message}", bean.getMessageTemplate());
    assertEquals(className, bean.getRootBean().getClass().getName());
    assertEquals(className, bean.getLeafBean().getClass().getName());
    String pp = bean.getItems()[0].getPropertyPath();
    assertEquals("str1", pp);
    assertEquals("null", bean.getInvalidValue());
  }

  @Test
  public void test11_obtaining_validatorClass() {
    ConstraintViolationBean<?> bean =
        ConstraintViolationBean.createConstraintViolationBean(ObjectsUtil.requireNonNull(violation));
    assertEquals("jakarta.validation.constraints.NotNull", bean.getValidatorClass());
  }

  @Test
  public void test12_obtainingToString() {
    String str =
        "message:must not be null\n" + "annotation:jakarta.validation.constraints.NotNull\n"
            + "rootClassName:jp.ecuacion.lib.core.exception.checked."
            + "ValidationAppExceptionTest$SampleObj\n"
            + "leafClassName:jp.ecuacion.lib.core.exception.checked."
            + "ValidationAppExceptionTest$SampleObj\n" + "propertyPath:str1\ninvalidValue:null";
    ConstraintViolationBean<?> bean =
        ConstraintViolationBean.createConstraintViolationBean(ObjectsUtil.requireNonNull(violation));
    assertEquals(str, bean.toString());
  }

  public static class SampleObj {
    @NotNull
    public @Nullable String str1;
  }
}
