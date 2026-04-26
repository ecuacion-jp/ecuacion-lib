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

import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.util.TestTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
public class ViolationsTest extends TestTools {

  @Test
  public void test01_empty_violations_does_not_throw() {
    new Violations().throwIfAny();
    assertTrue(true);
  }

  @Test
  public void test02_throwIfAny_throws_when_business_violation_present() {
    Violations violations = new Violations()
        .add(new BusinessViolation("TEST_KEY"));

    Assertions.assertThrows(ViolationException.class, violations::throwIfAny);
  }

  @Test
  public void test03_business_violations_stored_correctly() {
    Violations violations = new Violations()
        .add(new BusinessViolation("KEY_1"))
        .add(new BusinessViolation("KEY_2"))
        .add(new BusinessViolation("KEY_3"));

    assertEquals(3, violations.getBusinessViolations().size());
    assertEquals("KEY_1", violations.getBusinessViolations().get(0).getMessageId());
    assertEquals("KEY_2", violations.getBusinessViolations().get(1).getMessageId());
    assertEquals("KEY_3", violations.getBusinessViolations().get(2).getMessageId());
  }

  @Test
  public void test04_thrown_violation_exception_holds_violations() {
    Violations violations = new Violations()
        .add(new BusinessViolation("TEST_KEY"));

    @SuppressWarnings("null")
    ViolationException ex =
        Assertions.assertThrows(ViolationException.class, violations::throwIfAny);
    assertFalse(ex.getViolations() == null);
    assertEquals(1, ex.getViolations().getBusinessViolations().size());
  }
}
