# collectionにおけるpropertyPath

## 概要

このページでは、List・Set・Map といったコレクション型のフィールドに対する `itemPropertyPath` の書き方（省略形を含む）について説明します。
`itemPropertyPath` そのものの基本的な考え方は [itemとItemContainer・itemPropertyPath](item-property-path.md) を参照してください。

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

## itemPropertyPath としての省略形

`new Item()` に渡す `itemPropertyPath` は、上記の propertyPath 準拠形式（インデックスあり）と、インデックスを省いた省略形のどちらでも指定できます。
内部で正規化されるため、どちらを渡しても同じ結果になります。

```java
// どちらも同じ意味
new Item("bookList[1].title")   // propertyPath 準拠形式（インデックスあり）
new Item("bookList[].title")    // 省略形
```

以下の表に、コレクション型ごとの propertyPath 準拠形式と省略形をまとめます。
フィールド名はすべて例示用です。`User` は `name` フィールドを持つクラスとします。

### List

| フィールドの型 | 対象 | propertyPath 準拠形式 | 省略形 |
| --- | --- | --- | --- |
| `List<String> strList` | 要素そのもの | `strList[0].<list element>` | `strList[]` |
| `List<User> userList` | `User.name` | `userList[0].name` | `userList[].name` |
| `List<List<String>> nestedList` | 内側の String 要素 | `nestedList[0].<list element>[1].<list element>` | `nestedList[][]` |
| `List<List<User>> nestedList` | 内側の `User.name` | `nestedList[0].<list element>[1].name` | `nestedList[][].name` |

### Set

正規化前後で形式が変わらないケースが多くなります。

| フィールドの型 | 対象 | propertyPath 準拠形式 | 省略形 |
| --- | --- | --- | --- |
| `Set<String> strSet` | 要素そのもの | `strSet[].<iterable element>` | `strSet[]` |
| `Set<User> userSet` | `User.name` | `userSet[].name` | `userSet[].name`（同一） |

### Map

キーと値の `itemPropertyPath` は区別されます。

| フィールドの型 | 対象 | propertyPath 準拠形式 | 省略形 |
| --- | --- | --- | --- |
| `Map<String, ?> strMap` | キーそのもの | `strMap<K>[].<map key>` | `strMap<K>[]` |
| `Map<?, String> strMap` | 値そのもの | `strMap[key1].<map value>` | `strMap[]` |
| `Map<?, User> strMap` | 値の `User.name` | `strMap[key1].name` | `strMap[].name` |

### 重複登録について

異なるインデックスを持つパスは正規化後に同一キーとなるため、同じ `customizedItems()`
内で重複登録すると実行時に例外が発生します。

```java
// NG: 正規化後に両方とも "userList[].name" になり重複
new Item("userList[1].name"),
new Item("userList[2].name")
```
