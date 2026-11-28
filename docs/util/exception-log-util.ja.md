# ExceptionLogUtil

`ExceptionLogUtil`（`jp.ecuacion.lib.core.util.ExceptionLogUtil`）は、例外をログ出力用の文字列に変換するユーティリティクラスです。
`LogUtil` や `MailUtil` のエラー通知が内部で使っています。
ログ出力やエラーメールを独自に組み立てたい場合に使えます。

ログ出力そのものが失敗すると元のエラーの情報が失われるため、渡された例外の内容やメッセージの組み立ての失敗によって例外がスローされることはありません。

- `throwable` に `null` を渡すと、例外情報の代わりに `ExceptionLogUtil.NULL_THROWABLE_MESSAGE`（`(throwable argument is null)`）を出力します。
- `getErrLogString` と `getMessageAndStackTraceStringRecursively` は、`locale` に `null` を渡すと `Locale.ENGLISH` として扱います。

---

## getErrLogString — エラーログ文字列の取得

システムエラーを表す区切り行、追加メッセージ、例外メッセージとスタックトレース（cause を含む）をまとめた文字列を返します。

```java
String log = ExceptionLogUtil.getErrLogString(throwable, "orderId=12345", Locale.ENGLISH);
```

```text
=============== system error occurred ===============
orderId=12345


java.lang.NumberFormatException[For input string: "abc"]
	at java.lang.NumberFormatException.forInputString(NumberFormatException.java:67)
	at java.lang.Integer.parseInt(Integer.java:565)
	...
```

- 例外メッセージは `ExceptionUtil.getMessageList(...)` で取得したもので、`ViolationException` などはローカライズされたメッセージになります。
- 追加メッセージが不要な場合は `null` を渡します。
- cause が循環していても、無限ループにはなりません。

### スタックトレースのパッケージ表示を短くする

ログを表示する領域が狭い場合は、`packagesShown` でスタックトレースに表示するパッケージの階層数を指定できます。
指定した場合、クラス名は表示されません。

```java
ExceptionLogUtil.getErrLogString(throwable, null, Locale.ENGLISH, 1);
```

| `packagesShown` | 表示例 |
| --- | --- |
| 指定なし | `at com.example.OrderService.process(OrderService.java:7)` |
| `0` | `at ..process(OrderService.java:7)` |
| `1` | `at com...process(OrderService.java:7)` |
| `2` | `at com.example...process(OrderService.java:7)` |

---

## getMessageAndStackTraceStringRecursively — StringBuilder への追記

`getErrLogString` の区切り行と追加メッセージを除いた部分（例外メッセージとスタックトレース）を、
指定した `StringBuilder` に追記します。`packagesShown` を指定するオーバーロードもあります。

```java
StringBuilder sb = new StringBuilder();
ExceptionLogUtil.getMessageAndStackTraceStringRecursively(sb, throwable, Locale.ENGLISH);
```

---

## getMessageListStringSafely — メッセージの安全な取得

`ExceptionUtil.getMessageList(throwable, locale, showsItemNameAsDefault)` の結果を文字列で返します。
メッセージの組み立て自体が失敗した場合（メッセージリソースの不備など）も例外をスローせず、
失敗の内容と `throwable` 自身のメッセージを返します。

```java
String messages = ExceptionLogUtil.getMessageListStringSafely(throwable, Locale.ENGLISH, true);
// → "[For input string: \"abc\"]"
```

`throwable` に `null` は渡せません。`locale` が `null` の場合は `LocaleUtil.getFallbackLocale()` のロケールが使われます。
