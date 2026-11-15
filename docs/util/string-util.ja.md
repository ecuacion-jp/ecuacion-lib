# StringUtil

`StringUtil`（`jp.ecuacion.lib.core.util.StringUtil`）は文字列操作のユーティリティクラスです。
Apache Commons Lang の `StringUtils` が提供するメソッドとは重複しないものを実装しています。
`StringUtils` にない操作が必要な場合はこちらを使用してください。

---

## ケース変換

### snake_case → lowerCamelCase / UpperCamelCase

```java
StringUtil.getLowerCamelFromSnake("user_name");              // "userName"
StringUtil.getLowerCamelFromSnake("VALIDATION_MESSAGES_JA"); // "validationMessagesJa"
StringUtil.getUpperCamelFromSnake("user_name");              // "UserName"
```

`getLowerCamelFromSnake` は以下の入力形式に対応します。

| 入力例 | 出力例 |
| --- | --- |
| `user_name` | `userName` |
| `VALIDATION_MESSAGES_JA` | `validationMessagesJa` |
| `ValidationMessages_ja` | `validationMessagesJa` |

先頭または末尾に `_` がある文字列、`__`（連続アンダースコア）を含む文字列は
`RuntimeException` をスローします。

### camelCase → lower_snake_case

```java
StringUtil.getLowerSnakeFromCamel("userName"); // "user_name"
StringUtil.getLowerSnakeFromCamel("UserName"); // "user_name"
```

---

## 数値フォーマット

```java
StringUtil.toCurrencyFormat("1234567"); // "1,234,567"
```

---

## 区切り文字列の生成

複数の文字列を区切り文字で結合します。

```java
List<String> items = List.of("apple", "banana", "cherry");

// 区切り文字のみ
StringUtil.getSeparatedValuesString(items, ", ");
// → "apple, banana, cherry"

// 各要素を囲む文字を指定
StringUtil.getSeparatedValuesString(items, ", ", "'");
// → "'apple', 'banana', 'cherry'"

// 左右で異なる囲み文字
StringUtil.getSeparatedValuesString(items, ", ", "[", "]");
// → "[apple], [banana], [cherry]"
```

配列（`String[]`）を引数に取るオーバーロードも同様のシグネチャで使用できます。

### CSV

```java
StringUtil.getCsv("a", "b", "c");          // "a,b,c"
StringUtil.getCsvWithSpace("a", "b", "c"); // "a, b, c"
```

`getCsvWithSpace` はログ出力やコード生成など、読みやすさが重要な場面向けです。

---

## null / 空文字判定

```java
StringUtil.isObjectNullOrEmpty(null);   // true
StringUtil.isObjectNullOrEmpty("");     // true
StringUtil.isObjectNullOrEmpty("abc"); // false
StringUtil.isObjectNullOrEmpty(123);   // false（String 以外の型は常に false）
```

`Object` 型の値が `null` または空文字列かを確認します。
リフレクションで取得したフィールド値の判定など、型が `Object` の場合に使います。
