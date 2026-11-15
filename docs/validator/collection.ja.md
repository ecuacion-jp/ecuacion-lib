# コレクション・アサーション

## コレクション系 — 複数フィールドの null / empty 一括検証

クラスレベルで `propertyPath[]` に複数フィールドを指定し、それらの null / empty 状態をまとめて検証します。

### Any系

| アノテーション | 検証内容 |
| ------------- | -------- |
| `@AnyNull` | 少なくとも1フィールドが `null` であること |
| `@AnyNotNull` | 少なくとも1フィールドが `null` でないこと |
| `@AnyEmpty` | 少なくとも1フィールドが空（null または空文字）であること |
| `@AnyNotEmpty` | 少なくとも1フィールドが空でないこと |

### All系

| アノテーション | 検証内容 |
| ------------- | -------- |
| `@AllOrNoneNull` | 全フィールドが `null`、または全フィールドが `null` でないこと |
| `@AllOrNoneEmpty` | 全フィールドが空、または全フィールドが空でないこと |

### 型別の empty 判定

`Empty` 系（`@AnyEmpty`, `@AnyNotEmpty`, `@AllOrNoneEmpty`）では、型によって「空」の定義が異なります。

| 型 | 「空」とみなす条件 |
| ---- | ----------------- |
| `String` | `null` または空文字（`""`） |
| その他の型 | `null` のみ |

`@AnyNotNull` など `Null` 系は型を問わず `null` のみを対象とします。

### 使用例

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

---

## AssertTrueWithPropertyPath — フィールド紐付き AssertTrue

標準の `@AssertTrue` と同様に `true` であることを検証しますが、`propertyPath` 属性でエラーを特定のフィールドに紐付けられます。メソッドレベルのアノテーションです。

```java
public class EventForm {
    @AssertTrueWithPropertyPath(
        propertyPath = {"startDate", "endDate"},
        message = "開始日は終了日より前に設定してください"
    )
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) return true;
        return startDate.isBefore(endDate);
    }
}
```

### propertyPath の取得

`propertyPath` の値は `ConstraintViolation` のアノテーション属性から取得できます。

```java
Set<ConstraintViolation<EventForm>> set = validator.validate(form);
for (ConstraintViolation<?> cv : set) {
    String[] paths = (String[]) cv.getConstraintDescriptor()
        .getAttributes().get("propertyPath");
    // → ["startDate", "endDate"]
}
```

---

## ReturnTrue — クラスレベルのメソッドバリデーター

クラスに付与し、`methodName` で指定したメソッドが `true` を返すことを検証します。クラスレベルのアノテーションです。

```java
@ReturnTrue(
    methodName = "isDateRangeValid",
    propertyPath = {"startDate", "endDate"},
    message = "開始日は終了日より前に設定してください"
)
public class EventForm {
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) return true;
        return startDate.isBefore(endDate);
    }
}
```

| 属性 | 説明 |
| ---- | ---- |
| `methodName` | 実行するメソッド名（引数なし、`boolean` 戻り値） |
| `propertyPath` | エラーを紐付けるフィールド |
| `message` | エラーメッセージ（メッセージキーまたはリテラル文字列） |

### AssertTrueWithPropertyPath との比較

| | `@AssertTrueWithPropertyPath` | `@ReturnTrue` |
| ---- | ---- | ---- |
| 付与レベル | メソッド | クラス |
| `getPropertyPath()` のデフォルト | `isDateRangeValid.startDate`（メソッド名が prefix に付く） | `startDate`（prefix なし） |

`@AssertTrueWithPropertyPath` は `getPropertyPath()` にメソッド名が prefix として付くため、フレームワーク側でフィールドを特定する用途には `@ReturnTrue` が適しています。

---

## CreateMultipleConstraintViolationsConstraintValidatorFactory

クラスレベルバリデータのデフォルト動作では、`ConstraintViolation.getPropertyPath()` は空文字を返すため、どのフィールドのエラーかを `getPropertyPath()` で特定できません。

`CreateMultipleConstraintViolationsConstraintValidatorFactory` を `Validator` 生成時に指定すると、`propertyPath` の数だけ `ConstraintViolation` を生成し、それぞれの `getPropertyPath()` にフィールド名が設定されます。

```java
// デフォルト（propertyPath が空文字）
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

// CreateMultipleConstraintViolationsConstraintValidatorFactory 使用
Validator validator = Validation.byDefaultProvider().configure()
    .constraintValidatorFactory(
        new CreateMultipleConstraintViolationsConstraintValidatorFactory())
    .buildValidatorFactory().getValidator();
```

`startDate` と `endDate` を `propertyPath` に指定した場合の違い：

```text
// デフォルト
propertyPath : ""  （空文字）

// CreateMultiple... 使用
propertyPath : startDate
propertyPath : endDate  （2件生成）
```

ecuacion-lib 内部では1件の `ConstraintViolation` を生成する方式を標準としているため、このファクトリはオプションです。複数件生成すると複数バリデータ由来のエラーとの判別が難しくなる点に注意してください。
