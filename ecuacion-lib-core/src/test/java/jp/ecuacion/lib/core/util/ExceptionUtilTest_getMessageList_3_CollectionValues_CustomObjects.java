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
import jakarta.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.lib.core.violation.Violations.MessageParameters;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
public class ExceptionUtilTest_getMessageList_3_CollectionValues_CustomObjects {

  @BeforeAll
  public static void before() {
    PropertiesFileUtil.addResourceBundlePostfix("lib-core-test");
  }

  private String validateCollection(Object object, boolean isMsgWithItemName,
      boolean showsItemManeMapth) {
    MessageParameters msgParams = ValidationUtil.messageParameters()
        .isMessageWithItemName(isMsgWithItemName).showsItemNamePath(showsItemManeMapth);
    Violations violations = ValidationUtil.validateThenReturn(object, msgParams);
    return ExceptionUtil.getMessageList(violations, Locale.ENGLISH, true).get(0);
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
    Assertions.assertEquals("'target field' at element 2 contained by 'target list'" + MSG, msg);
    // itemNamePath + itemNameKeyClass
    msg = validateCollection(new SingleListInkc(targetInkcList), true, true);
    Assertions.assertEquals(
        "'ItemNameKeyClass considered field' at element 2" + " contained by 'target list'" + MSG,
        msg);
    // itemNamePath + ItemContiner(root)
    msg = validateCollection(new SingleListConRoot(targetList), true, true);
    Assertions.assertEquals(
        "'ItemContainer considered field' " + "at element 2 contained by 'target list'" + MSG, msg);
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

  public static record TargetCls(@NotNull @Nullable String field) {
  }
  @ItemNameKeyClass("itemNameKeyClass")
  public static record TargetClsInkc(@NotNull @Nullable String field) {
  }
  public static record SingleList(@Valid List<TargetCls> targetList) {
  }
  public static record SingleListInkc(@Valid List<TargetClsInkc> targetList) {
  }
  public static record SingleListConRoot(@Valid List<TargetCls> targetList)
      implements ItemContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[] {new Item("targetList[].field").itemNameKey("icField")};
    }
  }
  public static record SingleListConChild(@Valid Child child) {

    private static record Child(List<List<@Valid @NonNull GrandChild>> grandChildListList)
        implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("field").itemNameKey("icField")};
      }
    }
    private static record GrandChild(@NotNull @Nullable String field) {

    }
  }

  public static record MulList(@Valid Child child) {
    private static record Child(List<List<@Valid @Nullable GrandChild>> grandChild) {
    }
    private static record GrandChild(@NotNull @Nullable String field) {
    }
  }
  public static record MulListInkc(@Valid Child child) {
    private static record Child(List<List<@Valid @NonNull GrandChild>> grandChild) {
    }
    @ItemNameKeyClass("itemNameKeyClass")
    private static record GrandChild(@NotNull @Nullable String field) {
    }
  }
  public static record MulListConRoot(@Valid Child child) implements ItemContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[] {new Item("child.grandChildListList[][].field").itemNameKey("icField")};
    }

    private static record Child(List<List<@Valid @NonNull GrandChild>> grandChildListList) {
    }
    private static record GrandChild(@NotNull @Nullable String field) {
    }
  }

  @Test
  public void itemNameAndItemNamePathTest_Set() {
    String msg = null;
    String MSG = " must not be null.";

    Set<TargetCls> targetSet = new HashSet<>();
    targetSet.add(new TargetCls("a"));
    targetSet.add(new TargetCls(null));

    // no itemName
    msg = validateCollection(new SingleSet(targetSet), false, false);
    Assertions.assertEquals("must not be null", msg);
    // itemName
    msg = validateCollection(new SingleSet(targetSet), true, false);
    Assertions.assertEquals("'target field'" + MSG, msg);
    // itemNamePath
    msg = validateCollection(new SingleSet(targetSet), true, true);
    Assertions.assertEquals("'target field' at some element contained by 'target set'" + MSG, msg);
  }

  @Test
  public void itemNameAndItemNamePathTest_MapValue() {
    String msg = null;
    String MSG = " must not be null.";

    Map<String, TargetCls> targetMapValue = new HashMap<>();
    targetMapValue.put("key1", new TargetCls(null));

    // no itemName
    msg = validateCollection(new SingleMapValue(targetMapValue), false, false);
    Assertions.assertEquals("must not be null", msg);
    // itemName
    msg = validateCollection(new SingleMapValue(targetMapValue), true, false);
    Assertions.assertEquals("'target field'" + MSG, msg);
    // itemNamePath
    msg = validateCollection(new SingleMapValue(targetMapValue), true, true);
    Assertions.assertEquals(
        "'target field' at element with key [key1] contained by 'target map value'" + MSG, msg);
  }

  @Test
  public void itemNameAndItemNamePathTest_Array() {
    String msg = null;
    String MSG = " must not be null.";

    TargetCls[] targetArray = new TargetCls[] {new TargetCls("a"), new TargetCls(null)};

    // no itemName
    msg = validateCollection(new SingleArray(targetArray), false, false);
    Assertions.assertEquals("must not be null", msg);
    // itemName
    msg = validateCollection(new SingleArray(targetArray), true, false);
    Assertions.assertEquals("'target field'" + MSG, msg);
    // itemNamePath
    msg = validateCollection(new SingleArray(targetArray), true, true);
    Assertions.assertEquals("'target field' at element 2 contained by 'target array'" + MSG, msg);
  }

  public static record SingleSet(@Valid Set<TargetCls> targetSet) {
  }
  public static record SingleMapValue(@Valid Map<String, TargetCls> targetMapValue) {
  }
  public static record SingleArray(@Valid TargetCls[] targetArray) {
  }

  public static record MulListConChild(@Valid Child child) {

    private static record Child(@Valid GrandChild grandChild) implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("grandChildListList.field").itemNameKey("icField")};
      }
    }
    private static record GrandChild(List<List<@Valid @NonNull TheChild>> theChildListList) {
    }
    private static record TheChild(@NotNull @Nullable String field) {

    }
  }



  // list : propertyPath=targetList[0].<list element>[0].field
  // set : propertyPath=targetList[].<iterable element>[].field
  // map key: propertyPath=targetList[].<map value><K>[TargetCls[field=null]].field
  // map val: propertyPath=targetList[key1].<map value>[key2].field

  public static record MulListList(List<List<@Valid TargetCls>> targetList) {
  }
  public static record MulSetSet(Set<Set<@Valid TargetCls>> targetList) {
  }
  public static record MulMapMapKey(Map<String, Map<@Valid TargetCls, String>> targetList) {
  }
  public static record MulMapMapVal(Map<String, Map<String, @Valid TargetCls>> targetList) {
  }


  @ItemNameKeyClass("StringMulListList")
  public static record StringMulListListInkc(@Valid Child child) {
    private static record Child(@Valid GrandChild grandChild) {
    }
    @ItemNameKeyClass("itemNameKeyClass")
    private static record GrandChild(List<List<@Pattern(regexp = "[1-9]*") String>> strListList) {
    }
  }
}
