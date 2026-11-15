# Item

## Overview

For an overview, see also [Common Topics > item, ItemContainer and itemPropertyPath](item-property-path.md).

`Item` (`jp.ecuacion.lib.core.item.Item`) is a class that holds the attributes of a single field.
It is primarily used to control how item names and field values are displayed in validation error messages.

You do not need to set an `Item` for every field subject to validation; if none is specified, the default
settings are applied. Only when you want to customize how a field is displayed in error messages do you
implement `customizedItems()` in [ItemContainer](item-container.md) to create `Item` instances.

```java
@Override
public Item[] customizedItems() {
    return new Item[] {
        new Item("password").hideValue(),
        new Item("name").itemNameKey("fullName")
    };
}
```

---

## Available Methods

### hideValue()

Use `hideValue()` for fields such as passwords where you do not want to include the value in error messages.

```java
new Item("password").hideValue()
```

For example, given the following message definition:

```properties
jakarta.validation.constraints.Size.message.base = {0} must have a size between {min} and {max}. (input: {invalidValue})
```

Without `hideValue()`, the actual input value is shown in the message as is:

```
password must have a size between 8 and 20. (input: abc)
```

With `hideValue()`, the `{invalidValue}` part is replaced with a hidden-value marker:

```
password must have a size between 8 and 20. (input: (hidden))
```

The default is to show the value (`showsValue = true`).

### itemNameKey()

For details, see [ItemNameKey](item-name-key.md).

---

## Method Chaining

Each method returns `this`, so they can be written as method chains.

```java
Item item = new Item("password")
    .itemNameKey("account.password")
    .hideValue();
```
