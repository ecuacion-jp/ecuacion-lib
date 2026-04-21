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

import jakarta.validation.ConstraintValidatorContext;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import jp.ecuacion.lib.core.util.ReflectionUtil;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.validation.constraints.enums.TypeConversionFromString;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides the validation logic for {@code EnumElement}.
 */
public abstract class ComparisonValidator<A extends Annotation, T> extends ClassValidator<A, T> {

  private String baselinePropertyPath = "";
  private boolean isValidWhenLessThanBasis;
  private boolean allowsEqual;
  // Put anything to avoid null error.
  private TypeConversionFromString typeConversionFromString = TypeConversionFromString.NONE;
  private String typeConversionDateTimeFormat = "";

  private @Nullable Field fieldOfBasisPropertyPath;
  private @Nullable Object valueOfBasisPropertyPath;

  /** Initializes an instance. */
  public void initialize(String message, String[] propertyPath, String baselinePropertyPath,
      boolean isValidWhenLessThanBasis, boolean allowsEqual,
      TypeConversionFromString typeConversionFromString, String typeConversionDateTimeFormat) {
    super.initialize(message, propertyPath);

    this.baselinePropertyPath = baselinePropertyPath;
    this.isValidWhenLessThanBasis = isValidWhenLessThanBasis;
    this.allowsEqual = allowsEqual;
    this.typeConversionFromString = typeConversionFromString;
    this.typeConversionDateTimeFormat = typeConversionDateTimeFormat;
  }

  /**
   * Executes validation check.
   */
  public boolean internalIsValid(Object instance, @Nullable ConstraintValidatorContext context) {

    procedureBeforeLoopForEachPropertyPath(instance);

    List<Pair<@NonNull String, Object>> valueOfFieldList = Arrays.asList(propertyPaths).stream()
        .map(path -> Pair.of(path, getValue(instance, path))).toList();

    for (Pair<@NonNull String, Object> pair : valueOfFieldList) {
      @SuppressWarnings("null")
      boolean result = isValidForSinglePropertyPath(instance, pair.getLeft(), pair.getRight());

      if (!result) {
        return false;
      }
    }

    return true;
  }

  protected void procedureBeforeLoopForEachPropertyPath(Object instance) {
    fieldOfBasisPropertyPath = ReflectionUtil.getField(instance.getClass(), baselinePropertyPath);
    valueOfBasisPropertyPath = getValue(instance, baselinePropertyPath);
  }

  protected boolean isValidForSinglePropertyPath(Object instance, String propertyPath,
      Object valueOfPropertyPath) {

    Field fieldOfPropertyPath = ReflectionUtil.getField(instance.getClass(), propertyPath);
    @NonNull
    Field nonNullFieldOfBasisPropertyPath = Objects.requireNonNull(fieldOfBasisPropertyPath);

    // Throws an exception when the types of two PropertyPaths differ.
    if (!fieldOfPropertyPath.getType()
        .isAssignableFrom(nonNullFieldOfBasisPropertyPath.getType())) {
      throw new RuntimeException("Types of two propertyPath differ. propertyPath: "
          + fieldOfPropertyPath.getType() + ", basisPropertyPath: "
          + Objects.requireNonNull(nonNullFieldOfBasisPropertyPath).getType());
    }

    // True when one of valueOfField or fieldOfBasisPropertyPath is empty.
    boolean isValueOfPropertyPathEmpty = StringUtil.isObjectNullOrEmpty(valueOfPropertyPath);
    boolean isValueOfBasisPropertyPathEmpty =
        StringUtil.isObjectNullOrEmpty(valueOfBasisPropertyPath);
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
        throw new RuntimeException(msg);
      }

      String valOfPp = (String) valueOfPropertyPath;
      String valOfBpp = Objects.requireNonNull((String) valueOfBasisPropertyPath);
      if (typeConversionFromString == TypeConversionFromString.NUMBER) {
        valueOfPropertyPath = new BigDecimal(valOfPp.replaceAll(",", ""));
        valueOfBasisPropertyPath = new BigDecimal(valOfBpp.replaceAll(",", ""));

      } else if (typeConversionFromString == TypeConversionFromString.DATE) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(typeConversionDateTimeFormat);
        valueOfPropertyPath = LocalDate.parse(valOfPp, fmt);
        valueOfBasisPropertyPath = LocalDate.parse(valOfBpp, fmt);
      }
    }

    @NonNull
    Object nonNullValueOfBasisPropertyPath = Objects.requireNonNull(valueOfBasisPropertyPath);

    // comparison of 2 values
    Boolean validWhenLessThanBasis = switch (valueOfPropertyPath) {
      case Long x -> x < (Long) nonNullValueOfBasisPropertyPath;
      case Integer x -> x < (Integer) nonNullValueOfBasisPropertyPath;
      case Short x -> x < (Short) nonNullValueOfBasisPropertyPath;
      case Byte x -> x < (Byte) nonNullValueOfBasisPropertyPath;
      case Double x -> x < (Double) nonNullValueOfBasisPropertyPath;
      case Float x -> x < (Float) nonNullValueOfBasisPropertyPath;
      case BigInteger x -> x.compareTo((BigInteger) nonNullValueOfBasisPropertyPath) < 0;
      case BigDecimal x -> x.compareTo((BigDecimal) nonNullValueOfBasisPropertyPath) < 0;
      case LocalDate x -> x.isBefore((LocalDate) nonNullValueOfBasisPropertyPath);
      case LocalDateTime x -> x.isBefore((LocalDateTime) nonNullValueOfBasisPropertyPath);
      case OffsetDateTime x -> x.isBefore((OffsetDateTime) nonNullValueOfBasisPropertyPath);
      case ZonedDateTime x -> x.isBefore((ZonedDateTime) valueOfBasisPropertyPath);
      case String x -> isStringValidWhenLessThanBasis(x, (String) nonNullValueOfBasisPropertyPath);
      default -> null;
    };

    // Throws an exception when rtn == null, which means the type of propertyPath is unexpected.
    if (validWhenLessThanBasis == null) {
      throw new RuntimeException("The type of propertyPath is unexpected. type: "
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
      throw new RuntimeException(e);
    }
  }
}
