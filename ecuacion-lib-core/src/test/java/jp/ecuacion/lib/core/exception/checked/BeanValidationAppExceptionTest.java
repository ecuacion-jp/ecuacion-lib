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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Set;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BeanValidationAppExceptionTest {

  private ConstraintViolation<SampleObj> violation;

  @BeforeEach
  public void before() {
    Locale.setDefault(Locale.JAPANESE);

    SampleObj obj = new SampleObj();
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<SampleObj>> violationSet = validator.validate(obj);
    violation = violationSet.iterator().next();
  }

  @Test
  public void test01_値の格納_01_ConstarintViolationを引数に取るコンストラクタ_01_引数がnull() {
    try {
      // NPEが起きるのが正解
      @SuppressWarnings("unused")
      ValidationAppException ex = new ValidationAppException((ConstraintViolation<?>) null);
      Assertions.fail();

    } catch (NullPointerException npe) {
      // OK

    } catch (Exception e) {
      Assertions.fail();
    }
  }

  @Test
  public void test01_値の格納_01_ConstarintViolationを引数に取るコンストラクタ_11_引数が正常() {
    try {
      // findbugsに引っかからないよう、変数に入れた上でそれを使用（getStackTrace()）する；
      ValidationAppException ex = new ValidationAppException(violation);
      ex.getStackTrace();

    } catch (Exception e) {
      Assertions.fail();
    }
  }

  @Test
  public void test02_obtaining_values() {
    final String className = "jp.ecuacion.lib.core.exception.checked."
        + "BeanValidationAppExceptionTest$SampleObj";

    ValidationAppException ex = new ValidationAppException(violation);
    ConstraintViolationBean bean = ex.getConstraintViolationBean();
    Assertions.assertThat(bean.getValidatorClass()).isEqualTo("jakarta.validation.constraints.NotNull");
    Assertions.assertThat(bean.getOriginalMessage()).isEqualTo("null は許可されていません");
    Assertions.assertThat(bean.getMessageTemplate())
        .isEqualTo("jakarta.validation.constraints.NotNull.message");
    Assertions.assertThat(bean.getRootBean().getClass().getName()).isEqualTo(className);
    Assertions.assertThat(bean.getLeafBean().getClass().getName()).isEqualTo(className);
    Assertions.assertThat(bean.getItemPropertyPathsForForm()[0]).isEqualTo("str1");
    Assertions.assertThat(bean.getInvalidValue()).isEqualTo("null");
  }

  @Test
  public void test11_obtaining_messageId() {
    ValidationAppException ex = new ValidationAppException(violation);
    ConstraintViolationBean bean = ex.getConstraintViolationBean();
    Assertions.assertThat(bean.getMessageId()).isEqualTo("jakarta.validation.constraints.NotNull");
  }

  @Test
  public void test12_toStringの取得() {
    String str = "message:null は許可されていません\n" + "annotation:jakarta.validation.constraints.NotNull\n"
        + "rootClassName:jp.ecuacion.lib.core.exception.checked."
        + "BeanValidationAppExceptionTest$SampleObj\n"
        + "leafClassName:jp.ecuacion.lib.core.exception.checked."
        + "BeanValidationAppExceptionTest$SampleObj\n"
        + "propertyPath:str1\ninvalidValue:null";
    ValidationAppException ex = new ValidationAppException(violation);
    Assertions.assertThat(ex.toString()).isEqualTo(str);
  }

  // @Test
  // public void test13_getMessageArgMapの取得_01_パラメータなし() {
  // BeanValidationAppException ex = new BeanValidationAppException(violation);
  // Map<String, String> map = ex.getMessageArgMap();
  //
  // assertThat(map).isNotEqualTo(null);
  // assertThat(map.size()).isEqualTo(0)));
  // }

  // @Test
  // public void test13_getMessageArgMapの取得_02_パラメータあり() {
  // SampleWithParamObj obj = new SampleWithParamObj();
  // Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
  // Set<ConstraintViolation<SampleWithParamObj>> violationSet = validator.validate(obj);
  // ConstraintViolation<SampleWithParamObj> violation = violationSet.iterator().next();
  //
  // BeanValidationAppException ex = new BeanValidationAppException(violation);
  // Map<String, String> map = ex.getMessageArgMap();
  //
  // assertThat(map.size()).isEqualTo(1)));
  // assertThat(map.keySet().iterator().next()).isEqualTo("value")));
  // assertThat(map.get(map.keySet().iterator().next())).isEqualTo("3")));
  // }

  public static class SampleObj {
    @NotNull
    public String str1;
  }

  class SampleWithParamObj {
    @Min(value = 3)
    public Integer int1 = 2;
  }
}


