# ItemUtil

## Overview

`ItemUtil` (`jp.ecuacion.lib.core.util.ItemUtil`) is a utility class for working with `Item` and `ItemContainer`.

It sits at a higher layer that internally uses general-purpose utilities such as `PropertyPathUtil`,
and provides a pipeline specialized for ecuacion's Item model.
While [ItemContainer#getItem()](item-container.md) handles searching within an ItemContainer,
`ItemUtil` receives the rootBean and fullPropertyPath,
and provides the entire pipeline including finding an ItemContainer, delegating to it, and finalizing the `itemNameKey`.

---

## resolveItem()

```java
Item item = ItemUtil.resolveItem(fullPropertyPath, rootBean);
```

Executes the following pipeline and returns a finalized `Item` with `itemNameKey` and `showsValue` resolved.

1. Finds the ItemContainer from `fullPropertyPath` and `rootBean` (up to 1 level)
2. If an ItemContainer is found, delegates to `ItemContainer#getItem(itemPropertyPath)`
3. Finalizes `itemNameKey` and reflects `showsValue`
4. Returns the finalized `Item`

### ItemContainer Search Rules

| rootBean State | Behavior |
| --- | --- |
| rootBean itself is an `ItemContainer` | Calls `rootBean.getItem(fullPropertyPath)` |
| The child at the 1st node of `fullPropertyPath` is an `ItemContainer` | Calls `getItem(remaining path)` on that child |
| Neither | Resolves directly from type information via `PropertyPathUtil.getClass()` |

The search is **up to 1 level deep**.

### Usage Example

```java
// Example of using with Jakarta Validation's ConstraintViolation
Item item = ItemUtil.resolveItem(
    cv.getPropertyPath().toString(),
    cv.getRootBean()
);
```

---

## Difference from ItemContainer#getItem()

| | `ItemContainer#getItem()` | `ItemUtil#resolveItem()` |
| --- | --- | --- |
| Input base | ItemContainer itself | RootBean |
| ItemContainer discovery | Does not (it is the ItemContainer) | Yes (up to 1 level) |
| itemNameKey finalization | No | Yes |
| Return value | Intermediate object | Finalized object |
