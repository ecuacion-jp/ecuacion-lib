# item, ItemContainer and itemPropertyPath

## item

An `item` is a concept defined by this library, representing a single "item" on screen, such as an input field or a display field.
Its value itself is held as a field on an object, but displaying it also requires attributes beyond the value, such as the label text or whether it is required.
An `Item` is an object that holds these attribute values for a single field (see [Item](item.md) for details).

In this library (ecuacion-lib), `Item` and its related classes are used solely to generate error messages for Jakarta Validation
and `BusinessViolation` (for details, see [Overview of Violations and Exceptions](../concepts/violations-overview.md)).
In other libraries that build on this library, such as ecuacion-splib, they are also used for on-screen display, such as display labels.

---

## ItemContainer

`ItemContainer` (`jp.ecuacion.lib.core.item.ItemContainer`) is an interface implemented by an object that holds items.
It is implemented on classes such as a Form or the DTO directly beneath it (for details, see [ItemContainer](item-container.md)).

To configure an item, the object holding that item must implement `ItemContainer`.
In the following example, `ItemContainer` is used to set an attribute called `hideValue` on the `password` item.

```java
public class UserDto implements ItemContainer {

    @Size(min = 8, max = 20)
    private String password;

    @Override
    public Item[] customizedItems() {
        return new Item[] {
            new Item("password").hideValue()
        };
    }
}
```

---

## itemPropertyPath

The `"xxx"` part of `new Item("xxx")` is the `itemPropertyPath`.
In the example above, `"password"` is the `itemPropertyPath`.

An `itemPropertyPath` is written as a path to the target field **starting from the `ItemContainer` itself that defines `customizedItems()`**.
For example, even if `UserDto` sits directly under the rootBean `UserForm`, you write `"password"`, not `"userDto.password"`, in `UserDto`'s `customizedItems()`.

To point to a field of a nested object, use the same dot notation as propertyPath.

| itemPropertyPath | Meaning |
| --- | --- |
| `"name"` | The `name` field directly under the `ItemContainer` |
| `"dept.name"` | The `name` field of the `dept` object held by the `ItemContainer` |
