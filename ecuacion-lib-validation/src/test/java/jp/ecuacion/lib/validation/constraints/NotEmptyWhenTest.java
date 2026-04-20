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
package jp.ecuacion.lib.validation.constraints;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Locale;
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NotEmptyWhenTest {

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void beforeAll() {
    PropertiesFileUtil.addResourceBundlePostfix("lib-validation-test");
  }

  /**
   * Tests validation logic for {@code conditionValue = NULL} and {@code NOT_NULL}.
   *
   * <p>Key difference from {@code EMPTY}/{@code NOT_EMPTY}: blank strings do NOT satisfy
   *     the {@code NULL} condition.</p>
   */
  @Test
  public void validationOnConditionIsNullNotNullTest() {
    // NULL: condition satisfied only when condValue is strictly null (not blank)
    // null condValue -> condition satisfied -> value=null -> fail
    Assertions.assertEquals(1, validator.validate(new NullCondBean(null, null)).size());
    // null condValue -> condition satisfied -> value="a" -> pass
    Assertions.assertEquals(0, validator.validate(new NullCondBean("a", null)).size());
    // blank condValue -> condition NOT satisfied (blank != null) -> pass
    Assertions.assertEquals(0, validator.validate(new NullCondBean(null, "")).size());
    // non-null condValue -> condition NOT satisfied -> pass
    Assertions.assertEquals(0, validator.validate(new NullCondBean(null, "a")).size());

    // NOT_NULL: condition satisfied when condValue is not null (including blank strings)
    // non-null condValue -> condition satisfied -> value=null -> fail
    Assertions.assertEquals(1, validator.validate(new NotNullCondBean(null, "a")).size());
    // blank condValue -> condition satisfied (blank is not null) -> value=null -> fail
    Assertions.assertEquals(1, validator.validate(new NotNullCondBean(null, "")).size());
    // null condValue -> condition NOT satisfied -> pass
    Assertions.assertEquals(0, validator.validate(new NotNullCondBean(null, null)).size());
    // non-null condValue -> condition satisfied -> value="a" -> pass
    Assertions.assertEquals(0, validator.validate(new NotNullCondBean("a", "x")).size());
  }

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.NULL)
  public static record NullCondBean(@Nullable String value, @Nullable String condValue) {
  }

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.NOT_NULL)
  public static record NotNullCondBean(@Nullable String value, @Nullable String condValue) {
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
  public static record StrBean(@Nullable String value, @Nullable String condValue) {
  }

  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY)
  public static record IntBean(@Nullable Integer value, @Nullable String condValue) {
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

    // EMPTY, EQUAL_TO
    msg = getMessage(new EmptyEqualToBean(null, null));
    Assertions.assertEquals(prefix2 + "is empty", msg);
    // EMPTY, NOT_EQUAL_TO
    msg = getMessage(new NotEmptyEqualToBean(null, null));
    Assertions.assertEquals(prefix2 + "is not empty", msg);
    // NOT_EMPTY, EQUAL_TO
    msg = getMessage(new EmptyNotEqualToBean(null, "a"));
    Assertions.assertEquals(prefix2 + "is not empty", msg);
    // NOT_EMPTY, NOT_EQUAL_TO
    msg = getMessage(new NotEmptyNotEqualToBean(null, "a"));
    Assertions.assertEquals(prefix2 + "is empty", msg);
    // TRUE, EQUAL_TO
    msg = getMessage(new TrueEqualToBean(null, true));
    Assertions.assertEquals(prefix2 + "is ON", msg);
    // TRUE, NOT_EQUAL_TO
    msg = getMessage(new TrueNotEqualToBean(null, false));
    Assertions.assertEquals(prefix2 + "is not ON", msg);
    // FALSE, EQUAL_TO
    msg = getMessage(new FalseEqualToBean(null, false));
    Assertions.assertEquals(prefix2 + "is OFF", msg);
    // FALSE, NOT_EQUAL_TO
    msg = getMessage(new FalseNotEqualToBean(null, true));
    Assertions.assertEquals(prefix2 + "is not OFF", msg);

    // STRING, EQUAL_TO, no displayString, single
    msg = getMessage(new StringEqualToSinBean(null, "test"));
    Assertions.assertEquals(prefix2 + "is 'test'", msg);
    // STRING, EQUAL_TO, no displayString, multiple
    msg = getMessage(new StringEqualToMulBean(null, "test1"));
    Assertions.assertEquals(prefix2 + "is one of 'test1', 'test2'", msg);
    // STRING, EQUAL_TO, description not defined in properties file, single
    msg = getMessage(new StringEqualToWithDisplayStringSinBean(null, "test", "some value"));
    Assertions.assertEquals(prefix2 + "is 'some value'", msg);
    // STRING, EQUAL_TO, description not defined in properties file, multiple
    msg = getMessage(new StringEqualToWithDisplayStringMulBean(null, "test1",
        new String[] {"some value 1", "some value 2"}));
    Assertions.assertEquals(prefix2 + "is one of 'some value 1', 'some value 2'", msg);
    // STRING, EQUAL_TO, description defined in properties file, single
    msg = getMessage(
        new StringEqualToWithDisplayStringSinBean(null, "test", "value.from.enum_names"));
    Assertions.assertEquals(prefix2 + "is 'some value'", msg);
    // STRING, EQUAL_TO, description defined in properties file, multiple
    msg = getMessage(new StringEqualToWithDisplayStringMulBean(null, "test1",
        new String[] {"value.from.item_names.1", "value.from.item_names.2"}));
    Assertions.assertEquals(prefix2 + "is one of 'some value 1', 'some value 2'", msg);
    // STRING, NOT_EQUAL_TO, no displayString, single
    msg = getMessage(new StringNotEqualToSinBean(null, "a"));
    Assertions.assertEquals(prefix2 + "is not 'test'", msg);
    // STRING, NOT_EQUAL_TO, no displayString, multiple
    msg = getMessage(new StringNotEqualToMulBean(null, "a"));
    Assertions.assertEquals(prefix2 + "is not one of 'test1', 'test2'", msg);
    // STRING, NOT_EQUAL_TO, description not defined in properties file, single
    msg = getMessage(new StringNotEqualToWithDisplayStringSinBean(null, "a", "some value"));
    Assertions.assertEquals(prefix2 + "is not 'some value'", msg);
    // STRING, NOT_EQUAL_TO, description not defined in properties file, multiple
    msg = getMessage(new StringNotEqualToWithDisplayStringMulBean(null, "a",
        new String[] {"some value 1", "some value 2"}));
    Assertions.assertEquals(prefix2 + "is not one of 'some value 1', 'some value 2'", msg);
    // STRING, NOT_EQUAL_TO, description defined in properties file, single
    msg = getMessage(
        new StringNotEqualToWithDisplayStringSinBean(null, "a", "value.from.enum_names"));
    Assertions.assertEquals(prefix2 + "is not 'some value'", msg);
    // STRING, NOT_EQUAL_TO, description defined in properties file, multiple
    msg = getMessage(new StringNotEqualToWithDisplayStringMulBean(null, "a",
        new String[] {"value.from.item_names.1", "value.from.item_names.2"}));
    Assertions.assertEquals(prefix2 + "is not one of 'some value 1', 'some value 2'", msg);

    // PATTERN, EQUAL_TO, no description
    msg = getMessage(new PatternEqualToBean(null, "test"));
    Assertions.assertEquals(prefix2 + "matches the pattern: .*test.*", msg);
    // PATTERN, EQUAL_TO, description which is not defined in item_names
    msg = getMessage(new PatternEqualToWithDescNonResolvedBean(null, "test"));
    Assertions.assertEquals(prefix2 + "matches the pattern: notExistBean.test", msg);
    // PATTERN, EQUAL_TO, description which is defined in item_names
    msg = getMessage(new PatternEqualToWithDescResolvedBean(null, "test"));
    Assertions.assertEquals(prefix2 + "matches the pattern: the string which contains 'test'", msg);
    // PATTERN, NOT_EQUAL_TO, no description
    msg = getMessage(new PatternNotEqualToBean(null, "a"));
    Assertions.assertEquals(prefix2 + "does not match the pattern: .*test.*", msg);
    // PATTERN, NOT_EQUAL_TO, description which is not defined in item_names
    msg = getMessage(new PatternNotEqualToWithDescNonResolvedBean(null, "a"));
    Assertions.assertEquals(prefix2 + "does not match the pattern: notExistBean.test", msg);
    // PATTERN, NOT_EQUAL_TO, description which is defined in item_names
    msg = getMessage(new PatternNotEqualToWithDescResolvedBean(null, "a"));
    Assertions.assertEquals(
        prefix2 + "does not match the pattern: the string which contains 'test'", msg);

    // VALUE_OF_PROPERTY_PATH, EQUAL_TO, no displayString, single
    msg = getMessage(new PropertyPathEqualToSinBean(null, "abc", "abc"));
    Assertions.assertEquals(prefix2 + "is 'abc'", msg);
    // VALUE_OF_PROPERTY_PATH, EQUAL_TO, no displayString, multiple
    msg = getMessage(new PropertyPathEqualToMulBean(null, "abc", new String[] {"abc", "def"}));
    Assertions.assertEquals(prefix2 + "is one of 'abc', 'def'", msg);
    // VALUE_OF_PROPERTY_PATH, EQUAL_TO, displayString not defined in properties file, single
    msg = getMessage(
        new PropertyPathEqualToWithDisplayStringSinBean(null, "abc", "abc", "some value"));
    Assertions.assertEquals(prefix2 + "is 'some value'", msg);
    // VALUE_OF_PROPERTY_PATH, EQUAL_TO, description not defined in properties file, multiple
    msg = getMessage(new PropertyPathEqualToWithDisplayStringMulBean(null, "abc",
        new String[] {"abc", "def"}, new String[] {"some value 1", "some value 2"}));
    Assertions.assertEquals(prefix2 + "is one of 'some value 1', 'some value 2'", msg);
    // VALUE_OF_PROPERTY_PATH, EQUAL_TO, displayString defined in properties file, single
    msg = getMessage(new PropertyPathEqualToWithDisplayStringSinBean(null, "abc", "abc",
        "value.from.enum_names"));
    Assertions.assertEquals(prefix2 + "is 'some value'", msg);
    // VALUE_OF_PROPERTY_PATH, EQUAL_TO, description defined in properties file, multiple
    msg = getMessage(
        new PropertyPathEqualToWithDisplayStringMulBean(null, "abc", new String[] {"abc", "def"},
            new String[] {"value.from.item_names.1", "value.from.item_names.2"}));
    Assertions.assertEquals(prefix2 + "is one of 'some value 1', 'some value 2'", msg);
    // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO, no displayString, single
    msg = getMessage(new PropertyPathNotEqualToSinBean(null, "xyz", "abc"));
    Assertions.assertEquals(prefix2 + "is not 'abc'", msg);
    // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO, no displayString, multiple
    msg = getMessage(new PropertyPathNotEqualToMulBean(null, "xyz", new String[] {"abc", "def"}));
    Assertions.assertEquals(prefix2 + "is not one of 'abc', 'def'", msg);
    // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO, displayString not defined in properties file, single
    msg = getMessage(
        new PropertyPathNotEqualToWithDisplayStringSinBean(null, "xyz", "abc", "some value"));
    Assertions.assertEquals(prefix2 + "is not 'some value'", msg);
    // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO, description not defined in properties file, multiple
    msg = getMessage(new PropertyPathNotEqualToWithDisplayStringMulBean(null, "xyz",
        new String[] {"abc", "def"}, new String[] {"some value 1", "some value 2"}));
    Assertions.assertEquals(prefix2 + "is not one of 'some value 1', 'some value 2'", msg);
    // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO, displayString defined in properties file, single
    msg = getMessage(new PropertyPathNotEqualToWithDisplayStringSinBean(null, "xyz", "abc",
        "value.from.enum_names"));
    Assertions.assertEquals(prefix2 + "is not 'some value'", msg);
    // VALUE_OF_PROPERTY_PATH, NOT_EQUAL_TO, description defined in properties file, multiple
    msg = getMessage(
        new PropertyPathNotEqualToWithDisplayStringMulBean(null, "xyz", new String[] {"abc", "def"},
            new String[] {"value.from.item_names.1", "value.from.item_names.2"}));
    Assertions.assertEquals(prefix2 + "is not one of 'some value 1', 'some value 2'", msg);

    // NULL, EQUAL_TO
    msg = getMessage(new NullEqualToBean(null, null));
    Assertions.assertEquals(prefix2 + "is null", msg);
    // NULL, NOT_EQUAL_TO
    msg = getMessage(new NullNotEqualToBean(null, "a"));
    Assertions.assertEquals(prefix2 + "is not null", msg);
    // NOT_NULL, EQUAL_TO
    msg = getMessage(new NotNullEqualToBean(null, "a"));
    Assertions.assertEquals(prefix2 + "is not null", msg);
    // NOT_NULL, NOT_EQUAL_TO
    msg = getMessage(new NotNullNotEqualToBean(null, null));
    Assertions.assertEquals(prefix2 + "is null", msg);

    // emptyWhenConditionNotSatisfied
    msg = getMessage(new EmptyWhenConditionNotSatisfiedBean(null, null));
    Assertions.assertEquals(prefix2 + "is empty, and empty when otherwise", msg);

    // parenthesis for item name
    msg = ExceptionUtil
        .getMessageList(validator.validate(new EmptyEqualToBean(null, null)), Locale.ITALIAN)
        .get(0);
    Assertions.assertEquals(prefix1 + "'condition value' is empty", msg);
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY)
  public static record EmptyEqualToBean(@Nullable String value, @Nullable String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.EMPTY)
  public static record EmptyNotEqualToBean(@Nullable String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.NOT_EMPTY)
  public static record NotEmptyEqualToBean(@Nullable String value, @Nullable String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.NOT_EMPTY)
  public static record NotEmptyNotEqualToBean(@Nullable String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.TRUE)
  public static record TrueEqualToBean(@Nullable String value, boolean condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.TRUE)
  public static record TrueNotEqualToBean(@Nullable String value, Boolean condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.FALSE)
  public static record FalseEqualToBean(@Nullable String value, boolean condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.FALSE)
  public static record FalseNotEqualToBean(@Nullable String value, Boolean condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.STRING, conditionValueString = {"test"})
  public static record StringEqualToSinBean(@Nullable String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.STRING, conditionValueString = {"test1", "test2"})
  public static record StringEqualToMulBean(@Nullable String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.STRING, conditionValueString = {"test"},
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record StringEqualToWithDisplayStringSinBean(@Nullable String value,
      String condValue, String condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.STRING, conditionValueString = {"test1", "test2"},
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record StringEqualToWithDisplayStringMulBean(@Nullable String value,
      String condValue, String[] condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.STRING,
      conditionValueString = {"test"})
  public static record StringNotEqualToSinBean(@Nullable String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.STRING,
      conditionValueString = {"test1", "test2"})
  public static record StringNotEqualToMulBean(@Nullable String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.STRING,
      conditionValueString = {"test"}, conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record StringNotEqualToWithDisplayStringSinBean(@Nullable String value,
      String condValue, String condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.STRING,
      conditionValueString = {"test1", "test2"},
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record StringNotEqualToWithDisplayStringMulBean(@Nullable String value,
      String condValue, String[] condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.PATTERN, conditionValuePatternRegexp = ".*test.*")
  public static record PatternEqualToBean(@Nullable String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.PATTERN, conditionValuePatternRegexp = ".*test.*",
      conditionValuePatternDescription = "notExistBean.test")
  public static record PatternEqualToWithDescNonResolvedBean(@Nullable String value,
      String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.PATTERN, conditionValuePatternRegexp = ".*test.*",
      conditionValuePatternDescription = "bean.test")
  public static record PatternEqualToWithDescResolvedBean(@Nullable String value,
      String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.PATTERN,
      conditionValuePatternRegexp = ".*test.*")
  public static record PatternNotEqualToBean(@Nullable String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.PATTERN,
      conditionValuePatternRegexp = ".*test.*",
      conditionValuePatternDescription = "notExistBean.test")
  public static record PatternNotEqualToWithDescNonResolvedBean(@Nullable String value,
      String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.PATTERN,
      conditionValuePatternRegexp = ".*test.*", conditionValuePatternDescription = "bean.test")
  public static record PatternNotEqualToWithDescResolvedBean(@Nullable String value,
      String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathEqualToSinBean(@Nullable String value, String condValue,
      String condEqual) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathEqualToMulBean(@Nullable String value, String condValue,
      String[] condEqual) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathEqualToWithDisplayStringSinBean(@Nullable String value,
      String condValue, String condEqual, String condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathEqualToWithDisplayStringMulBean(@Nullable String value,
      String condValue, String[] condEqual, String[] condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathNotEqualToSinBean(@Nullable String value, String condValue,
      String condEqual) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual")
  public static record PropertyPathNotEqualToMulBean(@Nullable String value, String condValue,
      String[] condEqual) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathNotEqualToWithDisplayStringSinBean(@Nullable String value,
      String condValue, String condEqual, String condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO,
      conditionValue = ConditionValue.VALUE_OF_PROPERTY_PATH,
      conditionValuePropertyPath = "condEqual",
      conditionValueDisplayStringPropertyPath = "condEqualDisplay")
  public static record PropertyPathNotEqualToWithDisplayStringMulBean(@Nullable String value,
      String condValue, String[] condEqual, String[] condEqualDisplay) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.EMPTY, emptyWhenConditionNotSatisfied = true)
  public static record EmptyWhenConditionNotSatisfiedBean(@Nullable String value,
      @Nullable String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.NULL)
  public static record NullEqualToBean(@Nullable String value, @Nullable String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.NULL)
  public static record NullNotEqualToBean(@Nullable String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionValue = ConditionValue.NOT_NULL)
  public static record NotNullEqualToBean(@Nullable String value, String condValue) {
  }

  @ItemNameKeyClass("NotEmptyWhenTest")
  @NotEmptyWhen(propertyPath = "value", conditionPropertyPath = "condValue",
      conditionOperator = ConditionOperator.NOT_EQUAL_TO, conditionValue = ConditionValue.NOT_NULL)
  public static record NotNullNotEqualToBean(@Nullable String value, @Nullable String condValue) {
  }
}
