# Basic Usage

## What is PropertiesFileUtil?

`PropertiesFileUtil` (`jp.ecuacion.lib.core.util.PropertiesFileUtil`) is a utility class for
centrally loading various `.properties` files within an application.

It is based on Java's standard `ResourceBundle` with additional features. (Details described below)

---

## Supported File Types

| File Name | Retrieval Method | Locale | Description |
| --- | --- | :---: | --- |
| `application[_xxx].properties` | `getApplication(...)` | | Application configuration values |
| `constants[_xxx].properties` | `getConstant(...)` | | Non-localized strings (constants) |
| `messages[_xxx].properties` | `getMessage(...)` | ✓ | Localized messages |
| `messages_with_item_names[_xxx].properties` | `getMessageWithItemName(...)` | ✓ | Messages including item names |
| `item_names[_xxx].properties` | `getItemName(...)` | ✓ | Item names |
| `enum_names[_xxx].properties` | `getEnumName(...)` | ✓ | Display names for Enum values |
| `ValidationMessages[_xxx].properties` | `getValidationMessage(...)` | ✓ | Validation messages |
| `ValidationMessagesWithItemNames[_xxx].properties` | `getValidationMessageWithItemName(...)` | ✓ | Validation messages with item names |
| `ValidationMessagesPatternDescriptions[_xxx].properties` | `getValidationMessagePatternDescription(...)` | ✓ | Pattern description text |

The 3 `ValidationMessages` variants have a different argument format from the others.
For details, see [ValidationMessages](validation-messages.md).

For methods with ✓ in the Locale column, if the Locale argument is omitted or `null` is passed, it is treated as `Locale.ROOT` (the value is obtained from the file without a locale, like `messages.properties`).
However, arguments such as numbers and dates are formatted with the locale of `LocaleUtil.getFallbackLocale()`.

The existence check by `hasXxx(...)` is done against the files without a locale.
Files with a locale (like `messages_ja.properties`) are supposed to have the same keys as the file without a locale.

If an empty string is passed as the key, `getXxx(...)` throws an `IllegalArgumentException`, and `hasXxx(...)` returns `false`.

---

## Reading application.properties

```java
String value = PropertiesFileUtil.getApplication("app.title");

// Check if a key exists
boolean exists = PropertiesFileUtil.hasApplication("app.title");

// Get with a default value (returns second argument if key is not found)
String value = PropertiesFileUtil.getApplicationOrElse("app.optional-key", "default-value");
```

---

## Reading constants.properties

A file for storing fixed strings that do not require localization (URL prefixes, code snippets, common constants, etc.).

```java
String value = PropertiesFileUtil.getConstant("common.app-name");
boolean exists = PropertiesFileUtil.hasConstant("common.app-name");
```

---

## Reading messages.properties

`messages.properties` is a locale-aware message file.

```java
// Get with a specified locale
String msg = PropertiesFileUtil.getMessage(Locale.ENGLISH, "error.required");

// Without locale (uses Locale.ROOT)
String msg = PropertiesFileUtil.getMessage("error.required");

// Check if a key exists
boolean exists = PropertiesFileUtil.hasMessage("error.required");
```

---

## Reading messages_with_item_names.properties

Stores messages where `{item_name}` is the item name placeholder.
The difference from `messages.properties` is that messages contain `{item_name}`.
The framework replaces `{item_name}` with the actual item name before displaying it to the user.

If a key is not found, it automatically falls back to `messages.properties`.

```java
// With locale
String msg = PropertiesFileUtil.getMessageWithItemName(Locale.ENGLISH, "error.required");

// Without locale (uses Locale.ROOT)
String msg = PropertiesFileUtil.getMessageWithItemName("error.required");

boolean exists = PropertiesFileUtil.hasMessageWithItemName("error.required");
```

### When to Use vs. messages.properties

Since it falls back to `messages.properties`, it works even without creating a separate file.
Calling `getMessage(...)` and `getMessageWithItemName(...)` with the same key returns the same value
when only `messages.properties` exists.

A typical case for creating a separate file is when displaying errors in two places simultaneously on a web screen:

- **Error message list** (e.g., top of screen) → With item name: "Full Name is required"
- **Next to/below each field** → Without item name: "is required"

In this case, by defining the message without item name in `messages.properties` and the message with item name in `messages_with_item_names.properties`, you can serve both displays with the same key.

---

## Reading item_names.properties

A file that stores localized display names for item names (field labels) on screen.
Used for the `{0}` part in messages like "`{0}` is required" in validation error messages.

If a key is not found, it falls back to `messages.properties`.
This fallback allows `getItemName(...)` to work using only `messages.properties` without creating `item_names.properties`.

```java
// With locale
String itemName = PropertiesFileUtil.getItemName(Locale.ENGLISH, "user.name");

// Without locale (uses Locale.ROOT)
String itemName = PropertiesFileUtil.getItemName("user.name");

boolean exists = PropertiesFileUtil.hasItemName("user.name");
```

---

## Reading enum_names.properties

A file that stores localized display names for Enum values.
For example, defines display names like "Active" for the key `Status.ACTIVE`.

If a key is not found, it falls back to `messages.properties`.

```java
// With locale
String enumName = PropertiesFileUtil.getEnumName(Locale.ENGLISH, "Status.ACTIVE");

// Without locale (uses Locale.ROOT)
String enumName = PropertiesFileUtil.getEnumName("Status.ACTIVE");

boolean exists = PropertiesFileUtil.hasEnumName("Status.ACTIVE");
```
