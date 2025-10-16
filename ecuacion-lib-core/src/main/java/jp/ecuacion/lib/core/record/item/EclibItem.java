package jp.ecuacion.lib.core.record.item;

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
   * 
   * <p>The display name can be obtained by referring {@code item_names.properties} with it.</p>
   */
  protected String itemNameKeyClass;

  /**
   * Is a field part (= right part) of itemNameKey. (like "name" from itemNameKey: "acc.name")
   * 
   * <p>The display name can be obtained by referring {@code item_names.properties} with it.</p>
   */
  protected String itemNameKeyField;

  /**
   * Constructs a new instance with {@code itemPropertyPath}.
   * 
   * <p>Generally propertyPath starts with a rootRecord 
   *     (a record field name which is directly defined in a form),
   *     which should be like {@code user.name} or {@code user.dept.name}. 
   *     (It's called {@code recordPropertyPath} in the library.)<br>
   *     But here you need to set {@code itemPropertyPath}, 
   *     which is a propertyPath with rootRecordName + "." removed at the start part of it.<br><br>
   *     You cannot set recordPropertyPath here.
   *     Setting it is considered as a duplication of rootRecordName.</p>
   * 
   * @param itemPropertyPath itemPropertyPath
   */
  public EclibItem(@RequireNonempty String itemPropertyPath) {

    this.itemPropertyPath = ObjectsUtil.requireNonEmpty(itemPropertyPath);

    // Remove far left part ("name" in "acc.name") from propertyPath.
    // It's null when propertyPath doesn't contain ".".
    String itemPropertyPathClass = itemPropertyPath.contains(".")
        ? itemPropertyPath.substring(0, itemPropertyPath.lastIndexOf("."))
        : null;

    this.itemNameKeyClass = itemPropertyPathClass == null ? null
        : (itemPropertyPathClass.contains(".")
            ? itemPropertyPathClass.substring(itemPropertyPathClass.lastIndexOf(".") + 1)
            : itemPropertyPathClass);
    this.itemNameKeyField = itemPropertyPath.contains(".")
        ? itemPropertyPath.substring(itemPropertyPath.lastIndexOf(".") + 1)
        : itemPropertyPath;
  }

  public String getItemPropertyPath() {
    return itemPropertyPath;
  }

  /**
   * Sets {@code itemNameKey} and returns this for method chain.
   * 
   * <p>The format of itemNameKey is the same as itemNameKey, which is like "acc.name",
   *     always has one dot (not more than one) in the middle of the string.<br>
   *     But the argument of the method can be like "name".
   *     In that case itemNameKeyClass is used instead. 
   *     When itemNameKeyClass is {@code null}, rootRecordName is used instead.</p>
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

  /**
   * Returns {@code itemNameKey} value.
   * 
   * <p>Its value is {@code null} means 
   *     the item's original itemNameKey is equal to itemNameKey.</p>
   * 
   * @return itemNameKeyFieldForName
   */
  public String getItemNameKey(String rootRecordName) {
    String classPart = !StringUtils.isEmpty(itemNameKeyClass) ? itemNameKeyClass : rootRecordName;
    String fieldPart = itemNameKeyField;

    return classPart + "." + fieldPart;
  }
  
}
