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
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.jakartavalidation.validator.PatternWithDescription;
import org.apache.commons.lang3.StringUtils;

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

  /**
   * Returns a variable name embedded in an argument string.
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
   * @return variable name, may be null when argument string doesn't contain shell variables.
   * @throws MultipleAppException MultipleAppException
   * @throws AppException AppException
   */
  static String getFirstFoundEmbeddedParameter(String string, String startSymbol, String endSymbol)
      throws AppException {

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
      throw new BizLogicAppException(MSG_PREFIX + "variableFormatIncorrect.message", string,
          startSymbol, endSymbol);
    }

    // return "VAR"
    String var =
        string.substring(startIndex + startSymbol.length(), endIndex - 1 + endSymbol.length());
    ValidationUtil.validateThenThrow(new ValidationBean(var));

    return var;
  }

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
   * Returns string with embedded parameters replaced.
   * 
   * @param string string with parameters embedded
   * @param startSymbol left-side symbol enclosing parameters
   * @param endSymbol right-side symbol enclosing parameters
   * @param map It stores parameter keys and those values.
   * @return string with embedded parameters replaced
   * @throws AppException AppException
   */
  public static String getParameterReplacedString(String string, String startSymbol,
      String endSymbol, Map<String, String> map) throws AppException {
    String param = null;

    // The following logic is not good:
    // search 1st parameter -> replace 1st parameter -> search 2nd parameter -> ...
    // Because if 1st parameter constains start or end symbol, 2nd parameter cannot be found.
    // So logic must be like this:
    // search all parameters -> replace all parameters

    // search all the parameters and put each part to the list.
    List<Part> list = new ArrayList<>();
    String str = string;
    while (true) {
      // In each loop, one part is added to the list.
      // but when a parameter exists and there is a normal string part before the parameter,
      // both the normal string and a parameter are added to the list.

      param = getFirstFoundEmbeddedParameter(str, startSymbol, endSymbol);

      // End loop when var == null, which means parameter not contained in str
      if (param == null) {
        if (str.length() > 0) {
          list.add(new Part(false, str));
        }

        break;
      }

      // Throw an error when the map does not contain the key
      if (!map.containsKey(param)) {
        throw new BizLogicAppException(MSG_PREFIX + "paramNotFoundInMap.message", param);
      }

      String parameterWithSymbols = startSymbol + param + endSymbol;

      // When a prefix string exists, add it to list
      if (str.indexOf(parameterWithSymbols) > 0) {
        list.add(new Part(false, str.substring(0, str.indexOf(parameterWithSymbols))));
      }

      // Add the parameter to list
      list.add(new Part(true, param));

      str = str.substring(str.indexOf(parameterWithSymbols) + parameterWithSymbols.length());
    }

    StringBuilder sb = new StringBuilder();
    for (Part part : list) {
      String partStr = part.isParameter() ? map.get(part.getValue()) : part.getValue();
      sb.append(partStr);
    }

    return sb.toString();
  }

  private static class Part {
    private boolean isParameter;
    // Store normal string when isParameter == false, parameter name otherwise.
    // (without start and end symbol)
    private String value;

    public Part(boolean isParameter, String value) {
      this.isParameter = isParameter;
      this.value = value;
    }

    public boolean isParameter() {
      return isParameter;
    }

    public String getValue() {
      return value;
    }
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
}
