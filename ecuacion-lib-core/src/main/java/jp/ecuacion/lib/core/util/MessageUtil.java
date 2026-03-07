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
import jp.ecuacion.lib.core.util.PropertyFileUtil.Arg;
import jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum;

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
   * Returns an array of item names considering prependSymbol, appendSymbol and separator.
   */
  @Nonnull
  public static String getItemNames(Locale locale, @RequireNonnull String[] itemNameKeys) {
    final String prependParenthesis =
        PropertyFileUtil.getMessage(locale, "jp.ecuacion.lib.core.common.itemName.prependSymbol");
    final String appendParenthesis =
        PropertyFileUtil.getMessage(locale, "jp.ecuacion.lib.core.common.itemName.appendSymbol");
    final String separator =
        PropertyFileUtil.getMessage(locale, "jp.ecuacion.lib.core.common.itemName.separator");

    List<String> itemNameList = Arrays.asList(ObjectsUtil.requireNonNull(itemNameKeys)).stream()
        .map(key -> PropertyFileUtil.getItemName(locale, key))
        .map(name -> prependParenthesis + name + appendParenthesis).toList();

    return StringUtil.getSeparatedValuesString(itemNameList, separator);
  }

  /**
   * Returns an array of item names considering prependSymbol, appendSymbol and separator.
   */
  public static String getItemNames(Locale locale, @RequireNonnull List<String> itemNameKeyList) {
    return getItemNames(locale, itemNameKeyList.toArray(new String[itemNameKeyList.size()]));
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
