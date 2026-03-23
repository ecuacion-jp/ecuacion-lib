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
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Locale;
import jp.ecuacion.lib.core.exception.checked.ConstraintViolationExceptionWithParameters;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.jakartavalidation.annotation.ItemNameKeyClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ExceptionUtilTest_getMessageList_3_CollectionValues_CustomObjects {

  // private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void before() {
    PropertyFileUtil.addResourceBundlePostfix("lib-core-test");
  }

  private String validateCollection(Object object, boolean isMsgWithItemName,
      boolean showsItemManeMapth) {
    ConstraintViolationExceptionWithParameters ex =
        (ConstraintViolationExceptionWithParameters) ValidationUtil
            .validateThenReturn(object, ValidationUtil.messageParameters()
                .isMessageWithItemName(isMsgWithItemName).showsItemNamePath(showsItemManeMapth))
            .get();

    return ExceptionUtil.getMessageList(ex, Locale.ENGLISH, true).get(0);
  }

  /**
   * It doesn't seem important
   *     whether the validator is {@code @NotNull} or other validators,
   *     but SingleLayer test is executed just in case.
   */
  @Test
  public void itemNameAndItemNamePathTest_List() {
    String msg = null;

    String MSG = " must not be null.";

    List<TargetCls> targetList = List.of(new TargetCls[] {new TargetCls("a"), new TargetCls(null)});
    List<TargetClsInkc> targetInkcList =
        List.of(new TargetClsInkc[] {new TargetClsInkc("a"), new TargetClsInkc(null)});

    // Single layer
    // single collection

    // no itemName
    msg = validateCollection(new SingleList(targetList), false, false);
    Assertions.assertEquals("must not be null", msg);
    // itemName
    msg = validateCollection(new SingleList(targetList), true, false);
    Assertions.assertEquals("'target field'" + MSG, msg);
    // itemNamePath
    msg = validateCollection(new SingleList(targetList), true, true);
    Assertions.assertEquals(
        "'target field' at element 2 contained by 'target list'" + MSG, msg);
    // itemNamePath + itemNameKeyClass
    msg = validateCollection(new SingleListInkc(targetInkcList), true, true);
    Assertions.assertEquals("'ItemNameKeyClass considered field' at element 2"
        + " contained by 'target list'" + MSG, msg);
    // itemNamePath + ItemContiner(root)
    msg = validateCollection(new SingleListConRoot(targetList), true, true);
    Assertions.assertEquals("'ItemContainer considered field' "
        + "at element 2 contained by 'target list'" + MSG, msg);
    // itemNamePath + ItemContiner(child)
    SingleListConChild.Child child =
        new SingleListConChild.Child(List.of(List.of(new SingleListConChild.GrandChild(null))));
    String expected = "'sample field in grandChild' at element 1 > element 1 "
        + "contained by 'grand child list list'" + MSG;
    msg = validateCollection(child, true, true);
    Assertions.assertEquals(expected, msg);

    // Multiple layer
    // Multiple collection (propertyPath: strListList[0].<list element>[0].<list element>)
    // (Multiple layer / single collection, single layer / multiple collection will be tested
    // when needed)

    // no itemName
    MulList mulListList =
        new MulList(new MulList.Child(List.of(List.of(new MulList.GrandChild(null)))));
    msg = validateCollection(mulListList, false, false);
    Assertions.assertEquals("must not be null", msg);
    // itemName
    msg = validateCollection(mulListList, true, false);
    Assertions.assertEquals("'sample field in grandChild'" + MSG, msg);
    // itemNamePath
    expected = "'sample field in grandChild' at 'child field' "
        + "> element 1 > element 1 contained by 'grand child field'" + MSG;
    msg = validateCollection(mulListList, true, true);
    Assertions.assertEquals(expected, msg);
    // itemNamePath + itemNameKeyClass
    MulListInkc mulListListInkc =
        new MulListInkc(new MulListInkc.Child(List.of(List.of(new MulListInkc.GrandChild(null)))));
    expected = "'ItemNameKeyClass considered field' at 'child field' > element 1 > element 1"
        + " contained by 'grand child field'" + MSG;
    msg = validateCollection(mulListListInkc, true, true);
    Assertions.assertEquals(expected, msg);
    // itemNamePath + ItemContiner(root)
    MulListConRoot mulListListConRoot = new MulListConRoot(
        new MulListConRoot.Child(List.of(List.of(new MulListConRoot.GrandChild(null)))));
    expected = "'ItemContainer considered field' at 'child field' "
        + "> element 1 > element 1 contained by 'grand child list list'" + MSG;
    msg = validateCollection(mulListListConRoot, true, true);
    Assertions.assertEquals(expected, msg);
    // itemNamePath + ItemContiner(child)
    MulListConChild mulListListConChild = new MulListConChild(new MulListConChild.Child(
        new MulListConChild.GrandChild(List.of(List.of(new MulListConChild.TheChild(null))))));
    expected = "'ItemContainer considered field' at 'child field' "
        + "> element 1 > element 1 contained by 'grand child list list'" + MSG;
    msg = validateCollection(mulListListConChild, true, true);
  }

  public static record TargetCls(@NotNull String field) {
  }
  @ItemNameKeyClass("itemNameKeyClass")
  public static record TargetClsInkc(@NotNull String field) {
  }
  public static record SingleList(@Valid List<TargetCls> targetList) {
  }
  public static record SingleListInkc(@Valid List<TargetClsInkc> targetList) {
  }
  public static record SingleListConRoot(@Valid List<TargetCls> targetList)
      implements ItemContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[] {new Item("targetList.field").itemNameKey("icField")};
    }
  }
  public static record SingleListConChild(@Valid Child child) {

    private static record Child(List<List<@Valid GrandChild>> grandChildListList)
        implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("field").itemNameKey("icField")};
      }
    }
    private static record GrandChild(@NotNull String field) {

    }
  }

  public static record MulList(@Valid Child child) {
    private static record Child(List<List<@Valid GrandChild>> grandChild) {
    }
    private static record GrandChild(@NotNull String field) {
    }
  }
  public static record MulListInkc(@Valid Child child) {
    private static record Child(List<List<@Valid GrandChild>> grandChild) {
    }
    @ItemNameKeyClass("itemNameKeyClass")
    private static record GrandChild(@NotNull String field) {
    }
  }
  public static record MulListConRoot(@Valid Child child) implements ItemContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[] {new Item("child.grandChildListList.field").itemNameKey("icField")};
    }

    private static record Child(List<List<@Valid GrandChild>> grandChildListList) {
    }
    private static record GrandChild(@NotNull String field) {
    }
  }
  public static record MulListConChild(@Valid Child child) {

    private static record Child(@Valid GrandChild grandChild) implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("grandChildListList.field").itemNameKey("icField")};
      }
    }
    private static record GrandChild(List<List<@Valid TheChild>> theChildListList) {
    }
    private static record TheChild(@NotNull String field) {

    }
  }

  //
  //
  // // list : propertyPath=targetList[0].<list element>[0].field
  // // set : propertyPath=targetList[].<iterable element>[].field
  // // map key: propertyPath=targetList[].<map value><K>[TargetCls[field=null]].field
  // // map val: propertyPath=targetList[key1].<map value>[key2].field
  //
  // public static record MulListList(List<List<@Valid TargetCls>> targetList) {
  // }
  // public static record MulSetSet(Set<Set<@Valid TargetCls>> targetList) {
  // }
  // public static record MulMapMapKey(Map<String, Map<@Valid TargetCls, String>> targetList) {
  // }
  // public static record MulMapMapVal(Map<String, Map<String, @Valid TargetCls>> targetList) {
  // }
  //
  //
  // @ItemNameKeyClass("StringMulListList")
  // public static record StringMulListListInkc(@Valid Child child) {
  // private static record Child(@Valid GrandChild grandChild) {
  // }
  // @ItemNameKeyClass("itemNameKeyClass")
  // private static record GrandChild(List<List<@Pattern(regexp = "[1-9]*") String>> strListList) {
  // }
  // }
  // // public static class StringMulListListConRoot extends StringMulListList implements
  // ItemContainer
  // // {
  // // public StringMulListListConRoot(@SuppressWarnings("exports") StringMulListList.Child child)
  // {
  // // this.child = child;
  // // }
  // //
  // // @Override
  // // public Item[] customizedItems() {
  // // return new Item[] {new Item("child.grandChild.strListList").itemNameKey("icStrListList")};
  // // }
  // // }
  // // public static class StringMulListListConChild {
  // // @Valid
  // // protected Child child = new Child(new GrandChild(List.of(List.of("1", "a"))));
  // //
  // // private static record Child(@Valid GrandChild grandChild) implements ItemContainer {
  // // @Override
  // // public Item[] customizedItems() {
  // // return new Item[] {new Item("grandChild.strListList").itemNameKey("icStrListList")};
  // // }
  // // }
  // // private static record GrandChild(List<List<@Pattern(regexp = "[1-9]*") String>> strListList)
  // {
  // // }
  // // }
  //
  // /**
  // * It doesn't seem important
  // * whether the validator is {@code @NotNull} or other validators,
  // * but SingleLayer test is executed just in case.
  // */
  // @Test
  // public void itemNameAndItemNamePathTest_SingleLayer_OtherThanList_NotNullValidation() {
  // String msg = null;
  //
  // // Set is null (propertyPath: integerSet)
  // msg = validateCollection(new ColFieldOfBasicNotNull.IntegerSet(null), true, false);
  // Assertions.assertEquals("'integer set' must not be null.", msg);
  // // element of List is null (propertyPath: integerSet[].<iterable element>)
  // Set<Integer> intSet = new HashSet<>();
  // intSet.add(1);
  // intSet.add(null);
  // msg = validateCollection(new ColFieldOfBasicNotNull.IntegerSet(intSet), true, false);
  // Assertions.assertEquals("One of 'integer set' must not be null.", msg);
  //
  // // Map is null (propertyPath: strDateMap)
  // msg = validateCollection(new ColFieldOfBasicNotNull.StringDateMap(null), true, false);
  // Assertions.assertEquals("'string date map' must not be null.", msg);
  // // element of Map key is null (propertyPath: strDateMap<K>[].<map key>)
  // Map<String, LocalDate> strDateMap = new HashMap<>();
  // strDateMap.put(null, LocalDate.now());
  // msg = validateCollection(new ColFieldOfBasicNotNull.StringDateMap(strDateMap), true, false);
  // Assertions.assertEquals("One of the key item of 'string date map' must not be null.", msg);
  // // element of Map value is null (propertyPath: strDateMap[test].<map value>)
  // strDateMap.clear();
  // strDateMap.put("testKey", null);
  // msg = validateCollection(new ColFieldOfBasicNotNull.StringDateMap(strDateMap), true, false);
  // Assertions.assertEquals("Value of 'string date map' with key [testKey] must not be null.",
  // msg);
  //
  // // array is null (propertyPath: blArray)
  // msg = validateCollection(new ColFieldOfBasicNotNull.BooleanArray(null), true, false);
  // Assertions.assertEquals("'boolean array' must not be null.", msg);
  // // element of array cannot be validated.
  // Assertions.assertEquals(0, validator
  // .validate(new ColFieldOfBasicNotNull.BooleanArray(new Boolean[] {true, null})).size());
  // }
  //
  // public static class ColFieldOfBasicNotNull {
  // public static record IntegerSet(@NotNull Set<@NotNull Integer> intSet) {
  // }
  // public static record StringDateMap(
  // @NotNull Map<@NotNull String, @NotNull LocalDate> strDateMap) {
  // }
  // public static record BooleanArray(@NotNull Boolean @NotNull [] blArray) {
  // }
  // }
  //
  // @Test
  // public void itemNameAndItemNamePathTest_SingleLayer_NotNonNullValidation() {
  // String msg = null;
  //
  // // element of Set is not min (propertyPath: intSet[].<iterable element>)
  // Set<Integer> intSet = new HashSet<>();
  // intSet.add(5);
  // intSet.add(2);
  // msg = validateCollection(new ColFieldOfBasicOtherThanNotNull.IntegerSet(intSet), true, false);
  // Assertions.assertEquals("One of 'integer set' must be greater than or equal to 3. (input: 2)",
  // msg);
  //
  // // element of Map key is not regexp (propertyPath: strDateMap<K>[a].<map key>)
  // Map<String, LocalDate> strDateMap = new HashMap<>();
  // strDateMap.put("a", LocalDate.of(2101, 1, 1));
  // msg = validateCollection(new ColFieldOfBasicOtherThanNotNull.StringDateMap(strDateMap), true,
  // false);
  // Assertions.assertEquals(
  // "One of the key item of 'string date map' must match \"[1-9]*\". (input: a)", msg);
  //
  // // element of Map value is null (propertyPath: strDateMap[1].<map value>)
  // strDateMap.clear();
  // strDateMap.put("1", LocalDate.of(2001, 1, 1));
  // msg = validateCollection(new ColFieldOfBasicOtherThanNotNull.StringDateMap(strDateMap), true,
  // false);
  // Assertions.assertEquals(
  // "Value of 'string date map' with key [1] must be a future date. (input: 2001-01-01)", msg);
  //
  // // array is null (propertyPath: blArray)
  // msg = validateCollection(new ColFieldOfBasicOtherThanNotNull.BooleanArray(null), true, false);
  // Assertions.assertEquals("'boolean array' must not be null.", msg);
  // // element of array cannot be validated.
  // Assertions.assertEquals(0,
  // validator
  // .validate(new ColFieldOfBasicOtherThanNotNull.BooleanArray(new Boolean[] {true, null}))
  // .size());
  // }
  //
  // public static class ColFieldOfBasicOtherThanNotNull {
  // public static record IntegerSet(Set<@Min(3) Integer> intSet) {
  // }
  // public static record StringDateMap(
  // Map<@Pattern(regexp = "[1-9]*") String, @Future LocalDate> strDateMap) {
  // }
  // public static record BooleanArray(@NotNull Boolean @NotNull [] blArray) {
  // }
  // }
  //
  // @Test
  // public void itemNameAndItemNamePathTest_MultipleLayers_otherThanList() {
  // String msg = null;
  // // element of Set in Set is null
  // // (propertyPath: intSetSet[].<iterable element>[].<iterable element>)
  // Set<Set<Integer>> intSetSet = new HashSet<>();
  // Set<Integer> intSet = new HashSet<>();
  // intSet.add(1);
  // intSet.add(null);
  // intSetSet.add(intSet);
  // msg = validateCollection(new ColFieldOfMulLayerBasic.IntegerSetSet(intSetSet), true, false);
  // Assertions.assertEquals("One of 'data' > one of 'integer set set' must not be null.", msg);
  //
  // // element of Map key in Map key is null
  // // (propertyPath: strDateMapMap<K>[{null=2026-03-19}].<map key><K>[].<map key>)
  // Map<Map<String, LocalDate>, Map<String, LocalDate>> strDateMapMap = new HashMap<>();
  // Map<String, LocalDate> nonnullStrDateMap = new HashMap<>();
  // nonnullStrDateMap.put("a", LocalDate.of(2001, 1, 1));
  // Map<String, LocalDate> strDateMap = new HashMap<>();
  // strDateMap.put(null, LocalDate.now());
  // strDateMapMap.put(strDateMap, nonnullStrDateMap);
  // msg = validateCollection(new ColFieldOfMulLayerBasic.StringDateMapMap(strDateMapMap), true,
  // false);
  // Assertions.assertEquals(
  // "One of the key item of 'data' > one of the key item of 'string date map map' must not be
  // null.",
  // msg);
  // // element of Map value in Map value is null
  // // (propertyPath: strDateMapMap[{a=2026-03-19}].<map value>[testKey].<map value>)
  // strDateMap.clear();
  // strDateMap.put("testKey", null);
  // strDateMapMap.clear();
  // strDateMapMap.put(nonnullStrDateMap, strDateMap);
  // msg = validateCollection(new ColFieldOfMulLayerBasic.StringDateMapMap(strDateMapMap), true,
  // false);
  // Assertions.assertEquals(
  // "Value of 'data' with key [{a=2001-01-01}] > value of 'string date map map' with key [testKey]
  // "
  // + "must not be null.",
  // msg);
  //
  // // element of array in array cannot be validated.
  // Assertions.assertEquals(0, validator.validate(
  // new ColFieldOfMulLayerBasic.BooleaArraynArray(new Boolean[][] {new Boolean[] {true, null}}))
  // .size());
  //
  // // mix
  // Map<String, @NotNull Integer> map = new HashMap<>();
  // map.put("testKey", null);
  // Set<Map<String, @NotNull Integer>> setMap = Set.of(map);
  // List<Set<Map<String, @NotNull Integer>>> listSetMap = List.of(setMap);
  // msg = validateCollection(new ColFieldOfMulLayerBasic.ListSetMap(listSetMap), true, false);
  // Assertions.assertEquals(
  // "'data' 1 > one of 'data' > value of 'list set map' with key [testKey] must not be null.",
  // msg);
  // }
  //
  // public static class ColFieldOfMulLayerBasic {
  // public static record IntegerSetSet(@NotNull Set<@NotNull Set<@NotNull Integer>> intSetSet) {
  // }
  // public static record StringDateMapMap(
  // @NotNull Map<@NotNull Map<@NotNull String, @NotNull LocalDate>, Map<@NotNull String, @NotNull
  // LocalDate>> strDateMapMap) {
  // }
  // public static record BooleaArraynArray(@NotNull Boolean @NotNull [] @NotNull [] blArrayArray) {
  // }
  // public static record ListSetMap(List<Set<Map<String, @NotNull Integer>>> listSetMap) {
  // }
  // }
  //
  // @Test
  // public void itemNameAndItemNamePathTestWithConstraintViolationBean() {
  // // Looks like it's tested at ConstraintViolationBeanTest.
  // }
}
