# 独自バリデーター 概要

## 概要

`ecuacion-lib-validation` モジュールは、Jakarta Validation の標準アノテーション（`@NotNull`, `@Size` など）を補完する独自のバリデーターを提供します。

```xml
<dependency>
  <groupId>jp.ecuacion.lib</groupId>
  <artifactId>ecuacion-lib-validation</artifactId>
</dependency>
```

---

## 適用レベルによる分類

| レベル | 説明 | 代表的なアノテーション |
| ------ | ---- | ---------------------- |
| クラスレベル | クラス全体に付与し、複数フィールドの関係を検証 | `@TrueWhen`, `@GreaterThan`, `@AnyNotNull`, `@ReturnTrue` |
| フィールドレベル | 個別のフィールドに付与 | `@IntegerString`, `@EnumElement`, `@PatternWithDescription` |
| メソッドレベル | メソッドに付与し、その戻り値を検証 | `@AssertTrueWithPropertyPath` |

クラスレベルのアノテーションは `propertyPath` 属性でどのフィールドに違反を関連付けるかを指定します。
これらは単なる文字列としてリフレクションで解決されるため、フィールド名を変更してもコンパイルでは検知
できません。検知するための対策は [propertyPathのリネーム対策](property-path-safety.md)
を参照してください。

`propertyPath`・`conditionPropertyPath`・`baselinePropertyPath` などで指定した名前（`"dept.name"` のようなネストしたパスでは各ノード）は、
まず同名のフィールドとして解決され、フィールドが存在しない場合は getter（`getName()`、またはプリミティブの `boolean` を返す `isName()`。`Boolean` を返す `isName()` は対象外）として解決されます。
Jakarta Validation 標準の制約を getter に付与した場合と同様に、getter の戻り値が検証対象の値になるため、フィールドを持たないプロパティも指定できます。

```java
@LessThan(propertyPath = "startDate", baselinePropertyPath = "endDate")
public class Period {
    private LocalDate startDate;
    private LocalDate base;
    private int days;

    // フィールド endDate はなく、getter の戻り値が baselinePropertyPath の値として使われる
    public LocalDate getEndDate() {
        return base.plusDays(days);
    }
}
```

---

## 機能による分類

### When系（条件付きバリデーション）

「〇〇が××のとき、このフィールドは△△でなければならない」という条件付きルールを表現します。
クラスレベルのアノテーションで、`conditionPropertyPath` と `conditionValue` で条件を定義します。

| アノテーション | バリデーション内容 |
| ------------- | ---------------- |
| `@TrueWhen` | 条件が成立するとき `true` であること |
| `@NotNullWhen` | 条件が成立するとき `null` でないこと |
| `@NotEmptyWhen` | 条件が成立するとき 空でないこと |
| ほか 9種類 | ... |

### 比較系

2つのフィールドの大小関係を検証します。
クラスレベルのアノテーションで、`propertyPath` と `baselinePropertyPath` の2フィールドを比較します。

| アノテーション | バリデーション内容 |
| ------------- | ---------------- |
| `@GreaterThan` | `propertyPath` > `baselinePropertyPath` |
| `@LessThan` | `propertyPath` < `baselinePropertyPath` |
| ほか 2種類 | ... |

### フィールド系

フィールドの値の形式や型適合性を検証します。フィールドレベルで使用します。

| アノテーション | バリデーション内容 |
| ------------- | ---------------- |
| `@IntegerString` | 整数文字列であること |
| `@EnumElement` | 指定 Enum の要素として有効な値であること |
| `@PatternWithDescription` | 正規表現マッチ（説明付きメッセージ対応） |
| ほか 8種類 | ... |

### コレクション・アサーション系

複数フィールドの null / empty 状態を一括検証したり、メソッドの戻り値を検証します。

| アノテーション | バリデーション内容 |
| ------------- | ---------------- |
| `@AnyNotNull` | 指定フィールドのうち少なくとも1つが `null` でないこと |
| `@AllOrNoneNull` | 全フィールドが `null`、または全フィールドが `null` でないこと |
| `@AssertTrueWithPropertyPath` | フィールドに紐付けた `@AssertTrue` 相当 |
| `@ReturnTrue` | 指定メソッドが `true` を返すこと |
| ほか 4種類 | ... |

---

## ValidationMessages との連携

各アノテーションのデフォルトメッセージは `ValidationMessages.properties` に定義されています。
アプリ側で上書きする場合は、アノテーションの完全修飾名に `.message` を付けたキーを定義します。

```properties
# ValidationMessages.properties
jp.ecuacion.lib.validation.constraints.TrueWhen.message = 同意が必要です
```
