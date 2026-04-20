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
package jp.ecuacion.lib.core.violation;

import jp.ecuacion.lib.core.TestTools;
import org.junit.jupiter.api.Test;
public class BusinessViolationTest extends TestTools {
  private static final String SAMPLE_MSG_ID = "MSG_ID";

  @Test
  public void test01_constructor_01_messageId_02_argIsNotNull() {
    BusinessViolation v = new BusinessViolation(SAMPLE_MSG_ID);
    assertEquals(SAMPLE_MSG_ID, v.getMessageId());
  }

  @Test
  public void test01_constructor_02_messageId_messageArgs_02_allExceptMessageIdAreNull() {
    BusinessViolation v = new BusinessViolation(SAMPLE_MSG_ID);

    assertEquals(SAMPLE_MSG_ID, v.getMessageId());
    assertFalse(v.getMessageArgs() == null);
    assertEquals(0, v.getMessageArgs().length);
  }

  @Test
  public void test01_constructor_02_messageId_messageArgs_03_valid() {
    BusinessViolation v = new BusinessViolation(SAMPLE_MSG_ID, "abc");

    assertEquals(SAMPLE_MSG_ID, v.getMessageId());
    assertEquals(1, v.getMessageArgs().length);
    assertEquals("abc", v.getMessageArgs()[0].getArgString());
  }

  @Test
  public void test01_constructor_12_messageId_02_allExceptMessageIdAreNull() {
    BusinessViolation v = new BusinessViolation("TEST_KEY");

    assertEquals("TEST_KEY", v.getMessageId());
    assertFalse(v.getMessageArgs() == null);
    assertEquals(0, v.getMessageArgs().length);
  }
}
