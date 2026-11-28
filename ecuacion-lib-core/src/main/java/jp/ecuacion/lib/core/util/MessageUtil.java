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
        extractCollectionLayers(PropertyPathUtil.getRightMostNode(item.getDisplayPropertyPath()));

    String itemName =
        prependSymbol + PropertiesFileUtil.getItemName(locale, itemNameKey) + appendSymbol;

    if (collectionLayerList.isEmpty()) {
      return itemName;
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < collectionLayerList.size(); i++) {
      String itemNameKeyPart = collectionLayerList.get(i);
      // Positions are searched in the masked string
      // so that characters in the toString() value of a map key are not misinterpreted.
      String maskedPart = PropertyPathUtil.maskBracketContents(itemNameKeyPart);
      int openPos = maskedPart.lastIndexOf('[');
      String index = itemNameKeyPart.substring(openPos + 1, maskedPart.indexOf(']', openPos));

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
    // Positions are searched in the masked string
    // so that characters in the toString() value of a map key are not misinterpreted.
    String masked = PropertyPathUtil.maskBracketContents(rightMostNode);
    while (true) {
      int cutPos = masked.contains("<K>") ? masked.lastIndexOf("<K>") : masked.lastIndexOf('[');
      if (cutPos < 0) {
        break;
      }

      layers.add(rightMostNode.substring(cutPos));
      rightMostNode = rightMostNode.substring(0, cutPos);
      masked = masked.substring(0, cutPos);
    }
    return layers.reversed();
  }

  private record KeywordAndIndex(String keyword, String index) {
  }

  private static KeywordAndIndex determineKeyword(String itemNameKeyPart, String index) {
    if (itemNameKeyPart.startsWith("<K>")) {
      // Map key access via @Valid cascade (e.g., targetMap<K>[keyRep] -> layer "<K>[keyRep]")
      return new KeywordAndIndex("mapKey", index);

    } else if (PropertyPathUtil.maskBracketContents(itemNameKeyPart).contains("<")) {
      String elType = itemNameKeyPart
          .substring(PropertyPathUtil.maskBracketContents(itemNameKeyPart).lastIndexOf("<"));
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
        PropertyPathUtil.getPropertyPathWithoutRightMostNode(item.getDisplayPropertyPath());
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
        itemNamePathList.stream().map(path -> getItemName(locale,
            ItemUtil.resolveItem(path, rootBean), prependSymbol, appendSymbol)).toList();

    String pathString = StringUtil.getSeparatedValuesString(modifiedPathItemNameList, pseparator);
    itemName = PropertiesFileUtil.getMessage(locale, pstring, itemName, pathString);

    return itemName;
  }

  /**
   * Wraps each value with display symbols and joins them with a separator.
   *
   * <p>Values are treated as literal strings without any property-key resolution.
   * Use this when the values themselves are already the final display strings.<br>
   * {@code #{...}}, braces and quotes in the values are also displayed as they are,
   * so values from untrusted input (like an end-user-supplied value) can be passed.</p>
   *
   * <p>Example (with English default symbols {@code '...'} and separator {@code ", "}):</p>
   * <pre>
   * formatValues(new String[]{"ACTIVE", "INACTIVE"})
   *   // resolves to "'ACTIVE', 'INACTIVE'"
   * </pre>
   */
  public static Arg formatValues(String[] values) {
    Objects.requireNonNull(values);

    // Values are passed as arguments, not embedded into the template,
    // so that #{...}, braces and quotes in them are not interpreted.
    return Arg.formattedString(placeholderTemplate(values.length), (Object[]) values);
  }

  /*
   * Returns the template like 
   * "#{prependSymbol}{0}#{appendSymbol}#{separator}#{prependSymbol}{1}..."
   * for the number of values.
   */
  private static String placeholderTemplate(int numberOfValues) {
    List<@NonNull String> placeholders = new ArrayList<>();
    for (int i = 0; i < numberOfValues; i++) {
      placeholders.add("{" + i + "}");
    }

    return StringUtil.getSeparatedValuesString(placeholders.toArray(String[]::new), VALUE_SEPARATOR,
        VALUE_PREPEND_SYMBOL, VALUE_APPEND_SYMBOL);
  }

  /**
   * Resolves each value as a property key, then wraps with display symbols and joins with
   * a separator. If a value is not found as a property key, it is used as a literal string.
   * An empty string is also used as a literal string (an empty string is not a key).
   *
   * <p>Searches across messages, item_names, enum_names, and constants (in that order).</p>
   *
   * <p>Example (with English default symbols {@code '...'} and separator {@code ", "}):</p>
   * <pre>
   * // enum_names.properties: status.active=Active, status.inactive=Inactive
   * formatValuesWithResolution(new String[]{"status.active", "status.inactive"})
   *   // resolves to "'Active', 'Inactive'"
   *
   * // key not found: used as-is
   * formatValuesWithResolution(new String[]{"ACTIVE", "INACTIVE"})
   *   // resolves to "'ACTIVE', 'INACTIVE'"
   * </pre>
   */
  public static Arg formatValuesWithResolution(String[] values) {
    // APPLICATION is excluded: its throwsExceptionWhenKeyDoesNotExist=true causes an exception
    // when a literal string (not a property key) is passed.
    PropertiesFileUtilFileKindEnum[] fileKinds = new PropertiesFileUtilFileKindEnum[] {
        PropertiesFileUtilFileKindEnum.MESSAGES, PropertiesFileUtilFileKindEnum.ITEM_NAMES,
        PropertiesFileUtilFileKindEnum.ENUM_NAMES, PropertiesFileUtilFileKindEnum.CONSTANTS};
    // An empty string is not a key, so it is passed as a literal string.
    @Nullable
    Object[] args = Arrays.stream(Objects.requireNonNull(values))
        .map(str -> str.isEmpty() ? str : Arg.fromFileKinds(fileKinds, str)).toArray();

    return Arg.formattedString(placeholderTemplate(args.length), args);
  }

}
