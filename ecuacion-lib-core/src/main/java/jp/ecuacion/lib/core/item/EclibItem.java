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
   */
  protected String itemNameKeyClass;

  /**
   * Is a field part (= right part) of itemNameKey. (like "name" from itemNameKey: "acc.name")
   */
  protected String itemNameKeyField;

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
   * <p>There can be 3 candidates for itemNameKeyClass,<br>
   *     1: itemNameKeyClass part of itemNameKey set by itemNameKey(itemNameKey)<br>
   *     2: part of itemPropertyPath<br>
   *     3: rootRecordName set to getItemNameKey(rootRecordName)<br><br>
   *     
   *     When 1 exists, 1 is always used. <br>
   *     When 1 does not exist, if 2. contains "." the second last part of it<br>
   *     (if the string is 1.2.3.4, second last part is "3").<br>
   *     When 1 does not exist and 2. does not contain ".", 3. is used.<br>
   * </p>
   * 
   * <p>Notice that the return value of this method does not consider 
   *     {@code @ItemNameKeyClass} annotations, 
   *     which means the return value should not be used 
   *     for resolution of itemName directly. 
   *     Use {@code EclibRecord#getItemNameKey()} for it.</p>
   * 
   * @return itemNameKeyFieldForName
   */
  public String getItemNameKey(String defaultItemNameKeyClass) {
    String tmpItemNameKeyClass;
    String tmpItemNameKeyField;

    // tmpItemNameKeyClass
    if (!StringUtils.isEmpty(itemNameKeyClass)) {
      tmpItemNameKeyClass = itemNameKeyClass;

    } else if (itemPropertyPath.contains(".")) {
      // Remove far right part ("name" in "acc.name") from propertyPath.
      // It's null when propertyPath doesn't contain ".".
      String itemPropertyPathClass =
          itemPropertyPath.substring(0, itemPropertyPath.lastIndexOf("."));

      tmpItemNameKeyClass = (itemPropertyPathClass.contains(".")
          ? itemPropertyPathClass.substring(itemPropertyPathClass.lastIndexOf(".") + 1)
          : itemPropertyPathClass);

    } else {
      tmpItemNameKeyClass = defaultItemNameKeyClass;
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
}
