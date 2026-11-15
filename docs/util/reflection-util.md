# ReflectionUtil

`ReflectionUtil` (`jp.ecuacion.lib.core.util.ReflectionUtil`) is a utility class for low-level
reflection operations using `java.lang.reflect`.

Navigation of object graphs using propertyPath strings (getting field values, getting leafBean, etc.)
is handled by `PropertyPathUtil`.

---

## Checking Class Existence and Creating Instances

```java
// Check if a class exists
boolean exists = ReflectionUtil.classExists("jp.ecuacion.lib.core.util.StringUtil"); // true
boolean exists2 = ReflectionUtil.classExists("com.example.NonExistent");             // false

// Create an instance using the no-arg constructor
Object instance = ReflectionUtil.newInstance("jp.ecuacion.example.MyClass");
```

**`className` must not come from untrusted (e.g. end-user) input.** `classExists` triggers
loading and static initialization of the named class, and `newInstance` additionally triggers
no-argument construction of it — both let whoever controls `className` run arbitrary code that
happens to be on the classpath.

---

## Getting Fields by Simple Field Name

Searches for fields including superclasses, regardless of access modifiers.
Does not accept dot-separated paths or collection indices (use `PropertyPathUtil.getField` for those).

```java
// Get a field (simple name only)
Field field = ReflectionUtil.getDeclaredField(MyBean.class, "fieldName");

// Also searches superclass fields
Field inherited = ReflectionUtil.getDeclaredField(SubClass.class, "parentField");
```

---

## Getting Field Values

Uses `setAccessible(true)` to access even private fields.

```java
Field field = ReflectionUtil.getDeclaredField(MyBean.class, "fieldName");
Object value = ReflectionUtil.getFieldValue(bean, field);
```

---

## Getting Properties (a Field, or a Getter)

`getBeanProperty` first searches for the field of the given name (including superclasses), and searches for a getter when no such field exists.
Getters follow the JavaBeans naming convention: a non-static, no-argument `getXxx()` with a return value, or `isXxx()` returning primitive `boolean`.
Note that `isXxx()` returning `Boolean` (the wrapper type) is not treated as a getter, following the JavaBeans naming convention; name it `getXxx()` instead.
Getters are also searched including superclasses, regardless of access modifiers.
When neither is found, a `RuntimeException` with `NoSuchFieldException` as its cause is thrown.

The returned `BeanProperty` hides the difference between a field and a getter, so its type, annotations and value can be obtained in the same way.

```java
BeanProperty property = ReflectionUtil.getBeanProperty(MyBean.class, "name");

Class<?> type = property.getType();             // the field type, or the getter's return type
Type genericType = property.getGenericType();   // the type including generics
MyAnnotation ann = property.getAnnotation(MyAnnotation.class); // the annotation on the field or the getter
Object value = property.getValue(bean);         // the field value, or the getter's return value
```

When the getter throws an exception, it is wrapped in a `RuntimeException` whose message tells which getter threw it (its cause is the original exception).

---

## Searching for Annotations in the Class Hierarchy

Searches for annotations by traversing from the class itself up through superclasses.

```java
Optional<MyAnnotation> ann = ReflectionUtil.searchAnnotationPlacedAtClass(
    myInstance.getClass(), MyAnnotation.class);

ann.ifPresent(a -> System.out.println(a.value()));
```

Returns the first annotation found. Returns an empty `Optional` if not found
even after traversing up to `Object.class`.
