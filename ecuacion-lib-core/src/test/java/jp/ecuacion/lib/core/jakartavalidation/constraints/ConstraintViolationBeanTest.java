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
package jp.ecuacion.lib.core.jakartavalidation.constraints;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import static org.assertj.core.api.Assertions.assertThat;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link ConstraintViolationBean}. */
@DisplayName("ConstraintViolationBean")
@SuppressWarnings({"SameNameButDifferent", "UnusedVariable"})
public class ConstraintViolationBeanTest {

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  //@formatter:off
  ///
  ///Data patterns are as follows:
  ///
  /// 1. container structure pattern: `record` / `other`
  /// 1. validator pattern: `field validator` / `class validator`
  /// 1. violation occurring path: `root` / `child` / `grandChild`
  ///
  /// #1, 2 and 3 are not totally, but relatively independent,
  /// so it seems too much to execute every combination.
  ///
  /// So the test patterns will be:
  ///
  /// 1. container structure pattern: `record` / (`root`, `child`, `grandChild`) = (`field validator`, `class validator`, `field validator`)
  /// 1. container structure pattern: `other` / (`root`, `child`, `grandChild`) = (`class validator`, `field validator`, `class validator`)
  ///
  //@formatter:on
  @SuppressWarnings("null")
  @Test
  public void dataPatternTest() {
    // 1. container structure pattern: `record` / (`root`, `child`, `grandChild`)
    // = (`field validator`, `class validator`, `field validator`)
    Set<ConstraintViolation<dataPatternTest.No2.Root>> set2 =
        validator.validate(new dataPatternTest.No2.Root());
    assertThat(set2).isNotEmpty();
    for (ConstraintViolation<?> cvBean2 : set2) {
      ConstraintViolationBean<?> bean =
          ConstraintViolationBean.createConstraintViolationBean(cvBean2);
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("Root"))
        check(bean, "root.field");
      else if (leafClassName.equals("Child"))
        check(bean, "child.field");
      else if (leafClassName.equals("GrandChild"))
        check(bean, "grandChild.field");
      else
        throw new RuntimeException();
    }

    // 2. container structure pattern: `other` / (`root`, `child`, `grandChild`)
    // = (`class validator`, `field validator`, `class validator`)
    Set<ConstraintViolation<dataPatternTest.No3.Root>> set3 =
        validator.validate(new dataPatternTest.No3.Root());
    assertThat(set3).isNotEmpty();
    for (ConstraintViolation<?> cvBean3 : set3) {
      ConstraintViolationBean<?> bean =
          ConstraintViolationBean.createConstraintViolationBean(cvBean3);
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("Root"))
        check(bean, "root.field");
      else if (leafClassName.equals("Child"))
        check(bean, "child.field");
      else if (leafClassName.equals("GrandChild"))
        check(bean, "grandChild.field");
      else
        throw new RuntimeException();
    }
  }

  private void check(ConstraintViolationBean<?> bean, String itemNameKey) {
    Assertions.assertEquals(itemNameKey, bean.getItems()[0].getItemNameKey());
  }

  public static class dataPatternTest {
    public static class No2 {
      public static class Root implements ItemContainer {
        @Override
        public Item[] customizedItems() {
          return new Item[] {};
        }

        @Min(3)
        public Integer field = 2;

        @Valid
        public Child child = new Child();

      }

      @ClassAlwaysFalse(propertyPath = "field")
      public static class Child {
        public @Nullable String field;
        public @Nullable String conditionField;

        @Valid
        public GrandChild grandChild = new GrandChild();
      }

      public static class GrandChild {
        @Min(3)
        public Integer field = 2;
      }
    }

    public static class No3 {

      @ClassAlwaysFalse(propertyPath = "field")
      public static class Root {

        public @Nullable String field;
        public @Nullable String conditionField;

        @Valid
        public Child child = new Child();

      }

      public static class Child {
        @Min(3)
        public Integer field = 2;

        @Valid
        public GrandChild grandChild = new GrandChild();
      }

      @ClassAlwaysFalse(propertyPath = "field")
      public static class GrandChild {
        public @Nullable String field;
        public @Nullable String conditionField;
      }
    }
  }

  //@formatter:off
  ///
  /// EclibItem#itemNameKey tests.
  ///
  //@formatter:on
  @SuppressWarnings("null")
  @Test
  public void item_itemNameKeyTest() {
    // Record in Form
    Set<ConstraintViolation<eclibItem_itemNameKeyTest.Form>> setRif =
        validator.validate(new eclibItem_itemNameKeyTest.Form());
    assertThat(setRif).isNotEmpty();
    for (ConstraintViolation<?> cvBean : setRif) {
      ConstraintViolationBean<?> bean =
          ConstraintViolationBean.createConstraintViolationBean(cvBean);
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("RootRecord")) {
        if (bean.getItems()[0].getPropertyPath().equals("root.field1"))
          assertEqualsItemNameKeyClass("itemNameKeyClass_InItem_Root", bean);
        else
          assertEqualsItemNameKeyClass("rootRecord", bean);

      } else if (leafClassName.equals("ChildRecord")) {
        if (bean.getItems()[0].getPropertyPath().equals("root.child.field1"))
          assertEqualsItemNameKeyClass("itemNameKeyClass_InItem_Child", bean);
        else
          assertEqualsItemNameKeyClass("childRecord", bean);

      } else {
        throw new RuntimeException();
      }
    }

    // Record directly
    Set<ConstraintViolation<eclibItem_itemNameKeyTest.RootRecord>> setRd =
        validator.validate(new eclibItem_itemNameKeyTest.RootRecord());
    assertThat(setRd).isNotEmpty();
    for (ConstraintViolation<?> cvBean : setRd) {
      ConstraintViolationBean<?> bean =
          ConstraintViolationBean.createConstraintViolationBean(cvBean);
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("RootRecord")) {
        if (bean.getItems()[0].getPropertyPath().equals("field1"))
          assertEqualsItemNameKeyClass("itemNameKeyClass_InItem_Root", bean);
        else
          assertEqualsItemNameKeyClass("rootRecord", bean);

      } else if (leafClassName.equals("ChildRecord")) {
        if (bean.getItems()[0].getPropertyPath().equals("child.field1"))
          assertEqualsItemNameKeyClass("itemNameKeyClass_InItem_Child", bean);
        else
          assertEqualsItemNameKeyClass("childRecord", bean);

      } else {
        throw new RuntimeException();
      }
    }
  }

  static class eclibItem_itemNameKeyTest {
    public static class Form {
      @Valid
      RootRecord root = new RootRecord();
    }

    public static class RootRecord implements ItemContainer {

      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("field1").itemNameKey("ItemNameKeyClass_InItem_Root.field"),
            new Item("field2").itemNameKey("field"),
            new Item("child.field1").itemNameKey("ItemNameKeyClass_InItem_Child.field"),
            new Item("child.field2").itemNameKey("field")};
      }

      @NotEmpty
      private @Nullable String field1;

      @NotEmpty
      private @Nullable String field2;

      @Valid
      ChildRecord child = new ChildRecord();
    }

    public static class ChildRecord {

      @NotEmpty
      private @Nullable String field1;

      @NotEmpty
      private @Nullable String field2;
    }
  }

  //@formatter:off
  ///
  ///`@ItemNameKeyClass` tests can be done together with other tests,
  /// but it was divided from them because by integrating them tests are too complicated to understand.
  ///
  /// Data patterns are as follows:
  ///
  /// 1. itemNameKeyClass existence: `no` / `self` / `ancestor`
  /// 1. violation occuring path: `root` / `child` / `grandChild`
  ///
  /// Let's add conditions to reduce the number of tests without losing the quality of the test.
  ///
  /// 1. The tests with no itemNameKeyClass existence will be done by other itemNameKey tests, So
  /// it's skipped in this test.
  ///
  /// So the test patterns will be:
  ///
  /// 1. itemNameKeyClass existence: `self` (*1)
  /// 1. itemNameKeyClass existence: `ancestor`
  ///
  /// (*) Each test has a target object with `root` / `child` / `grandChild`.
  ///
  /// (*1) Add far ancestor record and put `@ItemNameKeyClass` to it to check the `@ItemNameKeyClass`
  /// with self or the nearest ancestor is adopted and others are ignored.
  ///
  //@formatter:on
  @SuppressWarnings("null")
  @Test
  public void itemNameKeyClassAnnotationReadTest() {
    // 1. itemNameKeyClass existence: `self` (*1)
    Set<ConstraintViolation<itemNameKeyClassAnnotationReadTest.No1.Root>> set1 =
        validator.validate(new itemNameKeyClassAnnotationReadTest.No1.Root());
    assertThat(set1).isNotEmpty();
    for (ConstraintViolation<?> cvBean : set1) {
      ConstraintViolationBean<?> bean =
          ConstraintViolationBean.createConstraintViolationBean(cvBean);
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("Root"))
        assertEqualsItemNameKeyClass("itemNameKeyClass_Root", bean);
      else if (leafClassName.equals("Child"))
        assertEqualsItemNameKeyClass("itemNameKeyClass_Child", bean);
      else if (leafClassName.equals("GrandChild"))
        assertEqualsItemNameKeyClass("itemNameKeyClass_GrandChild", bean);
      else
        throw new RuntimeException();
    }

    // 2. construction pattern: `created from ConstraintViolation` / itemNameKeyClass
    // existence: `ancestor`
    Set<ConstraintViolation<itemNameKeyClassAnnotationReadTest.No2.Root>> set2 =
        validator.validate(new itemNameKeyClassAnnotationReadTest.No2.Root());
    assertThat(set2).isNotEmpty();
    for (ConstraintViolation<?> cvBean : set2) {
      ConstraintViolationBean<?> bean =
          ConstraintViolationBean.createConstraintViolationBean(cvBean);
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("Root"))
        assertEqualsItemNameKeyClass("itemNameKeyClass_Root_Parent", bean);
      else if (leafClassName.equals("Child"))
        assertEqualsItemNameKeyClass("itemNameKeyClass_Child_Parent", bean);
      else if (leafClassName.equals("GrandChild"))
        assertEqualsItemNameKeyClass("itemNameKeyClass_GrandChild_Parent", bean);
      else
        throw new RuntimeException();
    }
  }

  @SuppressWarnings("StringSplitter")
  private <T> void assertEqualsItemNameKeyClass(String expected, ConstraintViolationBean<T> bean) {
    Assertions.assertEquals(expected, bean.getItems()[0].getItemNameKey().split("\\.")[0]);
  }

  static class itemNameKeyClassAnnotationReadTest {
    public static class No1 {
      @ItemNameKeyClass("ItemNameKeyClass_Root")
      public static class Root extends RootParent {
        @Min(3)
        public Integer field = 2;

        @SuppressWarnings("exports")
        @Valid
        public Child child = new Child();
      }

      @ItemNameKeyClass("ItemNameKeyClass_Root_Parent")
      public static class RootParent {
      }

      @ClassAlwaysFalse(propertyPath = "field")
      @ItemNameKeyClass("ItemNameKeyClass_Child")
      public static class Child extends ChildParent {
        public @Nullable Integer field = null;
        public @Nullable String conditionField = null;

        @SuppressWarnings("exports")
        @Valid
        public GrandChild grandChild = new GrandChild();
      }

      @ItemNameKeyClass("ItemNameKeyClass_Child_Parent")
      public static class ChildParent {
      }

      @ItemNameKeyClass("ItemNameKeyClass_GrandChild")
      public static class GrandChild extends ChildParent {
        @Min(3)
        public Integer field = 2;
      }

      @ItemNameKeyClass("ItemNameKeyClass_GrandChild_Parent")
      public static class GrandChildParent {
      }
    }

    public static class No2 {
      public static class Root extends RootParent {
        @Min(3)
        public Integer field = 2;

        @SuppressWarnings("exports")
        @Valid
        public Child child = new Child();
      }

      @ItemNameKeyClass("ItemNameKeyClass_Root_Parent")
      public static class RootParent {
      }

      @ClassAlwaysFalse(propertyPath = "field")
      public static class Child extends ChildParent {
        public @Nullable Integer field = null;
        public @Nullable String conditionField = null;

        @SuppressWarnings("exports")
        @Valid
        public GrandChild grandChild = new GrandChild();
      }

      @ItemNameKeyClass("ItemNameKeyClass_Child_Parent")
      public static class ChildParent {
      }

      public static class GrandChild extends GrandChildParent {
        @Min(3)
        public Integer field = 2;
      }

      @ItemNameKeyClass("ItemNameKeyClass_GrandChild_Parent")
      public static class GrandChildParent {
      }
    }

  }

  //@formatter:off
  /// 
  ///The itemNameKeyClass part of `EclibItem#itemNameKey` overrides `@ItemNameKeyClass`
  /// when explicit itemNameKeyClass is set to `EclibItem`.
  ///
  //@formatter:on
  @SuppressWarnings("null")
  @Test
  public void itemNameKeyClassAnnotationOverrideTest() {

    // Record in Form
    Set<ConstraintViolation<itemNameKeyClassAnnotationOverrideTest.Form>> setRif =
        validator.validate(new itemNameKeyClassAnnotationOverrideTest.Form());
    assertThat(setRif).isNotEmpty();
    for (ConstraintViolation<?> cvBean : setRif) {
      ConstraintViolationBean<?> bean =
          ConstraintViolationBean.createConstraintViolationBean(cvBean);
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("RootRecord")) {
        if (bean.getItems()[0].getPropertyPath().equals("root.field1"))
          assertEqualsItemNameKeyClass("itemNameKeyClass_InItem_Root", bean);
        else
          assertEqualsItemNameKeyClass("itemNameKeyClass_Root", bean);

      } else if (leafClassName.equals("ChildRecord")) {
        if (bean.getItems()[0].getPropertyPath().equals("root.child.field1")) {
          assertEqualsItemNameKeyClass("itemNameKeyClass_InItem_Child", bean);

        } else {
          assertEqualsItemNameKeyClass("itemNameKeyClass_Child", bean);
        }

      } else {
        throw new RuntimeException();
      }
    }

    // Record directly
    Set<ConstraintViolation<itemNameKeyClassAnnotationOverrideTest.RootRecord>> setRd =
        validator.validate(new itemNameKeyClassAnnotationOverrideTest.RootRecord());
    assertThat(setRd).isNotEmpty();
    for (ConstraintViolation<?> cvBean : setRd) {
      ConstraintViolationBean<?> bean =
          ConstraintViolationBean.createConstraintViolationBean(cvBean);
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("RootRecord")) {
        if (bean.getItems()[0].getPropertyPath().equals("field1"))
          assertEqualsItemNameKeyClass("itemNameKeyClass_InItem_Root", bean);
        else
          assertEqualsItemNameKeyClass("itemNameKeyClass_Root", bean);

      } else if (leafClassName.equals("ChildRecord")) {
        if (bean.getItems()[0].getPropertyPath().equals("child.field1"))
          assertEqualsItemNameKeyClass("itemNameKeyClass_InItem_Child", bean);
        else
          assertEqualsItemNameKeyClass("itemNameKeyClass_Child", bean);

      } else {
        throw new RuntimeException();
      }
    }
  }

  static class itemNameKeyClassAnnotationOverrideTest {
    public static class Form {
      @Valid
      RootRecord root = new RootRecord();
    }

    @ItemNameKeyClass("ItemNameKeyClass_Root")
    public static class RootRecord implements ItemContainer {

      @Override
      public Item[] customizedItems() {
        return new Item[] {new Item("field1").itemNameKey("ItemNameKeyClass_InItem_Root.field"),
            new Item("field2").itemNameKey("field"),
            new Item("child.field1").itemNameKey("ItemNameKeyClass_InItem_Child.field"),
            new Item("child.field2").itemNameKey("field")};
      }

      @NotEmpty
      private @Nullable String field1;

      @NotEmpty
      private @Nullable String field2;

      @Valid
      ChildRecord child = new ChildRecord();
    }

    @ItemNameKeyClass("ItemNameKeyClass_Child")
    public static class ChildRecord {

      @NotEmpty
      private @Nullable String field1;

      @NotEmpty
      private @Nullable String field2;
    }
  }
}
