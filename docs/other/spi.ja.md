# SPI

## 概要

SPI（Service Provider Interface）とは、Java のモジュールシステムでリソースの提供者を宣言する仕組みです。

`jp.ecuacion.lib.core.spi` パッケージは、**Java 9 モジュールシステム（JPMS）使用時**に
`PropertiesFileUtil` が各種 `.properties` ファイルを読み込めるようにするための
Service Provider Interface（SPI）を提供します。

通常のクラスパスベースのアプリ（`module-info.java` なし）では不要です。

---

## 必要になる場面

`module-info.java` を使った Java モジュールシステム環境で `PropertiesFileUtil` を使用する場合、
SPI の登録が必要になります。`PropertiesFileUtil` は内部で `ResourceBundle` を使って
`.properties` ファイルを読み込んでおり、モジュールシステム環境ではこの読み込みに
SPI の登録が必要になるためです。

アプリの各モジュールが、`PropertiesFileUtil` で読み込む `.properties` ファイル
（`messages.properties`、`application.properties` など。[下表](#spi-インターフェースの種類) 参照）を持つ場合、
そのモジュールに対応する SPI 実装を登録する必要があります。

---

## SPI インターフェースの種類

各プロパティファイル種別に対応したインターフェースが用意されています。

| SPI インターフェース | 対応ファイル |
| ------------------ | ------------ |
| `MessagesProvider` / `MessagesBaseProvider` / `MessagesCoreProvider` | messages.properties |
| `MessagesWithItemNamesProvider` / `MessagesWithItemNamesBaseProvider` / `MessagesWithItemNamesCoreProvider` | messages_with_item_names.properties |
| `ConstantsProvider` / `ConstantsBaseProvider` / `ConstantsCoreProvider` | constants.properties |
| `ItemNamesProvider` / `ItemNamesBaseProvider` / `ItemNamesCoreProvider` | item_names.properties |
| `EnumNamesProvider` / `EnumNamesBaseProvider` / `EnumNamesCoreProvider` | enum_names.properties |
| `ApplicationProvider` / `ApplicationBaseProvider` / `ApplicationCoreProvider` / `ApplicationProfileProvider` / `ApplicationCoreProfileProvider` | application.properties |
| `ValidationMessagesPatternDescriptionsProvider` | ValidationMessagesPatternDescriptions.properties |

---

## module-info.java の設定

アプリモジュールが、`PropertiesFileUtil` で読み込む `.properties` ファイル（上表の `messages.properties`、`application.properties` など）を持つ場合、以下の2点が必要です。

### 1. SPI 実装クラスの作成

`AbstractPropertiesFileProviderImpl` を継承した実装クラスを作成します。

```java
// 例: messages.properties を持つモジュールの場合
public class AppMessagesProvider extends AbstractPropertiesFileProviderImpl
    implements MessagesProvider {
}
```

### 2. module-info.java への登録

`.properties` ファイルの配置場所によって書き方が異なります。

**パターンA：パッケージ配下に置く場合（`opens` で個別公開）**

```java
module your.app.module {
    requires jp.ecuacion.lib.core;

    // SPI プロバイダーを登録
    provides jp.ecuacion.lib.core.spi.MessagesProvider
        with your.app.AppMessagesProvider;

    // ライブラリがリソースファイルにアクセスできるよう open する
    opens your.app.resources;
}
```

**パターンB：クラスパス直下に置く場合（`open module` で全公開）**

`.properties` ファイルをパッケージに属さないクラスパスのルートに置く場合、
`opens` でパッケージを指定できないため、モジュール全体を `open` にします。

```java
open module your.app.module {
    requires jp.ecuacion.lib.core;

    // SPI プロバイダーを登録
    provides jp.ecuacion.lib.core.spi.MessagesProvider
        with your.app.AppMessagesProvider;
}
```

`opens` または `open module` の宣言がないと、ライブラリがリソースファイルを読み込めないため注意してください。

なお、リフレクションでアプリ側のクラスにアクセスする機能のための `opens` の設定については、
[named module 環境での利用](named-module.md) を参照してください。

---

## 通常のアプリ開発への影響

`module-info.java` を使わないアプリでは、SPI を意識する必要はありません。
`PropertiesFileUtil` は自動的にクラスパス上の `.properties` ファイルを検索します。
