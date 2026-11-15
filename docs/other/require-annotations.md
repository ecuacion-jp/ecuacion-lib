# Require Annotations

## Overview

The `jp.ecuacion.lib.core.annotation` package provides **annotations for declaring requirements** that can be placed on method parameters.

These annotations themselves **do nothing**. They perform no processing at runtime, and do not provide compile-time warnings like IDE static analysis tools (such as jspecify's `@NonNull` / `@Nullable`). They function solely as documentation indicating "this method expects this argument to satisfy a certain condition."

Actual validation is implemented inside the method using the corresponding methods of `ObjectsUtil`.

---

## List of Annotations

| Annotation | Meaning | ObjectsUtil Method | Exception Thrown |
| ------------- | ---- | ------------------- | -------------- |
| `@RequireNonEmpty` | The argument must not be empty (null or blank string) | `requireNonEmpty(String value)` | `RequireNonEmptyException` |
| `@RequireSizeNonZero` | The collection/array must have at least 1 element | `requireSizeNonZero(Collection/array)` | `RequireSizeNonZeroException` |
| `@RequireElementNonNull` | Each element in the collection/array must not be null | `requireElementNonNull(Collection/array)` | `RequireElementNonNullException` |
| `@RequireElementNonEmpty` | Each element in the collection/array must not be empty | `requireElementNonEmpty(Collection/array)` | `RequireElementNonEmptyException` |
| `@RequireElementNonDuplicated` | No element in the collection/array may be duplicated | `requireElementsNonDuplicated(Collection/array)` | `RequireElementsNonDuplicatedException` |

Each method returns the argument as-is after validation, so it can be used in method chains.

```java
String validated = ObjectsUtil.requireNonEmpty(name);
```

Note that there is no annotation corresponding to `ObjectsUtil.requireNonNull()`.
This method is used to null-check a `@Nullable` value at runtime and return it as `@NonNull`.

---

## Usage

Declare with the annotation and actually validate using `ObjectsUtil` inside the method.

```java
import jp.ecuacion.lib.core.annotation.RequireNonEmpty;
import jp.ecuacion.lib.core.util.ObjectsUtil;

public void process(@RequireNonEmpty String name, @RequireSizeNonZero List<String> items) {
    ObjectsUtil.requireNonEmpty(name);
    ObjectsUtil.requireSizeNonZero(items);

    // From here on, name is guaranteed to be non-empty and items to have at least 1 element
}
```

---

## Difference from @NonNull / @Nullable

| | Require Annotations | jspecify's `@NonNull` / `@Nullable` |
| --- | --- | --- |
| Runtime validation | Explicitly implemented with `ObjectsUtil` | None (declaration only) |
| IDE static analysis | Not supported | Supported (warning display) |
| Purpose | Clarifying implementation intent + runtime validation | Compile-time null safety checks |

`@NonNull` / `@Nullable` excels at compile-time static analysis, while Require annotations are used for actual runtime validation. Both can be used together.
