# ItemNameKey

## 概要

概要説明は [共通事項など > ItemNameKey](../concepts/item-and-name-key.md) もご確認ください。

`itemNameKey` は、項目の表示名を `item_names.properties` などから引くためのキーです。
形式は `"クラス部.フィールド部"` （例：`"user.name"`）です。

`Item` に明示的に指定しない限り、`itemPropertyPath` やクラス情報をもとに自動で決まります。

---

## itemPropertyPath からの自動決定

`itemNameKey()` を指定しない場合、`itemNameKey` は `itemPropertyPath` から以下のように決まります。

- **フィールド部**：`itemPropertyPath` の右端のノード
- **クラス部**：`itemPropertyPath` の右端から 2 番目のノード。`itemPropertyPath` がフィールド名のみ（クラス部にあたるノードがない）の場合は、`ItemContainer` のクラス名の先頭を小文字化したもの

以下、`ItemContainer` を実装するオブジェクトを `UserDto` とし、`UserDto` がフィールドとして `DeptDto`（フィールド名 `dept`）を保持しているものとします。

```java
public class UserDto implements ItemContainer {
    private String name;
    private DeptDto dept;   // DeptDto は name フィールドを持つ
}
```

| itemPropertyPath | 結果の itemNameKey |
| --- | --- |
| `"name"` | `"userDto.name"`<br>（クラス部にあたるノードがないため、`ItemContainer` のクラス名を使用） |
| `"dept.name"` | `"dept.name"`<br>（クラス部は `dept` ノードの名前をそのまま使用） |

itemPropertyPath が `dept.manager.name` のように複数階層になる場合、フィールド部には一番右のノード（`name`）、クラス部にはその一つ左のノード（`manager`）が使われます。

---

## itemNameKey の明示指定

`Item` の `itemNameKey()` を使って `itemNameKey` を明示的に指定することもできます。
明示的に指定した場合は、`itemPropertyPath` の内容によらず、指定した値が最優先で使われます。

```java
new Item("mobilePhoneNumber.value").itemNameKey("mobilePhone.number")
// → itemNameKey は "mobilePhone.number"
```

---

## itemNameKey の決定ルール（詳細）

`itemNameKey()` には、クラス部を省略してフィールド部のみを指定することもできます。
`"."` を含む場合は `"クラス部.フィールド部"` と解釈され、含まない場合はフィールド部のみと解釈されます。

```java
// フィールド部のみ指定（クラス部は自動解決）
new Item("name").itemNameKey("fullName")
```

このように、itemNameKey 及び itemPropertyPath にはクラス部の指定有無のパターンがあるため、クラス部とフィールド部はそれぞれ独立に、以下の優先順位で決定されます（1がない場合は2を使用、2がない場合は3を使用）。

1. `itemNameKey()` に指定された値を使用
2. `itemPropertyPath` を使用
3. （クラス部のみ）`ItemContainer` クラスのクラス名を使用

前項までの例も含めた具体的なパターンは以下のとおりです（`UserDto`・`DeptDto` の前提は前述と同じ）。

| itemNameKey() の指定 | itemPropertyPath | 結果の itemNameKey |
| --- | --- | --- |
| `"user.name"`<br>（クラス部+フィールド部） | 任意 | `"user.name"`<br>（itemPropertyPath の内容によらずそのまま採用） |
| `"fullName"`<br>（フィールド部のみ） | `"dept.name"` | `"dept.fullName"`<br>（クラス部は `dept` ノードの名前をそのまま使用） |
| `"fullName"`<br>（フィールド部のみ） | `"name"` | `"userDto.fullName"`<br>（クラス部は `ItemContainer` のクラス名から解決） |
| 未指定 | `"name"` | `"userDto.name"`<br>（フィールド部は右端ノード、クラス部は `ItemContainer` のクラス名） |
| 未指定 | `"dept.name"` | `"dept.name"`<br>（クラス部は `dept` ノードの名前をそのまま使用、フィールド部は右端ノード） |

---

## `@ItemNameKeyClass` アノテーション

前述のとおり、`itemNameKey()` や `itemPropertyPath` でクラス部が指定されなかった場合、デフォルトで `userDto` が itemNameKey のクラス部として使われることを説明しました。
しかし `item_names.properties` には、通常 `userDto.name` ではなく `user.name` のような一般的な形で定義することが多いはずです。
このデフォルトのクラス部 `userDto` は、`@ItemNameKeyClass`（`jp.ecuacion.lib.core.annotation.ItemNameKeyClass`）を使うことで `user` に変更できます。

`@ItemNameKeyClass` は、クラスに対して `itemNameKey` のクラス部を一括指定するアノテーションです。
指定しない場合、クラス部は前述のとおりクラス名の先頭を小文字化した文字列になります。

```java
@ItemNameKeyClass("user")
public class UserDto implements ItemContainer {
    private String name;    // itemNameKey: "user.name"
    private String email;   // itemNameKey: "user.email"
}
```

### フィールドへの指定

`@ItemNameKeyClass` はフィールドにも指定できます。
itemPropertyPath がネストしている場合、クラス部にはフィールド名がそのまま使われますが、
`List<DeptDto> deptList` のようなフィールドでは、クラス部が `deptList` となり `deptList.name` を `item_names.properties` に定義する必要が出てしまいます。
このような場合、フィールドに `@ItemNameKeyClass` を指定すると、そのフィールド配下の項目のクラス部を一括で変更できます。

```java
public class UserDto implements ItemContainer {
    @ItemNameKeyClass("dept")
    private List<DeptDto> deptList;   // deptList[0].name の itemNameKey: "dept.name"

    private DeptDto belongingDept;    // belongingDept.name の itemNameKey: "belongingDept.name"
}
```

フィールドへの指定は、itemPropertyPath のクラス部（右端から2番目のノード）に対応するフィールドに対してのみ有効です。
また、`itemNameKey()` でクラス部を明示的に指定した場合はそちらが優先されます。

itemPropertyPath のノードがフィールドではなく getter に解決される場合（同名のフィールドが存在しない場合）は、getter に `@ItemNameKeyClass` を指定できます。
同名のフィールドが存在する場合はフィールドが使われるため、getter に指定した `@ItemNameKeyClass` は無視されます。

```java
public class UserDto implements ItemContainer {
    @ItemNameKeyClass("dept")
    public List<DeptDto> getDeptList() { ... }   // deptList[0].name の itemNameKey: "dept.name"
}
```

---

## item_names.properties との対応

解決された `itemNameKey` を使って、`item_names.properties` から項目名が取得されます。

```properties
# item_names.properties
userDto.name=氏名
userDto.email=メールアドレス
user.name=氏名
```
