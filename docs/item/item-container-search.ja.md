# ItemContainer の探索

## 概要

Jakarta Validation のバリデーションエラーメッセージを生成する際、エラーとなったフィールドに対応する `Item` は、
rootBean と違反の propertyPath をもとに `ItemContainer` を探索して解決されます。
本ページでは、その探索の仕組みを説明します。

---

## ItemContainer の検索範囲

`ItemUtil.resolveItem()` は、rootBean と propertyPath をもとに ItemContainer を探して
`Item` を解決するメソッドです。バリデーションエラーメッセージを生成する際などにフレームワーク内部から呼ばれます（詳細は [ItemUtil](item-util.md) を参照）。

この検索範囲は **rootBean から 1 階層まで** です。

---

## 補足: 兄弟関係にある複数の ItemContainer があっても曖昧にならない理由

例えば `UserForm` の直下に `UserDto`・`DeptDto` という 2 つの `ItemContainer` があり、両方とも `name` フィールドを持つ場合、
`itemPropertyPath` として単に `"name"` とだけ書いたのでは、どちらの `ItemContainer` を指しているか区別できないのでは、と疑問に思うかもしれません。
実際には、以下の 2 つの利用パターンにおいて、この曖昧性は生じません。

1. **Jakarta Validation でのメッセージ表示時**（違反の発生したフィールドを起点とするケース）: バリデーション違反が発生したフィールドは、rootBean から辿って
   Jakarta Validation 自身がすでに特定済みです。`ItemContainer` の探索は、その特定済みの rootBean 起点の propertyPath（fullPropertyPath。例: `"userDto.name"`）を起点に行われるため、
   単なる `"name"` のような曖昧な形になることはありません。
2. **splib の Thymeleaf による項目名表示時**（rootBean から順に対象の item を辿るケース）: 各コンポーネントに記載する `itemPropertyPath` は省略形
   （例: `"name"`）であっても、その省略された部分（どの `ItemContainer` のスコープかという情報）は別途指定されているため、
   コンポーネント全体としては対象を一意に特定できます。
