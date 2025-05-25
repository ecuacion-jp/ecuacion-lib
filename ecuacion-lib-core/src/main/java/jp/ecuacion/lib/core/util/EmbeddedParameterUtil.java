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
import jp.ecuacion.lib.core.annotation.RequireElementNonempty;
import jp.ecuacion.lib.core.annotation.RequireNonempty;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.annotation.RequireSizeNonzero;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.AppExceptionItemIds;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.jakartavalidation.validator.PatternWithDescription;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Provides utilities to handle parameters embeddded in a string, 
 *     enclosed by some symbols from both sides.
 */
public class EmbeddedParameterUtil {

  private static final String MSG_PREFIX = "jp.ecuacion.lib.core.util.EmbeddedParameterUtil.";

  /**
   * Prevents other classes from instantiating it.
   */
  private EmbeddedParameterUtil() {}

  /*
   * Finds an index of symbol ignoring a symbol with an escape character.
   */
  private static int getIndexOfSymbol(String string, String symbol) {
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
   * Returns a first-found parameter name embedded in an argument string.
   * 
   * <p>The method finds a variable from the start of the string, 
   *     and it returns the first one it finds.
   *     In case of startSymbol = "${" and endSymbol = "}" (linux shell variable),
   *     return value is {@code b} when the string is {@code <pre>a${b}c${d}e</pre>.</p>
   * 
   * <p>The return value of the method is often used like
   *     "If the value is present, else exit the while loop (break)".
   *     Even if the method returns Optional it's not very useful 
   *     because you cannot "break" in ifPresentOrElse. (because it's lambda)<br>
   *     That's why return value is not Optional 
   *     and it returns {@code null} when no value exists.</p>
   * 
   * <p>When a string with multiple variables, you cannot replace variables to values one by one.
   *     You need to know all the variable locations first, and then replace them to values
   *     because if the first value contains "}", 
   *     you cannot find the location of the second variable.
   *     That's why this method should not be used from outside, and it's not public.
   *     (It's not private because of unit test)</p>
   * 
   * @param string string which contains linux shell variable
   * @return variable name, may be {@code null} 
   *     when argument string doesn't contain parameters.
   * @throws MultipleAppException MultipleAppException
   * @throws AppException AppException
   */
  @Nullable
  static String getFirstFoundEmbeddedParameter(@RequireNonnull String string,
      @RequireNonempty String startSymbol, @RequireNonempty String endSymbol) throws AppException {

    ObjectsUtil.requireNonNull(string);
    ObjectsUtil.requireNonEmpty(startSymbol, endSymbol);

    if (startSymbol.length() == 0 || endSymbol.length() == 0) {
      throw new BizLogicAppException(MSG_PREFIX + "symbolLengthZero.message", startSymbol,
          endSymbol);
    }

    // extract "${VAR}"

    // if escape character ("\") exists before start or end symbol, it needs to be ignored
    int startIndex = getIndexOfSymbol(string, startSymbol);
    int endIndex = getIndexOfSymbol(string, endSymbol);

    // in the case of an absence of start and end symbol
    if (startIndex < 0 && endIndex < 0) {
      return null;
    }

    // incorrect format
    if (startIndex < 0 && endIndex >= 0 || startIndex > endIndex) {
      throw new StringFormatIncorrectException(string, startSymbol, endSymbol);
    }

    // return "VAR"
    String var =
        string.substring(startIndex + startSymbol.length(), endIndex - 1 + endSymbol.length());
    ValidationUtil.validateThenThrow(new ValidationBean(var));

    return var;
  }

  /**
   * Returns a first-found parameter name embedded in an argument string 
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
   * @return Pair: start symbol is the left side, and the parameter name is the right-side,
   *     may be {@code null} when argument string doesn't contain parameters.
   * @throws AppException AppException
   */
  @Nullable
  static Pair<String, String> getFirstFoundEmbeddedParameter(@RequireNonnull String string,
      @RequireNonnull @RequireSizeNonzero @RequireElementNonempty String[] startSymbols,
      @RequireNonempty String endSymbol) throws AppException {

    ObjectsUtil.requireSizeNonZero(ObjectsUtil.requireNonNull(startSymbols));
    List<StringFormatIncorrectException> exList = new ArrayList<>();

    Map<Integer, String> firstFoundParamNameMap = new HashMap<>();
    for (String startSymbol : startSymbols) {
      try {
        String param = getFirstFoundEmbeddedParameter(string, startSymbol, endSymbol);

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
        // simply no parameter found.
        return null;

      } else {
        // format is wrong
        throw new MultipleAppException(exList);
      }

    } else {
      String firstFoundStartSymbol = firstFoundParamNameMap.get(firstFoundParamNameList.get(0));
      return Pair.of(firstFoundParamNameMap.get(firstFoundParamNameList.get(0)),
          getFirstFoundEmbeddedParameter(string, firstFoundStartSymbol, endSymbol));
    }
  }

  /**
   * Divides the argument string into simple string and parameter parts and Returns list of them.
   * 
   * <p>When you replace embedded parameter into strings, the following logic is not good:
   * search 1st parameter -> replace 1st parameter -> search 2nd parameter -> ...
   * because if 1st parameter constains start or end symbol, 2nd parameter cannot be found.</p>
   * 
   * <p>The logic must be like this:
   * search all parameters -> replace all parameters</p>
   * 
   * <p>This method is in charge of the former part.
   *     It searches and divides all the parameters and strings.</p>
   * 
   * <p>In the case that there are multiple start symbols, 
   *     it's passed as String[].</p>
   * 
   * @return Pair of String, String. 
   *     The left side of the pair is the startSymbol, 
   *     and the right parameter name when param exists.
   *     And {null, "string"} when it's simple string.
   *     It returns list with size zero when the argument string is blank("").
   * @throws AppException AppException 
   */
  @Nonnull
  public static List<Pair<String, String>> getPartList(@RequireNonnull String string,
      @RequireNonnull @RequireSizeNonzero @RequireElementNonempty String[] startSymbols,
      @RequireNonempty String endSymbol) throws AppException {

    // the left side of the pair is startSymbol, and the right parameter name.
    Pair<String, String> param = null;

    // search all the parameters and put each part to the list.
    List<Pair<String, String>> list = new ArrayList<>();

    String part = string;
    while (true) {
      // In each loop, one part is added to the list.
      // but when a parameter exists and there is a normal string part before the parameter,
      // both the normal string and a parameter are added to the list.

      param = getFirstFoundEmbeddedParameter(part, startSymbols, endSymbol);

      // param == null means parameter not contained in part
      if (param == null) {
        if (part.length() > 0) {
          list.add(Pair.of(null, part));
        }

        return list;

      } else {
        String firstFoundStartSymbol = param.getLeft();
        String firstFoundParamName = param.getRight();

        String parameterWithSymbols = firstFoundStartSymbol + firstFoundParamName + endSymbol;

        // When a prefix string exists, add it to list
        if (part.indexOf(parameterWithSymbols) > 0) {
          list.add(Pair.of(null, part.substring(0, part.indexOf(parameterWithSymbols))));
        }

        // Add the parameter to list
        list.add(Pair.of(firstFoundStartSymbol, firstFoundParamName));

        part = part.substring(part.indexOf(parameterWithSymbols) + parameterWithSymbols.length());
      }
    }
  }

  /**
   * Returns string with embedded parameters replaced.
   * 
   * @param string string with parameters embedded
   * @param startSymbol left-side symbol enclosing parameters
   * @param endSymbol right-side symbol enclosing parameters
   * @param map It stores parameter keys and those values.
   * @return string with embedded parameters replaced
   * @throws AppException AppException
   */
  public static String getParameterReplacedString(@RequireNonnull String string,
      @RequireNonempty String startSymbol, @RequireNonempty String endSymbol,
      @RequireNonnull Map<String, String> map) throws AppException {
    
    ObjectsUtil.requireNonNull(map);

    List<Pair<String, String>> list = getPartList(string, new String[] {startSymbol}, endSymbol);

    StringBuilder sb = new StringBuilder();
    for (Pair<String, String> pair : list) {

      // Throw an error when the map does not contain the key
      if (pair.getLeft() != null && !map.containsKey(pair.getRight())) {
        throw new BizLogicAppException(MSG_PREFIX + "paramNotFoundInMap.message", pair.getRight());
      }

      String partStr = pair.getLeft() != null ? map.get(pair.getRight()) : pair.getRight();
      sb.append(partStr);
    }

    return sb.toString();
  }

  private static class ValidationBean {
    @PatternWithDescription(regexp = "^[a-zA-Z0-9_\\-\\.]*$", descriptionId = "embeddedParameter")
    private String value;

    public ValidationBean(String value) {
      this.value = value;
    }

    @SuppressWarnings("unused")
    public String getValue() {
      return value;
    }
  }

  /**
   * Designates an exception which occurs because the format of an argument string is wrong.
   */
  public static class StringFormatIncorrectException extends BizLogicAppException {

    private static final long serialVersionUID = 1L;
    private static final String MSG_ID = MSG_PREFIX + "variableFormatIncorrect.message";

    /**
     * Construct a new instance.
     */
    public StringFormatIncorrectException(String string, String startSymbol, String endSymbol) {
      super(MSG_ID, new String[] {string, startSymbol, endSymbol});
    }

    /**
     * Construct a new instance.
     */
    public StringFormatIncorrectException(AppExceptionItemIds itemIds, String string,
        String startSymbol, String endSymbol) {
      super(itemIds, MSG_ID, new String[] {string, startSymbol, endSymbol});
    }
  }
}
