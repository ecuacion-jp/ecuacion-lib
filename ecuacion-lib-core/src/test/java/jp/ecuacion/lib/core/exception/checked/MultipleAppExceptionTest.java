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
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

public class MultipleAppExceptionTest extends TestTools {

  @Test
  public void test01_constructor_01_list_02_argSizeIsZero() {
    try {
      @SuppressWarnings("unused")
      MultipleAppException ex = new MultipleAppException(new ArrayList<@NonNull SingleAppException>());
      fail();

    } catch (RuntimeException npe) {
      assertTrue(true);
    }
  }

  @SuppressWarnings("null")
  @Test
  public void test01_constructor_01_list_03_valid() {
    List<@NonNull SingleAppException> list = new ArrayList<>();
    list.add(new BizLogicAppException("TEST_KEY"));
    list.add(new BizLogicAppException("TEST_KEY"));
    list.add(new BizLogicAppException("TEST_KEY"));
    MultipleAppException ex = new MultipleAppException(list);

    assertEquals(3, list.size());
    assertEquals("TEST_KEY", ((BizLogicAppException) ex.getList().get(0)).getMessageId());
    assertEquals("TEST_KEY", ((BizLogicAppException) ex.getList().get(1)).getMessageId());
    assertEquals("TEST_KEY", ((BizLogicAppException) ex.getList().get(2)).getMessageId());
  }
}
