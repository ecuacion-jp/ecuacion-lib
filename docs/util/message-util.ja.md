# MessageUtil

`MessageUtil`（`jp.ecuacion.lib.core.util.MessageUtil`）は
`Item` と `PropertiesFileUtil` を組み合わせたメッセージ構築のユーティリティクラスです。

主にフレームワーク内部から呼び出されますが、独自バリデーターのメッセージ生成で
`formatValuesWithResolution()` を使う場面があります。

---

## getItemNames()

```java
String result = MessageUtil.getItemNames(locale, itemList, showsItemNamePath, rootBean);
```

`Item` のリストから、表示用の項目名文字列を生成します。
複数項目がある場合はセパレーターで連結し、先頭を大文字化して返します。

### showsItemNamePath

`showsItemNamePath` が `true` の場合、項目名に親パスの名前が付加されます。

例えば `address.city` フィールドの場合：

| `showsItemNamePath` | 出力例 |
| --- | --- |
| `false` | 「市区町村」 |
| `true` | 「住所 > 市区町村」（※ `>` はセパレーター設定値） |

### コレクション要素の項目名

コレクション内の要素に対するバリデーションエラーでは、どの要素かを示す表現が自動付加されます。

| コレクション種別 | 出力例 |
| --- | --- |
| `List` | 「名前（1番目）」 |
| `Set` | 「名前（任意）」 |
| Map（キー） | 「名前（キー: abc）」 |
| Map（値） | 「名前（値: abc）」 |

### 表示記号のカスタマイズ

項目名の前後の記号・セパレーターは `messages.properties` で設定します。

| キー | デフォルト | 説明 |
| --- | --- | --- |
| `jp.ecuacion.lib.core.common.itemName.prependSymbol` | `「` | 項目名の前に付ける記号 |
| `jp.ecuacion.lib.core.common.itemName.appendSymbol` | `」` | 項目名の後に付ける記号 |
| `jp.ecuacion.lib.core.common.itemName.separator` | `、` | 複数項目名の区切り |
| `jp.ecuacion.lib.core.common.itemNamePath.string` | （パス形式） | `showsItemNamePath` 時のフォーマット |
| `jp.ecuacion.lib.core.common.itemNamePath.separator` | ` > ` | パス区切り記号 |

---

## formatValuesWithResolution()

```java
Arg arg = MessageUtil.formatValuesWithResolution(String[] values);
```

各文字列を `messages` → `item_names` → `enum_names` → `constants` の順にプロパティキーとして解決し、
表示記号で囲んでセパレーターで結合した `Arg` を返します。
キーが見つからない場合はリテラル文字列として扱われます。

```java
// enum_names.properties: status.active=Active, status.inactive=Inactive
Arg arg = MessageUtil.formatValuesWithResolution(
    new String[]{"status.active", "status.inactive"});
// resolves to "'Active', 'Inactive'"

// key not found: used as-is
Arg arg = MessageUtil.formatValuesWithResolution(new String[]{"ACTIVE", "INACTIVE"});
// resolves to "'ACTIVE', 'INACTIVE'"
```

独自バリデーターで条件値をエラーメッセージに表示する場合に使います。

---

## formatValues()

```java
Arg arg = MessageUtil.formatValues(String[] values);
```

各文字列をプロパティキー解決せずリテラルとして扱い、
表示記号で囲んでセパレーターで結合した `Arg` を返します。

```java
Arg arg = MessageUtil.formatValues(new String[]{"ACTIVE", "INACTIVE"});
// resolves to "'ACTIVE', 'INACTIVE'"
```

`formatValuesWithResolution()` との違いは、プロパティキー解決を行わない点のみです。
値が既に表示用の最終文字列である場合に使います。
