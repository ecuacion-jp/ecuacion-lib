# List・Set・Map の項目名

## 概要

バリデーションエラーが発生したフィールドがコレクション（List・Set・Map）の要素の場合、
項目名には「どの要素か」の情報が付加されます。

例: `ArticleForm` の `tags`（`List<String>`）の 1 番目の要素が `@NotNull` 違反の場合

```java
public class ArticleForm {
    private List<@NotNull String> tags;
}
```

```properties
# item_names_ja.properties
articleForm.tags=タグ
```

```text
「タグ」の1番目の要素にnullは許可されていません
```

「」の中に表示される項目名（`タグ`）は、`item_names.properties` に定義した表示名です。
キー（`articleForm.tags`）は `itemNameKey` で、通常はクラス名とフィールド名から自動で決まります（[ItemNameKey](../item/item-name-key.md) 参照）。
`item_names.properties` にキーが定義されていない場合は、キー文字列（`articleForm.tags`）がそのまま表示されます。

---

## コレクション種別と出力パターン

`itemPropertyPath` のコレクション層から種別を解析し、対応するキーワードで説明文を生成します。

| コレクション種別 | キーワード | デフォルト表示（日本語） |
| --- | --- | --- |
| `List<T>` の要素 | `order` | `{n}番目の要素`（1始まり） |
| `Set<T>` の要素 | `any` | `複数の要素のいずれか` |
| `Map<K, V>` のキー | `mapKey` | `キー項目のいずれか` |
| `Map<K, V>` の値 | `mapValue` | `キーが[{key}]の要素` |

配列の場合（`@Valid` を付けた Bean の配列）は、`List<T>` と同じく `order` で表示されます。
配列の要素そのものへの制約は検証されないため、項目名にも現れません（[collectionにおけるpropertyPath > 配列の場合](../item/collection-property-path.md#配列の場合) 参照）。

---

## 具体例

以下の例では、出力メッセージは日本語のものを示します。

### List&lt;String&gt; の場合（要素そのもの）

上記「概要」の例のとおりです。違反の propertyPath は `tags[0].<list element>` です。

### List&lt;T&gt; の場合（要素内フィールド）

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
# item_names_ja.properties
orderForm.items=注文明細
items.productCode=商品コード
```

`productCode` の `itemNameKey` は、`orderItemRecord.productCode` ではなく `items.productCode` になる点に注意してください。
ネストしたパス（`items[1].productCode`）では、クラス部に直前のノードのフィールド名（`items`）が使われるためです（[ItemNameKey](../item/item-name-key.md) 参照）。

`items[1].productCode` で `@NotNull` 違反が発生した場合、`MessageParameters.showsItemNamePath=true` を設定するとコレクション位置情報が付加されます:

```text
「注文明細」の2番目の要素の「商品コード」にnullは許可されていません
```

`showsItemNamePath=false`（デフォルト）では `「商品コード」` のみ表示されます。
詳細は [itemNamePath](item-name-path.md) を参照してください。

### Map の場合

```java
public class ProductForm {
    private Map<String, @NotNull String> labels;
}
```

```properties
# item_names_ja.properties
productForm.labels=ラベル
```

キー `en` の値が `@NotNull` 違反の場合（propertyPath: `labels[en].<map value>`）:

```text
「ラベル」のキーが[en]の要素にnullは許可されていません
```

---

## 表示文字列のカスタマイズ

コレクション要素の説明文は `messages.properties` で上書きできます。

| キー（jp.ecuacion.lib.core.common.itemName.*） | デフォルト値（ja） | 説明 |
| --- | --- | --- |
| `order` | `{0}番目の要素` | List の n 番目（`{0}` = 1始まり） |
| `any` | `複数の要素のいずれか` | Set の要素 |
| `mapKey` | `キー項目のいずれか` | Map のキー |
| `mapValue` | `キーが[{0}]の要素` | Map の値（`{0}` = キー値） |
| `collectionItemName` | `{0}の{1}` | フィールド名（`{0}`）と要素説明（`{1}`）の結合パターン |

カスタマイズ例:

```properties
# messages.properties
jp.ecuacion.lib.core.common.itemName.order=第{0}項目
```

---

## 詳細リファレンス

- itemPropertyPath のコレクション記法 → **[collectionにおけるpropertyPath](../item/collection-property-path.md)**
- prefix・postfix・separator のカスタマイズ → **prefix・postfix・separator**
- ネストしたオブジェクトの場合 → **[itemNamePath](item-name-path.md)**
