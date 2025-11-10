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
package jp.ecuacion.lib.core.jakartavalidation.bean;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.ValidationAppException;
import jp.ecuacion.lib.core.item.EclibItem;
import jp.ecuacion.lib.core.item.EclibItemContainer;
import jp.ecuacion.lib.core.jakartavalidation.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.jakartavalidation.validator.ConditionalNotEmpty;
import jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionValuePattern;
import jp.ecuacion.lib.core.util.ValidationUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConstraintViolationBeanTest {

  private static final String NOT_EMPTY = "jakarta.validation.constraints.NotEmpty";


  //@formatter:off
  /// 
  ///Data patterns are as follows:
  ///
  /// 1. container structure pattern: `record` / `record stored in form` / `other` 
  /// 1. validator pattern: `no validator(not empty)` (*1) / `field validator` / `class validator`
  /// 1. violation occuring path: `root` / `child` / `grandChild`
  ///
  /// (*1) Constructing ConstraintViolationBean without ContraintViolation.
  ///
  /// Let's add conditions to reduce the number of tests without losing the quality of the test.
  ///
  /// 1. when 3. validator pattern == `no validator(not empty)`, 2. container
  ///    structure pattern is `record stored in form` only.  
  ///    (This is not a condition for test. `no
  ///    validator(not empty)` can be used only for `record stored in form` in nature.) 
  /// 1. #1, 2 and 3 are not totally, but relatively independent,
  ///    so it seems too much to execute 3(#1) * 3(#2) * 3(#3) tests.
  /// 
  /// So the test patterns will be:
  ///
  /// 1. container structure pattern: `record stored in form` / validator pattern: `no validator(not empty)`
  /// 1. container structure pattern: `record` / (`root`, `child`, `grandChild`) = (`field validator`, `class validator`, `field validator`)
  /// 1. container structure pattern: `other` / (`root`, `child`, `grandChild`) = (`class validator`, `field validator`, `class validator`)
  ///
  //@formatter:on
  @Test
  public void dataPatternTest() {
    Optional<MultipleAppException> mae;
    List<ConstraintViolationBean> list;
    ConstraintViolationBean cvBean;

    // 1. container structure pattern: `record stored in form`
    // / validator pattern: `no validator(not empty)`
    cvBean =
        new ConstraintViolationBean(new dataPatternTest.No1.Form(), "", NOT_EMPTY, "root", "field");
    checkForForm(cvBean, "field", "root.field");
    cvBean = new ConstraintViolationBean(new dataPatternTest.No1.Form(), "", NOT_EMPTY, "root",
        "child.field");
    checkForForm(cvBean, "child.field", "child.field");
    cvBean = new ConstraintViolationBean(new dataPatternTest.No1.Form(), "", NOT_EMPTY, "root",
        "child.grandChild.field");
    checkForForm(cvBean, "child.grandChild.field", "grandChild.field");

    // 2. container structure pattern: `record` / (`root`, `child`, `grandChild`)
    // = (`field validator`, `class validator`, `field validator`)
    mae = ValidationUtil.validateThenReturn(new dataPatternTest.No2.Root());
    Assertions.assertTrue(mae.isPresent());
    list = mae.get().getList().stream()
        .map(ex -> ((ValidationAppException) ex).getConstraintViolationBean()).toList();
    for (ConstraintViolationBean bean : list) {
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

    // 3. container structure pattern: `other` / (`root`, `child`, `grandChild`)
    // = (`class validator`, `field validator`, `class validator`)
    mae = ValidationUtil.validateThenReturn(new dataPatternTest.No3.Root());
    Assertions.assertTrue(mae.isPresent());
    list = mae.get().getList().stream()
        .map(ex -> ((ValidationAppException) ex).getConstraintViolationBean()).toList();
    for (ConstraintViolationBean bean : list) {
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

  private void checkForForm(ConstraintViolationBean bean, String itemPropertyPath,
      String itemNameKey) {
    Assertions.assertEquals(itemPropertyPath,
        bean.getFieldInfoBeans()[0].itemPropertyPathForForm);
    check(bean, itemNameKey);
  }

  private void check(ConstraintViolationBean bean, String itemNameKey) {
    Assertions.assertEquals(itemNameKey, bean.getFieldInfoBeans()[0].itemNameKey);
  }

  public static class dataPatternTest {
    public static class No1 {

      public static class Form {
        @Valid
        public Root root = new Root();
      }

      public static class Root implements EclibItemContainer {
        @Override
        public EclibItem[] getItems() {
          return new EclibItem[] {};
        }

        public String field;

        @Valid
        public Child child = new Child();

      }

      public static class Child {
        public String field;

        @Valid
        public GrandChild grandChild = new GrandChild();
      }

      public static class GrandChild {
        public String field;
      }
    }

    public static class No2 {
      public static class Root implements EclibItemContainer {
        @Override
        public EclibItem[] getItems() {
          return new EclibItem[] {};
        }

        @Min(3)
        public Integer field = 2;

        @Valid
        public Child child = new Child();

      }

      @ConditionalNotEmpty(propertyPath = "field", conditionPropertyPath = "conditionField",
          conditionPattern = ConditionValuePattern.empty)
      public static class Child {
        public String field;
        public String conditionField;

        @Valid
        public GrandChild grandChild = new GrandChild();
      }

      public static class GrandChild {
        @Min(3)
        public Integer field = 2;
      }
    }

    public static class No3 {
      @ConditionalNotEmpty(propertyPath = "field", conditionPropertyPath = "conditionField",
          conditionPattern = ConditionValuePattern.empty)
      public static class Root {

        public String field;
        public String conditionField;

        @Valid
        public Child child = new Child();

      }

      public static class Child {
        @Min(3)
        public Integer field = 2;

        @Valid
        public GrandChild grandChild = new GrandChild();
      }


      @ConditionalNotEmpty(propertyPath = "field", conditionPropertyPath = "conditionField",
          conditionPattern = ConditionValuePattern.empty)
      public static class GrandChild {
        public String field;
        public String conditionField;
      }
    }
  }

  //@formatter:off
  ///
  /// EclibItem#itemNameKey tests.
  ///
  //@formatter:on
  @Test
  public void eclibItem_itemNameKeyTest() {
    Optional<MultipleAppException> mae;
    List<ConstraintViolationBean> list;

    // Record in Form
    mae = ValidationUtil.validateThenReturn(new eclibItem_itemNameKeyTest.Form());
    Assertions.assertTrue(mae.isPresent());
    list = mae.get().getList().stream()
        .map(ex -> ((ValidationAppException) ex).getConstraintViolationBean()).toList();
    for (ConstraintViolationBean bean : list) {
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("RootRecord")) {
        if (bean.getFieldInfoBeans()[0].fullPropertyPath.equals("root.field1"))
          assertEqualsItemNameKeyClass("ItemNameKeyClass_InItem_Root", bean);
        else
          assertEqualsItemNameKeyClass("root", bean);

      } else if (leafClassName.equals("ChildRecord")) {
        if (bean.getFieldInfoBeans()[0].fullPropertyPath.equals("root.child.field1"))
          assertEqualsItemNameKeyClass("ItemNameKeyClass_InItem_Child", bean);
        else
          assertEqualsItemNameKeyClass("child", bean);

      } else {
        throw new RuntimeException();
      }
    }

    // Record directly
    mae = ValidationUtil.validateThenReturn(new eclibItem_itemNameKeyTest.RootRecord());
    Assertions.assertTrue(mae.isPresent());
    list = mae.get().getList().stream()
        .map(ex -> ((ValidationAppException) ex).getConstraintViolationBean()).toList();
    for (ConstraintViolationBean bean : list) {
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("RootRecord")) {
        if (bean.getFieldInfoBeans()[0].fullPropertyPath.equals("field1"))
          assertEqualsItemNameKeyClass("ItemNameKeyClass_InItem_Root", bean);
        else
          assertEqualsItemNameKeyClass("rootRecord", bean);

      } else if (leafClassName.equals("ChildRecord")) {
        if (bean.getFieldInfoBeans()[0].fullPropertyPath.equals("child.field1"))
          assertEqualsItemNameKeyClass("ItemNameKeyClass_InItem_Child", bean);
        else
          assertEqualsItemNameKeyClass("child", bean);

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

    public static class RootRecord implements EclibItemContainer {

      @Override
      public EclibItem[] getItems() {
        return new EclibItem[] {
            new EclibItem("field1").itemNameKey("ItemNameKeyClass_InItem_Root.field"),
            new EclibItem("field2").itemNameKey("field"),
            new EclibItem("child.field1").itemNameKey("ItemNameKeyClass_InItem_Child.field"),
            new EclibItem("child.field2").itemNameKey("field")};
      }

      @NotEmpty
      private String field1;

      @NotEmpty
      private String field2;

      @Valid
      ChildRecord child = new ChildRecord();
    }

    public static class ChildRecord {

      @NotEmpty
      private String field1;

      @NotEmpty
      private String field2;
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
  /// 1. construction pattern: `created from ConstraintViolation` / `the other constructor` 
  /// 1. violation occuring path: `root` / `child` / `grandChild`
  ///
  /// Let's add conditions to reduce the number of tests without losing the quality of the test.
  ///
  /// 1. The tests with no itemNameKeyClass existence will be done by other itemNameKey tests, So
  /// it's skipped in this test.
  ///
  /// So the test patterns will be:
  ///
  /// 1. construction pattern: `created from ConstraintViolation` / itemNameKeyClass existence:
  /// `self` (*1) 
  /// 1. construction pattern: `created from ConstraintViolation` / itemNameKeyClass
  /// existence: `ancestor` 
  /// 1. construction pattern: `the other constructor` / itemNameKeyClass existence: `self` 
  /// 1. construction pattern: `the other constructor` / itemNameKeyClass existence:` ancestor` (*1)
  ///
  /// (*) Each test has a target object with `root` / `child` / `grandChild`.
  ///
  /// (*1) Add far ancestor record and put `@ItemNameKeyClass` to it to check the `@ItemNameKeyClass`
  /// with self or the nearest ancestor is adopted and others are ignored.
  ///
  //@formatter:on
  @Test
  public void itemNameKeyClassAnnotationReadTest() {

    Optional<MultipleAppException> mae;
    ConstraintViolationBean cvBean;
    List<ConstraintViolationBean> list;

    // 1. construction pattern: `created from ConstraintViolation` / itemNameKeyClass existence:
    /// `self` (*1)
    mae = ValidationUtil.validateThenReturn(new itemNameKeyClassAnnotationReadTest.No1.Root());
    Assertions.assertTrue(mae.isPresent());
    list = mae.get().getList().stream()
        .map(ex -> ((ValidationAppException) ex).getConstraintViolationBean()).toList();
    for (ConstraintViolationBean bean : list) {
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("Root"))
        assertEqualsItemNameKeyClass("ItemNameKeyClass_Root", bean);
      else if (leafClassName.equals("Child"))
        assertEqualsItemNameKeyClass("ItemNameKeyClass_Child", bean);
      else if (leafClassName.equals("GrandChild"))
        assertEqualsItemNameKeyClass("ItemNameKeyClass_GrandChild", bean);
      else
        throw new RuntimeException();
    }

    /// 2. construction pattern: `created from ConstraintViolation` / itemNameKeyClass
    /// existence: `ancestor`
    mae = ValidationUtil.validateThenReturn(new itemNameKeyClassAnnotationReadTest.No2.Root());
    Assertions.assertTrue(mae.isPresent());
    list = mae.get().getList().stream()
        .map(ex -> ((ValidationAppException) ex).getConstraintViolationBean()).toList();
    for (ConstraintViolationBean bean : list) {
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("Root"))
        assertEqualsItemNameKeyClass("ItemNameKeyClass_Root_Parent", bean);
      else if (leafClassName.equals("Child"))
        assertEqualsItemNameKeyClass("ItemNameKeyClass_Child_Parent", bean);
      else if (leafClassName.equals("GrandChild"))
        assertEqualsItemNameKeyClass("ItemNameKeyClass_GrandChild_Parent", bean);
      else
        throw new RuntimeException();
    }

    // 3. construction pattern: `the other constructor` / itemNameKeyClass existence: `self`
    cvBean = new ConstraintViolationBean(new itemNameKeyClassAnnotationReadTest.No3.Form(), "",
        NOT_EMPTY, "root", "field");
    assertEqualsItemNameKeyClass("ItemNameKeyClass_Root", cvBean);
    cvBean = new ConstraintViolationBean(new itemNameKeyClassAnnotationReadTest.No3.Form(), "",
        NOT_EMPTY, "root", "child.field");
    assertEqualsItemNameKeyClass("ItemNameKeyClass_Child", cvBean);
    cvBean = new ConstraintViolationBean(new itemNameKeyClassAnnotationReadTest.No3.Form(), "",
        NOT_EMPTY, "root", "child.grandChild.field");
    assertEqualsItemNameKeyClass("ItemNameKeyClass_GrandChild", cvBean);

    /// 4. construction pattern: `the other constructor` / itemNameKeyClass existence:`
    /// ancestor` (*1)
    cvBean = new ConstraintViolationBean(new itemNameKeyClassAnnotationReadTest.No4.Form(), "",
        NOT_EMPTY, "root", "field");
    assertEqualsItemNameKeyClass("ItemNameKeyClass_Root_Parent", cvBean);
    cvBean = new ConstraintViolationBean(new itemNameKeyClassAnnotationReadTest.No4.Form(), "",
        NOT_EMPTY, "root", "child.field");
    assertEqualsItemNameKeyClass("ItemNameKeyClass_Child_Parent", cvBean);
    cvBean = new ConstraintViolationBean(new itemNameKeyClassAnnotationReadTest.No4.Form(), "",
        NOT_EMPTY, "root", "child.grandChild.field");
    assertEqualsItemNameKeyClass("ItemNameKeyClass_GrandChild_Parent", cvBean);
  }

  private void assertEqualsItemNameKeyClass(String expected, ConstraintViolationBean bean) {
    Assertions.assertEquals(expected, bean.getFieldInfoBeans()[0].itemNameKey.split("\\.")[0]);
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

      @ConditionalNotEmpty(propertyPath = "field", conditionPropertyPath = "conditionField",
          conditionPattern = ConditionValuePattern.empty)
      @ItemNameKeyClass("ItemNameKeyClass_Child")
      public static class Child extends ChildParent {
        public Integer field = null;
        public String conditionField = null;

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

      @ConditionalNotEmpty(propertyPath = "field", conditionPropertyPath = "conditionField",
          conditionPattern = ConditionValuePattern.empty)
      public static class Child extends ChildParent {
        public Integer field = null;
        public String conditionField = null;

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

    public static class No3 {
      public static class Form {
        @SuppressWarnings("exports")
        @Valid
        public Root root = new Root();
      }

      @ItemNameKeyClass("ItemNameKeyClass_Root")
      public static class Root {
        public String field;

        @SuppressWarnings("exports")
        @Valid
        public Child child = new Child();
      }

      @ItemNameKeyClass("ItemNameKeyClass_Child")
      public static class Child {
        public String field;

        @SuppressWarnings("exports")
        @Valid
        public GrandChild grandChild = new GrandChild();
      }

      @ItemNameKeyClass("ItemNameKeyClass_GrandChild")
      public static class GrandChild {
        public String field;
      }
    }

    public static class No4 {
      public static class Form {
        @SuppressWarnings("exports")
        @Valid
        public Root root = new Root();
      }

      public static class Root extends RootParent {
        public String field;

        @SuppressWarnings("exports")
        @Valid
        public Child child = new Child();
      }

      @ItemNameKeyClass("ItemNameKeyClass_Root_Parent")
      public static class RootParent extends RootGrandParent {
      }

      @ItemNameKeyClass("ItemNameKeyClass_Root_GrandParent")
      public static class RootGrandParent {
      }

      public static class Child extends ChildParent {
        public String field;

        @SuppressWarnings("exports")
        @Valid
        public GrandChild grandChild = new GrandChild();
      }

      @ItemNameKeyClass("ItemNameKeyClass_Child_Parent")
      public static class ChildParent extends ChildGrandParent {
      }

      @ItemNameKeyClass("ItemNameKeyClass_Child_GrandParent")
      public static class ChildGrandParent {
      }

      public static class GrandChild extends GrandChildParent {
        public String field;
      }

      @ItemNameKeyClass("ItemNameKeyClass_GrandChild_Parent")
      public static class GrandChildParent extends GrandChildGrandParent {
      }

      @ItemNameKeyClass("ItemNameKeyClass_GrandChild_GrandParent")
      public static class GrandChildGrandParent {
      }
    }
  }

  //@formatter:off
  /// 
  ///The itemNameKeyClass part of `EclibItem#itemNameKey` overrides `@ItemNameKeyClass`
  /// when explicit itemNameKeyClass is set to `EclibItem`.
  ///
  //@formatter:on
  @Test
  public void itemNameKeyClassAnnotationOverrideTest() {
    Optional<MultipleAppException> mae;
    List<ConstraintViolationBean> list;

    // Record in Form
    mae = ValidationUtil.validateThenReturn(new itemNameKeyClassAnnotationOverrideTest.Form());
    Assertions.assertTrue(mae.isPresent());
    list = mae.get().getList().stream()
        .map(ex -> ((ValidationAppException) ex).getConstraintViolationBean()).toList();
    for (ConstraintViolationBean bean : list) {
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("RootRecord")) {
        if (bean.getFieldInfoBeans()[0].fullPropertyPath.equals("root.field1"))
          assertEqualsItemNameKeyClass("ItemNameKeyClass_InItem_Root", bean);
        else
          assertEqualsItemNameKeyClass("ItemNameKeyClass_Root", bean);

      } else if (leafClassName.equals("ChildRecord")) {
        if (bean.getFieldInfoBeans()[0].fullPropertyPath.equals("root.child.field1"))
          assertEqualsItemNameKeyClass("ItemNameKeyClass_InItem_Child", bean);
        else
          assertEqualsItemNameKeyClass("ItemNameKeyClass_Child", bean);

      } else {
        throw new RuntimeException();
      }
    }

    // Record directly
    mae =
        ValidationUtil.validateThenReturn(new itemNameKeyClassAnnotationOverrideTest.RootRecord());
    Assertions.assertTrue(mae.isPresent());
    list = mae.get().getList().stream()
        .map(ex -> ((ValidationAppException) ex).getConstraintViolationBean()).toList();
    for (ConstraintViolationBean bean : list) {
      String leafClassName = bean.getLeafBean().getClass().getSimpleName();
      if (leafClassName.equals("RootRecord")) {
        if (bean.getFieldInfoBeans()[0].fullPropertyPath.equals("field1"))
          assertEqualsItemNameKeyClass("ItemNameKeyClass_InItem_Root", bean);
        else
          assertEqualsItemNameKeyClass("ItemNameKeyClass_Root", bean);

      } else if (leafClassName.equals("ChildRecord")) {
        if (bean.getFieldInfoBeans()[0].fullPropertyPath.equals("child.field1"))
          assertEqualsItemNameKeyClass("ItemNameKeyClass_InItem_Child", bean);
        else
          assertEqualsItemNameKeyClass("ItemNameKeyClass_Child", bean);

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
    public static class RootRecord implements EclibItemContainer {

      @Override
      public EclibItem[] getItems() {
        return new EclibItem[] {
            new EclibItem("field1").itemNameKey("ItemNameKeyClass_InItem_Root.field"),
            new EclibItem("field2").itemNameKey("field"),
            new EclibItem("child.field1").itemNameKey("ItemNameKeyClass_InItem_Child.field"),
            new EclibItem("child.field2").itemNameKey("field")};
      }

      @NotEmpty
      private String field1;

      @NotEmpty
      private String field2;

      @Valid
      ChildRecord child = new ChildRecord();
    }

    @ItemNameKeyClass("ItemNameKeyClass_Child")
    public static class ChildRecord {

      @NotEmpty
      private String field1;

      @NotEmpty
      private String field2;
    }
  }
}
