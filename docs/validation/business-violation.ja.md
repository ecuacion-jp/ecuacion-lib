# BusinessViolation

## プレースホルダー引数を渡す

```java
// {0} に "年齢"、{1} に "0"、{2} に "150" が埋め込まれる
violations.add(new BusinessViolation("error.range", "年齢", "0", "150"));
```

---

## 関連フィールドを指定する

どのフィールドに関する違反かを `itemPropertyPath` で関連付けられます。

```java
violations.add(new BusinessViolation(new String[] {"birthDate"}, "error.future-date"));

// 複数フィールドにまたがる違反
violations.add(new BusinessViolation(
    new String[] {"startDate", "endDate"}, "error.date-range"));
```

---

## 項目名をメッセージに埋め込む（itemNameKeys）

`messages.properties` に `{item_name}` プレースホルダーを含むメッセージを定義し、
`itemNameKeys` を指定すると、メッセージ生成時に `item_names.properties` から項目名が解決されます。

```properties
# messages.properties
error.already-registered={item_name}はすでに登録されています
```

```properties
# item_names.properties
customer.email=メールアドレス
```

```java
// 第1引数：itemNameKeys（item_names.properties のキー）
// 第2引数：itemPropertyPaths（UI のフィールドハイライト用）
violations.add(new BusinessViolation(
    new String[] {"customer.email"},
    new String[] {"email"},
    "error.already-registered"));
// → 「メールアドレスはすでに登録されています」
```

`itemNameKeys` が不要（`{item_name}` を使わない）場合は、従来通り `itemPropertyPaths` のみ指定します。

---

## violations.add() ショートハンド

`BusinessViolation` を明示せず、`Violations` に直接メッセージキーなどを指定できます。

```java
// BusinessViolation を明示した書き方（標準）
violations.add(new BusinessViolation("error.some-message-id"));

// 同等のショートハンド
violations.add("error.some-message-id");
```

`itemPropertyPath` 付きや引数付きのオーバーロードも同様です。

```java
violations.add(new String[] {"fieldName"}, "error.message");
violations.add("error.range", "年齢", "0", "150");
```
