# ItemContainer Search

## Overview

A Jakarta Validation error message reflects attributes of the `Item` corresponding to the field that caused
the error, such as the item name (`itemNameKey`) or the setting that hides the value (`hideValue`).
So when generating the message, that `Item` must be obtained first.

Since an `Item` is held by the `ItemContainer` that defines it (the class implementing `customizedItems()`),
the target `ItemContainer` has to be identified before the `Item` can be obtained.
This `ItemContainer` is identified by searching based on the rootBean and the propertyPath of the violation.
This page describes how that search works.

---

## ItemContainer Search Scope

`ItemUtil.resolveItem()` is a method that finds the ItemContainer based on the rootBean and propertyPath
and resolves the `Item`. It is called internally by the framework when generating validation error messages
(for details, see [ItemUtil](item-util.md)).

The search scope is **up to 1 level from the rootBean**. Specifically, the `ItemContainer` is searched
for in the following order.

1. If the **rootBean itself** is an `ItemContainer`, it is used. The propertyPath of the violation is
   passed to `getItem()` as is.
2. Otherwise, if the **object directly under the rootBean (1st level)** that corresponds to the first node
   of the propertyPath is an `ItemContainer`, it is used. The propertyPath with its first node removed
   is passed to `getItem()`.
3. If neither applies, the `ItemContainer` is treated as not found. In this case no `Item` settings are
   used, and the `itemNameKey` is determined automatically (see [ItemNameKey](item-name-key.md)).

Objects at the 2nd level or deeper are not searched, even if they implement `ItemContainer`.

### Example

Consider the following structure. `UserForm` is the rootBean and does not implement `ItemContainer`.

```java
public class UserForm {                              // rootBean
    @Valid
    private UserDto userDto;
}

public class UserDto implements ItemContainer {      // 1st level from the rootBean
    @NotNull
    private String name;

    @Valid
    private AddressDto address;

    @Override
    public Item[] customizedItems() {
        return new Item[] {
            new Item("name").itemNameKey("user.name"),
            new Item("address.city").itemNameKey("user.city"),
        };
    }
}

public class AddressDto implements ItemContainer {   // 2nd level from the rootBean
    @NotNull
    private String city;

    @Override
    public Item[] customizedItems() {
        return new Item[] {
            new Item("city").itemNameKey("address.city"),
        };
    }
}
```

When `name` and `city` each violate `@NotNull`, the `Item` is resolved as follows.

| propertyPath of the violation | ItemContainer used | itemPropertyPath passed to `getItem()` | Resulting itemNameKey |
| --- | --- | --- | --- |
| `"userDto.name"` | `UserDto` | `"name"` | `user.name` |
| `"userDto.address.city"` | `UserDto` | `"address.city"` | `user.city` |

In the second row, `city` is a field of `AddressDto`, but since `AddressDto` is at the 2nd level it is not
searched; `UserDto`'s `customizedItems()` at the 1st level is used instead. Therefore the
`new Item("city")` setting on the `AddressDto` side is not used in this case.
If you want to configure the `Item` of a field at the 2nd level or deeper, write it in the `ItemContainer`
at the 1st level (or the rootBean) with a path such as `"address.city"`, as in `UserDto` above.

Note that if `UserForm` itself implements `ItemContainer`, case 1 applies and `UserForm` is used for both
violations (`"userDto.name"` and `"userDto.address.city"` are passed to `getItem()`).

---

## Note: Why Sibling ItemContainers Don't Cause Ambiguity

For example, if `UserForm` has two direct children, `UserDto` and `DeptDto`, both `ItemContainer`s and both with a `name` field,
you might wonder whether writing `itemPropertyPath` as simply `"name"` would leave it unclear which `ItemContainer` is meant.
In practice, this ambiguity does not arise, for two reasons corresponding to the two ways `itemPropertyPath` is used:

1. **When displaying validation messages** (starting from the field where the violation occurred): Jakarta Validation itself has already
   identified the field where the violation occurred by walking from the rootBean. The search for an `ItemContainer` starts from that
   already-identified propertyPath from the rootBean (fullPropertyPath, e.g., `"userDto.name"`), so it is never reduced to an ambiguous bare `"name"`.
2. **When displaying item names via Thymeleaf in splib** (walking from the rootBean toward the target item in order): even though the
   `itemPropertyPath` written on each component may be in shorthand form (e.g., `"name"`), the part that was shortened (i.e., which
   `ItemContainer`'s scope it belongs to) is specified separately elsewhere, so the component as a whole can still uniquely identify its target.
