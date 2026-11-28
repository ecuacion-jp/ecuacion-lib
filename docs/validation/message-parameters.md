# MessageParameters

## Overview

`Violations.MessageParameters` is a parameter class that controls how error messages are generated.
It can be configured using `withMessageParameters()` in method chain format.

```java
violations.withMessageParameters(p -> p
    .showsItemName(true)
    .messagePrefix("upload.row.prefix"));
```

To use the same settings for multiple `Violations`, you can also create an instance with
`Violations.newMessageParameters()` and set it with `messageParameters(...)`. The current settings can be obtained with `messageParameters()`.

```java
Violations.MessageParameters params = Violations.newMessageParameters()
    .showsItemName(true)
    .messagePrefix("upload.row.prefix");

new Violations().messageParameters(params).validate(row1).throwIfAny();
```

`messageParameters(...)` keeps the instance as is without copying it.
So if you change it after setting (for example with `withMessageParameters()`), the change also affects other `Violations` set with the same instance.

---

## showsItemName — Switching Between Messages With/Without Item Names

Controls whether validation error messages are retrieved from `messages.properties` or
`messages_with_item_names.properties`.

| Value | Behavior |
| ------ | ---- |
| `null` (default) | Follows the value of `showsItemNameAsDefault` in `ExceptionUtil.getMessageList()` |
| `true` | Uses messages with item names |
| `false` | Uses messages without item names |

The former name `isMessageWithItemName` is deprecated as of 16.5.0. It behaves the same as `showsItemName`.

When `null`, the fallback is the `showsItemNameAsDefault` parameter of `ExceptionUtil.getMessageList()`.
It is not configured in a settings file; you pass it as an argument when calling `getMessageList()`.
Overloads without this argument treat it as `false`.

```java
// Violations whose showsItemName is null use messages with item names
List<String> messages = ExceptionUtil.getMessageList(violations, Locale.ENGLISH, true);
```

When `showsItemName` is explicitly set to `true` / `false`, it takes priority over `showsItemNameAsDefault`.

For details, see **util > ExceptionUtil**.

```java
violations.withMessageParameters(p -> p.showsItemName(true));
```

**Example with item names:**

```properties
# messages_with_item_names.properties
error.required = {item_name} is required
```

When `showsItemName(true)`, the item name of the field is embedded at `{item_name}`.
`Item` / `ItemUtil` are used to resolve the item name (see util > Item for details).

---

## showsItemNamePath — Displaying the Item Name Path

Controls whether to include position information (path) in the message for validation errors
on elements within a collection (list, etc.).

| Value | Display Example |
| ------ | ------ |
| `false` (default) | `name is required` |
| `true` | `name of the 1st element of list is required` |

```java
violations.withMessageParameters(p -> p.showsItemNamePath(true));
```

---

## messagePrefix / messagePostfix — Adding Text Before/After Messages

Appends fixed text before or after each error message.
Used for displays like "In the uploaded file, ○○ is required" in Excel upload validation.

### String Version — Specify as Literal or Key

The string passed to `messagePrefix(String)` is first resolved as a key in `messages.properties`.
If found, its value is used; if not, the passed string is used as-is.

```properties
# messages.properties
excel.upload.prefix = In the uploaded Excel file,
```

```java
// Key specification (retrieves value from messages.properties)
violations.withMessageParameters(p -> p.messagePrefix("excel.upload.prefix"));
// → "In the uploaded Excel file, 'Full Name' is required"

// Literal string (key not found, used as-is)
violations.withMessageParameters(p -> p.messagePrefix("In the imported data, "));
// → "In the imported data, 'Full Name' is required"
```

### Arg Version — Dynamic Values Including Placeholders

When the text contains placeholders (`{0}`, etc.) or embeds runtime values, use `Arg.message()`.

```properties
# messages.properties
upload.error.row.prefix = Row {0}:
```

```java
violations.withMessageParameters(p -> p
    .messagePrefix(Arg.message("upload.error.row.prefix", rowNumber)));
// → "Row 3: 'Full Name' is required"
```

`messagePostfix` works the same way.

---

## representativePropertyPath — Associating the Whole Batch with a Single Item

When violations are found deep inside a structure that isn't itself displayed on screen
(e.g., contents of an uploaded Excel file), their `itemPropertyPaths` don't correspond to
any single displayable UI item. `representativePropertyPath` lets you designate one
property path (e.g., the file upload item) that the entire batch of violations should be
associated with for display purposes, without changing the violations' own `itemPropertyPaths`.

```java
violations.withMessageParameters(p -> p.representativePropertyPath("fileToUpload"));
```

Consumers such as splib's screen-error binding can use this value to highlight the
representative item (e.g., mark the file upload field as invalid) even though the
violations themselves are not tied to that item's property path.

### Using It as a Message Placeholder

The value is also available as the `{representativePropertyPath}` named placeholder when
building the message itself, so a message like "See the file upload field for details" can
reference it directly.

```properties
# ValidationMessages.properties
jp.ecuacion.ClassValidatorSample.message = Related to {representativePropertyPath}: {0}
```

Availability differs depending on the violation kind:

| Violation kind | Availability |
| --- | --- |
| `ConstraintViolation` (`ValidationMessages*.properties`) | Always available when `representativePropertyPath` is set |
| `BusinessViolation` with `showsItemName(true)` (`messages_with_item_names.properties`) | Available, same as `{item_name}` |
| `BusinessViolation` with `showsItemName(false)` (`messages.properties`) | Not available — this file only receives positional (`{0}`, `{1}`, ...) arguments |

---

## Combined Example

An example of validating an Excel file with the row number as prefix and including item names in messages.

```java
for (int i = 0; i < rows.size(); i++) {
  int rowNum = i + 2; // Row number excluding the header row
  new Violations()
      .validate(rows.get(i))
      .withMessageParameters(p -> p
          .showsItemName(true)
          .showsItemNamePath(true)
          .messagePrefix(rowNum + ": "))
      .throwIfAny();
}
```
