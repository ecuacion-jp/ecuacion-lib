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

public class ExceptionUtilTest_getMessageList_2_CollectionValues_BasicObjects {

  private String testUncommentNeeded;
  
//
//  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
//
//  @BeforeAll
//  public static void before() {
//    PropertyFileUtil.addResourceBundlePostfix("lib-core-test");
//  }
//
//  private String validateCollection(Object object, boolean isMsgWithItemName,
//      boolean showsItemManeMapth) {
//    ConstraintViolationExceptionWithParameters ex =
//        (ConstraintViolationExceptionWithParameters) ValidationUtil
//            .validateThenReturn(object, ValidationUtil.messageParameters()
//                .isMessageWithItemName(isMsgWithItemName).showsItemNamePath(showsItemManeMapth))
//            .get();
//
//    return ExceptionUtil.getMessageList(ex, Locale.ENGLISH, true).get(0);
//  }
//
//  /**
//   * It doesn't seem important
//   *     whether the validator is {@code @NotNull} or other validators,
//   *     but SingleLayer test is executed just in case.
//   */
//  @Test
//  public void itemNameAndItemNamePathTest_List() {
//    String msg = null;
//    List<String> strList = null;
//
//    // Common
//
//    // Container (besides collection) needed to validate values.
//    @Valid
//    @NotNull
//    List<@NotNull String> lst = new ArrayList<>();
//    lst.add(null);
//    Assertions.assertEquals(0, validator.validate(lst).size());
//
//    // not null
//
//    // List is null (propertyPath: stringList)
//    msg = validateCollection(new StringListNotNull(null), true, false);
//    Assertions.assertEquals("'string list' must not be null.", msg);
//    // element of List is null (propertyPath: stringList[1].<list element>)
//    strList = new ArrayList<>(List.of("a"));
//    strList.add(null);
//    msg = validateCollection(new StringListNotNull(strList), true, false);
//    Assertions.assertEquals("Element 2 contained by 'string list' must not be null.", msg);
//
//    String MSG = " must match \"[1-9]*\". (input: a)";
//
//    // Single layer
//    // single collection (propertyPath: strList[1].<list element>)
//
//    // no itemName
//    msg = validateCollection(new StringList(List.of("1", "a")), false, false);
//    Assertions.assertEquals("must match \"[1-9]*\"", msg);
//    // itemName
//    msg = validateCollection(new StringList(List.of("1", "a")), true, false);
//    Assertions.assertEquals("Element 2 contained by 'string list'" + MSG, msg);
//    // itemNamePath
//    msg = validateCollection(new StringList(List.of("1", "a")), true, true);
//    Assertions.assertEquals("Element 2 contained by 'string list'" + MSG, msg);
//    // itemNamePath + itemNameKeyClass
//    msg = validateCollection(new StringListInkc(List.of("1", "a")), true, true);
//    Assertions.assertEquals(
//        "Element 2 contained by 'ItemNameKeyClass considered string list'" + MSG, msg);
//    // itemNamePath + ItemContiner(root)
//    msg = validateCollection(new StringListConRoot(List.of("1", "a")), true, true);
//    Assertions.assertEquals("Element 2 contained by 'ItemContainer considered string list'" + MSG,
//        msg);
//    // itemNamePath + ItemContiner(child)
//    StringListConChild.Child child = new StringListConChild.Child(List.of("1", "a"));
//    String expected =
//        "Element 2 contained by 'ItemContainer considered string list' at 'child field'" + MSG;
//    msg = validateCollection(new StringListConChild(child), true, true);
//    Assertions.assertEquals(expected, msg);
//
//    // Multiple layer
//    // Multiple collection (propertyPath: strListList[0].<list element>[0].<list element>)
//    // (Multiple layer / single collection, single layer / multiple collection will be tested
//    // when needed)
//
//    // no itemName
//    StringMulListList strMulListList = new StringMulListList();
//    msg = validateCollection(strMulListList, false, false);
//    Assertions.assertEquals("must match \"[1-9]*\"", msg);
//    // itemName
//    msg = validateCollection(strMulListList, true, false);
//    Assertions.assertEquals(
//        "Element 1 > element 2 contained by 'grand child string list list'" + MSG, msg);
//    // // itemNamePath
//    expected = "Element 1 > element 2 contained by 'grand child string list list' "
//        + "at 'child field' > 'grand child field'" + MSG;
//    msg = validateCollection(strMulListList, true, true);
//    Assertions.assertEquals(expected, msg);
//    // itemNamePath + itemNameKeyClass
//    StringMulListListInkc strMulListListInkc =
//        new StringMulListListInkc(new StringMulListListInkc.Child(
//            new StringMulListListInkc.GrandChild(List.of(List.of("1", "a")))));
//    expected = "Element 1 > element 2 contained by "
//        + "'ItemNameKeyClass considered string list list' at 'child field' > 'grand child field'"
//        + MSG;
//    msg = validateCollection(strMulListListInkc, true, true);
//    Assertions.assertEquals(expected, msg);
//    // itemNamePath + ItemContiner(root)
//    StringMulListListConRoot strMulListListConRoot = new StringMulListListConRoot(
//        new StringMulListList.Child(new StringMulListList.GrandChild(List.of(List.of("1", "a")))));
//    expected = "Element 1 > element 2 contained by 'ItemContainer considered string list list' "
//        + "at 'child field' > 'grand child field'" + MSG;
//    msg = validateCollection(strMulListListConRoot, true, true);
//    Assertions.assertEquals(expected, msg);
//    // itemNamePath + ItemContiner(child)
//    StringMulListListConChild strMulListListConChild = new StringMulListListConChild();
//    expected = "Element 1 > element 2 contained by 'ItemContainer considered string list list' "
//        + "at 'child field' > 'grand child field'" + MSG;
//    msg = validateCollection(strMulListListConChild, true, true);
//    Assertions.assertEquals(expected, msg);
//  }
//
//  public static record StringListNotNull(@NotNull List<@NotNull String> strList) {
//  }
//  public static record StringList(List<@Pattern(regexp = "[1-9]*") String> strList) {
//  }
//  @ItemNameKeyClass("itemNameKeyClass")
//  public static record StringListInkc(List<@Pattern(regexp = "[1-9]*") String> strList) {
//  }
//  public static record StringListConRoot(List<@Pattern(regexp = "[1-9]*") String> strList)
//      implements ItemContainer {
//    @Override
//    public Item[] customizedItems() {
//      return new Item[] {new Item("strList").itemNameKey("icStrList")};
//    }
//  }
//  public static record StringListConChild(@Valid Child child) {
//
//    private static record Child(List<@Pattern(regexp = "[1-9]*") String> strList)
//        implements ItemContainer {
//      @Override
//      public Item[] customizedItems() {
//        return new Item[] {new Item("strList").itemNameKey("icStrList")};
//      }
//    }
//  }
//
//  public static class StringMulListList {
//    @Valid
//    protected Child child = new Child(new StringMulListList.GrandChild(List.of(List.of("1", "a"))));
//
//    private static record Child(@Valid GrandChild grandChild) {
//    }
//    private static record GrandChild(List<List<@Pattern(regexp = "[1-9]*") String>> strListList) {
//    }
//  }
//  @ItemNameKeyClass("StringMulListList")
//  public static record StringMulListListInkc(@Valid Child child) {
//    private static record Child(@Valid GrandChild grandChild) {
//    }
//    @ItemNameKeyClass("itemNameKeyClass")
//    private static record GrandChild(List<List<@Pattern(regexp = "[1-9]*") String>> strListList) {
//    }
//  }
//  public static class StringMulListListConRoot extends StringMulListList implements ItemContainer {
//    public StringMulListListConRoot(@SuppressWarnings("exports") StringMulListList.Child child) {
//      this.child = child;
//    }
//
//    @Override
//    public Item[] customizedItems() {
//      return new Item[] {new Item("child.grandChild.strListList").itemNameKey("icStrListList")};
//    }
//  }
//  public static class StringMulListListConChild {
//    @Valid
//    protected Child child = new Child(new GrandChild(List.of(List.of("1", "a"))));
//
//    private static record Child(@Valid GrandChild grandChild) implements ItemContainer {
//      @Override
//      public Item[] customizedItems() {
//        return new Item[] {new Item("grandChild.strListList").itemNameKey("icStrListList")};
//      }
//    }
//    private static record GrandChild(List<List<@Pattern(regexp = "[1-9]*") String>> strListList) {
//    }
//  }
//
//  /**
//   * It doesn't seem important
//   *     whether the validator is {@code @NotNull} or other validators,
//   *     but SingleLayer test is executed just in case.
//   */
//  @Test
//  public void itemNameAndItemNamePathTest_SingleLayer_OtherThanList_NotNullValidation() {
//    String msg = null;
//
//    // Set is null (propertyPath: integerSet)
//    msg = validateCollection(new ColFieldOfBasicNotNull.IntegerSet(null), true, false);
//    Assertions.assertEquals("'integer set' must not be null.", msg);
//    // element of List is null (propertyPath: integerSet[].<iterable element>)
//    Set<Integer> intSet = new HashSet<>();
//    intSet.add(1);
//    intSet.add(null);
//    msg = validateCollection(new ColFieldOfBasicNotNull.IntegerSet(intSet), true, false);
//    Assertions.assertEquals("Some element contained by 'integer set' must not be null.", msg);
//
//    // Map is null (propertyPath: strDateMap)
//    msg = validateCollection(new ColFieldOfBasicNotNull.StringDateMap(null), true, false);
//    Assertions.assertEquals("'string date map' must not be null.", msg);
//    // element of Map key is null (propertyPath: strDateMap<K>[].<map key>)
//    Map<String, LocalDate> strDateMap = new HashMap<>();
//    strDateMap.put(null, LocalDate.now());
//    msg = validateCollection(new ColFieldOfBasicNotNull.StringDateMap(strDateMap), true, false);
//    Assertions.assertEquals(
//        "Some key element of key-value data contained " + "by 'string date map' must not be null.",
//        msg);
//    // element of Map value is null (propertyPath: strDateMap[test].<map value>)
//    strDateMap.clear();
//    strDateMap.put("testKey", null);
//    msg = validateCollection(new ColFieldOfBasicNotNull.StringDateMap(strDateMap), true, false);
//    Assertions.assertEquals(
//        "Element with key [testKey] contained by 'string date map' must not be null.", msg);
//
//    // array is null (propertyPath: blArray)
//    msg = validateCollection(new ColFieldOfBasicNotNull.BooleanArray(null), true, false);
//    Assertions.assertEquals("'boolean array' must not be null.", msg);
//    // element of array cannot be validated.
//    Assertions.assertEquals(0, validator
//        .validate(new ColFieldOfBasicNotNull.BooleanArray(new Boolean[] {true, null})).size());
//  }
//
//  public static class ColFieldOfBasicNotNull {
//    public static record IntegerSet(@NotNull Set<@NotNull Integer> intSet) {
//    }
//    public static record StringDateMap(
//        @NotNull Map<@NotNull String, @NotNull LocalDate> strDateMap) {
//    }
//    public static record BooleanArray(@NotNull Boolean @NotNull [] blArray) {
//    }
//  }
//
//  @Test
//  public void itemNameAndItemNamePathTest_SingleLayer_NotNonNullValidation() {
//    String msg = null;
//
//    // element of Set is not min (propertyPath: intSet[].<iterable element>)
//    Set<Integer> intSet = new HashSet<>();
//    intSet.add(5);
//    intSet.add(2);
//    msg = validateCollection(new ColFieldOfBasicOtherThanNotNull.IntegerSet(intSet), true, false);
//    Assertions.assertEquals(
//        "Some element contained by 'integer set' must be greater than or equal to 3. (input: 2)",
//        msg);
//
//    // element of Map key is not regexp (propertyPath: strDateMap<K>[a].<map key>)
//    Map<String, LocalDate> strDateMap = new HashMap<>();
//    strDateMap.put("a", LocalDate.of(2101, 1, 1));
//    msg = validateCollection(new ColFieldOfBasicOtherThanNotNull.StringDateMap(strDateMap), true,
//        false);
//    Assertions.assertEquals("Some key element of key-value data contained by 'string date map' "
//        + "must match \"[1-9]*\". (input: a)", msg);
//
//    // element of Map value is null (propertyPath: strDateMap[1].<map value>)
//    strDateMap.clear();
//    strDateMap.put("1", LocalDate.of(2001, 1, 1));
//    msg = validateCollection(new ColFieldOfBasicOtherThanNotNull.StringDateMap(strDateMap), true,
//        false);
//    Assertions.assertEquals("Element with key [1] contained by 'string date map' "
//        + "must be a future date. (input: 2001-01-01)", msg);
//
//    // array is null (propertyPath: blArray)
//    msg = validateCollection(new ColFieldOfBasicOtherThanNotNull.BooleanArray(null), true, false);
//    Assertions.assertEquals("'boolean array' must not be null.", msg);
//    // element of array cannot be validated.
//    Assertions.assertEquals(0,
//        validator
//            .validate(new ColFieldOfBasicOtherThanNotNull.BooleanArray(new Boolean[] {true, null}))
//            .size());
//  }
//
//  public static class ColFieldOfBasicOtherThanNotNull {
//    public static record IntegerSet(Set<@Min(3) Integer> intSet) {
//    }
//    public static record StringDateMap(
//        Map<@Pattern(regexp = "[1-9]*") String, @Future LocalDate> strDateMap) {
//    }
//    public static record BooleanArray(@NotNull Boolean @NotNull [] blArray) {
//    }
//  }
//
//  @Test
//  public void itemNameAndItemNamePathTest_MultipleLayers_otherThanList() {
//    String msg = null;
//    // element of Set in Set is null
//    // (propertyPath: intSetSet[].<iterable element>[].<iterable element>)
//    Set<Set<Integer>> intSetSet = new HashSet<>();
//    Set<Integer> intSet = new HashSet<>();
//    intSet.add(1);
//    intSet.add(null);
//    intSetSet.add(intSet);
//    msg = validateCollection(new ColFieldOfMulLayerBasic.IntegerSetSet(intSetSet), true, false);
//    Assertions.assertEquals(
//        "Some element > some element contained by 'integer set set' must not be null.", msg);
//
//    // element of Map key in Map key is null
//    // (propertyPath: strDateMapMap<K>[{null=2026-03-19}].<map key><K>[].<map key>)
//    Map<Map<String, LocalDate>, Map<String, LocalDate>> strDateMapMap = new HashMap<>();
//    Map<String, LocalDate> nonnullStrDateMap = new HashMap<>();
//    nonnullStrDateMap.put("a", LocalDate.of(2001, 1, 1));
//    Map<String, LocalDate> strDateMap = new HashMap<>();
//    strDateMap.put(null, LocalDate.now());
//    strDateMapMap.put(strDateMap, nonnullStrDateMap);
//    msg = validateCollection(new ColFieldOfMulLayerBasic.StringDateMapMap(strDateMapMap), true,
//        false);
//    Assertions
//        .assertEquals("Some key element of key-value data > some key element of key-value data "
//            + "contained by 'string date map map' must not be null.", msg);
//    // element of Map value in Map value is null
//    // (propertyPath: strDateMapMap[{a=2026-03-19}].<map value>[testKey].<map value>)
//    strDateMap.clear();
//    strDateMap.put("testKey", null);
//    strDateMapMap.clear();
//    strDateMapMap.put(nonnullStrDateMap, strDateMap);
//    msg = validateCollection(new ColFieldOfMulLayerBasic.StringDateMapMap(strDateMapMap), true,
//        false);
//    Assertions.assertEquals("Element with key [{a=2001-01-01}] > element with key [testKey] "
//        + "contained by 'string date map map' must not be null.", msg);
//
//    // element of array in array cannot be validated.
//    Assertions.assertEquals(0, validator.validate(
//        new ColFieldOfMulLayerBasic.BooleaArraynArray(new Boolean[][] {new Boolean[] {true, null}}))
//        .size());
//
//    // mix
//    Map<String, @NotNull Integer> map = new HashMap<>();
//    map.put("testKey", null);
//    Set<Map<String, @NotNull Integer>> setMap = Set.of(map);
//    List<Set<Map<String, @NotNull Integer>>> listSetMap = List.of(setMap);
//    msg = validateCollection(new ColFieldOfMulLayerBasic.ListSetMap(listSetMap), true, false);
//    Assertions.assertEquals("Element 1 > some element > element with key [testKey] "
//        + "contained by 'list set map' must not be null.", msg);
//  }
//
//  public static class ColFieldOfMulLayerBasic {
//    public static record IntegerSetSet(@NotNull Set<@NotNull Set<@NotNull Integer>> intSetSet) {
//    }
//    public static record StringDateMapMap(
//        @NotNull Map<@NotNull Map<@NotNull String, @NotNull LocalDate>, Map<@NotNull String, @NotNull LocalDate>> strDateMapMap) {
//    }
//    public static record BooleaArraynArray(@NotNull Boolean @NotNull [] @NotNull [] blArrayArray) {
//    }
//    public static record ListSetMap(List<Set<Map<String, @NotNull Integer>>> listSetMap) {
//    }
//  }
//
//  @Test
//  public void itemNameAndItemNamePathTestWithConstraintViolationBean() {
//    // Looks like it's tested at ConstraintViolationBeanTest.
//  }
}
