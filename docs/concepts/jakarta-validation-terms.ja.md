# Jakarta Validation の用語

Jakarta Validation の仕様で定義されているオブジェクト・用語です。
ecuacion-lib のバリデーション関連クラスのドキュメントや API でこれらの用語が登場します。

---

## ConstraintViolation

`jakarta.validation.ConstraintViolation` は、単一のバリデーション違反を表す Jakarta Validation 標準インターフェースです。
バリデーションを実行すると、違反1件ごとに1つの `ConstraintViolation` が生成されます。

---

## rootBean

バリデーションを呼び出したときの最上位オブジェクトです。

```java
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
validator.validate(form);  // form が rootBean
```

`Validator.validate()` に渡したオブジェクトが `rootBean` になります。
ネストしたオブジェクトのフィールドを検証する場合（`@Valid` を使用）も、`rootBean` は変わりません。

---

## leafBean

バリデーション対象フィールドを直接保持するオブジェクトです。

フィールドが `rootBean` 自身に属する場合は `leafBean == rootBean` です。
ネストしたオブジェクトのフィールドを検証する場合は、そのネストしたオブジェクトが `leafBean` になります。

例：`form.dept.name` を検証する場合

| | オブジェクト |
| --- | --- |
| `rootBean` | `form` |
| `leafBean` | `form.dept`（`name` フィールドを持つ `Dept` オブジェクト） |

---

## propertyPath

rootBean を起点として、バリデーション対象フィールドへのパスを表す文字列です。
ドット記法で表現され、コレクション要素にはインデックスが付きます。

| propertyPath | 意味 |
| --- | --- |
| `name` | rootBean 直下の `name` フィールド |
| `dept.name` | rootBean が保持する `dept` オブジェクトの `name` フィールド |
| `bookList[1].title` | rootBean が保持する `bookList` の2番目の要素の `title` フィールド |
