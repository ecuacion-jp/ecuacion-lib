# Using Item Names in Messages

## Overview

ecuacion-lib's validation error messages can be displayed including an **item name**
that indicates "which field caused the error."

Without item name (Hibernate Validator default):

```text
must not be null
```

With item name:

```text
'name' must not be null
```

---

## Overall Flow

```text
Field
  ↓  identified by itemPropertyPath
Item
  ↓  resolves itemNameKey
Key in item_names.properties
  ↓  retrieves value
Display name (e.g., Full Name)
  ↓  substituted into ValidationMessagesWithItemNames.properties
Error message (e.g., 'Full Name' must not be null)
```

---

## item_names.properties

A properties file that manages display names for item names.

```properties
# item_names.properties
userRecord.name=Full Name
userRecord.email=Email Address
userRecord.birthDate=Date of Birth
```

The key is the `itemNameKey` and the value is the name displayed on screen.

### When to Use vs. messages.properties

It is also possible to define item names in `messages.properties` instead of `item_names.properties`.
However, it is recommended to manage them separately in `item_names.properties` to avoid mixing
regular message text with item name definitions.

### Localization

You can define locale-specific names using files with suffixes.

```properties
# item_names.properties (default)
userRecord.name=Full Name

# item_names_ja.properties (Japanese)
userRecord.name=氏名
```

Since it is managed as the `item_names` file type in `PropertiesFileUtil`,
locale resolution follows the same rules as Java's standard `ResourceBundle`.

---

## ValidationMessagesWithItemNames.properties

A properties file for messages that include item names.
The item name (e.g., `'Full Name'`) is substituted at the `{0}` position.

```properties
# ValidationMessagesWithItemNames.properties (ecuacion-lib-core default)
jakarta.validation.constraints.NotNull.message.base = {0} must not be null
```

Since ecuacion-lib-core provides defaults for standard annotations,
you do not need to create this file in your application.

Whether this file is used is determined by the following priority:

1. If `MessageParameters.isMessageWithItemName` is explicitly set, that value is used
2. If not set, the `isMessagesWithItemNamesAsDefault` flag of `ExceptionUtil.getMessageList()`

Frameworks such as ecuacion-splib-web set this flag to `true` for messages displayed on screen.

---

## Setup Steps

### 1. Implement ItemContainer

Implement `ItemContainer` on the class to be validated.
For details, see [ItemContainer](../item/item-container.md).

```java
public class UserRecord implements ItemContainer {

    @NotNull
    private String name;

    @Override
    public Item[] customizedItems() {
        return new Item[] {};  // Empty array when no customization is needed
    }
}
```

### 2. Register Item Names in item_names.properties

```properties
# src/main/resources/item_names.properties
userRecord.name=Full Name
userRecord.email=Email Address
```

---

## Using with BusinessViolation

When you want to use messages with item names in business logic checks, pass `itemNameKeys` to `BusinessViolation`.
The values of `itemNameKeys` are used as keys in `item_names.properties`.

```java
violations.add(new BusinessViolation(
    new String[] {"customer.email"},  // itemNameKeys
    new String[] {"email"},            // itemPropertyPaths (for UI highlighting)
    "error.already-registered"));
```

For details, see [Violation](../validation/violation.md).

---

## Overriding Messages in Your Application

To use different messages per annotation, define keys without a suffix in your application's
`ValidationMessagesWithItemNames.properties`.

```properties
# ValidationMessagesWithItemNames.properties (application side)
jakarta.validation.constraints.NotNull.message = {0} is required
```

Due to `PropertiesFileUtil`'s key priority (`key` > `key.default` > `key.base`),
the plain key takes priority over ecuacion-lib-core's `.base` default.

---

## Detailed Reference

- Automatic resolution rules for itemNameKey → **[ItemNameKey](../item/item-name-key.md)**
- Details on ItemContainer → **[ItemContainer](../item/item-container.md)**
- For collection elements → **[Item Names for List, Set, and Map](collection-item-name.md)**
