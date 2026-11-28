# DateTimeApiUtil

`DateTimeApiUtil`（`jp.ecuacion.lib.core.util.DateTimeApiUtil`）は Java 標準の `java.time` API
（Date and Time API）に関するユーティリティクラスです。
日時の表示文字列への変換と、文字列から日時オブジェクトへのパースを提供します。

---

## 表示文字列への変換

### LocalDateTime → 表示文字列（`yyyy-MM-dd HH:mm:ss`）

```java
LocalDateTime ldt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

DateTimeApiUtil.getLocalDateTimeDisplayString(ldt);
// → "2024-01-15 10:30:00"
```

### OffsetDateTime → LocalDateTime 表示文字列（タイムゾーン変換あり）

```java
OffsetDateTime odt = OffsetDateTime.parse("2024-01-15T01:30:00+00:00");

// 指定タイムゾーンに変換して表示
DateTimeApiUtil.getLocalDateTimeDisplayString(odt, ZoneId.of("Asia/Tokyo"));
// → "2024-01-15 10:30:00"

// null を渡すと ZoneId.systemDefault() を使用
DateTimeApiUtil.getLocalDateTimeDisplayString(odt, null);
```

`ZoneOffset` は `ZoneId` を継承しているため、そのまま渡すことができます。

### OffsetDateTime → タイムゾーン付き表示文字列（`yyyy-MM-dd HH:mm:ss +HH:mm`）

```java
DateTimeApiUtil.getOffsetDateTimeDisplayString(odt, ZoneId.of("Asia/Tokyo"));
// → "2024-01-15 10:30:00 +09:00"
```

### LocalDateTime → ファイル名用タイムスタンプ（`yyyy-MM-dd-HH-mm-ss-SSSSSS`）

```java
DateTimeApiUtil.getTimestampStringForFilename(LocalDateTime.now());
// → "2024-01-15-10-30-00-123456"
```

ファイル名に使用できない文字（`:` など）を含まない形式です。

---

## 文字列からのパース

### 文字列 → LocalDateTime

```java
DateTimeApiUtil.getLocalDateTime("2024-01-15 10:30:00");
DateTimeApiUtil.getLocalDateTime("2024/01/15 10:30:00");
DateTimeApiUtil.getLocalDateTime("2024-01-15T10:30:00");
DateTimeApiUtil.getLocalDateTime("2024-01-15 10:30:00.123");
```

対応するフォーマット：

| フォーマット | 例 |
| --- | --- |
| `yyyy-MM-dd HH:mm:ss` | `2024-01-15 10:30:00` |
| `yyyy/MM/dd HH:mm:ss` | `2024/01/15 10:30:00` |
| `yyyy-MM-ddTHH:mm:ss` | `2024-01-15T10:30:00` |
| 上記 + 小数秒 | `2024-01-15 10:30:00.123` |

年は4桁、月・日・時・分・秒はすべて2桁（ゼロパディング）が必須です。

### 文字列 → OffsetDateTime

```java
DateTimeApiUtil.getOffsetDateTime("2024-01-15 10:30:00+09:00");
DateTimeApiUtil.getOffsetDateTime("2024-01-15 10:30:00 +09:00");
DateTimeApiUtil.getOffsetDateTime("2024-01-15 10:30:00-05:00");
DateTimeApiUtil.getOffsetDateTime("2024-01-15T01:30:00Z");
```

LocalDateTime のすべての対応フォーマットに、オフセット部分を付加した形式に対応しています。オフセットの前にスペースがある形式も許容します。
オフセットは `+09:00` のようなプラス、`-05:00` のようなマイナス、UTC を表す `Z` のいずれも使えます。
