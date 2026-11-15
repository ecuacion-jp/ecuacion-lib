# ItemUtil

## 概要

`ItemUtil`（`jp.ecuacion.lib.core.util.ItemUtil`）は、
`Item` や `ItemContainer` を扱うユーティリティクラスです。

内部で `PropertyPathUtil` などの汎用ユーティリティを使用する上位レイヤーに位置し、
ecuacion の Item モデルに特化したパイプラインを提供します。
[ItemContainer#getItem()](item-container.md) が ItemContainer 内部での検索を担うのに対し、
`ItemUtil` は rootBean と fullPropertyPath を受け取り、
ItemContainer の発見・委譲・`itemNameKey` の確定まで行うパイプライン全体を提供します。

---

## resolveItem()

```java
Item item = ItemUtil.resolveItem(fullPropertyPath, rootBean);
```

以下のパイプラインを実行し、`itemNameKey` と `showsValue` が確定した完成品の `Item` を返します。

1. `fullPropertyPath` と `rootBean` から ItemContainer を探す（1 階層まで）
2. ItemContainer が見つかれば `ItemContainer#getItem(itemPropertyPath)` に委譲
3. `itemNameKey` を確定し、`showsValue` を反映
4. 完成品の `Item` を返す

### ItemContainer の検索ルール

| rootBean の状態 | 動作 |
| --- | --- |
| rootBean 自身が `ItemContainer` | `rootBean.getItem(fullPropertyPath)` を呼ぶ |
| `fullPropertyPath` の第 1 ノードの子が `ItemContainer` | その子の `getItem(残りのパス)` を呼ぶ |
| どちらでもない | `PropertyPathUtil.getClass()` で型情報から直接解決 |

検索は **1 階層まで** です。

### 使用例

```java
// Jakarta Validation の ConstraintViolation から利用する例
Item item = ItemUtil.resolveItem(
    cv.getPropertyPath().toString(),
    cv.getRootBean()
);
```

---

## ItemContainer#getItem() との違い

| | `ItemContainer#getItem()` | `ItemUtil#resolveItem()` |
| --- | --- | --- |
| 入力の起点 | ItemContainer 自身 | RootBean |
| ItemContainer の発見 | しない（自分がそれ） | する（1 階層まで） |
| itemNameKey の確定 | しない | する |
| 返り値 | 中間オブジェクト | 完成品 |
