# Standard Annotation Message Output

## Overview

Validation errors from Jakarta Validation standard annotations such as `@NotNull` can be output in
various formats by combining `Violations.validate()` with `MessageParameters`.

The following examples all use this form class.
All output examples assume that `ecuacion-lib-validation-business-messages` has been added as a
dependency. Without it, the messages are ecuacion-lib-core's (Hibernate Validator's) default wording
(such as `must not be null`).

```java
class SomeForm {
    @NotNull
    private String name;
}
```

---

## Basic

```java
new Violations().validate(form).throwIfAny();
// → "is required"
```

---

## Including Item Names in Messages

Specifying `showsItemName(true)` uses the message from `ValidationMessagesWithItemNames.properties`,
with the item name embedded at `{0}`.

```properties
# item_names.properties
someForm.name = Full Name
```

```java
new Violations().validate(form)
    .withMessageParameters(p -> p.showsItemName(true))
    .throwIfAny();
// → "'Full Name' is required"
```

For details on how item names are resolved, see [Using Item Names in Messages](../messaging/item-name-in-message.md).

### ValidationMessagesWithItemNames.properties

A properties file for messages that include item names.
The item name (e.g., `'Full Name'`) is substituted at the `{0}` position.

```properties
# ValidationMessagesWithItemNames.properties (ecuacion-lib-core default)
jakarta.validation.constraints.NotNull.message.base = {0} must not be null.

# ValidationMessagesWithItemNames.properties (ecuacion-lib-validation-business-messages)
jakarta.validation.constraints.NotNull.message.default = {0} is required
```

ecuacion-lib-core provides defaults for standard annotations, and adding
`ecuacion-lib-validation-business-messages` replaces them with business-oriented wording.
In either case, you do not need to create this file in your application.

This mechanism is not limited to standard annotations; it works the same way for the custom validators of
ecuacion-lib-validation (their defaults are provided by ecuacion-lib-validation).

For details as a file kind, see [PropertiesFileUtil > ValidationMessages](../properties-file-util/validation-messages.md).

### When Messages with Item Names Are Used

Whether `ValidationMessagesWithItemNames.properties` is used is determined by the following priority:

1. If `MessageParameters.showsItemName` is explicitly set, that value is used
2. If not set, the `showsItemNameAsDefault` flag of `ExceptionUtil.getMessageList()`

Frameworks such as ecuacion-splib-web set this flag to `true` for messages displayed on screen.

### Overriding Messages in Your Application

To use different messages per annotation, define keys without a suffix in your application's
`ValidationMessagesWithItemNames.properties`.

```properties
# ValidationMessagesWithItemNames.properties (application side)
jakarta.validation.constraints.NotNull.message = Please enter {0}
```

Due to `PropertiesFileUtil`'s key priority (`key` > `key.default` > `key.base`),
the plain key takes priority over ecuacion-lib-core's `.base` and
ecuacion-lib-validation-business-messages' `.default`.

---

## Including the Path to the Violated Field

Specifying `showsItemNamePath(true)` appends the path to the violated field to the message.
The following two types of information are included:

- **Violations on collection elements**: Indicates which element number
- **Nested objects**: Includes the item name of the parent object traversed

Using the outer class that holds `SomeForm` in a List as an example:

```java
class OuterForm {
    @Valid
    private List<SomeForm> forms;  // item_names.properties: outerForm.forms=Form List
}
```

```java
// showsItemNamePath=false (default)
new Violations().validate(outerForm)
    .withMessageParameters(p -> p.showsItemName(true))
    .throwIfAny();
// → 'Full Name' is required

// showsItemNamePath=true
new Violations().validate(outerForm)
    .withMessageParameters(p -> p
        .showsItemName(true)
        .showsItemNamePath(true))
    .throwIfAny();
// → 'Full Name' at element 2 contained by 'Form List' is required
//    ↑ field item name  ↑ parent object (collection)
```

---

## Adding a Prefix to Messages

Used when you want messages to show where the validated object came from, such as an uploaded file.

```java
new Violations().validate(form)
    .withMessageParameters(p -> p
        .showsItemName(true)
        .messagePrefix("In the uploaded Excel file, "))
    .throwIfAny();
// → "In the uploaded Excel file, 'Full Name' is required"
```

Which element the violation occurred in can be shown with `showsItemNamePath`.

---

## Combined Example

An example of validating the contents of an uploaded Excel file as `OuterForm`.

```java
new Violations().validate(outerForm)
    .withMessageParameters(p -> p
        .showsItemName(true)
        .showsItemNamePath(true)
        .messagePrefix("In the uploaded Excel file, "))
    .throwIfAny();
// → "In the uploaded Excel file, 'Full Name' at element 2 contained by 'Form List' is required"
```

For details on each option of `MessageParameters`, see **violation > MessageParameters**.
