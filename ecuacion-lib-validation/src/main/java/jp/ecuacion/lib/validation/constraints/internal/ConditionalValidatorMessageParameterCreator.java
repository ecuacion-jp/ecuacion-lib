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

import static jp.ecuacion.lib.validation.constraints.enums.ConditionValuePattern.string;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValuePattern.valueOfPropertyPath;

import jakarta.validation.ConstraintViolation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean.FieldInfoBean;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean.LocalizedMessageParameter;
import jp.ecuacion.lib.core.jakartavalidation.bean.ValidatorMessageParameterCreator;
import jp.ecuacion.lib.core.util.PropertyFileUtil.Arg;
import jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.validation.constraints.enums.ConditionValuePattern;
import org.apache.commons.lang3.StringUtils;

public class ConditionalValidatorMessageParameterCreator extends ReflectionUtil
    implements ValidatorMessageParameterCreator {


  @Override
  public Set<LocalizedMessageParameter> create(ConstraintViolation<?> cv,
      Map<String, Object> paramMap, String rootRecordNameForForm) {
    final String annotationPrefix = "jp.ecuacion.lib.validation.constraints.Conditional";
    Set<LocalizedMessageParameter> messageParameterSet = new HashSet<>();
    final String validatorClass = (String) paramMap.get("annotation");

    // conditionFieldItemNameKey
    String conditionPropertyPath = (StringUtils.isEmpty(cv.getPropertyPath().toString()) ? ""
        : cv.getPropertyPath().toString() + ".")
        + ((String) paramMap.get(ConditionalValidator.CONDITION_PROPERTY_PATH));
    FieldInfoBean bean = ConstraintViolationBean.getItemDependentValues(
        conditionPropertyPath, ConstraintViolationBean
            .getLeafBean(cv.getRootBean(), conditionPropertyPath).getClass(),
        cv.getRootBean(), rootRecordNameForForm);
    messageParameterSet
        .add(new LocalizedMessageParameter(ConditionalValidator.CONDITION_PROPERTY_PATH_ITEM_NAME,
            new PropertyFileUtilFileKindEnum[] {PropertyFileUtilFileKindEnum.ITEM_NAMES},
            bean.itemNameKey));

    // displayStringOfConditionValue
    ConditionValuePattern conditionPtn =
        (ConditionValuePattern) paramMap.get(ConditionalValidator.CONDITION_PATTERN);
    String[] fileKinds = new String[] {PropertyFileUtilFileKindEnum.MESSAGES.toString(),
        PropertyFileUtilFileKindEnum.ITEM_NAMES.toString(),
        PropertyFileUtilFileKindEnum.ENUM_NAMES.toString()};
    Arg displayStringOfConditionValueArg = Arg.string("");

    if (conditionPtn == valueOfPropertyPath) {
      Object obj = getValue(cv.getLeafBean(),
          (String) paramMap.get(ConditionalValidator.CONDITION_VALUE_PROPERTY_PATH));
      String displayStringOfConditionValue = null;

      if (obj instanceof Object[]) {
        List<String> strList = Arrays.asList(obj).stream().map(o -> o.toString()).toList();
        displayStringOfConditionValue =
            StringUtil.getCsvWithSpace((String[]) strList.toArray(new String[strList.size()]));

      } else {
        // String
        displayStringOfConditionValue = String.valueOf(obj);
      }

      displayStringOfConditionValueArg = Arg.get(fileKinds, displayStringOfConditionValue);

    } else if (conditionPtn == string) {
      // conditionValue is used
      String[] strs = (String[]) paramMap.get(ConditionalValidator.CONDITION_VALUE_STRING);
      Arg valueArg = Arg.string(StringUtil.getCsvWithSpace(strs));
      displayStringOfConditionValueArg =
          strs.length > 1 ? Arg.message(annotationPrefix + ".messagePart.string.multiple", valueArg)
              : valueArg;
    }

    // when fieldHoldingConditionValueDisplayName is not blank,
    // valuesOfConditionFieldToValidate is overrided by its value.
    String displayStringPropertyPathOfConditionValuePropertyPath = (String) paramMap
        .get(ConditionalValidator.DISPLAY_STRING_PROPERTY_PATH_OF_CONDITION_VALUE_PROPERTY_PATH);
    if (!displayStringPropertyPathOfConditionValuePropertyPath.equals("")) {
      Object obj =
          getValue(cv.getLeafBean(), displayStringPropertyPathOfConditionValuePropertyPath);

      String[] strs = obj instanceof String[] ? (String[]) obj : new String[] {((String) obj)};
      displayStringOfConditionValueArg = Arg.get(fileKinds, StringUtil.getCsvWithSpace(strs));
    }

    String propKey =
        annotationPrefix + ".messagePart." + paramMap.get(ConditionalValidator.CONDITION_PATTERN)
            + "." + paramMap.get(ConditionalValidator.CONDITION_OPERATOR);
    messageParameterSet
        .add(new LocalizedMessageParameter(ConditionalValidator.DISPLAY_STRING_OF_CONDITION_VALUE,
            new PropertyFileUtilFileKindEnum[] {PropertyFileUtilFileKindEnum.MESSAGES}, propKey,
            displayStringOfConditionValueArg));

    // validatesWhenConditionNotSatisfied
    boolean bl = validatorClass.endsWith("ConditionalEmpty")
        && (Boolean) paramMap.get("notEmptyWhenConditionNotSatisfied")
        || validatorClass.endsWith("ConditionalNotEmpty")
            && (Boolean) paramMap.get("emptyWhenConditionNotSatisfied");
    String paramKey = ConditionalValidator.VALIDATES_WHEN_CONDITION_NOT_SATISFIED + "Description";
    if (bl) {
      messageParameterSet.add(new LocalizedMessageParameter(paramKey,
          new PropertyFileUtilFileKindEnum[] {PropertyFileUtilFileKindEnum.MESSAGES},
          paramMap.get("annotation") + ".messagePart."
              + ConditionalValidator.VALIDATES_WHEN_CONDITION_NOT_SATISFIED));

    } else {
      // Add blank ("") value by designating empty PropertyFileUtilFileKindEnum array.
      messageParameterSet.add(
          new LocalizedMessageParameter(paramKey, new PropertyFileUtilFileKindEnum[] {}, null));
    }

    return messageParameterSet;
  }

}
