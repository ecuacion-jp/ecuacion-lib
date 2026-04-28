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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link ItemContainer}. */
@DisplayName("ItemContainer")
public class ItemContainerTest {

  private static class SimpleContainer implements ItemContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[]{};
    }
  }

  @Nested
  @DisplayName("mergeItems")
  class MergeItems {

    @Test
    @DisplayName("merges two non-overlapping Item arrays")
    void success() {
      SimpleContainer c = new SimpleContainer();
      Item[] merged = c.mergeItems(
          new Item[]{new Item("field1")},
          new Item[]{new Item("field2")});
      assertThat(merged).hasSize(2);
    }

    @Test
    @DisplayName("throws RuntimeException on duplicate property path")
    void duplicateThrows() {
      SimpleContainer c = new SimpleContainer();
      assertThatThrownBy(() -> c.mergeItems(
          new Item[]{new Item("field1")},
          new Item[]{new Item("field1")}))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("field1");
    }
  }

  @Nested
  @DisplayName("getItem")
  class GetItem {

    @Test
    @DisplayName("returns new item for unknown property path")
    void basic() {
      Item item = new SimpleContainer().getItem("myField");
      assertThat(item.getPropertyPath()).isEqualTo("myField");
    }

    @Test
    @DisplayName("returns customized item when property path matches")
    void customizedItem() {
      ItemContainer c = new ItemContainer() {
        @Override
        public Item[] customizedItems() {
          return new Item[]{new Item("known").itemNameKey("cls.known")};
        }
      };
      Item item = c.getItem("known");
      assertThat(item.getPropertyPath()).isEqualTo("known");
      assertThat(item.setsItemNameKeyClassExplicitly()).isTrue();
    }

    @Test
    @DisplayName("handles path ending with <list element>")
    void listElementPath() {
      Item item = new SimpleContainer().getItem("myList.<list element>");
      assertThat(item).isNotNull();
    }
  }
}
