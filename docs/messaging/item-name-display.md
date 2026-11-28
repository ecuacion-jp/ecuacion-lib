# prefix, postfix, separator

## Overview

When displaying item names in error messages, you can customize the symbols placed before and after the name (prefix/postfix),
and the delimiter used when listing multiple item names (separator).

The default (English) wraps item names in single quotes like `'name'`.

For details on how to set up messages with item names (`showsItemName`), see [MessageParameters > showsItemName](../validation/message-parameters.md#showsitemname--switching-between-messages-withwithout-item-names).

---

## Default Values

| Setting | Property Key | Default (ja) | Default (en) |
| --- | --- | --- | --- |
| prefix (leading symbol) | `...itemName.prependSymbol` | `「` | `'` |
| postfix (trailing symbol) | `...itemName.appendSymbol` | `」` | `'` |
| separator (delimiter) | `...itemName.separator` | `、` | `, ` |

The property key prefix is `jp.ecuacion.lib.core.common`.
The other supported languages also have their own defaults that follow each language's conventions (e.g. `« »` for French, `„ “` for German).

---

## How to Customize

Add keys to `messages.properties` (or `messages_en.properties`).

```properties
# To wrap with 【 】
jp.ecuacion.lib.core.common.itemName.prependSymbol=【
jp.ecuacion.lib.core.common.itemName.appendSymbol=】

# To change the separator
jp.ecuacion.lib.core.common.itemName.separator= / 
```

---

## Delimiter for Multiple Item Names

For validation annotations that span multiple fields such as `@AnyNotEmpty`,
multiple item names are joined with the separator and displayed.

Default (English):

```
Please enter at least one of 'name', 'email address'
```

When `separator` is changed to `" / "`:

```
Please enter at least one of 'name' / 'email address'
```

---

## prefix, postfix, separator for Values

When error messages contain values (such as `{invalidValue}`), the same symbols are also applied to values.
Value-specific settings are managed with `jp.ecuacion.lib.core.common.value.*` keys.
There are no Japanese-specific default definitions, so even in Japanese environments, the English default `'...'` (single quotes) is used.

| Key | Default Value |
| --- | --- |
| `...value.prependSymbol` | `'` |
| `...value.appendSymbol` | `'` |
| `...value.separator` | `,` |

To use `「」`, define them in `messages_ja.properties`.

---

## Detailed Reference

- Customizing collection element display → **[Item Names for List, Set, and Map](collection-item-name.md)**
- Separator for itemNamePath → **[itemNamePath](item-name-path.md)**
