# ExceptionUtil

`ExceptionUtil` (`jp.ecuacion.lib.core.util.ExceptionUtil`) is a utility class for
extracting message strings from exceptions.
It is used when implementing custom exception handlers.

---

## Separating Validation from Message Generation

In standard Jakarta Validation, messages are generated at the moment `validator.validate(...)` is called.
`ConstraintViolation.getMessage()` simply returns the pre-generated string.

```java
// Standard case: message is generated at the same time as validate
Set<ConstraintViolation<Account>> set = validator.validate(account);
set.forEach(v -> System.out.println(v.getMessage())); // Already converted to string
```

This means that for multilingual systems that switch the locale per user,
you need to pass `Locale` as an argument to service layer methods.

```java
// Forced to bring locale into the service layer
public void createAccount(Account account, Locale locale) {
    ValidatorFactory factory = Validation.byDefaultProvider().configure()
        .messageInterpolator(new LocaleSpecificMessageInterpolator(locale))
        .buildValidatorFactory();
    ...
}
```

### Solution: Separate Validation from Message Generation

1. Validate in the service layer → throw an exception if violations exist (no locale needed)
2. Catch in ExceptionHandler → generate messages using the locale there

```java
// Service layer: does not involve locale
public void createAccount(Account account) {
    Set<ConstraintViolation<Account>> set = validator.validate(account);
    if (!set.isEmpty()) {
        throw new ConstraintViolationException(set);
    }
}

// ExceptionHandler: generate messages using locale here
List<String> messages = ExceptionUtil.getMessageList(ex, Locale.ENGLISH);
```

Messages are generated at the time `ExceptionUtil.getMessageList()` is called.

---

## getMessageList — Get a Message List from an Exception

Returns a list of message strings in a unified manner regardless of the exception type.

```java
List<String> messages = ExceptionUtil.getMessageList(throwable);

// With locale
List<String> messages = ExceptionUtil.getMessageList(throwable, Locale.ENGLISH);
```

Internally, processing is dispatched based on the exception type as follows:

| Exception Type | Processing |
| --- | --- |
| `ViolationException` | Builds messages from each Violation in `Violations` |
| `ConstraintViolationException` | Builds messages from each `ConstraintViolation` |
| Others | Returns `throwable.getMessage()` as-is |

Since `ConstraintViolationException` can hold multiple violations, the return value is always a `List`.
Regular exceptions also return a `List` (with 1 element).

---

## Overloads by Input Type

In addition to `Throwable`, there are overloads for directly specifying types.

```java
// From Violations
List<String> messages = ExceptionUtil.getMessageList(violations);
List<String> messages = ExceptionUtil.getMessageList(violations, Locale.ENGLISH);

// From a Set of ConstraintViolation
List<String> messages = ExceptionUtil.getMessageList(constraintViolationSet);
List<String> messages = ExceptionUtil.getMessageList(constraintViolationSet, Locale.ENGLISH);
```

---

## isMessagesWithItemNamesAsDefault Parameter

Some overloads have an `isMessagesWithItemNamesAsDefault` flag.

```java
// Setting true prioritizes messages_with_item_names.properties
List<String> messages = ExceptionUtil.getMessageList(throwable, Locale.ENGLISH, true);
```

Setting it to `true` prioritizes `ValidationMessagesWithItemNames.properties`
for resolving validation messages.
This is a value set as the system default by frameworks such as splib.

When `MessageParameters.isMessageWithItemName` is explicitly set to `true` / `false` per Violation,
that value takes priority over this default.
For details on the relationship with `isMessageWithItemName`, see **violation > MessageParameters**.
