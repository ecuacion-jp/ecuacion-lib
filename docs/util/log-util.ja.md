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

`ErrorLogger` は `LogUtil` 内部でインスタンスを生成して使用します。
呼び出し元から渡す必要があるのは `DetailLogger` のみです。

---

## 各ロガーの役割

各ロガー（`DetailLogger`, `ErrorLogger` 等）の詳細は
[Logging](../other/logging.md) を参照してください。
