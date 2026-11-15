# Args

## Overview

By using placeholders such as `{0}`, `{1}` in values in `messages.properties` and similar files,
you can embed dynamic values at runtime.

Arguments are passed in `Object...` format. The `Arg` class enables advanced control,
such as specifying the argument itself as a property key.

---

## Passing Directly as Object...

This is the simplest approach. You can pass any type such as strings, numbers, and dates.

Java's standard `MessageFormat` is used for formatting,
so type-specific patterns like `{0,number,#,###}` can be used directly.

```properties
# messages.properties
error.range={0} must be between {1} and {2}.
amount.label=Total: {0,number,#,###}
```

```java
// Passing strings
String msg = PropertiesFileUtil.getMessage(
    Locale.ENGLISH, "error.range", "Age", "0", "150");
// => "Age must be between 0 and 150."

// Passing a number directly → MessageFormat's type-aware formatting takes effect
String msg = PropertiesFileUtil.getMessage(Locale.ENGLISH, "amount.label", 1234567);
// => "Total: 1,234,567"
```

Since it's `Object...`, you can pass `String`, `Integer`, `Date`, and more directly.

---

## Using Arg

The `Arg` class lets you resolve the argument itself as a property key,
or perform further formatting within the Arg.

### Arg.message(String) — Pass a Message ID as an Argument

Resolves the argument itself as a key in `messages.properties`.

```properties
# messages.properties
greeting=Hello, {0}.
role.admin=Administrator
```

```java
getMessage(locale, "greeting", Arg.message("role.admin"));
// => "Hello, Administrator."
```

Useful when you want to switch the argument itself depending on the locale.

### Arg.message(String, Object...) — Message ID with Arguments

```properties
# messages.properties
item.range.desc={0} ({1}~{2})
field.age=Age
```

```java
getMessage(locale, "...", Arg.message("item.range.desc", "field.age", "0", "150"));
```

### Arg.formattedString(String, Object...) — Format within an Arg

Since property keys can be embedded with `#{key}`, you can construct localized strings without writing literal strings.

```properties
# messages.properties
required=(required)
```

```java
Arg.formattedString("{0} #{required}", Arg.message("field.name"))
// => "Full Name (required)"
```

### Arg.itemName(String) / Arg.constant(String) / Arg.enumName(String) — Reference Other File Types

Used when you want to reference keys from file types other than `messages.properties`.

```java
// Use a key from item_names.properties as an argument
getMessage(locale, "greeting", Arg.itemName("user.name"));
// => "Hello, Full Name."

// Use a key from constants.properties as an argument
getMessage(locale, "message", Arg.constant("max.file.size"));

// Use a key from enum_names.properties as an argument
getMessage(locale, "message", Arg.enumName("status.active"));
```

---

## Mixing Arg and Object

In `Object...` parameters, `Arg` and plain Objects can be freely mixed.
`Arg` is resolved as a message ID, while other `Object` values are passed directly to `MessageFormat`.

```java
// Mixing Arg.message and a number
getMessage(locale, "message",
    Arg.message("role.admin"),  // → "Administrator" (resolved from messages.properties)
    1234567);                   // → 1,234,567 (type-aware formatting)
```
