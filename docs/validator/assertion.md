# Assertion Validators

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
