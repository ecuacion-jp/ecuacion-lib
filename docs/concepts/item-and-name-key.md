# ItemNameKey

This describes the mechanism for item name keys used in validation error messages.

---

## itemNameKey

A key used to look up the display name of an item from `item_names.properties`.
The format is `"classPart.fieldPart"` (e.g., `"user.name"`), which corresponds to keys in `item_names.properties`.

```properties
# item_names.properties
user.name=Full Name
user.email=Email Address
```

`itemNameKey` is normally resolved automatically, but can also be specified explicitly on an `Item`.
For details on the automatic resolution rules, see [ItemNameKey](../item/item-name-key.md).

---

## @ItemNameKeyClass

An annotation that specifies the default value of the class part of `itemNameKey` in bulk.

For details, see [ItemNameKey](../item/item-name-key.md).
