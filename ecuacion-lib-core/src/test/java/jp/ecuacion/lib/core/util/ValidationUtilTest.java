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
import jp.ecuacion.lib.core.jakartavalidation.validator.ItemIdClass;
import jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern;
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
  public void validateThenReturn_args_object_itemIdsTest() {

    // Normal classes

    // normal without form : <className>.<fieldname> (Manipulated so by ConstraintViolationBean)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_ObjWithNormalValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("ValidationUtilTest_ObjWithNormalValidator.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_DirectContainerWithNormalValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("normal.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_IndirectContainerWithNormalValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("normal.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // classValidator without form : <className>.<fieldname>
    // (Manipulated so by ConstraintViolationBean)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_ObjWithClassValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("ValidationUtilTest_ObjWithClassValidator.value", itemIds[0]);

        }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_DirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("classValidator.value", itemIds[0]);

        }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new ValidationUtilTest_IndirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("classValidator.value", itemIds[0]);

        }, () -> Assertions.fail());

    // internal static classes

    // normal without form : <className>.<fieldname> (Manipulated so by ConstraintViolationBean)
    ValidationUtil.validateThenReturn(new NoItemIdClass.ObjWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("ObjWithFieldValidator.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new NoItemIdClass.DirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("normal.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new NoItemIdClass.IndirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("normal.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // classValidator without form : <className>.<fieldname>
    // (Manipulated so by ConstraintViolationBean)
    ValidationUtil.validateThenReturn(new NoItemIdClass.ObjWithClassValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("ObjWithClassValidator.value", itemIds[0]);

        }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new NoItemIdClass.DirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("classValidator.value", itemIds[0]);

        }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new NoItemIdClass.IndirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("classValidator.value", itemIds[0]);

        }, () -> Assertions.fail());

    // @ItemIdClass at field (field validator only)

    ValidationUtil.validateThenReturn(new ItemIdClassAtField.ObjWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.str1", itemIds[0]);

        }, () -> Assertions.fail());

    ValidationUtil.validateThenReturn(new ItemIdClassAtField.DirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.str1", itemIds[0]);

        }, () -> Assertions.fail());

    ValidationUtil.validateThenReturn(new ItemIdClassAtField.IndirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // @ItemIdClass at class

    // ObjWithFieldValidator
    ValidationUtil.validateThenReturn(new ItemIdClassAtClass.ObjWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // DirectContainerWithFieldValidator
    ValidationUtil.validateThenReturn(new ItemIdClassAtClass.DirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // IndirectContainerWithFieldValidator
    ValidationUtil.validateThenReturn(new ItemIdClassAtClass.IndirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // ObjWithClassValidator
    ValidationUtil.validateThenReturn(new ItemIdClassAtClass.ObjWithClassValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.value", itemIds[0]);

        }, () -> Assertions.fail());

    // DirectContainerWithClassValidadtor
    ValidationUtil.validateThenReturn(new ItemIdClassAtClass.DirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.value", itemIds[0]);

        }, () -> Assertions.fail());

    // IndirectContainerWithClassValidadtor
    ValidationUtil.validateThenReturn(new ItemIdClassAtClass.IndirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.value", itemIds[0]);

        }, () -> Assertions.fail());

    // @ItemIdClass at ancestor class

    // ObjWithFieldValidator
    ValidationUtil.validateThenReturn(new ItemIdClassAtAncestorClass.ObjWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // DirectContainerWithFieldValidator
    ValidationUtil
        .validateThenReturn(new ItemIdClassAtAncestorClass.DirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // IndirectContainerWithFieldValidator
    ValidationUtil
        .validateThenReturn(new ItemIdClassAtAncestorClass.IndirectContainerWithFieldValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // ObjWithClassValidator
    ValidationUtil.validateThenReturn(new ItemIdClassAtAncestorClass.ObjWithClassValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.value", itemIds[0]);

        }, () -> Assertions.fail());

    // DirectContainerWithClassValidadtor
    ValidationUtil
        .validateThenReturn(new ItemIdClassAtAncestorClass.DirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.value", itemIds[0]);

        }, () -> Assertions.fail());

    // IndirectContainerWithClassValidadtor
    ValidationUtil
        .validateThenReturn(new ItemIdClassAtAncestorClass.IndirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("itemIdClass.value", itemIds[0]);

        }, () -> Assertions.fail());
  }

  private String[] getItemIdForNotNullExceptionBean(MultipleAppException mae) {
    return ((ValidationAppException) mae.getList().stream()
        .filter(ex -> ((ValidationAppException) ex).getConstraintViolationBean().getMessageId()
            .equals(NOT_NULL))
        .toList().get(0)).getConstraintViolationBean().getItemIds();
  }


  @Test
  public void validateThenReturn_args_object_PropertyPathHasBeanInstanceTest() {
    ValidationUtil
    .validateThenReturn(new PropertyPathHasBeanInstance.ObjWithClassValidator())
    .ifPresentOrElse(mae -> {
      String[] itemIds = ((ValidationAppException) mae.getList().get(0))
          .getConstraintViolationBean().getItemIds();
      Assertions.assertTrue(itemIds.length == 1);
      Assertions.assertEquals("itemIdClass.value", itemIds[0]);

    }, () -> Assertions.fail());

  }
  
  // No @ItemIdClasas

  public static class NoItemIdClass {

    public static class ObjWithFieldValidator {
      @NotNull
      public String str1 = null;

      @Min(3)
      public int int1 = 2;
    }

    @ConditionalNotEmpty(propertyPath = "value", conditionPropertyPath = "conditionValue",
        conditionPattern = ConditionPattern.stringValueOfConditionPropertyPathIsEqualTo,
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

  // @ItemIdClass at field

  public static class ItemIdClassAtField {

    public static class ObjWithFieldValidator {
      @ItemIdClass("itemIdClass")
      @NotNull
      public String str1 = null;

      @ItemIdClass("itemIdClass")
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

  // @ItemIdClass at class

  public static class ItemIdClassAtClass {

    @ItemIdClass("itemIdClass")
    public static class ObjWithFieldValidator {
      @NotNull
      public String str1 = null;

      @Min(3)
      public int int1 = 2;
    }

    @ItemIdClass("itemIdClass")
    @ConditionalNotEmpty(propertyPath = "value", conditionPropertyPath = "conditionValue",
        conditionPattern = ConditionPattern.stringValueOfConditionPropertyPathIsEqualTo,
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

  // @ItemIdClass at ancestor class

  public static class ItemIdClassAtAncestorClass {

    @ItemIdClass("itemIdClass")
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
        conditionPattern = ConditionPattern.stringValueOfConditionPropertyPathIsEqualTo,
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

    @ItemIdClass("itemIdClass")
    public static class GrandParentOfTargetBean {

    }

    public static class ParentTargetBean extends GrandParentOfTargetBean {

    }

    @ConditionalNotEmpty(propertyPath = "child.value", conditionPropertyPath = "child.conditionValue",
        conditionPattern = ConditionPattern.valueOfConditionPropertyPathIsEqualToValueOf,
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
