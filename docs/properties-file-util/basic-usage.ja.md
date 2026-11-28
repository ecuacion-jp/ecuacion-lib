# 基本的な使い方

## PropertiesFileUtil とは

`PropertiesFileUtil`（`jp.ecuacion.lib.core.util.PropertiesFileUtil`）は、アプリケーション内の各種
`.properties` ファイルを一元的に読み込むためのユーティリティクラスです。

Java 標準の `ResourceBundle` をベースに、各種機能を追加しています。（詳細は後述）

---

## サポートするファイルの種類

| ファイル名 | 取得メソッド | Locale | 説明 |
| --- | --- | :---: | --- |
| `application[_xxx].properties` | `getApplication(...)` | | アプリ設定値 |
| `constants[_xxx].properties` | `getConstant(...)` | | 非ローカライズ文字列（定数） |
| `messages[_xxx].properties` | `getMessage(...)` | ✓ | ローカライズ済みメッセージ |
| `messages_with_item_names[_xxx].properties` | `getMessageWithItemName(...)` | ✓ | 項目名を含むメッセージ |
| `item_names[_xxx].properties` | `getItemName(...)` | ✓ | 項目名 |
| `enum_names[_xxx].properties` | `getEnumName(...)` | ✓ | Enum 値の表示名 |
| `ValidationMessages[_xxx].properties` | `getValidationMessage(...)` | ✓ | バリデーションメッセージ |
| `ValidationMessagesWithItemNames[_xxx].properties` | `getValidationMessageWithItemName(...)` | ✓ | 項目名付きバリデーションメッセージ |
| `ValidationMessagesPatternDescriptions[_xxx].properties` | `getValidationMessagePatternDescription(...)` | ✓ | パターン説明文 |

`ValidationMessages` 系の3種類は引数の形式が他と異なります。
詳細は [ValidationMessages](validation-messages.md) を参照してください。

Locale 列に ✓ があるメソッドは、Locale 引数を省略するか `null` を渡した場合、`Locale.ROOT` として扱われます（`messages.properties` のようなロケールなしのファイルから取得します）。
ただし、引数の数値・日付などのフォーマットには `LocaleUtil.getFallbackLocale()` のロケールが使われます。

`hasXxx(...)` によるキーの存在確認は、ロケールなしのファイルに対して行います。
ロケール付きのファイル（`messages_ja.properties` など）は、ロケールなしのファイルと同じキーを持つ前提です。

キーに空文字を渡すと、`getXxx(...)` は `IllegalArgumentException` をスローし、`hasXxx(...)` は `false` を返します。

---

## application.properties を読む

```java
String value = PropertiesFileUtil.getApplication("app.title");

// キーの存在確認
boolean exists = PropertiesFileUtil.hasApplication("app.title");

// デフォルト値付きで取得（キーがなければ第二引数を返す）
String value = PropertiesFileUtil.getApplicationOrElse("app.optional-key", "default-value");
```

---

## constants.properties を読む

ロケール対応しない固定文字列（URL プレフィックス、コードスニペット、共通定数など）を格納するファイルです。

```java
String value = PropertiesFileUtil.getConstant("common.app-name");
boolean exists = PropertiesFileUtil.hasConstant("common.app-name");
```

---

## messages.properties を読む

`messages.properties` はロケール対応のメッセージファイルです。

```java
// ロケールを指定して取得
String msg = PropertiesFileUtil.getMessage(Locale.JAPANESE, "error.required");

// ロケール指定なし（Locale.ROOT を使用）
String msg = PropertiesFileUtil.getMessage("error.required");

// キーの存在確認
boolean exists = PropertiesFileUtil.hasMessage("error.required");
```

---

## messages_with_item_names.properties を読む

`{item_name}` が項目名プレースホルダーになっているメッセージを格納します。
`messages.properties` との違いは、メッセージに `{item_name}` が含まれる点です。
フレームワークが `{item_name}` を実際の項目名に置換した上でユーザーに表示します。

キーが存在しない場合は `messages.properties` へ自動的にフォールバックします。

```java
// ロケール指定
String msg = PropertiesFileUtil.getMessageWithItemName(Locale.JAPANESE, "error.required");

// ロケール指定なし（Locale.ROOT を使用）
String msg = PropertiesFileUtil.getMessageWithItemName("error.required");

boolean exists = PropertiesFileUtil.hasMessageWithItemName("error.required");
```

### messages.properties との使い分け

`messages_with_item_names.properties` にフォールバックするため、ファイルを別途作成しなくても機能します。
`getMessage(...)` と `getMessageWithItemName(...)` を同じキーで呼んでも、
`messages.properties` しか存在しない場合は同じ値が返ります。

別途作成する典型的なケースは、Web 画面でエラーを2箇所に同時表示する場合です。

- **エラーメッセージ一覧**（画面上部など）→ 項目名あり：「氏名は必須です」
- **各項目の横・下** → 項目名なし：「必須です」

このとき `messages.properties` に項目名なし文言、`messages_with_item_names.properties` に項目名あり文言を定義することで、同じキーで両方の表示を使い分けられます。

---

## item_names.properties を読む

画面上の項目名（フィールドのラベル）のローカライズ済み表示名を格納するファイルです。
バリデーションエラーメッセージなどで「`{0}` は必須です」の `{0}` 部分に使われます。

キーが存在しない場合は `messages.properties` にフォールバックします。
このフォールバックにより、`item_names.properties` を作成せずに `messages.properties` だけで
`getItemName(...)` を動作させることができます。

```java
// ロケール指定
String itemName = PropertiesFileUtil.getItemName(Locale.JAPANESE, "user.name");

// ロケール指定なし（Locale.ROOT を使用）
String itemName = PropertiesFileUtil.getItemName("user.name");

boolean exists = PropertiesFileUtil.hasItemName("user.name");
```

---

## enum_names.properties を読む

Enum 値のローカライズ済み表示名を格納するファイルです。
例えば `Status.ACTIVE` というキーに対して「有効」などの表示名を定義します。

キーが存在しない場合は `messages.properties` にフォールバックします。

```java
// ロケール指定
String enumName = PropertiesFileUtil.getEnumName(Locale.JAPANESE, "Status.ACTIVE");

// ロケール指定なし（Locale.ROOT を使用）
String enumName = PropertiesFileUtil.getEnumName("Status.ACTIVE");

boolean exists = PropertiesFileUtil.hasEnumName("Status.ACTIVE");
```
