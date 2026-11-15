# Standard Annotation Message Output

## Overview

Validation errors from Jakarta Validation standard annotations such as `@NotNull` can be output in
various formats by combining `Violations.validate()` with `MessageParameters`.

The following examples all use this form class:

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

Specifying `isMessageWithItemName(true)` uses the message from `ValidationMessagesWithItemNames.properties`,
with the item name embedded at `{0}`.

```properties
# ValidationMessagesWithItemNames.properties
jakarta.validation.constraints.NotNull.message = {0} must not be null
```

```properties
# item_names.properties
someForm.name = Full Name
```

```java
new Violations().validate(form)
    .withMessageParameters(p -> p.isMessageWithItemName(true))
    .throwIfAny();
// → "Full Name is required"
```

For details on how item names are resolved, see [Using Item Names in Messages](../messaging/item-name-in-message.md).

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
    .withMessageParameters(p -> p.isMessageWithItemName(true))
    .throwIfAny();
// → 'name' must not be null

// showsItemNamePath=true
new Violations().validate(outerForm)
    .withMessageParameters(p -> p
        .isMessageWithItemName(true)
        .showsItemNamePath(true))
    .throwIfAny();
// → 'name' of the 2nd element of 'Form List' must not be null
//    ↑ parent object (collection)        ↑ field item name
```

---

## Adding a Prefix to Messages

Used when you want to add a row number to error messages, such as in Excel file validation.

```java
new Violations().validate(form)
    .withMessageParameters(p -> p
        .isMessageWithItemName(true)
        .messagePrefix("Row 3: "))
    .throwIfAny();
// → "Row 3: Full Name is required"
```

---

## Combined Example

An example of validating a list of `OuterForm` row by row.

```java
List<OuterForm> rows = ...;
for (int i = 0; i < rows.size(); i++) {
    int rowNum = i + 2; // Row number excluding the header row
    new Violations()
        .validate(rows.get(i))
        .withMessageParameters(p -> p
            .isMessageWithItemName(true)
            .showsItemNamePath(true)
            .messagePrefix(rowNum + ": "))
        .throwIfAny();
}
// → "Row 3: 'name' of the 2nd element of 'Form List' must not be null"
```

For details on each option of `MessageParameters`, see **violation > MessageParameters**.
