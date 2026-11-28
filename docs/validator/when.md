# Conditional Validators

## Overview

When annotations express conditional rules such as "when `conditionPropertyPath` is in a certain state,
`propertyPath` must satisfy a certain condition." These are class-level annotations.

```java
// When applying as a corporation (corporate is true), companyName must not be empty
@NotEmptyWhen(
    propertyPath = "companyName",
    conditionPropertyPath = "corporate",
    conditionValueBoolean = true
)
public class ApplicationForm { ... }
```

---

## Key Attributes

These attributes are required and common to all When annotations.

### propertyPath — Field to Validate

```java
propertyPath = "agreedToTerms"            // Single field
propertyPath = {"startDate", "endDate"}   // Multiple fields validated with the same rule
```

### conditionPropertyPath — Condition Field

The field name used for evaluating the condition. Nested fields can also be specified.
To specify multiple conditions, use `conditions` (see [Multiple Conditions](#conditions--multiple-conditions-and)) instead.

```java
conditionPropertyPath = "accountType"
conditionPropertyPath = "address.country"
```

### conditionValueXxx — Condition Value

Specifies the state of the `conditionPropertyPath` field that triggers validation,
using an attribute named `conditionValueXxx`
(for example, `conditionValueBoolean = true` to validate when `conditionPropertyPath` is `true`).
See [Specifying the Condition Value](#specifying-the-condition-value) for details.

---

## Validators and Examples

### List of Validators

The When annotations are listed below. They differ in what they validate for `propertyPath`.

| Annotation | Condition that `propertyPath` Must Satisfy | Specific Attributes |
| ------------- | ------------------------------- | ------------- |
| `@TrueWhen` | Must be `true` | None |
| `@FalseWhen` | Must be `false` | None |
| `@NullWhen` | Must be `null` | None |
| `@NotNullWhen` | Must not be `null` | None |
| `@EmptyWhen` | Must be empty (null or blank string) | None |
| `@NotEmptyWhen` | Must not be empty | None |
| `@PatternWhen` | Must match a regular expression | `regexp`, `description` |
| `@NotPatternWhen` | Must not match a regular expression | `regexp`, `description` |
| `@StringWhen` | Must be one of the specified strings | `string` |
| `@NotStringWhen` | Must not be any of the specified strings | `string` |
| `@ValueOfPropertyPathWhen` | Must have the same value as another field | `valuePropertyPath` |
| `@NotValueOfPropertyPathWhen` | Must have a different value from another field | `valuePropertyPath` |

### Examples

Positive and negative pairs (such as `@TrueWhen` and `@FalseWhen`) are shown in one example.
The negative one only reverses what is validated, and is written in the same way.
The examples use the simple `conditionValueBoolean` / `conditionValueState` for the condition value
(see [Specifying the Condition Value](#specifying-the-condition-value) for details).

#### @TrueWhen / @FalseWhen

```java
// When a coupon code is entered, agreement to the coupon terms (agreedToCouponTerms) must be true
@TrueWhen(
    propertyPath = "agreedToCouponTerms",
    conditionPropertyPath = "couponCode",
    conditionValueState = ConditionValueState.NOT_EMPTY
)
public class OrderForm { ... }
```

#### @NullWhen / @NotNullWhen

```java
// When delivery is requested (needsDelivery is true), the delivery date (deliveryDate) must not be null
@NotNullWhen(
    propertyPath = "deliveryDate",
    conditionPropertyPath = "needsDelivery",
    conditionValueBoolean = true
)
public class OrderForm { ... }
```

#### @EmptyWhen / @NotEmptyWhen

```java
// When applying as an individual (corporate is false), companyName must be empty
@EmptyWhen(
    propertyPath = "companyName",
    conditionPropertyPath = "corporate",
    conditionValueBoolean = false
)
public class ApplicationForm { ... }
```

#### @PatternWhen / @NotPatternWhen

```java
// When the address is domestic (domestic is true), postalCode must be a 7-digit number
@PatternWhen(
    propertyPath = "postalCode",
    regexp = "\\d{7}",
    description = "7-digit number",
    conditionPropertyPath = "domestic",
    conditionValueBoolean = true
)
public class AddressForm { ... }
```

When `description` is specified, the error message shows it instead of the regular expression.
It is looked up in `ValidationMessagesPatternDescriptions.properties`, and the specified string itself is shown when the key does not exist.
When it is omitted, the regular expression itself is shown.

`regexp` is matched against a value that may come from end-user input. Avoid patterns prone to catastrophic
backtracking (e.g. nested quantifiers like `(a+)+`), which can make matching take exponential time on a crafted input.

#### @StringWhen / @NotStringWhen

```java
// When applying as a corporation (corporate is true), plan must be "BUSINESS" or "ENTERPRISE"
@StringWhen(
    propertyPath = "plan",
    string = {"BUSINESS", "ENTERPRISE"},
    conditionPropertyPath = "corporate",
    conditionValueBoolean = true
)
public class ApplicationForm { ... }
```

#### @ValueOfPropertyPathWhen / @NotValueOfPropertyPathWhen

```java
// When changing the password (changesPassword is true), confirmPassword must be the same as newPassword
@ValueOfPropertyPathWhen(
    propertyPath = "confirmPassword",
    valuePropertyPath = "newPassword",
    conditionPropertyPath = "changesPassword",
    conditionValueBoolean = true
)
public class AccountSettingsForm { ... }
```

---

## Specifying the Condition Value

Specifies the state of the `conditionPropertyPath` field that triggers validation,
using exactly one of the following attributes.

| Condition | Attribute to Set |
| --------- | ---------------- |
| Is `null` | `conditionValueState = ConditionValueState.NULL` |
| Is not `null` | `conditionValueState = ConditionValueState.NOT_NULL` |
| Is empty (null or blank string) | `conditionValueState = ConditionValueState.EMPTY` |
| Is not empty | `conditionValueState = ConditionValueState.NOT_EMPTY` |
| Is `true` | `conditionValueBoolean = true` |
| Is `false` | `conditionValueBoolean = false` |
| Is one of the specified strings | `conditionValueString` |
| Matches a regular expression | `conditionValuePatternRegexp` |
| Has the same value as another field | `conditionValuePropertyPath` |

Setting more than one of these attributes at the same time, or setting none of them, is an error.

### conditionValueState — Null or Empty

```java
// When note is empty, noteReason must be empty
@EmptyWhen(
    propertyPath = "noteReason",
    conditionPropertyPath = "note",
    conditionValueState = ConditionValueState.EMPTY
)
```

### conditionValueBoolean — True or False

The `conditionPropertyPath` field must be `boolean` / `Boolean`.
A `null` `Boolean` is treated as neither `true` nor `false` (with `conditionValueBoolean = true` the condition is not met,
and with `conditionOperator = NOT_EQUAL_TO` added it is met). To use `null` itself as the condition, use `conditionValueState = ConditionValueState.NULL`.

```java
// When sendsMail is true, mailAddress must not be empty
@NotEmptyWhen(
    propertyPath = "mailAddress",
    conditionPropertyPath = "sendsMail",
    conditionValueBoolean = true
)
```

### conditionValueString — Specified Strings

The condition is met when the value equals the specified string. When multiple strings are specified,
the condition is met when the value equals one of them.
The `conditionPropertyPath` field must be a string.

```java
// When role is "ADMIN", adminCode must not be empty
@NotEmptyWhen(
    propertyPath = "adminCode",
    conditionPropertyPath = "role",
    conditionValueString = "ADMIN"
)

// When role is "ADMIN" or "OPERATOR", adminCode must not be empty
@NotEmptyWhen(
    propertyPath = "adminCode",
    conditionPropertyPath = "role",
    conditionValueString = {"ADMIN", "OPERATOR"}
)
```

### conditionValuePatternRegexp — Regular Expression

The `conditionPropertyPath` field must be a string.

```java
// When phone is a mobile phone number, smsConsentAgreed must be true
@TrueWhen(
    propertyPath = "smsConsentAgreed",
    conditionPropertyPath = "phone",
    conditionValuePatternRegexp = "^(070|080|090).*",
    conditionValuePatternDescription = "mobile phone number"
)
// → When 'phone number' matches 'mobile phone number' ...
```

`conditionValuePatternDescription` specifies a user-facing description to display in error messages instead of the regular expression.
If omitted, the regular expression is displayed as-is (in the example above, "When 'phone number' matches '^(070|080|090).*' ...").
It plays the same role as `description` of `@PatternWithDescription`.

As with `regexp`, avoid patterns prone to catastrophic backtracking
(see [@PatternWhen / @NotPatternWhen](#patternwhen--notpatternwhen)).

### conditionValuePropertyPath — Value of Another Field

The condition is met when the value equals the value of another field. When the other field is an array,
the condition is met when the value equals one of its elements.

```java
// When the start date (startDate) is the same as the application date (applicationDate),
// that is, when the service starts on the day of application,
// agreement to the same-day start (agreedToSameDayStart) must be true
@TrueWhen(
    propertyPath = "agreedToSameDayStart",
    conditionPropertyPath = "startDate",
    conditionValuePropertyPath = "applicationDate"
)
```

---

## Other Features

### conditionOperator — Inverting the Condition

| `ConditionOperator` | Meaning |
| ------------------- | ---- |
| `EQUAL_TO` (default) | Validates when the condition is met |
| `NOT_EQUAL_TO` | Validates when the condition is not met |

```java
// When confirmed is not true, reason must not be empty
@NotEmptyWhen(
    propertyPath = "reason",
    conditionPropertyPath = "confirmed",
    conditionValueBoolean = true,
    conditionOperator = ConditionOperator.NOT_EQUAL_TO
)
```

### oppositeWhenConditionNotSatisfied — Validating the Opposite When the Condition Is Not Met

When `oppositeWhenConditionNotSatisfied` is set to `true`, the opposite is validated when the condition is not met.
Default is `false`, and nothing is validated when the condition is not met.

| Annotation | Validation When the Condition Is Not Met |
| ------------- | -------------------------- |
| `@TrueWhen` | Must be `false` |
| `@FalseWhen` | Must be `true` |
| `@NullWhen` | Must not be `null` |
| `@NotNullWhen` | Must be `null` |
| `@EmptyWhen` | Must not be empty |
| `@NotEmptyWhen` | Must be empty |
| `@PatternWhen` | Must not match the regular expression |
| `@NotPatternWhen` | Must match the regular expression |
| `@StringWhen` | Must not be any of the specified strings |
| `@NotStringWhen` | Must be one of the specified strings |
| `@ValueOfPropertyPathWhen` | Must have a different value from another field |
| `@NotValueOfPropertyPathWhen` | Must have the same value as another field |

```java
// When sendsMail is true, mailAddress must not be empty; otherwise it must be empty
@NotEmptyWhen(
    propertyPath = "mailAddress",
    conditionPropertyPath = "sendsMail",
    conditionValueBoolean = true,
    oppositeWhenConditionNotSatisfied = true
)
// → 'mail address' needs to be not empty when 'send mail' is ON, and empty when otherwise.
```

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
- When `oppositeWhenConditionNotSatisfied` is `true`, the opposite is validated
  unless all the conditions are met (that is, when at least one of them is not met).

### conditionValueDisplayStringPropertyPath — Display Name of the Condition Value

For `conditionValueString` / `conditionValuePropertyPath`, displays the value of another field in error messages
instead of the condition value. If omitted, the condition value (the specified string or the value of the other field) is displayed as-is.

The value to display is looked up as a key in the `messages` / `item_names` / `enum_names` / `constants` properties files,
and the value itself is displayed when the key does not exist.

```java
@NotEmptyWhen(
    propertyPath = "cancelReason",
    conditionPropertyPath = "status",
    conditionValueString = "9",
    conditionValueDisplayStringPropertyPath = "cancelledStatusName"  // the value is "Cancelled"
)
// Omitted   → ... when 'status' is '9'
// Specified → ... when 'status' is 'Cancelled'
```

When there are multiple condition values, make the display name field an array with the same number of elements
in the same order as the condition values.
To display a single display name summarizing all the condition values, you can also use a single value instead of an array.
