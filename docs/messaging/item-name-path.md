# itemNamePath

A feature that appends information about which nesting level a validation error occurred at
to the item name when an error occurs in a nested object.

Without itemNamePath:

```text
'zipCode' must not be null
```

With itemNamePath:

```text
'zipCode' of 'Address' must not be null
```

---

## How It Works

When `itemPropertyPath` is `"address.zipCode"`, the rightmost `zipCode` is resolved as the item name,
and the remaining `address` is processed as the itemNamePath.

Each is converted to a name from `item_names.properties`,
and joined using the `jp.ecuacion.lib.core.common.itemNamePath.string` template.

Default template (English):

```properties
jp.ecuacion.lib.core.common.itemNamePath.string.default={0} of {1}
# {0} = item name ('zipCode')
# {1} = path ('Address')
```

---

## Setup Example

```java
public class UserForm implements ItemContainer {

    @Valid
    private AddressRecord address;

    @Override
    public Item[] customizedItems() { return new Item[] {}; }
}

public class AddressRecord implements ItemContainer {

    @NotNull
    private String zipCode;

    @Override
    public Item[] customizedItems() { return new Item[] {}; }
}
```

```properties
# item_names.properties
userForm.address=Address
addressRecord.zipCode=Zip Code
```

When a `@NotNull` violation occurs at `address.zipCode`:

```text
'Zip Code' of 'Address' must not be null
```

---

## Multiple Levels of Nesting

For 3 or more levels of nesting, the separator between levels in the path is
specified with `jp.ecuacion.lib.core.common.itemNamePath.separator`.

| Key | Default Value (en) | Description |
| --- | --- | --- |
| `...itemNamePath.string` | `{0} of {1}` | Format for the entire path |
| `...itemNamePath.separator` | `, ` | Separator between levels within the path |

Example: For `company.department.name` (3 levels):

```text
'Name' of 'Company', 'Department' must not be null
```
