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
import java.util.Locale;
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

  private <T> void assertEqual(T object, String msg) {
    Set<ConstraintViolation<T>> cvs = validator.validate(object);
    Assertions.assertEquals(msg, ExceptionUtil.getMessageList(cvs, Locale.ENGLISH).get(0));
  }

  @Test
  public void messageTest() {
    String msg = null;

    // @EmptyWhen
    msg = "needs to be empty when 'condition field' is ON";
    assertEqual(new EmptyWhenTest(), msg);

    // @NotEmptyWhen
    msg = "needs to be not empty when 'condition field' is ON";
    assertEqual(new NotEmptyWhenTest(), msg);

    // @TrueWhen
    msg = "needs to be true when 'condition field' is ON";
    assertEqual(new TrueWhenTest(), msg);

    // @FalseWhen
    msg = "needs to be false when 'condition field' is ON";
    assertEqual(new FalseWhenTest(), msg);

    // @StringWhen
    msg = "needs to be a specific value when 'condition field' is ON";
    assertEqual(new StringWhenTest(), msg);

    // @NotStringWhen
    msg = "needs to not be a specific value when 'condition field' is ON";
    assertEqual(new NotStringWhenTest(), msg);

    // @PatternWhen
    msg = "needs to match the pattern when 'condition field' is ON";
    assertEqual(new PatternWhenTest(), msg);

    // @NotPatternWhen
    msg = "needs to not match the pattern when 'condition field' is ON";
    assertEqual(new NotPatternWhenTest(), msg);

    // @ValueOfPropertyPathWhen
    msg = "needs to be the value of a specific field when 'condition field' is ON";
    assertEqual(new ValueOfPropertyPathWhenTest(), msg);
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
