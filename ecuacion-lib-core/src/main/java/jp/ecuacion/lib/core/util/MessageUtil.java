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

  private static final String EL_LIST = "<list element>";
  private static final String EL_SET = "<iterable element>";
  private static final String EL_MAP_KEY = "<map key>";
  private static final String EL_MAP_VAL = "<map value>";


  private static final String ipf = "jp.ecuacion.lib.core.common.itemName.";
  private static final String ppf = "jp.ecuacion.lib.core.common.itemNamePath.";

  /**
   * Returns {@code itemNameKey} value.
   *     It resolves itemNameKeyClassFromAnnotation by leaBeanClass and propertyPath.
   */
  public static String getItemNameKey(String explicitlySetItemNameKeyClass, Object rootBean,
      Class<?> leafBeanClass, String defaultItemNameKeyClass, String itemNameKeyField,
      String propertyPath) {

    
    // Set finalDefaultItemNameKeyClass.
    Optional<ItemNameKeyClass> optAn =
        ReflectionUtil.searchAnnotationPlacedAtClass(leafBeanClass, ItemNameKeyClass.class);
    String itemNameKeyClassFromAnnotation = optAn.isEmpty() ? null : optAn.get().value();

    String leafBeanPropertyPath =
        propertyPath.contains(".") ? propertyPath.substring(0, propertyPath.lastIndexOf(".")) : "";
    Class<?> leafBeanClassClassValidatorConsidered =
        leafBeanPropertyPath.equals("") ? rootBean.getClass()
            : ReflectionUtil.getValue(rootBean, leafBeanPropertyPath).getClass();

    return getItemNameKey(explicitlySetItemNameKeyClass, itemNameKeyClassFromAnnotation,
        defaultItemNameKeyClass, leafBeanClassClassValidatorConsidered.getSimpleName(),
        itemNameKeyField, propertyPath);
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
      tmpItemNameKeyField = getItemNameKeyFieldFromPropertyPath(propertyPath);
    }

    return StringUtils.uncapitalize(tmpItemNameKeyClass) + "." + tmpItemNameKeyField;
  }

  /**
   * Creates itemNameKeyField from propertyPath.
   */
  private static String getItemNameKeyFieldFromPropertyPath(String propertyPath) {

    String tmpItemNameKeyField = propertyPath;

    // Replace dots to "_" in propertyPath from the right.
    while (true) {
      if (!tmpItemNameKeyField.contains(".")) {
        return tmpItemNameKeyField;
      }

      if (containsCollectionElement(tmpItemNameKeyField, true)) {
        // Replcae last emerged "." to "_".
        int idx = tmpItemNameKeyField.lastIndexOf(".");
        tmpItemNameKeyField =
            tmpItemNameKeyField.substring(0, idx) + "_" + tmpItemNameKeyField.substring(idx + 1);

      } else {
        tmpItemNameKeyField =
            tmpItemNameKeyField.substring(tmpItemNameKeyField.lastIndexOf(".") + 1);
        break;
      }
    }

    return tmpItemNameKeyField;
  }

  private static boolean containsCollectionElement(String itemNameKeyField, boolean startsWith) {

    String[] els = new String[] {EL_LIST, EL_SET, EL_MAP_KEY, EL_MAP_VAL};
    String leafNode = itemNameKeyField.substring(itemNameKeyField.lastIndexOf(".") + 1);

    for (String el : els) {
      boolean bl = startsWith ? leafNode.startsWith(el) : leafNode.endsWith(el);
      if (bl) {
        return true;
      }
    }

    return false;
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
    String tmpItemNameKey = itemNameKey;
    while (true) {
      if (containsCollectionElement(tmpItemNameKey, false)) {
        collectionLayerList.add(tmpItemNameKey.endsWith(EL_MAP_KEY)
            ? tmpItemNameKey.substring(tmpItemNameKey.lastIndexOf("<K>"))
            : tmpItemNameKey.substring(tmpItemNameKey.lastIndexOf("[")));

        tmpItemNameKey = tmpItemNameKey.endsWith(EL_MAP_KEY)
            ? tmpItemNameKey.substring(0, tmpItemNameKey.lastIndexOf("<K>"))
            : tmpItemNameKey.substring(0, tmpItemNameKey.lastIndexOf("["));

      } else {
        break;
      }
    }

    // reverse order
    collectionLayerList = collectionLayerList.reversed();

    StringBuilder itemName = new StringBuilder();
    if (collectionLayerList.size() == 0) {
      String tmpItemName = PropertyFileUtil.getItemName(locale, itemNameKey);
      itemName.append(prependSymbol + tmpItemName + appendSymbol);

    } else {
      for (int i = 0; i < collectionLayerList.size(); i++) {
        String itemNameKeyPart = collectionLayerList.get(i);

        // Get index. (empty for Set)
        String tmp = itemNameKeyPart.substring(itemNameKeyPart.lastIndexOf("["));
        String index = tmp.substring(1, tmp.indexOf("]"));

        String tmpItemName = "data";
        if (i == collectionLayerList.size() - 1) {
          tmpItemName = PropertyFileUtil.getItemName(locale, tmpItemNameKey);
        }

        tmpItemName = prependSymbol + tmpItemName + appendSymbol;

        // Add "one of ...", "... n", etc...
        if (containsCollectionElement(itemNameKeyPart, false)) {
          if (itemNameKeyPart.endsWith(EL_LIST)) {
            index = Integer.toString(Integer.valueOf(index) + 1);
          }

          String keyword = switch (itemNameKeyPart.substring(itemNameKeyPart.lastIndexOf("<"))) {
            case EL_LIST -> "order";
            case EL_SET -> "any";
            case EL_MAP_KEY -> "mapKey";
            case EL_MAP_VAL -> "mapValue";
            default -> throw new RuntimeException("Not assumed.");
          };

          tmpItemName = PropertyFileUtil.getMessage(locale, ipf + keyword, tmpItemName, index);
        }

        itemName.append(
            (i == 0 ? "" : PropertyFileUtil.getMessage(locale, ppf + "separator")) + tmpItemName);
      }
    }

    return itemName.toString();
  }

  private static String addItemNamePath(Locale locale, Object rootBean, FieldInfoBean infoBean,
      String itemName, final String prependSymbol, final String appendSymbol) {

    final String pstring = PropertyFileUtil.getMessage(locale, ppf + "string");
    final String pseparator = PropertyFileUtil.getMessage(locale, ppf + "separator");

    String tmpPropertyPath = infoBean.propertyPath();
    String prefix = "";

    List<String> itemNamePathList = new ArrayList<>();
    while (true) {
      if (!tmpPropertyPath.contains(".")) {
        break;
      }

      String rootNode = tmpPropertyPath.substring(0, tmpPropertyPath.indexOf("."));
      itemNamePathList.add(prefix + rootNode);
      tmpPropertyPath = tmpPropertyPath.substring(tmpPropertyPath.indexOf(".") + 1);
      prefix = prefix + rootNode + ".";
    }

    // Return itemName when no itemNamePath exists.
    if (itemNamePathList.size() == 0) {
      return itemName;
    }

    // The following is when itemNamePath exists.

    List<String> modifiedItemNamePathList = new ArrayList<>();
    for (String path : itemNamePathList) {
      String leaf = path.contains(".") ? path.split("\\.")[path.split("\\.").length - 1] : path;
      // Prepares for collections. Usually (means leaf does not contain "[") it's -1.
      int order = leaf.contains("[")
          ? Integer.parseInt(leaf.substring(leaf.indexOf("[") + 1, leaf.indexOf("]"))) + 1
          : -1;
      path = path.contains("[") ? path.substring(0, path.indexOf("[")) : path;
      String itemNameKey = getFieldInfoBean(path, rootBean,
          ReflectionUtil.getLeafBeanClass(rootBean.getClass(), path)).itemNameKey();
      String finalItemName =
          prependSymbol + PropertyFileUtil.getItemName(locale, itemNameKey) + appendSymbol;
      finalItemName = order == -1 ? finalItemName
          : PropertyFileUtil.getMessage(locale, ppf + "order", Integer.valueOf(order).toString())
              + finalItemName;

      modifiedItemNamePathList.add(finalItemName);
    }

    String pathString = StringUtil.getSeparatedValuesString(modifiedItemNamePathList, pseparator);
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
      Class<?> leafBeanClass) {

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
      itemNameKey =
          MessageUtil.getItemNameKey(null, rootBean, leafBeanClass, null, null, propertyPath);

    } else {
      itemNameKey = item.getItemNameKey();
      // setsItemNameKeyClassExplicitly = item.setsItemNameKeyClassExplicitly();
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
