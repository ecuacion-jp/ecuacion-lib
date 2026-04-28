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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassAlwaysFalse;
import jp.ecuacion.lib.core.jakartavalidation.constraints.MethodAlwaysFalse;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link ExceptionUtil#getMessageList}. */
@DisplayName("ExceptionUtil - getMessageList")
@SuppressWarnings({"SameNameButDifferent", "UnusedVariable", "ArrayRecordComponent",
    "MultipleNullnessAnnotations", "JavaTimeDefaultTimeZone", "MissingOverride",
    "UnnecessaryParentheses"})
public class ExceptionUtilTest {

  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void before() {
    PropertiesFileUtil.addResourceBundlePostfix("lib-core-test");
  }

  // shared helper used by BasicCollectionTypes and CustomObjectsInCollections
  private static String validateCollection(Object object, boolean isMsgWithItemName,
      boolean showsItemNamePath) {
    Violations violations = ViolationUtil.validate(object).withMessageParameters(
        p -> p.isMessageWithItemName(isMsgWithItemName).showsItemNamePath(showsItemNamePath));
    return ExceptionUtil.getMessageList(violations, Locale.ENGLISH, true).get(0);
  }

  // -------------------------------------------------------------------------
  // Property path label and parentheses
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("property path label and parentheses")
  class PropertyPathLabel {

    @Test
    @DisplayName("label is shown with locale-specific parentheses around item name")
    void propertyPathLabelNameDisplayWithVariousParentheses() {
      // field validator
      // Italian (parenthesis not blank)
      String message = ExceptionUtil
          .getMessageList(validator.validate(new FieldValidator(null)), Locale.ITALIAN).get(0);
      assertThat(message).isEqualTo("'nome' è obbligatorio");
      // German (parenthesis blank)
      message = ExceptionUtil
          .getMessageList(validator.validate(new FieldValidator(null)), Locale.GERMAN).get(0);
      assertThat(message).isEqualTo("Name ist erforderlich");

      // class validator with 1 field
      // Italian (parenthesis not blank)
      message = ExceptionUtil
          .getMessageList(validator.validate(new ClassValidator1(null)), Locale.ITALIAN).get(0);
      assertThat(message).isEqualTo("'classValidator1.str1' è messaggio di esempio");
      // German (parenthesis blank)
      message = ExceptionUtil
          .getMessageList(validator.validate(new ClassValidator1(null)), Locale.GERMAN).get(0);
      assertThat(message).isEqualTo("ClassValidator1.str1 ist Beispielnachricht");

      // class validator with multiple fields
      // Italian (parenthesis not blank)
      message = ExceptionUtil
          .getMessageList(validator.validate(new ClassValidator2(null, null)), Locale.ITALIAN)
          .get(0);
      assertThat(message).isEqualTo(
          "'classValidator2.str1' | 'classValidator2.str2' è messaggio di esempio");
      // German (parenthesis blank)
      message = ExceptionUtil
          .getMessageList(validator.validate(new ClassValidator2(null, null)), Locale.GERMAN)
          .get(0);
      assertThat(message).isEqualTo(
          "ClassValidator2.str1, classValidator2.str2 ist Beispielnachricht");
    }

    public static record FieldValidator(@NotNull @Nullable String str1) {}

    @ClassAlwaysFalse(propertyPath = "str1")
    public static record ClassValidator1(@Nullable String str1) {}

    @ClassAlwaysFalse(propertyPath = {"str1", "str2"})
    public static record ClassValidator2(@Nullable String str1, @Nullable String str2) {}
  }

  // -------------------------------------------------------------------------
  // Various places
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("various violation places")
  class VariousPlaces {

    private String getMsg(Object obj) {
      return ExceptionUtil.getMessageList(validator.validate(obj), Locale.ENGLISH, true).get(0);
    }

    @Test
    @DisplayName("message includes item path from root, child node, and child-in-list")
    void variousPlaces() {
      // normal
      String message = getMsg(new Normal(null));
      assertThat(message).isEqualTo("'normal.name' must not be null.");
      // inside child node
      message = getMsg(new InsideChildNode(new Child(null)));
      assertThat(message).isEqualTo("'child.name' must not be null.");
      // inside child node in list
      message = getMsg(new InsideChildNodeInList(List.of(new Child(null))));
      assertThat(message).isEqualTo("'child.name' must not be null.");
    }

    public static record Normal(@NotNull @Nullable String name) {}

    public static record InsideChildNode(@Valid Child myChild) {}

    public static record InsideChildNodeInList(List<@Valid @NonNull Child> myChildList) {}

    public static record Child(@NotNull @Nullable String name) {}
  }

  // -------------------------------------------------------------------------
  // Non-collection values
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("item name and item name path - non-collection values")
  class NonCollectionValues {

    private String getMsg(Object obj, boolean isMsgWithItemName, boolean showsItemNamePath) {
      return ExceptionUtil.getMessageList(
          (ViolationUtil.validate(obj)
              .withMessageParameters(p -> p.showsItemNamePath(showsItemNamePath))),
          Locale.ENGLISH, isMsgWithItemName).get(0);
    }

    @Test
    @DisplayName("field validator: item name and item name path displayed correctly")
    void itemNameAndItemNamePath() {
      String msg = null;
      final String MSG = "must not be null.";

      // normal (no itemName / itemNamePath)
      assertThat(getMsg(new SingleLayer(), false, false)).isEqualTo("must not be null");

      // Single layer

      // itemName
      assertThat(getMsg(new SingleLayer(), true, false)).isEqualTo("'test field' " + MSG);
      // itemNamePathWithoutItemName (no itemName added since {0} is needed to show itemNamePath)
      assertThat(getMsg(new SingleLayer(), false, true)).isEqualTo("must not be null");
      // itemNamePath
      assertThat(getMsg(new SingleLayer(), true, true)).isEqualTo("'test field' " + MSG);
      // itemNamePath + itemNameKeyClass
      assertThat(getMsg(new SingleLayerInkc(), true, true))
          .isEqualTo("'@ItemNameKeyClass considered field' " + MSG);
      // itemNamePath + ItemContainer(root)
      assertThat(getMsg(new SingleLayerConRoot(), true, true))
          .isEqualTo("'ItemContainer considered field' " + MSG);
      // itemNamePath + ItemContainer(child)
      assertThat(getMsg(new SingleLayerConChild(), true, true))
          .isEqualTo("'ItemContainer considered field' at 'child field' " + MSG);

      // Multiple layer

      // itemName
      assertThat(getMsg(new MultipleLayer(), true, false))
          .isEqualTo("'sample field in grandChild' " + MSG);
      // itemNamePathWithoutItemName
      assertThat(getMsg(new MultipleLayer(), false, true)).isEqualTo("must not be null");
      // itemNamePath
      assertThat(getMsg(new MultipleLayer(), true, true)).isEqualTo(
          "'sample field in grandChild' at 'child field' > 'grand child field' " + MSG);
      // itemNamePath + itemNameKeyClass
      msg = "'@ItemNameKeyClass considered field' at '@ItemNameKeyClass considered child field' "
          + "> '@ItemNameKeyClass considered grandChild field' " + MSG;
      assertThat(getMsg(new MultipleLayerInkc(), true, true)).isEqualTo(msg);
      // itemNamePath + ItemContainer(root)
      msg = "'ItemContainer considered field' at 'ItemContainer considered child field'"
          + " > 'ItemContainer considered grand child field' " + MSG;
      assertThat(getMsg(new MultipleLayerConRoot(), true, true)).isEqualTo(msg);
      // itemNamePath + ItemContainer(child)
      msg = "'ItemContainer considered field' at 'child field'"
          + " > 'ItemContainer considered grand child field' " + MSG;
      assertThat(getMsg(new MultipleLayerConChild(), true, true)).isEqualTo(msg);
    }

    @Test
    @DisplayName("class validator: item name and item name path displayed correctly")
    void itemNameAndItemNamePathWithClassValidator() {
      String msg = null;
      final String MSG = " are always false.";

      assertThat(getMsg(new ClassSingleLayer(), true, true))
          .isEqualTo("'field 1', 'field 2' are always false.");
      assertThat(getMsg(new ClassSingleLayerInkc(), true, true)).isEqualTo(
          "'ItemNameKeyClass considered field 1', "
              + "'ItemNameKeyClass considered field 2' are always false.");
      assertThat(getMsg(new ClassSingleLayerConRoot(), true, true))
          .isEqualTo("'ItemContainer considered field 1', 'field 2' are always false.");
      msg = "'ItemContainer considered field 1' at 'child', 'child field 2' at 'child'" + MSG;
      assertThat(getMsg(new ClassSingleLayerConChild(), true, true)).isEqualTo(msg);

      msg = "'field 1' at 'child field' > 'grand child field' > 'the child', "
          + "'field 2' at 'child field' > 'grand child field' > 'the child'" + MSG;
      assertThat(getMsg(new ClassMultipleLayer(), true, true)).isEqualTo(msg);
      msg = "'ItemNameKeyClass considered field 1' at 'child field' > 'grand child field' "
          + "> 'the child', " + "'ItemNameKeyClass considered field 2' at 'child field' "
          + "> 'grand child field' > 'the child'" + MSG;
      assertThat(getMsg(new ClassMultipleLayerInkc(), true, true)).isEqualTo(msg);
      msg = "'ItemContainer considered the child field 1' "
          + "at 'child field' > 'grand child field' > 'the child', "
          + "'field 2' at 'child field' > 'grand child field' > 'the child'" + MSG;
      assertThat(getMsg(new ClassMultipleLayerConRoot(), true, true)).isEqualTo(msg);
      msg = "'ItemContainer considered the child field 1' "
          + "at 'child field' > 'grand child field' > 'the child', "
          + "'field 2' at 'child field' > 'grand child field' > 'the child'" + MSG;
      assertThat(getMsg(new ClassMultipleLayerConChild(), true, true)).isEqualTo(msg);
    }

    @Test
    @DisplayName("method validator: item name and item name path displayed correctly")
    void itemNameAndItemNamePathWithMethodValidator() {
      String msg = null;
      final String MSG = " are always false.";
      assertThat(getMsg(new MethodSingleLayer(), true, true))
          .isEqualTo("'field 1', 'field 2' are always false.");
      msg = "'ItemNameKeyClass considered field 1', 'ItemNameKeyClass considered field 2'" + MSG;
      assertThat(getMsg(new MethodSingleLayerInkc(), true, true)).isEqualTo(msg);
      msg = "'ItemContainer considered field 1', 'field 2'" + MSG;
      assertThat(getMsg(new MethodSingleLayerConRoot(), true, true)).isEqualTo(msg);
      msg = "'ItemContainer considered field 1' at 'child', 'child field 2' at 'child'" + MSG;
      assertThat(getMsg(new MethodSingleLayerConChild(), true, true)).isEqualTo(msg);

      msg = "'field 1' at 'child field' > 'grand child field' > 'the child', "
          + "'field 2' at 'child field' > 'grand child field' > 'the child'" + MSG;
      assertThat(getMsg(new MethodMultipleLayer(), true, true)).isEqualTo(msg);
      msg =
          "'ItemNameKeyClass considered field 1' at 'child field' > 'grand child field' > "
              + "'the child', 'ItemNameKeyClass considered field 2' at 'child field' "
              + "> 'grand child field' > 'the child'" + MSG;
      assertThat(getMsg(new MethodMultipleLayerInkc(), true, true)).isEqualTo(msg);
      msg = "'ItemContainer considered the child field 1' at 'child field' "
          + "> 'grand child field' > 'the child', 'field 2' at 'child field' "
          + "> 'grand child field' > 'the child'" + MSG;
      assertThat(getMsg(new MethodMultipleLayerConRoot(), true, true)).isEqualTo(msg);
      msg = "'ItemContainer considered the child field 1' at 'child field' "
          + "> 'grand child field' > 'the child', 'field 2' at 'child field' "
          + "> 'grand child field' > 'the child'" + MSG;
      assertThat(getMsg(new MethodMultipleLayerConChild(), true, true)).isEqualTo(msg);
    }

    public static class SingleLayer {
      @NotNull
      private @Nullable String field;
    }

    @ItemNameKeyClass("inkc")
    public static class SingleLayerInkc extends SingleLayer {}

    public static class SingleLayerConRoot extends SingleLayer implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[]{new Item("field").itemNameKey("icField")};
      }
    }

    public static class SingleLayerConChild {
      @Valid
      private Child child = new Child();

      public static class Child implements ItemContainer {
        @NotNull
        private @Nullable String field;

        @Override
        public Item[] customizedItems() {
          return new Item[]{new Item("field").itemNameKey("icField")};
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
          private @Nullable String field;
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
          private @Nullable String field;
        }
      }
    }

    public static class MultipleLayerConRoot extends MultipleLayer implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[]{new Item("child").itemNameKey("icChild"),
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
          return new Item[]{new Item("grandChild").itemNameKey("icGrandChild"),
              new Item("grandChild.field").itemNameKey("icField")};
        }

        @Valid
        private GrandChild grandChild = new GrandChild();

        public static class GrandChild {
          @NotNull
          private @Nullable String field;
        }
      }
    }

    @ClassAlwaysFalse(propertyPath = {"field1", "field2"})
    public static class ClassSingleLayer {
      @SuppressWarnings("unused")
      private @Nullable String field1;
      @SuppressWarnings("unused")
      private @Nullable String field2;
    }

    @ItemNameKeyClass("itemNameKeyClass")
    public static class ClassSingleLayerInkc extends ClassSingleLayer {}

    @ClassAlwaysFalse(propertyPath = {"field1", "field2"})
    public static class ClassSingleLayerConRoot extends ClassSingleLayer implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[]{new Item("field1").itemNameKey("icField1")};
      }
    }

    public static class ClassSingleLayerConChild {
      @Valid
      private Child child = new Child();

      @ClassAlwaysFalse(propertyPath = {"field1", "field2"})
      private static class Child extends ClassSingleLayer implements ItemContainer {
        @Override
        public Item[] customizedItems() {
          return new Item[]{new Item("field1").itemNameKey("icField1")};
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
            private @Nullable String field1;
            @SuppressWarnings("unused")
            private @Nullable String field2;
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
            private @Nullable String field1;
            @SuppressWarnings("unused")
            private @Nullable String field2;
          }
        }
      }
    }

    public static class ClassMultipleLayerConRoot extends ClassMultipleLayer
        implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[]{new Item("child.grandChild.theChild.field1").itemNameKey("icField1")};
      }
    }

    public static class ClassMultipleLayerConChild {
      @Valid
      private Child child = new Child();

      private static class Child implements ItemContainer {
        @Override
        public Item[] customizedItems() {
          return new Item[]{new Item("grandChild.theChild.field1").itemNameKey("icField1")};
        }

        @Valid
        private GrandChild grandChild = new GrandChild();

        @ClassAlwaysFalse(propertyPath = {"theChild.field1", "theChild.field2"})
        private static class GrandChild {
          @SuppressWarnings("unused")
          private TheChild theChild = new TheChild();

          private static class TheChild {
            @SuppressWarnings("unused")
            private @Nullable String field1;
            @SuppressWarnings("unused")
            private @Nullable String field2;
          }
        }
      }
    }

    public static class MethodSingleLayer {
      @SuppressWarnings("unused")
      private @Nullable String field1;
      @SuppressWarnings("unused")
      private @Nullable String field2;

      @MethodAlwaysFalse(propertyPath = {"field1", "field2"})
      public boolean isAlwaysFalse() {
        return false;
      }
    }

    @ItemNameKeyClass("itemNameKeyClass")
    public static class MethodSingleLayerInkc extends MethodSingleLayer {}

    public static class MethodSingleLayerConRoot extends MethodSingleLayer
        implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[]{new Item("field1").itemNameKey("icField1")};
      }
    }

    public static class MethodSingleLayerConChild {
      @Valid
      private Child child = new Child();

      private static class Child extends MethodSingleLayer implements ItemContainer {
        @Override
        public Item[] customizedItems() {
          return new Item[]{new Item("field1").itemNameKey("icField1")};
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
            private @Nullable String field1;
            @SuppressWarnings("unused")
            private @Nullable String field2;
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
            private @Nullable String field1;
            @SuppressWarnings("unused")
            private @Nullable String field2;
          }
        }
      }
    }

    public static class MethodMultipleLayerConRoot extends MethodMultipleLayer
        implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[]{new Item("child.grandChild.theChild.field1").itemNameKey("icField1")};
      }
    }

    public static class MethodMultipleLayerConChild {
      @Valid
      private Child child = new Child();

      private static class Child implements ItemContainer {
        @Override
        public Item[] customizedItems() {
          return new Item[]{new Item("grandChild.theChild.field1").itemNameKey("icField1")};
        }

        @Valid
        private GrandChild grandChild = new GrandChild();

        @ClassAlwaysFalse(propertyPath = {"theChild.field1", "theChild.field2"})
        private static class GrandChild {
          @SuppressWarnings("unused")
          private TheChild theChild = new TheChild();

          private static class TheChild {
            @SuppressWarnings("unused")
            private @Nullable String field1;
            @SuppressWarnings("unused")
            private @Nullable String field2;
          }
        }
      }
    }
  }

  // -------------------------------------------------------------------------
  // Basic collection types (List, Set, Map, Array of primitives/basic types)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("item name and item name path - basic collection types")
  class BasicCollectionTypes {

    /**
     * It doesn't seem important
     *     whether the validator is {@code @NotNull} or other validators,
     *     but SingleLayer test is executed just in case.
     */
    @Test
    @DisplayName("List: item name path includes element index and containing field name")
    void list() {
      String msg = null;
      String expected = null;
      List<String> strList = null;

      // Container (besides collection) needed to validate values.
      @Valid
      @NotNull
      List<@NotNull String> lst = new ArrayList<>();
      lst.add(null);
      assertThat(validator.validate(lst)).isEmpty();

      // List is null (propertyPath: stringList)
      msg = validateCollection(new StringListNotNull(null), true, false);
      assertThat(msg).isEqualTo("'string list' must not be null.");
      // element of List is null (propertyPath: stringList[1].<list element>)
      strList = new ArrayList<>(List.of("a"));
      strList.add(null);
      msg = validateCollection(new StringListNotNull(strList), true, false);
      assertThat(msg).isEqualTo("Element 2 contained by 'string list' must not be null.");

      String MSG = " must match \"[1-9]*\". (input: a)";

      // Single layer - single collection (propertyPath: strList[1].<list element>)

      // no itemName
      msg = validateCollection(new StringList(List.of("1", "a")), false, false);
      assertThat(msg).isEqualTo("must match \"[1-9]*\"");
      // itemName
      msg = validateCollection(new StringList(List.of("1", "a")), true, false);
      assertThat(msg).isEqualTo("Element 2 contained by 'string list'" + MSG);
      // itemNamePath
      msg = validateCollection(new StringList(List.of("1", "a")), true, true);
      assertThat(msg).isEqualTo("Element 2 contained by 'string list'" + MSG);
      // itemNamePath + itemNameKeyClass
      msg = validateCollection(new StringListInkc(List.of("1", "a")), true, true);
      assertThat(msg).isEqualTo(
          "Element 2 contained by 'ItemNameKeyClass considered string list'" + MSG);
      // itemNamePath + ItemContainer(root)
      msg = validateCollection(new StringListConRoot(List.of("1", "a")), true, true);
      assertThat(msg).isEqualTo(
          "Element 2 contained by 'ItemContainer considered string list'" + MSG);
      // itemNamePath + ItemContainer(child)
      StringListConChild.Child child = new StringListConChild.Child(List.of("1", "a"));
      expected =
          "Element 2 contained by 'ItemContainer considered string list' at 'child field'" + MSG;
      msg = validateCollection(new StringListConChild(child), true, true);
      assertThat(msg).isEqualTo(expected);

      // Multiple layer - Multiple collection
      StringMulListList strMulListList = new StringMulListList();
      msg = validateCollection(strMulListList, false, false);
      assertThat(msg).isEqualTo("must match \"[1-9]*\"");
      msg = validateCollection(strMulListList, true, false);
      assertThat(msg).isEqualTo(
          "Element 1 > element 2 contained by 'grand child string list list'" + MSG);
      expected = "Element 1 > element 2 contained by 'grand child string list list' "
          + "at 'child field' > 'grand child field'" + MSG;
      msg = validateCollection(strMulListList, true, true);
      assertThat(msg).isEqualTo(expected);
      // itemNamePath + itemNameKeyClass
      StringMulListListInkc strMulListListInkc =
          new StringMulListListInkc(new StringMulListListInkc.Child(
              new StringMulListListInkc.GrandChild(List.of(List.of("1", "a")))));
      expected = "Element 1 > element 2 contained by "
          + "'ItemNameKeyClass considered string list list' at 'child field' > 'grand child field'"
          + MSG;
      msg = validateCollection(strMulListListInkc, true, true);
      assertThat(msg).isEqualTo(expected);
      // itemNamePath + ItemContainer(root)
      StringMulListListConRoot strMulListListConRoot = new StringMulListListConRoot(
          new StringMulListList.Child(
              new StringMulListList.GrandChild(List.of(List.of("1", "a")))));
      expected = "Element 1 > element 2 contained by 'ItemContainer considered string list list' "
          + "at 'child field' > 'grand child field'" + MSG;
      msg = validateCollection(strMulListListConRoot, true, true);
      assertThat(msg).isEqualTo(expected);
      // itemNamePath + ItemContainer(child)
      StringMulListListConChild strMulListListConChild = new StringMulListListConChild();
      expected = "Element 1 > element 2 contained by 'ItemContainer considered string list list' "
          + "at 'child field' > 'grand child field'" + MSG;
      msg = validateCollection(strMulListListConChild, true, true);
      assertThat(msg).isEqualTo(expected);
    }

    /**
     * It doesn't seem important
     *     whether the validator is {@code @NotNull} or other validators,
     *     but SingleLayer test is executed just in case.
     */
    @Test
    @DisplayName("Set, Map, Array (basic types): @NotNull violation message contains type context")
    void singleLayerOtherThanListNotNullValidation() {
      String msg = null;

      // Set is null (propertyPath: integerSet)
      msg = validateCollection(new ColFieldOfBasicNotNull.IntegerSet(null), true, false);
      assertThat(msg).isEqualTo("'integer set' must not be null.");
      // element of Set is null (propertyPath: integerSet[].<iterable element>)
      Set<Integer> intSet = new HashSet<>();
      intSet.add(1);
      intSet.add(null);
      msg = validateCollection(new ColFieldOfBasicNotNull.IntegerSet(intSet), true, false);
      assertThat(msg).isEqualTo("Some element contained by 'integer set' must not be null.");

      // Map is null (propertyPath: strDateMap)
      msg = validateCollection(new ColFieldOfBasicNotNull.StringDateMap(null), true, false);
      assertThat(msg).isEqualTo("'string date map' must not be null.");
      // element of Map key is null (propertyPath: strDateMap<K>[].<map key>)
      Map<String, LocalDate> strDateMap = new HashMap<>();
      strDateMap.put(null, LocalDate.now());
      msg = validateCollection(new ColFieldOfBasicNotNull.StringDateMap(strDateMap), true, false);
      assertThat(msg).isEqualTo(
          "Some key element of key-value data contained by 'string date map' must not be null.");
      // element of Map value is null (propertyPath: strDateMap[test].<map value>)
      strDateMap.clear();
      strDateMap.put("testKey", null);
      msg = validateCollection(new ColFieldOfBasicNotNull.StringDateMap(strDateMap), true, false);
      assertThat(msg).isEqualTo(
          "Element with key [testKey] contained by 'string date map' must not be null.");

      // array is null (propertyPath: blArray)
      msg = validateCollection(new ColFieldOfBasicNotNull.BooleanArray(null), true, false);
      assertThat(msg).isEqualTo("'boolean array' must not be null.");
      // element of array cannot be validated.
      assertThat(validator
          .validate(new ColFieldOfBasicNotNull.BooleanArray(new Boolean[]{true, null}))).isEmpty();
    }

    @Test
    @DisplayName("Set, Map, Array (basic types): non-null violation message contains type context")
    void singleLayerNotNonNullValidation() {
      String msg = null;

      // element of Set is not min (propertyPath: intSet[].<iterable element>)
      Set<Integer> intSet = new HashSet<>();
      intSet.add(5);
      intSet.add(2);
      msg = validateCollection(new ColFieldOfBasicOtherThanNotNull.IntegerSet(intSet), true, false);
      assertThat(msg).isEqualTo(
          "Some element contained by 'integer set' must be greater than or equal to 3. (input: 2)");

      // element of Map key is not regexp (propertyPath: strDateMap<K>[a].<map key>)
      Map<String, LocalDate> strDateMap = new HashMap<>();
      strDateMap.put("a", LocalDate.of(2101, 1, 1));
      msg = validateCollection(
          new ColFieldOfBasicOtherThanNotNull.StringDateMap(strDateMap), true, false);
      assertThat(msg).isEqualTo("Some key element of key-value data contained by 'string date map' "
          + "must match \"[1-9]*\". (input: a)");

      // element of Map value is future date failure
      strDateMap.clear();
      strDateMap.put("1", LocalDate.of(2001, 1, 1));
      msg = validateCollection(
          new ColFieldOfBasicOtherThanNotNull.StringDateMap(strDateMap), true, false);
      assertThat(msg).isEqualTo("Element with key [1] contained by 'string date map' "
          + "must be a future date. (input: 2001-01-01)");

      // array is null (propertyPath: blArray)
      msg = validateCollection(new ColFieldOfBasicOtherThanNotNull.BooleanArray(null), true, false);
      assertThat(msg).isEqualTo("'boolean array' must not be null.");
      assertThat(validator.validate(
          new ColFieldOfBasicOtherThanNotNull.BooleanArray(new Boolean[]{true, null}))).isEmpty();
    }

    @Test
    @DisplayName("nested Set and Map: item path reflects nesting depth")
    void multipleLayersOtherThanList() {
      String msg = null;
      // element of Set in Set is null
      Set<Set<Integer>> intSetSet = new HashSet<>();
      Set<Integer> intSet = new HashSet<>();
      intSet.add(1);
      intSet.add(null);
      intSetSet.add(intSet);
      msg = validateCollection(new ColFieldOfMulLayerBasic.IntegerSetSet(intSetSet), true, false);
      assertThat(msg).isEqualTo(
          "Some element > some element contained by 'integer set set' must not be null.");

      // element of Map key in Map key is null
      Map<Map<String, LocalDate>, Map<String, LocalDate>> strDateMapMap = new HashMap<>();
      Map<String, LocalDate> nonnullStrDateMap = new HashMap<>();
      nonnullStrDateMap.put("a", LocalDate.of(2001, 1, 1));
      Map<String, LocalDate> strDateMap = new HashMap<>();
      strDateMap.put(null, LocalDate.now());
      strDateMapMap.put(strDateMap, nonnullStrDateMap);
      msg = validateCollection(
          new ColFieldOfMulLayerBasic.StringDateMapMap(strDateMapMap), true, false);
      assertThat(msg).isEqualTo(
          "Some key element of key-value data > some key element of key-value data "
              + "contained by 'string date map map' must not be null.");
      // element of Map value in Map value is null
      strDateMap.clear();
      strDateMap.put("testKey", null);
      strDateMapMap.clear();
      strDateMapMap.put(nonnullStrDateMap, strDateMap);
      msg = validateCollection(
          new ColFieldOfMulLayerBasic.StringDateMapMap(strDateMapMap), true, false);
      assertThat(msg).isEqualTo("Element with key [{a=2001-01-01}] > element with key [testKey] "
          + "contained by 'string date map map' must not be null.");

      // element of array in array cannot be validated.
      assertThat(validator.validate(new ColFieldOfMulLayerBasic.BooleaArraynArray(
          new Boolean[][]{new Boolean[]{true, null}}))).isEmpty();

      // mix
      Map<String, @NotNull Integer> map = new HashMap<>();
      map.put("testKey", null);
      Set<Map<String, @NotNull Integer>> setMap = Set.of(map);
      List<Set<Map<String, @NotNull Integer>>> listSetMap = List.of(setMap);
      msg = validateCollection(new ColFieldOfMulLayerBasic.ListSetMap(listSetMap), true, false);
      assertThat(msg).isEqualTo("Element 1 > some element > element with key [testKey] "
          + "contained by 'list set map' must not be null.");
    }

    public static record StringListNotNull(@NotNull @Nullable List<@NotNull String> strList) {}

    public static record StringList(List<@Pattern(regexp = "[1-9]*") String> strList) {}

    @ItemNameKeyClass("itemNameKeyClass")
    public static record StringListInkc(List<@Pattern(regexp = "[1-9]*") String> strList) {}

    public static record StringListConRoot(List<@Pattern(regexp = "[1-9]*") String> strList)
        implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[]{new Item("strList").itemNameKey("icStrList")};
      }
    }

    public static record StringListConChild(@Valid Child child) {
      private static record Child(List<@Pattern(regexp = "[1-9]*") String> strList)
          implements ItemContainer {
        @Override
        public Item[] customizedItems() {
          return new Item[]{new Item("strList").itemNameKey("icStrList")};
        }
      }
    }

    public static class StringMulListList {
      @Valid
      protected Child child =
          new Child(new StringMulListList.GrandChild(List.of(List.of("1", "a"))));

      private static record Child(@Valid GrandChild grandChild) {}

      private static record GrandChild(
          List<List<@Pattern(regexp = "[1-9]*") String>> strListList) {}
    }

    @ItemNameKeyClass("StringMulListList")
    public static record StringMulListListInkc(@Valid Child child) {
      private static record Child(@Valid GrandChild grandChild) {}

      @ItemNameKeyClass("itemNameKeyClass")
      private static record GrandChild(
          List<List<@Pattern(regexp = "[1-9]*") String>> strListList) {}
    }

    public static class StringMulListListConRoot extends StringMulListList
        implements ItemContainer {
      public StringMulListListConRoot(
          @SuppressWarnings("exports") StringMulListList.Child child) {
        this.child = child;
      }

      @Override
      public Item[] customizedItems() {
        return new Item[]{
            new Item("child.grandChild.strListList").itemNameKey("icStrListList")};
      }
    }

    public static class StringMulListListConChild {
      @Valid
      protected Child child = new Child(new GrandChild(List.of(List.of("1", "a"))));

      private static record Child(@Valid GrandChild grandChild) implements ItemContainer {
        @Override
        public Item[] customizedItems() {
          return new Item[]{new Item("grandChild.strListList").itemNameKey("icStrListList")};
        }
      }

      private static record GrandChild(
          List<List<@Pattern(regexp = "[1-9]*") String>> strListList) {}
    }

    public static class ColFieldOfBasicNotNull {
      public static record IntegerSet(@NotNull @Nullable Set<@NotNull Integer> intSet) {}

      public static record StringDateMap(
          @NotNull @Nullable Map<@NotNull String, @NotNull LocalDate> strDateMap) {}

      public static record BooleanArray(@NotNull Boolean @NotNull @Nullable [] blArray) {}
    }

    public static class ColFieldOfBasicOtherThanNotNull {
      public static record IntegerSet(Set<@Min(3) Integer> intSet) {}

      public static record StringDateMap(
          Map<@Pattern(regexp = "[1-9]*") String, @Future LocalDate> strDateMap) {}

      public static record BooleanArray(@NotNull Boolean @NotNull @Nullable [] blArray) {}
    }

    public static class ColFieldOfMulLayerBasic {
      public static record IntegerSetSet(
          @NotNull Set<@NotNull Set<@NotNull Integer>> intSetSet) {}

      public static record StringDateMapMap(
          @NotNull Map<@NotNull Map<@NotNull String, @NotNull LocalDate>,
              Map<@NotNull String, @NotNull LocalDate>> strDateMapMap) {}

      public static record BooleaArraynArray(
          @NotNull Boolean @NotNull [] @NotNull [] blArrayArray) {}

      public static record ListSetMap(
          List<Set<Map<String, @NotNull Integer>>> listSetMap) {}
    }
  }

  // -------------------------------------------------------------------------
  // Custom objects in collections
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("item name and item name path - custom objects in collections")
  class CustomObjectsInCollections {

    /**
     * It doesn't seem important
     *     whether the validator is {@code @NotNull} or other validators,
     *     but SingleLayer test is executed just in case.
     */
    @Test
    @DisplayName("List of custom objects: item path includes element index")
    void list() {
      String msg = null;
      String MSG = " must not be null.";

      List<TargetCls> targetList =
          List.of(new TargetCls[]{new TargetCls("a"), new TargetCls(null)});
      List<TargetClsInkc> targetInkcList =
          List.of(new TargetClsInkc[]{new TargetClsInkc("a"), new TargetClsInkc(null)});

      // Single layer - single collection
      msg = validateCollection(new SingleList(targetList), false, false);
      assertThat(msg).isEqualTo("must not be null");
      msg = validateCollection(new SingleList(targetList), true, false);
      assertThat(msg).isEqualTo("'target field'" + MSG);
      msg = validateCollection(new SingleList(targetList), true, true);
      assertThat(msg).isEqualTo(
          "'target field' at element 2 contained by 'target list'" + MSG);
      msg = validateCollection(new SingleListInkc(targetInkcList), true, true);
      assertThat(msg).isEqualTo(
          "'ItemNameKeyClass considered field' at element 2 contained by 'target list'" + MSG);
      msg = validateCollection(new SingleListConRoot(targetList), true, true);
      assertThat(msg).isEqualTo(
          "'ItemContainer considered field' at element 2 contained by 'target list'" + MSG);
      SingleListConChild.Child child =
          new SingleListConChild.Child(List.of(List.of(new SingleListConChild.GrandChild(null))));
      String expected = "'sample field in grandChild' at element 1 > element 1 "
          + "contained by 'grand child list list'" + MSG;
      msg = validateCollection(child, true, true);
      assertThat(msg).isEqualTo(expected);

      // Multiple layer - Multiple collection
      MulList mulListList =
          new MulList(new MulList.Child(List.of(List.of(new MulList.GrandChild(null)))));
      msg = validateCollection(mulListList, false, false);
      assertThat(msg).isEqualTo("must not be null");
      msg = validateCollection(mulListList, true, false);
      assertThat(msg).isEqualTo("'sample field in grandChild'" + MSG);
      expected = "'sample field in grandChild' at 'child field' "
          + "> element 1 > element 1 contained by 'grand child field'" + MSG;
      msg = validateCollection(mulListList, true, true);
      assertThat(msg).isEqualTo(expected);
      MulListInkc mulListListInkc =
          new MulListInkc(new MulListInkc.Child(List.of(List.of(new MulListInkc.GrandChild(null)))));
      expected = "'ItemNameKeyClass considered field' at 'child field' > element 1 > element 1"
          + " contained by 'grand child field'" + MSG;
      msg = validateCollection(mulListListInkc, true, true);
      assertThat(msg).isEqualTo(expected);
      MulListConRoot mulListListConRoot = new MulListConRoot(
          new MulListConRoot.Child(List.of(List.of(new MulListConRoot.GrandChild(null)))));
      expected = "'ItemContainer considered field' at 'child field' "
          + "> element 1 > element 1 contained by 'grand child list list'" + MSG;
      msg = validateCollection(mulListListConRoot, true, true);
      assertThat(msg).isEqualTo(expected);
    }

    @Test
    @DisplayName("Set of custom objects: item path uses 'some element'")
    void set() {
      String MSG = " must not be null.";
      Set<TargetCls> targetSet = new HashSet<>();
      targetSet.add(new TargetCls("a"));
      targetSet.add(new TargetCls(null));

      assertThat(validateCollection(new SingleSet(targetSet), false, false))
          .isEqualTo("must not be null");
      assertThat(validateCollection(new SingleSet(targetSet), true, false))
          .isEqualTo("'target field'" + MSG);
      assertThat(validateCollection(new SingleSet(targetSet), true, true))
          .isEqualTo("'target field' at some element contained by 'target set'" + MSG);
    }

    @Test
    @DisplayName("Map value of custom objects: item path includes key")
    void mapValue() {
      String MSG = " must not be null.";
      Map<String, TargetCls> targetMapValue = new HashMap<>();
      targetMapValue.put("key1", new TargetCls(null));

      assertThat(validateCollection(new SingleMapValue(targetMapValue), false, false))
          .isEqualTo("must not be null");
      assertThat(validateCollection(new SingleMapValue(targetMapValue), true, false))
          .isEqualTo("'target field'" + MSG);
      assertThat(validateCollection(new SingleMapValue(targetMapValue), true, true))
          .isEqualTo(
              "'target field' at element with key [key1] contained by 'target map value'" + MSG);
    }

    @Test
    @DisplayName("Array of custom objects: item path includes element index")
    void array() {
      String MSG = " must not be null.";
      TargetCls[] targetArray = new TargetCls[]{new TargetCls("a"), new TargetCls(null)};

      assertThat(validateCollection(new SingleArray(targetArray), false, false))
          .isEqualTo("must not be null");
      assertThat(validateCollection(new SingleArray(targetArray), true, false))
          .isEqualTo("'target field'" + MSG);
      assertThat(validateCollection(new SingleArray(targetArray), true, true))
          .isEqualTo("'target field' at element 2 contained by 'target array'" + MSG);
    }

    public static record TargetCls(@NotNull @Nullable String field) {}

    @ItemNameKeyClass("itemNameKeyClass")
    public static record TargetClsInkc(@NotNull @Nullable String field) {}

    public static record SingleList(List<@Valid TargetCls> targetList) {}

    public static record SingleListInkc(List<@Valid TargetClsInkc> targetList) {}

    public static record SingleListConRoot(List<@Valid TargetCls> targetList)
        implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[]{new Item("targetList[].field").itemNameKey("icField")};
      }
    }

    public static record SingleListConChild(@Valid Child child) {
      private static record Child(List<List<@Valid @NonNull GrandChild>> grandChildListList)
          implements ItemContainer {
        @Override
        public Item[] customizedItems() {
          return new Item[]{new Item("field").itemNameKey("icField")};
        }
      }

      private static record GrandChild(@NotNull @Nullable String field) {}
    }

    public static record MulList(@Valid Child child) {
      private static record Child(List<List<@Valid @Nullable GrandChild>> grandChild) {}

      private static record GrandChild(@NotNull @Nullable String field) {}
    }

    public static record MulListInkc(@Valid Child child) {
      private static record Child(List<List<@Valid @NonNull GrandChild>> grandChild) {}

      @ItemNameKeyClass("itemNameKeyClass")
      private static record GrandChild(@NotNull @Nullable String field) {}
    }

    public static record MulListConRoot(@Valid Child child) implements ItemContainer {
      @Override
      public Item[] customizedItems() {
        return new Item[]{new Item("child.grandChildListList[][].field").itemNameKey("icField")};
      }

      private static record Child(List<List<@Valid @NonNull GrandChild>> grandChildListList) {}

      private static record GrandChild(@NotNull @Nullable String field) {}
    }

    public static record SingleSet(Set<@Valid TargetCls> targetSet) {}

    public static record SingleMapValue(Map<String, @Valid TargetCls> targetMapValue) {}

    // Hibernate Validator emits HV000271 ("@Valid on a container is deprecated;
    // use type argument") for the field/accessor/constructor of this record.
    // The warning is not actionable for arrays: arrays have no type-argument
    // syntax in Java, and `@Valid Foo[]` is the spec-compliant way to cascade
    // for arrays per Jakarta Bean Validation. Hibernate Validator's message
    // applies generically to all containers, so the warning is unavoidable here.
    public static record SingleArray(@Valid TargetCls[] targetArray) {}
  }

  // -------------------------------------------------------------------------
  // getMessageList from Throwable
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getMessageList from Throwable")
  class MessageListFromThrowable {

    @Test
    @DisplayName("RuntimeException: returns its message")
    void runtimeException() {
      List<String> msgs = ExceptionUtil.getMessageList(new RuntimeException("boom"));
      assertThat(msgs).containsExactly("boom");
    }

    @Test
    @DisplayName("RuntimeException with null message: returns empty list")
    void runtimeExceptionNullMessage() {
      List<String> msgs = ExceptionUtil.getMessageList(new RuntimeException());
      assertThat(msgs).isEmpty();
    }

    @Test
    @DisplayName("ViolationException: returns resolved business violation message")
    void violationException() {
      ViolationException ve =
          new ViolationException(new Violations().add(new BusinessViolation("MSG1")));
      List<String> msgs = ExceptionUtil.getMessageList(ve, Locale.ENGLISH, false);
      assertThat(msgs).containsExactly("message 1.");
    }

    @Test
    @DisplayName("ConstraintViolationException: returns resolved constraint message")
    void constraintViolationException() {
      Set<ConstraintViolation<SimpleNotNullBean>> cvs =
          validator.validate(new SimpleNotNullBean(null));
      List<String> msgs =
          ExceptionUtil.getMessageList(new ConstraintViolationException(cvs), Locale.ENGLISH, false);
      assertThat(msgs).hasSize(1);
      assertThat(msgs.get(0)).contains("must not be null");
    }

    @SuppressWarnings("unused")
    public static record SimpleNotNullBean(@NotNull @Nullable String value) {}
  }

  // -------------------------------------------------------------------------
  // getMessageList from Violations
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getMessageList from Violations")
  class MessageListFromViolations {

    @Test
    @DisplayName("single business violation: returns resolved message")
    void singleBusinessViolation() {
      Violations v = new Violations().add(new BusinessViolation("MSG1"));
      List<String> msgs = ExceptionUtil.getMessageList(v, Locale.ENGLISH, false);
      assertThat(msgs).containsExactly("message 1.");
    }

    @Test
    @DisplayName("multiple business violations: returns all messages")
    void multipleBusinessViolations() {
      Violations v = new Violations()
          .add(new BusinessViolation("MSG1"))
          .add(new BusinessViolation("MSG1"));
      List<String> msgs = ExceptionUtil.getMessageList(v, Locale.ENGLISH, false);
      assertThat(msgs).hasSize(2);
    }

    @Test
    @DisplayName("no locale specified: defaults to ROOT locale")
    void noLocale() {
      Violations v = new Violations().add(new BusinessViolation("MSG1"));
      List<String> msgs = ExceptionUtil.getMessageList(v);
      assertThat(msgs).hasSize(1);
    }
  }

  // -------------------------------------------------------------------------
  // getMessageList from ViolationException
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getMessageList from ViolationException")
  class MessageListFromViolationException {

    @Test
    @DisplayName("returns messages from the exception's violations")
    void basic() {
      ViolationException ve =
          new ViolationException(new Violations().add(new BusinessViolation("MSG1")));
      List<String> msgs = ExceptionUtil.getMessageList(ve);
      assertThat(msgs).containsExactly("message 1.");
    }

    @Test
    @DisplayName("with locale: message resolved for specified locale")
    void withLocale() {
      ViolationException ve =
          new ViolationException(new Violations().add(new BusinessViolation("MSG1")));
      List<String> msgs = ExceptionUtil.getMessageList(ve, Locale.ENGLISH);
      assertThat(msgs).containsExactly("message 1.");
    }

    @Test
    @DisplayName("with isMessagesWithItemNamesAsDefault flag: delegates correctly")
    void withFlag() {
      ViolationException ve =
          new ViolationException(new Violations().add(new BusinessViolation("MSG1")));
      List<String> msgs = ExceptionUtil.getMessageList(ve, false);
      assertThat(msgs).containsExactly("message 1.");
    }
  }

  // -------------------------------------------------------------------------
  // getMessageList delegate overloads (0% coverage)
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getMessageList delegate overloads")
  class MessageListDelegateOverloads {

    @SuppressWarnings("unused")
    private static record DelegateBean(@NotNull @Nullable String value) {}

    private Set<ConstraintViolation<DelegateBean>> violations() {
      return validator.validate(new DelegateBean(null));
    }

    @Test
    @DisplayName("getMessageList(Set): delegates to locale-less overload")
    void set() {
      assertThat(ExceptionUtil.getMessageList(violations())).hasSize(1);
    }

    @Test
    @DisplayName("getMessageList(Set, boolean): delegates with isWithItemNames flag")
    void setWithBoolean() {
      assertThat(ExceptionUtil.getMessageList(violations(), false)).hasSize(1);
    }

    @Test
    @DisplayName("getMessageList(Set, boolean, MessageParameters): delegates with params")
    void setWithBooleanAndParams() {
      assertThat(ExceptionUtil.getMessageList(
          violations(), false, Violations.newMessageParameters())).hasSize(1);
    }

    @Test
    @DisplayName("getMessageList(Throwable, Locale): delegates correctly")
    void throwableWithLocale() {
      List<String> msgs =
          ExceptionUtil.getMessageList(new RuntimeException("boom"), Locale.ENGLISH);
      assertThat(msgs).containsExactly("boom");
    }

    @Test
    @DisplayName("getMessageList(Throwable, boolean): delegates correctly")
    void throwableWithBoolean() {
      List<String> msgs = ExceptionUtil.getMessageList(new RuntimeException("boom"), false);
      assertThat(msgs).containsExactly("boom");
    }
  }

  // -------------------------------------------------------------------------
  // getMessageFromBusinessViolation additional branches
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getMessageFromBusinessViolation - additional branches")
  class MessageFromBusinessViolationBranches {

    @Test
    @DisplayName("isMessagesWithItemNamesAsDefault=true: uses withItemName message lookup")
    void isMessageWithItemNameTrue() {
      Violations v = new Violations().add(new BusinessViolation("MSG1"));
      List<String> msgs = ExceptionUtil.getMessageList(v, Locale.ENGLISH, true);
      assertThat(msgs).containsExactly("message 1.");
    }

    @Test
    @DisplayName("explicit isMessageWithItemName(false): overrides default")
    void explicitIsMessageWithItemNameFalse() {
      Violations v = new Violations().add(new BusinessViolation("MSG1"))
          .withMessageParameters(p -> p.isMessageWithItemName(false));
      List<String> msgs = ExceptionUtil.getMessageList(v, Locale.ENGLISH, true);
      assertThat(msgs).containsExactly("message 1.");
    }

    @Test
    @DisplayName("message prefix is prepended")
    void withPrefix() {
      Violations v = new Violations().add(new BusinessViolation("MSG1"))
          .withMessageParameters(p -> p.messagePrefix(Arg.string("[")));
      List<String> msgs = ExceptionUtil.getMessageList(v, Locale.ENGLISH, false);
      assertThat(msgs.get(0)).startsWith("[");
    }

    @Test
    @DisplayName("message postfix is appended")
    void withPostfix() {
      Violations v = new Violations().add(new BusinessViolation("MSG1"))
          .withMessageParameters(p -> p.messagePostfix(Arg.string("]")));
      List<String> msgs = ExceptionUtil.getMessageList(v, Locale.ENGLISH, false);
      assertThat(msgs.get(0)).endsWith("]");
    }
  }

  // -------------------------------------------------------------------------
  // ExceptionUtil.LocalizedEmbeddedParameter
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("LocalizedEmbeddedParameter")
  class LocalizedEmbeddedParameterTests {

    @Test
    @DisplayName("compact constructor sets defaults: isItemName=false, items=null, rootBean=null")
    void compactConstructor() {
      ExceptionUtil.LocalizedEmbeddedParameter p = new ExceptionUtil.LocalizedEmbeddedParameter(
          "paramKey",
          new PropertiesFileUtilFileKindEnum[]{PropertiesFileUtilFileKindEnum.MESSAGES},
          "some.property.key");
      assertThat(p.parameterKey()).isEqualTo("paramKey");
      assertThat(p.fileKinds()).hasSize(1);
      assertThat(p.isItemName()).isFalse();
      assertThat(p.items()).isNull();
      assertThat(p.rootBean()).isNull();
      assertThat(p.propertyFileKey()).isEqualTo("some.property.key");
    }
  }
}
