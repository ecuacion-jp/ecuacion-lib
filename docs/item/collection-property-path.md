# propertyPath in Collections

## Overview

This page explains how to write `itemPropertyPath` (including shorthand forms) for collection fields such as List, Set, and Map.
For the basic idea of `itemPropertyPath` itself, see [Item and Related Terms](../concepts/item-terms.md).
For arrays, see "[Arrays](#arrays)" at the end of this page.

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

## itemPropertyPath for Collections

The `itemPropertyPath` passed to `new Item()` can be the standard Jakarta Validation propertyPath form shown
above (hereafter the "propertyPath-compliant form") as is.
It has the same form as the string output by `ConstraintViolation#getPropertyPath()` for a violation, so you can
use a path you found in logs and the like without modification.

```java
public class BookshelfForm implements ItemContainer {

    private List<@NotNull String> tagList;

    @Valid
    private List<Book> bookList;

    @Valid
    private List<List<Book>> shelfList;

    @Valid
    private Map<String, Author> authorMap;

    @Override
    public Item[] customizedItems() {
        return new Item[] {
            // Field of a List element (Book)
            new Item("bookList[0].title").itemNameKey("book.title"),
            // Field of an element (Book) of a List element
            new Item("shelfList[0].<list element>[1].title").itemNameKey("book.title"),
            // Field of a Map value (Author)
            new Item("authorMap[key1].name").itemNameKey("author.name"),
            // Elements themselves (each String in tagList) are specified with the collection field
            // (see the note below)
            new Item("tagList").itemNameKey("bookshelf.tag"),
        };
    }
}
```

The index (`[0]`) and Map key (`[key1]`) values are removed by the normalization performed when the path is
stored in the `Item`. So `new Item("bookList[0].title")` in the example above applies not only to the 0th element
but to the `title` of **all elements** of `bookList`.
Likewise, `new Item("authorMap[key1].name")` applies to the `name` of all values of `authorMap` regardless of the
key value.

!!! note "Violations on elements themselves"
    When generating an error message for a violation on an **element itself**, such as an element of
    `List<@NotNull String>` or a Map key / value itself, the `Item` set for the **collection field** (`tagList`) is
    used, not the one for the element's path (such as `tagList[0].<list element>`).
    The item name is displayed as the collection's item name with information such as "the 1st element" added
    (see [Item Names for List, Set, and Map](../messaging/collection-item-name.md)).
    So if you want to configure the item name and the like for elements themselves, set it on
    `new Item("tagList")` as in the example above.

---

## Shorthand Forms as itemPropertyPath

Besides the propertyPath-compliant form in the previous section, `itemPropertyPath` can also be specified in a
shorthand form that omits indices and the keywords representing elements (such as `<list element>`).
Both are normalized internally, so either form produces the same result.

```java
// Both have the same meaning
new Item("bookList[1].title")   // propertyPath-compliant form (with index)
new Item("bookList[].title")    // shorthand form
```

The table below summarizes the propertyPath-compliant and shorthand forms by collection type.
All field names are for illustration purposes. `User` is a class with a `name` field.
For rows whose target is an element itself (other than `User.name`), as noted in the previous section, the
`Item` set for the collection field is used when generating error messages.

### List

| Field Type | Target | propertyPath-compliant Form | Shorthand Form |
| --- | --- | --- | --- |
| `List<String> strList` | Element itself | `strList[0].<list element>` | `strList[]` |
| `List<User> userList` | `User.name` | `userList[0].name` | `userList[].name` |
| `List<List<String>> nestedList` | Inner String element | `nestedList[0].<list element>[1].<list element>` | `nestedList[][]` |
| `List<List<User>> nestedList` | Inner `User.name` | `nestedList[0].<list element>[1].name` | `nestedList[][].name` |

### Set

Since a Set has no order, no index is attached even in the propertyPath-compliant form (it stays `[]`).

| Field Type | Target | propertyPath-compliant Form | Shorthand Form |
| --- | --- | --- | --- |
| `Set<String> strSet` | Element itself | `strSet[].<iterable element>` | `strSet[]` |
| `Set<User> userSet` | `User.name` | `userSet[].name` | `userSet[].name` (same) |
| `Set<Set<String>> nestedSet` | Inner String element | `nestedSet[].<iterable element>[].<iterable element>` | `nestedSet[][]` |
| `Set<Set<User>> nestedSet` | Inner `User.name` | `nestedSet[].<iterable element>[].name` | `nestedSet[][].name` |

### Map (Keys)

A node representing a key has `<K>` attached, which distinguishes it from the `itemPropertyPath` for values.
In the propertyPath-compliant form, the `[ ]` contains the value of the key's `toString()` (written as `key1` /
`key2` in the table below; it is `[]` when the key is `null` or an empty string).

| Field Type | Target | propertyPath-compliant Form | Shorthand Form |
| --- | --- | --- | --- |
| `Map<String, ?> strMap` | Key itself | `strMap<K>[key1].<map key>` | `strMap<K>[]` |
| `Map<User, ?> userMap` | Key's `User.name` | `userMap<K>[key1].name` | `userMap<K>[].name` |
| `Map<Map<String, ?>, ?> nestedMap` | Key itself of the inner Map | `nestedMap<K>[key1].<map key><K>[key2].<map key>` | `nestedMap<K>[]<K>[]` |
| `Map<Map<User, ?>, ?> nestedMap` | `User.name` of the inner Map's key | `nestedMap<K>[key1].<map key><K>[key2].name` | `nestedMap<K>[]<K>[].name` |

### Map (Values)

| Field Type | Target | propertyPath-compliant Form | Shorthand Form |
| --- | --- | --- | --- |
| `Map<?, String> strMap` | Value itself | `strMap[key1].<map value>` | `strMap[]` |
| `Map<?, User> userMap` | Value's `User.name` | `userMap[key1].name` | `userMap[].name` |
| `Map<?, Map<?, String>> nestedMap` | Value itself of the inner Map | `nestedMap[key1].<map value>[key2].<map value>` | `nestedMap[][]` |
| `Map<?, Map<?, User>> nestedMap` | `User.name` of the inner Map's value | `nestedMap[key1].<map value>[key2].name` | `nestedMap[][].name` |

### Combined Patterns

When List, Set, and Map are combined, the path is formed by joining the form of each collection from the
outside in.
In the shorthand form, List, Set, and Map (values) are no longer distinguished and all become `[]` (only Map
keys are distinguished, as `<K>[]`).

| Field Type | Target | propertyPath-compliant Form | Shorthand Form |
| --- | --- | --- | --- |
| `Map<?, List<User>> map` | `User.name` in the value's List | `map[key1].<map value>[1].name` | `map[][].name` |
| `List<Map<String, ?>> list` | Key itself of the element's Map | `list[0].<list element><K>[key1].<map key>` | `list[]<K>[]` |
| `List<Set<String>> list` | String element of the element's Set | `list[0].<list element>[].<iterable element>` | `list[][]` |
| `Map<?, Map<?, List<User>>> map` | `User.name` in the List of the value's Map value | `map[key1].<map value>[key2].<map value>[1].name` | `map[][][].name` |

### Duplicate Registration

Paths with different indices become the same key after normalization, so registering duplicates
within the same `customizedItems()` will cause a runtime exception.

```java
// NG: Both become "userList[].name" after normalization, causing duplicates
new Item("userList[1].name"),
new Item("userList[2].name")
```

---

## Arrays

Arrays are handled differently from List, Set, and Map, and Jakarta Validation can do less with them.

### Constraints on the Array Itself

Putting `@NotNull`, `@Size`, etc. on an array field validates the constraint against the array itself.
With `@Size`, the length of the array is checked. The propertyPath is the field name itself
(e.g., `strArray`).

### Constraints on Elements Are Not Validated

Even if you write `@NotNull String[] strArray`, it is not validated as a constraint on the array's
**elements** (as described above, it is treated only as `@NotNull` on the array itself).

This is because Jakarta Validation's constraints on elements (container element constraints) are defined
as a mechanism of putting constraints on the **type arguments** of generic containers such as `List`,
`Map`, and `Optional`, and arrays are outside that scope
(see [Jakarta Validation 3.1 Specification, 5.5 Container element constraints](https://jakarta.ee/specifications/bean-validation/3.1/jakarta-validation-spec-3.1.html#constraintdeclarationvalidationprocess-containerelementconstraints)).
If you want to validate elements, use a collection such as `List`, as in `List<@NotNull String>`.

### Arrays of Beans (`@Valid`)

For an array whose elements are beans, putting `@Valid` on the field validates the constraints placed on
the fields of each element bean (cascading with `@Valid` on arrays is explicitly stated in the Jakarta
Validation specification). Since arrays have no type arguments, put `@Valid` on the field itself.

The propertyPath and `itemPropertyPath` take the same form as for `List<User>`.

| Field Type | Target | propertyPath-compliant Form | Shorthand Form |
| --- | --- | --- | --- |
| `@Valid User[] userArray` | `User.name` | `userArray[0].name` | `userArray[].name` |
