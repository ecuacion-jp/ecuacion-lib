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
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean.FieldInfoBean;
import jp.ecuacion.lib.core.util.PropertyFileUtil.Arg;
import jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides utilities for Message creation.
 */
public class MessageUtil {

  private static final String VALUE_PREPEND_SYMBOL =
      "${+messages:jp.ecuacion.lib.core.common.value.prependSymbol}";
  private static final String VALUE_APPEND_SYMBOL =
      "${+messages:jp.ecuacion.lib.core.common.value.appendSymbol}";
  private static final String VALUE_SEPARATOR =
      "${+messages:jp.ecuacion.lib.core.common.value.separator}";

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
          propertyPath.contains(".") ? propertyPath.substring(propertyPath.lastIndexOf(".") + 1)
              : propertyPath;
    }

    return StringUtils.uncapitalize(tmpItemNameKeyClass) + "." + tmpItemNameKeyField;
  }

  /**
   * Returns an array of item names considering prependSymbol, appendSymbol and separator.
   */
  public static String getItemNames(Locale locale, @RequireNonnull FieldInfoBean[] fieldNameBeans,
      boolean showsItemNamePath) {
    return getItemNames(locale, Arrays.asList(fieldNameBeans), showsItemNamePath);
  }

  /**
   * Returns an array of item names considering prependSymbol, appendSymbol and separator.
   */
  @Nonnull
  public static String getItemNames(Locale locale,
      @RequireNonnull List<FieldInfoBean> fieldInfoBeanList, boolean showsItemNamePath) {
    final String prependParenthesis =
        PropertyFileUtil.getMessage(locale, "jp.ecuacion.lib.core.common.itemName.prependSymbol");
    final String appendParenthesis =
        PropertyFileUtil.getMessage(locale, "jp.ecuacion.lib.core.common.itemName.appendSymbol");
    final String separator =
        PropertyFileUtil.getMessage(locale, "jp.ecuacion.lib.core.common.itemName.separator");

    List<String> itemNameList = new ArrayList<>();
    for (FieldInfoBean infoBean : fieldInfoBeanList) {
      String itemName = PropertyFileUtil.getItemName(locale, infoBean.itemNameKey);
      itemName = prependParenthesis + itemName + appendParenthesis;

      // Add itemNamePath when showsItemNamePath == true.
      if (showsItemNamePath) {
        String propertyPath = infoBean.fullPropertyPath;
        System.out.println(propertyPath);
      }

      itemNameList.add(itemName);
    }

    return StringUtil.getSeparatedValuesString(itemNameList, separator);
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
