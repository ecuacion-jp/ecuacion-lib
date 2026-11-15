# List・Set・Map の項目名

## 概要

バリデーションエラーが発生したフィールドがコレクション（List・Set・Map）の要素の場合、
項目名には「どの要素か」の情報が付加されます。

例：`tags` リストの 1 番目の要素が `@NotNull` 違反の場合:

```text
「tags」の1番目の要素にnullは許可されていません
```

---

## コレクション種別と出力パターン

`itemPropertyPath` のコレクション層から種別を解析し、対応するキーワードで説明文を生成します。

| コレクション種別 | キーワード | デフォルト表示（日本語） |
| --- | --- | --- |
| `List<T>` の要素 | `order` | `{n}番目の要素`（1始まり） |
| `Set<T>` の要素 | `any` | `複数の要素のいずれか` |
| `Map<K, V>` のキー | `mapKey` | `キー項目のいずれか` |
| `Map<K, V>` の値 | `mapValue` | `キーが[{key}]の要素` |

---

## 具体例

### List&lt;String&gt; の場合（要素そのもの）

```java
@Valid
private List<@NotNull String> tags;
// itemPropertyPath: "tags[0].<list element>"
```

エラーメッセージ例:

```text
「tags」の1番目の要素にnullは許可されていません
```

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

`items[1].productCode` で `@NotNull` 違反が発生した場合、`MessageParameters.showsItemNamePath=true` を設定するとコレクション位置情報が付加されます:

```text
「items」の2番目の要素の「productCode」にnullは許可されていません
```

`showsItemNamePath=false`（デフォルト）では `「productCode」` のみ表示されます。
詳細は [itemNamePath](item-name-path.md) を参照してください。

### Map の場合

```java
@Valid
private Map<String, @NotNull String> labels;
// itemPropertyPath: "labels[en].<map value>"
```

エラーメッセージ例:

```text
「labels」のキーが[en]の要素にnullは許可されていません
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
