# Require系アノテーション

## 概要

`jp.ecuacion.lib.core.annotation` パッケージには、メソッドの引数に付与して**要件を宣言するアノテーション**が用意されています。

これらのアノテーション自体は**何も実行しません**。ランタイムでの処理もなく、IDE の静的解析（jspecify の `@NonNull` / `@Nullable` 等）のようにコンパイル時に警告を出す仕組みもありません。あくまで「このメソッドはこの引数に〇〇を期待する」というドキュメントとして機能します。

実際の検証は、メソッド内部で `ObjectsUtil` の対応メソッドを使って実装します。

---

## アノテーション一覧

| アノテーション | 意味 | ObjectsUtil メソッド | スローする例外 |
| ------------- | ---- | ------------------- | -------------- |
| `@RequireNonEmpty` | 引数が empty（null または空文字）でないこと | `requireNonEmpty(String value)` | `RequireNonEmptyException` |
| `@RequireSizeNonZero` | コレクション・配列の要素数が1以上であること | `requireSizeNonZero(Collection/array)` | `RequireSizeNonZeroException` |
| `@RequireElementNonNull` | コレクション・配列の各要素が null でないこと | `requireElementNonNull(Collection/array)` | `RequireElementNonNullException` |
| `@RequireElementNonEmpty` | コレクション・配列の各要素が empty でないこと | `requireElementNonEmpty(Collection/array)` | `RequireElementNonEmptyException` |
| `@RequireElementNonDuplicated` | コレクション・配列の各要素に重複がないこと | `requireElementsNonDuplicated(Collection/array)` | `RequireElementsNonDuplicatedException` |

各メソッドは検証後に引数をそのまま返すため、メソッドチェーンで使用できます。

```java
String validated = ObjectsUtil.requireNonEmpty(name);
```

なお、`ObjectsUtil.requireNonNull()` に対応するアノテーションはありません。
このメソッドは `@Nullable` な値を実行時に null チェックして `@NonNull` として返す用途で使用します。

---

## 使い方

アノテーションで宣言し、メソッド内部で `ObjectsUtil` を使って実際に検証します。

```java
import jp.ecuacion.lib.core.annotation.RequireNonEmpty;
import jp.ecuacion.lib.core.util.ObjectsUtil;

public void process(@RequireNonEmpty String name, @RequireSizeNonZero List<String> items) {
    ObjectsUtil.requireNonEmpty(name);
    ObjectsUtil.requireSizeNonZero(items);

    // 以降、name は empty でなく、items は要素数1以上であることが保証される
}
```

---

## @NonNull / @Nullable との違い

| | Require系アノテーション | jspecify の `@NonNull` / `@Nullable` |
| --- | --- | --- |
| 実行時検証 | `ObjectsUtil` で明示的に実装 | なし（宣言のみ） |
| IDE 静的解析 | サポートなし | サポートあり（警告表示） |
| 用途 | 実装の意図を明示 + ランタイム検証 | コンパイル時の null 安全性検査 |

`@NonNull` / `@Nullable` はコンパイル時の静的解析に強みがあり、Require系アノテーションはランタイムの実際の検証に使います。両者を併用することもできます。
