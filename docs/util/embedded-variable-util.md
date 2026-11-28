# EmbeddedVariableUtil

`EmbeddedVariableUtil` (`jp.ecuacion.lib.core.util.EmbeddedVariableUtil`) is a utility class that finds variables
enclosed by a start symbol and an end symbol, like `${VAR}`, in a string and replaces them with values.
It can be used, for example, to replace environment variables embedded in values of configuration files.

---

## getVariableReplacedString — Replacing Variables with Values

Specify the start symbol, the end symbol, and how to obtain a value from a variable name (a `Map` or a `Function`).

```java
// Obtain values from a Map
Map<String, String> map = Map.of("host", "example.com", "port", "8080");
EmbeddedVariableUtil.getVariableReplacedString("http://${host}:${port}/", "${", "}", map);
// → "http://example.com:8080/"

// Obtain values from a Function (e.g. environment variables)
EmbeddedVariableUtil.getVariableReplacedString("${HOME}/work", "${", "}", System::getenv);
```

- The positions of all variables are determined before they are replaced.
  So even if a replaced value contains the start or end symbol, the value is not treated as a variable again.

    ```java
    // The value of key1, "${x", is output as is
    getVariableReplacedString("a${key1}b", "${", "}", Map.of("key1", "${x"));  // "a${xb"
    ```

- A start or end symbol preceded by `\` is not treated as a variable.
  However, the `\` is not removed and remains as is (`"a\${b\}c"` → `"a\${b\}c"`).
- An empty string cannot be specified as the start or end symbol (`RequireNonEmptyException` is thrown).

### Exceptions

| Exception | Thrown when |
| --- | --- |
| `EmbeddedVariableUtil.VariableNotFoundException` | No value is found for a variable name (the `Map` doesn't have the key, the `Function` returns `null`, etc.) |
| `EmbeddedVariableUtil.StringFormatIncorrectException` | An end symbol appears before a start symbol, or a symbol has no counterpart (`"a}b"`, `"${a"`, etc.) |

Both are subclasses of `ViolationException`, and localized messages can be obtained with `ExceptionUtil.getMessageList(...)`.

---

## Options — Ignoring End Symbols on Their Own

With `Options.setIgnoresEmergenceOfEndSymbolOnly(true)`, an end symbol that appears before a start symbol
is treated as a normal string instead of causing a `StringFormatIncorrectException`.

```java
var options = new EmbeddedVariableUtil.Options().setIgnoresEmergenceOfEndSymbolOnly(true);

// The "}" of "{0}" is ignored, and only "{+item_names:user.id}" is treated as a variable
EmbeddedVariableUtil.getVariableReplacedString("{0} is invalid. ({+item_names:user.id})",
    "{+item_names:", "}", Map.of("user.id", "User ID"), options);
// → "{0} is invalid. (User ID)"
```

Use it when the end symbol is shared with another format like `{0}`.

---

## getPartList — Dividing into String and Variable Parts

Returns a list of the parts of the string, divided into normal string parts and variable parts, without replacing them.
Multiple start symbols can be specified (the end symbol is common).

```java
List<Pair<String, String>> list = EmbeddedVariableUtil.getPartList(
    "a${+b}c${-d}", new String[] {"${+", "${-"}, "}");
// → [(null, "a"), ("${+", "b"), (null, "c"), ("${-", "d")]
```

The left side of each element is the start symbol (`null` for a normal string part), and the right side is the variable name or the string.
An empty list is returned when an empty string is passed.
