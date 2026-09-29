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

import jakarta.validation.ConstraintViolation;
import java.util.List;
import java.util.Objects;
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import jp.ecuacion.lib.core.util.PropertyPathUtil.ElementOfCollectionCannotBeObtainedException;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
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
      // No ItemContainer was found anywhere in the object graph for this path. Determine the
      // itemNameKey class part the same way ItemContainer#getItem() does.
      String itemNameKeyClass = resolveItemNameKeyClass(fullPropertyPath, rootBean.getClass());
      String itemNameKeyField =
          PropertyPathUtil.toFieldPath(PropertyPathUtil.getRightMostNode(fullPropertyPath));
      itemNameKey = itemNameKeyClass + "." + itemNameKeyField;
    } else {
      itemNameKey = item.getItemNameKey();
      showsValue = item.getShowsValue();
    }

    return new Item(fullPropertyPath).itemNameKey(itemNameKey).showsValue(showsValue);
  }

  /**
   * Resolves an {@link Item} from {@code cv} and {@code propertyPath} specified
   *     at an attribute of the constraint annotation.
   *
   * <p>{@code propertyPath} (like {@code propertyPath}, {@code baselinePropertyPath}
   *     or {@code conditionPropertyPath}) is relative to the bean the annotation is placed on.
   *     It's converted to the one relative to the root bean by
   *     {@link #getFullPropertyPath(ConstraintViolation, String)}, and then resolved by
   *     {@link #resolveItem(String, Object)}, so every property path of a constraint annotation
   *     obtains its {@code itemNameKey} in the same way regardless of the attribute
   *     it is specified at.</p>
   *
   * @param cv ConstraintViolation
   * @param propertyPath property path relative to the bean the annotation is placed on
   * @return Item
   */
  public static Item resolveItem(ConstraintViolation<?> cv, String propertyPath) {
    return resolveItem(getFullPropertyPath(cv, propertyPath), cv.getRootBean());
  }

  /**
   * Returns the property path relative to the root bean of {@code cv}
   *     from {@code propertyPath} specified at an attribute of the constraint annotation.
   *
   * <p>For a constraint placed at a class, {@code propertyPath} is relative to that class,
   *     which {@code cv.getPropertyPath()} refers to.
   *     For a constraint placed at a method, it's relative to the class that owns the method,
   *     which the parent of {@code cv.getPropertyPath()} refers to.</p>
   *
   * @param cv ConstraintViolation
   * @param propertyPath property path relative to the bean the annotation is placed on
   * @return property path relative to the root bean
   */
  public static String getFullPropertyPath(ConstraintViolation<?> cv, String propertyPath) {
    String cvPp = cv.getPropertyPath() == null ? "" : cv.getPropertyPath().toString();
    boolean isClassValidator = ClassValidator.class.isAssignableFrom(
        cv.getConstraintDescriptor().getConstraintValidatorClasses().get(0));

    // Base differs class from method.
    String cvPpBase = isClassValidator ? cvPp
        : (cvPp.contains(".") ? cvPp.substring(0, cvPp.lastIndexOf(".")) : "");
    return (StringUtils.isEmpty(cvPpBase) ? "" : cvPpBase + ".") + propertyPath;
  }

  /**
   * Returns the itemNameKey class part for {@code itemPropertyPath}.
   *
   * <p>Shared by {@link ItemContainer#getItem(String)} and by this class's own
   *     {@link #resolveItem(String, Object)} fallback for when no {@code ItemContainer} is found
   *     anywhere in the object graph.</p>
   *
   * <p>When {@code itemPropertyPath} is nested (e.g. "dlUser.name"), the class part is taken
   *     directly from the path itself -- the node immediately before the rightmost (field-part)
   *     node -- rather than from {@code @ItemNameKeyClass} or the reflected Java type of that
   *     node. This takes priority over both, because the same {@code ItemContainer}-implementing
   *     class can be embedded under different field names in different places (e.g. "dlUser" and
   *     "requestingUser", both typed as a common {@code User} record), and the field name is what
   *     actually distinguishes them, whereas {@code @ItemNameKeyClass} only ever expresses one
   *     fixed value per class.</p>
   *
   * <p>The exception is when {@code @ItemNameKeyClass} is placed at the field the node refers to
   *     (e.g. {@code @ItemNameKeyClass("dept") List<Dept> deptList}). Then its value is used
   *     instead of the node name, so that fields reached through a field whose name does not suit
   *     as the class part (like "deptList") can share the itemNameKey (like "dept.name")
   *     without specifying {@code itemNameKey} field by field.</p>
   *
   * <p>{@code @ItemNameKeyClass} placed at a class therefore only applies when
   *     {@code itemPropertyPath} is NOT nested, 
   *     where there is no path segment to fall back on and it instead replaces the
   *     (usually unsuitable, e.g. "userBaseRecord") raw class name of {@code ownerClass}.</p>
   *
   * @param itemPropertyPath itemPropertyPath
   * @param ownerClass the class {@code itemPropertyPath} is relative to, consulted for
   *     {@code @ItemNameKeyClass} placed at the class when {@code itemPropertyPath} is not nested,
   *     or used to find the field for {@code @ItemNameKeyClass} placed at it when nested
   * @return the itemNameKey class part, already uncapitalized
   */
  public static String resolveItemNameKeyClass(String itemPropertyPath, Class<?> ownerClass) {
    List<@NonNull String> nodeList = PropertyPathUtil.getNodeList(itemPropertyPath);
    if (nodeList.size() >= 2) {
      String classPartNode = nodeList.get(nodeList.size() - 2);
      String classPartFieldName = PropertyPathUtil.toFieldPath(classPartNode);
      Class<?> classPartFieldOwner = PropertyPathUtil.getClass(ownerClass,
          String.join(".", nodeList.subList(0, nodeList.size() - 2)));
      @Nullable
      ItemNameKeyClass an = ReflectionUtil.getDeclaredField(classPartFieldOwner, classPartFieldName)
          .getAnnotation(ItemNameKeyClass.class);
      return StringUtils.uncapitalize(an == null ? classPartFieldName : an.value());
    }

    // Since what we want to know is class, instance is not needed.
    return ReflectionUtil.searchAnnotationPlacedAtClass(ownerClass, ItemNameKeyClass.class)
        .map(ItemNameKeyClass::value).map(StringUtils::uncapitalize)
        .orElseGet(() -> StringUtils.uncapitalize(ownerClass.getSimpleName()));
  }
}
