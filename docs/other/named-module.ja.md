# named module 環境での利用

## 概要

`ecuacion-lib` の一部の機能は、アプリ側のクラス（Record や Form など）のフィールド・getter・メソッドに
リフレクションでアクセスします。

Spring Boot の fat JAR など、`module-info.java` を使わない unnamed module 環境では、特に設定は不要です。
`module-info.java` を使った named module 環境では、対象クラスのパッケージを
`ecuacion-lib` のモジュールに `opens` する必要があります。

---

## リフレクションを使用する機能

| 機能 | アクセス元モジュール | `opens` がない場合 |
| ---- | ------------------- | ----------------- |
| `propertyPath` を指定するクラスバリデーター（比較バリデーター、条件付きバリデーター、コレクション・アサーションなど）による値の取得 | `jp.ecuacion.lib.core` | 例外が発生する |
| `@ValueOfPropertyPathWhen` / `@NotValueOfPropertyPathWhen` による値の取得 | `jp.ecuacion.lib.core` | 例外が発生する |
| エラーメッセージの項目名の解決（ネストした項目の場合） | `jp.ecuacion.lib.core` | 例外が発生する |
| `@ReturnTrue` によるメソッドの呼び出し | `jp.ecuacion.lib.validation` | 例外が発生する |
| `ItemContainer` の `customizedItems()` の呼び出し（親クラスからの継承を含む） | `jp.ecuacion.lib.core` | 例外が発生する（`opens` ではなく `exports` のみの場合は、例外にはならないが親クラスからの継承が効かない） |

値の取得では、`private` なフィールドにもアクセスできるよう `setAccessible(true)` を使用しています。
そのため、`opens` されていないパッケージのクラスに対しては `InaccessibleObjectException` が発生します。

---

## module-info.java の設定

対象クラスのパッケージを、使用する `ecuacion-lib` のモジュールに `opens` します。

```java
module com.example.myapp {
    requires jp.ecuacion.lib.core;
    requires jp.ecuacion.lib.validation;

    opens com.example.myapp.record to
        org.hibernate.validator, jp.ecuacion.lib.core, jp.ecuacion.lib.validation;
}
```

- `jp.ecuacion.lib.validation` への `opens` は、`@ReturnTrue` を使用する場合のみ必要です。
- `org.hibernate.validator` への `opens` は、`ecuacion-lib` に関係なく、named module 環境で
  Jakarta Validation（Hibernate Validator）を使用する場合に必要です。

`.properties` ファイルの読み込みに関する `module-info.java` の設定については、
[SPI](spi.md) を参照してください。
