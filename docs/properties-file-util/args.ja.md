# Args

## 概要

`messages.properties` などの値に `{0}`, `{1}` などのプレースホルダーを使うことで、実行時に動的な値を埋め込めます。

引数は `Object...` 形式で渡します。`Arg` クラスを使うと、引数自体をプロパティキーとして指定するなどの高度な制御ができます。

---

## Object... で直接渡す

最もシンプルな方法です。文字列・数値・日付など任意の型を渡せます。

フォーマットには Java 標準の `MessageFormat` が使われるため、
`{0,number,#,###}` のような型指定パターンもそのまま使えます。

```properties
# messages.properties
error.range={0} は {1} 以上 {2} 以下で入力してください。
amount.label=合計: {0,number,#,###} 円
```

```java
// 文字列を渡す
String msg = PropertiesFileUtil.getMessage(
    Locale.JAPANESE, "error.range", "年齢", "0", "150");
// => "年齢 は 0 以上 150 以下で入力してください。"

// 数値をそのまま渡す → MessageFormat の型aware フォーマットが効く
String msg = PropertiesFileUtil.getMessage(Locale.JAPANESE, "amount.label", 1234567);
// => "合計: 1,234,567 円"
```

`Object...` なので、`String` も `Integer` も `Date` もそのまま渡せます。

---

## Arg を使う

`Arg` クラスを使うと、引数自体をプロパティキーとして解決したり、Arg の中でさらにフォーマットしたりできます。

### Arg.message(String) — メッセージ ID を引数として渡す

引数自体を `messages.properties` のキーとして解決します。

```properties
# messages.properties
greeting=こんにちは、{0} さん。
role.admin=管理者
```

```java
getMessage(locale, "greeting", Arg.message("role.admin"));
// => "こんにちは、管理者 さん。"
```

引数自体をロケールに応じて切り替えたい場合に便利です。

### Arg.message(String, Object...) — 引数付きメッセージ ID

```properties
# messages.properties
item.range.desc={0}（{1}〜{2}）
field.age=年齢
```

```java
getMessage(locale, "...", Arg.message("item.range.desc", "field.age", "0", "150"));
```

### Arg.formattedString(String, Object...) — Arg の中でフォーマット

`#{key}` でプロパティキーを埋め込めるため、ベタ文字列を書かずにローカライズされた文字列を組み立てられます。

```properties
# messages.properties
required=（必須）
```

```java
Arg.formattedString("{0} #{required}", Arg.message("field.name"))
// => "名前 （必須）"
```

### Arg.itemName(String) / Arg.constant(String) / Arg.enumName(String) — 他ファイル種別から参照

`messages.properties` 以外のファイル種別のキーを参照したい場合に使います。

```java
// item_names.properties のキーを引数として使う
getMessage(locale, "greeting", Arg.itemName("user.name"));
// => "こんにちは、名前 さん。"

// constants.properties のキーを引数として使う
getMessage(locale, "message", Arg.constant("max.file.size"));

// enum_names.properties のキーを引数として使う
getMessage(locale, "message", Arg.enumName("status.active"));
```

---

## Arg と Object を混在させる

`Object...` パラメータでは `Arg` と plain Object を自由に混在できます。
`Arg` はメッセージ ID として解決され、それ以外の `Object` はそのまま `MessageFormat` に渡されます。

```java
// Arg.message と数値の混在
getMessage(locale, "message",
    Arg.message("role.admin"),  // → "管理者"（messages.properties から解決）
    1234567);                   // → 1,234,567（型aware フォーマット）
```
