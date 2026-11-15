# Comparison Validators

## Overview

Comparison annotations validate the relative order of two fields. These are class-level annotations.

```java
@LessThan(
    propertyPath = "startDate",
    baselinePropertyPath = "endDate"
)
public class PeriodForm { ... }
```

Validation is skipped when either field is null or empty.

---

## List of Annotations

| Annotation | Relationship |
| ------------- | ---- |
| `@GreaterThan` | `propertyPath` > `baselinePropertyPath` |
| `@GreaterThanOrEqualTo` | `propertyPath` >= `baselinePropertyPath` |
| `@LessThan` | `propertyPath` < `baselinePropertyPath` |
| `@LessThanOrEqualTo` | `propertyPath` <= `baselinePropertyPath` |

---

## Key Attributes

### propertyPath — Source Field for Comparison

```java
propertyPath = "startDate"
```

Specifying multiple fields as an array compares each field against `baselinePropertyPath`.

### baselinePropertyPath — Reference Field

```java
baselinePropertyPath = "endDate"
```

### typeConversionFromString — Type Conversion from String

When a field value is stored as a string, type conversion is performed before comparison.

| `TypeConversionFromString` | Meaning |
| --------------------------- | ---- |
| `NONE` (default) | No conversion (compares numeric and date types directly) |
| `NUMBER` | Converts string to number before comparison |
| `DATE` | Converts string to date before comparison |

```java
@LessThan(
    propertyPath = "startDateStr",
    baselinePropertyPath = "endDateStr",
    typeConversionFromString = TypeConversionFromString.DATE,
    typeConversionDateFormat = "yyyy/MM/dd"
)
```

### typeConversionDateFormat

The format to use when `typeConversionFromString = DATE`. Default is `"yyyy-MM-dd"`.

---

## Supported Types

Numeric types: `Long`, `Integer`, `Short`, `Byte`, `Double`, `Float`, `BigInteger`, `BigDecimal`

Date types: `LocalDate`, `LocalDateTime`, `OffsetDateTime`, `ZonedDateTime`

String: byte comparison

---

## Usage Examples

### Date Order Check

```java
@LessThan(
    propertyPath = "startDate",
    baselinePropertyPath = "endDate"
)
public class EventForm { ... }
```

### Numeric Range Check

```java
@LessThanOrEqualTo(
    propertyPath = "minPrice",
    baselinePropertyPath = "maxPrice"
)
public class PriceRangeForm { ... }
```

### Comparing Dates Stored as Strings

```java
@LessThan(
    propertyPath = "startDateStr",
    baselinePropertyPath = "endDateStr",
    typeConversionFromString = TypeConversionFromString.DATE
)
public class SearchForm { ... }
```
