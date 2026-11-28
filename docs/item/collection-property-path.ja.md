# collectionにおけるpropertyPath

## 概要

このページでは、List・Set・Map といったコレクション型のフィールドに対する `itemPropertyPath` の書き方（省略形を含む）について説明します。
配列の扱いについては、末尾の「[配列の場合](#配列の場合)」を参照してください。
`itemPropertyPath` そのものの基本的な考え方は [Itemと関連用語](../concepts/item-terms.md) を参照してください。

---

## Jakarta Validation 標準における collection の propertyPath

Jakarta Validation（Hibernate Validator）は、List・Set・Map の要素そのものに制約を課す場合、通常のフィールド名の代わりに専用のキーワードを使って要素を表します。

| 型 | 要素を表すキーワード |
| --- | --- |
| `List<T>` | `<list element>` |
| `Set<T>` | `<iterable element>` |
| `Map<K, V>` のキー | `<map key>` |
| `Map<K, V>` の値 | `<map value>` |

例えば `List<String> strList` の要素そのもの（`String` 自体）に制約がある場合、propertyPath はインデックス付きの角括弧の後にこのキーワードが続く形（`strList[0].<list element>`）になります。
Set はコレクションが順序を持たないため、実行時のパスにもインデックスが付きません（`strSet[].<iterable element>`）。
Map のキー制約には `strMap<K>[].<map key>` のようにキーアクセスを示す修飾子が付き、値制約は `strMap[key1].<map value>` のようにキー値を使った形になります。

---

## collection に対する itemPropertyPath 指定

`new Item()` に渡す `itemPropertyPath` には、上記の Jakarta Validation 標準の propertyPath の形式（以降「propertyPath 準拠形式」）をそのまま指定できます。
バリデーション違反の `ConstraintViolation#getPropertyPath()` に出力される文字列と同じ形なので、ログなどで確認したパスをそのまま使えます。

```java
public class BookshelfForm implements ItemContainer {

    private List<@NotNull String> tagList;

    @Valid
    private List<Book> bookList;

    @Valid
    private List<List<Book>> shelfList;

    @Valid
    private Map<String, Author> authorMap;

    @Override
    public Item[] customizedItems() {
        return new Item[] {
            // List の要素（Book）のフィールド
            new Item("bookList[0].title").itemNameKey("book.title"),
            // List の要素の List の要素（Book）のフィールド
            new Item("shelfList[0].<list element>[1].title").itemNameKey("book.title"),
            // Map の値（Author）のフィールド
            new Item("authorMap[key1].name").itemNameKey("author.name"),
            // 要素そのもの（tagList の各 String）は、コレクションのフィールドで指定する（下記注記参照）
            new Item("tagList").itemNameKey("bookshelf.tag"),
        };
    }
}
```

インデックス（`[0]`）や Map のキー（`[key1]`）の値は、`Item` に格納される際の正規化で取り除かれます。
そのため上の例の `new Item("bookList[0].title")` は 0 番目の要素だけでなく、`bookList` の**すべての要素**の `title` に適用されます。
`new Item("authorMap[key1].name")` も同様に、キーの値に関係なく `authorMap` のすべての値の `name` に適用されます。

!!! note "要素そのものへの違反の場合"
    `List<@NotNull String>` の要素や Map のキー・値そのものなど、**要素そのもの**への違反でエラーメッセージを生成する際は、
    要素のパス（`tagList[0].<list element>` など）ではなく、**コレクションのフィールド**（`tagList`）に設定した `Item` が使われます。
    項目名にはコレクションの項目名に「1番目の要素」などの情報が付加されて表示されます（[List・Set・Map の項目名](../messaging/collection-item-name.md) 参照）。
    そのため、要素そのものの項目名などを設定したい場合は、上の例のように `new Item("tagList")` に設定してください。

---

## itemPropertyPath としての省略形

`itemPropertyPath` は、前章の propertyPath 準拠形式のほか、インデックスや要素を表すキーワード（`<list element>` など）を省いた省略形でも指定できます。
内部で正規化されるため、どちらを渡しても同じ結果になります。

```java
// どちらも同じ意味
new Item("bookList[1].title")   // propertyPath 準拠形式（インデックスあり）
new Item("bookList[].title")    // 省略形
```

以下の表に、コレクション型ごとの propertyPath 準拠形式と省略形をまとめます。
フィールド名はすべて例示用です。`User` は `name` フィールドを持つクラスとします。
「対象」が要素そのもの（`User.name` 以外）の行は、前章の注記のとおり、エラーメッセージの生成ではコレクションのフィールドに設定した `Item` が使われます。

### List

| フィールドの型 | 対象 | propertyPath 準拠形式 | 省略形 |
| --- | --- | --- | --- |
| `List<String> strList` | 要素そのもの | `strList[0].<list element>` | `strList[]` |
| `List<User> userList` | `User.name` | `userList[0].name` | `userList[].name` |
| `List<List<String>> nestedList` | 内側の String 要素 | `nestedList[0].<list element>[1].<list element>` | `nestedList[][]` |
| `List<List<User>> nestedList` | 内側の `User.name` | `nestedList[0].<list element>[1].name` | `nestedList[][].name` |

### Set

Set は順序を持たないため、propertyPath 準拠形式でもインデックスは付きません（`[]` のまま）。

| フィールドの型 | 対象 | propertyPath 準拠形式 | 省略形 |
| --- | --- | --- | --- |
| `Set<String> strSet` | 要素そのもの | `strSet[].<iterable element>` | `strSet[]` |
| `Set<User> userSet` | `User.name` | `userSet[].name` | `userSet[].name`（同一） |
| `Set<Set<String>> nestedSet` | 内側の String 要素 | `nestedSet[].<iterable element>[].<iterable element>` | `nestedSet[][]` |
| `Set<Set<User>> nestedSet` | 内側の `User.name` | `nestedSet[].<iterable element>[].name` | `nestedSet[][].name` |

### Map（キー）

キーを表すノードには `<K>` が付き、値の `itemPropertyPath` とは区別されます。
propertyPath 準拠形式の `[ ]` の中にはキーの `toString()` の値が入ります（下表では `key1`・`key2` と表記。キーが `null` や空文字の場合は `[]`）。

| フィールドの型 | 対象 | propertyPath 準拠形式 | 省略形 |
| --- | --- | --- | --- |
| `Map<String, ?> strMap` | キーそのもの | `strMap<K>[key1].<map key>` | `strMap<K>[]` |
| `Map<User, ?> userMap` | キーの `User.name` | `userMap<K>[key1].name` | `userMap<K>[].name` |
| `Map<Map<String, ?>, ?> nestedMap` | 内側の Map のキーそのもの | `nestedMap<K>[key1].<map key><K>[key2].<map key>` | `nestedMap<K>[]<K>[]` |
| `Map<Map<User, ?>, ?> nestedMap` | 内側の Map のキーの `User.name` | `nestedMap<K>[key1].<map key><K>[key2].name` | `nestedMap<K>[]<K>[].name` |

### Map（値）

| フィールドの型 | 対象 | propertyPath 準拠形式 | 省略形 |
| --- | --- | --- | --- |
| `Map<?, String> strMap` | 値そのもの | `strMap[key1].<map value>` | `strMap[]` |
| `Map<?, User> userMap` | 値の `User.name` | `userMap[key1].name` | `userMap[].name` |
| `Map<?, Map<?, String>> nestedMap` | 内側の Map の値そのもの | `nestedMap[key1].<map value>[key2].<map value>` | `nestedMap[][]` |
| `Map<?, Map<?, User>> nestedMap` | 内側の Map の値の `User.name` | `nestedMap[key1].<map value>[key2].name` | `nestedMap[][].name` |

### 複合パターン

List・Set・Map を組み合わせた場合も、外側から順に各コレクションの形式をつなげた形になります。
省略形では、List・Set・Map（値）の区別はなくなり、いずれも `[]` になります（Map のキーのみ `<K>[]` で区別されます）。

| フィールドの型 | 対象 | propertyPath 準拠形式 | 省略形 |
| --- | --- | --- | --- |
| `Map<?, List<User>> map` | 値の List の `User.name` | `map[key1].<map value>[1].name` | `map[][].name` |
| `List<Map<String, ?>> list` | 要素の Map のキーそのもの | `list[0].<list element><K>[key1].<map key>` | `list[]<K>[]` |
| `List<Set<String>> list` | 要素の Set の String 要素 | `list[0].<list element>[].<iterable element>` | `list[][]` |
| `Map<?, Map<?, List<User>>> map` | 値の Map の値の List の `User.name` | `map[key1].<map value>[key2].<map value>[1].name` | `map[][][].name` |

### 重複登録について

異なるインデックスを持つパスは正規化後に同一キーとなるため、同じ `customizedItems()`
内で重複登録すると実行時に例外が発生します。

```java
// NG: 正規化後に両方とも "userList[].name" になり重複
new Item("userList[1].name"),
new Item("userList[2].name")
```

---

## 配列の場合

配列は List・Set・Map と扱いが異なり、Jakarta Validation でできることが限られます。

### 配列そのものへの制約

`@NotNull`・`@Size` などを配列のフィールドに付けると、配列そのものに対する制約として検証されます。
`@Size` の場合は配列の長さが判定対象になります。propertyPath はフィールド名そのもの（例: `strArray`）です。

### 要素への制約は検証されない

`@NotNull String[] strArray` のように書いても、配列の**要素**に対する制約としては検証されません
（上記のとおり、配列そのものへの `@NotNull` としてのみ扱われます）。

Jakarta Validation の要素への制約（container element constraints）は、`List`・`Map`・`Optional` などの
ジェネリックなコンテナの**型引数**に付ける仕組みとして定義されており、配列は対象外のためです
（[Jakarta Validation 3.1 仕様 5.5 Container element constraints](https://jakarta.ee/specifications/bean-validation/3.1/jakarta-validation-spec-3.1.html#constraintdeclarationvalidationprocess-containerelementconstraints) 参照）。
要素を検証したい場合は、`List<@NotNull String>` のように `List` などのコレクションを使ってください。

### Bean の配列（`@Valid`）

要素が Bean の配列は、フィールドに `@Valid` を付けることで、各要素の Bean のフィールドに付けた制約が検証されます
（配列への `@Valid` によるカスケードは Jakarta Validation の仕様で明記されています）。
配列は型引数を持たないため、`@Valid` はフィールド自体に付けます。

propertyPath・`itemPropertyPath` は、`List<User>` の場合と同じ形式になります。

| フィールドの型 | 対象 | propertyPath 準拠形式 | 省略形 |
| --- | --- | --- | --- |
| `@Valid User[] userArray` | `User.name` | `userArray[0].name` | `userArray[].name` |
