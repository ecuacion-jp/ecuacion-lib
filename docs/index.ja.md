# ecuacion-lib reference

こちらは ecuacion library（モジュール名：`ecuacion-lib-xxx`）のリファレンスページです。
各ライブラリのユーティリティクラスや機能を網羅的にまとめています。

## ecuacion library とは

ecuacion library は、アプリケーション開発で共通的に必要となる各種機能を提供するライブラリです。
本ライブラリ単体でも使用可能ですが、基本的には ecuacion-splib などのベースライブラリと組み合わせて使用します。

## モジュール構成

ecuacion library は以下の 6 つのモジュールで構成されています。

| モジュール名 | 役割 |
| --- | --- |
| `ecuacion-lib-parent` | 親 POM（ecuacion-lib 各モジュールのバージョン管理） |
| `ecuacion-lib-bom` | BOM（ecuacion-lib が依存する外部ライブラリのバージョン管理） |
| `ecuacion-lib-core` | コア機能（Item・PropertiesFileUtil・Violation など） |
| `ecuacion-lib-validation` | Jakarta Validation 独自アノテーション群 |
| `ecuacion-lib-validation-business-messages` | ビジネス向けバリデーションメッセージ |
| `ecuacion-lib-dependencies` | ecuacion モジュールのビルド用に使用（一般アプリ開発者向けには非推奨） |

`ecuacion-lib-core` と `ecuacion-lib-validation` の詳細はナビゲーションの各ページを参照してください。
`ecuacion-lib-dependencies` は ecuacion-lib 自身のビルド用モジュールで一般アプリケーション開発者は使わないため、以降では、このページで説明を完結させる残り 3 モジュールについて説明します。

---

## ecuacion-lib-parent

ecuacion-lib 全モジュールの親 POM です。dependencyManagement に ecuacion-lib 各モジュール（`ecuacion-lib-core`・`ecuacion-lib-validation`・`ecuacion-lib-validation-business-messages`）のバージョンが定義されており、BOM としてインポートすることでバージョンを個別に記述しなくて済みます。
インポート方法は下記の「セットアップ」を参照してください。

## ecuacion-lib-bom

ecuacion-lib-core・ecuacion-lib-validation などが直接依存する外部ライブラリ（`jakarta.validation-api`・`hibernate-validator`など）のバージョンを一元管理する Maven BOM です。dependencyManagement にインポートすることで、これらのライブラリのバージョンを指定せずに使えます。

`ecuacion-lib-parent` にこれらのバージョン管理を含めていないのは、`ecuacion-lib-parent` を継承する
`ecuacion-splib-parent`（Spring Boot ベース）を親 POM とする Spring Boot アプリケーションで、
ecuacion-lib と重複するモジュールのバージョンを ecuacion-lib 側ではなく Spring Boot 側の管理下に置けるようにするためです。

インポート方法は下記の「セットアップ」を参照してください。

## ecuacion-lib-validation-business-messages

Jakarta Validation のデフォルトメッセージ（Hibernate Validator が提供する `"null は許可されていません"` など）は技術者向けの表現です。このモジュールはそれをエンドユーザー向けの自然な表現（`"入力必須です"` など）に置き換える
**ValidationMessages プロパティファイルのみ**を含みます。Java コードは一切含まれておらず、
dependency に追加するだけで有効になります。

### 使い方

`pom.xml` に以下を追加します（バージョン指定の要否は「セットアップ」の各パターンを参照してください）。

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation-business-messages</artifactId>
</dependency>
```

### 提供するメッセージ

Jakarta Validation 標準アノテーションと ecuacion-lib-validation 独自アノテーションの両方を対象に、
英語（デフォルト）・日本語をはじめ、ecuacion-lib が対応する全言語のメッセージを提供します
（対応言語は [README](https://github.com/ecuacion-jp/ecuacion-lib#supported-languages) の「Supported Languages」を参照）。

このモジュールが提供するメッセージキーは、ecuacion モジュール共通の `.default` サフィックスの仕組みでアプリ側から上書きできます。詳細は [PropertiesFileUtil](properties-file-util/loading-rules.md)
の「`.default` サフィックスによるデフォルト値の上書き」を参照してください。

---

## セットアップ

`ecuacion-lib-core`・`ecuacion-lib-validation` は `hibernate-validator`・`jakarta.el`（validation
機能を使う場合に必要）、`org.eclipse.angus:angus-mail`（`MailUtil` を使う場合に必要）に
`provided` スコープで依存しています。`provided` は推移的に伝播しないため、これらはアプリ側で明示的に追加する必要があります。これらのライブラリ、および `ecuacion-lib-xxx` 自体のバージョンをどこまで自動管理したいかによって、以下の3パターンがあります。

### パターン1: BOM をインポートせず、直接バージョンを指定する

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation</artifactId>
    <version>（バージョン）</version>
</dependency>

<!-- validation機能を使う場合 -->
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>（バージョン）</version>
</dependency>
<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>jakarta.el</artifactId>
    <version>（バージョン）</version>
</dependency>
```

`ecuacion-lib-parent`・`ecuacion-lib-bom` のどちらもインポートしないため、
上記すべてのバージョンを自分で指定する必要があります。

### パターン2: `ecuacion-lib-parent` を BOM としてインポートする

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>jp.ecuacion.lib</groupId>
            <artifactId>ecuacion-lib-parent</artifactId>
            <version>（バージョン）</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>jp.ecuacion.lib</groupId>
        <artifactId>ecuacion-lib-validation</artifactId>
    </dependency>

    <!-- validation機能を使う場合。ecuacion-lib-parent は ecuacion-lib-xxx 自身の
         バージョンしか管理していないため、引き続きバージョン指定が必要。 -->
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>（バージョン）</version>
    </dependency>
    <dependency>
        <groupId>org.glassfish</groupId>
        <artifactId>jakarta.el</artifactId>
        <version>（バージョン）</version>
    </dependency>
</dependencies>
```

パターン1と異なり、`ecuacion-lib-parent` が `ecuacion-lib-validation` 自身のバージョンを管理しているため、
`ecuacion-lib-validation` のバージョン指定が不要になります。

### パターン3: `ecuacion-lib-bom` を BOM としてインポートする

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>jp.ecuacion.lib</groupId>
            <artifactId>ecuacion-lib-bom</artifactId>
            <version>（バージョン）</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>jp.ecuacion.lib</groupId>
        <artifactId>ecuacion-lib-validation</artifactId>
    </dependency>

    <!-- validation機能を使う場合 -->
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.glassfish</groupId>
        <artifactId>jakarta.el</artifactId>
    </dependency>
</dependencies>
```

`ecuacion-lib-bom` は `ecuacion-lib-parent` を親 POM としており、BOM インポートは親の
dependencyManagement も引き継ぐため、`ecuacion-lib-bom` だけをインポートすれば
`ecuacion-lib-validation`・`hibernate-validator`・`jakarta.el` のいずれもバージョン指定が不要になります。`ecuacion-lib-parent` を別途インポートする必要はありません。

### ビジネス向けバリデーションメッセージを使う場合

上記のいずれのパターンでも、`ecuacion-lib-validation-business-messages` を追加する場合のバージョン要否は `ecuacion-lib-validation` と同様です。
