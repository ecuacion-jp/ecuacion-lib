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

import jp.ecuacion.lib.core.annotation.RequireNonEmpty;
import jp.ecuacion.lib.core.util.MessageUtil;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import org.jspecify.annotations.Nullable;

/**
 * Stores item attributes.
 * 
 * <p>To understand details on item, 
 *     see <a href="https://github.com/ecuacion-jp/ecuacion-jp.github.io/blob/main/documentation/common/naming-convention.md">naming-convention.md</a></p>
 */
public class Item {

  /**
   * ID string of an item.
   * 
   * <p>When you want a item corresponding to the field {@code name}, 
   *     just set {@code name} to it.<br>
   *     It can be 'dept.name' when the field is in {@code dept} object.</p>
   */
  protected String propertyPath;

  /**
   * A class part (= left part) of {@code itemNameKey}. 
   * (like "acc" from itemNameKey: "acc.name")
   * 
   * <p>It is set by itemNameKey(String) and only when its argument contains ".".</p>
   * 
   * <p>The itemNameKeyClass obtained from {@code @ItemNameKeyClass} is not stored in it
   *     because the value from {@code @ItemNameKeyClass} is a default value 
   *     when this field is null.</p>
   * 
   * <p>It is nullable since {@code itemNameKey(String)} is not mandatory required to be called.</p>
   */
  protected @Nullable String itemNameKeyClassSetExplicitly;

  /**
   * An {@code itemNameKeyClass} obtained from {@code @ItemNameKeyClass}.
   * 
   * <p>It is nullable since {@code @ItemNameKeyClass} is not mandatory required to be called.</p>
   */
  private @Nullable String itemNameKeyClassFromAnnotation;

  /**
   * The class name the field belongs.
   * 
   * <p>It is actually nonNull since className always exists,
   *     but {@code @Nullable} is set because this cannot always be set at constructor.</p>
   */
  private @Nullable String itemNameKeyClassFromClassName;

  /**
   * A field part (= right part) of {@code itemNameKey}. (like "name" from itemNameKey: "acc.name")
   * 
   * <p>It is nullable since {@code itemNameKey(String)} is not mandatory required to be called.</p>
   */
  protected @Nullable String itemNameKeyField;

  /**
   * False when the value should not be open to public (like password).
   * 
   * <p>Default value is {@code true}.</p>
   */
  protected boolean showsValue = true;

  /**
   * Constructs a new instance with {@code itemPropertyPath}.
   * 
   * <p>You cannot set recordPropertyPath here.
   *     Setting it causes a duplication of rootRecordName and it cannot be found.</p>
   * 
   * @param propertyPath itemPropertyPath
   */
  public Item(@RequireNonEmpty String propertyPath) {
    this.propertyPath = ObjectsUtil.requireNonEmpty(propertyPath);
  }

  /**
   * Sets {@code itemNameKey} and returns this for method chain.
   * 
   * <p>The format of itemNameKey is the same as itemNameKey, which is like "acc.name",
   *     always has one dot (not more than one) in the middle of the string.<br>
   *     But the argument of the method can be like "name", itemNameKeyField only.
   *     In that case the value of itemnameKeyClass is determined by the rule 
   *     written at {@code getItemNameKey(rootRecordName)} javadoc.</p>
   * 
   * @param itemNameKey itemNameKey
   * @return Item
   */
  public Item itemNameKey(String itemNameKey) {
    ObjectsUtil.requireNonEmpty(itemNameKey);

    this.itemNameKeyClassSetExplicitly =
        itemNameKey.contains(".") ? itemNameKey.substring(0, itemNameKey.lastIndexOf(".")) : null;
    this.itemNameKeyField =
        itemNameKey.contains(".") ? itemNameKey.substring(itemNameKey.lastIndexOf(".") + 1)
            : itemNameKey;

    return this;
  }

  public String getPropertyPath() {
    return propertyPath;
  }

  /**
   * Returns whether the itemNameKeyClass is set explicitly.
   * 
   * @return boolean
   */
  public boolean setsItemNameKeyClassExplicitly() {
    return itemNameKeyClassSetExplicitly != null;
  }

  /**
   * Returns {@code itemNameKey} value.
   * 
   * <p>See {@code MessageUtil.getItemNameKey(@Nullable String defaultItemNameKeyClass)} 
   *     with {@code defaultItemNameKeyClass = null}.</p>
   */
  public String getItemNameKey() {
    return MessageUtil.getItemNameKey(itemNameKeyClassSetExplicitly, itemNameKeyClassFromAnnotation,
        itemNameKeyClassFromClassName, itemNameKeyField, propertyPath);
  }

  /**
   * Returns {@code itemNameKey} value.
   * 
   * <p>See {@code MessageUtil.getItemNameKey(@Nullable String defaultItemNameKeyClass)} 
   *     with {@code defaultItemNameKeyClass = null}.</p>
   */
  public String getItemNameKey(Object rootBean) {
    String leafBeanPropertyPath =
        propertyPath.contains(".") ? propertyPath.substring(0, propertyPath.lastIndexOf(".")) : "";
    Object leafBean = ReflectionUtil.getLeafBean(rootBean, leafBeanPropertyPath);

    return MessageUtil.getItemNameKey(itemNameKeyClassSetExplicitly, rootBean, leafBean.getClass(),
        leafBean.getClass().getSimpleName(), itemNameKeyField, propertyPath);
  }

  /**
   * Hides value from error messages and so on.
   */
  public Item hideValue() {
    showsValue = false;

    return this;
  }

  /**
   * Shows value.
   */
  public Item showsValue(boolean showsValue) {
    this.showsValue = showsValue;

    return this;
  }

  public boolean getShowsValue() {
    return showsValue;
  }

  /**
   * Sets itemNameKeyClass from {@code @ItemNameKeyClass} annotation.
   * 
   * @param itemNameKeyClass argument {@code itemNameKeyClass} is @{code @NonNull} 
   *     but property {@code itemNameKeyClass} is {@code @Nullable} 
   *     because the method is not always called.
   */
  public void setItemNameKeyClassFromAnnotation(String itemNameKeyClass) {
    this.itemNameKeyClassFromAnnotation = itemNameKeyClass;
  }

  public void setItemNameKeyClassFromClassName(String itemNameKeyClassFromClassName) {
    this.itemNameKeyClassFromClassName = itemNameKeyClassFromClassName;
  }
}
