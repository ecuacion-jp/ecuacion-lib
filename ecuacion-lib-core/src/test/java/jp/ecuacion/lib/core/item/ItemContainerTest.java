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
import java.util.List;
import org.jspecify.annotations.Nullable;
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

  private static class ParentContainer implements ItemContainer {
    @SuppressWarnings("unused")
    private @Nullable String name;
    @SuppressWarnings("unused")
    private @Nullable String password;

    @Override
    public Item[] customizedItems() {
      return new Item[]{
          new Item("name").itemNameKey("parent.name"),
          new Item("password").itemNameKey("parent.password").hideValue(),
      };
    }
  }

  private static class ChildContainer extends ParentContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[]{
          new Item("name").itemNameKey("child.name"),
      };
    }
  }

  private static class ChildContainerWithoutItemNameKey extends ParentContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[]{
          new Item("name"),
      };
    }
  }

  private static class User {
    @SuppressWarnings("unused")
    private @Nullable String name;
  }

  private static class UserListContainer implements ItemContainer {
    @SuppressWarnings("unused")
    private @Nullable List<User> userList;

    private final Item[] items;

    UserListContainer(Item... items) {
      this.items = items;
    }

    @Override
    public Item[] customizedItems() {
      return items;
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

    @Test
    @DisplayName("simplified form [] still matches runtime path with index")
    void simplifiedFormMatchesRuntimeIndex() {
      UserListContainer c =
          new UserListContainer(new Item("userList[].name").itemNameKey("cls.name"));
      assertThat(c.getItem("userList[2].name").setsItemNameKeyClassExplicitly()).isTrue();
    }

    @Test
    @DisplayName("full propertyPath form with index matches any index at runtime")
    void fullIndexFormMatchesAnyIndex() {
      UserListContainer c =
          new UserListContainer(new Item("userList[1].name").itemNameKey("cls.name"));
      assertThat(c.getItem("userList[1].name").setsItemNameKeyClassExplicitly()).isTrue();
      assertThat(c.getItem("userList[3].name").setsItemNameKeyClassExplicitly()).isTrue();
    }

    @Nested
    @DisplayName("parent class inheritance")
    class Inheritance {

      @Test
      @DisplayName("child's explicit itemNameKey overrides parent's")
      void childOverridesItemNameKey() {
        Item item = new ChildContainer().getItem("name");
        assertThat(item.getItemNameKey()).isEqualTo("child.name");
      }

      @Test
      @DisplayName("child inherits itemNameKey from parent when not set in child")
      void childInheritsItemNameKey() {
        Item item = new ChildContainerWithoutItemNameKey().getItem("name");
        assertThat(item.getItemNameKey()).isEqualTo("parent.name");
      }

      @Test
      @DisplayName("child inherits item from parent when not present in child at all")
      void childInheritsEntireItemFromParent() {
        Item item = new ChildContainerWithoutItemNameKey().getItem("password");
        assertThat(item.getItemNameKey()).isEqualTo("parent.password");
        assertThat(item.getShowsValue()).isFalse();
      }

      @Test
      @DisplayName("child inherits hideValue from parent when not set in child")
      void childInheritsShowsValue() {
        // ChildContainerWithoutItemNameKey has Item("name") with no hideValue - parent has hideValue on password
        // Create a case where child redefines password without hideValue
        ItemContainer child = new ParentContainer() {
          @Override
          public Item[] customizedItems() {
            return new Item[]{new Item("password").itemNameKey("child.password")};
          }
        };
        Item item = child.getItem("password");
        assertThat(item.getItemNameKey()).isEqualTo("child.password");
        assertThat(item.getShowsValue()).isFalse(); // inherited from parent
      }
    }

    @Test
    @DisplayName("two full-index forms that normalize to the same path are treated as duplicate")
    void fullFormDuplicatesDetected() {
      UserListContainer c = new UserListContainer(
          new Item("userList[1].name"),
          new Item("userList[2].name"));
      assertThatThrownBy(() -> c.getItem("userList[1].name"))
          .isInstanceOf(IllegalStateException.class);
    }
  }
}
