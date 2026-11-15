# ItemContainer

## Overview

For an overview, see also [Common Topics > item, ItemContainer and itemPropertyPath](item-property-path.md).

`ItemContainer` (`jp.ecuacion.lib.core.item.ItemContainer`) is an interface that holds `Item` instances
with customized display attributes for fields.

By implementing it on classes such as Records or Forms, you can centrally manage the item name key (`itemNameKey`)
and value visibility for the fields belonging to that class.

---

## How to Implement

To implement `ItemContainer`, define `customizedItems()`.

```java
public class UserRecord implements ItemContainer {

    private String name;
    private String password;

    @Override
    public Item[] customizedItems() {
        return new Item[] {
            new Item("password").hideValue(),
        };
    }
}
```

Fields that do not require customization do not need to be included in `customizedItems()`.
If no customized `Item` exists, an `Item` auto-generated with default values is used internally.

If no customization is needed at all, return an empty array.

```java
@Override
public Item[] customizedItems() {
    return new Item[] {};
}
```

---

## Inheriting Settings from Parent Classes

When a class implementing `ItemContainer` overrides `customizedItems()`,
`getItem()` (a method used internally to retrieve an `Item`) traverses up the class hierarchy and merges `Item` instances with the same `itemPropertyPath`.
Properties explicitly set in the child class take priority, and unset properties inherit the parent class's settings.

```java
// Parent class: sets itemNameKey and hideValue
public class UserRecord implements ItemContainer {
    private String name;
    private String password;

    @Override
    public Item[] customizedItems() {
        return new Item[] {
            new Item("name").itemNameKey("user.name"),
            new Item("password").itemNameKey("user.password").hideValue(),
        };
    }
}

// Child class: overrides itemNameKey for name depending on the screen
public class EditUserRecord extends UserRecord {
    @Override
    public Item[] customizedItems() {
        return new Item[] {
            new Item("name").itemNameKey("editUser.name"),
        };
    }
}
```

When calling `getItem("name")` on an instance of `EditUserRecord`:

- `itemNameKey` → `"editUser.name"` (child class setting takes priority)
- `showsValue` → `true` (not set in the parent either, so the default value is used)

When calling `getItem("password")`:

- `itemNameKey` → `"user.password"` (inherited from parent class)
- `showsValue` → `false` (inherits `hideValue()` from parent class)

If the child class does not override `customizedItems()` at all,
the parent class settings are simply used as-is.

> **Module Constraint**: This feature uses Java's `MethodHandles` to call each class hierarchy's
> `customizedItems()` individually, so `opens` settings are required in named module environments.
> See [Using in Named Module Environments](../other/named-module.md) for details.

---

## mergeItems()

A utility method that combines a common `Item[]` with a Record-specific `Item[]`.

```java
public class UserRecord implements ItemContainer {

    private static final Item[] COMMON_ITEMS = new Item[] {
        new Item("createdAt").itemNameKey("common.createdAt"),
        new Item("updatedAt").itemNameKey("common.updatedAt"),
    };

    @Override
    public Item[] customizedItems() {
        return mergeItems(COMMON_ITEMS, new Item[] {
            new Item("password").hideValue(),
        });
    }
}
```

If both arrays contain an `Item` with the same `itemPropertyPath`, a `RuntimeException` is thrown.

