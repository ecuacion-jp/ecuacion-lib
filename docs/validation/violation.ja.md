# Violation

## 概要

ecuacion-lib では、バリデーション違反を `Violations` クラスに集約してから
`throwIfAny()` でまとめてスローするパターンを採用しています。

`Violations` は2種類の違反を受け付けます。

| 種別 | クラス | 用途 |
| ------ | -------- | ------ |
| ビジネスルール違反 | `BusinessViolation` | 業務ロジック上の違反を手動で作成 |
| 制約違反 | `ConstraintViolation` | Jakarta Validation の検証結果 |

---

## 基本的な使い方

```java
Violations violations = new Violations();

// ビジネスルール違反を追加
violations.add(new BusinessViolation("error.some-message-id"));

// Jakarta Validation で検証し、結果を追加
violations.addAll(validator.validate(someObject));

// 1件でも違反があれば ViolationException をスロー
violations.throwIfAny();
```

複数チェックを先に行い**まとめてスロー**することで、1リクエストで全エラーをユーザーに提示できます。

```java
Violations violations = new Violations();

if (conditionA) {
  violations.add(new BusinessViolation("error.condition-a"));
}
if (conditionB) {
  violations.add(new BusinessViolation("error.condition-b"));
}

// Jakarta Validation でフォームを検証し、結果を追加
violations.addAll(validator.validate(form));

violations.throwIfAny();
```

---

## throwIfAny() — まとめてスロー

`throwIfAny()` は違反が1件でも存在すれば `ViolationException` をスローします。
違反がなければ何もしません。

---

## ViolationException

`ViolationException` は `RuntimeException` を継承した非検査例外です。
`getViolations()` で `Violations` を取得できます。

通常のアプリケーション開発では直接キャッチするケースは少なく、
フレームワーク層（ecuacion-splib など）が一括して処理します。

---

## throwWarningIfAny() — 警告として処理する

`throwIfAny()` の代わりに `throwWarningIfAny()` を使うと、`ViolationException` ではなく
`ViolationWarningException` がスローされます。

```java
violations.throwWarningIfAny();
```

`ViolationWarningException` は `ViolationException` のサブクラスであるため、
`catch (ViolationException e)` でまとめて捕捉することもできます。

### 主な用途

**ecuacion-splib-web との組み合わせ**（主な用途）:
UI 画面上で「〇〇しますがよろしいですか？」という確認ダイアログを出す仕組みに使います。
ユーザーが確認すると、その警告を無視して処理を続行するフローになります。
詳しくは ecuacion-splib-web のドキュメントを参照してください。

**ecuacion-lib 単体での活用例**:
バッチ処理や API ハンドラで「エラー（処理停止）」と「警告（続行可）」を型で区別したい場合に使えます。

```java
// 呼び出し側
try {
    someService.process(data);
} catch (ViolationWarningException e) {
    // 警告 → ログ記録して続行
    logger.warn(e.getViolations().toString());
} catch (ViolationException e) {
    // エラー → 処理停止
    throw e;
}
```
