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
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ValidateWhenValidatorMessageParameterCreator
    implements ValidatorMessageParameterCreator {

  @Override
  public Map<@NonNull String, @Nullable Object> create(ConstraintViolation<?> cv,
      Map<@NonNull String, @Nullable Object> paramMap) {
    final String commonMessagePrefix = "jp.ecuacion.lib.validation.constraints.ValidateWhen";
    Map<@NonNull String, @Nullable Object> result = new HashMap<>();

    // conditionFieldItemNameKey — resolved as item name (needs locale at render time)
    String cvPropertyPath = cv.getPropertyPath() == null ? "" : cv.getPropertyPath().toString();
    String conditionPropertyPath =
        (StringUtils.isEmpty(cvPropertyPath) ? "" : cvPropertyPath + ".")
            + ((String) paramMap.get(ValidateWhenValidator.CONDITION_PROPERTY_PATH));
    Item item = ItemUtil.resolveItem(conditionPropertyPath, cv.getRootBean(), cv.getLeafBean());
    result.put(ValidateWhenValidator.CONDITION_PROPERTY_PATH_ITEM_NAME,
        new ItemNameParam(new Item[] {item}, cv.getRootBean()));

    // displayStringOfConditionValue
    displayStringOfConditionValue(cv, paramMap, commonMessagePrefix, result);

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

  private void displayStringOfConditionValue(ConstraintViolation<?> cv,
      Map<@NonNull String, @Nullable Object> paramMap, final String commonMessagePrefix,
      Map<@NonNull String, @Nullable Object> result) {
    ConditionValue conditionPtn =
        (ConditionValue) paramMap.get(ValidateWhenValidator.CONDITION_VALUE);
    Object displayStringOfConditionValueArg = "";

    if (conditionPtn == VALUE_OF_PROPERTY_PATH) {
      Object values = PropertyPathUtil.getValue(cv.getLeafBean(), (String) Objects
          .requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_VALUE_PROPERTY_PATH)));

      displayStringOfConditionValueArg =
          displayStringCommon(commonMessagePrefix, cv, paramMap, Objects.requireNonNull(values));

    } else if (conditionPtn == STRING) {
      // conditionValue is used
      String[] values = (String[]) paramMap.get(ValidateWhenValidator.CONDITION_VALUE_STRING);

      displayStringOfConditionValueArg =
          displayStringCommon(commonMessagePrefix, cv, paramMap, Objects.requireNonNull(values));

    } else if (conditionPtn == ConditionValue.PATTERN) {
      String description =
          Objects.requireNonNull((String) paramMap.get("conditionValuePatternDescription"));
      String regExp = (String) paramMap.get("conditionValuePatternRegexp");

      if (description.equals(EclibValidationConstants.VALIDATOR_PARAMETER_NULL)
          || description.isEmpty()) {
        displayStringOfConditionValueArg = regExp;

      } else {
        displayStringOfConditionValueArg = Arg.itemName(description);
      }
    }

    String propKey = commonMessagePrefix + ".messagePart."
        + StringUtil.getLowerCamelFromSnake(
            Objects.requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_VALUE)).toString())
        + "." + StringUtil.getLowerCamelFromSnake(Objects
            .requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_OPERATOR)).toString());
    result.put(ValidateWhenValidator.DISPLAY_STRING_OF_CONDITION_VALUE,
        Arg.message(propKey, displayStringOfConditionValueArg));
  }

  private Arg displayStringCommon(final String commonMessagePrefix, ConstraintViolation<?> cv,
      Map<@NonNull String, @Nullable Object> paramMap, Object values) {
    String displayStringPp = (String) paramMap
        .get(ValidateWhenValidator.CONDITION_VALUE_PROPERTY_PATH_DISPLAY_STRING_PROPERTY_PATH);

    Objects.requireNonNull(displayStringPp);

    Object displayStringObj = displayStringPp.isEmpty() ? values
        : Objects.requireNonNull(PropertyPathUtil.getValue(cv.getLeafBean(), displayStringPp));

    List<@NonNull String> displayStringList =
        displayStringObj instanceof Object[] arr ? Arrays.stream(arr).map(Object::toString).toList()
            : List.of(Objects.requireNonNull(displayStringObj).toString());

    Arg valueArg = displayStringPp.isEmpty()
        ? Arg.formattedString(MessageUtil.getValuesOfFormattedString(displayStringList))
        : MessageUtil.getValuesArg(displayStringList);

    String[] strs = displayStringList.toArray(String[]::new);

    Arg displayStringOfConditionValueArg;
    displayStringOfConditionValueArg = strs.length > 1
        ? Arg.message(commonMessagePrefix + ".messagePart.string.multiple", valueArg)
        : valueArg;
    return displayStringOfConditionValueArg;
  }
}
