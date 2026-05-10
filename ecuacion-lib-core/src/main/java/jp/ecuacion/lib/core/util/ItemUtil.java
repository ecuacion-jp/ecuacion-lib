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
import jp.ecuacion.lib.core.util.ReflectionUtil.ElementOfCollectionCannotBeObtainedException;
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
          : ReflectionUtil.getValue(rootBean, fullPropertyPath1stPart);
    } catch (ElementOfCollectionCannotBeObtainedException ex) {
      // Do nothing.
    }

    String rightMostNode = PropertyPathUtil.getRightMostNode(fullPropertyPath);
    String rightMostRemoved =
        PropertyPathUtil.getPropertyPathWithoutRightMostNode(fullPropertyPath);
    String itemPropertyPath = (rightMostRemoved.isEmpty() ? "" : rightMostRemoved + ".")
        + PropertyPathUtil.removeCollectionPart(rightMostNode);

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
   * @param leafBean leaf bean
   * @return Item
   */
  public static Item resolveItem(String fullPropertyPath, Object rootBean, Object leafBean) {
    ItemContext ctx = resolveItemContext(rootBean, fullPropertyPath);

    Item item = null;
    if (ctx.itemContainer() != null) {
      item = Objects.requireNonNull(ctx.itemContainer()).getItem(ctx.itemPropertyPath());
    }

    String itemNameKey;
    boolean showsValue = true;

    if (item == null) {
      itemNameKey = getItemNameKey(null, rootBean, leafBean, null, null, fullPropertyPath);
    } else {
      itemNameKey = item.getItemNameKey();
      showsValue = item.getShowsValue();
    }

    return new Item(fullPropertyPath).itemNameKey(itemNameKey).showsValue(showsValue);
  }

  /**
   * Returns {@code itemNameKey} value.
   *
   * <p>Resolves {@code itemNameKeyClassFromAnnotation} by {@code leafBeanClass} derived
   * from {@code rootBean} and {@code propertyPath}.</p>
   *
   * @param explicitlySetItemNameKeyClass explicitly set itemNameKeyClass
   * @param rootBean root bean
   * @param leafBeanFromConstraintViolation leaf bean (unused; kept for API compatibility)
   * @param defaultItemNameKeyClass default itemNameKeyClass (unused; kept for API compatibility)
   * @param itemNameKeyField field part of itemNameKey
   * @param propertyPath itemPropertyPath
   * @return itemNameKey
   */
  public static String getItemNameKey(@Nullable String explicitlySetItemNameKeyClass,
      Object rootBean, Object leafBeanFromConstraintViolation,
      @Nullable String defaultItemNameKeyClass, @Nullable String itemNameKeyField,
      String propertyPath) {

    Class<?> leafBeanClass = ReflectionUtil.getClass(rootBean.getClass(),
        PropertyPathUtil.getPropertyPathWithoutRightMostNode(propertyPath));

    String itemNameKeyClassFromAnnotation =
        ReflectionUtil.searchAnnotationPlacedAtClass(leafBeanClass, ItemNameKeyClass.class)
            .map(ItemNameKeyClass::value).orElse(null);

    return getItemNameKey(explicitlySetItemNameKeyClass, itemNameKeyClassFromAnnotation,
        leafBeanClass.getSimpleName(), itemNameKeyField, propertyPath);
  }

  /**
   * Returns {@code itemNameKey} value.
   *
   * <p>Priority order for itemNameKeyClass (first non-empty wins):
   * <ol>
   *   <li>explicitly set via {@code itemNameKey(itemNameKey)}</li>
   *   <li>from {@code @ItemNameKeyClass} annotation</li>
   *   <li>uncapitalized class name (set by {@code ItemContainer#getItem(String)})</li>
   * </ol>
   * </p>
   *
   * @param explicitlySetItemNameKeyClass explicitly set itemNameKeyClass
   * @param itemNameKeyClassFromAnnotation itemNameKeyClass from {@code @ItemNameKeyClass}
   * @param itemNameKeyClassFromClassName itemNameKeyClass derived from class name
   * @param itemNameKeyField field part of itemNameKey
   * @param propertyPath itemPropertyPath
   * @return itemNameKey
   */
  public static String getItemNameKey(@Nullable String explicitlySetItemNameKeyClass,
      @Nullable String itemNameKeyClassFromAnnotation,
      @Nullable String itemNameKeyClassFromClassName, @Nullable String itemNameKeyField,
      String propertyPath) {
    @Nullable
    String tmpItemNameKeyClass;
    String tmpItemNameKeyField;

    if (StringUtils.isNotEmpty(explicitlySetItemNameKeyClass)) {
      tmpItemNameKeyClass = explicitlySetItemNameKeyClass;
    } else if (StringUtils.isNotEmpty(itemNameKeyClassFromAnnotation)) {
      tmpItemNameKeyClass = itemNameKeyClassFromAnnotation;
    } else {
      tmpItemNameKeyClass = itemNameKeyClassFromClassName;
    }

    if (!StringUtils.isEmpty(itemNameKeyField)) {
      tmpItemNameKeyField = ObjectsUtil.requireNonNull(itemNameKeyField);
    } else {
      tmpItemNameKeyField =
          PropertyPathUtil.removeCollectionPart(PropertyPathUtil.getRightMostNode(propertyPath));
    }

    return StringUtils.uncapitalize(tmpItemNameKeyClass) + "." + tmpItemNameKeyField;
  }
}
