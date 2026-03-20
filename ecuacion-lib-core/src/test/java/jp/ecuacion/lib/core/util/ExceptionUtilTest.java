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

import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jp.ecuacion.lib.core.exception.checked.ConstraintViolationExceptionWithParameters;
import jp.ecuacion.lib.core.jakartavalidation.bean.AlwaysFalse;
import jp.ecuacion.lib.core.util.ExceptionUtilTest.VariousPlaces.Child;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ExceptionUtilTest {

  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void before() {
    PropertyFileUtil.addResourceBundlePostfix("lib-core-test");
  }

  @Test
  public void getMessageList_propertyPathLabelNameDisplayWithVariousParenthesisTest() {
    // field validator
    // Italian (parenthesis not blank)
    String message = ExceptionUtil
        .getMessageList(validator.validate(new FieldValidator(null)), Locale.ITALIAN).get(0);
    Assertions.assertEquals("'nome' è obbligatorio", message);
    // German (parenthesis blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new FieldValidator(null)), Locale.GERMAN).get(0);
    Assertions.assertEquals("Name ist erforderlich", message);

    // class validator with 1 field
    // Italian (parenthesis not blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator1(null)), Locale.ITALIAN).get(0);
    Assertions.assertEquals("'classValidator1.str1' è messaggio di esempio", message);
    // German (parenthesis blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator1(null)), Locale.GERMAN).get(0);
    Assertions.assertEquals("ClassValidator1.str1 ist Beispielnachricht", message);

    // class validator with multiple fields
    // Italian (parenthesis not blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator2(null, null)), Locale.ITALIAN).get(0);
    Assertions.assertEquals(
        "'classValidator2.str1' | 'classValidator2.str2' è messaggio di esempio", message);
    // German (parenthesis blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator2(null, null)), Locale.GERMAN).get(0);
    Assertions.assertEquals("ClassValidator2.str1, classValidator2.str2 ist Beispielnachricht",
        message);
  }

  public static record FieldValidator(@NotNull String str1) {
  }

  @AlwaysFalse(propertyPath = "str1")
  public static record ClassValidator1(String str1) {
  }

  @AlwaysFalse(propertyPath = {"str1", "str2"})
  public static record ClassValidator2(String str1, String str2) {
  }

  private String getMsg(Object obj) {
    return ExceptionUtil.getMessageList(validator.validate(obj), Locale.ENGLISH, true).get(0);
  }

  @Test
  public void getMessageList_variousPlacesTest() {
    String message = null;

    // normal
    message = getMsg(new VariousPlaces.Normal(null));
    Assertions.assertEquals("'normal.name' must not be null.", message);
    // inside child node
    message = getMsg(new VariousPlaces.InsideChildNode(new Child(null)));
    Assertions.assertEquals("'child.name' must not be null.", message);
    // inside child node in list
    message = getMsg(
        new VariousPlaces.InsideChildNodeInList(Arrays.asList(new Child[] {new Child(null)})));
    Assertions.assertEquals("'myChildList[0].name' must not be null.", message);
    // anonymous class
    message = getMsg(new VariousPlaces.AnonymousClass());
    Assertions.assertEquals("'myChild.name' must not be null.", message);
  }

  public static class VariousPlaces {
    public static record Normal(@NotNull String name) {
    }
    public static record InsideChildNode(@Valid Child myChild) {
    }
    public static record InsideChildNodeInList(List<@Valid Object> myChildList) {
    }
    public static class AnonymousClass {
      @Valid
      public ChildIf myChild = new ChildIf() {
        @NotNull
        private String name;
      };
    }

    public static record Child(@NotNull String name) {
    }
    public static interface ChildIf {
    };
  }

  private String validateCollection(Object object) {
    ConstraintViolationExceptionWithParameters ex =
        (ConstraintViolationExceptionWithParameters) ValidationUtil
            .validateThenReturn(object, ValidationUtil.messageParameters()).get();

    return ExceptionUtil.getMessageList(ex, Locale.ENGLISH, true).get(0);
  }

  @Test
  public void getMessageList_messageWhenCollectionFieldOfBasicObjectsIsNull() {
    String msg = null;

    // Common

    // Container (besides collection) needed to validate values.
    @Valid
    @NotNull
    List<@NotNull String> lst = new ArrayList<>();
    lst.add(null);
    Assertions.assertEquals(0, validator.validate(lst).size());

    // List is null (propertyPath: stringList)
    msg = validateCollection(new ColFieldOfBasicNull.StringList(null));
    Assertions.assertEquals("'string list' must not be null.", msg);
    // element of List is null (propertyPath: stringList[1].<list element>)
    List<String> strList = new ArrayList<>();
    strList.add("a");
    strList.add(null);
    msg = validateCollection(new ColFieldOfBasicNull.StringList(strList));
    Assertions.assertEquals("'string list' 2 must not be null.", msg);

    // Set is null (propertyPath: integerSet)
    msg = validateCollection(new ColFieldOfBasicNull.IntegerSet(null));
    Assertions.assertEquals("'integer set' must not be null.", msg);
    // element of List is null (propertyPath: integerSett[].<iterable element>)
    Set<Integer> intSet = new HashSet<>();
    intSet.add(1);
    intSet.add(null);
    msg = validateCollection(new ColFieldOfBasicNull.IntegerSet(intSet));
    Assertions.assertEquals("One of 'integer set' must not be null.", msg);

    // Map is null (propertyPath: strDateMap)
    msg = validateCollection(new ColFieldOfBasicNull.StringDateMap(null));
    Assertions.assertEquals("'string date map' must not be null.", msg);
    // element of Map key is null (propertyPath: strDateMap<K>[].<map key>)
    Map<String, LocalDate> strDateMap = new HashMap<>();
    strDateMap.put(null, LocalDate.now());
    msg = validateCollection(new ColFieldOfBasicNull.StringDateMap(strDateMap));
    Assertions.assertEquals("One of the key item of 'string date map' must not be null.", msg);
    // element of Map value is null (propertyPath: strDateMap[test].<map value>)
    strDateMap.clear();
    strDateMap.put("testKey", null);
    msg = validateCollection(new ColFieldOfBasicNull.StringDateMap(strDateMap));
    Assertions.assertEquals("Value of 'string date map' with key [testKey] must not be null.", msg);

    // array is null (propertyPath: blArray)
    msg = validateCollection(new ColFieldOfBasicNull.BooleanArray(null));
    Assertions.assertEquals("'boolean array' must not be null.", msg);
    // element of array cannot be validated.
    Assertions.assertEquals(0, validator
        .validate(new ColFieldOfBasicNull.BooleanArray(new Boolean[] {true, null})).size());
  }

  public static class ColFieldOfBasicNull {
    public static record StringList(@NotNull List<@NotNull String> strList) {
    }
    public static record IntegerSet(@NotNull Set<@NotNull Integer> intSet) {
    }
    public static record StringDateMap(
        @NotNull Map<@NotNull String, @NotNull LocalDate> strDateMap) {
    }
    public static record BooleanArray(@NotNull Boolean @NotNull [] blArray) {
    }
  }

  @Test
  public void getMessageList_messageWhenCollectionFieldOfBasicObjectsIsNotNonNullValidation() {
    String msg = null;

    // element of List is not regexp (propertyPath: strList[1].<list element>)
    List<String> strList = new ArrayList<>();
    strList.add("1");
    strList.add("a");
    msg = validateCollection(new ColFieldOfBasicNonNull.StringList(strList));
    Assertions.assertEquals("'string list' 2 must match \"[1-9]*\". (input: a)", msg);

    // element of Set is not min (propertyPath: intSet[].<iterable element>)
    Set<Integer> intSet = new HashSet<>();
    intSet.add(5);
    intSet.add(2);
    msg = validateCollection(new ColFieldOfBasicNonNull.IntegerSet(intSet));
    Assertions.assertEquals("One of 'integer set' must be greater than or equal to 3. (input: 2)",
        msg);

    // element of Map key is not regexp (propertyPath: strDateMap<K>[a].<map key>)
    Map<String, LocalDate> strDateMap = new HashMap<>();
    strDateMap.put("a", LocalDate.now().plusDays(1));
    msg = validateCollection(new ColFieldOfBasicNonNull.StringDateMap(strDateMap));
    Assertions.assertEquals(
        "One of the key item of 'string date map' must match \"[1-9]*\". (input: a)", msg);
    // element of Map value is null (propertyPath: strDateMap[1].<map value>)
    strDateMap.clear();
    strDateMap.put("1", LocalDate.now());
    msg = validateCollection(new ColFieldOfBasicNonNull.StringDateMap(strDateMap));
    Assertions.assertEquals(
        "Value of 'string date map' with key [1] must be a future date. (input: 2026-03-19)", msg);

    // array is null (propertyPath: blArray)
    msg = validateCollection(new ColFieldOfBasicNonNull.BooleanArray(null));
    Assertions.assertEquals("'boolean array' must not be null.", msg);
    // element of array cannot be validated.
    Assertions.assertEquals(0, validator
        .validate(new ColFieldOfBasicNonNull.BooleanArray(new Boolean[] {true, null})).size());
  }

  public static class ColFieldOfBasicNonNull {
    public static record StringList(List<@Pattern(regexp = "[1-9]*") String> strList) {
    }
    public static record IntegerSet(Set<@Min(3) Integer> intSet) {
    }
    public static record StringDateMap(
        Map<@Pattern(regexp = "[1-9]*") String, @Future LocalDate> strDateMap) {
    }
    public static record BooleanArray(@NotNull Boolean @NotNull [] blArray) {
    }
  }

  @Test
  public void getMessageList_messageWhenMultipleLayersOfCollectionFieldOfBasicObjects() {
    String msg = null;

    // element of List in List is null
    // (propertyPath: strListList[0].<list element>[0].<list element>)
    List<List<String>> strListList = new ArrayList<>();
    List<String> strList = new ArrayList<>();
    strList.add("a");
    strList.add(null);
    strListList.add(strList);
    msg = validateCollection(new ColFieldOfMulLayerBasic.StringListList(strListList));
    Assertions.assertEquals("'data' 1 > 'string list list' 2 must not be null.", msg);

    // element of Set in Set is null
    // (propertyPath: intSetSet[].<iterable element>[].<iterable element>)
    Set<Set<Integer>> intSetSet = new HashSet<>();
    Set<Integer> intSet = new HashSet<>();
    intSet.add(1);
    intSet.add(null);
    intSetSet.add(intSet);
    msg = validateCollection(new ColFieldOfMulLayerBasic.IntegerSetSet(intSetSet));
    Assertions.assertEquals("One of 'data' > one of 'integer set set' must not be null.", msg);

    // element of Map key in Map key is null
    // (propertyPath: strDateMapMap<K>[{null=2026-03-19}].<map key><K>[].<map key>)
    Map<Map<String, LocalDate>, Map<String, LocalDate>> strDateMapMap = new HashMap<>();
    Map<String, LocalDate> nonnullStrDateMap = new HashMap<>();
    nonnullStrDateMap.put("a", LocalDate.of(2001, 1, 1));
    Map<String, LocalDate> strDateMap = new HashMap<>();
    strDateMap.put(null, LocalDate.now());
    strDateMapMap.put(strDateMap, nonnullStrDateMap);
    msg = validateCollection(new ColFieldOfMulLayerBasic.StringDateMapMap(strDateMapMap));
    Assertions.assertEquals(
        "One of the key item of 'data' > one of the key item of 'string date map map' must not be null.",
        msg);
    // element of Map value in Map value is null
    // (propertyPath: strDateMapMap[{a=2026-03-19}].<map value>[testKey].<map value>)
    strDateMap.clear();
    strDateMap.put("testKey", null);
    strDateMapMap.clear();
    strDateMapMap.put(nonnullStrDateMap, strDateMap);
    msg = validateCollection(new ColFieldOfMulLayerBasic.StringDateMapMap(strDateMapMap));
    Assertions.assertEquals(
        "Value of 'data' with key [{a=2001-01-01}] > value of 'string date map map' with key [testKey] "
            + "must not be null.",
        msg);

    // element of array in array cannot be validated.
    Assertions.assertEquals(0, validator.validate(
        new ColFieldOfMulLayerBasic.BooleaArraynArray(new Boolean[][] {new Boolean[] {true, null}}))
        .size());

    // mix
    Map<String, @NotNull Integer> map = new HashMap<>();
    map.put("testKey", null);
    Set<Map<String, @NotNull Integer>> setMap = Set.of(map);
    List<Set<Map<String, @NotNull Integer>>> listSetMap = List.of(setMap);
    msg = validateCollection(new ColFieldOfMulLayerBasic.ListSetMap(listSetMap));
    Assertions.assertEquals(
        "'data' 1 > one of 'data' > value of 'list set map' with key [testKey] must not be null.",
        msg);
  }

  public static class ColFieldOfMulLayerBasic {
    public static record StringListList(@NotNull List<@NotNull List<@NotNull String>> strListList) {
    }
    public static record IntegerSetSet(@NotNull Set<@NotNull Set<@NotNull Integer>> intSetSet) {
    }
    public static record StringDateMapMap(
        @NotNull Map<@NotNull Map<@NotNull String, @NotNull LocalDate>, Map<@NotNull String, @NotNull LocalDate>> strDateMapMap) {
    }
    public static record BooleaArraynArray(@NotNull Boolean @NotNull [] @NotNull [] blArrayArray) {
    }
    public static record ListSetMap(List<Set<Map<String, @NotNull Integer>>> listSetMap) {
    }
  }

  private String validateCollectionWithPath(Object object) {
    ConstraintViolationExceptionWithParameters ex =
        (ConstraintViolationExceptionWithParameters) ValidationUtil
            .validateThenReturn(object, ValidationUtil.messageParameters()).get();

    return ExceptionUtil.getMessageList(ex, Locale.ENGLISH, true).get(0);
  }
}
