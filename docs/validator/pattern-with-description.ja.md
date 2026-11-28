# PatternWithDescription

## 概要

標準の `@Pattern` はバリデーション失敗時に正規表現をそのままメッセージに表示するため、
エンドユーザーに見せるには不向きです。

`@PatternWithDescription` は `description` 属性に人が読める説明を記述することで、
わかりやすいエラーメッセージを出力できます。

```java
// @Pattern の場合
// → "正規表現 \"^[0-9]{7}$\" にマッチさせてください"

// @PatternWithDescription の場合
@PatternWithDescription(
    regexp = "^[0-9]{7}$",
    description = "7桁の数字"
)
private String postalCode;
// → "適切な形式（7桁の数字）で入力してください"
```

---

## ValidationMessages.properties の設定

`@PatternWithDescription` のメッセージテンプレートは `ValidationMessages.properties` で定義します。
`{description}` が `description` 属性の値に置き換えられます。

```properties
# ValidationMessages.properties
jp.ecuacion.lib.validation.constraints.PatternWithDescription.message = 「{description}」の形式で入力してください
```

`ecuacion-lib-validation` にはデフォルトのメッセージが含まれているため、
独自に定義しない場合はデフォルトが使われます。

`description` を省略した場合は、正規表現がそのままメッセージに表示されます。

---

## description のローカライズ

`description` 属性にプロパティキーを記述し、プロパティファイルで実際の説明文を定義するとローカライズできます。

```java
@PatternWithDescription(
    regexp = "^[A-Z][a-z]*$",
    description = "description.firstName"  // キーとして使用
)
private String firstName;
```

キーに対応する説明文は、パターン説明専用の `ValidationMessagesPatternDescriptions.properties` に定義します。

```properties
# ValidationMessagesPatternDescriptions.properties
description.firstName = 英語圏の名前の記述ルール（1文字目大文字・以降小文字）
```

結果：

```
「英語圏の名前の記述ルール（1文字目大文字・以降小文字）」の形式で入力してください
```

ローカライズ版は `ValidationMessagesPatternDescriptions_ja.properties` のようにサフィックスを付けます。
キーが見つからない場合は、`description` に指定した文字列がそのまま表示されます。
