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

import java.util.HashMap;
import java.util.Map;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EmbeddedParameterUtilTest {
  @BeforeEach
  public void before() {}

  // methodize to shorten the method name
  private String getVar(String string) throws AppException {
    return EmbeddedParameterUtil.getFirstFoundEmbeddedParameter(string, "${", "}");
  }

  @Test
  public void getFirstFoundEmbeddedParameterTest() throws AppException {

    // variable none
    Assertions.assertEquals(null, getVar("abc"));

    // variable 1

    // variable only
    Assertions.assertEquals("abc", getVar("${abc}"));

    // head of string
    Assertions.assertEquals("a", getVar("${a}bc"));

    // middle of string
    Assertions.assertEquals("bc", getVar("a${bc}de"));

    // tail of string
    Assertions.assertEquals("bc", getVar("a${bc}"));

    // variable multiple

    Assertions.assertEquals("a", getVar("${a}${b}c"));
    Assertions.assertEquals("a", getVar("${a}b${c}"));
    Assertions.assertEquals("a", getVar("${a}${b}${c}"));

    // vaviable name charactoer kind (a-zA-Z0-9.-_ only)

    // ok
    Assertions.assertDoesNotThrow(() -> getVar("${azAZ09.-_}"));

    // NG
    Assertions.assertThrows(AppException.class, () -> getVar("a${#b}c"));

    // wrong format

    // start symbol only
    Assertions.assertThrows(BizLogicAppException.class, () -> getVar("${abc"));
    Assertions.assertThrows(BizLogicAppException.class, () -> getVar("a${bc"));
    Assertions.assertThrows(BizLogicAppException.class, () -> getVar("abc${"));
    // end symbol only
    Assertions.assertThrows(BizLogicAppException.class, () -> getVar("}abc"));
    Assertions.assertThrows(BizLogicAppException.class, () -> getVar("a}bc"));
    Assertions.assertThrows(BizLogicAppException.class, () -> getVar("abc}"));
    // end symbol before start symbol
    Assertions.assertThrows(BizLogicAppException.class, () -> getVar("}${abc"));
    Assertions.assertThrows(BizLogicAppException.class, () -> getVar("}abc${"));
    Assertions.assertThrows(BizLogicAppException.class, () -> getVar("a}bc${"));
  }

  public String getReplacedString(String string) throws AppException {

    Map<String, String> paramMap = new HashMap<>();
    paramMap.put("key1", "value1");
    paramMap.put("key2", "value2");
    paramMap.put("key3", "value3");
    paramMap.put("key4", "${key1");
    paramMap.put("key5", "}a");
    return EmbeddedParameterUtil.getParameterReplacedString(string, "${", "}", paramMap);
  }

  @Test
  public void getParameterReplacedString() throws AppException {
    // parameter none
    Assertions.assertEquals("abc", getReplacedString("abc"));

    // 1 parameter (1 pattern only because finding parameter partis tested 
    // in getFirstFoundEmbeddedParameterTest)
    Assertions.assertEquals("avalue1c", getReplacedString("a${key1}c"));
    
    // multiple parameters
    Assertions.assertEquals("value3value2value1", getReplacedString("${key3}${key2}${key1}"));

    // param contains start or end symbol
    Assertions.assertEquals("a${key1c", getReplacedString("a${key4}c"));
    Assertions.assertEquals("}abc", getReplacedString("${key5}bc"));
    Assertions.assertEquals("${key1}abc", getReplacedString("${key4}${key5}bc"));
    
    // string outside param contains start or end symbol with escape char
    Assertions.assertEquals("a\\${key1\\}c", getReplacedString("a\\${key1\\}c"));
    Assertions.assertEquals("a\\${key1\\}cvalue1e", getReplacedString("a\\${key1\\}c${key1}e"));
  }
}
