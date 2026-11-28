# PatternWithDescription

## Overview

The standard `@Pattern` displays the regex directly in the message when validation fails,
which is not suitable for showing to end users.

`@PatternWithDescription` allows you to write a human-readable description in the `description` attribute,
enabling user-friendly error messages.

```java
// With @Pattern
// → "must match "^[0-9]{7}$""

// With @PatternWithDescription
@PatternWithDescription(
    regexp = "^[0-9]{7}$",
    description = "7-digit number"
)
private String postalCode;
// → "Please enter in the correct format (7-digit number)"
```

---

## Setting Up ValidationMessages.properties

The message template for `@PatternWithDescription` is defined in `ValidationMessages.properties`.
`{description}` is replaced with the value of the `description` attribute.

```properties
# ValidationMessages.properties
jp.ecuacion.lib.validation.constraints.PatternWithDescription.message = Please enter in the correct format ({description})
```

`ecuacion-lib-validation` includes a default message, so the default is used if you do not define your own.

When `description` is omitted, the regular expression itself is shown in the message.

---

## Localizing description

By writing a property key in the `description` attribute and defining the actual description text
in a properties file, you can localize the description.

```java
@PatternWithDescription(
    regexp = "^[A-Z][a-z]*$",
    description = "description.firstName"  // Used as a key
)
private String firstName;
```

The description text for the key is defined in `ValidationMessagesPatternDescriptions.properties`,
a dedicated file for pattern descriptions.

```properties
# ValidationMessagesPatternDescriptions.properties
description.firstName = First letter uppercase, rest lowercase (e.g., John)
```

Result:

```
Please enter in the correct format (First letter uppercase, rest lowercase (e.g., John))
```

For localized versions, add a suffix like `ValidationMessagesPatternDescriptions_ja.properties`.
When the key is not found, the string specified in `description` is shown as it is.
