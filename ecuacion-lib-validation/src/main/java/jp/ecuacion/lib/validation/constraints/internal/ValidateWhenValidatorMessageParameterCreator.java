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
package jp.ecuacion.lib.validation.constraints.internal;

import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.STRING;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.VALUE_OF_PROPERTY_PATH;

import jakarta.validation.ConstraintViolation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ValidatorMessageParameterCreator;
import jp.ecuacion.lib.core.util.ItemUtil;
import jp.ecuacion.lib.core.util.MessageUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.PropertyPathUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.Condition;
import jp.ecuacion.lib.validation.constraints.enums.ConditionOperator;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValueState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ValidateWhenValidatorMessageParameterCreator
    implements ValidatorMessageParameterCreator {

  private static final String COMMON_MESSAGE_PREFIX =
      "jp.ecuacion.lib.validation.constraints.ValidateWhen";

  @Override
  public Map<@NonNull String, @Nullable Object> create(ConstraintViolation<?> cv,
      Map<@NonNull String, @Nullable Object> paramMap) {
    Map<@NonNull String, @Nullable Object> result = new HashMap<>();

    List<@NonNull ConditionAttributes> conditionList = getConditionAttributesList(paramMap);

    // conditionPropertyPathItemName and displayStringOfConditionValue describe the first
    // condition only. They are kept for message templates which were written before
    // conditionDescription was introduced.
    ConditionAttributes firstCondition = conditionList.get(0);
    result.put(ValidateWhenValidator.CONDITION_PROPERTY_PATH_ITEM_NAME,
        conditionItemName(cv, firstCondition));
    result.put(ValidateWhenValidator.DISPLAY_STRING_OF_CONDITION_VALUE,
        displayStringOfConditionValue(cv, firstCondition, false));

    // conditionDescription describes all the conditions joined by the conjunction.
    // Conditions other than the last one use the conjunctive form of their message part
    // since some languages (like Japanese) need a different form before the conjunction.
    Object conditionDescription = null;
    for (int i = 0; i < conditionList.size(); i++) {
      ConditionAttributes condition = conditionList.get(i);
      boolean isConjunctive = i < conditionList.size() - 1;
      Arg clause = Arg.message(COMMON_MESSAGE_PREFIX + ".messagePart.condition",
          conditionItemName(cv, condition),
          displayStringOfConditionValue(cv, condition, isConjunctive));

      conditionDescription = conditionDescription == null ? clause
          : Arg.message(COMMON_MESSAGE_PREFIX + ".messagePart.conditionConjunction",
              conditionDescription, clause);
    }
    result.put(ValidateWhenValidator.CONDITION_DESCRIPTION, conditionDescription);

    // validatesWhenConditionNotSatisfied
    // Each When-validator annotation must have exactly one parameter whose name ends with
    // "ConditionNotSatisfied". That convention allows automatic lookup without a switch statement.
    String conditionNotSatisfiedKey = Objects.requireNonNull(paramMap.keySet().stream()
        .filter(k -> k.endsWith("WhenConditionNotSatisfied")).toList().get(0));
    boolean bl = (boolean) Objects.requireNonNull(paramMap.get(conditionNotSatisfiedKey));

    String paramKey = ValidateWhenValidator.VALIDATES_WHEN_CONDITION_NOT_SATISFIED + "Description";
    if (bl) {
      result.put(paramKey,
          Arg.message(paramMap.get("annotation") + ".messagePart."
              + ValidateWhenValidator.VALIDATES_WHEN_CONDITION_NOT_SATISFIED));
    } else {
      result.put(paramKey, "");
    }

    return result;
  }

  /**
   * Obtains conditions from {@code conditions} if it is set,
   *     or from {@code conditionPropertyPath} and other {@code condition*} parameters otherwise.
   *
   * <p>paramMap carries the raw (possibly UNSPECIFIED) annotation attribute, not the validator's
   *     resolved field, so {@code conditionValue} is resolved the same way
   *     {@link ValidateWhenValidator#initialize} does.</p>
   */
  private List<@NonNull ConditionAttributes> getConditionAttributesList(
      Map<@NonNull String, @Nullable Object> paramMap) {
    Condition[] conditions = (Condition[]) paramMap.get(ValidateWhenValidator.CONDITIONS);

    if (conditions != null && conditions.length > 0) {
      return Arrays.stream(conditions)
          .map(c -> new ConditionAttributes(c.propertyPath(),
              ValidateWhenValidator.resolveConditionValue(ConditionValue.UNSPECIFIED,
                  c.valueString(), c.valuePatternRegexp(), c.valuePropertyPath(),
                  c.valueBoolean(), c.valueState()),
              c.operator(), List.of(c.valueString()), c.valuePatternRegexp(),
              c.valuePatternDescription(), c.valuePropertyPath(),
              c.valueDisplayStringPropertyPath()))
          .toList();
    }

    String[] conditionValueString = (String[]) Objects
        .requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_VALUE_STRING));
    String conditionValuePatternRegexp =
        (String) Objects.requireNonNull(paramMap.get("conditionValuePatternRegexp"));
    String conditionValuePropertyPath = (String) Objects
        .requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_VALUE_PROPERTY_PATH));
    ConditionValue conditionValue = ValidateWhenValidator.resolveConditionValue(
        (ConditionValue) Objects
            .requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_VALUE)),
        conditionValueString, conditionValuePatternRegexp, conditionValuePropertyPath,
        (boolean[]) Objects
            .requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_VALUE_BOOLEAN)),
        (ConditionValueState) Objects
            .requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_VALUE_STATE)));

    String conditionValueDisplayStringPropertyPath = (String) Objects.requireNonNull(paramMap
        .get(ValidateWhenValidator.CONDITION_VALUE_PROPERTY_PATH_DISPLAY_STRING_PROPERTY_PATH));

    return List.of(new ConditionAttributes(
        (String) Objects
            .requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_PROPERTY_PATH)),
        conditionValue,
        (ConditionOperator) Objects
            .requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_OPERATOR)),
        List.of(conditionValueString), conditionValuePatternRegexp,
        (String) Objects.requireNonNull(paramMap.get("conditionValuePatternDescription")),
        conditionValuePropertyPath, conditionValueDisplayStringPropertyPath));
  }

  private ItemNameParam conditionItemName(ConstraintViolation<?> cv,
      ConditionAttributes condition) {
    // This constraint (e.g. @NotNullWhen) is always class-level (@Target(TYPE)), so
    // cv.getLeafBean() is always the exact bean instance the annotation is placed on, and
    // the condition propertyPath is always a direct property of that same bean. Resolving
    // relative to it directly (rather than concatenating cv.getPropertyPath() onto
    // cv.getRootBean()) keeps the item name based on the bean's own identity
    // (@ItemNameKeyClass or class name) regardless of how deeply that bean is nested under the
    // root.
    Item item = ItemUtil.resolveItem(condition.propertyPath(), cv.getLeafBean());
    return new ItemNameParam(List.of(item), cv.getRootBean());
  }

  private Arg displayStringOfConditionValue(ConstraintViolation<?> cv,
      ConditionAttributes condition, boolean isConjunctive) {
    ConditionValue conditionPtn = condition.conditionValue();
    Object displayStringOfConditionValueArg = "";

    if (conditionPtn == VALUE_OF_PROPERTY_PATH) {
      Object values = PropertyPathUtil.getValue(cv.getLeafBean(), condition.valuePropertyPath());

      displayStringOfConditionValueArg =
          displayStringCommon(cv, condition, Objects.requireNonNull(values));

    } else if (conditionPtn == STRING) {
      displayStringOfConditionValueArg =
          displayStringCommon(cv, condition, condition.valueString().toArray(String[]::new));

    } else if (conditionPtn == ConditionValue.PATTERN) {
      String description = condition.valuePatternDescription();

      if (description.equals(EclibValidationConstants.VALIDATOR_PARAMETER_NULL)
          || description.isEmpty()) {
        displayStringOfConditionValueArg = condition.valuePatternRegexp();

      } else {
        displayStringOfConditionValueArg = Arg.itemName(description);
      }
    }

    String propKey = COMMON_MESSAGE_PREFIX + ".messagePart."
        + StringUtil.getLowerCamelFromSnake(conditionPtn.toString()) + "."
        + StringUtil.getLowerCamelFromSnake(condition.operator().toString())
        + (isConjunctive ? ".conjunctive" : "");
    return Arg.message(propKey, displayStringOfConditionValueArg);
  }

  private Arg displayStringCommon(ConstraintViolation<?> cv, ConditionAttributes condition,
      Object values) {
    String displayStringPp = condition.valueDisplayStringPropertyPath();

    Object displayStringObj = displayStringPp.isEmpty() ? values
        : Objects.requireNonNull(PropertyPathUtil.getValue(cv.getLeafBean(), displayStringPp));

    List<@NonNull String> displayStringList =
        displayStringObj instanceof Object[] arr ? Arrays.stream(arr).map(Object::toString).toList()
            : List.of(Objects.requireNonNull(displayStringObj).toString());

    Arg valueArg = displayStringPp.isEmpty()
        ? MessageUtil.formatValues(displayStringList.toArray(String[]::new))
        : MessageUtil.formatValuesWithResolution(displayStringList.toArray(String[]::new));

    return displayStringList.size() > 1
        ? Arg.message(COMMON_MESSAGE_PREFIX + ".messagePart.string.multiple", valueArg)
        : valueArg;
  }

  /**
   * Holds the attributes of one condition, obtained either from {@link Condition}
   *     or from the {@code condition*} parameters of the annotation.
   */
  private record ConditionAttributes(String propertyPath, ConditionValue conditionValue,
      ConditionOperator operator, List<@NonNull String> valueString, String valuePatternRegexp,
      String valuePatternDescription, String valuePropertyPath,
      String valueDisplayStringPropertyPath) {
  }
}
