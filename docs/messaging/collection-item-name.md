# Item Names for List, Set, and Map

## Overview

When a validation error occurs on a field that is an element of a collection (List, Set, or Map),
information about "which element" is appended to the item name.

Example: When the 1st element of `tags` (`List<String>`) in `ArticleForm` violates `@NotNull`

```java
public class ArticleForm {
    private List<@NotNull String> tags;
}
```

```properties
# item_names.properties
articleForm.tags=Tags
```

```text
Element 1 contained by 'Tags' must not be null.
```

The item name shown in quotes (`Tags`) is the display name defined in `item_names.properties`.
The key (`articleForm.tags`) is the `itemNameKey`, which is normally determined automatically from the class
name and the field name (see [ItemNameKey](../item/item-name-key.md)).
If the key is not defined in `item_names.properties`, the key string (`articleForm.tags`) is displayed as is.

---

## Collection Types and Output Patterns

The collection type is analyzed from the collection layer of `itemPropertyPath`,
and a description is generated using the corresponding keyword.

| Collection Type | Keyword | Default Display (English) |
| --- | --- | --- |
| Element of `List<T>` | `order` | `element {n}` (1-based) |
| Element of `Set<T>` | `any` | `some element` |
| Key of `Map<K, V>` | `mapKey` | `some key element of key-value data` |
| Value of `Map<K, V>` | `mapValue` | `element with key [{key}]` |

For arrays (arrays of beans with `@Valid`), the item name is displayed with `order`, the same as `List<T>`.
Constraints on the elements of an array themselves are not validated, so they do not appear in item names either
(see [propertyPath in Collections > Arrays](../item/collection-property-path.md#arrays)).

---

## Concrete Examples

The output messages in the following examples are in English. The first letter of a message is capitalized.

### Case of List&lt;String&gt; (element itself)

As in the "Overview" example above. The propertyPath of the violation is `tags[0].<list element>`.

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

```properties
# item_names.properties
orderForm.items=Order Items
items.productCode=Product Code
```

Note that the `itemNameKey` of `productCode` is `items.productCode`, not `orderItemRecord.productCode`.
This is because, for a nested path (`items[1].productCode`), the field name of the preceding node (`items`) is
used as the class part (see [ItemNameKey](../item/item-name-key.md)).

When a `@NotNull` violation occurs at `items[1].productCode`, setting `MessageParameters.showsItemNamePath=true` appends collection position information:

```text
'Product Code' at element 2 contained by 'Order Items' must not be null.
```

With `showsItemNamePath=false` (default), only `'Product Code'` is shown.
For details, see [itemNamePath](item-name-path.md).

### Case of Map

```java
public class ProductForm {
    private Map<String, @NotNull String> labels;
}
```

```properties
# item_names.properties
productForm.labels=Labels
```

When the value for key `en` violates `@NotNull` (propertyPath: `labels[en].<map value>`):

```text
Element with key [en] contained by 'Labels' must not be null.
```

---

## Customizing Display Strings

The description text for collection elements can be overridden in `messages.properties`.

| Key (jp.ecuacion.lib.core.common.itemName.*) | Default Value (en) | Description |
| --- | --- | --- |
| `order` | `element {0}` | The nth element of a List (`{0}` = 1-based) |
| `any` | `some element` | Set element |
| `mapKey` | `some key element of key-value data` | Map key |
| `mapValue` | `element with key [{0}]` | Map value (`{0}` = key value) |
| `collectionItemName` | `{1} contained by {0}` | Pattern for combining field name (`{0}`) and element description (`{1}`) |

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
