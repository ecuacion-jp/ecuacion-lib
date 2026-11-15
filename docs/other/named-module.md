# Using in Named Module Environments

## Overview

Some features of `ecuacion-lib` access fields, getters and methods of application classes
(records, forms, etc.) via reflection.

In unnamed module environments without `module-info.java`, such as Spring Boot fat JARs,
no configuration is needed.
In named module environments with `module-info.java`, you need to `opens` the packages of
those classes to the `ecuacion-lib` modules.

---

## Features That Use Reflection

| Feature | Accessing Module | Without `opens` |
| ------- | ---------------- | --------------- |
| Obtaining values in class validators with `propertyPath` (comparison validators, conditional validators, collection assertions, etc.) | `jp.ecuacion.lib.core` | An exception is thrown |
| Obtaining values in `@ValueOfPropertyPathWhen` / `@NotValueOfPropertyPathWhen` | `jp.ecuacion.lib.core` | An exception is thrown |
| Resolving item names in error messages (for nested items) | `jp.ecuacion.lib.core` | An exception is thrown |
| Calling methods in `@ReturnTrue` | `jp.ecuacion.lib.validation` | An exception is thrown |
| Calling `customizedItems()` of `ItemContainer` (including inheritance from parent classes) | `jp.ecuacion.lib.core` | An exception is thrown (with `exports` only instead of `opens`, no exception is thrown, but inheritance from parent classes does not work) |

To obtain values, `setAccessible(true)` is used so that `private` fields can also be accessed.
Therefore, `InaccessibleObjectException` is thrown for classes in packages that are not opened.

---

## module-info.java Settings

Open the packages of those classes to the `ecuacion-lib` modules you use.

```java
module com.example.myapp {
    requires jp.ecuacion.lib.core;
    requires jp.ecuacion.lib.validation;

    opens com.example.myapp.record to
        org.hibernate.validator, jp.ecuacion.lib.core, jp.ecuacion.lib.validation;
}
```

- `opens` to `jp.ecuacion.lib.validation` is needed only when you use `@ReturnTrue`.
- `opens` to `org.hibernate.validator` is needed regardless of `ecuacion-lib`
  when you use Jakarta Validation (Hibernate Validator) in named module environments.

For `module-info.java` settings related to loading `.properties` files,
see [SPI](spi.md).
