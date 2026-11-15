# Overview of Violations and Exceptions

In ecuacion-lib, validation violations are consolidated and managed in the `Violations` class regardless of their type.

## Two Types of Violations

There are two types of violations that can be added to `Violations`.

**BusinessViolation** — Manually added violations for business logic rule failures.

**ConstraintViolation** — Violations resulting from validation by Jakarta Validation.

## Management with Violations

Both types of violations are consolidated into `Violations`, and thrown together with `throwIfAny()`.
Since multiple checks can be grouped together, all errors can be returned to the user in a single request.

For details, see the articles in the **violation** menu.
