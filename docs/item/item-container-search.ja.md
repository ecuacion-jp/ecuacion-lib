# ItemContainer の探索

## 概要

Jakarta Validation のバリデーションエラーメッセージには、項目名（`itemNameKey`）や値を表示しない設定（`hideValue`）など、
エラーとなったフィールドに対応する `Item` の属性情報が反映されます。
そのためメッセージを生成する際は、まずその `Item` を取得する必要があります。

`Item` は、その `Item` を定義している `ItemContainer`（`customizedItems()` を実装したクラス）に保持されているため、
`Item` を取得するには、まず対象の `ItemContainer` を特定しなければなりません。
この `ItemContainer` は、rootBean と違反の propertyPath をもとに探索して特定されます。
本ページでは、その探索の仕組みを説明します。

---

## ItemContainer の検索範囲

`ItemUtil.resolveItem()` は、rootBean と propertyPath をもとに ItemContainer を探して
`Item` を解決するメソッドです。バリデーションエラーメッセージを生成する際などにフレームワーク内部から呼ばれます（詳細は [ItemUtil](item-util.md) を参照）。

この検索範囲は **rootBean から 1 階層まで** です。具体的には、以下の順に `ItemContainer` を探します。

1. **rootBean 自体**が `ItemContainer` であれば、それを使います。`getItem()` には違反の propertyPath をそのまま渡します。
2. そうでなければ、propertyPath の先頭のノードにあたる **rootBean 直下（1 階層目）のオブジェクト**が `ItemContainer` であれば、それを使います。
   `getItem()` には、propertyPath から先頭のノードを除いたものを渡します。
3. どちらにも該当しない場合は、`ItemContainer` は見つからなかったものとして扱います。
   このとき `Item` の設定は使われず、`itemNameKey` は自動決定されます（[ItemNameKey](item-name-key.md) 参照）。

2 階層目以降のオブジェクトは、`ItemContainer` を実装していても探索の対象になりません。

### 例

以下のような構成で考えます。`UserForm` が rootBean で、`ItemContainer` は実装していません。

```java
public class UserForm {                              // rootBean
    @Valid
    private UserDto userDto;
}

public class UserDto implements ItemContainer {      // rootBean から 1 階層目
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

public class AddressDto implements ItemContainer {   // rootBean から 2 階層目
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

`name`・`city` がそれぞれ `@NotNull` 違反になった場合、`Item` は以下のように解決されます。

| 違反の propertyPath | 使われる ItemContainer | `getItem()` に渡す itemPropertyPath | 結果の itemNameKey |
| --- | --- | --- | --- |
| `"userDto.name"` | `UserDto` | `"name"` | `user.name` |
| `"userDto.address.city"` | `UserDto` | `"address.city"` | `user.city` |

2 行目の `city` は `AddressDto` のフィールドですが、`AddressDto` は 2 階層目なので探索されず、1 階層目の `UserDto` の `customizedItems()` が使われます。
そのため `AddressDto` 側の `new Item("city")` の設定は、この場合には使われません。
2 階層目以降のフィールドの `Item` を設定したい場合は、上の `UserDto` のように、1 階層目（または rootBean）の `ItemContainer` に `"address.city"` のようなパスで記載してください。

なお、`UserForm` 自体が `ItemContainer` を実装している場合は 1. に該当し、どちらの違反でも `UserForm` が使われます
（`getItem()` には `"userDto.name"`・`"userDto.address.city"` が渡されます）。

---

## 補足: 兄弟関係にある複数の ItemContainer があっても曖昧にならない理由

例えば `UserForm` の直下に `UserDto`・`DeptDto` という 2 つの `ItemContainer` があり、両方とも `name` フィールドを持つ場合、
`itemPropertyPath` として単に `"name"` とだけ書いたのでは、どちらの `ItemContainer` を指しているか区別できないのでは、と疑問に思うかもしれません。
実際には、以下の 2 つの利用パターンにおいて、この曖昧性は生じません。

1. **Jakarta Validation でのメッセージ表示時**（違反の発生したフィールドを起点とするケース）: バリデーション違反が発生したフィールドは、rootBean から辿って
   Jakarta Validation 自身がすでに特定済みです。`ItemContainer` の探索は、その特定済みの rootBean 起点の propertyPath（fullPropertyPath。例: `"userDto.name"`）を起点に行われるため、
   単なる `"name"` のような曖昧な形になることはありません。
2. **splib の Thymeleaf による項目名表示時**（rootBean から順に対象の item を辿るケース）: 各コンポーネントに記載する `itemPropertyPath` は省略形
   （例: `"name"`）であっても、その省略された部分（どの `ItemContainer` のスコープかという情報）は別途指定されているため、
   コンポーネント全体としては対象を一意に特定できます。
