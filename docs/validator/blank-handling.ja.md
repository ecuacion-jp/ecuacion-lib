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

---

## 空文字を valid として扱うバリデータ

`ecuacion-lib-validation` で空文字を invalid とするのは `@NotEmpty` / `@NotBlank` のみです。
それ以外の書式チェック系バリデータはすべて空文字を valid として扱います。

| カテゴリ | 代表的なバリデータ |
| -------- | ----------------- |
| 書式チェック系 | `@PatternWithDescription`, `@IntegerString`, `@BooleanString` など |
| 条件付き系 | `@NotEmptyWhen`, `@TrueWhen` など `@XxxWhen` 系 |
| 大小比較系 | `@LessThan`, `@GreaterThan` など |
