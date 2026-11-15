# ItemNameKey

## Overview

For an overview, see also [Common Topics > ItemNameKey](../concepts/item-and-name-key.md).

`itemNameKey` is a key used to look up the display name of an item from `item_names.properties` and similar files.
The format is `"classPart.fieldPart"` (e.g., `"user.name"`).

Unless explicitly specified on an `Item`, it is automatically determined based on the `itemPropertyPath` and class information.

---

## Automatic Resolution from itemPropertyPath

When `itemNameKey()` is not specified, `itemNameKey` is determined from the `itemPropertyPath` as follows.

- **Field part**: the rightmost node of the `itemPropertyPath`
- **Class part**: the second node from the right of the `itemPropertyPath`. If the `itemPropertyPath` is just a field name (there is no node for the class part), the `ItemContainer`'s class name with the first letter lowercased is used

Below, assume the object implementing `ItemContainer` is `UserDto`, and `UserDto` holds a `DeptDto` as a field (field name `dept`).

```java
public class UserDto implements ItemContainer {
    private String name;
    private DeptDto dept;   // DeptDto has a name field
}
```

| itemPropertyPath | Resulting itemNameKey |
| --- | --- |
| `"name"` | `"userDto.name"`<br>(there is no node for the class part, so the `ItemContainer`'s class name is used) |
| `"dept.name"` | `"dept.name"`<br>(class part is the `dept` node's name, used as-is) |

When the itemPropertyPath has multiple levels, such as `dept.manager.name`, the field part is the rightmost node (`name`), and the class part is the node immediately to its left (`manager`).

---

## Specifying itemNameKey Explicitly

You can also specify `itemNameKey` explicitly using `Item`'s `itemNameKey()`.
When specified explicitly, the specified value takes top priority regardless of the `itemPropertyPath`.

```java
new Item("mobilePhoneNumber.value").itemNameKey("mobilePhone.number")
// -> itemNameKey is "mobilePhone.number"
```

---

## itemNameKey Resolution Rules (Details)

`itemNameKey()` can also be given only the field part, omitting the class part.
If it contains `"."`, it is interpreted as `"classPart.fieldPart"`; otherwise it is interpreted as field part only.

```java
// Specifying only the field part (class part is resolved automatically)
new Item("name").itemNameKey("fullName")
```

Since `itemNameKey` and `itemPropertyPath` each have patterns with and without a class part specified, the class part and field part are determined independently of each other, in the following priority order (if 1 is unavailable, 2 is used; if 2 is unavailable, 3 is used).

1. The value specified via `itemNameKey()`
2. The `itemPropertyPath`
3. (Class part only) The `ItemContainer` class's class name

The concrete patterns, including the examples in the previous sections, are as follows (same `UserDto` / `DeptDto` assumptions as above).

| itemNameKey() specification | itemPropertyPath | Resulting itemNameKey |
| --- | --- | --- |
| `"user.name"`<br>(class part + field part) | Any | `"user.name"`<br>(used as-is regardless of the itemPropertyPath) |
| `"fullName"`<br>(field part only) | `"dept.name"` | `"dept.fullName"`<br>(class part is the `dept` node's name, used as-is) |
| `"fullName"`<br>(field part only) | `"name"` | `"userDto.fullName"`<br>(class part resolved from the `ItemContainer`'s class name) |
| Not specified | `"name"` | `"userDto.name"`<br>(field part is the rightmost node, class part is the `ItemContainer`'s class name) |
| Not specified | `"dept.name"` | `"dept.name"`<br>(class part is the `dept` node's name, used as-is; field part is the rightmost node) |

---

## `@ItemNameKeyClass` Annotation

As described above, when no class part is specified via `itemNameKey()` or `itemPropertyPath`, `userDto` is used by default as the class part of the itemNameKey.
In practice, though, `item_names.properties` is usually defined with a more generic form such as `user.name` rather than `userDto.name`.
This default class part `userDto` can be changed to `user` using `@ItemNameKeyClass` (`jp.ecuacion.lib.core.annotation.ItemNameKeyClass`).

`@ItemNameKeyClass` is an annotation that specifies the class part of `itemNameKey` in bulk for a class.
Without it, the class part defaults to the class name with the first letter lowercased, as described above.

```java
@ItemNameKeyClass("user")
public class UserDto implements ItemContainer {
    private String name;    // itemNameKey: "user.name"
    private String email;   // itemNameKey: "user.email"
}
```

### Placing It on a Field

`@ItemNameKeyClass` can also be placed on a field.
When the itemPropertyPath is nested, the field name is used as the class part as-is,
but for a field such as `List<DeptDto> deptList`, the class part becomes `deptList`, which forces you to define `deptList.name` in `item_names.properties`.
In such cases, placing `@ItemNameKeyClass` on the field changes the class part of all items under that field at once.

```java
public class UserDto implements ItemContainer {
    @ItemNameKeyClass("dept")
    private List<DeptDto> deptList;   // itemNameKey of deptList[0].name: "dept.name"

    private DeptDto belongingDept;    // itemNameKey of belongingDept.name: "belongingDept.name"
}
```

Placing it on a field takes effect only for the field corresponding to the class part of the itemPropertyPath (the second node from the right).
Also, if the class part is explicitly specified via `itemNameKey()`, that takes priority.

When a node of the itemPropertyPath is resolved to a getter rather than a field (that is, when no field of the same name exists), `@ItemNameKeyClass` can be placed on the getter.
When a field of the same name exists, the field is used, so `@ItemNameKeyClass` placed on the getter is ignored.

```java
public class UserDto implements ItemContainer {
    @ItemNameKeyClass("dept")
    public List<DeptDto> getDeptList() { ... }   // itemNameKey of deptList[0].name: "dept.name"
}
```

---

## Correspondence with item_names.properties

The resolved `itemNameKey` is used to look up the item name from `item_names.properties`.

```properties
# item_names.properties
userDto.name=Full Name
userDto.email=Email Address
user.name=Full Name
```
