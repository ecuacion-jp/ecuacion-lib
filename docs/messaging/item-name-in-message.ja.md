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

## ValidationMessagesWithItemNames.properties

項目名入りメッセージ用のプロパティファイルです。
`{0}` の位置に項目名（例: `「名前」`）が代入されます。

```properties
# ValidationMessagesWithItemNames.properties（ecuacion-lib-core のデフォルト）
jakarta.validation.constraints.NotNull.message.base = {0}にnullは許可されていません
```

ecuacion-lib-core が標準アノテーション用のデフォルトを提供しているため、
アプリ側で新たに作成する必要はありません。

このファイルが使われるかどうかは以下の優先順位で決まります。

1. `MessageParameters.isMessageWithItemName` が明示的に設定されている場合はその値
2. 未設定の場合は `ExceptionUtil.getMessageList()` の `isMessagesWithItemNamesAsDefault` フラグ

ecuacion-splib-web などのフレームワークは画面表示用メッセージに対してこのフラグを `true` に設定します。

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

## アプリ側でのメッセージ上書き

アノテーションごとにメッセージを変えたい場合は、アプリの
`ValidationMessagesWithItemNames.properties` にサフィックスなしのプレーンキーで定義します。

```properties
# ValidationMessagesWithItemNames.properties（アプリ側）
jakarta.validation.constraints.NotNull.message = {0}は必須入力です
```

`PropertiesFileUtil` のキー優先度（`key` > `key.default` > `key.base`）により、
プレーンキーが ecuacion-lib-core の `.base` デフォルトより優先されます。

---

## 詳細リファレンス

- itemNameKey の自動解決ルール → **[ItemNameKey](../item/item-name-key.md)**
- ItemContainer の詳細 → **[ItemContainer](../item/item-container.md)**
- コレクション要素の場合 → **[List・Set・Map の項目名](collection-item-name.md)**
