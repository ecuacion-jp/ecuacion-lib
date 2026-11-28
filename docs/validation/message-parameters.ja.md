# MessageParameters

## 概要

`Violations.MessageParameters` は、エラーメッセージの生成方法を制御するパラメータクラスです。
`withMessageParameters()` でメソッドチェーン形式で設定できます。

```java
violations.withMessageParameters(p -> p
    .showsItemName(true)
    .messagePrefix("upload.row.prefix"));
```

同じ設定を複数の `Violations` で使う場合は、`Violations.newMessageParameters()` で作成したインスタンスを
`messageParameters(...)` で設定することもできます。現在の設定は `messageParameters()` で取得できます。

```java
Violations.MessageParameters params = Violations.newMessageParameters()
    .showsItemName(true)
    .messagePrefix("upload.row.prefix");

new Violations().messageParameters(params).validate(row1).throwIfAny();
```

`messageParameters(...)` はインスタンスをコピーせずにそのまま保持します。
そのため、設定後に `withMessageParameters()` などで変更すると、同じインスタンスを設定した他の `Violations` にも反映されます。

---

## showsItemName — 項目名あり/なしのメッセージ切り替え

バリデーションエラーのメッセージを `messages.properties` と
`messages_with_item_names.properties` のどちらから取得するかを制御します。

| 設定値 | 動作 |
| ------ | ---- |
| `null`（デフォルト） | `ExceptionUtil.getMessageList()` の `showsItemNameAsDefault` の値に従う |
| `true` | 項目名あり版のメッセージを使用 |
| `false` | 項目名なし版のメッセージを使用 |

以前の名前の `isMessageWithItemName` は 16.5.0 で非推奨になりました。動作は `showsItemName` と同じです。

`null` の場合のフォールバック先は `ExceptionUtil.getMessageList()` の `showsItemNameAsDefault` パラメータです。
設定ファイルなどで指定するものではなく、`getMessageList()` を呼び出すときの引数として渡します。
この引数を持たないオーバーロードを使った場合は `false` として扱われます。

```java
// showsItemName が null の違反は、項目名あり版のメッセージになる
List<String> messages = ExceptionUtil.getMessageList(violations, Locale.JAPANESE, true);
```

`showsItemName` に `true` / `false` を明示した場合は、`showsItemNameAsDefault` の値より優先されます。

詳細は **util &gt; ExceptionUtil** を参照してください。

```java
violations.withMessageParameters(p -> p.showsItemName(true));
```

**項目名あり版の例:**

```properties
# messages_with_item_names.properties
error.required = {item_name}は入力必須です
```

`showsItemName(true)` の場合、`{item_name}` にフィールドの項目名が埋め込まれます。
項目名の解決には Item / ItemUtil が使われます（詳細は util &gt; Item を参照）。

---

## showsItemNamePath — 項目名パスの表示

コレクション（リスト等）内の要素に対するバリデーションエラーで、
要素の位置情報（パス）をメッセージに含めるかを制御します。

| 設定値 | 表示例 |
| ------ | ------ |
| `false`（デフォルト） | `名前は入力必須です` |
| `true` | `リストの[1]番目の名前は入力必須です` |

```java
violations.withMessageParameters(p -> p.showsItemNamePath(true));
```

---

## messagePrefix / messagePostfix — メッセージの前後に追加テキスト

各エラーメッセージの前後に固定テキストを付加します。
Excelアップロードのバリデーションで「アップロードされたファイルにおいて、〇〇は必須です」のような表示に使います。

### String 版 — リテラルまたはキーで指定

`messagePrefix(String)` に渡した文字列は、まず `messages.properties` のキーとして解決されます。
キーが見つかればその値を、見つからなければ渡した文字列をそのまま使います。

```properties
# messages.properties
excel.upload.prefix = アップロードされたエクセルファイルにおいて、
```

```java
// キー指定（messages.properties から値を取得）
violations.withMessageParameters(p -> p.messagePrefix("excel.upload.prefix"));
// → "アップロードされたエクセルファイルにおいて、「氏名」は入力必須です"

// リテラル文字列（キーが見つからないのでそのまま使用）
violations.withMessageParameters(p -> p.messagePrefix("取込データにおいて、"));
// → "取込データにおいて、「氏名」は入力必須です"
```

### Arg 版 — プレースホルダーを含む動的な値

プレースホルダー（`{0}` など）を含む文言や、実行時の値を埋め込む場合は `Arg.message()` を使います。

```properties
# messages.properties
upload.error.row.prefix = {0}行目:
```

```java
violations.withMessageParameters(p -> p
    .messagePrefix(Arg.message("upload.error.row.prefix", rowNumber)));
// → "3行目: 「氏名」は入力必須です"
```

`messagePostfix` も同じ仕組みです。

---

## representativePropertyPath — まとまり全体を1つの項目に紐付ける

アップロードされたExcelファイルの中身のように、画面上に表示されない構造の奥で発生した違反は、
その`itemPropertyPaths`が画面上のどの項目にも対応しないことがあります。
`representativePropertyPath`は、各違反自身の`itemPropertyPaths`は変えずに、
このまとまり全体を表示上代表して紐付ける1つのプロパティパス（例: ファイルアップロード項目）を指定するためのものです。

```java
violations.withMessageParameters(p -> p.representativePropertyPath("fileToUpload"));
```

splibの画面エラー紐付けなどの利用側では、この値を使うことで、
違反自体はその項目のプロパティパスに紐づいていなくても、
代表項目（例: ファイルアップロード欄）をエラー表示（赤枠等）にすることができます。

### メッセージのプレースホルダーとしての利用

この値は、メッセージ本文を組み立てる際の`{representativePropertyPath}`という名前付きプレースホルダーとしても利用できます。
「詳細はファイルアップロード欄を確認してください」のようなメッセージで直接参照できます。

```properties
# ValidationMessages.properties
jp.ecuacion.ClassValidatorSample.message = {representativePropertyPath}に関連: {0}
```

違反の種類によって利用可否が異なります。

| 違反の種類 | 利用可否 |
| --- | --- |
| `ConstraintViolation`（`ValidationMessages*.properties`） | `representativePropertyPath`が設定されていれば常に利用可能 |
| `showsItemName(true)`の`BusinessViolation`（`messages_with_item_names.properties`） | 利用可能（`{item_name}`と同じ扱い） |
| `showsItemName(false)`の`BusinessViolation`（`messages.properties`） | 利用不可 — このファイルは位置引数（`{0}`、`{1}`等）のみを受け取る |

---

## 組み合わせ例

Excelファイルの検証で、行番号をprefixに付けつつ項目名を含めたメッセージを出す例です。

```java
for (int i = 0; i < rows.size(); i++) {
  int rowNum = i + 2; // ヘッダー行を除いた行番号
  new Violations()
      .validate(rows.get(i))
      .withMessageParameters(p -> p
          .showsItemName(true)
          .showsItemNamePath(true)
          .messagePrefix(rowNum + "行目: "))
      .throwIfAny();
}
```
