# 項目名の使い方

## 概要

ecuacion-lib のバリデーションエラーメッセージは、「どのフィールドでエラーが起きたか」を示す
**項目名**を含めて表示できます。

項目名なし（Hibernate Validator デフォルト）:

```text
null は許可されていません
```

項目名あり:

```text
「名前」にnullは許可されていません
```

項目名入りのメッセージにする設定（`showsItemName`）方法の詳細は [MessageParameters > showsItemName](../validation/message-parameters.md#showsitemname--項目名ありなしのメッセージ切り替え) を参照してください。

---

## 全体の流れ

```text
フィールド
  ↓  itemPropertyPath で特定
Item
  ↓  itemNameKey を解決
item_names.properties のキー
  ↓  値を取得
表示名（例: 名前）
  ↓  ValidationMessagesWithItemNames.properties に代入
エラーメッセージ（例: 「名前」にnullは許可されていません）
```

最後のステップで使われるメッセージ文（`ValidationMessagesWithItemNames.properties`）の内容・使われる条件・上書き方法は、
[標準アノテーションのメッセージ出力 > 項目名をメッセージに含める](../validator/standard-annotation-messages.md#項目名をメッセージに含める) を参照してください。

---

## item_names.properties

項目名の表示名を管理するプロパティファイルです。

```properties
# item_names.properties
userRecord.name=名前
userRecord.email=メールアドレス
userRecord.birthDate=生年月日
```

キーが `itemNameKey`、値が画面に表示される名前です。

### messages.properties との使い分け

`item_names.properties` の代わりに `messages.properties` に定義することも可能です。
ただし、通常のメッセージ文と項目名定義が混在して煩雑になるため、
`item_names.properties` に分けて管理することを推奨します。

### ローカライズ

サフィックスを付けたファイルでロケール別に定義できます。

```properties
# item_names.properties（デフォルト）
userRecord.name=name

# item_names_ja.properties（日本語）
userRecord.name=名前
```

`PropertiesFileUtil` の `item_names` ファイル種別として管理されるため、
ロケールの解決は標準の Java ResourceBundle と同じ規則に従います。

---

## セットアップ手順

### 1. ItemContainer を実装する

バリデーション対象のクラスに `ItemContainer` を実装します。
詳細は [ItemContainer](../item/item-container.md) を参照してください。

```java
public class UserRecord implements ItemContainer {

    @NotNull
    private String name;

    @Override
    public Item[] customizedItems() {
        return new Item[] {};  // カスタマイズ不要な場合は空配列
    }
}
```

### 2. item_names.properties に項目名を登録する

```properties
# src/main/resources/item_names.properties
userRecord.name=名前
userRecord.email=メールアドレス
```

---

## BusinessViolation での利用

業務ロジックのチェックで項目名入りメッセージを使いたい場合は、`BusinessViolation` に `itemNameKeys` を渡します。
`itemNameKeys` の値が `item_names.properties` のキーとして使われます。

```java
violations.add(new BusinessViolation(
    new String[] {"customer.email"},  // itemNameKeys
    new String[] {"email"},            // itemPropertyPaths（UI ハイライト用）
    "error.already-registered"));
```

詳細は [Violation](../validation/violation.md) を参照してください。

---

## 詳細リファレンス

- itemNameKey の自動解決ルール → **[ItemNameKey](../item/item-name-key.md)**
- ItemContainer の詳細 → **[ItemContainer](../item/item-container.md)**
- コレクション要素の場合 → **[List・Set・Map の項目名](collection-item-name.md)**
- 項目名入りメッセージ文（`ValidationMessagesWithItemNames.properties`） → **[標準アノテーションのメッセージ出力](../validator/standard-annotation-messages.md#項目名をメッセージに含める)**
