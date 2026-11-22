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

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests the propertyPaths of collections actually output by Hibernate Validator,
 *     their normalization by {@link PropertyPathUtil#toIndexlessPath(String)}
 *     and the resolution of {@link Item}s from them.
 *
 * <p>The patterns are the ones described in the documentation page
 *     "propertyPath in Collections" ({@code docs/item/collection-property-path.md}).</p>
 */
@DisplayName("propertyPath of collections - actual paths, normalization and Item resolution")
// NullAway: null is intentionally put into collections to cause violations.
@SuppressWarnings({"UnusedVariable", "MultipleNullnessAnnotations", "FieldCanBeFinal",
    "NullAway"})
public class CollectionPropertyPathTest {

  private static final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  private static final String L = PropertyPathUtil.EL_LIST;
  private static final String S = PropertyPathUtil.EL_SET;
  private static final String MK = PropertyPathUtil.EL_MAP_KEY;
  private static final String MV = PropertyPathUtil.EL_MAP_VAL;

  /** itemNameKey of the Item registered with the shorthand itemPropertyPath. */
  private static final String KEY_SHORTHAND = "test.shorthand";

  /** itemNameKey of the Item registered with the field name of the collection. */
  private static final String KEY_FIELD = "test.field";

  /**
   * The base class of the validated beans.
   *
   * <p>It registers two items: one with the field name of the collection,
   *     and one with the shorthand itemPropertyPath of the violated element.</p>
   */
  abstract static class Target implements ItemContainer {
    private final Item[] items;

    Target(String fieldName, String shorthand) {
      this.items = new Item[] {new Item(fieldName).itemNameKey(KEY_FIELD),
          new Item(shorthand).itemNameKey(KEY_SHORTHAND)};
    }

    @Override
    public Item[] customizedItems() {
      return items;
    }
  }

  /** A bean for elements. {@code toString()} is fixed so that it can be used as a map key. */
  static class User {
    @NotNull
    private @Nullable String name;

    User(@Nullable String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "user";
    }
  }

  private static <K, V> Map<K, V> map(K key, V value) {
    Map<K, V> map = new HashMap<>();
    map.put(key, value);
    return map;
  }

  private static String validateAndGetPath(Target bean) {
    Set<ConstraintViolation<Target>> set = validator.validate(bean);
    assertThat(set).hasSize(1);
    return set.iterator().next().getPropertyPath().toString();
  }

  /**
   * Asserts the propertyPath, its shorthand form and the resolved items.
   *
   * @param bean bean to validate, which has exactly one violation
   * @param expectedPath the propertyPath expected to be output by Hibernate Validator
   * @param shorthand the expected shorthand form, which is also registered in {@code bean}
   * @param isElementItself {@code true} when the violated value is an element of the collection
   *     itself (like {@code List<@NotNull String>}),
   *     {@code false} when it is a field of a bean in the collection
   */
  private static void assertPath(Target bean, String expectedPath, String shorthand,
      boolean isElementItself) {
    String path = validateAndGetPath(bean);

    // propertyPath output by Hibernate Validator
    assertThat(path).isEqualTo(expectedPath);

    // normalization
    assertThat(PropertyPathUtil.toIndexlessPath(path)).isEqualTo(shorthand);

    // ItemContainer#getItem matches the propertyPath to the item registered with the shorthand
    assertThat(bean.getItem(path).getItemNameKey()).isEqualTo(KEY_SHORTHAND);

    // For a violation on an element itself, the message uses the item of the collection field.
    // For a violation on a field of a bean in a collection, the item of the shorthand is used.
    assertThat(ItemUtil.resolveItem(path, bean).getItemNameKey())
        .isEqualTo(isElementItself ? KEY_FIELD : KEY_SHORTHAND);
  }

  // -------------------------------------------------------------------------
  // List
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("List")
  class ListPatterns {

    static class StrList extends Target {
      List<@NotNull String> strList = Arrays.asList("a", null);

      StrList() {
        super("strList", "strList[]");
      }
    }

    static class UserList extends Target {
      List<@Valid User> userList = List.of(new User("a"), new User(null));

      UserList() {
        super("userList", "userList[].name");
      }
    }

    static class NestedList extends Target {
      List<List<@NotNull String>> nestedList = List.of(List.of("a"), Arrays.asList("b", null));

      NestedList() {
        super("nestedList", "nestedList[][]");
      }
    }

    static class NestedUserList extends Target {
      List<List<@Valid User>> nestedList =
          List.of(List.of(new User("a")), List.of(new User("b"), new User(null)));

      NestedUserList() {
        super("nestedList", "nestedList[][].name");
      }
    }

    @Test
    @DisplayName("element itself")
    void elementItself() {
      assertPath(new StrList(), "strList[1]." + L, "strList[]", true);
    }

    @Test
    @DisplayName("field of a bean element")
    void beanField() {
      assertPath(new UserList(), "userList[1].name", "userList[].name", false);
    }

    @Test
    @DisplayName("element itself of the inner List")
    void nestedElementItself() {
      assertPath(new NestedList(), "nestedList[1]." + L + "[1]." + L, "nestedList[][]", true);
    }

    @Test
    @DisplayName("field of a bean element of the inner List")
    void nestedBeanField() {
      assertPath(new NestedUserList(), "nestedList[1]." + L + "[1].name", "nestedList[][].name",
          false);
    }
  }

  // -------------------------------------------------------------------------
  // Set
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Set")
  class SetPatterns {

    static class StrSet extends Target {
      Set<@NotNull String> strSet = new HashSet<>(Arrays.asList("a", null));

      StrSet() {
        super("strSet", "strSet[]");
      }
    }

    static class UserSet extends Target {
      Set<@Valid User> userSet = new HashSet<>(List.of(new User(null)));

      UserSet() {
        super("userSet", "userSet[].name");
      }
    }

    static class NestedSet extends Target {
      Set<Set<@NotNull String>> nestedSet =
          new HashSet<>(List.of(new HashSet<>(Arrays.asList("a", null))));

      NestedSet() {
        super("nestedSet", "nestedSet[][]");
      }
    }

    static class NestedUserSet extends Target {
      Set<Set<@Valid User>> nestedSet =
          new HashSet<>(List.of(new HashSet<>(List.of(new User(null)))));

      NestedUserSet() {
        super("nestedSet", "nestedSet[][].name");
      }
    }

    @Test
    @DisplayName("element itself")
    void elementItself() {
      assertPath(new StrSet(), "strSet[]." + S, "strSet[]", true);
    }

    @Test
    @DisplayName("field of a bean element (same before and after normalization)")
    void beanField() {
      assertPath(new UserSet(), "userSet[].name", "userSet[].name", false);
    }

    @Test
    @DisplayName("element itself of the inner Set")
    void nestedElementItself() {
      assertPath(new NestedSet(), "nestedSet[]." + S + "[]." + S, "nestedSet[][]", true);
    }

    @Test
    @DisplayName("field of a bean element of the inner Set")
    void nestedBeanField() {
      assertPath(new NestedUserSet(), "nestedSet[]." + S + "[].name", "nestedSet[][].name", false);
    }
  }

  // -------------------------------------------------------------------------
  // Map (keys)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Map (keys)")
  class MapKeyPatterns {

    static class StrMap extends Target {
      Map<@Pattern(regexp = "[0-9]*") String, String> strMap = map("key1", "v");

      StrMap() {
        super("strMap", "strMap<K>[]");
      }
    }

    static class NullKeyMap extends Target {
      Map<@NotNull String, String> strMap = map(null, "v");

      NullKeyMap() {
        super("strMap", "strMap<K>[]");
      }
    }

    static class UserMap extends Target {
      Map<@Valid User, String> userMap = map(new User(null), "v");

      UserMap() {
        super("userMap", "userMap<K>[].name");
      }
    }

    static class NestedMap extends Target {
      Map<Map<@Pattern(regexp = "[0-9]*") String, String>, String> nestedMap =
          map(map("key2", "v"), "v");

      NestedMap() {
        super("nestedMap", "nestedMap<K>[]<K>[]");
      }
    }

    static class NestedUserMap extends Target {
      Map<Map<@Valid User, String>, String> nestedMap = map(map(new User(null), "v"), "v");

      NestedUserMap() {
        super("nestedMap", "nestedMap<K>[]<K>[].name");
      }
    }

    @Test
    @DisplayName("key itself: [ ] contains the toString() value of the key")
    void keyItself() {
      assertPath(new StrMap(), "strMap<K>[key1]." + MK, "strMap<K>[]", true);
    }

    @Test
    @DisplayName("key itself: [ ] is empty when the key is null")
    void nullKey() {
      assertPath(new NullKeyMap(), "strMap<K>[]." + MK, "strMap<K>[]", true);
    }

    @Test
    @DisplayName("field of a bean key")
    void beanField() {
      assertPath(new UserMap(), "userMap<K>[user].name", "userMap<K>[].name", false);
    }

    @Test
    @DisplayName("key itself of the inner Map: no '.' between <map key> and <K>")
    void nestedKeyItself() {
      assertPath(new NestedMap(), "nestedMap<K>[{key2=v}]." + MK + "<K>[key2]." + MK,
          "nestedMap<K>[]<K>[]", true);
    }

    @Test
    @DisplayName("field of a bean key of the inner Map")
    void nestedBeanField() {
      assertPath(new NestedUserMap(), "nestedMap<K>[{user=v}]." + MK + "<K>[user].name",
          "nestedMap<K>[]<K>[].name", false);
    }
  }

  // -------------------------------------------------------------------------
  // Map (values)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Map (values)")
  class MapValuePatterns {

    static class StrMap extends Target {
      Map<String, @NotNull String> strMap = map("key1", null);

      StrMap() {
        super("strMap", "strMap[]");
      }
    }

    static class UserMap extends Target {
      Map<String, @Valid User> userMap = map("key1", new User(null));

      UserMap() {
        super("userMap", "userMap[].name");
      }
    }

    static class NestedMap extends Target {
      Map<String, Map<String, @NotNull String>> nestedMap = map("key1", map("key2", null));

      NestedMap() {
        super("nestedMap", "nestedMap[][]");
      }
    }

    static class NestedUserMap extends Target {
      Map<String, Map<String, @Valid User>> nestedMap = map("key1", map("key2", new User(null)));

      NestedUserMap() {
        super("nestedMap", "nestedMap[][].name");
      }
    }

    @Test
    @DisplayName("value itself")
    void valueItself() {
      assertPath(new StrMap(), "strMap[key1]." + MV, "strMap[]", true);
    }

    @Test
    @DisplayName("field of a bean value")
    void beanField() {
      assertPath(new UserMap(), "userMap[key1].name", "userMap[].name", false);
    }

    @Test
    @DisplayName("value itself of the inner Map")
    void nestedValueItself() {
      assertPath(new NestedMap(), "nestedMap[key1]." + MV + "[key2]." + MV, "nestedMap[][]",
          true);
    }

    @Test
    @DisplayName("field of a bean value of the inner Map")
    void nestedBeanField() {
      assertPath(new NestedUserMap(), "nestedMap[key1]." + MV + "[key2].name",
          "nestedMap[][].name", false);
    }
  }

  // -------------------------------------------------------------------------
  // Combined patterns
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("combined patterns")
  class CombinedPatterns {

    static class MapOfUserList extends Target {
      Map<String, List<@Valid User>> map = map("key1", List.of(new User("a"), new User(null)));

      MapOfUserList() {
        super("map", "map[][].name");
      }
    }

    static class ListOfMapKey extends Target {
      List<Map<@Pattern(regexp = "[0-9]*") String, String>> list = List.of(map("key1", "v"));

      ListOfMapKey() {
        super("list", "list[]<K>[]");
      }
    }

    static class ListOfSet extends Target {
      List<Set<@NotNull String>> list = List.of(new HashSet<>(Arrays.asList("a", null)));

      ListOfSet() {
        super("list", "list[][]");
      }
    }

    static class MapOfMapOfUserList extends Target {
      Map<String, Map<String, List<@Valid User>>> map =
          map("key1", map("key2", List.of(new User("a"), new User(null))));

      MapOfMapOfUserList() {
        super("map", "map[][][].name");
      }
    }

    @Test
    @DisplayName("Map<?, List<User>>")
    void mapOfUserList() {
      assertPath(new MapOfUserList(), "map[key1]." + MV + "[1].name", "map[][].name", false);
    }

    @Test
    @DisplayName("List<Map<String, ?>> (key of the element Map)")
    void listOfMapKey() {
      assertPath(new ListOfMapKey(), "list[0]." + L + "<K>[key1]." + MK, "list[]<K>[]", true);
    }

    @Test
    @DisplayName("List<Set<String>>")
    void listOfSet() {
      assertPath(new ListOfSet(), "list[0]." + L + "[]." + S, "list[][]", true);
    }

    @Test
    @DisplayName("Map<?, Map<?, List<User>>>")
    void mapOfMapOfUserList() {
      assertPath(new MapOfMapOfUserList(), "map[key1]." + MV + "[key2]." + MV + "[1].name",
          "map[][][].name", false);
    }
  }

  // -------------------------------------------------------------------------
  // Arrays
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("arrays")
  class ArrayPatterns {

    static class UserArray extends Target {
      @Valid
      User[] userArray = new User[] {new User("a"), new User(null)};

      UserArray() {
        super("userArray", "userArray[].name");
      }
    }

    static class SizeArray {
      @Size(min = 3)
      String[] sizeArray = new String[] {"ab"};
    }

    static class ElementConstraintArray {
      @NotNull
      String[] strArray = new String[] {"a", null};
    }

    @Test
    @DisplayName("field of a bean element (@Valid on the field)")
    void beanField() {
      assertPath(new UserArray(), "userArray[1].name", "userArray[].name", false);
    }

    @Test
    @DisplayName("@Size on an array checks the length of the array, not its elements")
    void sizeIsArrayLength() {
      Set<ConstraintViolation<SizeArray>> set = validator.validate(new SizeArray());
      assertThat(set).hasSize(1);
      assertThat(set.iterator().next().getPropertyPath().toString()).isEqualTo("sizeArray");
    }

    @Test
    @DisplayName("constraints on elements of an array are not validated")
    void elementConstraintNotValidated() {
      assertThat(validator.validate(new ElementConstraintArray())).isEmpty();
    }
  }

  // -------------------------------------------------------------------------
  // map keys whose toString() contains characters of the propertyPath syntax
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("map key whose toString() contains '.', '[' or ']'")
  class SpecialKeys {

    static class DotKeyMap extends Target {
      Map<String, @Valid User> map = map("1.5", new User(null));

      DotKeyMap() {
        super("map", "map[].name");
      }
    }

    static class BracketKeyMap extends Target {
      Map<String, @NotNull String> map = map("a]b", null);

      BracketKeyMap() {
        super("map", "map[]");
      }
    }

    static class ListKeyMap extends Target {
      Map<List<@NotNull String>, String> map = map(Arrays.asList("x", null), "v");

      ListKeyMap() {
        super("map", "map<K>[][]");
      }
    }

    @Test
    @DisplayName("key containing '.'")
    void dotKey() {
      assertPath(new DotKeyMap(), "map[1.5].name", "map[].name", false);
    }

    @Test
    @DisplayName("key containing ']'")
    void bracketKey() {
      assertPath(new BracketKeyMap(), "map[a]b]." + MV, "map[]", true);
    }

    @Test
    @DisplayName("key which is a List (toString() contains balanced brackets)")
    void listKey() {
      assertPath(new ListKeyMap(), "map<K>[[x, null]]." + MK + "[1]." + L, "map<K>[][]", true);
    }
  }
}
