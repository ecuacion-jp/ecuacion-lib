# ItemContainer

## 概要

概要説明は [共通事項など > itemとItemContainer・itemPropertyPath](item-property-path.md) もご確認ください。

`ItemContainer`（`jp.ecuacion.lib.core.item.ItemContainer`）は、
フィールドの表示属性をカスタマイズした `Item` を保持するインターフェースです。

Record や Form などのクラスに実装することで、そのクラスに属するフィールドの項目名キー（`itemNameKey`）や値の表示/非表示を一元管理できます。

---

## 実装方法

`ItemContainer` を実装するには `customizedItems()` を定義します。

```java
public class UserRecord implements ItemContainer {

    private String name;
    private String password;

    @Override
    public Item[] customizedItems() {
        return new Item[] {
            new Item("password").hideValue(),
        };
    }
}
```

カスタマイズが不要なフィールドは `customizedItems()` に含めなくて構いません。
カスタマイズ済み `Item` が存在しなかった場合は、
デフォルト値で自動生成された `Item` が内部的に使用されます。

カスタマイズが一切不要な場合は空配列を返します。

```java
@Override
public Item[] customizedItems() {
    return new Item[] {};
}
```

---

## 親クラスからの設定継承

`ItemContainer` を実装したクラスが `customizedItems()` をオーバーライドした場合、
`getItem()`（内部的に使用される、Item 取得用のメソッド）はクラス階層を上に辿りながら同じ `itemPropertyPath` を持つ `Item` をマージします。
子クラスで明示的に設定されたプロパティが優先され、未設定のプロパティは親クラスの設定を引き継ぎます。

```java
// 親クラス：itemNameKey と hideValue を設定
public class UserRecord implements ItemContainer {
    private String name;
    private String password;

    @Override
    public Item[] customizedItems() {
        return new Item[] {
            new Item("name").itemNameKey("user.name"),
            new Item("password").itemNameKey("user.password").hideValue(),
        };
    }
}

// 子クラス：画面によって name の itemNameKey を上書き
public class EditUserRecord extends UserRecord {
    @Override
    public Item[] customizedItems() {
        return new Item[] {
            new Item("name").itemNameKey("editUser.name"),
        };
    }
}
```

`EditUserRecord` のインスタンスで `getItem("name")` を呼ぶと：

- `itemNameKey` → `"editUser.name"`（子クラスの設定を優先）
- `showsValue` → `true`（親クラスでも設定されていないためデフォルト値）

`getItem("password")` を呼ぶと：

- `itemNameKey` → `"user.password"`（親クラスから継承）
- `showsValue` → `false`（親クラスの `hideValue()` を継承）

子クラスで一切 `customizedItems()` をオーバーライドしない場合は、
単純に親クラスの設定がそのまま使われます。

> **モジュール制約**：この機能は Java の `MethodHandles` を用いて各クラス階層の
> `customizedItems()` を個別に呼び出すため、named module 環境では `opens` の設定が必要です。
> 詳細は [named module 環境での利用](../other/named-module.md) を参照してください。

---

## mergeItems()

共通の `Item[]` と Record 固有の `Item[]` を結合するユーティリティメソッドです。

```java
public class UserRecord implements ItemContainer {

    private static final Item[] COMMON_ITEMS = new Item[] {
        new Item("createdAt").itemNameKey("common.createdAt"),
        new Item("updatedAt").itemNameKey("common.updatedAt"),
    };

    @Override
    public Item[] customizedItems() {
        return mergeItems(COMMON_ITEMS, new Item[] {
            new Item("password").hideValue(),
        });
    }
}
```

同じ `itemPropertyPath` を持つ `Item` が両方の配列に含まれると `RuntimeException` がスローされます。

