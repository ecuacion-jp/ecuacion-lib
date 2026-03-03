package jp.ecuacion.lib.validation.constraints;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Locale;
import jp.ecuacion.lib.core.jakartavalidation.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.PropertyFileUtil;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
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

  private String getMessage(Object object) {
    return ExceptionUtil.getMessageList(validator.validate(object), Locale.ENGLISH).get(0);
  }

  /**
   * Tests of messages of condition part is done in ValidateWhenTest.
   * This method tests NotEmptyWhen dependent part only.
   */
  @Test
  public void messagePatternTest() {
    String msg;
    String prefix1 = "needs to be not empty when ";
    String prefix2 = prefix1 + "'condition value' ";

    msg = getMessage(new PropertyPathEqualToWithDisplayStringMulBean(null, "abc",
        new String[] {"abc", "def"}, new String[] {"some value 1", "some value 2"}));
    Assertions.assertEquals(prefix2 + "is one of some value 1, some value 2.", msg);

    // EMPTY, EQUAL_TO
    msg = getMessage(new EmptyEqualToBean(null, null));
    Assertions.assertEquals(prefix2 + "is empty.", msg);
    // EMPTY, NOT_EQUAL_TO
    msg = getMessage(new NotEmptyEqualToBean(null, null));
    Assertions.assertEquals(prefix2 + "is not empty.", msg);
    // NOT_EMPTY, EQUAL_TO
    msg = getMessage(new EmptyNotEqualToBean(null, "a"));
    Assertions.assertEquals(prefix2 + "is not empty.", msg);
    // NOT_EMPTY, NOT_EQUAL_TO
    msg = getMessage(new NotEmptyNotEqualToBean(null, "a"));
    Assertions.assertEquals(prefix2 + "is empty.", msg);
    // TRUE, EQUAL_TO
    msg = getMessage(new TrueEqualToBean(null, true));
    Assertions.assertEquals(prefix2 + "is ON.", msg);
    // TRUE, NOT_EQUAL_TO
    msg = getMessage(new TrueNotEqualToBean(null, false));
    Assertions.assertEquals(prefix2 + "is not ON.", msg);
    // FALSE, EQUAL_TO
    msg = getMessage(new FalseEqualToBean(null, false));
    Assertions.assertEquals(prefix2 + "is OFF.", msg);
    // FALSE, NOT_EQUAL_TO
    msg = getMessage(new FalseNotEqualToBean(null, true));
    Assertions.assertEquals(prefix2 + "is not OFF.", msg);
    // STRING, EQUAL_TO, single
    msg = getMessage(new StringEqualToSingleBean(null, "test"));
    Assertions.assertEquals(prefix2 + "is test.", msg);
    // STRING, EQUAL_TO, multiple
    msg = getMessage(new StringEqualToMultipleBean(null, "test1"));
    Assertions.assertEquals(prefix2 + "is one of test1, test2.", msg);
    // STRING, NOT_EQUAL_TO, single
    msg = getMessage(new StringNotEqualToSingleBean(null, "a"));
    Assertions.assertEquals(prefix2 + "is not test.", msg);
    // STRING, NOT_EQUAL_TO, multiple
    msg = getMessage(new StringNotEqualToMultipleBean(null, "a"));
    Assertions.assertEquals(prefix2 + "is not one of test1, test2.", msg);
    // PATTERN, EQUAL_TO, no description
    msg = getMessage(new PatternEqualToBean(null, "test"));
    Assertions.assertEquals(prefix2 + "matches the pattern: .*test.*.", msg);
    // PATTERN, EQUAL_TO, description which is not defined in item_names
    msg = getMessage(new PatternEqualToWithDescNonResolvedBean(null, "test"));
    Assertions.assertEquals(prefix2 + "matches the pattern: notExistBean.test.", msg);
    // PATTERN, EQUAL_TO, description which is defined in item_names
    msg = getMessage(new PatternEqualToWithDescResolvedBean(null, "test"));
    Assertions.assertEquals(prefix2 + "matches the pattern: the string which contains 'test'.",
        msg);
    // PATTERN, NOT_EQUAL_TO, no description
    msg = getMessage(new PatternNotEqualToBean(null, "a"));
    Assertions.assertEquals(prefix2 + "does not match the pattern: .*test.*.", msg);
    // PATTERN, NOT_EQUAL_TO, description which is not defined in item_names
    msg = getMessage(new PatternNotEqualToWithDescNonResolvedBean(null, "a"));
    Assertions.assertEquals(prefix2 + "does not match the pattern: notExistBean.test.", msg);
    // PATTERN, NOT_EQUAL_TO, description which is defined in item_names
    msg = getMessage(new PatternNotEqualToWithDescResolvedBean(null, "a"));
    Assertions.assertEquals(
        prefix2 + "does not match the pattern: the string which contains 'test'.", msg);
    // VALUE_OF_PROPERTY_PATH, EQUAL_TO, no displayString, single
    msg = getMessage(new PropertyPathEqualToSinBean(null, "abc", "abc"));
    Assertions.assertEquals(prefix2 + "is abc.", msg);
    // VALUE_OF_PROPERTY_PATH, EQUAL_TO, no displayString, multiple
    msg = getMessage(new PropertyPathEqualToMulBean(null, "abc", new String[] {"abc", "def"}));
    Assertions.assertEquals(prefix2 + "is one of abc, def.", msg);
    // VALUE_OF_PROPERTY_PATH, EQUAL_TO, displayString not defined in properties file, single
    msg = getMessage(
        new PropertyPathEqualToWithDisplayStringSinBean(null, "abc", "abc", "some value"));
    Assertions.assertEquals(prefix2 + "is some value.", msg);
    // VALUE_OF_PROPERTY_PATH, EQUAL_TO, description not defined in properties file, multiple
    msg = getMessage(new PropertyPathEqualToWithDisplayStringMulBean(null, "abc",
        new String[] {"abc", "def"}, new String[] {"some value 1", "some value 2"}));
    Assertions.assertEquals(prefix2 + "is one of some value 1, some value 2.", msg);

    // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO, no displayString, single
    msg = getMessage(new PropertyPathNotEqualToSinBean(null, "xyz", "abc"));
    Assertions.assertEquals(prefix2 + "is not abc.", msg);
    // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO, no displayString, multiple
    msg = getMessage(new PropertyPathNotEqualToMulBean(null, "xyz", new String[] {"abc", "def"}));
    Assertions.assertEquals(prefix2 + "is not one of abc, def.", msg);
    // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO, displayString not defined in properties file, single
    msg = getMessage(
        new PropertyPathNotEqualToWithDisplayStringSinBean(null, "xyz", "abc", "some value"));
    Assertions.assertEquals(prefix2 + "is not some value.", msg);
    // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO, description not defined in properties file, multiple
    msg = getMessage(new PropertyPathNotEqualToWithDisplayStringMulBean(null, "xyz",
        new String[] {"abc", "def"}, new String[] {"some value 1", "some value 2"}));
    Assertions.assertEquals(prefix2 + "is not one of some value 1, some value 2.", msg);

    // emptyWhenConditionNotSatisfied
    msg = getMessage(new EmptyWhenConditionNotSatisfiedBean(null, null));
    Assertions.assertEquals(prefix2 + "is empty, and empty when otherwise.", msg);

    // parenthesis for item name
    msg = ExceptionUtil
        .getMessageList(validator.validate(new EmptyEqualToBean(null, null)), Locale.ITALIAN)
        .get(0);
    Assertions.assertEquals(prefix1 + "'condition value' is empty.", msg);
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY)
  public static record EmptyEqualToBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.EMPTY)
  public static record EmptyNotEqualToBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.NOT_EMPTY)
  public static record NotEmptyEqualToBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.NOT_EMPTY)
  public static record NotEmptyNotEqualToBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.TRUE)
  public static record TrueEqualToBean(String value, boolean condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.TRUE)
  public static record TrueNotEqualToBean(String value, Boolean condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.FALSE)
  public static record FalseEqualToBean(String value, boolean condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.FALSE)
  public static record FalseNotEqualToBean(String value, Boolean condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.STRING, conditionValueString = {"test"})
  public static record StringEqualToSingleBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.STRING, conditionValueString = {"test1", "test2"})
  public static record StringEqualToMultipleBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.STRING,
      conditionValueString = {"test"})
  public static record StringNotEqualToSingleBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.STRING,
      conditionValueString = {"test1", "test2"})
  public static record StringNotEqualToMultipleBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.PATTERN, conditionValuePatternRegexp = ".*test.*")
  public static record PatternEqualToBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.PATTERN, conditionValuePatternRegexp = ".*test.*",
      conditionValuePatternDescription = "notExistBean.test")
  public static record PatternEqualToWithDescNonResolvedBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.PATTERN, conditionValuePatternRegexp = ".*test.*",
      conditionValuePatternDescription = "bean.test")
  public static record PatternEqualToWithDescResolvedBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.PATTERN,
      conditionValuePatternRegexp = ".*test.*")
  public static record PatternNotEqualToBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.PATTERN,
      conditionValuePatternRegexp = ".*test.*",
      conditionValuePatternDescription = "notExistBean.test")
  public static record PatternNotEqualToWithDescNonResolvedBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.PATTERN,
      conditionValuePatternRegexp = ".*test.*", conditionValuePatternDescription = "bean.test")
  public static record PatternNotEqualToWithDescResolvedBean(String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathEqualToSinBean(String value, String condValue,
      String condEqual) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathEqualToMulBean(String value, String condValue,
      String[] condEqual) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValuePropertyPathDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathEqualToWithDisplayStringSinBean(String value, String condValue,
      String condEqual, String condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValuePropertyPathDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathEqualToWithDisplayStringMulBean(String value, String condValue,
      String[] condEqual, String[] condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathNotEqualToSinBean(String value, String condValue,
      String condEqual) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathNotEqualToMulBean(String value, String condValue,
      String[] condEqual) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValuePropertyPathDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathNotEqualToWithDisplayStringSinBean(String value,
      String condValue, String condEqual, String condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValuePropertyPathDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathNotEqualToWithDisplayStringMulBean(String value,
      String condValue, String[] condEqual, String[] condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY, emptyWhenConditionNotSatisfied = true)
  public static record EmptyWhenConditionNotSatisfiedBean(String value, String condValue) {
  }
}
