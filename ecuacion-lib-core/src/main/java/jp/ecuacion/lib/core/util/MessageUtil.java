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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides utilities for Message creation.
 */
public class MessageUtil {
  private static final String MSG_CMN_VAL = "#{messages:jp.ecuacion.lib.core.common.value.";
  private static final String VALUE_PREPEND_SYMBOL = MSG_CMN_VAL + "prependSymbol}";
  private static final String VALUE_APPEND_SYMBOL = MSG_CMN_VAL + "appendSymbol}";
  private static final String VALUE_SEPARATOR = MSG_CMN_VAL + "separator}";

  private static final String ipf = "jp.ecuacion.lib.core.common.itemName.";
  private static final String ppf = "jp.ecuacion.lib.core.common.itemNamePath.";

  /**
   * Returns an array of item names considering prependSymbol, appendSymbol and separator.
   */
  public static String getItemNames(@Nullable Locale locale, List<@NonNull Item> itemList,
      boolean showsItemNamePath, Object rootBean) {
    final String separator = PropertiesFileUtil.getMessage(locale, ipf + "separator");
    final String prependSymbol = PropertiesFileUtil.getMessage(locale, ipf + "prependSymbol");
    final String appendSymbol = PropertiesFileUtil.getMessage(locale, ipf + "appendSymbol");

    List<@NonNull String> itemNameList = new ArrayList<>();
    for (Item infoBean : itemList) {
      Objects.requireNonNull(infoBean);
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

  private static String getItemName(@Nullable Locale locale, Item item, final String prependSymbol,
      final String appendSymbol) {

    String itemNameKey = item.getItemNameKey();
    List<@NonNull String> collectionLayerList =
        extractCollectionLayers(PropertyPathUtil.getRightMostNode(item.getPropertyPath()));

    String itemName =
        prependSymbol + PropertiesFileUtil.getItemName(locale, itemNameKey) + appendSymbol;

    if (collectionLayerList.isEmpty()) {
      return itemName;
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < collectionLayerList.size(); i++) {
      String itemNameKeyPart = collectionLayerList.get(i);
      String tmp = itemNameKeyPart.substring(itemNameKeyPart.lastIndexOf("["));
      String index = tmp.substring(1, tmp.indexOf("]"));

      KeywordAndIndex ki = determineKeyword(itemNameKeyPart, index);
      String itemNamePath = PropertiesFileUtil.getMessage(locale, ipf + ki.keyword(), ki.index());

      sb.append(
          (i == 0 ? "" : PropertiesFileUtil.getMessage(locale, ppf + "separator")) + itemNamePath);
    }

    return PropertiesFileUtil.getMessage(locale, ipf + "collectionItemName", itemName,
        sb.toString());
  }

  private static List<@NonNull String> extractCollectionLayers(String rightMostNode) {
    List<@NonNull String> layers = new ArrayList<>();
    while (true) {
      if (rightMostNode.contains("<K>")) {
        layers.add(rightMostNode.substring(rightMostNode.lastIndexOf("<K>")));
        rightMostNode = rightMostNode.substring(0, rightMostNode.lastIndexOf("<K>"));

      } else if (rightMostNode.contains("[")) {
        layers.add(rightMostNode.substring(rightMostNode.lastIndexOf("[")));
        rightMostNode = rightMostNode.substring(0, rightMostNode.lastIndexOf("["));

      } else {
        break;
      }
    }
    return layers.reversed();
  }

  private record KeywordAndIndex(String keyword, String index) {
  }

  private static KeywordAndIndex determineKeyword(String itemNameKeyPart, String index) {
    if (itemNameKeyPart.startsWith("<K>")) {
      // Map key access via @Valid cascade (e.g., targetMap<K>[keyRep] -> layer "<K>[keyRep]")
      return new KeywordAndIndex("mapKey", index);

    } else if (itemNameKeyPart.contains("<")) {
      String elType = itemNameKeyPart.substring(itemNameKeyPart.lastIndexOf("<"));
      String keyword = switch (elType) {
        case PropertyPathUtil.EL_LIST -> "order";
        case PropertyPathUtil.EL_SET -> "any";
        case PropertyPathUtil.EL_MAP_KEY -> "mapKey";
        case PropertyPathUtil.EL_MAP_VAL -> "mapValue";
        default -> throw new RuntimeException("Not assumed.");
      };
      String adjustedIndex =
          PropertyPathUtil.EL_LIST.equals(elType) ? Integer.toString(Integer.parseInt(index) + 1)
              : index;
      return new KeywordAndIndex(keyword, adjustedIndex);

    } else if (index.isEmpty()) {
      // Set element: empty index means unordered collection
      return new KeywordAndIndex("any", index);

    } else {
      try {
        KeywordAndIndex rtn =
            new KeywordAndIndex("order", Integer.toString(Integer.parseInt(index) + 1));
        return rtn;

      } catch (NumberFormatException ex) {
        // Non-integer index means Map value access via @Valid cascade
        return new KeywordAndIndex("mapValue", index);
      }
    }
  }

  private static String addItemNamePath(@Nullable Locale locale, Object rootBean, Item item,
      String itemName, final String prependSymbol, final String appendSymbol) {

    final String pstring = PropertiesFileUtil.getMessage(locale, ppf + "string");
    final String pseparator = PropertiesFileUtil.getMessage(locale, ppf + "separator");

    // Cut each itemNamePath and put them into a list.
    String leafBeanPropertyPath =
        PropertyPathUtil.getPropertyPathWithoutRightMostNode(item.getPropertyPath());
    List<@NonNull String> itemNamePathList = new ArrayList<>();
    String prefix = "";
    for (String node : PropertyPathUtil.getNodeList(leafBeanPropertyPath)) {
      itemNamePathList.add(prefix + (prefix.isEmpty() ? "" : ".") + node);
      prefix = prefix + (prefix.isEmpty() ? "" : ".") + node;
    }

    // Return itemName when no itemNamePath exists.
    if (itemNamePathList.isEmpty()) {
      return itemName;
    }

    // The following is when itemNamePath exists.

    List<@NonNull String> modifiedPathItemNameList =
        itemNamePathList
            .stream().map(path -> getItemName(locale,
                ItemUtil.resolveItem(path, rootBean, rootBean), prependSymbol, appendSymbol))
            .toList();

    String pathString = StringUtil.getSeparatedValuesString(modifiedPathItemNameList, pseparator);
    itemName = PropertiesFileUtil.getMessage(locale, pstring, itemName, pathString);

    return itemName;
  }

  /**
   * Returns an array of values of formattedString(resolved to message by Arg.formattedString) 
   *     considering the prependSymbol, appendSymbol and the separator.
   */
  public static String getValuesOfFormattedString(String[] values) {

    List<@NonNull String> itemNameList = Arrays.stream(ObjectsUtil.requireNonNull(values))
        .map(name -> VALUE_PREPEND_SYMBOL + name + VALUE_APPEND_SYMBOL).toList();

    return StringUtil.getSeparatedValuesString(itemNameList, VALUE_SEPARATOR);
  }

  /**
   * Returns an array of values of formattedString(resolved to message by Arg.formattedString) 
   *     considering the prependSymbol, appendSymbol and the separator.
   */
  public static String getValuesOfFormattedString(List<@NonNull String> valueList) {
    return getValuesOfFormattedString(valueList.toArray(String[]::new));
  }

  /**
   * Returns an array of values of formattedString(resolved to message by Arg.formattedString) 
   *     considering the prependSymbol, appendSymbol and the separator.
   */
  public static Arg getValuesArg(String[] values) {
    // Get a list of Args from values
    // APPLICATION is excluded: its throwsExceptionWhenKeyDoesNotExist=true causes an exception
    // when a literal string (not a property key) is passed.
    PropertiesFileUtilFileKindEnum[] fileKinds =
        new PropertiesFileUtilFileKindEnum[] {PropertiesFileUtilFileKindEnum.MESSAGES,
            PropertiesFileUtilFileKindEnum.ITEM_NAMES, PropertiesFileUtilFileKindEnum.ENUM_NAMES,
            PropertiesFileUtilFileKindEnum.CONSTANTS};
    List<@NonNull Arg> argList =
        Arrays.stream(values).map(str -> Arg.fromFileKinds(fileKinds, str)).toList();

    List<@NonNull String> itemNameList = new ArrayList<>();
    for (int i = 0; i < argList.size(); i++) {
      itemNameList.add(VALUE_PREPEND_SYMBOL + "{" + i + "}" + VALUE_APPEND_SYMBOL);
    }

    return Arg.formattedString(StringUtil.getSeparatedValuesString(itemNameList, VALUE_SEPARATOR),
        (Object[]) argList.toArray(Arg[]::new));
  }

  /**
   * Returns an array of values of formattedString(resolved to message by Arg.formattedString) 
   *     considering the prependSymbol, appendSymbol and the separator.
   */
  public static Arg getValuesArg(List<@NonNull String> valueList) {
    return getValuesArg(valueList.toArray(String[]::new));
  }
}
