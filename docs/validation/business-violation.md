# BusinessViolation

## Passing Placeholder Arguments

```java
// "Age", "0", "150" are embedded at {0}, {1}, {2} respectively
violations.add(new BusinessViolation("error.range", "Age", "0", "150"));
```

---

## Specifying Related Fields

You can associate a violation with which field it relates to using `itemPropertyPath`.

```java
violations.add(new BusinessViolation(new String[] {"birthDate"}, "error.future-date"));

// Violation spanning multiple fields
violations.add(new BusinessViolation(
    new String[] {"startDate", "endDate"}, "error.date-range"));
```

---

## Embedding Item Names in Messages (itemNameKeys)

Define a message containing the `{item_name}` placeholder in `messages.properties`,
and specify `itemNameKeys` to have the item name resolved from `item_names.properties` when the message is generated.

```properties
# messages.properties
error.already-registered={item_name} is already registered
```

```properties
# item_names.properties
customer.email=Email Address
```

```java
// 1st argument: itemNameKeys (keys in item_names.properties)
// 2nd argument: itemPropertyPaths (for UI field highlighting)
violations.add(new BusinessViolation(
    new String[] {"customer.email"},
    new String[] {"email"},
    "error.already-registered"));
// → "Email Address is already registered"
```

When `itemNameKeys` is not needed (not using `{item_name}`), specify only `itemPropertyPaths` as before.

---

## violations.add() Shorthand

You can directly specify a message key and other parameters on `Violations` without explicitly creating a `BusinessViolation`.

```java
// Standard way (with explicit BusinessViolation)
violations.add(new BusinessViolation("error.some-message-id"));

// Equivalent shorthand
violations.add("error.some-message-id");
```

Overloads with `itemPropertyPath` and arguments are similarly available.

```java
violations.add(new String[] {"fieldName"}, "error.message");
violations.add("error.range", "Age", "0", "150");
```
