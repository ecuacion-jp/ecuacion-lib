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
import org.junit.jupiter.api.Test;

public class BizLogicAppExceptionTest extends TestTools {
  private static final String SAMPLE_MSG_ID = "MSG_ID";

  @Test
  public void test01_constructor_01_messageId_01_argIsNull() {
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
  public void test01_constructor_01_messageId_02_argIsNotNull() {
    BizLogicAppException ex = new BizLogicAppException(SAMPLE_MSG_ID);
    assertEquals(SAMPLE_MSG_ID, ex.getMessageId());
  }

  @Test
  public void test01_constructor_02_messageId_messageArgs_01_allNull() {
    try {
      @SuppressWarnings("unused")
      BizLogicAppException ex =
          new BizLogicAppException((String) null);
      fail();

    } catch (RequireNonNullException npe) {
      assertTrue(true);

    } catch (Exception ex) {
      fail();
    }
  }

  @Test
  public void test01_constructor_02_messageId_messageArgs_02_allExceptMessageIdAreNull() {
    BizLogicAppException ex = new BizLogicAppException(SAMPLE_MSG_ID);

    assertEquals(SAMPLE_MSG_ID, ex.getMessageId());
    assertFalse(ex.getMessageArgs() == null);
    assertEquals(0, ex.getMessageArgs().length);
  }

  @Test
  public void test01_constructor_02_messageId_messageArgs_03_valid() {
    BizLogicAppException ex = new BizLogicAppException(SAMPLE_MSG_ID, "abc");

    assertEquals(SAMPLE_MSG_ID, ex.getMessageId());
    assertEquals(1, ex.getMessageArgs().length);
    assertEquals("abc", ex.getMessageArgs()[0].getArgString());
  }

  @Test
  public void test01_constructor_12_locale_messageId_messageArgs_02_allExceptMessageIdAreNull() {
    BizLogicAppException ex = new BizLogicAppException("TEST_KEY");

    assertEquals("TEST_KEY", ex.getMessageId());
    assertFalse(ex.getMessageArgs() == null);
    assertEquals(0, ex.getMessageArgs().length);
  }

  // @Test
  // public void test11_getMessageArgMap_01_noParams() {
  // BizLogicAppException ex =
  // new BizLogicAppException(SAMPLE_MSG_ID);
  //
  // assertThat(ex.getMessageArgMap()).isNotEqualTo(null);
  // assertThat(ex.getMessageArgMap().size()).isEqualTo(0));
  // }

  // @Test
  // public void test11_getMessageArgMap_02_withParams() {
  // BizLogicAppException ex =
  // new BizLogicAppException(SAMPLE_MSG_ID, "abc", "def");
  //
  // assertThat(ex.getMessageArgMap().size()).isEqualTo(2));
  // // The map is actually a LinkedHashMap, so the order is preserved.
  // assertThat(ex.getMessageArgMap().get("$0")).isEqualTo("abc"));
  // assertThat(ex.getMessageArgMap().get("$1")).isEqualTo("def"));
  // }
}
