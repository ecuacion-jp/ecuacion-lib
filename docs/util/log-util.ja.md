# LogUtil

`LogUtil`（`jp.ecuacion.lib.core.util.LogUtil`）は、`ErrorLogger` と `DetailLogger` の両方に同時にログ出力する手順をまとめたユーティリティクラスです。

---

## logSystemError

システムエラーが発生した際に、`ErrorLogger`（監視アラート用）と `DetailLogger`（詳細ログ）の両方へ一度にログ出力します。

```java
private final DetailLogger detailLog = new DetailLogger(getClass());

// スタックトレース付きでログ出力
LogUtil.logSystemError(detailLog, throwable);

// 追加メッセージ付きでログ出力
LogUtil.logSystemError(detailLog, throwable, "追加情報");
```

### 出力例

以下のコードを実行した場合を例にします。

```java
package com.example;

public class OrderService {
  private final DetailLogger detailLog = new DetailLogger(getClass());

  void process() {
    try {
      Integer.parseInt("abc");
    } catch (Exception e) {
      LogUtil.logSystemError(detailLog, e, "orderId=12345");
    }
  }
}
```

出力は次のようになります（ログ出力パターンを `%d %-5level [%logger] %msg%n` とした場合）。

```text
2026-11-28 11:48:55.183 ERROR [error-logger] A system error has occurred: java.lang.NumberFormatException - [For input string: "abc"] (orderId=12345)
2026-11-28 11:48:55.187 ERROR [com.example.OrderService] java.lang.NumberFormatException[For input string: "abc"]
	at java.lang.NumberFormatException.forInputString(NumberFormatException.java:67)
	at java.lang.Integer.parseInt(Integer.java:565)
	at java.lang.Integer.parseInt(Integer.java:662)
	at com.example.OrderService.process(OrderService.java:7)
	at com.example.OrderService.main(OrderService.java:10)

2026-11-28 11:48:55.194 ERROR [com.example.OrderService] =============== system error occurred ===============
2026-11-28 11:48:55.194 ERROR [com.example.OrderService] orderId=12345
```

- **`ErrorLogger`**（ロガー名 `error-logger`）：例外クラス名とメッセージを1行にまとめて出力し、追加メッセージは末尾に `(...)` で付加されます。
  監視サービスでのアラート検知を想定し、スタックトレースは含みません。
- **`DetailLogger`**（ロガー名は呼び出し元クラス名）：例外メッセージとスタックトレース（cause を含む）を出力した後、
  区切り行 `=============== system error occurred ===============` を出力し、最後に追加メッセージを独立した1行として出力します。

追加メッセージを指定しない場合（または `null`・空文字の場合）は、`ErrorLogger` の末尾の `(...)` と
`DetailLogger` の追加メッセージ行が出力されなくなります。

ログ出力で例外が発生しないよう、`throwable` には `null` も渡せます。
その場合は例外情報の代わりに、`throwable` が `null` である旨のメッセージを出力します。

`ErrorLogger` は `LogUtil` 内部でインスタンスを生成して使用します。
呼び出し元から渡す必要があるのは `DetailLogger` のみです。

---

## 各ロガーの役割

各ロガー（`DetailLogger`, `ErrorLogger` 等）の詳細は
[Logging](../other/logging.md) を参照してください。
