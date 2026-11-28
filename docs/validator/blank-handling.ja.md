# ecuacion-libのvalidatorにおける空文字の扱い

## 概要

`ecuacion-lib-validation` のバリデータは、空文字（`""`）を **valid** として扱います（＝validation エラーにならない）。
これは Jakarta Validation 標準とは異なる設計です。

---

## 標準の @Pattern との違い

標準の `@Pattern` は `null` を valid とする一方、`""` は invalid 扱いです。
一方で、ecuacion-lib の独自バリデータである `@PatternWithDescription` は、`null` に加えて `""` も valid 扱いです。

| 値 | 標準 `@Pattern` | ecuacion-lib `@PatternWithDescription` |
| ---- | --------------- | ------------------------- |
| `"abc123"`<br>（正規表現一致） | ✅ valid | ✅ valid |
| `"ABC"`<br>（正規表現不一致） | ❌ invalid | ❌ invalid |
| `null` | ✅ valid | ✅ valid |
| `""` | ❌ **invalid** | ✅ **valid** |

---

## 空文字を valid にする理由

Web アプリケーションでは `null` と `""` には意味の違いがあります。

- `""` ＝ 画面に項目があり、ユーザーが未入力のまま送信した
- `null` ＝ そもそも画面に項目がない（送信データに含まれていない）

ユーザーが未入力で送信した場合に `@Pattern` が invalid を返すと、
「形式が不正です」というエラーが表示されます。
しかし、必須項目であれば本来あるべきエラーは「入力必須です」（`@NotEmpty` によるもの）ですし、
必須項目でない場合はエラーは発生しないのが正しい挙動です。

空文字で submit されてきた文字列を一括で `null` に変換することもできますが、
それでは画面に項目があっての未入力と、そもそも項目がない場合の区別がつけられません。

そのため、書式チェック系のバリデータは未入力（`""`）に反応すべきではありません。

この方針は、画面からの入力を検証することを前提にしています。
REST API のように画面を経由しない入力では、`""` も送り手が意図して送った値なので、invalid として扱いたい場合もあります。

その場合は、`@NotEmpty` などと組み合わせれば空文字を invalid にできます。
`null` は valid のまま `""` だけを invalid にしたい場合は、`@Size(min = 1)` を組み合わせます。

```java
@Size(min = 1)
@PatternWithDescription(regexp = "[0-9]+")
private String code;  // "" → @Size の違反、null → valid
```

逆に、空文字を invalid とするバリデータを、空文字を valid としたい状況で使うことはできません。
空文字を valid としておけば、組み合わせによってどちらの状況にも対応できます。

---

## 空文字を valid として扱うバリデータ

`ecuacion-lib-validation` の独自バリデータは、空文字を valid として扱います
（`@PatternWithDescription`、`@IntegerString`、`@LessThan` など）。

ただし、条件付きバリデータ（`@XxxWhen`）のうち値そのものを検証するものは、
条件を満たすときの空の値（`null` と `""`）の扱いが、バリデータごとに異なります。

| バリデータ | 空の値（`null` / `""`） |
| --- | --- |
| `@PatternWhen` / `@StringWhen` | invalid |
| `@NotPatternWhen` / `@NotStringWhen` | valid |
| `@ValueOfPropertyPathWhen` | 比較先の値と同じなら valid（`null` と `""` は別の値として比べる） |
| `@NotValueOfPropertyPathWhen` | 比較先の値と違えば valid |

`@NotEmptyWhen` / `@EmptyWhen` などは、空かどうかを検証することが目的なので、ここには含みません。
