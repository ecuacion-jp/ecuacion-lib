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

import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.TestTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test21_21_exception_MultipleAppException extends TestTools {

  @Test
  public void test01_コンストラクタ_01_list_01_引数がnull() {
    try {
      @SuppressWarnings("unused")
      MultipleAppException ex = new MultipleAppException((List<SingleAppException>) null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test01_コンストラクタ_01_list_02_引数がサイズ0() {
    try {
      @SuppressWarnings("unused")
      MultipleAppException ex = new MultipleAppException(new ArrayList<SingleAppException>());
      fail();

    } catch (RuntimeException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test01_コンストラクタ_01_list_03_正常() {
    List<SingleAppException> list = new ArrayList<>();
    list.add(new BizLogicAppException("MSG1"));
    list.add(new BizLogicAppException("MSG2"));
    list.add(new BizLogicAppException("MSG3"));
    MultipleAppException ex = new MultipleAppException(list);

    Assertions.assertThat(list.size()).isEqualTo(3);
    Assertions.assertThat(((BizLogicAppException) ex.getList().get(0)).getMessageId()).isEqualTo("MSG1");
    Assertions.assertThat(((BizLogicAppException) ex.getList().get(1)).getMessageId()).isEqualTo("MSG2");
    Assertions.assertThat(((BizLogicAppException) ex.getList().get(2)).getMessageId()).isEqualTo("MSG3");
  }
}
