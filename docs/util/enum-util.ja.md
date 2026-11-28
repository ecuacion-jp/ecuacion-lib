# EnumUtil

`EnumUtil`（`jp.ecuacion.lib.core.util.EnumUtil`）は ecuacion ライブラリの enum フォーマット向けのユーティリティクラスです。

---

## ecuacion の各種モジュールにおける enum フォーマット

ecuacion の各種モジュールでは、enum に以下の2つのプロパティを持たせる形式を標準として使用します。

- **`code`**: DB保存や HTML の dropdown 識別などに使う固定コード値
- **表示名（displayName）**: 画面表示用の名前。`PropertiesFileUtil.getEnumName(...)` で
  `enum_names.properties` から取得

コードジェネレーターで生成される enum もこの形式に従います。

```java
public enum StatusEnum {
    ACTIVE("1"), INACTIVE("0");

    private final String code;

    StatusEnum(String code) { this.code = code; }

    public String getCode() { return code; }

    public String getDisplayName(Locale locale) {
        return PropertiesFileUtil.getEnumName(locale, "StatusEnum." + name());
    }
}
```

---

## コードから enum 値を取得

```java
// コード "1" に対応する StatusEnum.ACTIVE を取得
StatusEnum status = EnumUtil.getEnumFromCode(StatusEnum.class, "1");

// コードが存在するか確認
boolean exists = EnumUtil.hasEnumFromCode(StatusEnum.class, "99"); // false
```

コードが存在しない場合、`getEnumFromCode` は `RuntimeException` をスローします。

---

## HTML の select 要素用リスト

コードと表示名のペア（`String[]`）のリストを返します。Thymeleaf の select タグなどにそのまま渡せます。

```java
// [["1", "有効"], ["0", "無効"]] のようなリストを返す
List<String[]> items = EnumUtil.getListForHtmlSelect(
    StatusEnum.class, Locale.JAPANESE, null);
```

### フィルタリングオプション

オプション文字列で要素を絞り込むことができます。
`firstCharOfCodeLessThanOrEqualTo` / `firstCharOfCodeGreaterThanOrEqualTo` は、
ステータスをコードの数値で表し、進捗ごとに数値が増えていく場合などに、一定以上（または以下）のステータスの要素に絞り込む用途に使用します。

| オプション | 説明 | 例 |
| --- | --- | --- |
| `including=V1\|V2` | 指定した名前の要素のみ含める | `including=ACTIVE\|PENDING` |
| `excluding=V1\|V2` | 指定した名前の要素を除外する | `excluding=DELETED` |
| `firstCharOfCodeEqualTo=X\|Y` | コードの先頭文字が一致する要素のみ | `firstCharOfCodeEqualTo=1\|2` |
| `firstCharOfCodeLessThanOrEqualTo=X` | コードの先頭文字が X 以下 | `firstCharOfCodeLessThanOrEqualTo=3` |
| `firstCharOfCodeGreaterThanOrEqualTo=X` | コードの先頭文字が X 以上 | `firstCharOfCodeGreaterThanOrEqualTo=2` |

オプションは1種類のみ指定可能です。複数指定するとエラーになります。

```java
// ACTIVE のみ
EnumUtil.getListForHtmlSelect(StatusEnum.class, Locale.JAPANESE, "including=ACTIVE");

// DELETED を除外
EnumUtil.getListForHtmlSelect(StatusEnum.class, Locale.JAPANESE, "excluding=DELETED");
```

---

## EnumClassInfo / EnumValueInfo の取得

enum クラスの情報（値一覧・コード・表示名）を一括取得します。

```java
EnumUtil.EnumClassInfo<StatusEnum> info =
    EnumUtil.getEnumInfo(StatusEnum.class, Locale.JAPANESE);

for (EnumUtil.EnumValueInfo<StatusEnum> valueInfo : info.getValueList()) {
    String name        = valueInfo.getName();     // "ACTIVE"
    String code        = valueInfo.getCode();     // "1"
    String label       = valueInfo.getLabel();    // "有効"
    StatusEnum instance = valueInfo.getInstance(); // StatusEnum.ACTIVE
}
```

ロケール指定なしの場合は `Locale.ROOT` が使用されます。

```java
EnumUtil.EnumClassInfo<StatusEnum> info = EnumUtil.getEnumInfo(StatusEnum.class);
```
