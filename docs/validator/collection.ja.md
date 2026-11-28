# コレクション系バリデーター

クラスレベルで `propertyPath[]` に複数フィールドを指定し、それらの null / empty 状態をまとめて検証します。

## Any系

| アノテーション | 検証内容 |
| ------------- | -------- |
| `@AnyNull` | 少なくとも1フィールドが `null` であること |
| `@AnyNotNull` | 少なくとも1フィールドが `null` でないこと |
| `@AnyEmpty` | 少なくとも1フィールドが空（null または空文字）であること |
| `@AnyNotEmpty` | 少なくとも1フィールドが空でないこと |

## All系

| アノテーション | 検証内容 |
| ------------- | -------- |
| `@AllOrNoneNull` | 全フィールドが `null`、または全フィールドが `null` でないこと |
| `@AllOrNoneEmpty` | 全フィールドが空、または全フィールドが空でないこと |

## 型別の empty 判定

`Empty` 系（`@AnyEmpty`, `@AnyNotEmpty`, `@AllOrNoneEmpty`）では、型によって「空」の定義が異なります。

| 型 | 「空」とみなす条件 |
| ---- | ----------------- |
| `String` | `null` または空文字（`""`） |
| その他の型 | `null` のみ |

`@AnyNotNull` など `Null` 系は型を問わず `null` のみを対象とします。

## 使用例

```java
// startDate と endDate はどちらも入力するか、どちらも入力しないかのどちらか
@AllOrNoneNull(propertyPath = {"startDate", "endDate"})
public class SearchForm { ... }
```

```java
// email / phone / address のうち少なくとも1つは入力必須
@AnyNotEmpty(propertyPath = {"email", "phone", "address"})
public class ContactForm { ... }
```
