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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import jp.ecuacion.lib.core.util.ExceptionUtil;
import jp.ecuacion.lib.core.util.PropertyFileUtil;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests messages for ValidateWhenValidators.
 */
public class ValidateWhenValidatorMessageTest {

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void beforeAll() {
    PropertyFileUtil.addResourceBundlePostfix("lib-validation-test");
  }

  @Test
  public void messageTest() {
    String msg = null;

    // @EmptyWhen
    Set<ConstraintViolation<EmptyWhenTest>> cvsEmptyWhen = validator.validate(new EmptyWhenTest());
    msg = "needs to be empty when 'condition field' is ON";
    Assertions.assertEquals(msg, ExceptionUtil.getMessageList(cvsEmptyWhen).get(0));

    // @NotEmptyWhen
    Set<ConstraintViolation<NotEmptyWhenTest>> cvsNotEmptyWhen =
        validator.validate(new NotEmptyWhenTest());
    msg = "needs to be not empty when 'condition field' is ON";
    Assertions.assertEquals(msg, ExceptionUtil.getMessageList(cvsNotEmptyWhen).get(0));

    // @TrueWhen
    Set<ConstraintViolation<TrueWhenTest>> cvsTrueWhen = validator.validate(new TrueWhenTest());
    msg = "needs to be true when 'condition field' is ON";
    Assertions.assertEquals(msg, ExceptionUtil.getMessageList(cvsTrueWhen).get(0));

    // @FalseWhen
    Set<ConstraintViolation<FalseWhenTest>> cvsFalseWhen = validator.validate(new FalseWhenTest());
    msg = "needs to be false when 'condition field' is ON";
    Assertions.assertEquals(msg, ExceptionUtil.getMessageList(cvsFalseWhen).get(0));

    // @StringWhen
    Set<ConstraintViolation<StringWhenTest>> cvsStringWhen =
        validator.validate(new StringWhenTest());
    msg = "needs to be a specific value when 'condition field' is ON";
    Assertions.assertEquals(msg, ExceptionUtil.getMessageList(cvsStringWhen).get(0));

    // @NotStringWhen
    Set<ConstraintViolation<NotStringWhenTest>> cvsNotStringWhen =
        validator.validate(new NotStringWhenTest());
    msg = "needs to not be a specific value when 'condition field' is ON";
    Assertions.assertEquals(msg, ExceptionUtil.getMessageList(cvsNotStringWhen).get(0));

    // @PatternWhen
    Set<ConstraintViolation<PatternWhenTest>> cvsPatternWhen =
        validator.validate(new PatternWhenTest());
    msg = "needs to match the pattern when 'condition field' is ON";
    Assertions.assertEquals(msg, ExceptionUtil.getMessageList(cvsPatternWhen).get(0));

    // @NotPatternWhen
    Set<ConstraintViolation<NotPatternWhenTest>> cvsNotPatternWhen =
        validator.validate(new NotPatternWhenTest());
    msg = "needs to not match the pattern when 'condition field' is ON";
    Assertions.assertEquals(msg, ExceptionUtil.getMessageList(cvsNotPatternWhen).get(0));

    // @ValueOfPropertyPathWhen
    Set<ConstraintViolation<ValueOfPropertyPathWhenTest>> cvsValueOfPropertyPathWhen =
        validator.validate(new ValueOfPropertyPathWhenTest());
    msg = "needs to be the value of a specific field when 'condition field' is ON";
    Assertions.assertEquals(msg,
        ExceptionUtil.getMessageList(cvsValueOfPropertyPathWhen).get(0));
  }

  @EmptyWhen(propertyPath = "field", conditionPropertyPath = "conditionField",
      conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class EmptyWhenTest {
    private String field = "test";
    private boolean conditionField = true;
  }

  @NotEmptyWhen(propertyPath = "field", conditionPropertyPath = "conditionField",
      conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class NotEmptyWhenTest {
    private String field = "";
    private boolean conditionField = true;
  }

  @TrueWhen(propertyPath = "field", conditionPropertyPath = "conditionField",
      conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class TrueWhenTest {
    private Boolean field = false;
    private boolean conditionField = true;
  }

  @FalseWhen(propertyPath = "field", conditionPropertyPath = "conditionField",
      conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class FalseWhenTest {
    private Boolean field = true;
    private boolean conditionField = true;
  }

  @StringWhen(propertyPath = "field", propertyValueString = {"expected"},
      conditionPropertyPath = "conditionField", conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class StringWhenTest {
    private String field = "wrong";
    private boolean conditionField = true;
  }

  @NotStringWhen(propertyPath = "field", propertyValueString = {"forbidden"},
      conditionPropertyPath = "conditionField", conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class NotStringWhenTest {
    private String field = "forbidden";
    private boolean conditionField = true;
  }

  @PatternWhen(propertyPath = "field", propertyValuePatternRegexp = "\\d+",
      conditionPropertyPath = "conditionField", conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class PatternWhenTest {
    private String field = "abc";
    private boolean conditionField = true;
  }

  @NotPatternWhen(propertyPath = "field", propertyValuePatternRegexp = "\\d+",
      conditionPropertyPath = "conditionField", conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class NotPatternWhenTest {
    private String field = "123";
    private boolean conditionField = true;
  }

  @ValueOfPropertyPathWhen(propertyPath = "field", propertyValuePropertyPath = "otherField",
      conditionPropertyPath = "conditionField", conditionValue = ConditionValue.TRUE)
  @SuppressWarnings("unused")
  private static class ValueOfPropertyPathWhenTest {
    private String field = "abc";
    private String otherField = "xyz";
    private boolean conditionField = true;
  }
}
