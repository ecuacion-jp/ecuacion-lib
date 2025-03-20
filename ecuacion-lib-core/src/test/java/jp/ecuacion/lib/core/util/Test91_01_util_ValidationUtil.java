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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Objects;
import jp.ecuacion.lib.core.TestTools;
import jp.ecuacion.lib.core.exception.checked.ValidationAppException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.SingleAppException;
import jp.ecuacion.lib.core.jakartavalidation.bean.ValidationErrorInfoBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Test91_01_util_ValidationUtil extends TestTools {

  private ValidationUtil util;

  @BeforeEach
  public void before() {
    util = new ValidationUtil();
  }

  @Test
  public void test11_validateThenReturn_01_object_locale_01_objectがnull() {
    try {
      util.validateThenReturn(null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test11_validateThenReturn_01_object_locale_11_locale指定() {
    MultipleAppException exList = util.validateThenReturn(new SampleObj());

    Assertions.assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    ValidationAppException exNotNull = null;
    ValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      ValidationAppException bvEx = (ValidationAppException) singleEx;
      ValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    Assertions.assertThat(exNotNull == null).isFalse();
    Objects.requireNonNull(exNotNull);
    Assertions.assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("null は許可されていません");

    Assertions.assertThat(exMin == null).isFalse();
    Objects.requireNonNull(exMin);
    Assertions.assertThat(exMin.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("3 以上の値にしてください");
  }

  @Test
  public void test11_validateThenReturn_02_object_01_objectがnull() {
    try {
      util.validateThenReturn(null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test11_validateThenReturn_02_object_02_正常() {
    Locale.setDefault(Locale.ENGLISH);
    MultipleAppException exList = util.validateThenReturn(new SampleObj());

    Assertions.assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    ValidationAppException exNotNull = null;
    ValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      ValidationAppException bvEx = (ValidationAppException) singleEx;
      ValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    Assertions.assertThat(exNotNull == null).isFalse();
    Objects.requireNonNull(exNotNull);
    Assertions.assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must not be null");

    Assertions.assertThat(exMin == null).isFalse();
    Objects.requireNonNull(exMin);
    Assertions.assertThat(exMin.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must be greater than or equal to 3");
  }

  @Test
  public void test11_validateThenReturn_02_object_11_defaultLocaleを指定() {
    MultipleAppException exList = util.validateThenReturn(new SampleObj());

    Assertions.assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    ValidationAppException exNotNull = null;
    ValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      ValidationAppException bvEx = (ValidationAppException) singleEx;
      ValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    Assertions.assertThat(exNotNull == null).isFalse();
    Objects.requireNonNull(exNotNull);
    Assertions.assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must not be null");

    Assertions.assertThat(exMin == null).isFalse();
    Objects.requireNonNull(exMin);
    Assertions.assertThat(exMin.getBeanValidationErrorInfoBean().getMessage()).isEqualTo("must be greater than or equal to 3");
  }

  @Test
  public void test21_validateThenThrow_01_object_locale_03_正常() {
    MultipleAppException exList = null;
    try {
      util.validateThenThrow(new SampleObj());
      fail();

    } catch (MultipleAppException ex) {
      exList = ex;

    } catch (Exception ex) {
      fail();
    }

    Assertions.assertThat(exList == null).isFalse();
    Objects.requireNonNull(exList);
    Assertions.assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    ValidationAppException exNotNull = null;
    ValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      ValidationAppException bvEx = (ValidationAppException) singleEx;
      ValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    Assertions.assertThat(exNotNull == null).isFalse();
    Objects.requireNonNull(exNotNull);
    Assertions.assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must not be null");

    Assertions.assertThat(exMin == null).isFalse();
    Objects.requireNonNull(exMin);
    Assertions.assertThat(exMin.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must be greater than or equal to 3");
  }

  @Test
  public void test21_validateThenThrow_01_object_locale_11_locale指定() {
    MultipleAppException exList = null;
    try {
      util.validateThenThrow(new SampleObj());
      fail();

    } catch (MultipleAppException ex) {
      exList = ex;

    } catch (Exception ex) {
      fail();
    }

    Assertions.assertThat(exList == null).isFalse();
    Objects.requireNonNull(exList);
    Assertions.assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    ValidationAppException exNotNull = null;
    ValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      ValidationAppException bvEx = (ValidationAppException) singleEx;
      ValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    Assertions.assertThat(exNotNull == null).isFalse();
    Objects.requireNonNull(exNotNull);
    Assertions.assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must not be null");

    Assertions.assertThat(exMin == null).isFalse();
    Objects.requireNonNull(exMin);
    Assertions.assertThat(exMin.getBeanValidationErrorInfoBean().getMessage()).isEqualTo("must be greater than or equal to 3");
  }

  @Test
  public void test21_validateThenThrow_02_object_01_objectがnull() throws MultipleAppException {
    try {
      util.validateThenThrow(null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test21_validateThenThrow_02_object_02_正常() {
    Locale.setDefault(Locale.ENGLISH);
    MultipleAppException exList = null;
    try {
      util.validateThenThrow(new SampleObj());
      fail();

    } catch (MultipleAppException ex) {
      exList = ex;

    } catch (Exception ex) {
      fail();
    }

    Assertions.assertThat(exList == null).isFalse();
    Objects.requireNonNull(exList);
    Assertions.assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    ValidationAppException exNotNull = null;
    ValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      ValidationAppException bvEx = (ValidationAppException) singleEx;
      ValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    Assertions.assertThat(exNotNull == null).isFalse();
    Objects.requireNonNull(exNotNull);
    Assertions.assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must not be null");

    Assertions.assertThat(exMin == null).isFalse();
    Objects.requireNonNull(exMin);
    Assertions.assertThat(exMin.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must be greater than or equal to 3");
  }

  @Test
  public void test21_validateThenThrow_02_object_11_defaultLocaleを指定() {
    MultipleAppException exList = null;
    try {
      util.validateThenThrow(new SampleObj());
      fail();

    } catch (MultipleAppException ex) {
      exList = ex;

    } catch (Exception ex) {
      fail();
    }

    Assertions.assertThat(exList == null).isFalse();
    Objects.requireNonNull(exList);
    Assertions.assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    ValidationAppException exNotNull = null;
    ValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      ValidationAppException bvEx = (ValidationAppException) singleEx;
      ValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    Assertions.assertThat(exNotNull == null).isFalse();
    Objects.requireNonNull(exNotNull);
    Assertions.assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must not be null");

    Assertions.assertThat(exMin == null).isFalse();
    Objects.requireNonNull(exMin);
    Assertions.assertThat(exMin.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must be greater than or equal to 3");
  }

  public static class SampleObj {
    @NotNull
    public String str1 = null;

    @Min(3)
    public int int1 = 2;
  }
}
