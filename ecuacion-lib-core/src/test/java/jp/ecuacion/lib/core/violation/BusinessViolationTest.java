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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link BusinessViolation}. */
@DisplayName("BusinessViolation")
public class BusinessViolationTest {

  private static final String SAMPLE_MSG_ID = "MSG_ID";

  @Test
  @DisplayName("messageId is stored on construction")
  public void messageId() {
    BusinessViolation v = new BusinessViolation(SAMPLE_MSG_ID);
    assertThat(v.getMessageId()).isEqualTo(SAMPLE_MSG_ID);
  }

  @Test
  @DisplayName("messageArgs defaults to empty array when not provided")
  public void messageArgsDefaultsToEmpty() {
    BusinessViolation v = new BusinessViolation(SAMPLE_MSG_ID);
    assertThat(v.getMessageId()).isEqualTo(SAMPLE_MSG_ID);
    assertThat(v.getMessageArgs()).isNotNull();
    assertThat(v.getMessageArgs()).isEmpty();
  }

  @Test
  @DisplayName("messageArgs stores provided argument")
  public void messageArgsWithValue() {
    BusinessViolation v = new BusinessViolation(SAMPLE_MSG_ID, "abc");
    assertThat(v.getMessageId()).isEqualTo(SAMPLE_MSG_ID);
    assertThat(v.getMessageArgs()).hasSize(1);
    assertThat(v.getMessageArgs()[0]).isEqualTo("abc");
  }

  @Test
  @DisplayName("messageArgs is empty when only messageId is given (alternate constructor)")
  public void messageArgsEmptyForAltConstructor() {
    BusinessViolation v = new BusinessViolation("TEST_KEY");
    assertThat(v.getMessageId()).isEqualTo("TEST_KEY");
    assertThat(v.getMessageArgs()).isNotNull();
    assertThat(v.getMessageArgs()).isEmpty();
  }

  @Test
  @DisplayName("constructor(String, Object[]): stores message args")
  public void constructorWithArgArray() {
    BusinessViolation v =
        new BusinessViolation(SAMPLE_MSG_ID, new Object[]{"x", "y"});
    assertThat(v.getMessageId()).isEqualTo(SAMPLE_MSG_ID);
    assertThat(v.getMessageArgs()).hasSize(2);
  }

  @Test
  @DisplayName("constructor(String[], String, Object[]): stores paths and args")
  public void constructorWithPathsAndArgArray() {
    BusinessViolation v =
        new BusinessViolation(new String[]{"field1"}, SAMPLE_MSG_ID, new Object[]{"x"});
    assertThat(v.getMessageId()).isEqualTo(SAMPLE_MSG_ID);
    assertThat(v.getItemPropertyPaths()).containsExactly("field1");
  }

  @Test
  @DisplayName("getItemNameKeys: returns empty array when not set")
  public void getItemNameKeysEmpty() {
    BusinessViolation v = new BusinessViolation(SAMPLE_MSG_ID);
    assertThat(v.getItemNameKeys()).isEmpty();
  }

  @Test
  @DisplayName("getItemNameKeys: returns set keys")
  public void getItemNameKeysSet() {
    BusinessViolation v = new BusinessViolation(new String[]{"customer.name"},
        new String[]{"name"}, SAMPLE_MSG_ID, new Object[]{});
    assertThat(v.getItemNameKeys()).containsExactly("customer.name");
  }

  @Test
  @DisplayName("getItemPropertyPaths: returns empty array when not set")
  public void getItemPropertyPathsEmpty() {
    BusinessViolation v = new BusinessViolation(SAMPLE_MSG_ID);
    assertThat(v.getItemPropertyPaths()).isEmpty();
  }

  @Test
  @DisplayName("getItemPropertyPaths: returns set paths")
  public void getItemPropertyPathsSet() {
    BusinessViolation v =
        new BusinessViolation(new String[]{"path1", "path2"}, SAMPLE_MSG_ID);
    assertThat(v.getItemPropertyPaths()).containsExactly("path1", "path2");
  }

  @Test
  @DisplayName("toString: returns string containing messageId")
  public void toStringContainsMessageId() {
    BusinessViolation v = new BusinessViolation(SAMPLE_MSG_ID);
    assertThat(v.toString()).contains(SAMPLE_MSG_ID);
  }
}
