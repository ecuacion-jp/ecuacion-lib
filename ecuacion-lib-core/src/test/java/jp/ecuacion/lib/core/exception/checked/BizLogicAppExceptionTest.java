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
import org.junit.jupiter.api.Test;
public class BizLogicAppExceptionTest extends TestTools {

  @Test
  @SuppressWarnings("removal")
  public void test_getBusinessViolation_delegates_correctly() {
    BizLogicAppException ex = new BizLogicAppException("MSG_ID", "arg1");
    assertFalse(ex.getBusinessViolation() == null);
    assertEquals("MSG_ID", ex.getBusinessViolation().getMessageId());
    assertEquals(1, ex.getBusinessViolation().getMessageArgs().length);
  }
}
