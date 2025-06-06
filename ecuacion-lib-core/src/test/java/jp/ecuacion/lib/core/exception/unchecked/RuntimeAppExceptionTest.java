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
package jp.ecuacion.lib.core.exception.unchecked;

import jp.ecuacion.lib.core.TestTools;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import org.junit.jupiter.api.Test;

public class RuntimeAppExceptionTest extends TestTools {

  @Test
  public void test01_コンストラクタ_01_引数がnull() {
    try {
      @SuppressWarnings("unused")
      UncheckedAppException ex = new UncheckedAppException(null);
      fail();

    } catch (NullPointerException npe) {
      assertTrue(true);
    }
  }

  @Test
  public void test01_コンストラクタ_02_引数がnull以外() {
    AppException appEx = new BizLogicAppException("MSG1");
    UncheckedAppException ex = new UncheckedAppException(appEx);

    assertEquals(ex.getCause(), appEx);
  }
}
