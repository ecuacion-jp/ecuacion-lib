# クロスファイル参照

## 概要

`messages.properties` などのプロパティ値の中に `#{...}` 構文を使って他のプロパティファイルのキーを参照できます。
これにより、エラーメッセージの中で項目名を再利用したり、共通文字列を一元管理したりすることが簡単にできます。

---

## `#{fileKind:key}` 構文

プロパティ値の中に `#{fileKind:key}` と書くと、指定したファイル種別のキーに展開されます。

```properties
# messages.properties
error.name.required=#{item_names:user.name} は必須です。

# item_names.properties
user.name=氏名
```

`getMessage(locale, "error.name.required")` を呼ぶと、`item_names.properties` の `user.name` の値が埋め込まれます。

```text
氏名 は必須です。
```

利用可能なファイル種別は `PropertiesFileUtilFileKindEnum` の全値です。

| fileKind | 対象ファイル |
| --- | --- |
| `messages` | messages[_xxx].properties |
| `messages_with_item_names` | messages_with_item_names[_xxx].properties |
| `item_names` | item_names[_xxx].properties |
| `enum_names` | enum_names[_xxx].properties |
| `constants` | constants[_xxx].properties |
| `ValidationMessages` | ValidationMessages[_xxx].properties |
| `ValidationMessagesWithItemNames` | ValidationMessagesWithItemNames[_xxx].properties |
| `ValidationMessagesPatternDescriptions` | ValidationMessagesPatternDescriptions[_xxx].properties |

---

## `#{key}` 構文（ファイル種別省略）

ファイル種別を省略した `#{key}` 構文を使うと、`messages` → `item_names` → `enum_names` → `constants` の順に自動で検索されます。

```properties
# messages.properties
error.name.required=#{user.name} は必須です。
```

`user.name` というキーが `item_names.properties` に定義されていれば、自動的にそこから値が取得されます。

---

## 再帰的な解決

`#{...}` 構文は再帰的に解決されます。

```properties
# messages.properties
greeting=こんにちは、#{messages:user.title}-#{messages:user.name} さん。
user.title=田中
user.name=太郎
```

`getMessage(locale, "greeting")` の結果：

```text
こんにちは、田中-太郎 さん。
```

さらに深い参照も可能です。

```properties
full.message=#{messages:part1}-#{messages:part2}
part1=a
part2=b-#{messages:part3}
part3=c
```

`getMessage(locale, "full.message")` の結果：`a-b-c`

---

## 実用例：バリデーションエラーメッセージで項目名を再利用する

フォームのバリデーションエラーメッセージで、`item_names.properties` に定義した項目名を参照する典型的な使い方です。

```properties
# item_names.properties
user.name=氏名
user.email=メールアドレス
user.age=年齢

# messages.properties
error.required=#{item_names:{0}} は必須入力です。
error.invalid.email=#{item_names:{0}} の形式が正しくありません。
```

このように定義することで、項目名を一箇所で管理しつつ、複数のエラーメッセージから参照できます。
