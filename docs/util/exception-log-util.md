# ExceptionLogUtil

`ExceptionLogUtil` (`jp.ecuacion.lib.core.util.ExceptionLogUtil`) is a utility class that converts exceptions into strings for logs.
It is used internally by `LogUtil` and by the error notification of `MailUtil`.
You can use it when you want to build your own log output or error mail.

Since the information about the original error is lost if logging itself fails, no exception is thrown because of the content of the passed exception or a failure in building messages.

- When `null` is passed as `throwable`, `ExceptionLogUtil.NULL_THROWABLE_MESSAGE` (`(throwable argument is null)`) is output instead of the exception information.
- `getErrLogString` and `getMessageAndStackTraceStringRecursively` treat a `null` `locale` as `Locale.ENGLISH`.

---

## getErrLogString — Obtaining an Error Log String

Returns a string that combines a separator line indicating a system error, an additional message, and the exception messages and stack traces (including causes).

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

- The exception messages are obtained with `ExceptionUtil.getMessageList(...)`, so `ViolationException` and the like are output as localized messages.
- Pass `null` when no additional message is needed.
- Even if the cause chain is circular, it does not loop infinitely.

### Shortening Packages in Stack Traces

When the area to display logs is small, you can specify the number of package levels shown in stack traces with `packagesShown`.
When it is specified, class names are not shown.

```java
ExceptionLogUtil.getErrLogString(throwable, null, Locale.ENGLISH, 1);
```

| `packagesShown` | Example |
| --- | --- |
| Not specified | `at com.example.OrderService.process(OrderService.java:7)` |
| `0` | `at ..process(OrderService.java:7)` |
| `1` | `at com...process(OrderService.java:7)` |
| `2` | `at com.example...process(OrderService.java:7)` |

---

## getMessageAndStackTraceStringRecursively — Appending to a StringBuilder

Appends the part of `getErrLogString` without the separator line and the additional message (the exception messages and stack traces)
to the specified `StringBuilder`. There is also an overload that takes `packagesShown`.

```java
StringBuilder sb = new StringBuilder();
ExceptionLogUtil.getMessageAndStackTraceStringRecursively(sb, throwable, Locale.ENGLISH);
```

---

## getMessageListStringSafely — Obtaining Messages Safely

Returns the result of `ExceptionUtil.getMessageList(throwable, locale, showsItemNameAsDefault)` as a string.
Even when building the messages itself fails (for example, because of a broken message resource), it does not throw an exception
and returns the failure and the message of the `throwable` itself.

```java
String messages = ExceptionLogUtil.getMessageListStringSafely(throwable, Locale.ENGLISH, true);
// → "[For input string: \"abc\"]"
```

`null` cannot be passed as `throwable`. When `locale` is `null`, the locale of `LocaleUtil.getFallbackLocale()` is used.
