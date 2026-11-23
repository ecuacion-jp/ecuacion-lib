# 条件付きバリデーター

## 概要

When系アノテーションは、「`conditionPropertyPath` が特定の状態のとき、`propertyPath` は〇〇でなければならない」という条件付きルールを表現します。クラスレベルのアノテーションです。

```java
@TrueWhen(
    propertyPath = "agreedToTerms",
    conditionPropertyPath = "accountType",
    conditionValueState = ConditionValueState.NOT_EMPTY
)
public class RegistrationForm { ... }
```

---

## アノテーション一覧

### 基本条件（値そのものを持たない条件）

| アノテーション | `propertyPath` が満たすべき条件 |
| ------------- | ------------------------------- |
| `@TrueWhen` | `true` であること |
| `@FalseWhen` | `false` であること |
| `@NullWhen` | `null` であること |
| `@NotNullWhen` | `null` でないこと |
| `@EmptyWhen` | 空（null または空文字）であること |
| `@NotEmptyWhen` | 空でないこと |

### パターン・文字列・値参照条件

これらのアノテーションは `conditionValue` に加え、固有のパラメータを持ちます。

| アノテーション | `propertyPath` が満たすべき条件 | 固有パラメータ |
| ------------- | ------------------------------- | ------------- |
| `@PatternWhen` | 正規表現にマッチすること | `regexp` |
| `@NotPatternWhen` | 正規表現にマッチしないこと | `regexp` |
| `@StringWhen` | 指定した文字列のいずれかであること | `string[]` |
| `@NotStringWhen` | 指定した文字列のいずれでもないこと | `string[]` |
| `@ValueOfPropertyPathWhen` | 別フィールドと同値であること | `valuePropertyPath` |
| `@NotValueOfPropertyPathWhen` | 別フィールドと異なること | `valuePropertyPath` |

> **Note:** `@PatternWhen` / `@NotPatternWhen` の `regexp`、および `conditionValuePatternRegexp`
> （下記 [conditionValue](#conditionvalue--条件の種類) を参照）は、エンドユーザー入力に由来しうる値に対して
> マッチングされます。ネストした量指定子（例：`(a+)+`）のような、破滅的バックトラッキングを起こしやすい
> パターンは避けてください。細工した入力に対してマッチングが指数時間かかることがあります。

---

## 主要属性

### propertyPath — 検証対象フィールド

```java
propertyPath = "agreedToTerms"            // 単一フィールド
propertyPath = {"startDate", "endDate"}   // 複数フィールドを同じルールで検証
```

### conditionPropertyPath — 条件フィールド

条件の判定に使うフィールド名です。ネストしたフィールドも指定できます。
条件を複数指定したい場合は、代わりに `conditions`（下記参照）を使います。

```java
conditionPropertyPath = "accountType"
conditionPropertyPath = "address.country"
```

### conditionValue — 条件の種類

`conditionPropertyPath` のフィールドがどういう状態のときにバリデーションを実行するかを指定します。

| `ConditionValue` | 意味 | それを決める属性 |
| ---------------- | ---- | --------------- |
| `NULL` | `null` であること | `conditionValueState = ConditionValueState.NULL` |
| `NOT_NULL` | `null` でないこと | `conditionValueState = ConditionValueState.NOT_NULL` |
| `EMPTY` | 空（null または空文字）であること | `conditionValueState = ConditionValueState.EMPTY` |
| `NOT_EMPTY` | 空でないこと | `conditionValueState = ConditionValueState.NOT_EMPTY` |
| `TRUE` | `true` であること | `conditionValueBoolean = true` |
| `FALSE` | `false` であること | `conditionValueBoolean = false` |
| `STRING` | 指定文字列のいずれかであること | `conditionValueString` |
| `PATTERN` | 正規表現にマッチすること | `conditionValuePatternRegexp` |
| `VALUE_OF_PROPERTY_PATH` | 別フィールドと同値であること | `conditionValuePropertyPath` |

`STRING` / `PATTERN` / `VALUE_OF_PROPERTY_PATH` は追加で `conditionValueDisplayStringPropertyPath`
（下記参照）も指定でき、`PATTERN` はさらに `conditionValuePatternDescription` も指定できます。

> **Note:** `conditionValue` 自体は基本的に省略できます。`conditionValueString`、
> `conditionValuePatternRegexp`、`conditionValuePropertyPath`、`conditionValueBoolean`、
> `conditionValueState` のうちどれか1つだけを設定すれば一意に決まるため、実際には
> `conditionValue = ...` を書く必要はなく、意図する条件に対応する属性を1つ設定するだけで済みます。
> これら5つの属性を複数同時に設定した場合や、いずれも設定しなかった場合はエラーになります。
> これらの属性に加えて `conditionValue` を明示的に指定することも可能で、その場合は指定した属性の値と
> 整合するかがチェックされます（例：`conditionValue = TRUE` と `conditionValueBoolean = false` を
> 同時に指定するとエラーになります）。

### conditionOperator — 条件演算子

| `ConditionOperator` | 意味 |
| ------------------- | ---- |
| `EQUAL_TO`（デフォルト） | 条件が成立するとき validte する |
| `NOT_EQUAL_TO` | 条件が成立しないとき validate する |

### conditionValuePatternDescription

`conditionValue = PATTERN` のとき、`conditionValuePatternRegexp` に設定した正規表現の代わりにエラーメッセージへ表示する人間向けの説明文を指定します。省略すると正規表現がそのまま表示されます。

```java
// conditionValuePatternDescription なし
// → 「電話番号」が「^(070|080|090).*」に合致する場合は〜

// conditionValuePatternDescription あり
@TrueWhen(
    propertyPath = "smsConsentAgreed",
    conditionPropertyPath = "phone",
    conditionValuePatternRegexp = "^(070|080|090).*",
    conditionValuePatternDescription = "携帯電話番号"  // メッセージに使う説明
)
// → 「電話番号」が「携帯電話番号」に合致する場合は〜
```

### conditionValueDisplayStringPropertyPath

`STRING` / `PATTERN` / `VALUE_OF_PROPERTY_PATH` のとき、エラーメッセージに表示する「条件値の表示名」を別フィールドの itemNameKey から解決する際に指定します。省略すると条件値の実値（文字列・正規表現・フィールド値）がそのまま表示されます。

### conditionValueBoolean / conditionValueState

値そのものを持たない6つの条件（自明な区別材料がないもの）は、これら2つの属性を使うことで
`conditionValue` を明示せずに指定できます。

```java
conditionValueBoolean = true                              // conditionValue = TRUE
conditionValueBoolean = false                              // conditionValue = FALSE
conditionValueState = ConditionValueState.NULL             // conditionValue = NULL
conditionValueState = ConditionValueState.NOT_NULL         // conditionValue = NOT_NULL
conditionValueState = ConditionValueState.EMPTY            // conditionValue = EMPTY
conditionValueState = ConditionValueState.NOT_EMPTY        // conditionValue = NOT_EMPTY
```

### falseWhenConditionNotSatisfied

`true` にすると、条件が成立しないときに逆のルールを適用します（`@TrueWhen` なら、条件不成立のとき `false` であることを検証）。デフォルトは `false`。

### conditions — 複数条件（AND）

条件を2つ以上指定したい場合は、`conditionPropertyPath` などの `condition*` 属性の代わりに
`conditions` に `@Condition` を並べて指定します。**すべての条件が成立したとき**（AND）にバリデーションが実行されます。

```java
@NotEmptyWhen(propertyPath = "otherTypeDetail", conditions = {
    @Condition(propertyPath = "type", valueString = "OTHER"),
    @Condition(propertyPath = "status", valueState = ConditionValueState.NOT_EMPTY)})
// → 「種別」が「OTHER」で、かつ「ステータス」が空欄以外の場合は空欄以外にしてください
```

`@Condition` の属性は、`condition` 接頭辞を外した名前で When系アノテーションの同名属性と同じ意味を持ちます
（`propertyPath`、`operator`、`valueString`、`valuePatternRegexp`、`valuePatternDescription`、
`valuePropertyPath`、`valueBoolean`、`valueState`、`valueDisplayStringPropertyPath`）。

- `conditions` と `conditionPropertyPath` などの `condition*` 属性を同時に指定するとエラーになります。
  いずれも指定しなかった場合もエラーです。
- OR 条件は指定できません（Java のアノテーションは自分自身を入れ子にできないため、任意の論理式は表現できません）。
  OR にしたい場合は、When系アノテーションを複数付けてください。
- `falseWhenConditionNotSatisfied` などを `true` にした場合、「すべての条件が成立した」以外のとき
  （いずれか1つでも不成立のとき）に逆のルールが適用されます。

---

## 使用例

### 基本例 — 条件付き必須

```java
// accountType が入力済みのとき、agreedToTerms は true でなければならない
// conditionValue は conditionValueState から NOT_EMPTY と推論されるため省略可能
@TrueWhen(
    propertyPath = "agreedToTerms",
    conditionPropertyPath = "accountType",
    conditionValueState = ConditionValueState.NOT_EMPTY
)
public class RegistrationForm { ... }
```

### STRING 条件 — 特定の値のとき

```java
// role が "ADMIN" のとき、adminCode は空でないこと
// conditionValue は conditionValueString から STRING と推論されるため省略可能
@NotEmptyWhen(
    propertyPath = "adminCode",
    conditionPropertyPath = "role",
    conditionValueString = {"ADMIN"}
)
public class UserForm { ... }
```

### NOT_EQUAL_TO — 条件不成立時に検証

```java
// 送信確認フラグが true でないとき、reason は空でないこと
// conditionValue は conditionValueBoolean から TRUE と推論されるため省略可能
@NotEmptyWhen(
    propertyPath = "reason",
    conditionPropertyPath = "confirmed",
    conditionValueBoolean = true,
    conditionOperator = ConditionOperator.NOT_EQUAL_TO
)
public class CancelForm { ... }
```

### @PatternWhen — 正規表現で検証（regexp）

```java
// type が "POSTAL" のとき、code は7桁の数字であること
// conditionValue は conditionValueString から STRING と推論されるため省略可能
@PatternWhen(
    propertyPath = "code",
    regexp = "\\d{7}",
    conditionPropertyPath = "type",
    conditionValueString = {"POSTAL"}
)
public class AddressForm { ... }
```

`description` を指定すると、エラーメッセージに正規表現の代わりにその文言が表示されます。
文言は `ValidationMessagesPatternDescriptions.properties` から探し、キーがなければ指定した文字列をそのまま表示します。
省略した場合は、正規表現がそのまま表示されます。

### @StringWhen — 特定の文字列であることを検証（string[]）

```java
// role が NOT_EMPTY のとき、accountType は "ADMIN" または "OPERATOR" であること
@StringWhen(
    propertyPath = "accountType",
    string = {"ADMIN", "OPERATOR"},
    conditionPropertyPath = "role",
    conditionValueState = ConditionValueState.NOT_EMPTY
)
public class UserForm { ... }
```

### @ValueOfPropertyPathWhen — 別フィールドと同値であることを検証（valuePropertyPath）

```java
// confirmed が true のとき、confirmPassword は password と同じ値であること
@ValueOfPropertyPathWhen(
    propertyPath = "confirmPassword",
    valuePropertyPath = "password",
    conditionPropertyPath = "confirmed",
    conditionValueBoolean = true
)
public class PasswordForm { ... }
```
