# DateTimeApiUtil

`DateTimeApiUtil` (`jp.ecuacion.lib.core.util.DateTimeApiUtil`) is a utility class for Java's standard
`java.time` API (Date and Time API).
It provides conversion of date/time values to display strings and parsing of strings to date/time objects.

---

## Converting to Display Strings

### LocalDateTime → Display String (`yyyy-MM-dd HH:mm:ss`)

```java
LocalDateTime ldt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

DateTimeApiUtil.getLocalDateTimeDisplayString(ldt);
// → "2024-01-15 10:30:00"
```

### OffsetDateTime → LocalDateTime Display String (with timezone conversion)

```java
OffsetDateTime odt = OffsetDateTime.parse("2024-01-15T01:30:00+00:00");

// Convert to specified timezone and display
DateTimeApiUtil.getLocalDateTimeDisplayString(odt, ZoneId.of("Asia/Tokyo"));
// → "2024-01-15 10:30:00"

// Pass null to use ZoneId.systemDefault()
DateTimeApiUtil.getLocalDateTimeDisplayString(odt, null);
```

`ZoneOffset` extends `ZoneId`, so it can be passed directly.

### OffsetDateTime → Display String with Timezone (`yyyy-MM-dd HH:mm:ss +HH:mm`)

```java
DateTimeApiUtil.getOffsetDateTimeDisplayString(odt, ZoneId.of("Asia/Tokyo"));
// → "2024-01-15 10:30:00 +09:00"
```

### LocalDateTime → Timestamp for Filename (`yyyy-MM-dd-HH-mm-ss-SSSSSS`)

```java
DateTimeApiUtil.getTimestampStringForFilename(LocalDateTime.now());
// → "2024-01-15-10-30-00-123456"
```

This format does not contain characters that cannot be used in file names (such as `:`).

---

## Parsing from Strings

### String → LocalDateTime

```java
DateTimeApiUtil.getLocalDateTime("2024-01-15 10:30:00");
DateTimeApiUtil.getLocalDateTime("2024/01/15 10:30:00");
DateTimeApiUtil.getLocalDateTime("2024-01-15T10:30:00");
DateTimeApiUtil.getLocalDateTime("2024-01-15 10:30:00.123");
```

Supported formats:

| Format | Example |
| --- | --- |
| `yyyy-MM-dd HH:mm:ss` | `2024-01-15 10:30:00` |
| `yyyy/MM/dd HH:mm:ss` | `2024/01/15 10:30:00` |
| `yyyy-MM-ddTHH:mm:ss` | `2024-01-15T10:30:00` |
| Above + fractional seconds | `2024-01-15 10:30:00.123` |

Year must be 4 digits; month, day, hour, minute, and second must all be 2 digits (zero-padded).

### String → OffsetDateTime

```java
DateTimeApiUtil.getOffsetDateTime("2024-01-15 10:30:00+09:00");
DateTimeApiUtil.getOffsetDateTime("2024-01-15 10:30:00 +09:00");
```

Supports all LocalDateTime formats with an offset part (e.g., `+09:00`) appended.
Forms with a space before the offset are also accepted.
