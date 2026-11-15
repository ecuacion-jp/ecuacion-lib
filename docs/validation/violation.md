# Violation

## Overview

In ecuacion-lib, the pattern of consolidating validation violations into the `Violations` class
and then throwing them together with `throwIfAny()` is adopted.

`Violations` accepts two types of violations.

| Type | Class | Purpose |
| ------ | -------- | ------ |
| Business rule violation | `BusinessViolation` | Manually created for business logic violations |
| Constraint violation | `ConstraintViolation` | Result of Jakarta Validation |

---

## Basic Usage

```java
Violations violations = new Violations();

// Add a business rule violation
violations.add(new BusinessViolation("error.some-message-id"));

// Validate with Jakarta Validation and add the result
violations.addAll(validator.validate(someObject));

// Throw a ViolationException if there is at least one violation
violations.throwIfAny();
```

By performing multiple checks first and **throwing them together**, all errors can be presented to the user in a single request.

```java
Violations violations = new Violations();

if (conditionA) {
  violations.add(new BusinessViolation("error.condition-a"));
}
if (conditionB) {
  violations.add(new BusinessViolation("error.condition-b"));
}

// Validate the form with Jakarta Validation and add the result
violations.addAll(validator.validate(form));

violations.throwIfAny();
```

---

## throwIfAny() — Throw Together

`throwIfAny()` throws a `ViolationException` if there is at least one violation.
If there are no violations, it does nothing.

---

## ViolationException

`ViolationException` is an unchecked exception that extends `RuntimeException`.
You can retrieve the `Violations` with `getViolations()`.

In typical application development, it is rarely caught directly;
the framework layer (such as ecuacion-splib) handles it collectively.

---

## throwWarningIfAny() — Handling as a Warning

Using `throwWarningIfAny()` instead of `throwIfAny()` throws a `ViolationWarningException`
instead of a `ViolationException`.

```java
violations.throwWarningIfAny();
```

Since `ViolationWarningException` is a subclass of `ViolationException`,
it can also be caught together with `catch (ViolationException e)`.

### Primary Use Cases

**Combined with ecuacion-splib-web** (primary use case):
Used in a mechanism for displaying a confirmation dialog on a UI screen such as "Are you sure you want to do this?".
When the user confirms, the flow continues by ignoring that warning.
For details, refer to the ecuacion-splib-web documentation.

**Usage with ecuacion-lib alone**:
Can be used in batch processing or API handlers when you want to distinguish by type between
"errors (stop processing)" and "warnings (can continue)".

```java
// Calling side
try {
    someService.process(data);
} catch (ViolationWarningException e) {
    // Warning → log and continue
    logger.warn(e.getViolations().toString());
} catch (ViolationException e) {
    // Error → stop processing
    throw e;
}
```
