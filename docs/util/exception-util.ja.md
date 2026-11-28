# ExceptionUtil

`ExceptionUtil`（`jp.ecuacion.lib.core.util.ExceptionUtil`）は例外からメッセージ文字列を取り出すユーティリティクラスです。
カスタム例外ハンドラーを実装する際に使用します。

---

## validate とメッセージ生成の分離

Jakarta Validation 標準では、`validator.validate(...)` を呼んだ瞬間にメッセージが生成されます。
`ConstraintViolation.getMessage()` は生成済みの文字列を返すだけです。

```java
// 標準の場合：validate と同時にメッセージが生成される
Set<ConstraintViolation<Account>> set = validator.validate(account);
set.forEach(v -> System.out.println(v.getMessage())); // すでに文字列化されている
```

このため、ユーザーごとに locale を切り替える多言語対応システムでは、
service 層のメソッドに `Locale` を引数として持ち込む必要が生じます。

```java
// locale をサービス層に持ち込まざるを得ない
public void createAccount(Account account, Locale locale) {
    ValidatorFactory factory = Validation.byDefaultProvider().configure()
        .messageInterpolator(new LocaleSpecificMessageInterpolator(locale))
        .buildValidatorFactory();
    ...
}
```

### 解決策：validate とメッセージ生成を分離する

1. service 層で validate → 違反があれば例外をスロー（locale 不要）
2. ExceptionHandler で catch → そこで locale を使ってメッセージを生成

```java
// service 層：locale に関与しない
public void createAccount(Account account) {
    Set<ConstraintViolation<Account>> set = validator.validate(account);
    if (!set.isEmpty()) {
        throw new ConstraintViolationException(set);
    }
}

// ExceptionHandler：ここで locale を使ってメッセージを生成
List<String> messages = ExceptionUtil.getMessageList(ex, Locale.JAPANESE);
```

`ExceptionUtil.getMessageList()` を呼んだタイミングでメッセージが生成されます。

---

## getMessageList — 例外からメッセージリストを取得

例外の種類に関わらず、統一した方法でメッセージ文字列のリストを返します。

```java
List<String> messages = ExceptionUtil.getMessageList(throwable);

// ロケール指定
List<String> messages = ExceptionUtil.getMessageList(throwable, Locale.JAPANESE);
```

内部では例外の型に応じて以下のように処理を振り分けます。

| 例外の型 | 処理 |
| --- | --- |
| `ViolationException` | `Violations` の各 Violation からメッセージを構築 |
| `ConstraintViolationException` | 各 `ConstraintViolation` からメッセージを構築 |
| その他 | `throwable.getMessage()` をそのまま返す |

`ConstraintViolationException` は複数の違反を持てるため、返値は常に `List` です。
通常の例外でも `List`（要素1件）で返ります。
ただし、`getMessage()` が `null` の例外（`new NullPointerException()` など）の場合は空のリストを返します。
違反が0件の場合（空の `Set` や `Violations` など）も、空のリストを返します。

---

## 入力型ごとのオーバーロード

`Throwable` の他にも、直接型を指定したオーバーロードがあります。

```java
// Violations から
List<String> messages = ExceptionUtil.getMessageList(violations);
List<String> messages = ExceptionUtil.getMessageList(violations, Locale.JAPANESE);

// ConstraintViolation の Set から
List<String> messages = ExceptionUtil.getMessageList(constraintViolationSet);
List<String> messages = ExceptionUtil.getMessageList(constraintViolationSet, Locale.JAPANESE);
```

---

## showsItemNameAsDefault パラメータ

一部のオーバーロードには `showsItemNameAsDefault` フラグがあります。
このフラグを持たないオーバーロードでは `false` として扱われます。

```java
// true にすると項目名入りのメッセージを優先して参照
List<String> messages = ExceptionUtil.getMessageList(throwable, Locale.JAPANESE, true);
```

`true` にすると、メッセージの解決に項目名入りのメッセージファイルを優先して使用します。
Jakarta Validation の違反には `ValidationMessagesWithItemNames.properties`、
`BusinessViolation` には `messages_with_item_names.properties` が使われます。

各 Violation に `MessageParameters.showsItemName` が `true` / `false` で設定されている場合は、
その値がこのデフォルトより優先されます。
`showsItemName` との関係の詳細は **violation &gt; MessageParameters** を参照してください。
