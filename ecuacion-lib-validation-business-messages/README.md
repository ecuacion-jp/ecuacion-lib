# ecuacion-lib-validation-business-messages

## What is it?

An optional add-on module that provides business-friendly English and Japanese validation
messages for `ecuacion-lib-core` and `ecuacion-lib-validation` constraints.

Without this module, the library uses neutral messages aligned with Hibernate Validator's
standard wording (e.g. `must not be null`). Adding this module switches to more
user-facing messages (e.g. `is required`).

## Message Examples

Adding this module changes validation messages from neutral technical wording to user-facing business language:

| Constraint | Without this module (default) | With this module |
| --- | --- | --- |
| `@NotNull` | `must not be null` | `is required` |
| `@NotEmpty` | `must not be empty` | `is required` |
| `@Size(min=2, max=10)` | `size must be between 2 and 10` | `must be between 2 and 10 characters` |
| `@Max(100)` | `must be less than or equal to 100` | `must be 100 or less` |
| `@Email` | `must be a well-formed email address` | `must be a valid email address` |

`ValidationMessagesWithItemNames` variants include the field name for even clearer messages, e.g. `'Email Address' is required`.

## Covered Constraints

| Scope | Examples |
| --- | --- |
| Jakarta standard constraints (`@NotNull`, `@Size`, ...) | `is required`, `must be between {min} and {max} characters`, ... |
| ecuacion-lib-validation field constraints (`@SizeString`, `@BooleanString`, ...) | `must be between {min} and {max} characters`, `must be an integer`, ... |
| ecuacion-lib-validation class constraints (`@NotEmptyWhen`, `@AnyNotEmpty`, ...) | `must be provided when 'Type' is on, and blank otherwise`, `at least one of {0} must be provided`, ... |

## Dependent Ecuacion Libraries

### Automatically Loaded Libraries

- `ecuacion-lib-validation` (which transitively loads `ecuacion-lib-core`)

## Documentation

- Official reference documentation - [ecuacion-references-lib](https://references.ecuacion.jp/ecuacion-references-lib/public/showMarkdown/page?id=home)

## Installation

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation-business-messages</artifactId>
    <!-- Put the latest release version -->
    <version>x.x.x</version>
</dependency>
```
