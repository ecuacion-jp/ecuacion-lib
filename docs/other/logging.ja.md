# Logging

## 概要

ecuacion-lib は SLF4J をラップした専用ロガーを提供します。
SLF4J ロガーをそのまま使う代わりにこれらを使うことで、**ログの用途を型で明示**できます。

各ロガーは内部で SLF4J の `LoggerFactory.getLogger(...)` を使います。
ロガー名は、`DetailLogger` は呼び出し元クラス名、それ以外は固定名です（[ロガー名による運用](#ロガー名による運用) 参照）。

---

## 4種類のロガー

### DetailLogger — 詳細ログ

汎用の詳細ログ用ロガーです。全ログレベル（`trace` / `debug` / `info` / `warn` / `error`）をサポートします。
Spring などフレームワーク内部からの呼び出しも含む、通常のアプリログに使用します。

```java
private final DetailLogger logger = new DetailLogger(getClass());

logger.trace("detailed trace message");
logger.debug("debug info");
logger.info("processing started");
logger.warn("unexpected state");
logger.error("an error occurred");
logger.error(throwable);
logger.error(throwable, "additional message");
```

### ErrorLogger — 監視アラート用ログ

システム管理者への通知が必要なエラーを記録するロガーです。
`error` / `warn` / `info` レベルをサポートします。

監視サービス（Datadog など）がこのロガー名でフィルタリングしてアラートを飛ばす運用を想定しています。

```java
private final ErrorLogger errorLogger = new ErrorLogger();

errorLogger.error("critical system error");

// 例外を記録（例外クラス名とメッセージを1行で出力。スタックトレースは含まない）
errorLogger.logSystemError(throwable);
errorLogger.logSystemError(throwable, "additional message");
```

システムエラー発生時に `ErrorLogger` と `DetailLogger` の両方へ出力したい場合は
[LogUtil](../util/log-util.md) の `logSystemError` を使うと便利です。

### SqlLogger — SQL ログ

SQL 文とパラメータを記録するロガーです。`trace` / `debug` レベルをサポートします。
Spring はデフォルトで SQL ログ機能を持っているため、このロガーが必要になる場面は限定的です。

```java
private final SqlLogger sqlLogger = new SqlLogger();

sqlLogger.debug("SELECT * FROM users WHERE id = ?");
```

### SummaryLogger — バッチ実行サマリーログ

タイマー起動のバッチ処理などで、開始・終了時刻や実行結果の概要を記録するロガーです。
`info` / `warn` / `error` レベルをサポートします。

```java
private final SummaryLogger summaryLogger = new SummaryLogger();

summaryLogger.info("batch started");
summaryLogger.info("batch completed: processed 1000 records");
```

---

## コンストラクタ

`DetailLogger` は以下の2種類のコンストラクタを持ちます。

```java
new DetailLogger(this);           // インスタンスを渡す
new DetailLogger(getClass());     // クラスを渡す（static メソッドから使う場合）
```

内部では `LoggerFactory.getLogger(cls.getName())` に変換されるため、
logback / log4j 等の設定でクラス名（パッケージ名）によるログレベル制御が可能です。

`ErrorLogger` / `SqlLogger` / `SummaryLogger` は引数なしのコンストラクタのみを持ち、
ロガー名は呼び出し元に関係なく固定です。

```java
new ErrorLogger();
new SqlLogger();
new SummaryLogger();
```

---

## ロガー名による運用

4種類のロガーはそれぞれ以下のロガー名を使います。

| ロガー | ロガー名 |
|---|---|
| `DetailLogger` | 呼び出し元クラスの完全修飾名（例: `com.example.OrderService`） |
| `ErrorLogger` | `error-logger` |
| `SqlLogger` | `sql-logger` |
| `SummaryLogger` | `summary-logger` |

このため、`logback.xml` 等でログレベルや出力先を個別に制御できます。

```xml
<!-- 例: logback.xml -->
<logger name="error-logger" level="WARN" additivity="false">
    <appender-ref ref="ALERT_APPENDER"/>
</logger>
```
