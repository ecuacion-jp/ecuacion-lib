# Item

## 概要

概要説明は [共通事項など > itemとItemContainer・itemPropertyPath](item-property-path.md) もご確認ください。

`Item`（`jp.ecuacion.lib.core.item.Item`）は、フィールド 1 つ分の属性を保持するクラスです。
主にバリデーションエラーメッセージ中での項目名や値の表示方法を制御するために使われます。

バリデーション対象のフィールドすべてに `Item` を設定する必要はなく、指定がなければデフォルトの設定が適用されます。
エラーメッセージ上のフィールドの表示をカスタマイズしたい場合にのみ、[ItemContainer](item-container.md) の
`customizedItems()` を実装して `Item` を生成します。

```java
@Override
public Item[] customizedItems() {
    return new Item[] {
        new Item("password").hideValue(),
        new Item("name").itemNameKey("fullName")
    };
}
```

---

## 使用可能なメソッド

### hideValue()

パスワードなど、エラーメッセージに値を含めたくないフィールドには `hideValue()` を使います。

```java
new Item("password").hideValue()
```

例えば、以下のようなメッセージ定義がある場合、

```properties
jakarta.validation.constraints.Size.message.base = {0}は{min}から{max}の間のサイズにしてください（入力値：{invalidValue}）
```

`hideValue()` を指定しない場合、実際の入力値がそのままメッセージに出力されます。

```
passwordは8から20の間のサイズにしてください（入力値：abc）
```

`hideValue()` を指定した場合、`{invalidValue}` の部分が非表示を表す文言に置き換わります。

```
passwordは8から20の間のサイズにしてください（入力値：（非表示））
```

デフォルトは値を表示する設定（`showsValue = true`）です。

### itemNameKey()

詳細は [ItemNameKey](item-name-key.md) を参照。

---

## メソッドチェーン

各メソッドは `this` を返すため、メソッドチェーンで記述できます。

```java
Item item = new Item("password")
    .itemNameKey("account.password")
    .hideValue();
```
