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

import jp.ecuacion.lib.core.TestTools;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireNonNullException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class BizLogicAppExceptionTest extends TestTools {
  private static final String SAMPLE_MSG_ID = "MSG_ID";

  @Test
  public void test01_コンストラクタ_01_messageId_01_引数がnull() {
    try {
      @SuppressWarnings("unused")
      BizLogicAppException ex = new BizLogicAppException(null);
      fail();

    } catch (RequireNonNullException npe) {
      assertTrue(true);

    } catch (Exception ex) {
      fail();
    }
  }

  @Test
  public void test01_コンストラクタ_01_messageId_02_引数がnull以外() {
    BizLogicAppException ex = new BizLogicAppException(SAMPLE_MSG_ID);
    Assertions.assertThat(ex.getMessageId()).isEqualTo(SAMPLE_MSG_ID);
  }

  @Test
  public void test01_コンストラクタ_02_messageId_messageArgs_01_全部null() {
    try {
      @SuppressWarnings("unused")
      BizLogicAppException ex =
          new BizLogicAppException((String) null, (String[]) null);
      fail();

    } catch (RequireNonNullException npe) {
      assertTrue(true);

    } catch (Exception ex) {
      fail();
    }
  }

  @Test
  public void test01_コンストラクタ_02_messageId_messageArgs_02_messageId以外null() {
    BizLogicAppException ex = new BizLogicAppException(SAMPLE_MSG_ID);

    Assertions.assertThat(ex.getMessageId()).isEqualTo(SAMPLE_MSG_ID);
    assertFalse(ex.getMessageArgs() == null);
    Assertions.assertThat(ex.getMessageArgs().length).isEqualTo(0);
  }

  @Test
  public void test01_コンストラクタ_02_messageId_messageArgs_03_正常() {
    BizLogicAppException ex = new BizLogicAppException(SAMPLE_MSG_ID, "abc");

    Assertions.assertThat(ex.getMessageId()).isEqualTo(SAMPLE_MSG_ID);
    Assertions.assertThat(ex.getMessageArgs().length).isEqualTo(1);
    Assertions.assertThat(ex.getMessageArgs()[0].getArgString()).isEqualTo("abc");
  }

  @Test
  public void test01_コンストラクタ_12_locale_messageId_messageArgs_02_messageId以外null() {
    BizLogicAppException ex = new BizLogicAppException("TEST_KEY");

    Assertions.assertThat(ex.getMessageId()).isEqualTo("TEST_KEY");
    Assertions.assertThat(ex.getMessageArgs() == null).isFalse();
    Assertions.assertThat(ex.getMessageArgs().length).isEqualTo(0);
  }

  // @Test
  // public void test11_getMessageArgMapの取得_01_パラメータなし() {
  // BizLogicAppException ex =
  // new BizLogicAppException(SAMPLE_MSG_ID);
  //
  // assertThat(ex.getMessageArgMap()).isNotEqualTo(null);
  // assertThat(ex.getMessageArgMap().size()).isEqualTo(0));
  // }

  // @Test
  // public void test11_getMessageArgMapの取得_02_パラメータあり() {
  // BizLogicAppException ex =
  // new BizLogicAppException(SAMPLE_MSG_ID, "abc", "def");
  //
  // assertThat(ex.getMessageArgMap().size()).isEqualTo(2));
  // //実はmapはlinkedHashMapなので順序が守られている
  // assertThat(ex.getMessageArgMap().get("$0")).isEqualTo("abc"));
  // assertThat(ex.getMessageArgMap().get("$1")).isEqualTo("def"));
  // }
}
