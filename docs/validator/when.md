# Conditional Validators

## Overview

When annotations express conditional rules such as "when `conditionPropertyPath` is in a certain state,
`propertyPath` must satisfy a certain condition." These are class-level annotations.

```java
@TrueWhen(
    propertyPath = "agreedToTerms",
    conditionPropertyPath = "accountType",
    conditionValueState = ConditionValueState.NOT_EMPTY
)
public class RegistrationForm { ... }
```

---

## List of Annotations

### Basic Conditions (No Value of Their Own)

| Annotation | Condition that `propertyPath` Must Satisfy |
| ------------- | ------------------------------- |
| `@TrueWhen` | Must be `true` |
| `@FalseWhen` | Must be `false` |
| `@NullWhen` | Must be `null` |
| `@NotNullWhen` | Must not be `null` |
| `@EmptyWhen` | Must be empty (null or blank string) |
| `@NotEmptyWhen` | Must not be empty |

### Pattern, String, and Value-Reference Conditions

These annotations have their own parameters in addition to `conditionValue`.

| Annotation | Condition that `propertyPath` Must Satisfy | Specific Parameter |
| ------------- | ------------------------------- | ------------- |
| `@PatternWhen` | Must match a regular expression | `regexp` |
| `@NotPatternWhen` | Must not match a regular expression | `regexp` |
| `@StringWhen` | Must be one of the specified strings | `string[]` |
| `@NotStringWhen` | Must not be any of the specified strings | `string[]` |
| `@ValueOfPropertyPathWhen` | Must have the same value as another field | `valuePropertyPath` |
| `@NotValueOfPropertyPathWhen` | Must have a different value from another field | `valuePropertyPath` |

> **Note:** `@PatternWhen` / `@NotPatternWhen`'s `regexp`, and `conditionValuePatternRegexp` (see
> [conditionValue](#conditionvalue--type-of-condition) below), are matched against a value that
> may come from end-user input. Avoid patterns prone to catastrophic backtracking (e.g. nested
> quantifiers like `(a+)+`), which can make matching take exponential time on a crafted input.

---

## Key Attributes

### propertyPath — Field to Validate

```java
propertyPath = "agreedToTerms"            // Single field
propertyPath = {"startDate", "endDate"}   // Multiple fields validated with the same rule
```

### conditionPropertyPath — Condition Field

The field name used for evaluating the condition. Nested fields can also be specified.
To specify multiple conditions, use `conditions` (see below) instead.

```java
conditionPropertyPath = "accountType"
conditionPropertyPath = "address.country"
```

### conditionValue — Type of Condition

Specifies the state of the `conditionPropertyPath` field that triggers validation.

| `ConditionValue` | Meaning | Attribute That Determines It |
| ---------------- | ---- | --------------- |
| `NULL` | Is `null` | `conditionValueState = ConditionValueState.NULL` |
| `NOT_NULL` | Is not `null` | `conditionValueState = ConditionValueState.NOT_NULL` |
| `EMPTY` | Is empty (null or blank string) | `conditionValueState = ConditionValueState.EMPTY` |
| `NOT_EMPTY` | Is not empty | `conditionValueState = ConditionValueState.NOT_EMPTY` |
| `TRUE` | Is `true` | `conditionValueBoolean = true` |
| `FALSE` | Is `false` | `conditionValueBoolean = false` |
| `STRING` | Is one of the specified strings | `conditionValueString` |
| `PATTERN` | Matches a regular expression | `conditionValuePatternRegexp` |
| `VALUE_OF_PROPERTY_PATH` | Has the same value as another field | `conditionValuePropertyPath` |

`STRING` / `PATTERN` / `VALUE_OF_PROPERTY_PATH` additionally accept
`conditionValueDisplayStringPropertyPath` (see below), and `PATTERN` also accepts
`conditionValuePatternDescription`.

> **Note:** `conditionValue` itself can normally be omitted: setting exactly one of
> `conditionValueString`, `conditionValuePatternRegexp`, `conditionValuePropertyPath`,
> `conditionValueBoolean` or `conditionValueState` already determines it unambiguously, so in
> practice you never need to write `conditionValue = ...` — just set whichever of those five
> attributes matches the condition you mean. Setting more than one of them at the same time, or
> setting none of them, is an error. Explicitly setting `conditionValue` on top of one of the
> five is still allowed and is checked for consistency with it (e.g. `conditionValue = TRUE`
> together with `conditionValueBoolean = false` is rejected).

### conditionOperator — Condition Operator

| `ConditionOperator` | Meaning |
| ------------------- | ---- |
| `EQUAL_TO` (default) | Validates when the condition is met |
| `NOT_EQUAL_TO` | Validates when the condition is not met |

### conditionValuePatternDescription

When `conditionValue = PATTERN`, specifies a human-readable description to display in error messages
instead of the regular expression set in `conditionValuePatternRegexp`.
If omitted, the regular expression is displayed as-is.

```java
// Without conditionValuePatternDescription
// → When 'phone number' matches '^(070|080|090).*' ...

// With conditionValuePatternDescription
@TrueWhen(
    propertyPath = "smsConsentAgreed",
    conditionPropertyPath = "phone",
    conditionValuePatternRegexp = "^(070|080|090).*",
    conditionValuePatternDescription = "mobile phone number"  // Description used in message
)
// → When 'phone number' matches 'mobile phone number' ...
```

### conditionValueDisplayStringPropertyPath

For `STRING` / `PATTERN` / `VALUE_OF_PROPERTY_PATH`, specifies another field's itemNameKey
to resolve the "display name of the condition value" to show in error messages.
If omitted, the actual value (string, regex, or field value) is displayed as-is.

### conditionValueBoolean / conditionValueState

For the 6 conditions that have no value of their own to distinguish them, these two attributes
let you specify them without writing `conditionValue` explicitly:

```java
conditionValueBoolean = true                              // conditionValue = TRUE
conditionValueBoolean = false                              // conditionValue = FALSE
conditionValueState = ConditionValueState.NULL             // conditionValue = NULL
conditionValueState = ConditionValueState.NOT_NULL         // conditionValue = NOT_NULL
conditionValueState = ConditionValueState.EMPTY            // conditionValue = EMPTY
conditionValueState = ConditionValueState.NOT_EMPTY        // conditionValue = NOT_EMPTY
```

### falseWhenConditionNotSatisfied

When set to `true`, applies the reverse rule when the condition is not met
(for `@TrueWhen`, validates that it is `false` when the condition is not met). Default is `false`.

### conditions — Multiple Conditions (AND)

To specify two or more conditions, list `@Condition` annotations in `conditions` instead of using
`conditionPropertyPath` and the other `condition*` attributes. Validation is executed
**when all the conditions are met** (AND).

```java
@NotEmptyWhen(propertyPath = "otherTypeDetail", conditions = {
    @Condition(propertyPath = "type", valueString = "OTHER"),
    @Condition(propertyPath = "status", valueState = ConditionValueState.NOT_EMPTY)})
// → needs to be not empty when 'type' is 'OTHER' and 'status' is not empty
```

Each attribute of `@Condition` has the same meaning as the When annotation's attribute of the same
name with the `condition` prefix (`propertyPath`, `operator`, `valueString`, `valuePatternRegexp`,
`valuePatternDescription`, `valuePropertyPath`, `valueBoolean`, `valueState`,
`valueDisplayStringPropertyPath`).

- Setting `conditions` together with `conditionPropertyPath` or the other `condition*` attributes
  is an error. Setting neither of them is also an error.
- OR conditions are not supported (Java annotations cannot nest themselves, so arbitrary logical
  expressions cannot be expressed). For OR, put multiple When annotations instead.
- When `falseWhenConditionNotSatisfied` or its counterpart is `true`, the reverse rule is applied
  unless all the conditions are met (that is, when at least one of them is not met).

---

## Usage Examples

### Basic Example — Conditional Required

```java
// When accountType is filled in, agreedToTerms must be true
// conditionValue is inferred as NOT_EMPTY from conditionValueState, so it can be omitted
@TrueWhen(
    propertyPath = "agreedToTerms",
    conditionPropertyPath = "accountType",
    conditionValueState = ConditionValueState.NOT_EMPTY
)
public class RegistrationForm { ... }
```

### STRING Condition — When a Specific Value

```java
// When role is "ADMIN", adminCode must not be empty
// conditionValue is inferred as STRING from conditionValueString, so it can be omitted
@NotEmptyWhen(
    propertyPath = "adminCode",
    conditionPropertyPath = "role",
    conditionValueString = {"ADMIN"}
)
public class UserForm { ... }
```

### NOT_EQUAL_TO — Validate When Condition Is Not Met

```java
// When the submission confirmation flag is not true, reason must not be empty
// conditionValue is inferred as TRUE from conditionValueBoolean, so it can be omitted
@NotEmptyWhen(
    propertyPath = "reason",
    conditionPropertyPath = "confirmed",
    conditionValueBoolean = true,
    conditionOperator = ConditionOperator.NOT_EQUAL_TO
)
public class CancelForm { ... }
```

### @PatternWhen — Validate with Regex (regexp)

```java
// When type is "POSTAL", code must be a 7-digit number
// conditionValue is inferred as STRING from conditionValueString, so it can be omitted
@PatternWhen(
    propertyPath = "code",
    regexp = "\\d{7}",
    conditionPropertyPath = "type",
    conditionValueString = {"POSTAL"}
)
public class AddressForm { ... }
```

### @StringWhen — Validate That Value Is a Specific String (string[])

```java
// When role is NOT_EMPTY, accountType must be "ADMIN" or "OPERATOR"
@StringWhen(
    propertyPath = "accountType",
    string = {"ADMIN", "OPERATOR"},
    conditionPropertyPath = "role",
    conditionValueState = ConditionValueState.NOT_EMPTY
)
public class UserForm { ... }
```

### @ValueOfPropertyPathWhen — Validate That Value Matches Another Field (valuePropertyPath)

```java
// When confirmed is true, confirmPassword must have the same value as password
@ValueOfPropertyPathWhen(
    propertyPath = "confirmPassword",
    valuePropertyPath = "password",
    conditionPropertyPath = "confirmed",
    conditionValueBoolean = true
)
public class PasswordForm { ... }
```
