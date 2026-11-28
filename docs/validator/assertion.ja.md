# アサーション系バリデーター

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
