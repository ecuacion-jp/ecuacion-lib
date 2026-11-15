# propertyPath in Collections

## Overview

This page explains how to write `itemPropertyPath` (including shorthand forms) for collection fields such as List, Set, and Map.
For the basic idea of `itemPropertyPath` itself, see [item, ItemContainer and itemPropertyPath](item-property-path.md).

---

## Standard Jakarta Validation propertyPath for Collections

When Jakarta Validation (Hibernate Validator) places a constraint on an element of a List, Set, or Map itself, it represents that
element using a dedicated keyword instead of a field name.

| Type | Keyword representing the element |
| --- | --- |
| `List<T>` | `<list element>` |
| `Set<T>` | `<iterable element>` |
| Key of `Map<K, V>` | `<map key>` |
| Value of `Map<K, V>` | `<map value>` |

For example, if there's a constraint on the element itself (the `String` value) of `List<String> strList`, the propertyPath takes
the form of an indexed bracket followed by this keyword (`strList[0].<list element>`).
Since sets do not have ordering, indices do not appear in runtime paths (`strSet[].<iterable element>`).
A constraint on a map key takes the form `strMap<K>[].<map key>`, with a qualifier indicating key access, while a constraint on a
map value takes the form `strMap[key1].<map value>`, using the key's value.

---

## Shorthand Forms as itemPropertyPath

The `itemPropertyPath` passed to `new Item()` can be specified either in the propertyPath-compliant form above (with indices)
or in the shorthand form with indices omitted.
Both are normalized internally, so either form produces the same result.

```java
// Both have the same meaning
new Item("bookList[1].title")   // propertyPath-compliant form (with index)
new Item("bookList[].title")    // shorthand form
```

The table below summarizes the propertyPath-compliant and shorthand forms by collection type.
All field names are for illustration purposes. `User` is a class with a `name` field.

### List

| Field Type | Target | propertyPath-compliant Form | Shorthand Form |
| --- | --- | --- | --- |
| `List<String> strList` | Element itself | `strList[0].<list element>` | `strList[]` |
| `List<User> userList` | `User.name` | `userList[0].name` | `userList[].name` |
| `List<List<String>> nestedList` | Inner String element | `nestedList[0].<list element>[1].<list element>` | `nestedList[][]` |
| `List<List<User>> nestedList` | Inner `User.name` | `nestedList[0].<list element>[1].name` | `nestedList[][].name` |

### Set

There are many cases where the form does not change between before and after normalization.

| Field Type | Target | propertyPath-compliant Form | Shorthand Form |
| --- | --- | --- | --- |
| `Set<String> strSet` | Element itself | `strSet[].<iterable element>` | `strSet[]` |
| `Set<User> userSet` | `User.name` | `userSet[].name` | `userSet[].name` (same) |

### Map

The `itemPropertyPath` for keys and values are distinguished.

| Field Type | Target | propertyPath-compliant Form | Shorthand Form |
| --- | --- | --- | --- |
| `Map<String, ?> strMap` | Key itself | `strMap<K>[].<map key>` | `strMap<K>[]` |
| `Map<?, String> strMap` | Value itself | `strMap[key1].<map value>` | `strMap[]` |
| `Map<?, User> strMap` | Value's `User.name` | `strMap[key1].name` | `strMap[].name` |

### Duplicate Registration

Paths with different indices become the same key after normalization, so registering duplicates
within the same `customizedItems()` will cause a runtime exception.

```java
// NG: Both become "userList[].name" after normalization, causing duplicates
new Item("userList[1].name"),
new Item("userList[2].name")
```
