# ReflectionUtil

`ReflectionUtil`（`jp.ecuacion.lib.core.util.ReflectionUtil`）は `java.lang.reflect` を使用した低レイヤーなリフレクション操作のユーティリティクラスです。

propertyPath 文字列を使ったオブジェクトグラフのナビゲーション（フィールド値の取得、
leafBean の取得など）は `PropertyPathUtil` が担います。

---

## クラスの存在確認とインスタンス生成

```java
// クラスが存在するか確認
boolean exists = ReflectionUtil.classExists("jp.ecuacion.lib.core.util.StringUtil"); // true
boolean exists2 = ReflectionUtil.classExists("com.example.NonExistent");             // false

// 引数なしコンストラクタでインスタンスを生成
Object instance = ReflectionUtil.newInstance("jp.ecuacion.example.MyClass");
```

**`className` に信頼できない（エンドユーザー由来の）値を渡さないでください。** `classExists` は指定したクラスのロードと static 初期化を発生させ、`newInstance` はさらに引数なしコンストラクタでのインスタンス化まで発生させます。`className` を制御できる者は、クラスパス上にある任意のコードを実行できてしまいます。

---

## 単純フィールド名でのフィールド取得

アクセス修飾子に関わらず、スーパークラスを含めてフィールドを検索します。
ドット区切りパスやコレクションインデックスは受け付けません（それらは `PropertyPathUtil.getField` を使用）。

```java
// フィールドを取得（単純名のみ）
Field field = ReflectionUtil.getDeclaredField(MyBean.class, "fieldName");

// スーパークラスのフィールドも検索
Field inherited = ReflectionUtil.getDeclaredField(SubClass.class, "parentField");
```

---

## フィールド値の取得

`setAccessible(true)` を使って private フィールドにもアクセスします。

```java
Field field = ReflectionUtil.getDeclaredField(MyBean.class, "fieldName");
Object value = ReflectionUtil.getFieldValue(bean, field);
```

---

## プロパティの取得（フィールド、またはgetter）

`getBeanProperty` は、まず同名のフィールドを（スーパークラスを含めて）検索し、存在しない場合は getter を検索します。
getter は JavaBeans の命名規則に従い、引数なし・非 static で戻り値のある `getXxx()`、またはプリミティブの `boolean` を返す `isXxx()` が対象です。
`Boolean`（ラッパー型）を返す `isXxx()` は、JavaBeans の命名規則に従い getter として扱われないため注意してください（`getXxx()` という名前にしてください）。
getter もアクセス修飾子に関わらず、スーパークラスを含めて検索します。
どちらも見つからない場合は、`NoSuchFieldException` を cause に持つ `RuntimeException` をスローします。

戻り値の `BeanProperty` は、フィールドと getter の違いを吸収し、型・アノテーション・値を同じ方法で取得できます。

```java
BeanProperty property = ReflectionUtil.getBeanProperty(MyBean.class, "name");

Class<?> type = property.getType();             // フィールドの型、または getter の戻り値の型
Type genericType = property.getGenericType();   // ジェネリクスを含む型
MyAnnotation ann = property.getAnnotation(MyAnnotation.class); // フィールド、または getter に付与されたアノテーション
Object value = property.getValue(bean);         // フィールドの値、または getter の戻り値
```

getter が例外をスローした場合は、どの getter かをメッセージに含めた `RuntimeException` でラップされます（cause は元の例外）。

---

## クラス階層からアノテーションを検索

クラス自身からスーパークラスへと順にさかのぼってアノテーションを検索します。

```java
Optional<MyAnnotation> ann = ReflectionUtil.searchAnnotationPlacedAtClass(
    myInstance.getClass(), MyAnnotation.class);

ann.ifPresent(a -> System.out.println(a.value()));
```

最初に見つかったアノテーションを返します。`Object.class` まで遡っても見つからない場合は空の `Optional` を返します。
