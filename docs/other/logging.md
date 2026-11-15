# Logging

## Overview

ecuacion-lib provides dedicated loggers that wrap SLF4J.
By using these instead of SLF4J loggers directly, you can **make the purpose of logging explicit via type**.

All loggers use `LoggerFactory.getLogger(cls)` internally.

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
errorLogger.error(violationException);   // Also records ViolationException details
```

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

All loggers have the following two types of constructors:

```java
new DetailLogger(this);           // Pass an instance
new DetailLogger(getClass());     // Pass a class
```

Since they are internally converted to `LoggerFactory.getLogger(cls.getName())`,
log level control by class name is possible in logback / log4j and similar configurations.

---

## Operations Using Logger Names

Since the 4 types of loggers each use different logger names, log levels and output destinations
can be individually controlled in `logback.xml` and similar files.

```xml
<!-- Example: logback.xml -->
<logger name="jp.ecuacion.lib.core.logging.ErrorLogger" level="WARN" additivity="false">
    <appender-ref ref="ALERT_APPENDER"/>
</logger>
```
