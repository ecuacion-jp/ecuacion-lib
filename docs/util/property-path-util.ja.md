# PropertyPathUtil

`PropertyPathUtil`（`jp.ecuacion.lib.core.util.PropertyPathUtil`）は
propertyPath 文字列の操作と、propertyPath を使ったオブジェクトグラフのナビゲーションを提供します。

propertyPath とは、jakarta validation の `ConstraintViolation` が使用するパス形式で、
`"address.city"` や `"items[0].name"` のような文字列です。

---

## propertyPath 文字列の操作

```java
// 末尾ノードを取得
String node = PropertyPathUtil.getRightMostNode("address.city"); // "city"
String node2 = PropertyPathUtil.getRightMostNode("items[0].<list element>"); // "items[0].<list element>"

// 末尾ノードを除いたパスを取得（leafBean パス）
String parent = PropertyPathUtil.getPropertyPathWithoutRightMostNode("address.city"); // "address"
String empty = PropertyPathUtil.getPropertyPathWithoutRightMostNode("fieldName");     // ""

// ノード一覧を取得
List<String> nodes = PropertyPathUtil.getNodeList("address.city"); // ["address", "city"]

// コレクション記法を完全除去してフィールドパスに変換（itemNameKey 取得のために使用）
String fieldPath = PropertyPathUtil.toFieldPath("items[0].<list element>"); // "items"
String fieldPath2 = PropertyPathUtil.toFieldPath("userList[1].name");       // "userList.name"

// インデックスのみ除去してインデックスなしの正規パスに変換（customizedItems() とのマッチングに使用）
String idxless = PropertyPathUtil.toIndexlessPath("items[1].<list element>"); // "items[]"
String idxless2 = PropertyPathUtil.toIndexlessPath("userList[1].name");        // "userList[].name"
```

---

## propertyPath によるフィールド取得

ドット区切りのネストしたパスに対応します。
内部で `ReflectionUtil.getDeclaredField` を使用してクラス階層を検索します。

```java
// ネストしたフィールドを取得
Field city = PropertyPathUtil.getField(MyBean.class, "address.city");

// 単純フィールドも可
Field name = PropertyPathUtil.getField(MyBean.class, "name");
```

---

## propertyPath によるプロパティ取得（フィールド、またはgetter）

`getField` と同様にドット区切りのパスを辿りますが、各ノードはフィールドが存在しない場合に getter としても解決されます
（`ReflectionUtil.getBeanProperty` を参照）。

```java
BeanProperty city = PropertyPathUtil.getBeanProperty(MyBean.class, "address.city");
Class<?> type = city.getType();
```

---

## propertyPath によるクラス取得

プロパティパスを辿って、対象プロパティの型を返します。
各ノードは、フィールドが存在しない場合は getter として解決されます。
コレクション（`List`, `Set`, `Map`）のジェネリック型パラメータにも対応しています。

```java
// MyBean.address.city フィールドの型を返す
Class<?> cls = PropertyPathUtil.getClass(MyBean.class, "address.city");

// List<Book> bookList の場合、Book.class を返す
Class<?> bookCls = PropertyPathUtil.getClass(MyBean.class, "bookList[0]");

// パスが空の場合は rootBeanClass 自身を返す
Class<?> self = PropertyPathUtil.getClass(MyBean.class, "");
```

---

## プロパティ値の取得

各ノードは、まず同名のフィールドとして解決され、フィールドが存在しない場合は getter（`getXxx()`、またはプリミティブの `boolean` を返す `isXxx()`。`Boolean` を返す `isXxx()` は対象外）として解決されます。
getter として解決された場合は、その戻り値が使われます。

```java
MyBean bean = new MyBean();

// シンプルなフィールド
Object value = PropertyPathUtil.getValue(bean, "fieldName");

// ネストしたパス
Object city = PropertyPathUtil.getValue(bean, "address.city");

// 配列・List の要素
Object item = PropertyPathUtil.getValue(bean, "items[0]");
Object element = PropertyPathUtil.getValue(bean, "strList[0].<list element>");

// Map の値・キー
Object value = PropertyPathUtil.getValue(bean, "map[key1].<map value>");
Object key = PropertyPathUtil.getValue(bean, "map<K>[key1].<map key>");
```

Jakarta Validation の propertyPath と同じ書き方（`<list element>` など）に対応しています。

Map のキーは、各エントリのキーの `String.valueOf(key)` と比較して特定します（Jakarta Validation が propertyPath にキーの `toString()` を使うため）。
一致するキーがない場合は `null` を返し、同じ文字列表現のキーが複数ある場合は `ElementOfCollectionCannotBeObtainedException` をスローします。

Set など、propertyPath（`set[].<iterable element>` など）から要素を特定できないコレクションは、`ElementOfCollectionCannotBeObtainedException` をスローします。

途中の値が `null` の場合（`"address.city"` の `address`、`"items[0]"` の `items` が `null` など）は、`null` を返します。
クラスレベルバリデータ（`@XxxWhen` など）の `propertyPath` も同じ扱いで、値が `null` として検証されます。

### leafBean の取得

propertyPath の直接の親オブジェクト（leafBean）を取得します。

```java
// "address.city" の場合、address オブジェクトを返す
Object leafBean = PropertyPathUtil.getLeafBean(rootBean, "address.city");

// 親ノードがない場合（"fieldName" のみ）は rootBean をそのまま返す
Object same = PropertyPathUtil.getLeafBean(rootBean, "fieldName");
```

---

## コレクション定数

コレクション要素を表す propertyPath ノードの文字列定数が定義されています。

| 定数 | 値 | 対象 |
|---|---|---|
| `EL_LIST` | `<list element>` | List 要素 |
| `EL_SET` | `<iterable element>` | Set 要素 |
| `EL_MAP_KEY` | `<map key>` | Map キー |
| `EL_MAP_VAL` | `<map value>` | Map 値 |
