# ecuacion-lib-validation-business-messages

## What is it?

An optional add-on module that provides business-friendly Japanese validation messages for
`ecuacion-lib-core` and `ecuacion-lib-validation` constraints.

Without this module, the library uses neutral messages aligned with Hibernate Validator's
standard wording (e.g. `must not be null`). Adding this module switches to more
user-facing messages (e.g. `This field is required.`).

## Covered Messages

| Scope | Examples |
| --- | --- |
| Jakarta standard constraints (`@NotNull`, `@Size`, ...) | `This field is required.`, `Please enter between {min} and {max} characters.`, ... |
| ecuacion-lib-validation field constraints (`@SizeString`, `@BooleanString`, ...) | `Please enter between {min} and {max} characters.`, ... |

Both plain (`ValidationMessages`) and item-name-qualified (`ValidationMessagesWithItemNames`)
variants are provided.

## System Requirements

- JDK 21 or above.

## Dependent Ecuacion Libraries

### Automatically Loaded Libraries

- `ecuacion-lib-core`

## Installation

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation-business-messages</artifactId>
    <!-- Put the latest release version -->
    <version>x.x.x</version>
</dependency>
```
