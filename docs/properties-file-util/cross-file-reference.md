# Cross-File References

## Overview

You can reference keys from other properties files using the `#{...}` syntax within property values
in `messages.properties` and similar files.
This makes it easy to reuse item names in error messages and centrally manage common strings.

---

## `#{fileKind:key}` Syntax

By writing `#{fileKind:key}` in a property value, it expands to the value of the key in the specified file type.

```properties
# messages.properties
error.name.required=#{item_names:user.name} is required.

# item_names.properties
user.name=Full Name
```

Calling `getMessage(locale, "error.name.required")` embeds the value of `user.name` from `item_names.properties`.

```text
Full Name is required.
```

The available file types are all values of `PropertiesFileUtilFileKindEnum`.

| fileKind | Target File |
| --- | --- |
| `messages` | messages[_xxx].properties |
| `messages_with_item_names` | messages_with_item_names[_xxx].properties |
| `item_names` | item_names[_xxx].properties |
| `enum_names` | enum_names[_xxx].properties |
| `constants` | constants[_xxx].properties |
| `ValidationMessages` | ValidationMessages[_xxx].properties |
| `ValidationMessagesWithItemNames` | ValidationMessagesWithItemNames[_xxx].properties |
| `ValidationMessagesPatternDescriptions` | ValidationMessagesPatternDescriptions[_xxx].properties |

---

## `#{key}` Syntax (File Type Omitted)

Using the `#{key}` syntax without specifying a file type automatically searches in the order
`messages` → `item_names` → `enum_names` → `constants`.

```properties
# messages.properties
error.name.required=#{user.name} is required.
```

If the key `user.name` is defined in `item_names.properties`, the value is automatically retrieved from there.

---

## Recursive Resolution

The `#{...}` syntax is resolved recursively.

```properties
# messages.properties
greeting=Hello, #{messages:user.title}-#{messages:user.name}.
user.title=Mr./Ms.
user.name=Smith
```

Result of `getMessage(locale, "greeting")`:

```text
Hello, Mr./Ms.-Smith.
```

Even deeper references are possible.

```properties
full.message=#{messages:part1}-#{messages:part2}
part1=a
part2=b-#{messages:part3}
part3=c
```

Result of `getMessage(locale, "full.message")`: `a-b-c`

---

## Practical Example: Reusing Item Names in Validation Error Messages

A typical use case is to reference item names defined in `item_names.properties`
in form validation error messages.

```properties
# item_names.properties
user.name=Full Name
user.email=Email Address
user.age=Age

# messages.properties
error.required=#{item_names:{0}} is required.
error.invalid.email=#{item_names:{0}} format is invalid.
```

By defining it this way, you can manage item names in one place and reference them from multiple error messages.
