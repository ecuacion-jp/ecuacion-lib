package jp.ecuacion.lib.core.jakartavalidation.validator.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import jp.ecuacion.lib.core.jakartavalidation.validator.Comparison;
import jp.ecuacion.lib.core.jakartavalidation.validator.GreaterThan;
import jp.ecuacion.lib.core.jakartavalidation.validator.GreaterThanOrEqualTo;
import jp.ecuacion.lib.core.jakartavalidation.validator.LessThan;
import jp.ecuacion.lib.core.jakartavalidation.validator.LessThanOrEqualTo;

@SuppressWarnings("unused")
public class ComparisonTestBean {

  public static class Irregular {

    @Comparison(propertyPath = {"propertyPath", "propertyPath2"},
        basisPropertyPath = "basisPropertyPath")
    public static class PropertyPathNotExist {
      private int propertyPath;
      private int basisPropertyPath;
    }

    @Comparison(propertyPath = {"propertyPath", "propertyPath2"},
        basisPropertyPath = "basisPropertyPath")
    public static class BasisPropertyPathNotExist {
      private int propertyPath;
    }

    @Comparison(propertyPath = {"propertyPath1", "propertyPath2"},
        basisPropertyPath = "basisPropertyPath")
    public static class TypesDifferBetweenPropertyPathAndBasisPropertyPath {
      private int propertyPath1;
      private long propertyPath2;
      private String basisPropertyPath;
    }

    @Comparison(propertyPath = "propertyPath", basisPropertyPath = "basisPropertyPath")
    public static class UnsupportedType {
      private ComparisonTestBean propertyPath = new ComparisonTestBean();
      private ComparisonTestBean basisPropertyPath = new ComparisonTestBean();
    }
  }

  public static class ValidCheck {
    private static class Bean {
      long ppLong = 0, bppLong = 1;
      Integer ppInt = -1, bppInt = 1;
      short ppShort = 0, bppShort = 1;
      byte ppByte = 0, bppByte = 1;
      double ppDouble = 0D, bppDouble = 0.1;
      Float ppFloat = 0.0F, bppFloat = 0.1F;
      BigInteger ppBigInteger = new BigInteger("0"), bppBigInteger = BigInteger.ONE;
      BigDecimal ppBigDecimal = new BigDecimal("0.1"), bppBigDecimal = new BigDecimal("0.2");
      LocalDate ppLocalDate = LocalDate.MIN, bppLocalDate = LocalDate.MAX;
      LocalDateTime ppLocalDateTime = LocalDateTime.MIN, bppLocalDateTime = LocalDateTime.MAX;
      OffsetDateTime ppOffsetDateTime = OffsetDateTime.MIN, bppOffsetDateTime = OffsetDateTime.MAX;
      ZonedDateTime ppZonedDateTime = ZonedDateTime.now(),
          bppZonedDateTime = ZonedDateTime.now().plus(1, ChronoUnit.MINUTES);
      String ppString = "a", bppString = "b";
    }

    @Comparison(propertyPath = "ppLong", basisPropertyPath = "bppLong")
    @Comparison(propertyPath = "ppInt", basisPropertyPath = "bppInt")
    @Comparison(propertyPath = "ppShort", basisPropertyPath = "bppShort")
    @Comparison(propertyPath = "ppByte", basisPropertyPath = "bppByte")
    @Comparison(propertyPath = "ppDouble", basisPropertyPath = "bppDouble")
    @Comparison(propertyPath = "ppFloat", basisPropertyPath = "bppFloat")
    @Comparison(propertyPath = "ppBigInteger", basisPropertyPath = "bppBigInteger")
    @Comparison(propertyPath = "ppBigDecimal", basisPropertyPath = "bppBigDecimal")
    @Comparison(propertyPath = "ppLocalDate", basisPropertyPath = "bppLocalDate")
    @Comparison(propertyPath = "ppLocalDateTime", basisPropertyPath = "bppLocalDateTime")
    @Comparison(propertyPath = "ppOffsetDateTime", basisPropertyPath = "bppOffsetDateTime")
    @Comparison(propertyPath = "ppZonedDateTime", basisPropertyPath = "bppZonedDateTime")
    @Comparison(propertyPath = "ppString", basisPropertyPath = "bppString")
    public static class ValidWhenLessThanBasisBean extends Bean {

    }
    
    @Comparison(propertyPath = "ppLong", basisPropertyPath = "bppLong", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppInt", basisPropertyPath = "bppInt", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppShort", basisPropertyPath = "bppShort", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppByte", basisPropertyPath = "bppByte", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppDouble", basisPropertyPath = "bppDouble", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppFloat", basisPropertyPath = "bppFloat", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppBigInteger", basisPropertyPath = "bppBigInteger", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppBigDecimal", basisPropertyPath = "bppBigDecimal", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppLocalDate", basisPropertyPath = "bppLocalDate", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppLocalDateTime", basisPropertyPath = "bppLocalDateTime", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppOffsetDateTime", basisPropertyPath = "bppOffsetDateTime", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppZonedDateTime", basisPropertyPath = "bppZonedDateTime", isValidWhenLessThanBasis = false)
    @Comparison(propertyPath = "ppString", basisPropertyPath = "bppString", isValidWhenLessThanBasis = false)
    public static class ValidWhenGreaterThanBasisBean extends Bean {

    }
    
    /** one test case for each kind of types */
    private static class EqualBean {
      long ppLong = 0, bppLong = 0;
      Float ppFloat = 1.2F, bppFloat = 1.2F;
      BigDecimal ppBigDecimal = new BigDecimal("0.1"), bppBigDecimal = new BigDecimal("0.1");
      OffsetDateTime ppOffsetDateTime = OffsetDateTime.MIN, bppOffsetDateTime = OffsetDateTime.MIN;
      String ppString = "a", bppString = "a";
    }
    
    @Comparison(propertyPath = "ppLong", basisPropertyPath = "bppLong")
    @Comparison(propertyPath = "ppFloat", basisPropertyPath = "bppFloat")
    @Comparison(propertyPath = "ppBigDecimal", basisPropertyPath = "bppBigDecimal")
    @Comparison(propertyPath = "ppOffsetDateTime", basisPropertyPath = "bppOffsetDateTime")
    @Comparison(propertyPath = "ppString", basisPropertyPath = "bppString")
    public static class EqualAllowedBean extends EqualBean {
      
    }
    
    @Comparison(propertyPath = "ppLong", basisPropertyPath = "bppLong", allowsEqual = false)
    @Comparison(propertyPath = "ppFloat", basisPropertyPath = "bppFloat", allowsEqual = false)
    @Comparison(propertyPath = "ppBigDecimal", basisPropertyPath = "bppBigDecimal", allowsEqual = false)
    @Comparison(propertyPath = "ppOffsetDateTime", basisPropertyPath = "bppOffsetDateTime", allowsEqual = false)
    @Comparison(propertyPath = "ppString", basisPropertyPath = "bppString", allowsEqual = false)
    public static class EqualNotAllowedBean extends EqualBean {
      
    }
  }
  
  public static class EachAnnotation {
    private static class Bean {
      private int one = 1;
      private int two = 2;
    }
    
    @LessThan(propertyPath = "one", basisPropertyPath = "two")
    @LessThanOrEqualTo(propertyPath = "one", basisPropertyPath = "two")
    @LessThanOrEqualTo(propertyPath = "one", basisPropertyPath = "one")
    @GreaterThan(propertyPath = "two", basisPropertyPath = "one")
    @GreaterThanOrEqualTo(propertyPath = "two", basisPropertyPath = "one")
    @GreaterThanOrEqualTo(propertyPath = "one", basisPropertyPath = "one")
    public static class Valid extends Bean {
      
    }
    
    @LessThan(propertyPath = "one", basisPropertyPath = "one")
    @LessThan(propertyPath = "two", basisPropertyPath = "one")
    @LessThanOrEqualTo(propertyPath = "two", basisPropertyPath = "one")
    @GreaterThan(propertyPath = "one", basisPropertyPath = "one")
    @GreaterThan(propertyPath = "one", basisPropertyPath = "two")
    @GreaterThanOrEqualTo(propertyPath = "one", basisPropertyPath = "two")
    public static class Invalid extends Bean {
      
    }
  }
}
