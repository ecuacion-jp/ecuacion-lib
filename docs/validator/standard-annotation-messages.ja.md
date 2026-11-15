# 標準アノテーションのメッセージ出力

## 概要

`@NotNull` などの Jakarta Validation 標準アノテーションによるバリデーションエラーは、
`Violations.validate()` と `MessageParameters` を組み合わせることで、
出力メッセージをさまざまな形式に変えられます。

以下の例では、すべて次のフォームクラスを使います。

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

`isMessageWithItemName(true)` を指定すると `ValidationMessagesWithItemNames.properties` のメッセージが使用され、
`{0}` に項目名が埋め込まれます。

```properties
# ValidationMessagesWithItemNames.properties
jakarta.validation.constraints.NotNull.message = {0}にnullは許可されていません
```

```properties
# item_names.properties
someForm.name = 名前
```

```java
new Violations().validate(form)
    .withMessageParameters(p -> p.isMessageWithItemName(true))
    .throwIfAny();
// → "名前は入力必須です"
```

項目名の解決の仕組みは [項目名の使い方](../messaging/item-name-in-message.md) を参照してください。

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
    .withMessageParameters(p -> p.isMessageWithItemName(true))
    .throwIfAny();
// → 「名前」にnullは許可されていません

// showsItemNamePath=true
new Violations().validate(outerForm)
    .withMessageParameters(p -> p
        .isMessageWithItemName(true)
        .showsItemNamePath(true))
    .throwIfAny();
// → 「フォームリスト」の2番目の要素の「名前」にnullは許可されていません
//    ↑ 親オブジェクト（コレクション）      ↑ フィールドの項目名
```

---

## メッセージにプレフィックスを付ける

Excel ファイルのバリデーションなど、行番号をエラーメッセージに付けたい場合に使います。

```java
new Violations().validate(form)
    .withMessageParameters(p -> p
        .isMessageWithItemName(true)
        .messagePrefix("3行目: "))
    .throwIfAny();
// → "3行目: 名前は入力必須です"
```

---

## 組み合わせ例

`OuterForm` の一覧を行ごとに検証する例です。

```java
List<OuterForm> rows = ...;
for (int i = 0; i < rows.size(); i++) {
    int rowNum = i + 2; // ヘッダー行を除いた行番号
    new Violations()
        .validate(rows.get(i))
        .withMessageParameters(p -> p
            .isMessageWithItemName(true)
            .showsItemNamePath(true)
            .messagePrefix(rowNum + "行目: "))
        .throwIfAny();
}
// → "3行目: 「フォームリスト」の2番目の要素の「名前」にnullは許可されていません"
```

`MessageParameters` の各オプションの詳細は **violation &gt; MessageParameters** を参照してください。
