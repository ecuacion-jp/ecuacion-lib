# ecuacion-lib-validation

## What is it?

`ecuacion-lib-validation` provides customized validators for Jakarta Validation.

## Validators

| Category | Annotations |
| --- | --- |
| Conditional (When) | `@NotEmptyWhen`, `@EmptyWhen`, `@NotNullWhen`, `@NullWhen`, `@TrueWhen`, `@FalseWhen`, `@StringWhen`, `@NotStringWhen`, `@PatternWhen`, `@NotPatternWhen`, `@ValueOfPropertyPathWhen`, `@NotValueOfPropertyPathWhen` |
| Multi-field | `@AllEmptyOrAllNotEmpty`, `@AllNullOrAllNotNull`, `@AnyNotEmpty`, `@AnyEmpty`, `@AnyNotNull`, `@AnyNull` |
| Numeric comparison | `@GreaterThan`, `@GreaterThanOrEqualTo`, `@LessThan`, `@LessThanOrEqualTo` |
| Others | `@PatternWithDescription`, `@AssertTrueWithPropertyPath`, `@ReturnTrue`, ... |

## Usage Examples

**Conditional (When)** — validate a field only when another field has a specific value:

```java
@NotEmptyWhen(
    propertyPath = "companyName",
    conditionPropertyPath = "type",
    conditionValue = ConditionValue.STRING,
    conditionValueString = "CORPORATE"
)
public class RegistrationForm {
    private String type;
    private String companyName;  // required only when type == "CORPORATE"
}
```

**Multi-field** — validate relationships across multiple fields:

```java
@AllEmptyOrAllNotEmpty(propertyPath = {"startDate", "endDate"})
public class SearchForm {
    private String startDate;  // both must be filled in, or both must be empty
    private String endDate;
}
```

**Numeric comparison** — compare values between two fields:

```java
@GreaterThan(propertyPath = "endDate", baselinePropertyPath = "startDate")
public class SearchForm {
    private String startDate;
    private String endDate;  // must be greater than startDate
}
```

## System Requirements

- JDK 21 or above.

## Dependent Ecuacion Libraries

### Automatically Loaded Libraries

- `ecuacion-lib-core`

### Manual Load Needed Libraries

(none)

### Optional Libraries

- `ecuacion-lib-validation-business-messages` — adds business-friendly Japanese messages
  (e.g. `This field is required.`) for both Jakarta standard constraints and ecuacion-lib-validation
  constraints. Without it, neutral HV-aligned messages are used.

## Dependent External Libraries

### Automatically Loaded Libraries

(none)

### Manual Load Needed Libraries

(modules depending on `ecuacion-lib-core`)
- `jakarta.validation:jakarta.validation-api`
- (any `jakarta.validation:jakarta.validation-api` compatible Jakarta Validation libraries. `org.hibernate.validator:hibernate-validator` and `org.glassfish:jakarta.el` are recommended.)
- `jakarta.mail:jakarta.mail-api` (If you want to use the mail related utility: `jp.ecuacion.lib.core.util.MailUtil`)
- `org.slf4j:slf4j-api`
- (any `org.slf4j:slf4j-api` compatible logging libraries. `ch.qos.logback:logback-classic` is recommended.)

## Documentation

- [javadoc](https://javadoc.ecuacion.jp/apidocs/ecuacion-lib-validation/)

## Installation

Check [Installation](https://github.com/ecuacion-jp/ecuacion-lib) part of `README` page in `ecuacion-lib`.  
The description of dependent `ecuacion` modules is as follows.

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation</artifactId>
    <!-- Put the latest release version -->
    <version>x.x.x</version>
</dependency>
```
