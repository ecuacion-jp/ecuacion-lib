# PropertyPathUtil

`PropertyPathUtil` (`jp.ecuacion.lib.core.util.PropertyPathUtil`) provides
manipulation of propertyPath strings and navigation of object graphs using propertyPath.

propertyPath is the path format used by Jakarta Validation's `ConstraintViolation`,
such as `"address.city"` or `"items[0].name"`.

---

## propertyPath String Operations

```java
// Get the rightmost node
String node = PropertyPathUtil.getRightMostNode("address.city"); // "city"
String node2 = PropertyPathUtil.getRightMostNode("items[0].<list element>"); // "items[0].<list element>"

// Get the path without the rightmost node (leafBean path)
String parent = PropertyPathUtil.getPropertyPathWithoutRightMostNode("address.city"); // "address"
String empty = PropertyPathUtil.getPropertyPathWithoutRightMostNode("fieldName");     // ""

// Get the list of nodes
List<String> nodes = PropertyPathUtil.getNodeList("address.city"); // ["address", "city"]

// Convert to field path by completely removing collection notation (used for getting itemNameKey)
String fieldPath = PropertyPathUtil.toFieldPath("items[0].<list element>"); // "items"
String fieldPath2 = PropertyPathUtil.toFieldPath("userList[1].name");       // "userList.name"

// Convert to index-less normalized path by removing only indices (used for matching with customizedItems())
String idxless = PropertyPathUtil.toIndexlessPath("items[1].<list element>"); // "items[]"
String idxless2 = PropertyPathUtil.toIndexlessPath("userList[1].name");        // "userList[].name"
```

---

## Getting Fields by propertyPath

Supports dot-separated nested paths.
Internally uses `ReflectionUtil.getDeclaredField` to search the class hierarchy.

```java
// Get a nested field
Field city = PropertyPathUtil.getField(MyBean.class, "address.city");

// Simple fields also work
Field name = PropertyPathUtil.getField(MyBean.class, "name");
```

---

## Getting Properties by propertyPath (a Field, or a Getter)

Traverses a dot-separated path like `getField`, but each node is also resolved to a getter when no field of the name exists
(see `ReflectionUtil.getBeanProperty`).

```java
BeanProperty city = PropertyPathUtil.getBeanProperty(MyBean.class, "address.city");
Class<?> type = city.getType();
```

---

## Getting Classes by propertyPath

Traverses the property path and returns the type of the target property.
Each node is resolved to a getter when no field of the name exists.
Also supports generic type parameters for collections (`List`, `Set`, `Map`).

```java
// Returns the type of MyBean.address.city field
Class<?> cls = PropertyPathUtil.getClass(MyBean.class, "address.city");

// For List<Book> bookList, returns Book.class
Class<?> bookCls = PropertyPathUtil.getClass(MyBean.class, "bookList[0]");

// Returns the rootBeanClass itself when path is empty
Class<?> self = PropertyPathUtil.getClass(MyBean.class, "");
```

---

## Getting Property Values

Each node is resolved to the field of that name first, and to a getter (`getXxx()`, or `isXxx()` returning primitive `boolean`; `isXxx()` returning `Boolean` is not treated as a getter) when no such field exists.
When resolved to a getter, its return value is used.

```java
MyBean bean = new MyBean();

// Simple field
Object value = PropertyPathUtil.getValue(bean, "fieldName");

// Nested path
Object city = PropertyPathUtil.getValue(bean, "address.city");

// List element (only arrays and List supported, not Set or Map)
Object item = PropertyPathUtil.getValue(bean, "items[0]");
```

Getting values via Set or Map keys throws `ElementOfCollectionCannotBeObtainedException`.

### Getting the leafBean

Gets the direct parent object (leafBean) of the propertyPath.

```java
// For "address.city", returns the address object
Object leafBean = PropertyPathUtil.getLeafBean(rootBean, "address.city");

// When there is no parent node (only "fieldName"), returns the rootBean as-is
Object same = PropertyPathUtil.getLeafBean(rootBean, "fieldName");
```

---

## Collection Constants

String constants are defined for propertyPath nodes that refer to collection elements themselves.

| Constant | Value | Target |
|---|---|---|
| `EL_LIST` | `<list element>` | List element |
| `EL_SET` | `<iterable element>` | Set element |
| `EL_MAP_KEY` | `<map key>` | Map key |
| `EL_MAP_VAL` | `<map value>` | Map value |
