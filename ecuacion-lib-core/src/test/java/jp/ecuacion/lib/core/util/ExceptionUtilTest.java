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

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import jp.ecuacion.lib.core.jakartavalidation.annotation.PlacedAtClass;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ExceptionUtilTest {

  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void before() {
    PropertyFileUtil.addResourceBundlePostfix("test");
  }

  @Test
  public void getMessageList_propertyPathLabelNameDisplayTest() {
    // field validator
    // Italian (parenthesis not blank)
    String message = ExceptionUtil
        .getMessageList(validator.validate(new FieldValidator(null)), Locale.ITALIAN).get(0);
    Assertions.assertEquals("「nome」 è obbligatorio", message);
    // German (parenthesis blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new FieldValidator(null)), Locale.GERMAN).get(0);
    Assertions.assertEquals("name ist erforderlich", message);

    // class validator with 1 field
    // Italian (parenthesis not blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator1(null)), Locale.ITALIAN).get(0);
    Assertions.assertEquals("「classValidator1.str1」 è messaggio di esempio", message);
    // German (parenthesis blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator1(null)), Locale.GERMAN).get(0);
    Assertions.assertEquals("classValidator1.str1 ist Beispielnachricht", message);

    // class validator with multiple fields
    // Italian (parenthesis not blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator2(null, null)), Locale.ITALIAN).get(0);
    Assertions.assertEquals("「classValidator2.str1」、「classValidator2.str2」 è messaggio di esempio", message);
    // German (parenthesis blank)
    message = ExceptionUtil
        .getMessageList(validator.validate(new ClassValidator2(null, null)), Locale.GERMAN).get(0);
    Assertions.assertEquals("classValidator2.str1, classValidator2.str2 ist Beispielnachricht", message);
  }

  public static record FieldValidator(@NotNull String str1) {
  }

  @SampleClassValidator(propertyPath = "str1")
  public static record ClassValidator1(String str1) {
  }

  @SampleClassValidator(propertyPath = {"str1", "str2"})
  public static record ClassValidator2(String str1, String str2) {
  }


  @PlacedAtClass
  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @Constraint(validatedBy = {SampleClassValidatorLogic.class})
  static @interface SampleClassValidator {

    String[] propertyPath();
    String message() default "{jp.ecuacion.lib.validation.constraints.SampleClassValidator.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
  }

  public static class SampleClassValidatorLogic extends ClassValidator
      implements ConstraintValidator<SampleClassValidator, Object> {

    @Override
    public void initialize(@SuppressWarnings("exports") SampleClassValidator annotation) {
      super.initialize(annotation.propertyPath());
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
      return false;
    }
  }
}
