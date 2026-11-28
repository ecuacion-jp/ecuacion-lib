# Collection Validators

Validates the null/empty state of multiple fields specified in `propertyPath[]` at the class level.

## Any Variants

| Annotation | Validation Content |
| ------------- | -------- |
| `@AnyNull` | At least one field is `null` |
| `@AnyNotNull` | At least one field is not `null` |
| `@AnyEmpty` | At least one field is empty (null or blank string) |
| `@AnyNotEmpty` | At least one field is not empty |

## All Variants

| Annotation | Validation Content |
| ------------- | -------- |
| `@AllOrNoneNull` | All fields are `null`, or all fields are not `null` |
| `@AllOrNoneEmpty` | All fields are empty, or all fields are not empty |

## Empty Definition by Type

For `Empty` variants (`@AnyEmpty`, `@AnyNotEmpty`, `@AllOrNoneEmpty`), the definition of "empty" differs by type.

| Type | Condition Considered "Empty" |
| ---- | ----------------- |
| `String` | `null` or blank string (`""`) |
| Other types | `null` only |

`Null` variants such as `@AnyNotNull` target only `null` regardless of type.

## Usage Examples

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
