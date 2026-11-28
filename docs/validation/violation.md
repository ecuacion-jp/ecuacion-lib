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

## Adding Violations

### Adding a BusinessViolation

You can also pass the same arguments as the `BusinessViolation` constructors directly to `add(...)`.

```java
// Same as new BusinessViolation("error.range", "Age", "0", "150")
violations.add("error.range", "Age", "0", "150");

// Specify the itemPropertyPaths of the related items
violations.add(new String[] {"birthDate"}, "error.future-date");

// Specify itemNameKeys and itemPropertyPaths
violations.add(new String[] {"user.birthDate"}, new String[] {"birthDate"},
    "error.future-date");
```

For the meaning of each argument, see [BusinessViolation](business-violation.md).

A list of `BusinessViolation`s can be added at once with `addAll(List<BusinessViolation>)`.

### Validating with Jakarta Validation

`validate(object)` validates `object` with Jakarta Validation and adds the result as is.
It is the same as `addAll(validator.validate(object))`, but you don't need to prepare a `Validator`.

```java
violations.validate(form);

// Validate with groups
violations.validate(form, UpdateGroup.class);
```

Add an already obtained `Set<ConstraintViolation>` with `addAll(...)`, and a single `ConstraintViolation` with `add(...)`.

### Merging Another Violations

`addAll(Violations)` merges all the violations (both `ConstraintViolation`s and `BusinessViolation`s) added to another `Violations`.
Use it when each method creates its own `Violations` and the caller combines them.

```java
Violations violations = new Violations();
violations.addAll(checkHeader(header));
violations.addAll(checkDetails(details));
violations.throwIfAny();
```

`MessageParameters` is not merged (the settings of the merging side are used).

`add(...)`, `addAll(...)` and `validate(...)` all return the instance itself, so they can be chained.

```java
new Violations().validate(form).add("error.condition-a").throwIfAny();
```

---

## Checking and Retrieving Violations

| Method | Description |
| ------ | ------ |
| `isEmpty()` | `true` if no violation has been added |
| `getConstraintViolations()` | List of the added `ConstraintViolation`s (a copy) |
| `getBusinessViolations()` | List of the added `BusinessViolation`s (a copy) |

The returned lists are copies, so modifying them does not affect the `Violations`.

To obtain the violations as message strings, use `ExceptionUtil.getMessageList(violations)`
(see [ExceptionUtil](../util/exception-util.md)).

---

## throwIfAny() — Throw Together

`throwIfAny()` throws a `ViolationException` if there is at least one violation.
If there are no violations, it does nothing.

### Throwing Your Own Exception Class

`throwIfAny(Class)` throws the specified subclass instead of `ViolationException`.

```java
violations.throwIfAny(CsvUploadViolationException.class);
```

The specified class needs a public constructor that takes a single `Violations`, the same as `ViolationException`.
If there is no such constructor, a `RuntimeException` is thrown.

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
    ExceptionUtil.getMessageList(e.getViolations()).forEach(logger::warn);
} catch (ViolationException e) {
    // Error → stop processing
    throw e;
}
```
