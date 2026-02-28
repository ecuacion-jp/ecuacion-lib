package jp.ecuacion.lib.core.jakartavalidation.bean;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
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
  public void irregular() {

    // propertyPath not set
    try {
      validator.validate(new PropertyPathNotSet(null)).size();
      Assertions.fail();

    } catch (Exception ex) {
      Assertions.assertTrue(ex.getCause() instanceof RuntimeException);
      Assertions.assertTrue(ex.getCause().getCause() instanceof NoSuchFieldException);
    }

    // propertyPath contains empty
    try {
      validator.validate(new PropertyPathContainsEmpty(null)).size();
      Assertions.fail();

    } catch (Exception ex) {
      Assertions.assertTrue(ex.getCause() instanceof RuntimeException);
      Assertions.assertTrue(ex.getCause().getCause() instanceof NoSuchFieldException);
    }

    // propertyPath length zero
    try {
      validator.validate(new PropertyPathLengthZero(null)).size();
      Assertions.fail();

    } catch (Exception ex) {
      Assertions.assertTrue(ex.getCause() instanceof ValidationException);
      Assertions.assertEquals("Length of propertyPath is zero.", ex.getCause().getMessage());
    }

    // propertyPath not found
    try {
      validator.validate(new PropertyPathNotFound(null)).size();
      Assertions.fail();

    } catch (Exception ex) {
      Assertions.assertTrue(ex.getCause() instanceof RuntimeException);
      Assertions.assertTrue(ex.getCause().getCause() instanceof NoSuchFieldException);
    }
  }

  @AlwaysFalse(propertyPath = "")
  public static record PropertyPathNotSet(String propertyPath) {

  }

  @AlwaysFalse(propertyPath = {"propertyPath", ""})
  public static record PropertyPathContainsEmpty(String propertyPath) {

  }

  @AlwaysFalse(propertyPath = {})
  public static record PropertyPathLengthZero(String propertyPath) {

  }

  @AlwaysFalse(propertyPath = {"a"})
  public static record PropertyPathNotFound(String propertyPath) {

  }
}
