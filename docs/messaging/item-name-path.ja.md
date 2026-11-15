# itemNamePath

ネストしたオブジェクトでバリデーションエラーが発生した場合に、
どのネスト階層で起きたかを項目名に付加する機能です。

itemNamePath なし:

```text
「郵便番号」にnullは許可されていません
```

itemNamePath あり:

```text
「住所」の「郵便番号」にnullは許可されていません
```

---

## 仕組み

`itemPropertyPath` が `"address.zipCode"` の場合、右端の `zipCode` を項目名として解決し、
残りの `address` を itemNamePath として処理します。

それぞれを `item_names.properties` から名前に変換し、
`jp.ecuacion.lib.core.common.itemNamePath.string` のテンプレートで結合します。

デフォルトのテンプレート（日本語）:

```properties
jp.ecuacion.lib.core.common.itemNamePath.string.default={1}の{0}
# {0} = 項目名（「郵便番号」）
# {1} = パス（「住所」）
```

---

## セットアップ例

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
userForm.address=住所
addressRecord.zipCode=郵便番号
```

`address.zipCode` で `@NotNull` 違反が発生した場合:

```text
「住所」の「郵便番号」にnullは許可されていません
```

---

## 複数階層のネスト

3階層以上ネストしている場合、パス内の区切りは
`jp.ecuacion.lib.core.common.itemNamePath.separator` で指定します。

| キー | デフォルト値（ja） | 説明 |
| --- | --- | --- |
| `...itemNamePath.string` | `{1}の{0}` | パス全体のフォーマット |
| `...itemNamePath.separator` | `の中の、` | パス内の階層区切り |

例：`company.department.name` の場合（3階層）:

```text
「会社」の中の、「部署」の「名前」にnullは許可されていません
```
