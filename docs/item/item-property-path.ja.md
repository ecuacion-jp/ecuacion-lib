# itemとItemContainer・itemPropertyPath

## item

`item` とは、本ライブラリで定義される、画面上の入力項目・表示項目など、1 つの「項目」を表す概念です。
その値自体はオブジェクトのフィールドとして保持されますが、画面表示にはラベル文言や入力必須といった値以外の属性も必要になります。
`Item` は、1 つのフィールドに対するこれらの属性情報を保持するオブジェクトです（詳細は [Item](item.md) を参照）。

本ライブラリ（ecuacion-lib）では、`Item` およびそれに関連する各種クラスは、Jakarta Validation 及び `BusinessViolation`（詳細は [違反・例外の概要](../concepts/violations-overview.md) を参照）のエラーメッセージを生成する目的でのみ使用されます。
ecuacion-splib など本ライブラリを利用する他のライブラリでは、画面表示用のラベルなど、画面表示の用途にも使用されています。

---

## ItemContainer

`ItemContainer`（`jp.ecuacion.lib.core.item.ItemContainer`）は、item を保持するオブジェクトに実装するインターフェースです。
Form やその直下の DTO などのクラスに実装します（詳細は [ItemContainer](item-container.md) を参照）。

item を設定するには、その item を保持するオブジェクトが `ItemContainer` を実装している必要があります。
以下の例では、`ItemContainer` を使用して `password` の item に `hideValue` という属性を設定しています。

```java
public class UserDto implements ItemContainer {

    @Size(min = 8, max = 20)
    private String password;

    @Override
    public Item[] customizedItems() {
        return new Item[] {
            new Item("password").hideValue()
        };
    }
}
```

---

## itemPropertyPath

`new Item("xxx")` の `"xxx"` の部分が `itemPropertyPath` です。
上の例では `"password"` が `itemPropertyPath` にあたります。

`itemPropertyPath` は、**`customizedItems()` を定義している `ItemContainer` 自身を起点**として、対象フィールドへのパスを記載します。
例えば rootBean `UserForm` の直下に `UserDto` があっても、`UserDto` の `customizedItems()` には `"userDto.password"` ではなく `"password"` と書きます。

ネストしたオブジェクトのフィールドを指す場合は、propertyPath と同じドット記法で記載します。

| itemPropertyPath | 意味 |
| --- | --- |
| `"name"` | `ItemContainer` 直下の `name` フィールド |
| `"dept.name"` | `ItemContainer` が保持する `dept` オブジェクトの `name` フィールド |
