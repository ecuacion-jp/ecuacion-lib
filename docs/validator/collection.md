# Collection and Assertion Validators

## Collection Validators — Bulk Null/Empty Validation for Multiple Fields

Validates the null/empty state of multiple fields specified in `propertyPath[]` at the class level.

### Any Variants

| Annotation | Validation Content |
| ------------- | -------- |
| `@AnyNull` | At least one field is `null` |
| `@AnyNotNull` | At least one field is not `null` |
| `@AnyEmpty` | At least one field is empty (null or blank string) |
| `@AnyNotEmpty` | At least one field is not empty |

### All Variants

| Annotation | Validation Content |
| ------------- | -------- |
| `@AllOrNoneNull` | All fields are `null`, or all fields are not `null` |
| `@AllOrNoneEmpty` | All fields are empty, or all fields are not empty |

### Empty Definition by Type

For `Empty` variants (`@AnyEmpty`, `@AnyNotEmpty`, `@AllOrNoneEmpty`), the definition of "empty" differs by type.

| Type | Condition Considered "Empty" |
| ---- | ----------------- |
| `String` | `null` or blank string (`""`) |
| Other types | `null` only |

`Null` variants such as `@AnyNotNull` target only `null` regardless of type.

### Usage Examples

```java
// startDate and endDate must both be entered or both be empty
@AllOrNoneNull(propertyPath = {"startDate", "endDate"})
public class SearchForm { ... }
```

```java
// At least one of email / phone / address is required
@AnyNotEmpty(propertyPath = {"email", "phone", "address"})
public class ContactForm { ... }
```

---

## AssertTrueWithPropertyPath — Field-Associated AssertTrue

Similar to the standard `@AssertTrue` in validating that a value is `true`, but allows associating
the error with a specific field using the `propertyPath` attribute. This is a method-level annotation.

```java
public class EventForm {
    @AssertTrueWithPropertyPath(
        propertyPath = {"startDate", "endDate"},
        message = "Start date must be before end date"
    )
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) return true;
        return startDate.isBefore(endDate);
    }
}
```

### Getting propertyPath

The value of `propertyPath` can be retrieved from the annotation attributes of `ConstraintViolation`.

```java
Set<ConstraintViolation<EventForm>> set = validator.validate(form);
for (ConstraintViolation<?> cv : set) {
    String[] paths = (String[]) cv.getConstraintDescriptor()
        .getAttributes().get("propertyPath");
    // → ["startDate", "endDate"]
}
```

---

## ReturnTrue — Class-Level Method Validator

Applied to a class, validates that the method specified by `methodName` returns `true`. This is a class-level annotation.

```java
@ReturnTrue(
    methodName = "isDateRangeValid",
    propertyPath = {"startDate", "endDate"},
    message = "Start date must be before end date"
)
public class EventForm {
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) return true;
        return startDate.isBefore(endDate);
    }
}
```

| Attribute | Description |
| ---- | ---- |
| `methodName` | Method name to execute (no-arg, `boolean` return type) |
| `propertyPath` | Fields to associate the error with |
| `message` | Error message (message key or literal string) |

### Comparison with AssertTrueWithPropertyPath

| | `@AssertTrueWithPropertyPath` | `@ReturnTrue` |
| ---- | ---- | ---- |
| Applied at | Method | Class |
| Default `getPropertyPath()` | `isDateRangeValid.startDate` (method name is a prefix) | `startDate` (no prefix) |

Since `@AssertTrueWithPropertyPath` includes the method name as a prefix in `getPropertyPath()`,
`@ReturnTrue` is better suited for use in frameworks that need to identify fields.

---

## CreateMultipleConstraintViolationsConstraintValidatorFactory

With the default behavior of class-level validators, `ConstraintViolation.getPropertyPath()` returns an empty string,
so it is not possible to identify which field's error it is from `getPropertyPath()`.

By specifying `CreateMultipleConstraintViolationsConstraintValidatorFactory` when creating a `Validator`,
it generates a `ConstraintViolation` for each entry in `propertyPath`,
with each having the field name set in its `getPropertyPath()`.

```java
// Default (propertyPath is empty string)
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

// Using CreateMultipleConstraintViolationsConstraintValidatorFactory
Validator validator = Validation.byDefaultProvider().configure()
    .constraintValidatorFactory(
        new CreateMultipleConstraintViolationsConstraintValidatorFactory())
    .buildValidatorFactory().getValidator();
```

The difference when `startDate` and `endDate` are specified in `propertyPath`:

```text
// Default
propertyPath : ""  (empty string)

// Using CreateMultiple...
propertyPath : startDate
propertyPath : endDate  (2 violations generated)
```

Since ecuacion-lib internally uses the approach of generating 1 `ConstraintViolation`, this factory is optional.
Note that generating multiple violations makes it harder to distinguish them from errors from other validators.
