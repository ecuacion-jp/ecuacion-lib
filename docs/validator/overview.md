# Custom Validators Overview

!!! note "Requires ecuacion-lib-validation"
    To use the features described in the pages under the "Custom Validators" menu, you need to add
    `ecuacion-lib-validation` as a dependency (`ecuacion-lib-core` alone is not enough).
    See [Home](../index.md) for the module structure.

## Overview

The `ecuacion-lib-validation` module provides custom validators that complement the standard Jakarta Validation
annotations (`@NotNull`, `@Size`, etc.).

```xml
<dependency>
  <groupId>jp.ecuacion.lib</groupId>
  <artifactId>ecuacion-lib-validation</artifactId>
</dependency>
```

---

## Classification by Application Level

| Level | Description | Representative Annotations |
| ------ | ---- | ---------------------- |
| Class level | Applied to the entire class, validates relationships between multiple fields | `@TrueWhen`, `@GreaterThan`, `@AnyNotNull`, `@ReturnTrue` |
| Field level | Applied to individual fields | `@IntegerString`, `@EnumElement`, `@PatternWithDescription` |
| Method level | Applied to methods, validates their return values | `@AssertTrueWithPropertyPath` |

Class-level annotations specify which field to associate the violation with using the `propertyPath` attribute.
Since these are plain strings resolved via reflection, a field rename is not caught by the compiler; see
[Guarding propertyPath Against Renames](property-path-safety.md) for
how to catch it anyway.

A name specified in `propertyPath`, `conditionPropertyPath`, `baselinePropertyPath` and so on (each node, for a nested path like `"dept.name"`)
is resolved to the field of that name first, and to a getter (`getName()`, or `isName()` returning primitive `boolean`; `isName()` returning `Boolean` is not treated as a getter) when no such field exists.
This definition of a getter follows the Jakarta Validation specification, which is based on the JavaBeans naming conventions.
Just as with standard Jakarta Validation constraints placed on a getter, the return value of the getter becomes the validated value, so a property without a field can also be specified.

```java
@LessThan(propertyPath = "startDate", baselinePropertyPath = "endDate")
public class Period {
    private LocalDate startDate;
    private LocalDate base;
    private int days;

    // There is no endDate field; the getter's return value is used as the value of baselinePropertyPath
    public LocalDate getEndDate() {
        return base.plusDays(days);
    }
}
```

---

## Classification by Feature

### When Variants (Conditional Validation)

Expresses conditional rules such as "when field X is Y, this field must be Z."
Class-level annotations that define conditions using `conditionPropertyPath` and `conditionValue`.

| Annotation | Validation Content |
| ------------- | ---------------- |
| `@TrueWhen` | Must be `true` when condition is met |
| `@NotNullWhen` | Must not be `null` when condition is met |
| `@NotEmptyWhen` | Must not be empty when condition is met |
| 9 more | ... |

### Comparison Validators

Validates the relative order of two fields.
Class-level annotations that compare 2 fields: `propertyPath` and `baselinePropertyPath`.

| Annotation | Validation Content |
| ------------- | ---------------- |
| `@GreaterThan` | `propertyPath` > `baselinePropertyPath` |
| `@LessThan` | `propertyPath` < `baselinePropertyPath` |
| 2 more | ... |

### Field Validators

Validates the format or type compatibility of field values. Used at the field level.

| Annotation | Validation Content |
| ------------- | ---------------- |
| `@IntegerString` | Must be an integer string |
| `@EnumElement` | Must be a valid value of the specified Enum |
| `@PatternWithDescription` | Regex match (with user-friendly message support) |
| 8 more | ... |

### Collection and Assertion Validators

Validates the null/empty state of multiple fields in bulk, or validates the return value of a method.

| Annotation | Validation Content |
| ------------- | ---------------- |
| `@AnyNotNull` | At least one of the specified fields must not be `null` |
| `@AllOrNoneNull` | All fields are `null`, or all fields are not `null` |
| `@AssertTrueWithPropertyPath` | Field-associated equivalent of `@AssertTrue` |
| `@ReturnTrue` | The specified method must return `true` |
| 4 more | ... |

---

## Integration with ValidationMessages

Default messages for each annotation are defined in `ValidationMessages.properties`.
To override them in the application, define a key with the annotation's fully qualified name + `.message`.

```properties
# ValidationMessages.properties
jp.ecuacion.lib.validation.constraints.TrueWhen.message = Agreement is required
```
