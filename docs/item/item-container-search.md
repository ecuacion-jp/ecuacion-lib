# ItemContainer Search

## Overview

When generating a Jakarta Validation error message, the `Item` corresponding to the field that caused the error
is resolved by searching for an `ItemContainer` based on the rootBean and the propertyPath of the violation.
This page describes how that search works.

---

## ItemContainer Search Scope

`ItemUtil.resolveItem()` is a method that finds the ItemContainer based on the rootBean and propertyPath
and resolves the `Item`. It is called internally by the framework when generating validation error messages
(for details, see [ItemUtil](item-util.md)).

The search scope is **up to 1 level from the rootBean**.

---

## Note: Why Sibling ItemContainers Don't Cause Ambiguity

For example, if `UserForm` has two direct children, `UserDto` and `DeptDto`, both `ItemContainer`s and both with a `name` field,
you might wonder whether writing `itemPropertyPath` as simply `"name"` would leave it unclear which `ItemContainer` is meant.
In practice, this ambiguity does not arise, for two reasons corresponding to the two ways `itemPropertyPath` is used:

1. **When displaying validation messages** (starting from the field where the violation occurred): Jakarta Validation itself has already
   identified the field where the violation occurred by walking from the rootBean. The search for an `ItemContainer` starts from that
   already-identified propertyPath from the rootBean (fullPropertyPath, e.g., `"userDto.name"`), so it is never reduced to an ambiguous bare `"name"`.
2. **When displaying item names via Thymeleaf in splib** (walking from the rootBean toward the target item in order): even though the
   `itemPropertyPath` written on each component may be in shorthand form (e.g., `"name"`), the part that was shortened (i.e., which
   `ItemContainer`'s scope it belongs to) is specified separately elsewhere, so the component as a whole can still uniquely identify its target.
