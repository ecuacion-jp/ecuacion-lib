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

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.jakartavalidation.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean.FieldInfoBean;
import jp.ecuacion.lib.core.util.PropertyFileUtil.Arg;
import jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum;
import jp.ecuacion.lib.core.util.ReflectionUtil.ElementOfCollectionCannotBeObtainedException;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides utilities for Message creation.
 */
public class MessageUtil {
  private static final String MSG_CMN_VAL = "${+messages:jp.ecuacion.lib.core.common.value.";
  private static final String VALUE_PREPEND_SYMBOL = MSG_CMN_VAL + "prependSymbol}";
  private static final String VALUE_APPEND_SYMBOL = MSG_CMN_VAL + "appendSymbol}";
  private static final String VALUE_SEPARATOR = MSG_CMN_VAL + "separator}";

  private static final String ipf = "jp.ecuacion.lib.core.common.itemName.";
  private static final String ppf = "jp.ecuacion.lib.core.common.itemNamePath.";

  /**
   * Returns {@code itemNameKey} value.
   *     It resolves itemNameKeyClassFromAnnotation by leaBeanClass and propertyPath.
   */
  public static String getItemNameKey(String explicitlySetItemNameKeyClass, Object rootBean,
      Object leafBeanFromConstraintViolation, String defaultItemNameKeyClass,
      String itemNameKeyField, String propertyPath) {

    Class<?> leafBeanClass = ReflectionUtil.getClass(rootBean.getClass(),
        PropertyPathUtil.getPropertyPathWithoutRightMostNode(propertyPath));
    // Set finalDefaultItemNameKeyClass.
    Optional<ItemNameKeyClass> optAn =
        ReflectionUtil.searchAnnotationPlacedAtClass(leafBeanClass, ItemNameKeyClass.class);
    String itemNameKeyClassFromAnnotation = optAn.isEmpty() ? null : optAn.get().value();

    return getItemNameKey(explicitlySetItemNameKeyClass, itemNameKeyClassFromAnnotation,
        defaultItemNameKeyClass, leafBeanClass.getSimpleName(), itemNameKeyField, propertyPath);
  }

  /**
   * Returns {@code itemNameKey} value.
   * 
   * <p>There can be 5 candidates for itemNameKeyClass. Candidates are ordered by their priority.
   *     They are adopted only when they are not empty. The last one is never null.<br>
   *     1: itemNameKeyClass part of itemNameKey set by itemNameKey(itemNameKey)<br>
   *     // 2: itemNameKeyClass part of itemPropertyPath set by constructor<br>
   *     3: itemNameKeyClassFromAnnotation set by setItemNameKeyClassFromAnnotation(String)<br>
   *     4: defaultItemNameKeyClass, the argument of this method<br>
   *     5: uncapitalized className (always set by ItemContainer#getItem(String))
   * </p>
   */
  @Nonnull
  public static String getItemNameKey(String explicitlySetItemNameKeyClass,
      String itemNameKeyClassFromAnnotation, String defaultItemNameKeyClass,
      String itemNameKeyClassFromClassName, String itemNameKeyField, String propertyPath) {
    String tmpItemNameKeyClass;
    String tmpItemNameKeyField;

    // tmpItemNameKeyClass
    if (StringUtils.isNotEmpty(explicitlySetItemNameKeyClass)) {
      tmpItemNameKeyClass = explicitlySetItemNameKeyClass;

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
      tmpItemNameKeyField =
          PropertyPathUtil.removeCollectionPart(PropertyPathUtil.getRightMostNode(propertyPath));
    }

    return StringUtils.uncapitalize(tmpItemNameKeyClass) + "." + tmpItemNameKeyField;
  }

  /**
   * Returns an array of item names considering prependSymbol, appendSymbol and separator.
   */
  @Nonnull
  public static String getItemNames(Locale locale,
      @RequireNonnull List<FieldInfoBean> fieldInfoBeanList, boolean showsItemNamePath,
      Object rootBean) {
    final String separator = PropertyFileUtil.getMessage(locale, ipf + "separator");
    final String prependSymbol = PropertyFileUtil.getMessage(locale, ipf + "prependSymbol");
    final String appendSymbol = PropertyFileUtil.getMessage(locale, ipf + "appendSymbol");

    List<String> itemNameList = new ArrayList<>();
    for (FieldInfoBean infoBean : fieldInfoBeanList) {
      String itemName = getItemName(locale, infoBean, prependSymbol, appendSymbol);

      if (showsItemNamePath) {
        itemName =
            addItemNamePath(locale, rootBean, infoBean, itemName, prependSymbol, appendSymbol);
      }

      itemNameList.add(itemName);
    }

    String rtn = StringUtil.getSeparatedValuesString(itemNameList, separator);

    return StringUtils.capitalize(rtn);
  }

  private static String getItemName(Locale locale, FieldInfoBean infoBean,
      final String prependSymbol, final String appendSymbol) {

    String itemNameKey = infoBean.itemNameKey();

    // Handle collections and arrays.
    List<String> collectionLayerList = new ArrayList<>();
    String rightMostNode = PropertyPathUtil.getRightMostNode(infoBean.propertyPath());
    while (true) {
      if (rightMostNode.contains("<K>")) {
        collectionLayerList.add(rightMostNode.substring(rightMostNode.lastIndexOf("<K>")));
        rightMostNode = rightMostNode.substring(0, rightMostNode.lastIndexOf("<K>"));

      } else if (rightMostNode.contains("[")) {
        collectionLayerList.add(rightMostNode.substring(rightMostNode.lastIndexOf("[")));
        rightMostNode = rightMostNode.substring(0, rightMostNode.lastIndexOf("["));

      } else {
        break;
      }
    }

    // reverse order
    collectionLayerList = collectionLayerList.reversed();

    String wholeItemName = null;
    String itemName =
        prependSymbol + PropertyFileUtil.getItemName(locale, itemNameKey) + appendSymbol;

    if (collectionLayerList.size() == 0) {
      wholeItemName = itemName;

    } else {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < collectionLayerList.size(); i++) {
        // String elName = prependSymbol + "element" + appendSymbol;
        String itemNamePath = "";

        // Get index. (empty for Set)
        String itemNameKeyPart = collectionLayerList.get(i);
        String tmp = itemNameKeyPart.substring(itemNameKeyPart.lastIndexOf("["));
        String index = tmp.substring(1, tmp.indexOf("]"));

        // Add "one of ...", "... n", etc...
        if (itemNameKeyPart.endsWith(PropertyPathUtil.EL_LIST)) {
          index = Integer.toString(Integer.valueOf(index) + 1);
        }

        String keyword = null;
        if (itemNameKeyPart.contains("<")) {
          keyword = switch (itemNameKeyPart.substring(itemNameKeyPart.lastIndexOf("<"))) {
            case PropertyPathUtil.EL_LIST -> "order";
            case PropertyPathUtil.EL_SET -> "any";
            case PropertyPathUtil.EL_MAP_KEY -> "mapKey";
            case PropertyPathUtil.EL_MAP_VAL -> "mapValue";
            default -> throw new RuntimeException("Not assumed.");
          };

        } else {
          // In the case of Collection<CustomObject>, for now Collection is always 'List'.
          try {
            index = Integer.toString(Integer.valueOf(index) + 1);
            keyword = "order";

          } catch (NumberFormatException ex) {
            keyword = "index";
          }
        }

        itemNamePath = PropertyFileUtil.getMessage(locale, ipf + keyword, index);

        sb.append(
            (i == 0 ? "" : PropertyFileUtil.getMessage(locale, ppf + "separator")) + itemNamePath);
      }

      wholeItemName =
          PropertyFileUtil.getMessage(locale, ipf + "collectionItemName", itemName, sb.toString());
    }

    return wholeItemName.toString();
  }

  private static String addItemNamePath(Locale locale, Object rootBean, FieldInfoBean infoBean,
      String itemName, final String prependSymbol, final String appendSymbol) {

    final String pstring = PropertyFileUtil.getMessage(locale, ppf + "string");
    final String pseparator = PropertyFileUtil.getMessage(locale, ppf + "separator");

    // Cut each itemNamePath and put them into a list.
    String leafBeanPropertyPath =
        PropertyPathUtil.getPropertyPathWithoutRightMostNode(infoBean.propertyPath());
    List<String> itemNamePathList = new ArrayList<>();
    String prefix = "";
    for (String node : PropertyPathUtil.getNodeList(leafBeanPropertyPath)) {
      itemNamePathList.add(prefix + (prefix.equals("") ? "" : ".") + node);
      prefix = prefix + (prefix.equals("") ? "" : ".") + node;
    }

    // Return itemName when no itemNamePath exists.
    if (itemNamePathList.size() == 0) {
      return itemName;
    }

    // The following is when itemNamePath exists.

    List<String> modifiedPathItemNameList = new ArrayList<>();
    for (String path : itemNamePathList) {
      FieldInfoBean bean = getFieldInfoBean(path, rootBean, rootBean);
      modifiedPathItemNameList.add(getItemName(locale, bean, prependSymbol, appendSymbol));
    }

    String pathString = StringUtil.getSeparatedValuesString(modifiedPathItemNameList, pseparator);
    itemName = PropertyFileUtil.getMessage(pstring, itemName, pathString);

    return itemName;
  }

  /**
   * Sets {@code itemNameKey} and {@code showsValue}.
   * 
   * <p>It does not consider {@code @ItemNameKeyClass}. In order to consider it,
   *     {@code getRootRecordNameConsideringItemNameKeyClass(itemPropertyPath)} 
   *     needs to be used together.</p>
   * 
   * @param propertyPath itemPropertyPath
   * @return itemNameKey
   */
  public static FieldInfoBean getFieldInfoBean(String propertyPath, Object rootBean,
      Object leafBean) {

    String fullPropertyPath1stPart =
        propertyPath.contains(".") ? propertyPath.substring(0, propertyPath.indexOf(".")) : null;

    // firstChild cannot be obtained when the firstChild is Set or Map key.
    Object firstChild = null;
    try {
      firstChild = fullPropertyPath1stPart == null ? null
          : ReflectionUtil.getValue(rootBean, fullPropertyPath1stPart);

    } catch (ElementOfCollectionCannotBeObtainedException ex) {
      // Do nothing.
    }

    Item item = null;
    // boolean setsItemNameKeyClassExplicitly = false;

    String itemNameKey = null;
    boolean showsValue = true;

    String collectionPartRemovedPropertyPath = propertyPath;
    while (true) {
      String rightMostNode = PropertyPathUtil.getRightMostNode(collectionPartRemovedPropertyPath);

      if (rightMostNode.contains("[")) {
        collectionPartRemovedPropertyPath = collectionPartRemovedPropertyPath.substring(0,
            collectionPartRemovedPropertyPath.lastIndexOf("["));

      } else if (rightMostNode.contains("<")) {
        collectionPartRemovedPropertyPath = collectionPartRemovedPropertyPath.substring(0,
            collectionPartRemovedPropertyPath.lastIndexOf("<"));

      } else {
        break;
      }
    }

    // Get item if exists.
    if (rootBean instanceof ItemContainer) {
      // the case that rootBean is an EclibRecord
      item = ((ItemContainer) rootBean).getItem(propertyPath);

    } else if (firstChild != null && firstChild instanceof ItemContainer) {

      // the case that EclibRecord is stored in form or something
      item = ((ItemContainer) firstChild)
          .getItem(propertyPath.substring(fullPropertyPath1stPart.length() + 1));
    }

    if (item == null) {
      itemNameKey = MessageUtil.getItemNameKey(null, rootBean, leafBean, null, null, propertyPath);

    } else {
      itemNameKey = item.getItemNameKey();
      showsValue = item.getShowsValue();
    }

    FieldInfoBean bean = new FieldInfoBean(propertyPath, itemNameKey, showsValue);

    return bean;
  }

  /**
   * Returns an array of values of formattedString(resolved to message by Arg.formattedString) 
   *     considering the prependSymbol, appendSymbol and the separator.
   */
  @Nonnull
  public static String getValuesOfFormattedString(@RequireNonnull String[] values) {

    List<String> itemNameList = Arrays.asList(ObjectsUtil.requireNonNull(values)).stream()
        .map(name -> VALUE_PREPEND_SYMBOL + name + VALUE_APPEND_SYMBOL).toList();

    return StringUtil.getSeparatedValuesString(itemNameList, VALUE_SEPARATOR);
  }

  /**
   * Returns an array of values of formattedString(resolved to message by Arg.formattedString) 
   *     considering the prependSymbol, appendSymbol and the separator.
   */
  public static String getValuesOfFormattedString(@RequireNonnull List<String> valueList) {
    return getValuesOfFormattedString(valueList.toArray(new String[valueList.size()]));
  }

  /**
   * Returns an array of values of formattedString(resolved to message by Arg.formattedString) 
   *     considering the prependSymbol, appendSymbol and the separator.
   */
  @Nonnull
  public static Arg getValuesArg(@RequireNonnull String[] values) {
    // Get a list of Args from values
    String[] fileKinds = new String[] {PropertyFileUtilFileKindEnum.MESSAGES.toString(),
        PropertyFileUtilFileKindEnum.ITEM_NAMES.toString(),
        PropertyFileUtilFileKindEnum.ENUM_NAMES.toString()};
    List<Arg> argList = Arrays.asList(values).stream().map(str -> Arg.get(fileKinds, str)).toList();

    List<String> itemNameList = new ArrayList<>();
    for (int i = 0; i < argList.size(); i++) {
      itemNameList.add(VALUE_PREPEND_SYMBOL + "{" + i + "}" + VALUE_APPEND_SYMBOL);
    }

    return Arg.formattedString(StringUtil.getSeparatedValuesString(itemNameList, VALUE_SEPARATOR),
        argList.toArray(new Arg[argList.size()]));
  }

  /**
   * Returns an array of values of formattedString(resolved to message by Arg.formattedString) 
   *     considering the prependSymbol, appendSymbol and the separator.
   */
  @Nonnull
  public static Arg getValuesArg(@RequireNonnull List<String> valueList) {
    return getValuesArg(valueList.toArray(new String[valueList.size()]));
  }
}
