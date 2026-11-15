# Jakarta Validation Terms

These are the objects and terms defined in the Jakarta Validation specification.
These terms appear in the documentation and APIs for validation-related classes in ecuacion-lib.

---

## ConstraintViolation

`jakarta.validation.ConstraintViolation` is the standard Jakarta Validation interface that represents a single validation violation.
When validation is executed, one `ConstraintViolation` is generated for each violation.

---

## rootBean

The top-level object from which validation was invoked.

```java
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
validator.validate(form);  // form is the rootBean
```

The object passed to `Validator.validate()` becomes the `rootBean`.
Even when validating fields of nested objects (using `@Valid`), the `rootBean` remains the same.

---

## leafBean

The object that directly holds the field being validated.

When the field belongs to the `rootBean` itself, `leafBean == rootBean`.
When validating a field of a nested object, that nested object becomes the `leafBean`.

Example: When validating `form.dept.name`

| | Object |
| --- | --- |
| `rootBean` | `form` |
| `leafBean` | `form.dept` (the `Dept` object that holds the `name` field) |

---

## propertyPath

A string that represents the path from the rootBean to the field being validated.
It is expressed in dot notation, with collection elements having indices.

| propertyPath | Meaning |
| --- | --- |
| `name` | The `name` field directly under the rootBean |
| `dept.name` | The `name` field of the `dept` object held by the rootBean |
| `bookList[1].title` | The `title` field of the second element of `bookList` held by the rootBean |
