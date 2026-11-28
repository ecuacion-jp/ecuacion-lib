# 条件付きバリデーター

## 概要

When系アノテーションは、「`conditionPropertyPath` が特定の状態のとき、`propertyPath` は〇〇でなければならない」という条件付きルールを表現します。クラスレベルのアノテーションです。

```java
// 法人として申し込むとき（corporate が true のとき）、companyName は空でないこと
@NotEmptyWhen(
    propertyPath = "companyName",
    conditionPropertyPath = "corporate",
    conditionValueBoolean = true
)
public class ApplicationForm { ... }
```

---

## 主要属性

すべての When系アノテーションに共通する、必須の属性です。

### propertyPath — 検証対象フィールド

```java
propertyPath = "agreedToTerms"            // 単一フィールド
propertyPath = {"startDate", "endDate"}   // 複数フィールドを同じルールで検証
```

### conditionPropertyPath — 条件フィールド

条件の判定に使うフィールド名です。ネストしたフィールドも指定できます。
条件を複数指定したい場合は、代わりに `conditions`（[複数条件](#conditions--複数条件and) を参照）を使います。

```java
conditionPropertyPath = "accountType"
conditionPropertyPath = "address.country"
```

### conditionValueXxx — 条件値

`conditionPropertyPath` のフィールドがどういう状態のときにバリデーションを実行するかを、
`conditionValueXxx` という名前の属性で指定します
（`conditionPropertyPath` が `true` のときに検証したい場合は `conditionValueBoolean = true` など）。
詳細は [条件値の指定](#条件値の指定) で説明します。

---

## バリデータ一覧と使用例

### バリデータ一覧

When系アノテーションには、次のものがあります。`propertyPath` に対する検証の内容が異なります。

| アノテーション | `propertyPath` が満たすべき条件 | 固有の属性 |
| ------------- | ------------------------------- | --------- |
| `@TrueWhen` | `true` であること | なし |
| `@FalseWhen` | `false` であること | なし |
| `@NullWhen` | `null` であること | なし |
| `@NotNullWhen` | `null` でないこと | なし |
| `@EmptyWhen` | 空（null または空文字）であること | なし |
| `@NotEmptyWhen` | 空でないこと | なし |
| `@PatternWhen` | 正規表現に合致すること | `regexp`、`description` |
| `@NotPatternWhen` | 正規表現に合致しないこと | `regexp`、`description` |
| `@StringWhen` | 指定した文字列のいずれかであること | `string` |
| `@NotStringWhen` | 指定した文字列のいずれでもないこと | `string` |
| `@ValueOfPropertyPathWhen` | 別フィールドと同値であること | `valuePropertyPath` |
| `@NotValueOfPropertyPathWhen` | 別フィールドと異なること | `valuePropertyPath` |

### 使用例

肯定・否定で対になっているもの（`@TrueWhen` と `@FalseWhen` など）は、まとめて1つの例を示します。
否定側は、検証の内容が逆になるだけで、書き方は同じです。
条件値には、簡単な `conditionValueBoolean` / `conditionValueState` を使います（条件値の詳細は [条件値の指定](#条件値の指定) を参照）。

#### @TrueWhen / @FalseWhen

```java
// クーポンコードを入力したとき、クーポン利用規約への同意（agreedToCouponTerms）は true であること
@TrueWhen(
    propertyPath = "agreedToCouponTerms",
    conditionPropertyPath = "couponCode",
    conditionValueState = ConditionValueState.NOT_EMPTY
)
public class OrderForm { ... }
```

#### @NullWhen / @NotNullWhen

```java
// 配送を希望するとき（needsDelivery が true のとき）、配送希望日（deliveryDate）は null でないこと
@NotNullWhen(
    propertyPath = "deliveryDate",
    conditionPropertyPath = "needsDelivery",
    conditionValueBoolean = true
)
public class OrderForm { ... }
```

#### @EmptyWhen / @NotEmptyWhen

```java
// 個人として申し込むとき（corporate が false のとき）、companyName は空であること
@EmptyWhen(
    propertyPath = "companyName",
    conditionPropertyPath = "corporate",
    conditionValueBoolean = false
)
public class ApplicationForm { ... }
```

#### @PatternWhen / @NotPatternWhen

```java
// 国内の住所のとき（domestic が true のとき）、postalCode は7桁の数字であること
@PatternWhen(
    propertyPath = "postalCode",
    regexp = "\\d{7}",
    description = "7桁の数字",
    conditionPropertyPath = "domestic",
    conditionValueBoolean = true
)
public class AddressForm { ... }
```

`description` を指定すると、エラーメッセージに正規表現の代わりにその文言が表示されます。
文言は `ValidationMessagesPatternDescriptions.properties` から探し、キーがなければ指定した文字列をそのまま表示します。
省略した場合は、正規表現がそのまま表示されます。

`regexp` はエンドユーザー入力に由来しうる値に対してマッチングされます。ネストした量指定子（例：`(a+)+`）のような、
破滅的バックトラッキングを起こしやすいパターンは避けてください。細工した入力に対してマッチングが指数時間かかることがあります。

#### @StringWhen / @NotStringWhen

```java
// 法人として申し込むとき（corporate が true のとき）、plan は "BUSINESS" または "ENTERPRISE" であること
@StringWhen(
    propertyPath = "plan",
    string = {"BUSINESS", "ENTERPRISE"},
    conditionPropertyPath = "corporate",
    conditionValueBoolean = true
)
public class ApplicationForm { ... }
```

#### @ValueOfPropertyPathWhen / @NotValueOfPropertyPathWhen

```java
// パスワードを変更するとき（changesPassword が true のとき）、confirmPassword は newPassword と同じ値であること
@ValueOfPropertyPathWhen(
    propertyPath = "confirmPassword",
    valuePropertyPath = "newPassword",
    conditionPropertyPath = "changesPassword",
    conditionValueBoolean = true
)
public class AccountSettingsForm { ... }
```

---

## 条件値の指定

`conditionPropertyPath` のフィールドがどういう状態のときにバリデーションを実行するかを、
次の属性のうちどれか1つで指定します。

| 条件 | 指定する属性 |
| ---- | ----------- |
| `null` であること | `conditionValueState = ConditionValueState.NULL` |
| `null` でないこと | `conditionValueState = ConditionValueState.NOT_NULL` |
| 空（null または空文字）であること | `conditionValueState = ConditionValueState.EMPTY` |
| 空でないこと | `conditionValueState = ConditionValueState.NOT_EMPTY` |
| `true` であること | `conditionValueBoolean = true` |
| `false` であること | `conditionValueBoolean = false` |
| 指定文字列のいずれかであること | `conditionValueString` |
| 正規表現に合致すること | `conditionValuePatternRegexp` |
| 別フィールドと同値であること | `conditionValuePropertyPath` |

これらの属性を複数同時に設定した場合や、いずれも設定しなかった場合はエラーになります。

### conditionValueState — null・空かどうか

```java
// note が空のとき、noteReason は空であること
@EmptyWhen(
    propertyPath = "noteReason",
    conditionPropertyPath = "note",
    conditionValueState = ConditionValueState.EMPTY
)
```

### conditionValueBoolean — true・false

`conditionPropertyPath` のフィールドは `boolean` / `Boolean` である必要があります。
`Boolean` の `null` は、`true` でも `false` でもない値として扱います（`conditionValueBoolean = true` なら条件を満たさず、
`conditionOperator = NOT_EQUAL_TO` を組み合わせると条件を満たします）。`null` であることを条件にする場合は `conditionValueState = ConditionValueState.NULL` を使います。

```java
// sendsMail が true のとき、mailAddress は空でないこと
@NotEmptyWhen(
    propertyPath = "mailAddress",
    conditionPropertyPath = "sendsMail",
    conditionValueBoolean = true
)
```

### conditionValueString — 指定文字列

指定した文字列と一致したときに条件が成立します。複数の文字列を指定すると、いずれかに一致したときに成立します。
`conditionPropertyPath` のフィールドは文字列である必要があります。

```java
// role が "ADMIN" のとき、adminCode は空でないこと
@NotEmptyWhen(
    propertyPath = "adminCode",
    conditionPropertyPath = "role",
    conditionValueString = "ADMIN"
)

// role が "ADMIN" または "OPERATOR" のとき、adminCode は空でないこと
@NotEmptyWhen(
    propertyPath = "adminCode",
    conditionPropertyPath = "role",
    conditionValueString = {"ADMIN", "OPERATOR"}
)
```

### conditionValuePatternRegexp — 正規表現

`conditionPropertyPath` のフィールドは文字列である必要があります。

```java
// phone が携帯電話番号のとき、smsConsentAgreed は true であること
@TrueWhen(
    propertyPath = "smsConsentAgreed",
    conditionPropertyPath = "phone",
    conditionValuePatternRegexp = "^(070|080|090).*",
    conditionValuePatternDescription = "携帯電話番号"
)
// → 「電話番号」が「携帯電話番号」に合致する場合は〜
```

`conditionValuePatternDescription` には、エラーメッセージに正規表現の代わりに表示する利用者向けの説明文を指定します。
省略すると正規表現がそのまま表示されます（上の例なら「電話番号」が「^(070|080|090).*」に合致する場合は〜）。
役割は `@PatternWithDescription` の `description` と同じです。

`regexp` と同じく、破滅的バックトラッキングを起こしやすいパターンは避けてください
（[@PatternWhen / @NotPatternWhen](#patternwhen--notpatternwhen) を参照）。

### conditionValuePropertyPath — 別フィールドの値

別のフィールドの値と一致したときに条件が成立します。別のフィールドが配列の場合は、いずれかの要素と一致したときに成立します。

```java
// 利用開始日（startDate）が申込日（applicationDate）と同じとき（申込当日から利用を開始するとき）、
// 即日開始への同意（agreedToSameDayStart）は true であること
@TrueWhen(
    propertyPath = "agreedToSameDayStart",
    conditionPropertyPath = "startDate",
    conditionValuePropertyPath = "applicationDate"
)
```

---

## その他の機能

### conditionOperator — 条件の反転

| `ConditionOperator` | 意味 |
| ------------------- | ---- |
| `EQUAL_TO`（デフォルト） | 条件が成立するとき validate する |
| `NOT_EQUAL_TO` | 条件が成立しないとき validate する |

```java
// confirmed が true でないとき、reason は空でないこと
@NotEmptyWhen(
    propertyPath = "reason",
    conditionPropertyPath = "confirmed",
    conditionValueBoolean = true,
    conditionOperator = ConditionOperator.NOT_EQUAL_TO
)
```

### oppositeWhenConditionNotSatisfied — 条件不成立時に反対の検証を行う

`oppositeWhenConditionNotSatisfied` を `true` にすると、条件が成立しないときに、反対の内容を検証します。
デフォルトは `false` で、条件が成立しないときは検証しません。

| アノテーション | 条件が成立しないときの検証 |
| ------------- | -------------------------- |
| `@TrueWhen` | `false` であること |
| `@FalseWhen` | `true` であること |
| `@NullWhen` | `null` でないこと |
| `@NotNullWhen` | `null` であること |
| `@EmptyWhen` | 空でないこと |
| `@NotEmptyWhen` | 空であること |
| `@PatternWhen` | 正規表現に合致しないこと |
| `@NotPatternWhen` | 正規表現に合致すること |
| `@StringWhen` | 指定文字列のいずれでもないこと |
| `@NotStringWhen` | 指定文字列のいずれかであること |
| `@ValueOfPropertyPathWhen` | 別フィールドと異なること |
| `@NotValueOfPropertyPathWhen` | 別フィールドと同値であること |

```java
// sendsMail が true のとき mailAddress は空でなく、それ以外のときは空であること
@NotEmptyWhen(
    propertyPath = "mailAddress",
    conditionPropertyPath = "sendsMail",
    conditionValueBoolean = true,
    oppositeWhenConditionNotSatisfied = true
)
// → 「メールアドレス」は、「メール送信」が選択された場合は空欄以外、それ以外の値の場合は空欄にしてください
```

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
- `oppositeWhenConditionNotSatisfied` を `true` にした場合、「すべての条件が成立した」以外のとき
  （いずれか1つでも不成立のとき）に反対の内容を検証します。

### conditionValueDisplayStringPropertyPath — 条件値の表示名

`conditionValueString` / `conditionValuePropertyPath` を使う場合に、エラーメッセージに表示する条件値の代わりに、
別のフィールドの値を表示します。省略すると、条件値（指定した文字列や別フィールドの値）がそのまま表示されます。

表示する値は、`messages` / `item_names` / `enum_names` / `constants` の properties ファイルからキーとして探し、
キーがなければ値をそのまま表示します。

```java
@NotEmptyWhen(
    propertyPath = "cancelReason",
    conditionPropertyPath = "status",
    conditionValueString = "9",
    conditionValueDisplayStringPropertyPath = "cancelledStatusName"  // 値は "取消"
)
// 省略時 → 「ステータス」が「9」の場合は〜
// 指定時 → 「ステータス」が「取消」の場合は〜
```

条件値が複数ある場合は、表示名のフィールドを条件値と同じ数・同じ順番の配列にします。
複数の条件値をまとめた表示名を 1 つだけ表示したい場合は、配列にせず 1 つの値にすることもできます。
