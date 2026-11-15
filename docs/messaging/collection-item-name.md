# Item Names for List, Set, and Map

## Overview

When a validation error occurs on a field that is an element of a collection (List, Set, or Map),
information about "which element" is appended to the item name.

Example: When the 1st element of a `tags` list violates `@NotNull`:

```text
null is not allowed for the 1st element of 'tags'
```

---

## Collection Types and Output Patterns

The collection type is analyzed from the collection layer of `itemPropertyPath`,
and a description is generated using the corresponding keyword.

| Collection Type | Keyword | Default Display (English) |
| --- | --- | --- |
| Element of `List<T>` | `order` | `the {n}th element` (1-based) |
| Element of `Set<T>` | `any` | `any of multiple elements` |
| Key of `Map<K, V>` | `mapKey` | `any of the key items` |
| Value of `Map<K, V>` | `mapValue` | `the element with key [{key}]` |

---

## Concrete Examples

### Case of List&lt;String&gt; (element itself)

```java
@Valid
private List<@NotNull String> tags;
// itemPropertyPath: "tags[0].<list element>"
```

Example error message:

```text
null is not allowed for the 1st element of 'tags'
```

### Case of List&lt;T&gt; (field within an element)

```java
public class OrderForm implements ItemContainer {

    @Valid
    private List<OrderItemRecord> items;

    @Override
    public Item[] customizedItems() { return new Item[] {}; }
}

public class OrderItemRecord implements ItemContainer {

    @NotNull
    private String productCode;

    @Override
    public Item[] customizedItems() { return new Item[] {}; }
}
```

When a `@NotNull` violation occurs at `items[1].productCode`, setting `MessageParameters.showsItemNamePath=true` appends collection position information:

```text
null is not allowed for 'productCode' of the 2nd element of 'items'
```

With `showsItemNamePath=false` (default), only `'productCode'` is shown.
For details, see [itemNamePath](item-name-path.md).

### Case of Map

```java
@Valid
private Map<String, @NotNull String> labels;
// itemPropertyPath: "labels[en].<map value>"
```

Example error message:

```text
null is not allowed for the element with key [en] of 'labels'
```

---

## Customizing Display Strings

The description text for collection elements can be overridden in `messages.properties`.

| Key (jp.ecuacion.lib.core.common.itemName.*) | Default Value (en) | Description |
| --- | --- | --- |
| `order` | `the {0}th element` | The nth element of a List (`{0}` = 1-based) |
| `any` | `any of multiple elements` | Set element |
| `mapKey` | `any of the key items` | Map key |
| `mapValue` | `the element with key [{0}]` | Map value (`{0}` = key value) |
| `collectionItemName` | `{1} of {0}` | Pattern for combining field name (`{0}`) and element description (`{1}`) |

Customization example:

```properties
# messages.properties
jp.ecuacion.lib.core.common.itemName.order=item #{0}
```

---

## Detailed Reference

- Collection notation for itemPropertyPath → **[propertyPath in Collections](../item/collection-property-path.md)**
- Customizing prefix, postfix, and separator → **prefix, postfix, separator**
- For nested objects → **[itemNamePath](item-name-path.md)**
