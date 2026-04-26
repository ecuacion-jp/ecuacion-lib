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

import java.util.Locale;
import jp.ecuacion.lib.core.util.internal.PropertiesFileUtilValueGetter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PropertiesFileUtilTest extends TestTools {

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtilValueGetter.addToDynamicPostfixList("lib-core-test");
  }

  @Test
  public void getMessage_objectArgs_numberFormat() {
    String result = PropertiesFileUtil.getMessage(Locale.ENGLISH, "MSG_WITH_NUMBER_FORMAT",
        new Object[] {1234567});
    Assertions.assertEquals("formatted: 1,234,567", result);
  }
}
