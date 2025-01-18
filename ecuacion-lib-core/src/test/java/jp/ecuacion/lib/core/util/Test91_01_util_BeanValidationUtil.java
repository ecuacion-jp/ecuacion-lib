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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Objects;
import jp.ecuacion.lib.core.TestTools;
import jp.ecuacion.lib.core.beanvalidation.bean.BeanValidationErrorInfoBean;
import jp.ecuacion.lib.core.exception.checked.BeanValidationAppException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.SingleAppException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Test91_01_util_BeanValidationUtil extends TestTools {

  private BeanValidationUtil util;

  @BeforeEach
  public void before() {
    util = new BeanValidationUtil();
  }

  @Test
  public void test11_validateThenReturn_01_object_locale_01_objectがnull() {
    try {
      util.validateThenReturn(null, Locale.getDefault());
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test11_validateThenReturn_01_object_locale_02_localeがnull() {
    try {
      util.validateThenReturn(new SampleObj(), null);

    } catch (NullPointerException npe) {
      fail();
    }
  }

  @Test
  public void test11_validateThenReturn_01_object_locale_03_正常() {
    MultipleAppException exList = util.validateThenReturn(new SampleObj(), Locale.ENGLISH);

    Assertions.assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    BeanValidationAppException exNotNull = null;
    BeanValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      BeanValidationAppException bvEx = (BeanValidationAppException) singleEx;
      BeanValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    Assertions.assertThat(exNotNull).isNotEqualTo(null);
    Objects.requireNonNull(exNotNull);
    Assertions.assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must not be null");

    Assertions.assertThat(exMin).isNotEqualTo(null);
    Objects.requireNonNull(exMin);
    Assertions.assertThat(exMin.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must be greater than or equal to 3");
  }

  @Test
  public void test11_validateThenReturn_01_object_locale_11_locale指定() {
    MultipleAppException exList = util.validateThenReturn(new SampleObj(), Locale.JAPANESE);

    Assertions.assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    BeanValidationAppException exNotNull = null;
    BeanValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      BeanValidationAppException bvEx = (BeanValidationAppException) singleEx;
      BeanValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    Assertions.assertThat(exNotNull).isNotEqualTo(null);
    Objects.requireNonNull(exNotNull);
    Assertions.assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("null は許可されていません");

    Assertions.assertThat(exMin).isNotEqualTo(null);
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
    BeanValidationAppException exNotNull = null;
    BeanValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      BeanValidationAppException bvEx = (BeanValidationAppException) singleEx;
      BeanValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    Assertions.assertThat(exNotNull).isNotEqualTo(null);
    Objects.requireNonNull(exNotNull);
    Assertions.assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must not be null");

    Assertions.assertThat(exMin).isNotEqualTo(null);
    Objects.requireNonNull(exMin);
    Assertions.assertThat(exMin.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must be greater than or equal to 3");
  }

  @Test
  public void test11_validateThenReturn_02_object_11_defaultLocaleを指定() {
    Locale.setDefault(Locale.JAPANESE);
    MultipleAppException exList = util.validateThenReturn(new SampleObj());

    assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    BeanValidationAppException exNotNull = null;
    BeanValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      BeanValidationAppException bvEx = (BeanValidationAppException) singleEx;
      BeanValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    assertThat(exNotNull).isNotEqualTo(null);
    Objects.requireNonNull(exNotNull);
    assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("null は許可されていません");

    assertThat(exMin).isNotEqualTo(null);
    Objects.requireNonNull(exMin);
    assertThat(exMin.getBeanValidationErrorInfoBean().getMessage()).isEqualTo("3 以上の値にしてください");
  }

  @Test
  public void test21_validateThenThrow_01_object_locale_01_objectがnull()
      throws MultipleAppException {
    try {
      util.validateThenThrow(null, Locale.getDefault());
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test21_validateThenThrow_01_object_locale_02_localeがnull()
      throws MultipleAppException {
    try {
      util.validateThenThrow(new SampleObj(), null);

    } catch (MultipleAppException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test21_validateThenThrow_01_object_locale_03_正常() {
    MultipleAppException exList = null;
    try {
      util.validateThenThrow(new SampleObj(), Locale.ENGLISH);
      fail();

    } catch (MultipleAppException ex) {
      exList = ex;

    } catch (Exception ex) {
      fail();
    }

    assertThat(exList).isNotEqualTo(null);
    Objects.requireNonNull(exList);
    assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    BeanValidationAppException exNotNull = null;
    BeanValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      BeanValidationAppException bvEx = (BeanValidationAppException) singleEx;
      BeanValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    assertThat(exNotNull).isNotEqualTo(null);
    Objects.requireNonNull(exNotNull);
    assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must not be null");

    assertThat(exMin).isNotEqualTo(null);
    Objects.requireNonNull(exMin);
    assertThat(exMin.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must be greater than or equal to 3");
  }

  @Test
  public void test21_validateThenThrow_01_object_locale_11_locale指定() {
    MultipleAppException exList = null;
    try {
      util.validateThenThrow(new SampleObj(), Locale.JAPANESE);
      fail();

    } catch (MultipleAppException ex) {
      exList = ex;

    } catch (Exception ex) {
      fail();
    }

    assertThat(exList).isNotEqualTo(null);
    Objects.requireNonNull(exList);
    assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    BeanValidationAppException exNotNull = null;
    BeanValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      BeanValidationAppException bvEx = (BeanValidationAppException) singleEx;
      BeanValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    assertThat(exNotNull).isNotEqualTo(null);
    Objects.requireNonNull(exNotNull);
    assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("null は許可されていません");

    assertThat(exMin).isNotEqualTo(null);
    Objects.requireNonNull(exMin);
    assertThat(exMin.getBeanValidationErrorInfoBean().getMessage()).isEqualTo("3 以上の値にしてください");
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

    assertThat(exList).isNotEqualTo(null);
    Objects.requireNonNull(exList);
    assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    BeanValidationAppException exNotNull = null;
    BeanValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      BeanValidationAppException bvEx = (BeanValidationAppException) singleEx;
      BeanValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    assertThat(exNotNull).isNotEqualTo(null);
    Objects.requireNonNull(exNotNull);
    assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must not be null");

    assertThat(exMin).isNotEqualTo(null);
    Objects.requireNonNull(exMin);
    assertThat(exMin.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("must be greater than or equal to 3");
  }

  @Test
  public void test21_validateThenThrow_02_object_11_defaultLocaleを指定() {
    Locale.setDefault(Locale.JAPANESE);
    MultipleAppException exList = null;
    try {
      util.validateThenThrow(new SampleObj());
      fail();

    } catch (MultipleAppException ex) {
      exList = ex;

    } catch (Exception ex) {
      fail();
    }

    assertThat(exList).isNotEqualTo(null);
    Objects.requireNonNull(exList);
    assertThat(exList.getList().size()).isEqualTo(2);
    // listの順序は保証されていないはずなので、取得したい対象を確認の上変数に登録
    BeanValidationAppException exNotNull = null;
    BeanValidationAppException exMin = null;
    for (SingleAppException singleEx : exList.getList()) {
      BeanValidationAppException bvEx = (BeanValidationAppException) singleEx;
      BeanValidationErrorInfoBean bean = bvEx.getBeanValidationErrorInfoBean();
      if (bean.getMessageId().equals("jakarta.validation.constraints.NotNull")) {
        exNotNull = bvEx;

      } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
        exMin = bvEx;
      }
    }

    assertThat(exNotNull).isNotEqualTo(null);
    Objects.requireNonNull(exNotNull);
    assertThat(exNotNull.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("null は許可されていません");

    Assertions.assertThat(exMin).isNotEqualTo(null);
    Objects.requireNonNull(exMin);
    Assertions.assertThat(exMin.getBeanValidationErrorInfoBean().getMessage())
        .isEqualTo("3 以上の値にしてください");
  }

  public static class SampleObj {
    @NotNull
    public String str1 = null;

    @Min(3)
    public int int1 = 2;
  }
}
