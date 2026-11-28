# クラス・メソッドバリデーターの補足

このページは、ecuacion-lib のクラスバリデーター（`@NotEmptyWhen`、`@LessThan`、`@ReturnTrue` など）と、
メソッドに付与する `@AssertTrueWithPropertyPath`（メソッドバリデーター）に共通する補足です。
いずれも `propertyPath` 属性で、エラーを紐付けるフィールドを指定します。

---

## ConstraintViolation#getPropertyPath() の値

クラス・メソッドバリデーターの `ConstraintViolation#getPropertyPath()` は、`propertyPath` 属性に指定したフィールドを指しません。

| バリデーター | `getPropertyPath()` |
| --- | --- |
| クラスバリデーター | アノテーションを付与したクラスのパス（ルートのクラスに付与した場合は `""`） |
| メソッドバリデーター | 付与したメソッドのプロパティ名（`isDateRangeValid()` に付与した場合は `dateRangeValid`） |

ecuacion-lib の [ExceptionUtil](../util/exception-util.md) で `ConstraintViolation` を例外に変換する場合は、
`getPropertyPath()` ではなく `propertyPath` 属性からフィールドを特定するため、この値を意識する必要はありません。
`ConstraintViolation#getPropertyPath()` を直接使う場合にのみ関係します。

---

## CreateMultipleConstraintViolationsConstraintValidatorFactory

クラス・メソッドバリデーターは、デフォルトでは `propertyPath` に指定したフィールドの数によらず、`ConstraintViolation` を 1 件だけ生成します。

`CreateMultipleConstraintViolationsConstraintValidatorFactory` を `Validator` 生成時に指定すると、
`propertyPath` の数だけ `ConstraintViolation` を生成し、それぞれの `getPropertyPath()` にフィールド名が設定されます。

```java
// デフォルト
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

// CreateMultipleConstraintViolationsConstraintValidatorFactory 使用
Validator validator = Validation.byDefaultProvider().configure()
    .constraintValidatorFactory(
        new CreateMultipleConstraintViolationsConstraintValidatorFactory())
    .buildValidatorFactory().getValidator();
```

`propertyPath = {"startDate", "endDate"}` を指定した場合の `getPropertyPath()` は以下のとおりです。

| バリデーター | デフォルト | `CreateMultiple...` 使用 |
| --- | --- | --- |
| クラスバリデーター（ルートのクラスに付与） | `""`（1 件） | `startDate`、`endDate`（2 件） |
| メソッドバリデーター（`isDateRangeValid()` に付与） | `dateRangeValid`（1 件） | `dateRangeValid.startDate`、`dateRangeValid.endDate`（2 件） |

メソッドバリデーターでは、付与したメソッドのプロパティ名が先頭に付きます。
`getPropertyPath()` をそのままフィールドに対応付ける仕組みと組み合わせる場合は、クラスバリデーター（`@ReturnTrue` など）の方が扱いやすくなります。

ecuacion-lib 内部では1件の `ConstraintViolation` を生成する方式を標準としているため、このファクトリはオプションです。複数件生成すると複数バリデータ由来のエラーとの判別が難しくなる点に注意してください。
