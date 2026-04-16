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
import jakarta.validation.constraints.NotNull;
import java.util.Locale;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.jakartavalidation.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassAlwaysFalse;
import jp.ecuacion.lib.core.jakartavalidation.constraints.MethodAlwaysFalse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ExceptionUtilTest_getMessageList_1_NonCollectionValues {

  @SuppressWarnings("unused")
  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void before() {
    PropertiesFileUtil.addResourceBundlePostfix("lib-core-test");
  }

  private String getMsg(Object obj, boolean isMsgWithItemName, boolean showsItemManeMapth) {
    return ExceptionUtil.getMessageList(
        ValidationUtil.validateThenReturn(obj,
            ValidationUtil.messageParameters().showsItemNamePath(showsItemManeMapth)).get(),
        Locale.ENGLISH, isMsgWithItemName).get(0);
  }

  @Test
  public void itemNameAndItemNamePathTest() {
    String msg = null;
    final String MSG = "must not be null.";

    // normal (no itemName / itemNamePath)
    Assertions.assertEquals("must not be null", getMsg(new SingleLayer(), false, false));

    // Single layer

    // itemName
    Assertions.assertEquals("'test field' " + MSG, getMsg(new SingleLayer(), true, false));
    // itemNamePathWithoutItemName
    // (no itemNaame and itemNamePath added since {0} is needed to show itemNamePath)
    Assertions.assertEquals("must not be null", getMsg(new SingleLayer(), false, true));
    // itemNamePath
    Assertions.assertEquals("'test field' " + MSG, getMsg(new SingleLayer(), true, true));
    // itemNamePath + itemNameKeyClass
    Assertions.assertEquals("'@ItemNameKeyClass considered field' " + MSG,
        getMsg(new SingleLayerInkc(), true, true));
    // itemNamePath + ItemContiner(root)
    Assertions.assertEquals("'ItemContainer considered field' " + MSG,
        getMsg(new SingleLayerConRoot(), true, true));
    // itemNamePath + ItemContiner(child)
    Assertions.assertEquals("'ItemContainer considered field' at 'child field' " + MSG,
        getMsg(new SingleLayerConChild(), true, true));

    // Multiple layer

    // itemName
    Assertions.assertEquals("'sample field in grandChild' " + MSG,
        getMsg(new MultipleLayer(), true, false));
    // itemNamePathWithoutItemName
    // (no itemNaame and itemNamePath added since {0} is needed to show itemNamePath)
    Assertions.assertEquals("must not be null", getMsg(new MultipleLayer(), false, true));
    // itemNamePath
    Assertions.assertEquals(
        "'sample field in grandChild' at 'child field' > 'grand child field' " + MSG,
        getMsg(new MultipleLayer(), true, true));
    // itemNamePath + itemNameKeyClass
    msg = "'@ItemNameKeyClass considered field' at '@ItemNameKeyClass considered child field' "
        + "> '@ItemNameKeyClass considered grandChild field' " + MSG;
    Assertions.assertEquals(msg, getMsg(new MultipleLayerInkc(), true, true));
    // itemNamePath + ItemContiner(root)
    msg = "'ItemContainer considered field' at 'ItemContainer considered child field'"
        + " > 'ItemContainer considered grand child field' " + MSG;
    Assertions.assertEquals(msg, getMsg(new MultipleLayerConRoot(), true, true));
    // itemNamePath + ItemContiner(child)
    msg = "'ItemContainer considered field' at 'child field'"
        + " > 'ItemContainer considered grand child field' " + MSG;
    Assertions.assertEquals(msg, getMsg(new MultipleLayerConChild(), true, true));
  }

  public static class SingleLayer {
    @NotNull
    private String field;
  }
  @ItemNameKeyClass("inkc")
  public static class SingleLayerInkc extends SingleLayer {
  }
  public static class SingleLayerConRoot extends SingleLayer implements ItemContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[] {new Item("field").itemNameKey("icField")};
    }
  }
  public static class SingleLayerConChild {
    @Valid
    private Child child = new Child();

    public static class Child implements ItemContainer {
      @NotNull
      private String field;

      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("field").itemNameKey("icField")};
      }
    }
  }

  public static class MultipleLayer {
    @Valid
    private Child child = new Child();

    public static class Child {
      @Valid
      private GrandChild grandChild = new GrandChild();

      public static class GrandChild {
        @NotNull
        private String field;
      }
    }
  }
  @ItemNameKeyClass("inkRoot")
  public static class MultipleLayerInkc {
    @Valid
    private Child child = new Child();

    @ItemNameKeyClass("inkcChild")
    public static class Child {
      @Valid
      private GrandChild grandChild = new GrandChild();

      @ItemNameKeyClass("inkcGrandChild")
      public static class GrandChild {
        @NotNull
        private String field;
      }
    }
  }
  public static class MultipleLayerConRoot extends MultipleLayer implements ItemContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[] {new Item("child").itemNameKey("icChild"),
          new Item("child.grandChild").itemNameKey("icGrandChild"),
          new Item("child.grandChild.field").itemNameKey("icField")};
    }
  }
  public static class MultipleLayerConChild {
    @Valid
    private Child child = new Child();

    public static class Child implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("grandChild").itemNameKey("icGrandChild"),
            new Item("grandChild.field").itemNameKey("icField")};
      }

      @Valid
      private GrandChild grandChild = new GrandChild();

      public static class GrandChild {
        @NotNull
        private String field;
      }
    }
  }

  @Test
  public void itemNameAndItemNamePathTestWithClassValidator() {
    String msg = null;
    final String MSG = " are always false.";

    // ClassSingleLayer
    Assertions.assertEquals("'field 1', 'field 2' are always false.",
        getMsg(new ClassSingleLayer(), true, true));
    // ClassSingleLayerItemNameKeyClass
    Assertions.assertEquals(
        "'ItemNameKeyClass considered field 1', 'ItemNameKeyClass considered field 2' are always false.",
        getMsg(new ClassSingleLayerInkc(), true, true));
    // ClassSingleLayerContainerRoot
    Assertions.assertEquals("'ItemContainer considered field 1', 'field 2' are always false.",
        getMsg(new ClassSingleLayerConRoot(), true, true));
    // ClassSingleLayerContainerChild
    msg = "'ItemContainer considered field 1' at 'child', 'child field 2' at 'child'" + MSG;
    Assertions.assertEquals(msg, getMsg(new ClassSingleLayerConChild(), true, true));

    // ClassMultipleLayer
    msg = "'field 1' at 'child field' > 'grand child field' > 'the child', "
        + "'field 2' at 'child field' > 'grand child field' > 'the child'" + MSG;
    Assertions.assertEquals(msg, getMsg(new ClassMultipleLayer(), true, true));
    // ClassMultipleLayer
    msg = "'ItemNameKeyClass considered field 1' at 'child field' > 'grand child field' "
        + "> 'the child', " + "'ItemNameKeyClass considered field 2' at 'child field' "
        + "> 'grand child field' > 'the child'" + MSG;
    Assertions.assertEquals(msg, getMsg(new ClassMultipleLayerInkc(), true, true));
    // ClassMultipleLayerContainerRoot
    msg = "'ItemContainer considered the child field 1' "
        + "at 'child field' > 'grand child field' > 'the child', "
        + "'field 2' at 'child field' > 'grand child field' > 'the child'" + MSG;
    Assertions.assertEquals(msg, getMsg(new ClassMultipleLayerConRoot(), true, true));
    // ClassSingleLayerContainerChild
    msg = "'ItemContainer considered the child field 1' "
        + "at 'child field' > 'grand child field' > 'the child', "
        + "'field 2' at 'child field' > 'grand child field' > 'the child'" + MSG;
    Assertions.assertEquals(msg, getMsg(new ClassMultipleLayerConChild(), true, true));
  }

  @ClassAlwaysFalse(propertyPath = {"field1", "field2"})
  public static class ClassSingleLayer {
    @SuppressWarnings("unused")
    private String field1;
    @SuppressWarnings("unused")
    private String field2;
  }
  @ItemNameKeyClass("itemNameKeyClass")
  public static class ClassSingleLayerInkc extends ClassSingleLayer {
  }
  @ClassAlwaysFalse(propertyPath = {"field1", "field2"})
  public static class ClassSingleLayerConRoot extends ClassSingleLayer implements ItemContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[] {new Item("field1").itemNameKey("icField1")};
    }
  }
  public static class ClassSingleLayerConChild {
    @Valid
    private Child child = new Child();

    @ClassAlwaysFalse(propertyPath = {"field1", "field2"})
    private static class Child extends ClassSingleLayer implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("field1").itemNameKey("icField1")};
      }
    }
  }

  public static class ClassMultipleLayer {
    @Valid
    private Child child = new Child();

    private static class Child {
      @Valid
      private GrandChild grandChild = new GrandChild();

      @ClassAlwaysFalse(propertyPath = {"theChild.field1", "theChild.field2"})
      private static class GrandChild {
        @SuppressWarnings("unused")
        private TheChild theChild = new TheChild();

        private static class TheChild {
          @SuppressWarnings("unused")
          private String field1;
          @SuppressWarnings("unused")
          private String field2;
        }
      }
    }
  }
  public static class ClassMultipleLayerInkc {
    @Valid
    private Child child = new Child();

    private static class Child {
      @Valid
      private GrandChild grandChild = new GrandChild();

      @ClassAlwaysFalse(propertyPath = {"theChild.field1", "theChild.field2"})
      private static class GrandChild {
        @SuppressWarnings("unused")
        private TheChild theChild = new TheChild();

        @ItemNameKeyClass("itemNameKeyClass")
        private static class TheChild {
          @SuppressWarnings("unused")
          private String field1;
          @SuppressWarnings("unused")
          private String field2;
        }
      }
    }
  }

  public static class ClassMultipleLayerConRoot extends ClassMultipleLayer
      implements ItemContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[] {new Item("child.grandChild.theChild.field1").itemNameKey("icField1")};
    }
  }
  public static class ClassMultipleLayerConChild {
    @Valid
    private Child child = new Child();

    private static class Child implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("grandChild.theChild.field1").itemNameKey("icField1")};
      }

      @Valid
      private GrandChild grandChild = new GrandChild();

      @ClassAlwaysFalse(propertyPath = {"theChild.field1", "theChild.field2"})
      private static class GrandChild {
        @SuppressWarnings("unused")
        private TheChild theChild = new TheChild();

        private static class TheChild {
          @SuppressWarnings("unused")
          private String field1;
          @SuppressWarnings("unused")
          private String field2;
        }
      }
    }
  }

  @Test
  public void itemNameAndItemNamePathTestWithMethodValidator() {
    String msg = null;
    final String MSG = " are always false.";
    // MethodSingleLayer
    Assertions.assertEquals("'field 1', 'field 2' are always false.",
        getMsg(new MethodSingleLayer(), true, true));
    // MethodSingleLayerItemNameKeyClass
    msg = "'ItemNameKeyClass considered field 1', 'ItemNameKeyClass considered field 2'" + MSG;
    Assertions.assertEquals(msg, getMsg(new MethodSingleLayerInkc(), true, true));
    // MethodSingleLayerContainerRoot
    msg = "'ItemContainer considered field 1', 'field 2'" + MSG;
    Assertions.assertEquals(msg, getMsg(new MethodSingleLayerConRoot(), true, true));
    // MethodSingleLayerContainerChild
    msg = "'ItemContainer considered field 1' at 'child', 'child field 2' at 'child'" + MSG;
    Assertions.assertEquals(msg, getMsg(new MethodSingleLayerConChild(), true, true));

    // MethodMultipleLayer
    msg = "'field 1' at 'child field' > 'grand child field' > 'the child', "
        + "'field 2' at 'child field' > 'grand child field' > 'the child'" + MSG;
    Assertions.assertEquals(msg, getMsg(new MethodMultipleLayer(), true, true));
    // MethodMultipleLayerItemNameKeyClass
    msg = "'ItemNameKeyClass considered field 1' at 'child field' > 'grand child field' > 'the child', 'ItemNameKeyClass considered field 2' at 'child field' " + "> 'grand child field' > 'the child'" + MSG;
    Assertions.assertEquals(msg, getMsg(new MethodMultipleLayerInkc(), true, true));
    // MethodMultipleLayerContainerRoot
    msg = "'ItemContainer considered the child field 1' at 'child field' "
        + "> 'grand child field' > 'the child', 'field 2' at 'child field' "
        + "> 'grand child field' > 'the child'" + MSG;
    Assertions.assertEquals(msg, getMsg(new MethodMultipleLayerConRoot(), true, true));
    // MethodMultipleLayerContainerChild
    msg = "'ItemContainer considered the child field 1' at 'child field' "
        + "> 'grand child field' > 'the child', 'field 2' at 'child field' "
        + "> 'grand child field' > 'the child'" + MSG;
    Assertions.assertEquals(msg, getMsg(new MethodMultipleLayerConChild(), true, true));
  }

  public static class MethodSingleLayer {
    @SuppressWarnings("unused")
    private String field1;
    @SuppressWarnings("unused")
    private String field2;

    @MethodAlwaysFalse(propertyPath = {"field1", "field2"})
    public boolean isAlwaysFalse() {
      return false;
    }
  }
  @ItemNameKeyClass("itemNameKeyClass")
  public static class MethodSingleLayerInkc extends MethodSingleLayer {

  }

  public static class MethodSingleLayerConRoot extends MethodSingleLayer implements ItemContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[] {new Item("field1").itemNameKey("icField1")};
    }
  }

  public static class MethodSingleLayerConChild {
    @Valid
    private Child child = new Child();

    private static class Child extends MethodSingleLayer implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("field1").itemNameKey("icField1")};
      }

      @MethodAlwaysFalse(propertyPath = {"field1", "field2"})
      public boolean isAlwaysFalse() {
        return false;
      }
    }
  }

  public static class MethodMultipleLayer {
    @Valid
    private Child child = new Child();

    private static class Child {
      @Valid
      private GrandChild grandChild = new GrandChild();

      @ClassAlwaysFalse(propertyPath = {"theChild.field1", "theChild.field2"})
      private static class GrandChild {
        @SuppressWarnings("unused")
        private TheChild theChild = new TheChild();

        private static class TheChild {
          @SuppressWarnings("unused")
          private String field1;
          @SuppressWarnings("unused")
          private String field2;
        }
      }
    }
  }

  public static class MethodMultipleLayerInkc {
    @Valid
    private Child child = new Child();

    private static class Child {
      @Valid
      private GrandChild grandChild = new GrandChild();

      @ClassAlwaysFalse(propertyPath = {"theChild.field1", "theChild.field2"})
      private static class GrandChild {
        @SuppressWarnings("unused")
        private TheChild theChild = new TheChild();

        @ItemNameKeyClass("itemNameKeyClass")
        private static class TheChild {
          @SuppressWarnings("unused")
          private String field1;
          @SuppressWarnings("unused")
          private String field2;
        }
      }
    }
  }

  public static class MethodMultipleLayerConRoot extends MethodMultipleLayer
      implements ItemContainer {
    @Override
    public Item[] customizedItems() {
      return new Item[] {new Item("child.grandChild.theChild.field1").itemNameKey("icField1")};
    }
  }
  public static class MethodMultipleLayerConChild {
    @Valid
    private Child child = new Child();

    private static class Child implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("grandChild.theChild.field1").itemNameKey("icField1")};
      }

      @Valid
      private GrandChild grandChild = new GrandChild();

      @ClassAlwaysFalse(propertyPath = {"theChild.field1", "theChild.field2"})
      private static class GrandChild {
        @SuppressWarnings("unused")
        private TheChild theChild = new TheChild();

        private static class TheChild {
          @SuppressWarnings("unused")
          private String field1;
          @SuppressWarnings("unused")
          private String field2;
        }
      }
    }
  }

  @Test
  public void itemNameAndItemNamePathTestWithConstraintViolationBean() {
    // Looks like it's tested at ConstraintViolationBeanTest.
  }
}
