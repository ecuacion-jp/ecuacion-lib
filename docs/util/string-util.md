# StringUtil

`StringUtil` (`jp.ecuacion.lib.core.util.StringUtil`) is a utility class for string operations.
It implements methods that are not provided by Apache Commons Lang's `StringUtils`.
Use this when you need operations not available in `StringUtils`.

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

```java
StringUtil.toCurrencyFormat("1234567"); // "1,234,567"
```

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
