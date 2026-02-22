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
 * Accepts and store data from user input, external system, and so on.
 * 
 * <p>This is an interface, not a class 
 *     because record can be customized according to the specification of page templates.
 *     Each record in apps always extends EclibRecord or its extended class, 
 *     so interface is needed to customize records.<br>
 *     At that time in that interface you want to record feature like getItems(),
 *     but if this is a class it can't be used.</p>
 * 
 * <p>It is frequently validated with jakarta validation. So it should have features below.</p>
 * <ul>
 * <li>To resolve item name from propertyPath. {@code getItems()} is used for it.<br>
 *     It's used especially for error message to users.</li>
 * </ul>
 */
public interface ItemContainer {

  /**
   * Returns an array of items.
   */
  @Nullable
  public abstract Item[] customizedItems();

  /**
   * Returns a new instance.
   */
  @Nonnull
  public default Item getNewItem(@RequireNonempty String itemPropertyPath) {
    return new Item(ObjectsUtil.requireNonEmpty(itemPropertyPath));
  }

  /**
   * Returns {@code EclibItem} from {@code EclibItem[]} and {@code fieldId}. 
   * 
   * @param itemPropertyPath itemPropertyPath
   * @return HtmlItem
   */
  @Nonnull
  default Item getItem(@RequireNonempty String itemPropertyPath) {

    Map<String, Item> map =
        Arrays.asList(customizedItems() == null ? new Item[] {} : customizedItems()).stream()
            .collect(Collectors.toMap(e -> e.getItemPropertyPath(), e -> e));

    Item item = map.get(ObjectsUtil.requireNonEmpty(itemPropertyPath));

    item = item == null ? getNewItem(itemPropertyPath) : item;

    // Set finalDefaultItemNameKeyClass.
    Optional<ItemNameKeyClass> optAn =
        ReflectionUtil.searchAnnotationPlacedAtClass(this.getClass(), ItemNameKeyClass.class);

    if (optAn.isPresent()) {
      item.setItemNameKeyClassFromAnnotation(StringUtils.uncapitalize(optAn.get().value()));
    }

    item.setItemNameKeyClassFromClassName(
        StringUtils.uncapitalize(this.getClass().getSimpleName()));

    return item;
  }

  /**
   * Merge common items and record dependent items.
   * 
   * <p>This is an utility method so it can be defined in Util class, 
   *     but it's frequently used in record instance and not used outside 
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
        Arrays.asList(items1).stream().map(e -> e.getItemPropertyPath()).toList();

    for (String propertyPath2 : Arrays.asList(items2).stream().map(e -> e.getItemPropertyPath())
        .toList()) {
      if (propertyPath1List.contains(propertyPath2)) {
        throw new RuntimeException(
            "'itemPropertyPath' of EclibItem[] duplicated in Items. key: " + propertyPath2);
      }
    }

    list.addAll(Arrays.asList(items2));

    return list.toArray(new Item[list.size()]);
  }
}
