# ValidationMessages

## 概要

`ValidationMessages` 系の3ファイルは、Jakarta Validation のバリデーションメッセージを扱うためのファイルです。
主に ecuacion フレームワーク内部から自動的に呼び出されます。

`getMessage()` などと異なり、内部的には `Map<String, Object>` を引数として使います。
これは Jakarta Validation のアノテーション属性が `{value}`, `{min}`, `{max}` のような名前付きプレースホルダーを使用するためです。

| ファイル名 | メソッド | フォールバック先 |
| --- | --- | --- |
| `ValidationMessages[_xxx].properties` | `getValidationMessage(...)` | なし |
| `ValidationMessagesWithItemNames[_xxx].properties` | `getValidationMessageWithItemName(...)` | `ValidationMessages` |
| `ValidationMessagesPatternDescriptions[_xxx].properties` | `getValidationMessagePatternDescription(...)` | なし |

基本的には Jakarta Validation の処理により自動でメッセージ生成されるため、アプリ開発者が明示的にこれらのメッセージファイルからメッセージを取得することはありません。
アプリ開発者が直接関わるのは以下の場面です。

- **既存バリデーションメッセージを上書きする場合** — アプリ側のファイルに `.default` なしのキーを定義する
- **`@PatternWithDescription` を使用する場合** — `ValidationMessagesPatternDescriptions.properties` にパターン説明文を定義する

---

## ValidationMessages.properties

バリデーション違反時のメッセージを格納します。キーは通常アノテーションの完全修飾クラス名 + `.message` です。

```properties
# ValidationMessages.properties
jp.ecuacion.lib.core.jakartavalidation.constraint.SomeAnnotation.message=\
    {0} は {min} 文字以上 {max} 文字以下で入力してください。
```

`{min}` や `{max}` はアノテーション属性のプレースホルダーです（`{0}` 形式ではなく名前付き）。

---

## ValidationMessagesWithItemNames.properties

`{0}` が項目名プレースホルダーになっているバリデーションメッセージを格納します。
`ValidationMessages.properties` との違いは、メッセージに `{0}` が含まれる点です。
フレームワークが `{0}` を実際の項目名に置換した上でユーザーに表示します。

```properties
# ValidationMessagesWithItemNames.properties
jp.ecuacion.lib.core.jakartavalidation.constraint.SomeAnnotation.message=\
    {0} は {min} 文字以上 {max} 文字以下で入力してください。
```

---

## ValidationMessagesPatternDescriptions.properties

`@PatternWithDescription` などで使用する、正規表現のエンドユーザー向け説明文を格納します。
キーが存在しない場合は `ValidationMessages.properties` にフォールバックします。

```properties
# ValidationMessagesPatternDescriptions.properties
pattern.phone-number=電話番号形式（例：090-1234-5678）
pattern.postal-code=郵便番号形式（例：123-4567）
```

---

## EL 式のサポート

プロパティ値の中で `${...}` を使った EL（Expression Language）式が利用できます。
ValidationMessages は `{min}`, `{max}` などのアノテーション属性を EL 変数として扱えるため、
属性値を使った計算を記述できます。

```properties
# ValidationMessages.properties
# min と max の平均値を計算して埋め込む例
jp.ecuacion.example.constraint.SomeAnnotation.message=\
    推奨値は ${(min + max) / 2} です（{min}〜{max}）。
```

`{min}`, `{max}` は名前付きプレースホルダー（Map 経由で解決）、
`${...}` は EL 式（アノテーション属性値を変数として評価）です。

`${...}` の EL 式評価自体が行われるのは `ValidationMessages`、`ValidationMessagesWithItemNames`、
`ValidationMessagesPatternDescriptions` のみです。それ以外のファイル種別（`application`、`messages`、
`item_names` など）では `${...}` は評価されずそのまま残ります。EL 変数（アノテーション属性）は
Jakarta Bean Validation のメッセージ補間においてのみ意味を持つためです。
