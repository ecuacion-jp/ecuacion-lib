# ロード動作・詳細設定

## フォールバック

一部のファイル種別では、キーが見つからない場合に別のファイル種別へフォールバックします。
フォールバックが設定されている種別は、対応するファイルを作成しなくてもフォールバック先のファイルで動作します。
ファイルを分けた方が管理しやすい場面がある一方、`messages.properties` に一元的に書くのが一般的な運用でもあるため、そのような使い方もそのまま動作するようにフォールバックが設定されています。

| ファイル名 | フォールバック先 |
| --- | --- |
| `application[_xxx].properties` | なし |
| `constants[_xxx].properties` | なし |
| `messages[_xxx].properties` | なし |
| `messages_with_item_names[_xxx].properties` | `messages` |
| `item_names[_xxx].properties` | `messages` |
| `enum_names[_xxx].properties` | `messages` |
| `ValidationMessages[_xxx].properties` | なし |
| `ValidationMessagesWithItemNames[_xxx].properties` | `ValidationMessages` |
| `ValidationMessagesPatternDescriptions[_xxx].properties` | なし |

例えば `item_names.properties` を作成せずに `messages.properties` だけを用意しても、
`getItemName(...)` は `messages.properties` からキーを検索して返します。

---

## 複数モジュールのファイルを一括読み込み

ecuacion では、アプリを複数のモジュール（`base`, `core`, `web`, `batch` など）に分割することを想定しています。
例えばアプリ名が `sample-app` の場合：

```text
sample-app-base  → messages_base.properties
sample-app-core  → messages_core.properties
sample-app-web   → messages.properties
```

`PropertiesFileUtil.getMessage(...)` は上記すべてを一括検索します。
同じキーが複数ファイルに定義されていると **例外がスロー** されます（重複検知）。

上記の `base`・`core`・`web` のようなアプリ固有のサフィックスは、
ecuacion-splib を使用している場合 `spring.messages.basename` の設定から自動的に検出・登録されます。
ecuacion-splib を使用しない場合や、独自のモジュール・フレームワークを構築する場合は手動で呼び出します。

```java
PropertiesFileUtil.addResourceBundlePostfix("mymodule");
// → messages_mymodule.properties, application_mymodule.properties なども検索対象に追加される
```

---

## キーが存在しない場合の挙動

| ファイル種別 | キー未存在時の挙動 |
| --- | --- |
| `application.properties` | 例外をスロー |
| それ以外 | キー文字列をそのまま返す（例外なし） |

`application.properties` だけ例外をスローするのは、設定値の欠落をアプリ起動時に確実に検知するためです。
一方 `messages.properties` などは開発中に未定義キーが画面に表示される方が都合が良いため、例外をスローしません。

---

## JVMシステムプロパティは全ファイル種別より優先される

どのファイル種別であっても、検索対象のキーと同名のJVMシステムプロパティが存在する場合、上記のプロパティファイルの値より優先されます。そのため、メッセージ／設定キーとたまたま同名のシステムプロパティが定義されていると、ファイル側の値が気付かないうちに上書きされてしまいます。JVMシステムプロパティを設定できる立場（実質的には起動時の `-D` を制御できるサーバー管理者）でなければ発生し得ませんが、キーの解決結果がファイルの定義と食い違って見える場合の原因として覚えておく価値があります。

---

## `.default` サフィックスによるデフォルト値の上書き

ecuacion の各モジュールが提供するキーには `.default` サフィックスが付いています。
アプリ側で上書きしたい場合は `.default` なしの同名キーをアプリのファイルに定義します。

```properties
# ecuacion モジュール内のファイル
some.key.default=ecuacion のデフォルト値

# アプリ側のファイル（上書き）
some.key=アプリ独自の値
```

`getApplication("some.key")` はアプリ側の値を優先して返します。

例えば `ecuacion-lib-validation-business-messages` が提供するメッセージキーには
`.default` サフィックスが付いています（例：`jakarta.validation.constraints.NotNull.message.default`）。
通常のキー（例：`jakarta.validation.constraints.NotNull.message`）がアプリの
`ValidationMessages.properties` に見つからない場合に `.default` キーへフォールバックするため、
アプリ固有のカスタマイズを妨げることなくデフォルトメッセージを差し替えられます。

---

## application.properties 内の `${...}` プレースホルダーの解決

パスワードなどの設定値は通常、`application.properties` からプレーンテキストとして読み込まれます。
このファイルは通常ソース管理にコミットされるため、実際の秘密情報を直接書き込むのは避けるべきです。
代わりに `${...}` プレースホルダー（例: `some.key=${SOME_ENV_VAR}`）を記述し、外部リゾルバーを登録することで、実行時に環境変数などの外部ソースから値を供給できます。

```java
PropertiesFileUtil.setExternalPlaceholderResolver(value -> System.getenv().getOrDefault(value, value));
```

ecuacion-lib 自体は環境変数やフレームワーク固有のプロパティソースについて何も組み込みで知りません。`setExternalPlaceholderResolver(...)` は単なる拡張ポイントです。`ecuacion-splib`
などのフレームワーク固有モジュールは、Spring の `Environment` を通じて `${...}` を解決するよう自動的に配線します。登録済みのリゾルバーを解除するには `null` を渡します。

この解決処理は `application[_xxx].properties` の値にのみ適用され、`messages.properties` や
`ValidationMessages.properties` などには影響しません（`ValidationMessages` 系ファイルにおける
`${...}` EL 式評価については [ValidationMessages](validation-messages.md) を参照してください）。

---

## キャッシュのクリア

`PropertiesFileUtil` は、`.properties` ファイルの内容を初回読み込み後にメモリ上へキャッシュします。
アプリケーション実行中にファイルがディスク上で更新された場合（管理画面からの更新など）は、
`clearCache()` を呼び出すことで、次回の `get...` / `has...` 呼び出し時にディスクから再読み込みさせることができます。

```java
PropertiesFileUtil.clearCache();
```
