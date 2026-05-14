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

import java.util.Objects;
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.util.PropertyPathUtil.ElementOfCollectionCannotBeObtainedException;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Provides utilities for {@link Item} and {@link ItemContainer}.
 */
public class ItemUtil {

  private record ItemContext(@Nullable ItemContainer itemContainer, String itemPropertyPath) {
  }

  private static ItemContext resolveItemContext(Object rootBean, String fullPropertyPath) {
    String fullPropertyPath1stPart = fullPropertyPath.contains(".")
        ? fullPropertyPath.substring(0, fullPropertyPath.indexOf("."))
        : null;

    Object firstChild = null;
    try {
      firstChild = fullPropertyPath1stPart == null ? null
          : PropertyPathUtil.getValue(rootBean, fullPropertyPath1stPart);
    } catch (ElementOfCollectionCannotBeObtainedException ex) {
      // Do nothing.
    }

    String rightMostNode = PropertyPathUtil.getRightMostNode(fullPropertyPath);
    String rightMostRemoved =
        PropertyPathUtil.getPropertyPathWithoutRightMostNode(fullPropertyPath);
    String itemPropertyPath = (rightMostRemoved.isEmpty() ? "" : rightMostRemoved + ".")
        + PropertyPathUtil.toFieldPath(rightMostNode);

    if (rootBean instanceof ItemContainer ic) {
      return new ItemContext(ic, itemPropertyPath);
    } else if (fullPropertyPath1stPart != null && firstChild instanceof ItemContainer ic) {
      return new ItemContext(ic, itemPropertyPath.substring(fullPropertyPath1stPart.length() + 1));
    }

    return new ItemContext(null, fullPropertyPath);
  }

  /**
   * Resolves an {@link Item} from {@code rootBean} and {@code fullPropertyPath}.
   *
   * <p>If an {@link ItemContainer} is found in the object graph, the item is retrieved
   * from it. Otherwise a new item is created using the derived {@code itemNameKey}.</p>
   *
   * @param fullPropertyPath property path relative to rootBean
   * @param rootBean root bean
   * @return Item
   */
  public static Item resolveItem(String fullPropertyPath, Object rootBean) {
    ItemContext ctx = resolveItemContext(rootBean, fullPropertyPath);

    Item item = null;
    if (ctx.itemContainer() != null) {
      item = Objects.requireNonNull(ctx.itemContainer()).getItem(ctx.itemPropertyPath());
    }

    String itemNameKey;
    boolean showsValue = true;

    if (item == null) {
      Class<?> leafBeanClass = PropertyPathUtil.getClass(rootBean.getClass(),
          PropertyPathUtil.getPropertyPathWithoutRightMostNode(fullPropertyPath));
      String itemNameKeyClassFromAnnotation =
          ReflectionUtil.searchAnnotationPlacedAtClass(leafBeanClass, ItemNameKeyClass.class)
              .map(ItemNameKeyClass::value).orElse(null);
      String itemNameKeyClass = StringUtils.isNotEmpty(itemNameKeyClassFromAnnotation)
          ? itemNameKeyClassFromAnnotation
          : leafBeanClass.getSimpleName();
      String itemNameKeyField =
          PropertyPathUtil.toFieldPath(PropertyPathUtil.getRightMostNode(fullPropertyPath));
      itemNameKey = StringUtils.uncapitalize(itemNameKeyClass) + "." + itemNameKeyField;
    } else {
      itemNameKey = item.getItemNameKey();
      showsValue = item.getShowsValue();
    }

    return new Item(fullPropertyPath).itemNameKey(itemNameKey).showsValue(showsValue);
  }
}
