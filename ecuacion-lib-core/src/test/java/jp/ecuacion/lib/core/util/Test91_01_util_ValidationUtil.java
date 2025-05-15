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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Test91_01_util_ValidationUtil {

  private static final String NOT_NULL = "jakarta.validation.constraints.NotNull";

  @BeforeEach
  public void before() {}

  @Test
  public void validateThenReturn_args_object_normal() {

    // object is null (Causes NPE)
    try {
      ValidationUtil.validateThenReturn(null);
      Assertions.fail();

    } catch (NullPointerException npe) {
    }

    // ordinal error occurred (Tests that error created)
    ValidationUtil.validateThenReturn(new Test91_01__ObjWithNormalValidator()).ifPresent(mae -> {
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
    ValidationUtil.validateThenReturn(new Test91_01__ObjWithNormalValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("Test91_01__ObjWithNormalValidator.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new Test91_01__DirectContainerWithNormalValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("normal.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new Test91_01__IndirectContainerWithNormalValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("normal.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // classValidator without form : <className>.<fieldname>
    // (Manipulated so by ConstraintViolationBean)
    ValidationUtil.validateThenReturn(new Test91_01__ObjWithClassValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("Test91_01__ObjWithClassValidator.value", itemIds[0]);

        }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new Test91_01__DirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("classValidator.value", itemIds[0]);

        }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new Test91_01__IndirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("classValidator.value", itemIds[0]);

        }, () -> Assertions.fail());

    // internal static classes

    // normal without form : <className>.<fieldname> (Manipulated so by ConstraintViolationBean)
    ValidationUtil.validateThenReturn(new TestObjWithNormalValidator()).ifPresentOrElse(mae -> {
      String[] itemIds = getItemIdForNotNullExceptionBean(mae);
      Assertions.assertTrue(itemIds.length == 1);
      Assertions.assertEquals("TestObjWithNormalValidator.str1", itemIds[0]);

    }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new TestDirectContainerWithNormalValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("normal.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // normal with form : <fieldNameInForm>.<fieldName> (which is a standard pattern for splib-web)
    ValidationUtil.validateThenReturn(new TestIndirectContainerWithNormalValidator())
        .ifPresentOrElse(mae -> {
          String[] itemIds = getItemIdForNotNullExceptionBean(mae);
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("normal.str1", itemIds[0]);

        }, () -> Assertions.fail());

    // classValidator without form : <className>.<fieldname>
    // (Manipulated so by ConstraintViolationBean)
    ValidationUtil.validateThenReturn(new TestObjWithClassValidator()).ifPresentOrElse(mae -> {
      String[] itemIds =
          ((ValidationAppException) mae.getList().get(0)).getConstraintViolationBean().getItemIds();
      Assertions.assertTrue(itemIds.length == 1);
      Assertions.assertEquals("TestObjWithClassValidator.value", itemIds[0]);

    }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new TestDirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("classValidator.value", itemIds[0]);

        }, () -> Assertions.fail());

    // classValidator with form :
    // <fieldNameInForm>.<fieldName> (which is a standard pattern forsplib-web)
    ValidationUtil.validateThenReturn(new TestIndirectContainerWithClassValidadtor())
        .ifPresentOrElse(mae -> {
          String[] itemIds = ((ValidationAppException) mae.getList().get(0))
              .getConstraintViolationBean().getItemIds();
          Assertions.assertTrue(itemIds.length == 1);
          Assertions.assertEquals("classValidator.value", itemIds[0]);

        }, () -> Assertions.fail());
    
    
  }

  private String[] getItemIdForNotNullExceptionBean(MultipleAppException mae) {
    return ((ValidationAppException) mae.getList().stream()
        .filter(ex -> ((ValidationAppException) ex).getConstraintViolationBean().getMessageId()
            .equals(NOT_NULL))
        .toList().get(0)).getConstraintViolationBean().getItemIds();
  }

  public static class TestObjWithNormalValidator {
    @NotNull
    public String str1 = null;

    @Min(3)
    public int int1 = 2;
  }

  @ConditionalNotEmpty(field = "value", conditionField = "conditionValue", conditionValue = "abc")
  public static class TestObjWithClassValidator {
    public String conditionValue = "abc";
    public String value = null;
  }

  public static class TestDirectContainerWithNormalValidator {
    @Valid
    public TestObjWithNormalValidator normal = new TestObjWithNormalValidator();
  }

  public static class TestDirectContainerWithClassValidadtor {
    @Valid
    public TestObjWithClassValidator classValidator = new TestObjWithClassValidator();
  }

  public static class TestIndirectContainerWithNormalValidator {
    @Valid
    public TestDirectContainerWithNormalValidator directContainer =
        new TestDirectContainerWithNormalValidator();
  }

  public class TestIndirectContainerWithClassValidadtor {
    @Valid
    public TestDirectContainerWithClassValidadtor directContainer =
        new TestDirectContainerWithClassValidadtor();
  }

}
