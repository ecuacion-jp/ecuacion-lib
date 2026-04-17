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

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  @SuppressWarnings("null")
  @Nonnull
  public default Item getItem(String itemPropertyPath) {
    @NonNull
    String noIndexPropertyPath = PropertyPathUtil.removeIndex(itemPropertyPath);

    Map<@NonNull String, Item> map =
        Arrays.asList(customizedItems() == null ? new Item[] {} : customizedItems()).stream()
            .collect(Collectors.toMap(e -> e.getPropertyPath(), e -> e));

    Item item = map.get(ObjectsUtil.requireNonEmpty(noIndexPropertyPath));

    final Item finalItem = item == null ? getNewItem(noIndexPropertyPath) : item;

    // Set finalDefaultItemNameKeyClass.
    // Since what we want to know is class, instance is not needed.
    @NonNull
    Optional<@NonNull ItemNameKeyClass> optAn = ReflectionUtil.searchAnnotationPlacedAtClass(
        ReflectionUtil.getClass(this.getClass(),
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

      leafBeanClass = ReflectionUtil.getClass(this.getClass(),
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
   *     It's supposed to be used by concrete classes.<br>
   */
  abstract @NonNull Item[] customizedItems();

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
   * Merge common items and record dependent items.
   * 
   * <p>It is NOT meant for use from outside.
   *     This is an utility method so it can be defined in Util class, 
   *     but it's frequently used in extended classes and not used outside 
   *     so let it be defined here.</p>
   */
  @SuppressWarnings("null")
  default Item[] mergeItems(Item[] items1, Item[] items2) {

    List<Item> list = new ArrayList<>(Arrays.asList(items1));

    // Throw an exception if item is duplicated.
    List<@NonNull String> propertyPath1List =
        Arrays.asList(items1).stream().map(e -> e.getPropertyPath()).toList();

    for (String propertyPath2 : Arrays.asList(items2).stream().map(e -> e.getPropertyPath())
        .toList()) {
      if (propertyPath1List.contains(propertyPath2)) {
        throw new RuntimeException(
            "'propertyPath' of Item[] duplicated in Items. key: " + propertyPath2);
      }
    }

    list.addAll(Arrays.asList(items2));

    return list.toArray(new Item[list.size()]);
  }
}
