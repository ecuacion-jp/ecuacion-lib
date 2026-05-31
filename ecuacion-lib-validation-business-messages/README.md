# ecuacion-lib-validation-business-messages

## What is it?

An optional add-on module that provides business-friendly Japanese validation messages for
`ecuacion-lib-core` and `ecuacion-lib-validation` constraints.

Without this module, the library uses neutral messages aligned with Hibernate Validator's
standard wording (e.g. `must not be null`). Adding this module switches to more
user-facing messages (e.g. `This field is required.`).

## Message Examples

Adding this module changes validation messages from neutral technical wording to user-facing business language:

| Constraint | Without this module (default) | With this module |
| --- | --- | --- |
| `@NotNull` | `must not be null` | `This field is required.` |
| `@NotEmpty` | `must not be empty` | `This field is required.` |
| `@Size(min=2, max=10)` | `size must be between 2 and 10` | `Please enter between 2 and 10 characters.` |
| `@Max(100)` | `must be less than or equal to 100` | `Please enter a value of 100 or less.` |
| `@Email` | `must be a well-formed email address` | `Please enter a valid email address.` |

`ValidationMessagesWithItemNames` variants include the field name for even clearer messages, e.g. `"Email Address" is required.`

## Covered Constraints

| Scope | Examples |
| --- | --- |
| Jakarta standard constraints (`@NotNull`, `@Size`, ...) | `This field is required.`, `Please enter between {min} and {max} characters.`, ... |
| ecuacion-lib-validation field constraints (`@SizeString`, `@BooleanString`, ...) | `Please enter between {min} and {max} characters.`, ... |

## System Requirements

- JDK 21 or above.

## Dependent Ecuacion Libraries

### Automatically Loaded Libraries

- `ecuacion-lib-validation` (which transitively loads `ecuacion-lib-core`)

## Installation

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation-business-messages</artifactId>
    <!-- Put the latest release version -->
    <version>x.x.x</version>
</dependency>
```
