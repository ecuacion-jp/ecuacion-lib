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
| `ecuacion-lib-core` | コア機能（Item・Violation・PropertiesFileUtil・各種 Util など） |
| `ecuacion-lib-validation` | Jakarta Validation 独自バリデーター群 |
| `ecuacion-lib-validation-business-messages` | ビジネス向けバリデーションメッセージ |
| `ecuacion-lib-parent` | 親 POM（ecuacion-lib 各モジュールのバージョン管理） |
| `ecuacion-lib-bom` | BOM（ecuacion-lib が依存する外部ライブラリのバージョン管理） |
| `ecuacion-lib-dependencies` | ecuacion モジュールのビルド用に使用（一般アプリ開発者向けには非推奨） |

`ecuacion-lib-dependencies` は ecuacion-lib 自身のビルド用モジュールで、一般アプリケーション開発者は使わないため、以降では説明を省略します。

### 依存関係

アプリケーションから dependency に指定する 3 モジュールは、以下のように `compile` スコープで依存しています。

```text
ecuacion-lib-validation-business-messages
  └─ ecuacion-lib-validation
       └─ ecuacion-lib-core
```

`compile` スコープの依存は推移的に引き継がれるため、使いたい機能のうち最上位のモジュールを 1 つ dependency に指定するだけで、その下位のモジュールも自動的に取り込まれます。

- `ecuacion-lib-validation-business-messages` を指定 → `ecuacion-lib-validation`・`ecuacion-lib-core` も入る
- `ecuacion-lib-validation` を指定 → `ecuacion-lib-core` も入る

---

## 各モジュールの説明

以下の dependency の記述例ではバージョンを省略しています。バージョン指定の要否は下記「セットアップ」を参照してください。

### ecuacion-lib-core

ecuacion-lib の基本機能を提供するモジュールです。
このドキュメントのうち、ナビゲーションの「独自バリデーター」**以外**のメニューで説明している機能（Item・Violation・PropertiesFileUtil・各種 Util など）は、すべてこのモジュールに含まれます。

`ecuacion-lib-validation` を dependency に指定している場合は推移的に取り込まれるため、別途指定する必要はありません。独自バリデーターを使わない場合は、このモジュールを直接指定します。

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-core</artifactId>
</dependency>
```

### ecuacion-lib-validation

Jakarta Validation の標準アノテーション（`@NotNull`・`@Size` など）を補完する、独自のバリデーター（`@EmptyWhen`・`@GreaterThan`・`@PatternWithDescription` など）を提供するモジュールです。
ナビゲーションの「独自バリデーター」メニューで説明している機能は、このモジュールを dependency に追加しないと使えません。

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation</artifactId>
</dependency>
```

### ecuacion-lib-validation-business-messages

Jakarta Validation のデフォルトメッセージ（Hibernate Validator が提供する `"null は許可されていません"` など）は技術者向けの表現です。このモジュールはそれをエンドユーザー向けの自然な表現（`"入力必須です"` など）に置き換える
**ValidationMessages プロパティファイルのみ**を含みます。Java コードは一切含まれておらず、
dependency に追加するだけで有効になります。

#### 使い方

`pom.xml` に以下を追加します。

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation-business-messages</artifactId>
</dependency>
```

このモジュールは `ecuacion-lib-validation`（さらにその先の `ecuacion-lib-core`）に依存しているため、これを追加すれば `ecuacion-lib-validation`・`ecuacion-lib-core` を別途 dependency に追加する必要はありません（上記「依存関係」参照）。

#### 提供するメッセージ

Jakarta Validation 標準アノテーションと ecuacion-lib-validation 独自アノテーションの両方を対象に、
英語（デフォルト）・日本語をはじめ、ecuacion-lib が対応する全言語のメッセージを提供します
（対応言語は [README](https://github.com/ecuacion-jp/ecuacion-lib#supported-languages) の「Supported Languages」を参照）。

このモジュールが提供するメッセージキーは、ecuacion モジュール共通の `.default` サフィックスの仕組みでアプリ側から上書きできます。詳細は [PropertiesFileUtil](properties-file-util/loading-rules.md)
の「`.default` サフィックスによるデフォルト値の上書き」を参照してください。

### ecuacion-lib-parent

ecuacion-lib 全モジュールの親 POM です。dependencyManagement に ecuacion-lib 各モジュール（`ecuacion-lib-core`・`ecuacion-lib-validation`・`ecuacion-lib-validation-business-messages`）のバージョンが定義されています。

ただし、上記「依存関係」のとおり dependency に指定する `ecuacion-lib-xxx` は最上位の 1 モジュールだけで済むため、`ecuacion-lib-parent` を単独で BOM としてインポートしても、バージョンを記述する箇所は 1 つのまま減らず、インポートの記述が増えるだけです。そのため `ecuacion-lib-parent` を直接インポートすることはおすすめしません。
`ecuacion-lib-parent` の dependencyManagement は、下記「セットアップ」のパターン2で `ecuacion-lib-bom` をインポートすると一緒に引き継がれます。

### ecuacion-lib-bom

ecuacion-lib-core・ecuacion-lib-validation などが直接依存する外部ライブラリ（`jakarta.validation-api`・`hibernate-validator`など）のバージョンを一元管理する Maven BOM です。dependencyManagement にインポートすることで、これらのライブラリのバージョンを指定せずに使えます。

`ecuacion-lib-parent` にこれらのバージョン管理を含めていないのは、`ecuacion-lib-parent` を継承する
`ecuacion-splib-parent`（Spring Boot ベース）を親 POM とする Spring Boot アプリケーションで、
ecuacion-lib と重複するモジュールのバージョンを ecuacion-lib 側ではなく Spring Boot 側の管理下に置けるようにするためです。

インポート方法は下記の「セットアップ」を参照してください。

---

## セットアップ

`ecuacion-lib-core`・`ecuacion-lib-validation` は `hibernate-validator`・`jakarta.el`（validation
機能を使う場合に必要）、`org.eclipse.angus:angus-mail`（`MailUtil` を使う場合に必要）に
`provided` スコープで依存しています。`provided` は推移的に伝播しないため、これらはアプリ側で明示的に追加する必要があります。これらのライブラリ、および `ecuacion-lib-xxx` 自体のバージョンをどこまで自動管理したいかによって、以下の2パターンがあります。

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

### パターン2: `ecuacion-lib-bom` を BOM としてインポートする

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

!!! warning "Spring Boot アプリケーションで使う場合"
    `ecuacion-lib-bom` は `hibernate-validator` など、Spring Boot も管理している外部ライブラリのバージョンを固定します。
    そのため Spring Boot アプリケーションでインポートすると、これらのライブラリのバージョンが Spring Boot と ecuacion-lib の二重管理になり、
    Spring Boot が想定するものとは異なるバージョンが採用されるおそれがあります
    （上記「ecuacion-lib-bom」の節で、これらのバージョン管理を `ecuacion-lib-parent` に含めていない理由と同じです）。

    Spring Boot アプリケーションでは `ecuacion-lib-bom` はインポートせず、以下のいずれかにしてください。

    - ecuacion-splib を使う場合: `ecuacion-splib-parent` を親 POM にする
    - それ以外の場合: パターン1と同様に `ecuacion-lib-xxx` のみバージョンを指定し、`hibernate-validator` などのバージョンは Spring Boot の管理に任せる

### ビジネス向けバリデーションメッセージを使う場合

上記のいずれのパターンでも、`ecuacion-lib-validation-business-messages` を追加する場合のバージョン要否は `ecuacion-lib-validation` と同様です。
