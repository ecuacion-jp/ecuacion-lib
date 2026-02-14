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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import jp.ecuacion.lib.validation.constraints.enums.TypeConversionFromString;

/**
 * Provides the validation logic for {@code EnumElement}.
 */
public abstract class ComparisonValidator extends ClassValidator {

  private String basisPropertyPath;
  private boolean isValidWhenLessThanBasis;
  private boolean allowsEqual;
  private TypeConversionFromString typeConversionFromString;

  private Field fieldOfBasisPropertyPath;
  private Object valueOfBasisPropertyPath;

  /** Initializes an instance. */
  public void initialize(String[] propertyPath, String basisPropertyPath,
      boolean isValidWhenLessThanBasis, boolean allowsEqual,
      TypeConversionFromString typeConversionFromString) {
    super.initialize(propertyPath);

    this.basisPropertyPath = basisPropertyPath;
    this.isValidWhenLessThanBasis = isValidWhenLessThanBasis;
    this.allowsEqual = allowsEqual;
    this.typeConversionFromString = typeConversionFromString;
  }

  @Override
  protected void procedureBeforeLoopForEachPropertyPath() {
    fieldOfBasisPropertyPath = ReflectionUtil.getField(instance.getClass(), basisPropertyPath);
    valueOfBasisPropertyPath = getValue(instance, basisPropertyPath);
  }

  @Override
  protected boolean isValidForSinglePropertyPath(String propertyPath, Object valueOfPropertyPath) {
    Field fieldOfPropertyPath = ReflectionUtil.getField(instance.getClass(), propertyPath);

    // Throws an exception when the types of two PropertyPaths differ.
    if (!fieldOfPropertyPath.getType().isAssignableFrom(fieldOfBasisPropertyPath.getType())) {
      throw new EclibRuntimeException(
          "Types of two propertyPath differ. propertyPath: " + fieldOfPropertyPath.getType()
              + ", basisPropertyPath: " + fieldOfBasisPropertyPath.getType());
    }

    // True when one of valueOfField or fieldOfBasisPropertyPath is empty.
    boolean isValueOfPropertyPathEmpty = valueOfPropertyPath == null
        || (valueOfPropertyPath instanceof String && ((String) valueOfPropertyPath).equals(""));
    boolean isValueOfBasisPropertyPathEmpty =
        valueOfBasisPropertyPath == null || (valueOfBasisPropertyPath instanceof String
            && ((String) valueOfBasisPropertyPath).equals(""));
    if (isValueOfPropertyPathEmpty || isValueOfBasisPropertyPathEmpty) {
      return true;
    }

    // same values treatment
    if (valueOfPropertyPath.equals(valueOfBasisPropertyPath)) {
      return allowsEqual;
    }

    // type conversion for record classes
    if (typeConversionFromString != TypeConversionFromString.NONE) {
      // Throw an exception when the type of valueOfPropertyPath is not string.
      if (!(valueOfPropertyPath instanceof String)) {
        String msg = "The type of propertyPath "
            + "needs to be String when typeConversionFromString is not 'NONE'.";
        throw new EclibRuntimeException(msg);
      }

      String valOfPp = (String) valueOfPropertyPath;
      String valOfBpp = (String) valueOfBasisPropertyPath;
      if (typeConversionFromString == TypeConversionFromString.NUMBER) {
        valueOfPropertyPath = Double.valueOf(valOfPp.replaceAll(",", ""));
        valueOfBasisPropertyPath = Double.valueOf(valOfBpp.replaceAll(",", ""));

      } else if (typeConversionFromString == TypeConversionFromString.DATE) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        valueOfPropertyPath = LocalDate.parse(valOfPp, fmt);
        valueOfBasisPropertyPath = LocalDate.parse(valOfBpp, fmt);
      }
    }

    // comparison of 2 values
    Boolean validWhenLessThanBasis = switch (valueOfPropertyPath) {
      case Long x -> x < (Long) valueOfBasisPropertyPath;
      case Integer x -> x < (Integer) valueOfBasisPropertyPath;
      case Short x -> x < (Short) valueOfBasisPropertyPath;
      case Byte x -> x < (Byte) valueOfBasisPropertyPath;
      case Double x -> x < (Double) valueOfBasisPropertyPath;
      case Float x -> x < (Float) valueOfBasisPropertyPath;
      case BigInteger x -> x.compareTo((BigInteger) valueOfBasisPropertyPath) < 0;
      case BigDecimal x -> x.compareTo((BigDecimal) valueOfBasisPropertyPath) < 0;
      case LocalDate x -> x.isBefore((LocalDate) valueOfBasisPropertyPath);
      case LocalDateTime x -> x.isBefore((LocalDateTime) valueOfBasisPropertyPath);
      case OffsetDateTime x -> x.isBefore((OffsetDateTime) valueOfBasisPropertyPath);
      case ZonedDateTime x -> x.isBefore((ZonedDateTime) valueOfBasisPropertyPath);
      case String x -> isStringValidWhenLessThanBasis(x, (String) valueOfBasisPropertyPath);
      default -> null;
    };

    // Throws an exception when rtn == null, which means the type of propertyPath is unexpected.
    if (validWhenLessThanBasis == null) {
      throw new EclibRuntimeException("The type of propertyPath is unexpected. type: "
          + fieldOfPropertyPath.getType().getCanonicalName());
    }

    return isValidWhenLessThanBasis ? validWhenLessThanBasis : !validWhenLessThanBasis;
  }

  protected boolean isStringValidWhenLessThanBasis(String x1, String x2) {
    try {
      byte[] bytesPropertyPath = x1.getBytes("UTF-8");
      byte[] bytesBasisPropertyPath = ((String) x2).getBytes("UTF-8");

      for (int i = 0; i < bytesPropertyPath.length; i++) {
        byte bytePropertyPath = bytesPropertyPath[i];

        if (bytesBasisPropertyPath.length <= i) {
          // It's the case when bytesPropertyPath = "ab" and bytesBasisPropertyPath = "a".
          // Return NOT VALID in this case.
          return false;
        }

        byte byteBasisPropertyPath = bytesBasisPropertyPath[i];

        if (bytePropertyPath != byteBasisPropertyPath) {
          return bytePropertyPath < byteBasisPropertyPath;
        }
      }

      // It's the case when bytesPropertyPath = "a" and bytesBasisPropertyPath = "ab".
      // Return VALID in this case.
      return true;

    } catch (UnsupportedEncodingException e) {
      throw new EclibRuntimeException(e);
    }
  }
}
