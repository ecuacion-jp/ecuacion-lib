# MessageUtil

`MessageUtil` (`jp.ecuacion.lib.core.util.MessageUtil`) is a utility class for building messages
by combining `Item` with `PropertiesFileUtil`.

It is primarily called internally by the framework, but there are cases where
`formatValuesWithResolution()` is used in custom validator message generation.

---

## getItemNames()

```java
String result = MessageUtil.getItemNames(locale, itemList, showsItemNamePath, rootBean);
```

Generates a display item name string from a list of `Item` instances.
When there are multiple items, joins them with a separator and capitalizes the first letter.

### showsItemNamePath

When `showsItemNamePath` is `true`, the name of the parent path is appended to the item name.

For example, for the `address.city` field:

| `showsItemNamePath` | Output Example |
| --- | --- |
| `false` | 'City' |
| `true` | 'City' of 'Address' (the separator is the configured value) |

### Item Names for Collection Elements

For validation errors on elements within a collection, an expression indicating which element is automatically appended.

| Collection Type | Output Example |
| --- | --- |
| `List` | 'Name' (1st) |
| `Set` | 'Name' (any) |
| Map (key) | 'Name' (key: abc) |
| Map (value) | 'Name' (value: abc) |

### Customizing Display Symbols

The symbols before and after item names and the separator are configured in `messages.properties`.

| Key | Default | Description |
| --- | --- | --- |
| `jp.ecuacion.lib.core.common.itemName.prependSymbol` | `'` | Symbol prepended to item name |
| `jp.ecuacion.lib.core.common.itemName.appendSymbol` | `'` | Symbol appended to item name |
| `jp.ecuacion.lib.core.common.itemName.separator` | `, ` | Separator for multiple item names |
| `jp.ecuacion.lib.core.common.itemNamePath.string` | (path format) | Format when `showsItemNamePath` is used |
| `jp.ecuacion.lib.core.common.itemNamePath.separator` | ` > ` | Path separator symbol |

---

## formatValuesWithResolution()

```java
Arg arg = MessageUtil.formatValuesWithResolution(String[] values);
```

Resolves each string as a property key in the order `messages` → `item_names` → `enum_names` → `constants`,
wraps them with display symbols, joins them with a separator, and returns the result as an `Arg`.
If a key is not found, the string is treated as a literal.

```java
// enum_names.properties: status.active=Active, status.inactive=Inactive
Arg arg = MessageUtil.formatValuesWithResolution(
    new String[]{"status.active", "status.inactive"});
// resolves to "'Active', 'Inactive'"

// key not found: used as-is
Arg arg = MessageUtil.formatValuesWithResolution(new String[]{"ACTIVE", "INACTIVE"});
// resolves to "'ACTIVE', 'INACTIVE'"
```

Used when displaying condition values in error messages in custom validators.

---

## formatValues()

```java
Arg arg = MessageUtil.formatValues(String[] values);
```

Treats each string as a literal without resolving property keys,
wraps them with display symbols, joins them with a separator, and returns the result as an `Arg`.

```java
Arg arg = MessageUtil.formatValues(new String[]{"ACTIVE", "INACTIVE"});
// resolves to "'ACTIVE', 'INACTIVE'"
```

The only difference from `formatValuesWithResolution()` is that property key resolution is not performed.
Used when the values are already the final strings for display.
