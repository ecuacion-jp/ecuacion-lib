# propertyPathのリネーム対策

## 概要

クラスレベルバリデータ（`@XxxWhen`、`@GreaterThan`/`@LessThan`、`@AnyNotNull` など）は、
`propertyPath`・`conditionPropertyPath`・`conditionValuePropertyPath`・`baselinePropertyPath` といった
文字列属性でフィールド（またはフィールドが存在しない場合は getter）を参照します。これらの文字列はバリデーション実行時にリフレクションで解決されるため、
参照先のフィールド名が変更されてもコンパイラは検知できません。Java のアノテーション属性はコンパイル時定数
でなければならず、メソッド参照や `KProperty` のような型安全なフィールド参照をここで使うことはできません。

このページでは、それでも早期に `propertyPath` の壊れを検知するための実践的な2つの方法を紹介します。

---

## 対策1: アノテーション付与クラスごとに1件のバリデーションテストを書く

上記の文字列はすべて、フィールドの値の中身に関係なく、`validate()` 呼び出しのたびに無条件で解決されます。
`ClassValidator.internalIsValid` はバリデータ本体のロジックに渡す前に全ての `propertyPath` を解決しますし、
`ValidateWhenValidator.getSatisfiesCondition` も同様に `conditionPropertyPath`（および
`VALUE_OF_PROPERTY_PATH` 使用時の `conditionValuePropertyPath`）を解決します。これらの文字列がもはや実在
するフィールドを指していない場合、テストデータの中身とは無関係に、解決処理自体が `RuntimeException` を
throw します。

つまり、対象クラスのインスタンスに対して `Validator#validate` を1回呼ぶだけのテストを書いておけば、
リネームを検知できます。テストが通り続けている限り、そのクラスのアノテーションが参照する全ての
`propertyPath` は実在するフィールド（または getter）に解決できている、ということになります。

```java
class UserProfileValidationTest {

  @Test
  void validatorPropertyPathsResolve() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    // テストデータの中身は無関係。UserProfile のアノテーションが参照する
    // propertyPath / conditionPropertyPath / conditionValuePropertyPath を
    // 一通り解決させ、リネーム時にビルドを壊すためだけのテスト。
    validator.validate(new UserProfile());
  }
}
```

注意点:

- テストデータの値そのものに意味はなく、フィールド解決を発生させることだけが目的です。
- クラス上のアノテーションが `groups` を指定している場合、`validate()` 呼び出し時にそのグループを
  明示的に渡す必要があります。渡さないとそのアノテーションはスキップされ、`propertyPath` も解決されません。
- クラスごとに1テストで十分です。1回の `validate()` 呼び出しでそのクラスの全 `propertyPath` がまとめて
  解決されるため、アノテーションのインスタンスごとにテストを分ける必要はありません。

---

## 対策2: コンパイル時にチェックされるフィールド名定数を使う

フィールド名を生の文字列リテラルで書く代わりに、生成された定数（Lombok の `@FieldNameConstants` や、
自前で作成した定数クラスなど）経由で参照します。こうしておけば、上記のテストが実行されるより前の
コンパイル時点で、リネームによる参照切れを検知できます。

```java
@FieldNameConstants
public record UserProfile(String name, Dept dept) {}

@NotEmptyWhen(
    propertyPath = UserProfile.Fields.name,
    conditionPropertyPath = "...",
    ...
)
```

ネストしたパス（例: `"dept.name"`）の場合、定数参照にできるのは各セグメントのみで、`.` 区切り自体は
リテラルのままになります。

```java
propertyPath = UserProfile.Fields.dept + "." + Dept.Fields.name
```

なお、[ecuacion-tool-code-generator](https://github.com/ecuacion-jp/ecuacion-tool-code-generator) で生成した Entity や Record にも、
Lombok と同様の `Fields` クラスが自動生成されるので、それを使うこともできます（例: `UserRecord.Fields.USER_NAME`）。

---

## どちらを使うか

| アプローチ | 検知タイミング | コスト |
| -------- | ---------------------------- | ---- |
| 対策1（バリデーションテスト） | テスト実行時（CI等） | 低 — 対象クラスごとに1テスト |
| 対策2（フィールド名定数化） | コンパイル時 | 中 — 定数化の仕組み（Lombok等）をプロジェクト全体で導入する必要あり |

両者は排他ではなく併用が基本です。対策1は導入コストが低いのでまず入れておき、プロジェクトに
フィールド名定数化の規約が既にある（あるいは導入してよい）場合は、対策2を上乗せするとよいでしょう。
