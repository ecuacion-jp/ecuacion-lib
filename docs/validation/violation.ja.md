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

## 違反の追加

### BusinessViolation の追加

`BusinessViolation` のコンストラクタと同じ引数を `add(...)` に直接渡すこともできます。

```java
// new BusinessViolation("error.range", "年齢", "0", "150") と同じ
violations.add("error.range", "年齢", "0", "150");

// 関連する項目の itemPropertyPath を指定する
violations.add(new String[] {"birthDate"}, "error.future-date");

// itemNameKey と itemPropertyPath を指定する
violations.add(new String[] {"user.birthDate"}, new String[] {"birthDate"},
    "error.future-date");
```

各引数の意味は [BusinessViolation](business-violation.md) を参照してください。

`BusinessViolation` のリストは `addAll(List<BusinessViolation>)` でまとめて追加できます。

### Jakarta Validation による検証

`validate(object)` は、Jakarta Validation で `object` を検証し、結果をそのまま追加します。
`addAll(validator.validate(object))` と同じですが、`Validator` を用意する必要がありません。

```java
violations.validate(form);

// グループを指定して検証する
violations.validate(form, UpdateGroup.class);
```

検証済みの `Set<ConstraintViolation>` は `addAll(...)`、1件の `ConstraintViolation` は `add(...)` で追加します。

### 別の Violations の取り込み

`addAll(Violations)` は、別の `Violations` に追加されたすべての違反（`ConstraintViolation` と `BusinessViolation` の両方）を取り込みます。
メソッドごとに `Violations` を作り、呼び出し元でまとめる場合に使います。

```java
Violations violations = new Violations();
violations.addAll(checkHeader(header));
violations.addAll(checkDetails(details));
violations.throwIfAny();
```

`MessageParameters` は取り込まれません（取り込む側の設定が使われます）。

`add(...)`・`addAll(...)`・`validate(...)` はいずれも自身を返すため、メソッドチェーンで記述できます。

```java
new Violations().validate(form).add("error.condition-a").throwIfAny();
```

---

## 違反の確認・取得

| メソッド | 内容 |
| ------ | ------ |
| `isEmpty()` | 違反が1件も追加されていなければ `true` |
| `getConstraintViolations()` | 追加された `ConstraintViolation` のリスト（コピー） |
| `getBusinessViolations()` | 追加された `BusinessViolation` のリスト（コピー） |

返されるリストはコピーのため、変更しても `Violations` には反映されません。

違反をメッセージの文字列として取得する場合は、`ExceptionUtil.getMessageList(violations)` を使います
（[ExceptionUtil](../util/exception-util.md) を参照）。

---

## throwIfAny() — まとめてスロー

`throwIfAny()` は違反が1件でも存在すれば `ViolationException` をスローします。
違反がなければ何もしません。

### 独自の例外クラスをスローする

`throwIfAny(Class)` を使うと、`ViolationException` の代わりに、指定したサブクラスをスローします。

```java
violations.throwIfAny(CsvUploadViolationException.class);
```

指定するクラスには、`ViolationException` と同じく `Violations` を1つ受け取る public コンストラクタが必要です。
コンストラクタがない場合は `RuntimeException` がスローされます。

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
    ExceptionUtil.getMessageList(e.getViolations()).forEach(logger::warn);
} catch (ViolationException e) {
    // エラー → 処理停止
    throw e;
}
```
