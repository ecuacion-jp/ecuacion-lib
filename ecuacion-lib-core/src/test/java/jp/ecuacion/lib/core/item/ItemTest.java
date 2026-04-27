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
package jp.ecuacion.lib.core.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireNonEmptyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link Item}. */
@DisplayName("Item")
public class ItemTest {

  @Test
  @DisplayName("constructor throws when itemPropertyPath is empty")
  public void constructorTest() {
    assertThatThrownBy(() -> new Item(""))
        .isInstanceOf(RequireNonEmptyException.class);
  }

  private void checkWithClassName(String itemPropertyPath, String className,
      String resultItemNameKey) {
    Item item = new Item(itemPropertyPath);
    item.setItemNameKeyClassFromClassName(className);
    assertThat(item.getItemNameKey()).isEqualTo(resultItemNameKey);
  }

  private void checkWithItemNameKey(String itemPropertyPath, String itemNameKey, String className,
      String resultItemNameKey) {
    Item item = new Item(itemPropertyPath);
    item.itemNameKey(itemNameKey);
    item.setItemNameKeyClassFromClassName(className);
    assertThat(item.getItemNameKey()).isEqualTo(resultItemNameKey);
  }

  @Test
  @DisplayName("itemNameKey is derived from className and propertyPath correctly")
  public void itemNameKeyTest() {

    // No itemNameKey settings / itemPropertyPath does not have "."
    checkWithClassName("itemPropertyPath", "rootRecordName", "rootRecordName.itemPropertyPath");

    // No itemNameKey settings / itemPropertyPath have 1 "."
    checkWithClassName("itemProperty.Path", "rootRecordName", "rootRecordName.Path");

    // No itemNameKey settings / itemPropertyPath have 2 "."
    checkWithClassName("item.Property.Path", "rootRecordName", "rootRecordName.Path");

    // itemNameKeyField settings / itemPropertyPath does not have "."
    checkWithItemNameKey("itemPropertyPath", "itemNameKeyField", "rootRecordName",
        "rootRecordName.itemNameKeyField");

    // itemNameKeyField settings / itemPropertyPath has 1 "."
    checkWithItemNameKey("itemProperty.Path", "itemNameKeyField", "rootRecordName",
        "rootRecordName.itemNameKeyField");

    // itemNameKeyField settings / itemPropertyPath has 2 "."
    checkWithItemNameKey("item.Property.Path", "itemNameKeyField", "rootRecordName",
        "rootRecordName.itemNameKeyField");

    // full itemNameKey settings / itemPropertyPath does not have "."
    checkWithItemNameKey("itemPropertyPath", "itemNameKeyClass.itemNameKeyField", "rootRecordName",
        "itemNameKeyClass.itemNameKeyField");

    // full itemNameKey settings / itemPropertyPath has 1 "."
    checkWithItemNameKey("itemProperty.Path", "itemNameKeyClass.itemNameKeyField", "rootRecordName",
        "itemNameKeyClass.itemNameKeyField");

    // full itemNameKey settings / itemPropertyPath has 2 "."
    checkWithItemNameKey("item.Property.Path", "itemNameKeyClass.itemNameKeyField",
        "rootRecordName", "itemNameKeyClass.itemNameKeyField");
  }
}
