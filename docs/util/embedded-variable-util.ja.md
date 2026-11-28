# EmbeddedVariableUtil

`EmbeddedVariableUtil`（`jp.ecuacion.lib.core.util.EmbeddedVariableUtil`）は、
`${VAR}` のように開始記号と終了記号で囲まれた変数を文字列から探し、値に置き換えるユーティリティクラスです。
設定ファイルの値に埋め込んだ環境変数の置換などに使えます。

---

## getVariableReplacedString — 変数を値に置き換える

開始記号・終了記号と、変数名から値を取得する方法（`Map` または `Function`）を指定します。

```java
// Map から値を取得する
Map<String, String> map = Map.of("host", "example.com", "port", "8080");
EmbeddedVariableUtil.getVariableReplacedString("http://${host}:${port}/", "${", "}", map);
// → "http://example.com:8080/"

// Function から値を取得する（例: 環境変数）
EmbeddedVariableUtil.getVariableReplacedString("${HOME}/work", "${", "}", System::getenv);
```

- 変数は、すべての位置を先に特定してから置き換えます。
  そのため、置き換えた値に開始記号や終了記号が含まれていても、その値が再び変数として扱われることはありません。

    ```java
    // key1 の値 "${x" はそのまま出力される
    getVariableReplacedString("a${key1}b", "${", "}", Map.of("key1", "${x"));  // "a${xb"
    ```

- 開始記号・終了記号の直前に `\` を付けると、変数として扱いません。
  ただし `\` は取り除かれず、そのまま残ります（`"a\${b\}c"` → `"a\${b\}c"`）。
- 開始記号・終了記号に空文字は指定できません（`RequireNonEmptyException` をスローします）。

### 例外

| 例外 | スローされる場合 |
| --- | --- |
| `EmbeddedVariableUtil.VariableNotFoundException` | 変数名に対応する値が見つからない場合（`Map` にキーがない、`Function` が `null` を返した、など） |
| `EmbeddedVariableUtil.StringFormatIncorrectException` | 終了記号が開始記号より前にある、または対応する記号がない場合（`"a}b"`、`"${a"` など） |

どちらも `ViolationException` のサブクラスで、`ExceptionUtil.getMessageList(...)` でローカライズされたメッセージを取得できます。

---

## Options — 単独の終了記号を無視する

`Options.setIgnoresEmergenceOfEndSymbolOnly(true)` を指定すると、開始記号より前に現れた終了記号を、
`StringFormatIncorrectException` にせず、変数ではない通常の文字列として扱います。

```java
var options = new EmbeddedVariableUtil.Options().setIgnoresEmergenceOfEndSymbolOnly(true);

// "{0}" の "}" は無視され、"{+item_names:user.id}" だけが変数として扱われる
EmbeddedVariableUtil.getVariableReplacedString("{0} is invalid. ({+item_names:user.id})",
    "{+item_names:", "}", Map.of("user.id", "User ID"), options);
// → "{0} is invalid. (User ID)"
```

終了記号が `{0}` のような別の書式と共通する場合に使います。

---

## getPartList — 文字列と変数の部分に分割する

置き換えは行わず、文字列を通常の文字列の部分と変数の部分に分割したリストを返します。
開始記号を複数指定できます（終了記号は共通です）。

```java
List<Pair<String, String>> list = EmbeddedVariableUtil.getPartList(
    "a${+b}c${-d}", new String[] {"${+", "${-"}, "}");
// → [(null, "a"), ("${+", "b"), (null, "c"), ("${-", "d")]
```

各要素の左側は開始記号（通常の文字列の部分は `null`）、右側は変数名または文字列です。
空文字を渡すと空のリストを返します。
