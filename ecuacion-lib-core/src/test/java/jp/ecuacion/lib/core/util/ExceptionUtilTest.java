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
import java.util.List;
import java.util.Locale;
import jp.ecuacion.lib.core.jakartavalidation.bean.AlwaysFalse;
import jp.ecuacion.lib.core.util.ExceptionUtilTest.VariousPlaces.Child;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ExceptionUtilTest {

  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void before() {
    PropertyFileUtil.addResourceBundlePostfix("lib-core-test");
  }

  @Test
  public void getMessageList_propertyPathLabelNameDisplayTest() {
    // field validator
    // Italian (parenthesis not blank)
    String message = ExceptionUtil
        .getMessageList(validator.validate(new FieldValidator(null)), Locale.ITALIAN).get(0);
    Assertions.assertEquals("'nome' è obbligatorio", message);
    // German (parenthesis blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new FieldValidator(null)), Locale.GERMAN).get(0);
    Assertions.assertEquals("name ist erforderlich", message);

    // class validator with 1 field
    // Italian (parenthesis not blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator1(null)), Locale.ITALIAN).get(0);
    Assertions.assertEquals("'classValidator1.str1' è messaggio di esempio", message);
    // German (parenthesis blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator1(null)), Locale.GERMAN).get(0);
    Assertions.assertEquals("classValidator1.str1 ist Beispielnachricht", message);

    // class validator with multiple fields
    // Italian (parenthesis not blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator2(null, null)), Locale.ITALIAN).get(0);
    Assertions.assertEquals(
        "'classValidator2.str1' | 'classValidator2.str2' è messaggio di esempio", message);
    // German (parenthesis blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator2(null, null)), Locale.GERMAN).get(0);
    Assertions.assertEquals("classValidator2.str1, classValidator2.str2 ist Beispielnachricht",
        message);
  }

  public static record FieldValidator(@NotNull String str1) {
  }

  @AlwaysFalse(propertyPath = "str1")
  public static record ClassValidator1(String str1) {
  }

  @AlwaysFalse(propertyPath = {"str1", "str2"})
  public static record ClassValidator2(String str1, String str2) {
  }

  private String getMsg(Object obj) {
    return ExceptionUtil.getMessageList(validator.validate(obj), Locale.ENGLISH, true).get(0);
  }

  @Test
  public void getMessageList_variousPlacesTest() {
    String message = null;

    // normal
    message = getMsg(new VariousPlaces.Normal(null));
    Assertions.assertEquals("'normal.name' must not be null.", message);
    // inside child node
    message = getMsg(new VariousPlaces.InsideChildNode(new Child(null)));
    Assertions.assertEquals("'child.name' must not be null.", message);
    // inside child node in list
    message = getMsg(
        new VariousPlaces.InsideChildNodeInList(Arrays.asList(new Child[] {new Child(null)})));
    Assertions.assertEquals("'child.name' must not be null.", message);
    // anonymous class
    message = getMsg(new VariousPlaces.AnonymousClass());
    Assertions.assertEquals("'childIf.name' must not be null.", message);
  }

  public static class VariousPlaces {
    public static record Normal(@NotNull String name) {
    }
    public static record InsideChildNode(@Valid Child myChild) {
    }
    public static record InsideChildNodeInList(List<@Valid Object> myChildList) {
    }
    public static class AnonymousClass {
      @Valid
      public ChildIf myChild = new ChildIf() {
        @NotNull
        private String name;
      };
    }


    public static record Child(@NotNull String name) {
    }
    public static interface ChildIf {
    };
  }
}
