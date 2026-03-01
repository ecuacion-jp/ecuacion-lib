package jp.ecuacion.lib.validation.constraints;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.PropertyFileUtil;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NotEmptyWhenTest {

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void beforeAll() {
    PropertyFileUtil.addResourceBundlePostfix("lib-validation-test");
  }

  @Test
  public void validationOnConditionIsTrueTest() {
    // String
    // null
    Assertions.assertEquals(1, validator.validate(new StrBean(null, null)).size());
    // empty
    Assertions.assertEquals(1, validator.validate(new StrBean(null, null)).size());
    // non-empty value
    Assertions.assertEquals(0, validator.validate(new StrBean("a", null)).size());

    // non-String (Integer)
    // null value
    Assertions.assertEquals(1, validator.validate(new IntBean(null, null)).size());
    // non-null value
    Assertions.assertEquals(0, validator.validate(new IntBean(0, null)).size());
  }

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY)
  public static record StrBean(String value, String condValue) {

  }

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY)
  public static record IntBean(Integer value, String condValue) {

  }

  /**
   * Tests of messages of condition part is done in ConditionalValidatorTest.
   * This method tests NotEmptyWhen dependent part only.
   */
  @Test
  public void messagePatternTest() {
    // EMPTY, EQUAL
    // English
    String msg =
        ExceptionUtil.getMessageList(validator.validate(new EmptyEqualToBean(null, null))).get(0);
    Assertions.assertEquals("condition valueが 空欄の場合は空欄以外である必要があります", msg);
  }

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY)
  public static record EmptyEqualToBean(String value, String condValue) {

  }
}
