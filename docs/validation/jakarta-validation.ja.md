# ConstraintViolation

## 概要

`ConstraintViolation` は、Jakarta Validation（旧 Bean Validation）によるバリデーション結果を表すクラスです。
Jakarta Validation アノテーション（`@NotNull`、`@Size` など）を付与したオブジェクトを検証すると、
違反ごとに `ConstraintViolation` が生成されます。

ecuacion-lib では `violations.addAll()` でこの結果を `Violations` に追加します。

---

## violations.addAll() — ConstraintViolation の追加

Jakarta Validation で検証すると `Set<ConstraintViolation<T>>` が返されます。
これを `violations.addAll()` で追加します。

```java
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
violations.addAll(validator.validate(someForm));
```

バリデーショングループを指定する場合:

```java
violations.addAll(validator.validate(someForm, GroupA.class, GroupB.class));
```

---

## Violations.validate() — ショートハンド

上記の検証と追加をまとめたショートハンドです。

```java
// 標準の書き方
violations.addAll(validator.validate(someObject));

// 同等のショートハンド
violations.validate(someObject);
violations.validate(someObject, GroupA.class);  // グループ指定
```
