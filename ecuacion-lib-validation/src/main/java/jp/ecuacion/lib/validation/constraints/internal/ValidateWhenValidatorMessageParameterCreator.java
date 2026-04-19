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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.jakartavalidation.bean.ValidatorMessageParameterCreator;
import jp.ecuacion.lib.core.util.ExceptionUtil.LocalizedEmbeddedParameter;
import jp.ecuacion.lib.core.util.MessageUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValue;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

public class ValidateWhenValidatorMessageParameterCreator extends ReflectionUtil
    implements ValidatorMessageParameterCreator {

  private static final String NULL = EclibValidationConstants.VALIDATOR_PARAMETER_NULL;

  @Override
  public Set<LocalizedEmbeddedParameter> create(ConstraintViolationBean<?> cv,
      Map<@NonNull String, Object> paramMap) {
    final String commonMessagePrefix = "jp.ecuacion.lib.validation.constraints.ValidateWhen";
    Set<LocalizedEmbeddedParameter> messageParameterSet = new HashSet<>();
    // conditionFieldItemNameKey
    String conditionPropertyPath = (StringUtils.isEmpty(cv.getPropertyPath().toString()) ? ""
        : cv.getPropertyPath().toString() + ".")
        + ((String) paramMap.get(ValidateWhenValidator.CONDITION_PROPERTY_PATH));
    Item item = MessageUtil.getItem(conditionPropertyPath, cv.getRootBean(),
        ConstraintViolationBean.getLeafBean(cv.getRootBean(), conditionPropertyPath));
    messageParameterSet
        .add(new LocalizedEmbeddedParameter(ValidateWhenValidator.CONDITION_PROPERTY_PATH_ITEM_NAME,
            new PropertiesFileUtilFileKindEnum[] {PropertiesFileUtilFileKindEnum.ITEM_NAMES}, true,
            new Item[] {item}, cv.getRootBean(), item.getItemNameKey(), new Arg[] {}));

    // displayStringOfConditionValue
    displayStringOfConditionValue(cv, paramMap, commonMessagePrefix, messageParameterSet);

    // validatesWhenConditionNotSatisfied
    // Each When-validator annotation must have exactly one parameter whose name ends with
    // "ConditionNotSatisfied". That convention allows automatic lookup without a switch statement.
    String conditionNotSatisfiedKey = Objects.requireNonNull(paramMap.keySet().stream()
        .filter(k -> k.endsWith("WhenConditionNotSatisfied")).toList().get(0));
    boolean bl = (boolean) Objects.requireNonNull(paramMap.get(conditionNotSatisfiedKey));

    String paramKey = ValidateWhenValidator.VALIDATES_WHEN_CONDITION_NOT_SATISFIED + "Description";
    if (bl) {
      messageParameterSet
          .add(
              new LocalizedEmbeddedParameter(paramKey,
                  new PropertiesFileUtilFileKindEnum[] {PropertiesFileUtilFileKindEnum.MESSAGES},
                  paramMap.get("annotation") + ".messagePart."
                      + ValidateWhenValidator.VALIDATES_WHEN_CONDITION_NOT_SATISFIED,
                  new Arg[] {}));

    } else {
      // Add blank ("") value by designating empty PropertiesFileUtilFileKindEnum array.
      messageParameterSet.add(new LocalizedEmbeddedParameter(paramKey,
          new PropertiesFileUtilFileKindEnum[] {}, "", new Arg[] {}));
    }

    return messageParameterSet;
  }

  private void displayStringOfConditionValue(ConstraintViolation<?> cv,
      Map<@NonNull String, Object> paramMap, final String commonMessagePrefix,
      Set<LocalizedEmbeddedParameter> messageParameterSet) {
    ConditionValue conditionPtn =
        (ConditionValue) paramMap.get(ValidateWhenValidator.CONDITION_VALUE);
    Arg displayStringOfConditionValueArg = Arg.string("");

    if (conditionPtn == VALUE_OF_PROPERTY_PATH) {
      Object values = getValue(cv.getLeafBean(), (String) Objects
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

      if (description.equals(NULL) || description.equals("")) {
        displayStringOfConditionValueArg = Arg.string(regExp);

      } else {
        displayStringOfConditionValueArg = Arg
            .get(new String[] {PropertiesFileUtilFileKindEnum.ITEM_NAMES.toString()}, description);
      }
    }

    String propKey = commonMessagePrefix + ".messagePart."
        + StringUtil.getLowerCamelFromSnake(
            Objects.requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_VALUE)).toString())
        + "." + StringUtil.getLowerCamelFromSnake(Objects
            .requireNonNull(paramMap.get(ValidateWhenValidator.CONDITION_OPERATOR)).toString());
    messageParameterSet
        .add(new LocalizedEmbeddedParameter(ValidateWhenValidator.DISPLAY_STRING_OF_CONDITION_VALUE,
            new PropertiesFileUtilFileKindEnum[] {PropertiesFileUtilFileKindEnum.MESSAGES}, propKey,
            new Arg[] {displayStringOfConditionValueArg}));
  }

  private Arg displayStringCommon(final String commonMessagePrefix, ConstraintViolation<?> cv,
      Map<@NonNull String, Object> paramMap, Object values) {
    String displayStringPp = (String) paramMap
        .get(ValidateWhenValidator.CONDITION_VALUE_PROPERTY_PATH_DISPLAY_STRING_PROPERTY_PATH);

    Object displayStringObj = Objects.requireNonNull(displayStringPp).equals("") ? values
        : Objects.requireNonNull(getValue(cv.getLeafBean(), displayStringPp));

    List<String> displayStringList = (displayStringObj instanceof Object[])
        ? Arrays.asList((Object[]) displayStringObj).stream().map(o -> o.toString()).toList()
        : Arrays.asList(new String[] {displayStringObj.toString()});

    Arg valueArg = displayStringPp.equals("")
        ? Arg.formattedString(MessageUtil.getValuesOfFormattedString(displayStringList))
        : MessageUtil.getValuesArg(displayStringList);

    String[] strs = displayStringList.toArray(new String[displayStringList.size()]);

    Arg displayStringOfConditionValueArg;
    displayStringOfConditionValueArg = strs.length > 1
        ? Arg.message(commonMessagePrefix + ".messagePart.string.multiple", valueArg)
        : valueArg;
    return displayStringOfConditionValueArg;
  }
}
