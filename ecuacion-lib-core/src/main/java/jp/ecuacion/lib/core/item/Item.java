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
import jp.ecuacion.lib.core.annotation.RequireNonempty;
import jp.ecuacion.lib.core.util.MessageUtil;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.ReflectionUtil;

/**
 * Stores item attributes.
 * 
 * <p>To understand details on item, 
 *     see <a href="https://github.com/ecuacion-jp/ecuacion-jp.github.io/blob/main/documentation/common/naming-convention.md">naming-convention.md</a></p>
 */
public class Item {

  /**
   * Is the ID string of an item.
   * 
   * <p>When you want a item corresponding to the field 'name', just set 'name' to it.<br>
   *     It can be 'dept.name' when the field is 'dept' object.</p>
   */
  @Nonnull
  protected String propertyPath;

  /**
   * Is a class part (= left part) of itemNameKey. (like "acc" from itemNameKey: "acc.name")
   * 
   * <p>It is set by itemNameKey(String) and only when its argument contains ".".</p>
   * 
   * <p>The itemNameKeyClass obtained from {@code @ItemNameKeyClass} is not stored in it
   *     because the value from {@code @ItemNameKeyClass} is a default value 
   *     when this field is null.</p>
   */
  protected String itemNameKeyClass;

  /**
   * Is a field part (= right part) of itemNameKey. (like "name" from itemNameKey: "acc.name")
   */
  protected String itemNameKeyField;

  /**
   * Is false when the value should not be open to public (like password).
   * 
   * <p>Default value is {@code true}.</p>
   */
  protected boolean showsValue = true;

  private String itemNameKeyClassFromAnnotation;

  private String itemNameKeyClassFromClassName;

  /**
   * Constructs a new instance with {@code itemPropertyPath}.
   * 
   * <p>You cannot set recordPropertyPath here.
   *     Setting it causes a duplication of rootRecordName and it cannot be found.</p>
   * 
   * @param propertyPath itemPropertyPath
   */
  public Item(@RequireNonempty String propertyPath) {

    this.propertyPath = ObjectsUtil.requireNonEmpty(ObjectsUtil.requireNonEmpty(propertyPath));
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
  public Item itemNameKey(@RequireNonempty String itemNameKey) {
    ObjectsUtil.requireNonEmpty(itemNameKey);

    this.itemNameKeyClass =
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
    return itemNameKeyClass != null;
  }

  /**
   * Returns {@code itemNameKey} value.
   * 
   * <p>See {@code MessageUtil.getItemNameKey(@Nullable String defaultItemNameKeyClass)} 
   *     with {@code defaultItemNameKeyClass = null}.</p>
   */
  @Nonnull
  public String getItemNameKey() {
    return MessageUtil.getItemNameKey(itemNameKeyClass, itemNameKeyClassFromAnnotation,
        (String) null, itemNameKeyClassFromClassName, itemNameKeyField, propertyPath);
  }

  /**
   * Returns {@code itemNameKey} value.
   * 
   * <p>See {@code MessageUtil.getItemNameKey(@Nullable String defaultItemNameKeyClass)} 
   *     with {@code defaultItemNameKeyClass = null}.</p>
   */
  @Nonnull
  public String getItemNameKey(Object rootBean) {
    String leafBeanPropertyPath =
        propertyPath.contains(".") ? propertyPath.substring(0, propertyPath.lastIndexOf(".")) : "";
    Object leafBean = ReflectionUtil.getLeafBean(rootBean, leafBeanPropertyPath);

    return MessageUtil.getItemNameKey(itemNameKeyClass, rootBean, leafBean.getClass(),
        leafBean.getClass().getSimpleName(), itemNameKeyField, propertyPath);
  }

  /**
   * Returns {@code itemNameKey} value.
   * 
   * <p>See {@code MessageUtil.getItemNameKey(@Nullable String defaultItemNameKeyClass)}.</p>
   */
  @Deprecated
  @Nonnull
  public String getItemNameKey(String defaultItemNameKeyClass) {
    return MessageUtil.getItemNameKey(itemNameKeyClass, itemNameKeyClassFromAnnotation,
        defaultItemNameKeyClass, itemNameKeyClassFromClassName, itemNameKeyField, propertyPath);
  }

  /**
   * Hides value from error messages and so on.
   */
  public Item hideValue() {
    showsValue = false;

    return this;
  }

  public boolean getShowsValue() {
    return showsValue;
  }

  public void setItemNameKeyClassFromAnnotation(String itemNameKeyClas) {
    this.itemNameKeyClassFromAnnotation = itemNameKeyClas;
  }

  public void setItemNameKeyClassFromClassName(String itemNameKeyClassFromClassName) {
    this.itemNameKeyClassFromClassName = itemNameKeyClassFromClassName;
  }
}
