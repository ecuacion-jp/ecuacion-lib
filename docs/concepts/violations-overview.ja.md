# 違反・例外の概要

ecuacion-lib では、バリデーション違反を種類によらず `Violations` クラスに集約して管理します。

## 2種類の違反

`Violations` に追加できる違反は2種類あります。

**BusinessViolation** — 業務ロジック上の違反を手動で追加します。

**ConstraintViolation** — Jakarta Validation によるバリデーション結果の違反です。

## Violations による管理

2種類の違反を `Violations` に集約し、`throwIfAny()` でまとめてスローします。
複数のチェックをひとまとめにできるため、1リクエストで全エラーをユーザーに返せます。

詳細は **violation** メニューの各記事を参照してください。
