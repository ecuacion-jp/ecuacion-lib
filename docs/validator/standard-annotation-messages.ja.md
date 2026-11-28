# 標準アノテーションのメッセージ出力

## 概要

`@NotNull` などの Jakarta Validation 標準アノテーションによるバリデーションエラーは、
`Violations.validate()` と `MessageParameters` を組み合わせることで、
出力メッセージをさまざまな形式に変えられます。

以下の例では、すべて次のフォームクラスを使います。
また、出力例はすべて `ecuacion-lib-validation-business-messages` を dependency に追加している場合のものです。
追加していない場合は、ecuacion-lib-core（Hibernate Validator）のデフォルトの文言（`null は許可されていません` など）になります。

```java
class SomeForm {
    @NotNull
    private String name;
}
```

---

## 基本

```java
new Violations().validate(form).throwIfAny();
// → "入力必須です"
```

---

## 項目名をメッセージに含める

`showsItemName(true)` を指定すると `ValidationMessagesWithItemNames.properties` のメッセージが使用され、
`{0}` に項目名が埋め込まれます。

```properties
# item_names.properties
someForm.name = 名前
```

```java
new Violations().validate(form)
    .withMessageParameters(p -> p.showsItemName(true))
    .throwIfAny();
// → "「名前」は入力必須です"
```

項目名の解決の仕組みは [項目名の使い方](../messaging/item-name-in-message.md) を参照してください。

### ValidationMessagesWithItemNames.properties

項目名入りメッセージ用のプロパティファイルです。
`{0}` の位置に項目名（例: `「名前」`）が代入されます。

```properties
# ValidationMessagesWithItemNames.properties（ecuacion-lib-core のデフォルト）
jakarta.validation.constraints.NotNull.message.base = {0}にnullは許可されていません

# ValidationMessagesWithItemNames.properties（ecuacion-lib-validation-business-messages）
jakarta.validation.constraints.NotNull.message.default = {0}は入力必須です
```

ecuacion-lib-core が標準アノテーション用のデフォルトを提供しており、`ecuacion-lib-validation-business-messages` を追加するとビジネス向けの文言に置き換わります。
いずれの場合も、アプリ側でこのファイルを新たに作成する必要はありません。

この仕組みは標準アノテーションに限らず、ecuacion-lib-validation の独自バリデーターでも同じです（独自バリデーター用のデフォルトは ecuacion-lib-validation が提供しています）。

ファイル種別としての詳細は [PropertiesFileUtil > ValidationMessages](../properties-file-util/validation-messages.md) を参照してください。

### 項目名入りメッセージが使われる条件

`ValidationMessagesWithItemNames.properties` が使われるかどうかは、以下の優先順位で決まります。

1. `MessageParameters.showsItemName` が明示的に設定されている場合はその値
2. 未設定の場合は `ExceptionUtil.getMessageList()` の `showsItemNameAsDefault` フラグ

ecuacion-splib-web などのフレームワークは、画面表示用メッセージに対してこのフラグを `true` に設定します。

### アプリ側でのメッセージ上書き

アノテーションごとにメッセージを変えたい場合は、アプリの
`ValidationMessagesWithItemNames.properties` にサフィックスなしのプレーンキーで定義します。

```properties
# ValidationMessagesWithItemNames.properties（アプリ側）
jakarta.validation.constraints.NotNull.message = {0}を入力してください
```

`PropertiesFileUtil` のキー優先度（`key` > `key.default` > `key.base`）により、
プレーンキーが ecuacion-lib-core の `.base`・ecuacion-lib-validation-business-messages の `.default` より優先されます。

---

## 違反フィールドへのパスを含める

`showsItemNamePath(true)` を指定すると、違反フィールドに至るまでのパスがメッセージに付加されます。
以下の2つの情報が含まれます。

- **コレクション要素への違反**：何番目の要素かが含まれます
- **ネストしたオブジェクト**：経由した親オブジェクトの項目名が含まれます

`SomeForm` を List で保持する外側のクラスを例にします。

```java
class OuterForm {
    @Valid
    private List<SomeForm> forms;  // item_names.properties: outerForm.forms=フォームリスト
}
```

```java
// showsItemNamePath=false（デフォルト）
new Violations().validate(outerForm)
    .withMessageParameters(p -> p.showsItemName(true))
    .throwIfAny();
// → 「名前」は入力必須です

// showsItemNamePath=true
new Violations().validate(outerForm)
    .withMessageParameters(p -> p
        .showsItemName(true)
        .showsItemNamePath(true))
    .throwIfAny();
// → 「フォームリスト」の2番目の要素の「名前」は入力必須です
//    ↑ 親オブジェクト（コレクション）      ↑ フィールドの項目名
```

---

## メッセージにプレフィックスを付ける

アップロードされたファイルなど、検証したオブジェクトがどこから来たものかをメッセージに示したい場合に使います。

```java
new Violations().validate(form)
    .withMessageParameters(p -> p
        .showsItemName(true)
        .messagePrefix("アップロードされたエクセルファイルにおいて、"))
    .throwIfAny();
// → "アップロードされたエクセルファイルにおいて、「名前」は入力必須です"
```

何番目の要素で発生したかは、`showsItemNamePath` で表示できます。

---

## 組み合わせ例

アップロードされたエクセルファイルの内容を `OuterForm` として検証する例です。

```java
new Violations().validate(outerForm)
    .withMessageParameters(p -> p
        .showsItemName(true)
        .showsItemNamePath(true)
        .messagePrefix("アップロードされたエクセルファイルにおいて、"))
    .throwIfAny();
// → "アップロードされたエクセルファイルにおいて、「フォームリスト」の2番目の要素の「名前」は入力必須です"
```

`MessageParameters` の各オプションの詳細は **violation &gt; MessageParameters** を参照してください。
