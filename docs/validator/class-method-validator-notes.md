# Notes on Class and Method Validators

This page contains notes common to ecuacion-lib's class validators (`@NotEmptyWhen`, `@LessThan`, `@ReturnTrue`, etc.)
and `@AssertTrueWithPropertyPath` placed on a method (method validator).
All of them specify the fields to which the error is associated with the `propertyPath` attribute.

---

## Value of ConstraintViolation#getPropertyPath()

`ConstraintViolation#getPropertyPath()` of class and method validators does not point to the fields specified in the `propertyPath` attribute.

| Validator | `getPropertyPath()` |
| --- | --- |
| Class validators | The path of the annotated class (`""` when placed on the root class) |
| Method validators | The property name of the annotated method (`dateRangeValid` when placed on `isDateRangeValid()`) |

When converting a `ConstraintViolation` to an exception with ecuacion-lib's [ExceptionUtil](../util/exception-util.md),
the fields are identified from the `propertyPath` attribute, not from `getPropertyPath()`, so you do not need to care about this value.
It matters only when you use `ConstraintViolation#getPropertyPath()` directly.

---

## CreateMultipleConstraintViolationsConstraintValidatorFactory

Class and method validators generate only one `ConstraintViolation` by default,
regardless of the number of fields specified in `propertyPath`.

By specifying `CreateMultipleConstraintViolationsConstraintValidatorFactory` when creating a `Validator`,
it generates a `ConstraintViolation` for each entry in `propertyPath`,
with each having the field name set in its `getPropertyPath()`.

```java
// Default
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

// Using CreateMultipleConstraintViolationsConstraintValidatorFactory
Validator validator = Validation.byDefaultProvider().configure()
    .constraintValidatorFactory(
        new CreateMultipleConstraintViolationsConstraintValidatorFactory())
    .buildValidatorFactory().getValidator();
```

`getPropertyPath()` when `propertyPath = {"startDate", "endDate"}` is specified:

| Validator | Default | Using `CreateMultiple...` |
| --- | --- | --- |
| Class validators (placed on the root class) | `""` (1 violation) | `startDate`, `endDate` (2 violations) |
| Method validators (placed on `isDateRangeValid()`) | `dateRangeValid` (1 violation) | `dateRangeValid.startDate`, `dateRangeValid.endDate` (2 violations) |

With method validators, the property name of the annotated method is added at the beginning.
When combined with a mechanism that maps `getPropertyPath()` directly to fields,
class validators (`@ReturnTrue`, etc.) are easier to handle.

Since ecuacion-lib internally uses the approach of generating 1 `ConstraintViolation`, this factory is optional.
Note that generating multiple violations makes it harder to distinguish them from errors from other validators.
