# prefix・postfix・separator

## 概要

項目名をエラーメッセージに表示する際、前後に記号を付けたり（prefix/postfix）、
複数の項目名を並べる際の区切り文字（separator）をカスタマイズできます。

デフォルト（日本語）では `「名前」` のように `「」` で囲まれます。

項目名入りのメッセージにする設定（`showsItemName`）方法の詳細は [MessageParameters > showsItemName](../validation/message-parameters.md#showsitemname--項目名ありなしのメッセージ切り替え) を参照してください。

---

## デフォルト値

| 設定 | プロパティキー | デフォルト値（ja） | デフォルト値（en） |
| --- | --- | --- | --- |
| prefix（前置記号） | `...itemName.prependSymbol` | `「` | `'` |
| postfix（後置記号） | `...itemName.appendSymbol` | `」` | `'` |
| separator（区切り） | `...itemName.separator` | `、` | `, ` |

プロパティキーのプレフィックスは `jp.ecuacion.lib.core.common` です。
その他の対応言語も、各言語の表記に合わせたデフォルト値を持っています（例: フランス語は `« »`、ドイツ語は `„ “`）。

---

## カスタマイズ方法

`messages.properties`（または `messages_ja.properties`）にキーを追加します。

```properties
# 【 】で囲む場合
jp.ecuacion.lib.core.common.itemName.prependSymbol=【
jp.ecuacion.lib.core.common.itemName.appendSymbol=】

# 区切り文字を変更する場合
jp.ecuacion.lib.core.common.itemName.separator=・
```

---

## 複数項目名の区切り

`@AnyNotEmpty` など複数フィールドにまたがるバリデーションアノテーションでは、
複数の項目名が separator で結合されて表示されます。

デフォルト（日本語）:

```
「名前」、「メールアドレス」のいずれかを入力してください
```

`separator` を `"・"` に変更した場合:

```
「名前」・「メールアドレス」のいずれかを入力してください
```

---

## 値の prefix・postfix・separator

エラーメッセージに値（`{invalidValue}` など）を含む場合、値にも同様の記号が付きます。
値用の設定は `jp.ecuacion.lib.core.common.value.*` キーで管理されます。
日本語専用のデフォルト定義はないため、日本語環境でも英語デフォルトの `'...'`（シングルクォート）が使われます。

| キー | デフォルト値 |
| --- | --- |
| `...value.prependSymbol` | `'` |
| `...value.appendSymbol` | `'` |
| `...value.separator` | `,` |

`「」` に変更したい場合は `messages_ja.properties` に定義します。

---

## 詳細リファレンス

- コレクション要素の表示カスタマイズ → **[List・Set・Map の項目名](collection-item-name.md)**
- itemNamePath の区切り文字 → **[itemNamePath](item-name-path.md)**
