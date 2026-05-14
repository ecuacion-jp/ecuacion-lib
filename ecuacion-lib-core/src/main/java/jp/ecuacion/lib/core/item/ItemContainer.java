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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.PropertyPathUtil;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

/**
 * Accepts and stores data from user input, external system, and so on.
 *
 * <p>It is mainly used when you want to customize an item name and its attributes
 *     in validation messages.</p>
 */
public interface ItemContainer {

  /**
   * Returns {@code Item} from {@code customizedItems[]} or newly created one if not exists.
   *
   * @param itemPropertyPath itemPropertyPath
   * @return HtmlItem
   */
  public default Item getItem(String itemPropertyPath) {
    @NonNull
    String noIndexPropertyPath = PropertyPathUtil.toIndexlessPath(itemPropertyPath);

    // Walk the class hierarchy to collect and merge customizedItems() from all levels.
    // Most-derived level takes precedence; unset properties are inherited from parent levels.
    Item item = null;
    boolean anyLevelFound = false;
    for (Class<?> cls = this.getClass(); cls != null
        && ItemContainer.class.isAssignableFrom(cls); cls = cls.getSuperclass()) {
      try {
        Method m = cls.getDeclaredMethod("customizedItems");
        anyLevelFound = true;
        Item[] levelItems = invokeCustomizedItemsNonVirtual(m, cls);
        // Build a map to detect duplicate paths within the same level (throws
        // IllegalStateException).
        Map<@NonNull String, Item> levelMap =
            Arrays.stream(levelItems).collect(Collectors.toMap(Item::getPropertyPath, e -> e));
        Item levelItem = levelMap.get(ObjectsUtil.requireNonEmpty(noIndexPropertyPath));
        if (levelItem != null) {
          if (item == null) {
            item = levelItem;
          } else {
            item.mergeFromParent(levelItem);
          }
        }
      } catch (NoSuchMethodException e) {
        // cls does not declare its own customizedItems(); skip this level.
      }
    }

    // Fallback: if no class in the hierarchy declared customizedItems() directly
    // (e.g. the method is provided as an interface default), use virtual dispatch.
    if (!anyLevelFound) {
      Item[] items = customizedItems();
      if (items != null) {
        Map<@NonNull String, Item> map =
            Arrays.stream(items).collect(Collectors.toMap(Item::getPropertyPath, e -> e));
        item = map.get(ObjectsUtil.requireNonEmpty(noIndexPropertyPath));
      }
    }

    final Item finalItem = item == null ? getNewItem(noIndexPropertyPath) : item;

    // Set finalDefaultItemNameKeyClass.
    // Since what we want to know is class, instance is not needed.
    @NonNull
    Optional<@NonNull ItemNameKeyClass> optAn = ReflectionUtil.searchAnnotationPlacedAtClass(
        PropertyPathUtil.getClass(this.getClass(),
            PropertyPathUtil.getPropertyPathWithoutRightMostNode(itemPropertyPath)),
        ItemNameKeyClass.class);

    optAn.ifPresent(
        an -> finalItem.setItemNameKeyClassFromAnnotation(StringUtils.uncapitalize(an.value())));

    // Get leafBeanClass.
    Class<?> leafBeanClass = this.getClass();
    if (itemPropertyPath.contains(".")) {
      // Handle collections and arrays
      if (itemPropertyPath.endsWith("<list element>")) {
        itemPropertyPath = itemPropertyPath.substring(0, itemPropertyPath.lastIndexOf("."));
      }

      leafBeanClass = PropertyPathUtil.getClass(this.getClass(),
          PropertyPathUtil.getPropertyPathWithoutRightMostNode(itemPropertyPath));
    }

    finalItem
        .setItemNameKeyClassFromClassName(StringUtils.uncapitalize(leafBeanClass.getSimpleName()));

    return finalItem;
  }

  /**
   * Returns an array of items.
   *
   * <p>It is NOT meant for use from outside.
   *     It's supposed to be used by concrete classes.</p>
   *
   * <p>Elements of the array are effectively {@code @NonNull},
   *     but the annotation is not added because you don't want to add it
   *     every time you define new {@code Item[]}.
   *     (in eclipse you need to do it to avoid a warning on it)</p>
   */
  abstract Item[] customizedItems();

  /**
   * Returns all unique property paths declared across the entire class hierarchy.
   *
   * <p>Used by subinterfaces (e.g. {@code HtmlItemContainer}) to aggregate items from all
   *     hierarchy levels. Child-level paths appear before parent-level paths in the result.</p>
   */
  default List<String> allCustomizedPropertyPaths() {
    Set<String> paths = new LinkedHashSet<>();
    boolean anyLevelFound = false;

    for (Class<?> cls = this.getClass(); cls != null
        && ItemContainer.class.isAssignableFrom(cls); cls = cls.getSuperclass()) {
      try {
        Method m = cls.getDeclaredMethod("customizedItems");
        anyLevelFound = true;
        Item[] levelItems = invokeCustomizedItemsNonVirtual(m, cls);
        for (Item levelItem : levelItems) {
          paths.add(levelItem.getPropertyPath());
        }
      } catch (NoSuchMethodException e) {
        // cls does not declare its own customizedItems(); skip this level.
      }
    }

    if (!anyLevelFound) {
      // Fallback: virtual dispatch (e.g. customizedItems() provided as interface default)
      Item[] items = customizedItems();
      if (items != null) {
        for (Item item : items) {
          paths.add(item.getPropertyPath());
        }
      }
    }

    return new ArrayList<>(paths);
  }

  /**
   * Creates new item.
   *
   * <p>It is NOT meant for use from outside.
   *     It's supposed to be used by ItemContainers
   *     which extends this class like {@code HtmlItemContainer}.<br>
   */
  default Item getNewItem(String propertyPath) {
    return new Item(propertyPath);
  }

  /**
   * Invokes the given {@code customizedItems()} method non-virtually so that the implementation
   * declared at {@code cls} is called regardless of any overrides in subclasses.
   *
   * <p>Falls back to virtual dispatch when the target class's module does not open its package
   *     to this library's module (e.g. named-module environments without {@code opens}).
   *     In that case the parent-class inheritance feature is unavailable for that class,
   *     but single-level behaviour is preserved.</p>
   */
  private Item[] invokeCustomizedItemsNonVirtual(Method m, Class<?> cls) {
    try {
      try {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(cls, MethodHandles.lookup());
        MethodHandle mh =
            lookup.findSpecial(cls, m.getName(), MethodType.methodType(m.getReturnType()), cls);
        Item[] result = (Item[]) mh.invoke(this);
        return result == null ? new Item[0] : result;
      } catch (IllegalAccessException e) {
        // Named module without opens: fall back to virtual dispatch.
        Item[] result = (Item[]) m.invoke(this);
        return result == null ? new Item[0] : result;
      }
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Merge common items and record dependent items.
   *
   * <p>It is NOT meant for use from outside.
   *     This is an utility method so it can be defined in Util class,
   *     but it's frequently used in extended classes and not used outside
   *     so let it be defined here.</p>
   */
  default Item[] mergeItems(Item[] items1, Item[] items2) {

    List<Item> list = new ArrayList<>(Arrays.asList(items1));

    // Throw an exception if item is duplicated.
    List<@NonNull String> propertyPath1List =
        Arrays.stream(items1).map(Item::getPropertyPath).toList();

    for (String propertyPath2 : Arrays.stream(items2).map(Item::getPropertyPath).toList()) {
      if (propertyPath1List.contains(propertyPath2)) {
        throw new RuntimeException(
            "'propertyPath' of Item[] duplicated in Items. key: " + propertyPath2);
      }
    }

    list.addAll(Arrays.asList(items2));

    return list.toArray(Item[]::new);
  }
}
