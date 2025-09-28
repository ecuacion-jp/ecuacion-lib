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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.SingleAppException;
import jp.ecuacion.lib.core.exception.checked.ValidationAppException;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.jakartavalidation.validator.ConditionalNotEmpty;
import jp.ecuacion.lib.core.jakartavalidation.validator.ItemNameKeyClass;
import jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionValuePattern;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireNonNullException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ValidationUtilTest {

  private static final String NOT_NULL = "jakarta.validation.constraints.NotNull";

  @BeforeEach
  public void before() {}

  @Test
  public void validateThenReturn_args_object_normal() {

    // object is null (Causes NPE)
    try {
      ValidationUtil.validateThenReturn(null);
      Assertions.fail();

    } catch (RequireNonNullException npe) {
    }

    // ordinal error occurred (Tests that error created)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_ObjWithNormalValidator())
        .ifPresent(mae -> {
          Assertions.assertEquals(2, mae.getList().size());
          ValidationAppException exNotNull = null;
          ValidationAppException exMin = null;
          for (SingleAppException singleEx : mae.getList()) {
            ValidationAppException bvEx = (ValidationAppException) singleEx;
            ConstraintViolationBean bean = bvEx.getConstraintViolationBean();
            if (bean.getMessageId().equals(NOT_NULL)) {
              exNotNull = bvEx;

            } else if (bean.getMessageId().equals("jakarta.validation.constraints.Min")) {
              exMin = bvEx;
            }
          }

          Assertions.assertFalse(exNotNull == null);
          Assertions.assertFalse(exMin == null);
        });
  }

  @Test
  public void validateThenReturn_args_object_itemNameKeysTest() {

    // Normal classes

    // normal without form : <className>.<fieldname> (Manipulated so by ConstraintViolationBean)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_ObjWithNormalValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("ValidationUtilTest_ObjWithNormalValidator.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_DirectContainerWithNormalValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("normal.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_IndirectContainerWithNormalValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("normal.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // classValidator without form : <className>.<fieldname>
    // (Manipulated so by ConstraintViolationBean)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_ObjWithClassValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("ValidationUtilTest_ObjWithClassValidator.value", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_DirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("classValidator.value", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_IndirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("classValidator.value", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // internal static classes

    // normal without form : <className>.<fieldname> (Manipulated so by ConstraintViolationBean)
    ValidationUtil.validateThenReturn(new NoItemNameKeyClass.ObjWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("ObjWithFieldValidator.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new NoItemNameKeyClass.DirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("normal.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new NoItemNameKeyClass.IndirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("normal.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // classValidator without form : <className>.<fieldname>
    // (Manipulated so by ConstraintViolationBean)
    ValidationUtil.validateThenReturn(new NoItemNameKeyClass.ObjWithClassValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("ObjWithClassValidator.value", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new NoItemNameKeyClass.DirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("classValidator.value", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new NoItemNameKeyClass.IndirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("classValidator.value", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // @ItemNameKeyClass at field (field validator only)

    ValidationUtil.validateThenReturn(new ItemNameKeyClassAtField.ObjWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    ValidationUtil.validateThenReturn(new ItemNameKeyClassAtField.DirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    ValidationUtil.validateThenReturn(new ItemNameKeyClassAtField.IndirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // @ItemNameKeyClass at class

    // ObjWithFieldValidator
    ValidationUtil.validateThenReturn(new ItemNameKeyClassAtClass.ObjWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // DirectContainerWithFieldValidator
    ValidationUtil.validateThenReturn(new ItemNameKeyClassAtClass.DirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // IndirectContainerWithFieldValidator
    ValidationUtil.validateThenReturn(new ItemNameKeyClassAtClass.IndirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // ObjWithClassValidator
    ValidationUtil.validateThenReturn(new ItemNameKeyClassAtClass.ObjWithClassValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.value", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // DirectContainerWithClassValidadtor
    ValidationUtil.validateThenReturn(new ItemNameKeyClassAtClass.DirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.value", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // IndirectContainerWithClassValidadtor
    ValidationUtil.validateThenReturn(new ItemNameKeyClassAtClass.IndirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.value", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // @ItemNameKeyClass at ancestor class

    // ObjWithFieldValidator
    ValidationUtil.validateThenReturn(new ItemNameKeyClassAtAncestorClass.ObjWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // DirectContainerWithFieldValidator
    ValidationUtil
        .validateThenReturn(new ItemNameKeyClassAtAncestorClass.DirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // IndirectContainerWithFieldValidator
    ValidationUtil
        .validateThenReturn(new ItemNameKeyClassAtAncestorClass.IndirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = getItemNameKeyForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.str1", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // ObjWithClassValidator
    ValidationUtil.validateThenReturn(new ItemNameKeyClassAtAncestorClass.ObjWithClassValidator())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.value", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // DirectContainerWithClassValidadtor
    ValidationUtil
        .validateThenReturn(new ItemNameKeyClassAtAncestorClass.DirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.value", itemNameKeys[0]);

        }, () -> Assertions.fail());

    // IndirectContainerWithClassValidadtor
    ValidationUtil
        .validateThenReturn(new ItemNameKeyClassAtAncestorClass.IndirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemNameKeys();
          Assertions.assertTrue(itemNameKeys.length == 1);
          Assertions.assertEquals("itemNameKeyClass.value", itemNameKeys[0]);

        }, () -> Assertions.fail());
  }

  private String[] getItemNameKeyForNotNullExceptionBean(MultipleAppException mae) {
    return ((ValidationAppException) mae.getList().stream()
        .filter(ex -> ((ValidationAppException) ex).getConstraintViolationBean().getMessageId()
            .equals(NOT_NULL))
        .toList().get(0)).getConstraintViolationBean().getItemNameKeys();
  }


  @Test
  public void validateThenReturn_args_object_PropertyPathHasBeanInstanceTest() {
    ValidationUtil
    .validateThenReturn(new PropertyPathHasBeanInstance.ObjWithClassValidator())
    .ifPresentOrElse(mae -> {
      String[] itemNameKeys = ((ValidationAppException) mae.getList().get(0))
          .getConstraintViolationBean().getItemNameKeys();
      Assertions.assertTrue(itemNameKeys.length == 1);
      Assertions.assertEquals("itemNameKeyClass.value", itemNameKeys[0]);

    }, () -> Assertions.fail());

  }
  
  // No @ItemNameKeyClass

  public static class NoItemNameKeyClass {

    public static class ObjWithFieldValidator {
      @NotNull
      public String str1 = null;

      @Min(3)
      public int int1 = 2;
    }

    @ConditionalNotEmpty(propertyPath = "value", conditionPropertyPath = "conditionValue",
        conditionPattern = ConditionValuePattern.string,
        conditionValueString = "abc")
    public static class ObjWithClassValidator {
      public String conditionValue = "abc";
      public String value = null;
    }

    public static class DirectContainerWithFieldValidator {
      @Valid
      public ObjWithFieldValidator normal = new ObjWithFieldValidator();
    }

    public static class DirectContainerWithClassValidadtor {
      @Valid
      public ObjWithClassValidator classValidator = new ObjWithClassValidator();
    }

    public static class IndirectContainerWithFieldValidator {
      @Valid
      public DirectContainerWithFieldValidator directContainer =
          new DirectContainerWithFieldValidator();
    }

    public static class IndirectContainerWithClassValidadtor {
      @Valid
      public DirectContainerWithClassValidadtor directContainer =
          new DirectContainerWithClassValidadtor();
    }
  }

  // @ItemNameKeyClass at field

  public static class ItemNameKeyClassAtField {

    public static class ObjWithFieldValidator {
      @ItemNameKeyClass("itemNameKeyClass")
      @NotNull
      public String str1 = null;

      @ItemNameKeyClass("itemNameKeyClass")
      @Min(3)
      public int int1 = 2;
    }

    public static class DirectContainerWithFieldValidator {
      @Valid
      public ObjWithFieldValidator normal = new ObjWithFieldValidator();
    }

    public static class IndirectContainerWithFieldValidator {
      @Valid
      public DirectContainerWithFieldValidator directContainer =
          new DirectContainerWithFieldValidator();
    }
  }

  // @ItemNameKeyClass at class

  public static class ItemNameKeyClassAtClass {

    @ItemNameKeyClass("itemNameKeyClass")
    public static class ObjWithFieldValidator {
      @NotNull
      public String str1 = null;

      @Min(3)
      public int int1 = 2;
    }

    @ItemNameKeyClass("itemNameKeyClass")
    @ConditionalNotEmpty(propertyPath = "value", conditionPropertyPath = "conditionValue",
        conditionPattern = ConditionValuePattern.string,
        conditionValueString = "abc")
    public static class ObjWithClassValidator {
      public String conditionValue = "abc";
      public String value = null;
    }

    public static class DirectContainerWithFieldValidator {
      @Valid
      public ObjWithFieldValidator normal = new ObjWithFieldValidator();
    }

    public static class DirectContainerWithClassValidadtor {
      @Valid
      public ObjWithClassValidator classValidator = new ObjWithClassValidator();
    }

    public static class IndirectContainerWithFieldValidator {
      @Valid
      public DirectContainerWithFieldValidator directContainer =
          new DirectContainerWithFieldValidator();
    }

    public static class IndirectContainerWithClassValidadtor {
      @Valid
      public DirectContainerWithClassValidadtor directContainer =
          new DirectContainerWithClassValidadtor();
    }
  }

  // @ItemNameKeyClass at ancestor class

  public static class ItemNameKeyClassAtAncestorClass {

    @ItemNameKeyClass("itemNameKeyClass")
    public static class GrandParent {

    }

    public static class Parent extends GrandParent {

    }

    public static class ObjWithFieldValidator extends Parent {
      @NotNull
      public String str1 = null;

      @Min(3)
      public int int1 = 2;
    }

    @ConditionalNotEmpty(propertyPath = "value", conditionPropertyPath = "conditionValue",
        conditionPattern = ConditionValuePattern.string,
        conditionValueString = "abc")
    public static class ObjWithClassValidator extends Parent {
      public String conditionValue = "abc";
      public String value = null;
    }

    public static class DirectContainerWithFieldValidator {
      @Valid
      public ObjWithFieldValidator normal = new ObjWithFieldValidator();
    }

    public static class DirectContainerWithClassValidadtor {
      @Valid
      public ObjWithClassValidator classValidator = new ObjWithClassValidator();
    }

    public static class IndirectContainerWithFieldValidator {
      @Valid
      public DirectContainerWithFieldValidator directContainer =
          new DirectContainerWithFieldValidator();
    }

    public static class IndirectContainerWithClassValidadtor {
      @Valid
      public DirectContainerWithClassValidadtor directContainer =
          new DirectContainerWithClassValidadtor();
    }
  }
  
  // propertyPath has bean instance

  public static class PropertyPathHasBeanInstance {

    @ItemNameKeyClass("itemNameKeyClass")
    public static class GrandParentOfTargetBean {

    }

    public static class ParentTargetBean extends GrandParentOfTargetBean {

    }

    @ConditionalNotEmpty(propertyPath = "child.value", conditionPropertyPath = "child.conditionValue",
        conditionPattern = ConditionValuePattern.valueOfPropertyPath,
        conditionValuePropertyPath = "child.conditionValuePropertyPath")
    public static class ObjWithClassValidator {
      ChildObj child = new ChildObj();
    }

    public static class ChildObj extends ParentTargetBean {
      public String value = null;
      public String conditionValue = "abc";
      public String conditionValuePropertyPath = "abc";
    }
  }
}
