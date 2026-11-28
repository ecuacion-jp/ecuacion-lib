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

### Output Example

Take the following code as an example.

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

The output looks like this (with the log pattern `%d %-5level [%logger] %msg%n`):

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

- **`ErrorLogger`** (logger name `error-logger`): outputs the exception class name and message on a single line,
  with the additional message appended at the end in `(...)`.
  It is intended for alert detection by monitoring services, so no stack trace is included.
- **`DetailLogger`** (logger name is the caller's class name): outputs the exception message and stack trace
  (including causes), then the separator line `=============== system error occurred ===============`,
  and finally the additional message as a separate line.

When no additional message is given (or it is `null` or an empty string), the trailing `(...)` in the `ErrorLogger` output
and the additional message line in the `DetailLogger` output are omitted.

So that logging does not throw an exception, `throwable` can also be `null`.
In that case, a message saying that `throwable` is `null` is output instead of the exception information.

`ErrorLogger` is instantiated inside `LogUtil` and used internally.
Only the `DetailLogger` needs to be passed from the caller.

---

## Role of Each Logger

For details on each logger (`DetailLogger`, `ErrorLogger`, etc.), see
[Logging](../other/logging.md).
