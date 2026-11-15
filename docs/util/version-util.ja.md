# VersionUtil

`VersionUtil`（`jp.ecuacion.lib.core.util.VersionUtil`）は、ecuacion 製品（`ecuacion-lib`、
`ecuacion-splib`、`ecuacion-utils` など）や、個々のアプリ自身のバージョンを取得するためのユーティリティクラスです。

---

## メソッド一覧

| メソッド | 説明 |
| --- | --- |
| `getVersion(productName)` | バージョン文字列を返す。クラスパス上にバージョンファイルが存在しない場合は `null` |

```java
// ecuacion 製品のバージョン
String libVersion = VersionUtil.getVersion("ecuacion-lib"); // 例: "16.0.0"

// アプリ自身のバージョン（"" を渡す）
String appVersion = VersionUtil.getVersion("");
```

---

## 仕組み

各 ecuacion 製品は、それぞれ `version_<productName>.properties` ファイル（例:
`version_ecuacion-lib.properties`）を同梱しています。また個々のアプリも同様に、独自の
`version.properties` ファイルを同梱できます（読み込むには `productName` に `""` を渡します）。
どちらの場合も、ファイル内の `version` キーには `@project.version@` が書かれており、Maven のリソースフィルタリングによってビルド時に実際のビルドバージョンへ置換されます。

このクラスが `ecuacion-lib-core` に実装されているのは、他のすべての ecuacion 製品が
`ecuacion-lib-core` に依存しているため、単一の共通実装でクラスパス上のあらゆる製品のバージョンファイルを読み込めるからです。取得結果は `productName` ごとにキャッシュされます。

自分のアプリのバージョンも同様の方法で取得できるようにするには、`version` キーを持つ
`version.properties` ファイルを同梱し、（`${...}` と衝突しないデリミタ、例えば `@...@` を使って）
Maven のリソースフィルタリングを設定し、ビルド時に `@project.version@` が置換されるようにします。
