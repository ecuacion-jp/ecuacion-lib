# Logging

## Overview

ecuacion-lib provides dedicated loggers that wrap SLF4J.
By using these instead of SLF4J loggers directly, you can **make the purpose of logging explicit via type**.

Each logger uses SLF4J's `LoggerFactory.getLogger(...)` internally.
The logger name is the caller's class name for `DetailLogger`, and a fixed name for the others
(see [Operations Using Logger Names](#operations-using-logger-names)).

---

## 4 Types of Loggers

### DetailLogger — Detailed Log

A general-purpose logger for detailed logging. Supports all log levels (`trace` / `debug` / `info` / `warn` / `error`).
Used for regular application logs, including calls from within frameworks like Spring.

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

### ErrorLogger — Monitoring Alert Log

A logger for recording errors that require notification to system administrators.
Supports `error` / `warn` / `info` levels.

Intended for use in operations where monitoring services (such as Datadog) filter by this logger name to send alerts.

```java
private final ErrorLogger errorLogger = new ErrorLogger();

errorLogger.error("critical system error");

// Log an exception (class name and message on a single line, without a stack trace)
errorLogger.logSystemError(throwable);
errorLogger.logSystemError(throwable, "additional message");
```

To log a system error to both `ErrorLogger` and `DetailLogger` at once,
use `logSystemError` in [LogUtil](../util/log-util.md).

### SqlLogger — SQL Log

A logger for recording SQL statements and parameters. Supports `trace` / `debug` levels.
Since Spring has built-in SQL logging functionality, the situations where this logger is needed are limited.

```java
private final SqlLogger sqlLogger = new SqlLogger();

sqlLogger.debug("SELECT * FROM users WHERE id = ?");
```

### SummaryLogger — Batch Execution Summary Log

A logger for recording start/end times and execution result summaries for timer-triggered batch processes and similar tasks.
Supports `info` / `warn` / `error` levels.

```java
private final SummaryLogger summaryLogger = new SummaryLogger();

summaryLogger.info("batch started");
summaryLogger.info("batch completed: processed 1000 records");
```

---

## Constructors

`DetailLogger` has the following two types of constructors:

```java
new DetailLogger(this);           // Pass an instance
new DetailLogger(getClass());     // Pass a class (when used from a static method)
```

Since they are internally converted to `LoggerFactory.getLogger(cls.getName())`,
log level control by class (or package) name is possible in logback / log4j and similar configurations.

`ErrorLogger` / `SqlLogger` / `SummaryLogger` have only a no-argument constructor,
and their logger names are fixed regardless of the caller.

```java
new ErrorLogger();
new SqlLogger();
new SummaryLogger();
```

---

## Operations Using Logger Names

The 4 types of loggers use the following logger names:

| Logger | Logger name |
|---|---|
| `DetailLogger` | Fully qualified name of the caller class (e.g. `com.example.OrderService`) |
| `ErrorLogger` | `error-logger` |
| `SqlLogger` | `sql-logger` |
| `SummaryLogger` | `summary-logger` |

Therefore, log levels and output destinations can be individually controlled in `logback.xml` and similar files.

```xml
<!-- Example: logback.xml -->
<logger name="error-logger" level="WARN" additivity="false">
    <appender-ref ref="ALERT_APPENDER"/>
</logger>
```
