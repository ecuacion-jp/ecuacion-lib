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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.util.EmbeddedVariableUtil.Options;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link EmbeddedVariableUtil}. */
@DisplayName("EmbeddedVariableUtil")
public class EmbeddedVariableUtilTest {

  @BeforeEach
  public void before() {}

  private @Nullable String getVar(String string) {
    return EmbeddedVariableUtil.getFirstFoundEmbeddedVariable(string, "${", "}", null);
  }

  private @Nullable String getVarWithOpt(String string, Options options) {
    return EmbeddedVariableUtil.getFirstFoundEmbeddedVariable(string, "${", "}", options);
  }

  @Test
  @DisplayName("getFirstFoundEmbeddedVariable: finds first variable or returns null")
  public void getFirstFoundEmbeddedParameter() {
    // string empty
    assertThat(getVar("")).isNull();
    // parameter none
    assertThat(getVar("abc")).isNull();

    // parameter 1 - parameter only
    assertThat(getVar("${abc}")).isEqualTo("abc");
    // head of string
    assertThat(getVar("${a}bc")).isEqualTo("a");
    // middle of string
    assertThat(getVar("a${bc}de")).isEqualTo("bc");
    // tail of string
    assertThat(getVar("a${bc}")).isEqualTo("bc");

    // parameter multiple
    assertThat(getVar("${a}${b}c")).isEqualTo("a");
    assertThat(getVar("${a}b${c}")).isEqualTo("a");
    assertThat(getVar("${a}${b}${c}")).isEqualTo("a");

    // wrong format - start symbol only
    assertThatThrownBy(() -> getVar("${abc")).isInstanceOf(ViolationException.class);
    assertThatThrownBy(() -> getVar("a${bc")).isInstanceOf(ViolationException.class);
    assertThatThrownBy(() -> getVar("abc${")).isInstanceOf(ViolationException.class);
    // end symbol only
    assertThatThrownBy(() -> getVar("}abc")).isInstanceOf(ViolationException.class);
    assertThatThrownBy(() -> getVar("a}bc")).isInstanceOf(ViolationException.class);
    assertThatThrownBy(() -> getVar("abc}")).isInstanceOf(ViolationException.class);
    // end symbol before start symbol
    assertThatThrownBy(() -> getVar("}${abc")).isInstanceOf(ViolationException.class);
    assertThatThrownBy(() -> getVar("}abc${")).isInstanceOf(ViolationException.class);
    assertThatThrownBy(() -> getVar("a}bc${")).isInstanceOf(ViolationException.class);

    // ignoresEmergenceOfEndSymbolOnly == true
    Options opt = new EmbeddedVariableUtil.Options().setIgnoresEmergenceOfEndSymbolOnly(true);
    assertThat(getVarWithOpt("}abc", opt)).isNull();
    assertThat(getVarWithOpt("a}bc", opt)).isNull();
    assertThat(getVarWithOpt("abc}", opt)).isNull();
    assertThat(getVarWithOpt("}a}bc}", opt)).isNull();
    assertThat(getVarWithOpt("}a}${b}c}", opt)).isEqualTo("b");
    // end symbol before start symbol still throws even with option
    assertThatThrownBy(() -> getVar("}${abc")).isInstanceOf(ViolationException.class);
    assertThatThrownBy(() -> getVar("}abc${")).isInstanceOf(ViolationException.class);
    assertThatThrownBy(() -> getVar("a}bc${")).isInstanceOf(ViolationException.class);
  }

  private @Nullable Pair<@NonNull String, String> getVarWithMultipleStartSymbols(String string) {
    return EmbeddedVariableUtil.getFirstFoundEmbeddedVariable(string,
        new @NonNull String[]{"${+", "${-"}, "}", null);
  }

  @Test
  @DisplayName("getFirstFoundEmbeddedVariable with multiple start symbols returns start+value pair")
  public void getFirstFoundEmbeddedParameterWithStartSymbols() {
    // parameter none
    assertThat(getVarWithMultipleStartSymbols("abc")).isNull();

    // parameter 1
    assertThat(getVarWithMultipleStartSymbols("${+abc}")).isEqualTo(Pair.of("${+", "abc"));

    // parameter 2
    assertThat(getVarWithMultipleStartSymbols("${+a}${-b}")).isEqualTo(Pair.of("${+", "a"));
    assertThat(getVarWithMultipleStartSymbols("${+a}${+b}")).isEqualTo(Pair.of("${+", "a"));
    assertThat(getVarWithMultipleStartSymbols("${+ab}${-cd}")).isEqualTo(Pair.of("${+", "ab"));
    assertThat(getVarWithMultipleStartSymbols("${+ab}${+cd}")).isEqualTo(Pair.of("${+", "ab"));
    assertThat(getVarWithMultipleStartSymbols("a${+b}c${-d}e")).isEqualTo(Pair.of("${+", "b"));
    assertThat(getVarWithMultipleStartSymbols("a${+b}c${+d}e")).isEqualTo(Pair.of("${+", "b"));

    // wrong format
    assertThatThrownBy(() -> getVarWithMultipleStartSymbols("a}c${+d}e"))
        .isInstanceOf(ViolationException.class);
  }

  private List<Pair<@Nullable String, String>> getPartList(String string) {
    return EmbeddedVariableUtil.getPartList(string, new @NonNull String[]{"${+", "${-"}, "}");
  }

  @Test
  @DisplayName("getPartList splits string into literal and variable parts")
  public void getPartList() {
    List<Pair<@Nullable String, String>> rtn;

    // parameter none
    rtn = getPartList("abc");
    assertThat(rtn).hasSize(1);
    assertThat(rtn.get(0)).isEqualTo(Pair.of(null, "abc"));

    // parameter 1 - only
    rtn = getPartList("${+abc}");
    assertThat(rtn).hasSize(1);
    assertThat(rtn.get(0)).isEqualTo(Pair.of("${+", "abc"));

    // parameter 1 - head
    rtn = getPartList("${+a}bc");
    assertThat(rtn).hasSize(2);
    assertThat(rtn.get(0)).isEqualTo(Pair.of("${+", "a"));
    assertThat(rtn.get(1)).isEqualTo(Pair.of(null, "bc"));

    // parameter 1 - tail
    rtn = getPartList("a${+bc}");
    assertThat(rtn).hasSize(2);
    assertThat(rtn.get(0)).isEqualTo(Pair.of(null, "a"));
    assertThat(rtn.get(1)).isEqualTo(Pair.of("${+", "bc"));

    // parameter 1 - middle
    rtn = getPartList("a${+b}c");
    assertThat(rtn).hasSize(3);
    assertThat(rtn.get(0)).isEqualTo(Pair.of(null, "a"));
    assertThat(rtn.get(1)).isEqualTo(Pair.of("${+", "b"));
    assertThat(rtn.get(2)).isEqualTo(Pair.of(null, "c"));

    // empty string
    rtn = getPartList("");
  }

  public String getReplacedString(String string) {
    Map<String, String> paramMap = new HashMap<>();
    paramMap.put("key1", "value1");
    paramMap.put("key2", "value2");
    paramMap.put("key3", "value3");
    paramMap.put("key4", "${key1");
    paramMap.put("key5", "}a");
    return EmbeddedVariableUtil.getVariableReplacedString(string, "${", "}", paramMap);
  }

  @Test
  @DisplayName("getVariableReplacedString substitutes variables from map")
  public void getParameterReplacedString() {
    // parameter none
    assertThat(getReplacedString("abc")).isEqualTo("abc");

    // 1 parameter
    assertThat(getReplacedString("a${key1}c")).isEqualTo("avalue1c");

    // multiple parameters
    assertThat(getReplacedString("${key3}${key2}${key1}")).isEqualTo("value3value2value1");

    // param contains start or end symbol
    assertThat(getReplacedString("a${key4}c")).isEqualTo("a${key1c");
    assertThat(getReplacedString("${key5}bc")).isEqualTo("}abc");
    assertThat(getReplacedString("${key4}${key5}bc")).isEqualTo("${key1}abc");

    // string outside param contains start or end symbol with escape char
    assertThat(getReplacedString("a\\${key1\\}c")).isEqualTo("a\\${key1\\}c");
    assertThat(getReplacedString("a\\${key1\\}c${key1}e")).isEqualTo("a\\${key1\\}cvalue1e");
  }
}
