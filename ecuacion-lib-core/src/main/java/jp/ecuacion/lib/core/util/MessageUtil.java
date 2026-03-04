package jp.ecuacion.lib.core.util;

import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import jp.ecuacion.lib.core.annotation.RequireNonnull;

/**
 * Provides utilities for Message creation.
 */
public class MessageUtil {

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
    final String prependParenthesis =
        "${+messages:jp.ecuacion.lib.core.common.value.prependSymbol}";
    final String appendParenthesis = "${+messages:jp.ecuacion.lib.core.common.value.appendSymbol}";
    final String separator = "${+messages:jp.ecuacion.lib.core.common.value.separator}";

    List<String> itemNameList = Arrays.asList(ObjectsUtil.requireNonNull(values)).stream()
        .map(name -> prependParenthesis + name + appendParenthesis).toList();

    return StringUtil.getSeparatedValuesString(itemNameList, separator);
  }

  /**
   * Returns an array of values of formattedString(resolved to message by Arg.formattedString) 
   *     considering the prependSymbol, appendSymbol and the separator.
   */
  public static String getValuesOfFormattedString(@RequireNonnull List<String> valueList) {
    return getValuesOfFormattedString(valueList.toArray(new String[valueList.size()]));
  }
}
