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

---

## Validators That Treat Empty Strings as Valid

In `ecuacion-lib-validation`, only `@NotEmpty` / `@NotBlank` treat empty strings as invalid.
All other format-checking validators treat empty strings as valid.

| Category | Representative Validators |
| -------- | ----------------- |
| Format-checking | `@PatternWithDescription`, `@IntegerString`, `@BooleanString`, etc. |
| Conditional | `@NotEmptyWhen`, `@TrueWhen`, and other `@XxxWhen` variants |
| Comparison | `@LessThan`, `@GreaterThan`, etc. |
