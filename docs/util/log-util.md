# LogUtil

`LogUtil` (`jp.ecuacion.lib.core.util.LogUtil`) is a utility class that consolidates the steps
for logging to both `ErrorLogger` and `DetailLogger` simultaneously.

---

## logSystemError

When a system error occurs, logs to both `ErrorLogger` (for monitoring alerts) and `DetailLogger` (for detailed logs)
at once.

```java
private final DetailLogger detailLog = new DetailLogger(getClass());

// Log with stack trace
LogUtil.logSystemError(detailLog, throwable);

// Log with an additional message
LogUtil.logSystemError(detailLog, throwable, "Additional info");
```

`ErrorLogger` is instantiated inside `LogUtil` and used internally.
Only the `DetailLogger` needs to be passed from the caller.

---

## Role of Each Logger

For details on each logger (`DetailLogger`, `ErrorLogger`, etc.), see
[Logging](../other/logging.md).
