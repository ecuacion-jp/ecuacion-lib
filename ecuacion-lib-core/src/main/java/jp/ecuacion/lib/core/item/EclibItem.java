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
import jp.ecuacion.lib.core.annotation.RequireNonempty;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Stores item attributes.
 * 
 * <p>To understand details on item, 
 *     see <a href="https://github.com/ecuacion-jp/ecuacion-jp.github.io/blob/main/documentation/common/naming-convention.md">naming-convention.md</a></p>
 */
public class EclibItem {

  /**
   * Is the ID string of an item.
   * 
   * <p>rootRecordName part (= far left part) can be omitted. Namely it can be "name" or "dept.name"
   *     when the propertyPath with rootRecordName (= also called "recordPropertyPath") 
   *     is "acc.name" or "acc.dept.name" where "acc" is the rootRecordName.</p>
   */
  @Nonnull
  protected String itemPropertyPath;

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
   *     Setting it caauses a duplication of rootRecordName and it cannot be found.</p>
   * 
   * @param itemPropertyPath itemPropertyPath
   */
  public EclibItem(@RequireNonempty String itemPropertyPath) {

    this.itemPropertyPath =
        ObjectsUtil.requireNonEmpty(ObjectsUtil.requireNonEmpty(itemPropertyPath));
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
  public EclibItem itemNameKey(@RequireNonempty String itemNameKey) {
    ObjectsUtil.requireNonEmpty(itemNameKey);

    this.itemNameKeyClass =
        itemNameKey.contains(".") ? itemNameKey.substring(0, itemNameKey.lastIndexOf(".")) : null;
    this.itemNameKeyField =
        itemNameKey.contains(".") ? itemNameKey.substring(itemNameKey.lastIndexOf(".") + 1)
            : itemNameKey;

    return this;
  }

  public String getItemPropertyPath() {
    return itemPropertyPath;
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
   * <p>There can be 5 candidates for itemNameKeyClass. Candidates are ordered by their priority.
   *     They are adopted only when they are not empty. The last one is never null.<br>
   *     1: itemNameKeyClass part of itemNameKey set by itemNameKey(itemNameKey)<br>
   *     2: itemNameKeyClass part of itemPropertyPath set by constructor<br>
   *     3: itemNameKeyClassFromAnnotation set by setItemNameKeyClassFromAnnotation(String)<br>
   *     4: defaultItemNameKeyClass, the argument of this method<br>
   *     5: uncapitalized className (always set by EclibItemContainer#getItem(String))
   * </p>
   */
  @Nonnull
  public String getItemNameKey(@Nullable String defaultItemNameKeyClass) {
    String tmpItemNameKeyClass;
    String tmpItemNameKeyField;

    // tmpItemNameKeyClass
    if (StringUtils.isNotEmpty(itemNameKeyClass)) {
      tmpItemNameKeyClass = itemNameKeyClass;

    } else if (itemPropertyPath.contains(".")) {
      // Remove far right part ("name" in "acc.name") from propertyPath.
      // It's null when propertyPath doesn't contain ".".
      String itemPropertyPathClass =
          itemPropertyPath.substring(0, itemPropertyPath.lastIndexOf("."));

      tmpItemNameKeyClass = (itemPropertyPathClass.contains(".")
          ? itemPropertyPathClass.substring(itemPropertyPathClass.lastIndexOf(".") + 1)
          : itemPropertyPathClass);

    } else if (StringUtils.isNotEmpty(itemNameKeyClassFromAnnotation)) {
      tmpItemNameKeyClass = itemNameKeyClassFromAnnotation;

    } else if (StringUtils.isNotEmpty(defaultItemNameKeyClass)) {
      tmpItemNameKeyClass = defaultItemNameKeyClass;
      
    } else {
      tmpItemNameKeyClass = itemNameKeyClassFromClassName;
    }

    // tmpItemNameKeyField
    if (!StringUtils.isEmpty(itemNameKeyField)) {
      tmpItemNameKeyField = itemNameKeyField;

    } else {
      tmpItemNameKeyField = itemPropertyPath.contains(".")
          ? itemPropertyPath.substring(itemPropertyPath.lastIndexOf(".") + 1)
          : itemPropertyPath;
    }

    return tmpItemNameKeyClass + "." + tmpItemNameKeyField;
  }

  /**
   * Returns {@code itemNameKey} value.
   * 
   * <p>See {@code getItemNameKey(@Nullable String defaultItemNameKeyClass)} 
   *     with {@code defaultItemNameKeyClass = null}.</p>
   */
  @Nonnull
  public String getItemNameKey() {
    return getItemNameKey(null);
  }

  /**
   * Hides value from error messages and so on.
   */
  public EclibItem hideValue() {
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
