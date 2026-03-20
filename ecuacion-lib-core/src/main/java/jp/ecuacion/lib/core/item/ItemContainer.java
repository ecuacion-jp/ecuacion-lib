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
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.annotation.RequireNonempty;
import jp.ecuacion.lib.core.jakartavalidation.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Accepts and stores data from user input, external system, and so on.
 * 
 * <p>It is mainly used when you want to customize an item name and its value 
 *     in validation messages.</p>
 */
public interface ItemContainer {

  /**
   * Returns {@code Item} from {@code items[]}. 
   * 
   * @param propertyPath propertyPath
   * @return HtmlItem
   */
  @Nonnull
  public default Item getItem(@RequireNonempty String propertyPath) {

    Map<String, Item> map =
        Arrays.asList(customizedItems() == null ? new Item[] {} : customizedItems()).stream()
            .collect(Collectors.toMap(e -> e.getPropertyPath(), e -> e));

    Item item = map.get(ObjectsUtil.requireNonEmpty(propertyPath));

    item = item == null ? getNewItem(propertyPath) : item;

    // Set finalDefaultItemNameKeyClass.
    Optional<ItemNameKeyClass> optAn = ReflectionUtil.searchAnnotationPlacedAtClass(
        ReflectionUtil.getLeafBeanClass(this.getClass(), propertyPath), ItemNameKeyClass.class);

    if (optAn.isPresent()) {
      item.setItemNameKeyClassFromAnnotation(StringUtils.uncapitalize(optAn.get().value()));
    }

    // Get leafBeanClass.
    Class<?> leafBeanClass = this.getClass();
    if (propertyPath.contains(".")) {
      // Handle collections and arrays
      if (propertyPath.endsWith("<list element>")) {
        propertyPath = propertyPath.substring(0, propertyPath.lastIndexOf("."));
      }

      leafBeanClass = ReflectionUtil.getLeafBeanClass(this.getClass(), propertyPath);
    }

    item.setItemNameKeyClassFromClassName(StringUtils.uncapitalize(leafBeanClass.getSimpleName()));

    return item;
  }

  /**
   * Returns an array of items.
   * 
   * <p>It is NOT meant for use from outside.
   *     It's supposed to be used by concrete classes.<br>
   */
  @Nullable
  abstract Item[] customizedItems();

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
  @Nonnull
  default Item[] mergeItems(@Nullable Item[] items1, @Nullable Item[] items2) {
    // Replace null to empty arrays.
    items1 = items1 == null ? new Item[] {} : items1;
    items2 = items2 == null ? new Item[] {} : items2;

    List<Item> list = new ArrayList<>(Arrays.asList(items1));

    // Throw an exception if item is duplicated.
    List<String> propertyPath1List =
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
