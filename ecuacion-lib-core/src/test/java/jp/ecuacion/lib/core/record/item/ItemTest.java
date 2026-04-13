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
package jp.ecuacion.lib.core.record.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireNonEmptyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ItemTest {

  @Test
  public void constructorTest() {

    // itemPropertyPath is empty
    try {
      new Item(null);
      assertFalse(true);
    } catch (RequireNonEmptyException ex) {
      // okay
    }

    try {
      new Item("");
      assertFalse(true);
    } catch (RequireNonEmptyException ex) {
      // okay
    }
  }

  private void checkWithClassName(String itemPropertyPath, String className,
      String resultItemNameKey) {
    Item item = new Item(itemPropertyPath);
    item.setItemNameKeyClassFromClassName(className);
    String result = item.getItemNameKey();
    Assertions.assertEquals(resultItemNameKey, result);
  }

  private void checkWithItemNameKey(String itemPropertyPath, String itemNameKey, String className,
      String resultItemNameKey) {
    Item item = new Item(itemPropertyPath);
    item.itemNameKey(itemNameKey);
    item.setItemNameKeyClassFromClassName(className);
    String result = item.getItemNameKey();
    Assertions.assertEquals(resultItemNameKey, result);
  }

  @Test
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
    checkWithItemNameKey("item.Property.Path", "itemNameKeyClass.itemNameKeyField", "rootRecordName",
        "itemNameKeyClass.itemNameKeyField");
  }
}
