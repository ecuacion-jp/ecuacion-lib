# Handling of Empty Strings in ecuacion-lib Validators

## Overview

The validators in `ecuacion-lib-validation` treat empty strings (`""`) as **valid** (i.e., no validation error occurs).
This is a different design from the Jakarta Validation standard.

---

## Difference from Standard @Pattern

The standard `@Pattern` treats `null` as valid but `""` as invalid.
On the other hand, `@PatternWithDescription`, a custom validator provided by ecuacion-lib, treats `""` as valid in addition to `null`.

| Value | Standard `@Pattern` | ecuacion-lib `@PatternWithDescription` |
| ---- | --------------- | ------------------------- |
| `"abc123"`<br>(regex match) | valid | valid |
| `"ABC"`<br>(regex mismatch) | invalid | invalid |
| `null` | valid | valid |
| `""` | **invalid** | **valid** |

---

## Reasons for Treating Empty Strings as Valid

In web applications, `null` and `""` have different meanings.

- `""` = The field exists on the screen, and the user submitted it without entering anything
- `null` = The field does not exist on the screen (not included in the submitted data)

When the user submits without entering anything and `@Pattern` returns invalid,
the error "invalid format" is displayed.
However, if the field is required, the proper error should be "required" (from `@NotEmpty`),
and if the field is not required, the correct behavior is that no error occurs.

You could convert all submitted empty strings to `null` at once,
but then you could no longer distinguish between a field left empty on the screen and a field that does not exist on the screen at all.

Therefore, format-checking validators should not react to empty input (`""`).

This policy assumes that the input comes from a screen.
For input that does not go through a screen, such as a REST API, `""` is a value the sender sent intentionally, so you may want to treat it as invalid.

In that case, you can make empty strings invalid by combining the validator with `@NotEmpty` or a similar constraint.
If you want only `""` to be invalid while keeping `null` valid, combine it with `@Size(min = 1)`.

```java
@Size(min = 1)
@PatternWithDescription(regexp = "[0-9]+")
private String code;  // "" → violation of @Size, null → valid
```

Conversely, a validator that treats empty strings as invalid cannot be used where you want empty strings to be valid.
Treating empty strings as valid lets you handle both situations through combinations.

---

## Validators That Treat Empty Strings as Valid

The custom validators of `ecuacion-lib-validation` treat empty strings as valid
(`@PatternWithDescription`, `@IntegerString`, `@LessThan`, etc.).

However, among the conditional validators (`@XxxWhen`), the ones that validate the value itself
handle empty values (`null` and `""`) differently from one validator to another
when the condition is satisfied.

| Validator | Empty values (`null` / `""`) |
| --- | --- |
| `@PatternWhen` / `@StringWhen` | invalid |
| `@NotPatternWhen` / `@NotStringWhen` | valid |
| `@ValueOfPropertyPathWhen` | valid when equal to the compared value (`null` and `""` are compared as different values) |
| `@NotValueOfPropertyPathWhen` | valid when different from the compared value |

`@NotEmptyWhen` / `@EmptyWhen` and the like are not included here, since their purpose is to check whether the value is empty.
