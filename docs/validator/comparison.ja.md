# 比較バリデーター

## 概要

比較系アノテーションは、2つのフィールドの大小関係を検証します。クラスレベルのアノテーションです。

```java
@LessThan(
    propertyPath = "startDate",
    baselinePropertyPath = "endDate"
)
public class PeriodForm { ... }
```

どちらかのフィールドが null / empty のときは検証をスキップします。

---

## アノテーション一覧

| アノテーション | 関係 |
| ------------- | ---- |
| `@GreaterThan` | `propertyPath` > `baselinePropertyPath` |
| `@GreaterThanOrEqualTo` | `propertyPath` >= `baselinePropertyPath` |
| `@LessThan` | `propertyPath` < `baselinePropertyPath` |
| `@LessThanOrEqualTo` | `propertyPath` <= `baselinePropertyPath` |

---

## 主要属性

### propertyPath — 比較元フィールド

```java
propertyPath = "startDate"
```

配列で複数指定すると、各フィールドを `baselinePropertyPath` と比較します。

### baselinePropertyPath — 基準フィールド

```java
baselinePropertyPath = "endDate"
```

### typeConversionFromString — 文字列からの型変換

フィールドの値が文字列として保持されている場合、比較前に型変換を行います。

| `TypeConversionFromString` | 意味 |
| --------------------------- | ---- |
| `NONE`（デフォルト） | 変換なし（数値型・日付型をそのまま比較） |
| `NUMBER` | 文字列を数値に変換してから比較 |
| `DATE` | 文字列を日付に変換してから比較 |

```java
@LessThan(
    propertyPath = "startDateStr",
    baselinePropertyPath = "endDateStr",
    typeConversionFromString = TypeConversionFromString.DATE,
    typeConversionDateFormat = "yyyy/MM/dd"
)
```

### typeConversionDateFormat

`typeConversionFromString = DATE` のとき使用するフォーマット。デフォルトは `"yyyy-MM-dd"`。

---

## 対応する型

数値型: `Long`, `Integer`, `Short`, `Byte`, `Double`, `Float`, `BigInteger`, `BigDecimal`

日付型: `LocalDate`, `LocalDateTime`, `OffsetDateTime`, `ZonedDateTime`

文字列: バイト比較

---

## 使用例

### 日付の前後チェック

```java
@LessThan(
    propertyPath = "startDate",
    baselinePropertyPath = "endDate"
)
public class EventForm { ... }
```

### 数値の範囲チェック

```java
@LessThanOrEqualTo(
    propertyPath = "minPrice",
    baselinePropertyPath = "maxPrice"
)
public class PriceRangeForm { ... }
```

### 文字列で保持された日付の比較

```java
@LessThan(
    propertyPath = "startDateStr",
    baselinePropertyPath = "endDateStr",
    typeConversionFromString = TypeConversionFromString.DATE
)
public class SearchForm { ... }
```
