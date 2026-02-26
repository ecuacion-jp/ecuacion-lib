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
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import jp.ecuacion.lib.core.annotation.RequireElementNonempty;
import jp.ecuacion.lib.core.annotation.RequireNonempty;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.annotation.RequireSizeNonzero;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Provides utilities to handle variables embedded in a string, 
 *     enclosed by some symbols from both sides.
 */
public class EmbeddedVariableUtil {

  private static final String MSG_PREFIX = "jp.ecuacion.lib.core.util.EmbeddedVariableUtil.";

  /**
   * Prevents other classes from instantiating it.
   */
  private EmbeddedVariableUtil() {}

  /*
   * Finds an index of symbol ignoring a symbol with an escape character.
   */
  private static int getFirstFoundIndexOfSymbol(String string, String symbol) {
    int indexOfSymbol;
    int indexOfSymbolWithEscape;
    int ordinal = 1;

    while (true) {
      indexOfSymbol = StringUtils.ordinalIndexOf(string, symbol, ordinal);
      indexOfSymbolWithEscape = StringUtils.ordinalIndexOf(string, "\\" + symbol, ordinal);

      if (indexOfSymbol > 0 && indexOfSymbolWithEscape == indexOfSymbol - 1) {
        ordinal++;
        continue;

      } else {
        return indexOfSymbol;
      }
    }
  }

  /**
   * Returns a first-found variable name embedded in an argument string.
   * 
   * <p>The method searches a variable from the start of the string, 
   *     and returns the first one.
   *     When startSymbol = "${" and endSymbol = "}" (linux shell variable),
   *     return value from string the string <pre>a${b}c${d}e</pre> is {@code b}.</p>
   * 
   * <p>You cannot replace variables to values one by one with a string with multiple variables.
   *     You need to know all the variable locations first, and then replace them to values
   *     because you cannot find the location of the second variable 
   *     if the replaced first value contains the start symbol.</p>
   * 
   * @param string string which contains variable(s) 
   * @return variable name, may be {@code null} 
   *     when argument string doesn't contain variables.
   * @throws StringFormatIncorrectException StringFormatIncorrectException
   * @throws MultipleAppException MultipleAppException
   */
  @Nullable
  static String getFirstFoundEmbeddedVariable(@RequireNonnull String string,
      @RequireNonempty String startSymbol, @RequireNonempty String endSymbol,
      @Nullable Options options) throws StringFormatIncorrectException, MultipleAppException {

    ObjectsUtil.requireNonNull(string);
    ObjectsUtil.requireNonEmpty(startSymbol, endSymbol);

    // extract "${VAR}"

    // if escape character ("\") exists before start or end symbol, it needs to be ignored
    int startIndex = getFirstFoundIndexOfSymbol(string, startSymbol);
    int endIndex = getFirstFoundIndexOfSymbol(string, endSymbol);

    // in the case of an absence of start and end symbol
    if (startIndex < 0 && endIndex < 0) {
      return null;
    }

    // incorrect format
    if (startIndex < 0 && endIndex >= 0 || startIndex > endIndex) {
      if (options == null || !options.ignoresEmergenceOfEndSymbolOnly) {
        throw new StringFormatIncorrectException(string, startSymbol, endSymbol);

      } else {
        // Ignore the emergence of endSymbol. Remove that part and call the method recursively.
        return getFirstFoundEmbeddedVariable(
            string.substring(getFirstFoundIndexOfSymbol(string, endSymbol) + endSymbol.length()),
            startSymbol, endSymbol, options);
      }
    }

    // return "VAR"
    String var =
        string.substring(startIndex + startSymbol.length(), endIndex - 1 + endSymbol.length());

    return var;
  }

  /**
   * Returns a first-found variable name embedded in an argument string 
   * with multiple kinds of start symbols.
   * 
   * <p>For example, there are two kinds of start symbols: 
   * <pre>${+ ... } and ${- ... }</pre>.
   * And the argument string is: a${+b}c${-d}e.</p>
   * 
   * <p>It premises that the end symbol is always the same.
   *     It seems too much to consider into account that end symbols can also differ.</p>
   * 
   * @param string string
   * @param startSymbols multiple kinds of start symbols
   * @param endSymbol end symbol
   * @return Pair: start symbol is the left side, and the variable name is the right-side,
   *     may be {@code null} when the argument string doesn't contain variables.
   * @throws MultipleAppException MultipleAppException
   * @throws StringFormatIncorrectException StringFormatIncorrectException
   */
  @Nullable
  static Pair<String, String> getFirstFoundEmbeddedVariable(@RequireNonnull String string,
      @RequireNonnull @RequireSizeNonzero @RequireElementNonempty String[] startSymbols,
      @RequireNonempty String endSymbol, @Nullable Options options)
      throws MultipleAppException, StringFormatIncorrectException {

    ObjectsUtil.requireSizeNonZero(ObjectsUtil.requireNonNull(startSymbols));
    List<StringFormatIncorrectException> exList = new ArrayList<>();

    Map<Integer, String> firstFoundParamNameMap = new HashMap<>();
    for (String startSymbol : startSymbols) {
      try {
        String param = getFirstFoundEmbeddedVariable(string, startSymbol, endSymbol, options);

        if (param != null) {
          int i = string.indexOf(startSymbol + param + endSymbol);
          firstFoundParamNameMap.put(i, startSymbol);
        }

      } catch (StringFormatIncorrectException ex) {
        exList.add(ex);
      }
    }

    // Sort the key of the map and get the first one.
    List<Integer> firstFoundParamNameList =
        firstFoundParamNameMap.keySet().stream().sorted().toList();

    if (firstFoundParamNameList.size() == 0) {
      if (exList.size() == 0) {
        // simply no variable found.
        return null;

      } else {
        // format is wrong
        throw new MultipleAppException(exList);
      }

    } else {
      String firstFoundStartSymbol = firstFoundParamNameMap.get(firstFoundParamNameList.get(0));
      return Pair.of(firstFoundParamNameMap.get(firstFoundParamNameList.get(0)),
          getFirstFoundEmbeddedVariable(string, firstFoundStartSymbol, endSymbol, options));
    }
  }

  /**
   * Divides the argument string into simple string and variable parts and Returns list of them.
   * 
   * <p>When you replace embedded variable into strings, the following logic is not good:
   * search 1st variable -> replace 1st variable -> search 2nd variable -> ...
   * because if 1st variable contains start or end symbol, 2nd variable cannot be found.</p>
   * 
   * <p>The logic must be like this:
   * search all variables -> replace all variables</p>
   * 
   * <p>This method is in charge of the former part.
   *     It searches and divides all the variables and strings.</p>
   * 
   * <p>In the case that there are multiple start symbols, 
   *     it's passed as String[].</p>
   * 
   * @return Pair of String, String. 
   *     The left side of the pair is the startSymbol, 
   *     and the right variable name when param exists.
   *     And {null, "string"} when it's simple string.
   *     It returns list with size zero when the argument string is blank("").
   * @throws MultipleAppException MultipleAppException
   * @throws StringFormatIncorrectException StringFormatIncorrectException
   */
  @Nonnull
  public static List<Pair<String, String>> getPartList(@RequireNonnull String string,
      @RequireNonnull @RequireSizeNonzero @RequireElementNonempty String[] startSymbols,
      @RequireNonempty String endSymbol, @Nullable Options options)
      throws StringFormatIncorrectException, MultipleAppException {

    // the left side of the pair is startSymbol, and the right variable name.
    Pair<String, String> param = null;

    // search all the variables and put each part to the list.
    List<Pair<String, String>> list = new ArrayList<>();

    String part = string;
    while (true) {
      // In each loop, one part is added to the list.
      // but when a variable exists and there is a normal string part before the variable,
      // both the normal string and a variable are added to the list.

      param = getFirstFoundEmbeddedVariable(part, startSymbols, endSymbol, options);

      // param == null means variable not contained in part
      if (param == null) {
        if (part.length() > 0) {
          list.add(Pair.of(null, part));
        }

        return list;

      } else {
        String firstFoundStartSymbol = param.getLeft();
        String firstFoundParamName = param.getRight();

        String variableWithSymbols = firstFoundStartSymbol + firstFoundParamName + endSymbol;

        // When a prefix string exists, add it to list
        if (part.indexOf(variableWithSymbols) > 0) {
          list.add(Pair.of(null, part.substring(0, part.indexOf(variableWithSymbols))));
        }

        // Add the variable to list
        list.add(Pair.of(firstFoundStartSymbol, firstFoundParamName));

        part = part.substring(part.indexOf(variableWithSymbols) + variableWithSymbols.length());
      }
    }
  }

  /**
   * Divides the argument string into simple string and variable parts and Returns list of them.
   * 
   * <p>When you replace embedded variable into strings, the following logic is not good:
   * search 1st variable -> replace 1st variable -> search 2nd variable -> ...
   * because if 1st variable constains start or end symbol, 2nd variable cannot be found.</p>
   * 
   * <p>The logic must be like this:
   * search all variables -> replace all variables</p>
   * 
   * <p>This method is in charge of the former part.
   *     It searches and divides all the variables and strings.</p>
   * 
   * <p>In the case that there are multiple start symbols, 
   *     it's passed as String[].</p>
   * 
   * @return Pair of String, String. 
   *     The left side of the pair is the startSymbol, 
   *     and the right variable name when param exists.
   *     And {null, "string"} when it's simple string.
   *     It returns list with size zero when the argument string is blank("").
   * @throws AppException AppException 
   */
  @Nonnull
  public static List<Pair<String, String>> getPartList(@RequireNonnull String string,
      @RequireNonnull @RequireSizeNonzero @RequireElementNonempty String[] startSymbols,
      @RequireNonempty String endSymbol) throws AppException {

    return getPartList(string, startSymbols, endSymbol, null);
  }

  private static Function<String, String> getValueGetterFromKey(Map<String, String> parameterMap) {
    return (key) -> {
      return parameterMap.get(key);
    };
  }

  /**
   * Returns string with embedded variables replaced.
   * 
   * @param string string with variables embedded
   * @param startSymbol left-side symbol enclosing variables
   * @param endSymbol right-side symbol enclosing variables
   * @param valueGetterFromKey Function which obtains value from key.
   * @param options options
   * @return string with embedded variables replaced
   * @throws MultipleAppException MultipleAppException
   * @throws StringFormatIncorrectException StringFormatIncorrectException
   * @throws VariableNotFoundException StringFormatIncorrectException
   */
  public static String getVariableReplacedString(@RequireNonnull String string,
      @RequireNonempty String startSymbol, @RequireNonempty String endSymbol,
      @RequireNonnull Function<String, String> valueGetterFromKey, @Nullable Options options)
      throws StringFormatIncorrectException, MultipleAppException, VariableNotFoundException {

    ObjectsUtil.requireNonNull(valueGetterFromKey);

    List<Pair<String, String>> list =
        getPartList(string, new String[] {startSymbol}, endSymbol, options);

    StringBuilder sb = new StringBuilder();
    for (Pair<String, String> pair : list) {

      if (pair.getLeft() == null) {
        sb.append(pair.getRight());

      } else {
        // Throw an error when the map does not contain the key
        String value = valueGetterFromKey.apply(pair.getRight());
        if (value == null) {
          throw new VariableNotFoundException(pair.getRight());

        } else {
          sb.append(value);
        }
      }
    }

    return sb.toString();
  }

  /**
   * Returns string with embedded variables replaced.
   * 
   * @param string string with variables embedded
   * @param startSymbol left-side symbol enclosing variables
   * @param endSymbol right-side symbol enclosing variables
   * @param valueGetterFromKey Function which obtains value from key.
   * @return string with embedded variables replaced
   * @throws MultipleAppException MultipleAppException
   * @throws StringFormatIncorrectException StringFormatIncorrectException
   * @throws VariableNotFoundException StringFormatIncorrectException
   */
  public static String getVariableReplacedString(@RequireNonnull String string,
      @RequireNonempty String startSymbol, @RequireNonempty String endSymbol,
      @RequireNonnull Function<String, String> valueGetterFromKey)
      throws StringFormatIncorrectException, MultipleAppException, VariableNotFoundException {

    return getVariableReplacedString(string, startSymbol, endSymbol, valueGetterFromKey, null);
  }

  /**
   * Returns string with embedded variables replaced.
   * 
   * @param string string with variables embedded
   * @param startSymbol left-side symbol enclosing variables
   * @param endSymbol right-side symbol enclosing variables
   * @param parameterMap It stores parameter keys and those values.
   * @param options options
   * @return string with embedded variables replaced
   * @throws MultipleAppException MultipleAppException
   * @throws StringFormatIncorrectException StringFormatIncorrectException
   * @throws VariableNotFoundException StringFormatIncorrectException
   */
  public static String getVariableReplacedString(@RequireNonnull String string,
      @RequireNonempty String startSymbol, @RequireNonempty String endSymbol,
      @RequireNonnull Map<String, String> parameterMap, @Nullable Options options)
      throws StringFormatIncorrectException, MultipleAppException, VariableNotFoundException {

    ObjectsUtil.requireNonNull(parameterMap);

    return getVariableReplacedString(string, startSymbol, endSymbol,
        getValueGetterFromKey(parameterMap), options);
  }

  /**
   * Returns string with embedded variables replaced.
   * 
   * @param string string with variables embedded
   * @param startSymbol left-side symbol enclosing variables
   * @param endSymbol right-side symbol enclosing variables
   * @param parameterMap It stores parameter keys and those values.
   * @return string with embedded variables replaced
   * @throws MultipleAppException MultipleAppException
   * @throws StringFormatIncorrectException StringFormatIncorrectException
   * @throws VariableNotFoundException VariableNotFoundException
   */
  public static String getVariableReplacedString(@RequireNonnull String string,
      @RequireNonempty String startSymbol, @RequireNonempty String endSymbol,
      @RequireNonnull Map<String, String> parameterMap)
      throws StringFormatIncorrectException, MultipleAppException, VariableNotFoundException {

    return getVariableReplacedString(string, startSymbol, endSymbol, parameterMap, null);
  }

  /**
   * Provides options.
   */
  public static class Options {

    boolean ignoresEmergenceOfEndSymbolOnly = false;

    /**
     * Sets {@code ignoresEndSymbolShowsUpBeforeStartSymbolDoes}.
     * 
     * <p>When it's true, StringFormatIncorrectException is not thrown 
     *     and the format incorrection is just ignored.<br><br>
     *     
     *     It's supposed to used in the following cases:</p>
     *     <pre>
     *     - string     : Error! {+item_names:user.id} value incorrect. (value: {0})
     *     - startSymbol: {+item_names:
     *     - endSymbol  : }
     *     </pre>
     *     
     * <p>The point is, <code>{0}</code> is not replaced in this procedure.
     * It's maybe replace in other procedures.<br>
     * In this case the emergence of endSymbol needs to be ignored.</p>
     *     
     * @param ignoresEmergenceOfEndSymbolOnly ignoresEmergenceOfEndSymbolOnly
     */
    public Options setIgnoresEmergenceOfEndSymbolOnly(boolean ignoresEmergenceOfEndSymbolOnly) {
      this.ignoresEmergenceOfEndSymbolOnly = ignoresEmergenceOfEndSymbolOnly;

      return this;
    }
  }

  /**
   * Designates an exception which occurs because the format of an argument string is wrong.
   */
  public static class StringFormatIncorrectException extends BizLogicAppException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new instance.
     */
    public StringFormatIncorrectException(String string, String startSymbol, String endSymbol) {
      super(MSG_PREFIX + "variableFormatIncorrect.message", string, startSymbol, endSymbol);
    }
  }

  /**
   * Designates an exception which occurs because the format of an argument string is wrong.
   */
  public static class VariableNotFoundException extends BizLogicAppException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new instance.
     */
    public VariableNotFoundException(String key) {
      super(MSG_PREFIX + "paramNotFoundInMap.message", key);
    }
  }
}
