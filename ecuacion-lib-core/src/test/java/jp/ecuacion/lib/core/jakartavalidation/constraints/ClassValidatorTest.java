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

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests ClassValidator.
 * 
 * <p>Tests for {0} messages is executed in {@code ExceptionUtil}.</p>
 */
public class ClassValidatorTest {

  @BeforeAll
  public static void beforeAll() {}

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  /**
   * Tests irregulars on propertyPath.
   */
  @Test
  @SuppressWarnings("unused")
  public void irregular() {

    // propertyPath not set
    try {
      var unused = validator.validate(new PropertyPathNotSet(null)).size();
      Assertions.fail();

    } catch (Exception ex) {

      Assertions.assertTrue(ex.getCause() instanceof RuntimeException);
      Assertions.assertTrue(
          Objects.requireNonNull(ex.getCause()).getCause() instanceof NoSuchFieldException);
    }

    // propertyPath contains empty
    try {
      var unused = validator.validate(new PropertyPathContainsEmpty(null)).size();
      Assertions.fail();

    } catch (Exception ex) {
      Assertions.assertTrue(ex.getCause() instanceof RuntimeException);
      Assertions.assertTrue(
          Objects.requireNonNull(ex.getCause()).getCause() instanceof NoSuchFieldException);
    }

    // propertyPath length zero
    try {
      var unused = validator.validate(new PropertyPathLengthZero(null)).size();
      Assertions.fail();

    } catch (Exception ex) {
      Assertions.assertTrue(ex.getCause() instanceof ValidationException);
      Assertions.assertEquals("Length of propertyPath is zero.",
          Objects.requireNonNull(ex.getCause()).getMessage());
    }

    // propertyPath not found
    try {
      var unused = validator.validate(new PropertyPathNotFound(null)).size();
      Assertions.fail();

    } catch (Exception ex) {
      Assertions.assertTrue(ex.getCause() instanceof RuntimeException);
      Assertions.assertTrue(
          Objects.requireNonNull(ex.getCause()).getCause() instanceof NoSuchFieldException);
    }
  }

  @ClassAlwaysFalse(propertyPath = "")
  public static record PropertyPathNotSet(@Nullable String propertyPath) {

  }

  @ClassAlwaysFalse(propertyPath = {"propertyPath", ""})
  public static record PropertyPathContainsEmpty(@Nullable String propertyPath) {

  }

  @ClassAlwaysFalse(propertyPath = {})
  public static record PropertyPathLengthZero(@Nullable String propertyPath) {

  }

  @ClassAlwaysFalse(propertyPath = {"a"})
  public static record PropertyPathNotFound(@Nullable String propertyPath) {

  }
}
