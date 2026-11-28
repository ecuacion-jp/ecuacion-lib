# StringUtil

`StringUtil` (`jp.ecuacion.lib.core.util.StringUtil`) is a utility class for string operations.
It implements methods that are not provided by Apache Commons Lang's `StringUtils`.
Use this when you need operations not available in `StringUtils`.

The handling of `null` and empty strings basically aims at the same behavior as `StringUtils`
(for example, an empty string returns an empty string).
However, since ecuacion-lib adopts JSpecify and is basically `@NullMarked`, `StringUtil` does not return `null`.
Therefore, where `StringUtils` would return `null`, `StringUtil` throws a `NullPointerException` when `null` is passed as an argument.

---

## Case Conversion

### snake_case → lowerCamelCase / UpperCamelCase

```java
StringUtil.getLowerCamelFromSnake("user_name");              // "userName"
StringUtil.getLowerCamelFromSnake("VALIDATION_MESSAGES_JA"); // "validationMessagesJa"
StringUtil.getUpperCamelFromSnake("user_name");              // "UserName"
```

`getLowerCamelFromSnake` supports the following input formats:

| Input Example | Output Example |
| --- | --- |
| `user_name` | `userName` |
| `VALIDATION_MESSAGES_JA` | `validationMessagesJa` |
| `ValidationMessages_ja` | `validationMessagesJa` |

Strings with a leading or trailing `_`, or strings containing `__` (consecutive underscores)
will throw a `RuntimeException`.

### camelCase → lower_snake_case

```java
StringUtil.getLowerSnakeFromCamel("userName"); // "user_name"
StringUtil.getLowerSnakeFromCamel("UserName"); // "user_name"
```

---

## Number Formatting

Adds grouping separators to a number string.

```java
StringUtil.toNumberWithGroupingSeparators("1234567");    // "1,234,567"
StringUtil.toNumberWithGroupingSeparators("1234567.50"); // "1,234,567.50"

StringUtil.toNumberWithGroupingSeparators("1234567.50", Locale.GERMANY); // "1.234.567,50"
```

Without a locale, the grouping separator is always `,` and the decimal separator is `.`, regardless of the default locale.
With a locale, the grouping separator and the decimal separator of that locale are used.

Both integers and decimals are supported, with no limit on the number of digits.
The digits after the decimal point are kept as they are in the input (not rounded).

The former `toCurrencyFormat` is deprecated.
Since it depends on the default locale and supports integers (within the range of `int`) only, use `toNumberWithGroupingSeparators` instead.

---

## Generating Delimited Strings

Joins multiple strings with a delimiter.

```java
List<String> items = List.of("apple", "banana", "cherry");

// Delimiter only
StringUtil.getSeparatedValuesString(items, ", ");
// → "apple, banana, cherry"

// Specify surrounding characters for each element
StringUtil.getSeparatedValuesString(items, ", ", "'");
// → "'apple', 'banana', 'cherry'"

// Different surrounding characters for left and right
StringUtil.getSeparatedValuesString(items, ", ", "[", "]");
// → "[apple], [banana], [cherry]"
```

Overloads that take an array (`String[]`) as an argument have the same signature.

### CSV

```java
StringUtil.getCsv("a", "b", "c");          // "a,b,c"
StringUtil.getCsvWithSpace("a", "b", "c"); // "a, b, c"
```

`getCsvWithSpace` is intended for situations where readability is important, such as log output or code generation.

---

## Null / Empty Check

```java
StringUtil.isObjectNullOrEmpty(null);   // true
StringUtil.isObjectNullOrEmpty("");     // true
StringUtil.isObjectNullOrEmpty("abc"); // false
StringUtil.isObjectNullOrEmpty(123);   // false (always false for non-String types)
```

Checks whether an `Object` value is `null` or an empty string.
Used for judging field values retrieved via reflection when the type is `Object`.
