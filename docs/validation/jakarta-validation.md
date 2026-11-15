# ConstraintViolation

## Overview

`ConstraintViolation` is a class that represents the result of Jakarta Validation (formerly Bean Validation).
When an object annotated with Jakarta Validation annotations (`@NotNull`, `@Size`, etc.) is validated,
a `ConstraintViolation` is generated for each violation.

In ecuacion-lib, this result is added to `Violations` using `violations.addAll()`.

---

## violations.addAll() — Adding ConstraintViolations

Jakarta Validation returns a `Set<ConstraintViolation<T>>` when validation is performed.
Add this to `Violations` using `violations.addAll()`.

```java
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
violations.addAll(validator.validate(someForm));
```

When specifying validation groups:

```java
violations.addAll(validator.validate(someForm, GroupA.class, GroupB.class));
```

---

## Violations.validate() — Shorthand

This is a shorthand that combines the validation and addition described above.

```java
// Standard way
violations.addAll(validator.validate(someObject));

// Equivalent shorthand
violations.validate(someObject);
violations.validate(someObject, GroupA.class);  // With group specification
```
