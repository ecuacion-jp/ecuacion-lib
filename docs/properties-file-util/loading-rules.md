# Loading Behavior and Advanced Settings

## Fallback

Some file types fall back to another file type when a key is not found.
File types with a fallback configured work even without creating the corresponding file,
by falling back to the fallback target.
While splitting files into separate ones can be easier to manage, it is also common practice to write
everything in `messages.properties`, so fallback is configured to support that usage as well.

| File Name | Fallback Target |
| --- | --- |
| `application[_xxx].properties` | None |
| `constants[_xxx].properties` | None |
| `messages[_xxx].properties` | None |
| `messages_with_item_names[_xxx].properties` | `messages` |
| `item_names[_xxx].properties` | `messages` |
| `enum_names[_xxx].properties` | `messages` |
| `ValidationMessages[_xxx].properties` | None |
| `ValidationMessagesWithItemNames[_xxx].properties` | `ValidationMessages` |
| `ValidationMessagesPatternDescriptions[_xxx].properties` | None |

For example, even if you only prepare `messages.properties` without creating `item_names.properties`,
`getItemName(...)` will search for the key in `messages.properties` and return the result.

---

## Loading Files from Multiple Modules at Once

ecuacion assumes that applications are split into multiple modules (`base`, `core`, `web`, `batch`, etc.).
For example, if the application name is `sample-app`:

```text
sample-app-base  → messages_base.properties
sample-app-core  → messages_core.properties
sample-app-web   → messages.properties
```

`PropertiesFileUtil.getMessage(...)` searches all of the above at once.
If the same key is defined in multiple files, an **exception is thrown** (duplicate detection).

The application-specific suffixes such as `base`, `core`, and `web` above
are automatically detected and registered from the `spring.messages.basename` configuration
when using ecuacion-splib.
When not using ecuacion-splib or when building a custom module/framework,
call them manually.

```java
PropertiesFileUtil.addResourceBundlePostfix("mymodule");
// → messages_mymodule.properties, application_mymodule.properties, etc. are also added to the search targets
```

---

## Behavior When a Key Does Not Exist

| File Type | Behavior When Key Is Not Found |
| --- | --- |
| `application.properties` | Throws an exception |
| Others | Returns the key string as-is (no exception) |

Only `application.properties` throws an exception because you want to reliably detect missing configuration
values at application startup.
On the other hand, for `messages.properties` and similar files, it is more convenient during development
for undefined keys to be displayed on screen, so no exception is thrown.

---

## JVM System Properties Take Precedence Over Every File

For every file type, a JVM system property whose name equals the key being looked up takes
precedence over the value in the properties files above — so if a system property happens to
share a name with a message/config key, it silently overrides the file value. Only whoever can
set JVM system properties (effectively the server administrator, via `-D` at startup) can trigger
this, but it's worth being aware of if a key's resolved value is unexpectedly different from what
its file defines.

---

## Overriding Default Values with `.default` Suffix

Keys provided by ecuacion modules have a `.default` suffix.
To override them in the application, define a key with the same name without `.default` in the application's file.

```properties
# File within an ecuacion module
some.key.default=ecuacion default value

# Application's file (override)
some.key=Application-specific value
```

`getApplication("some.key")` returns the application's value with priority.

For example, message keys provided by `ecuacion-lib-validation-business-messages` have a `.default`
suffix (e.g., `jakarta.validation.constraints.NotNull.message.default`). ecuacion-lib falls back to the
`.default` key when the regular key (e.g., `jakarta.validation.constraints.NotNull.message`) is not
found in the application's `ValidationMessages.properties`, allowing you to replace default messages
without preventing application-specific customization.

---

## Replacing the Source of application.properties

By registering a resolver with `setApplicationResolver(...)`, you can replace the source of `application.properties` keys with the resolver.
It is an extension point for passing already-resolved values to ecuacion-lib when a framework has its own configuration mechanism
(external files, environment variables, profiles and so on, combined with its own precedence rules).
`ecuacion-splib` automatically registers a resolver that returns values through Spring's `Environment`.

```java
// Returns the value for the key, or null when the key does not exist
PropertiesFileUtil.setApplicationResolver(key -> environment.getProperty(key));
```

When a resolver is registered, `application.properties` lookups (`getApplication*` / `hasApplication`) consult **only the resolver**.

- The `application.properties` on the classpath and JVM system properties are not consulted.
- A key for which the resolver returns `null` is treated as nonexistent.

Pass `null` to clear a registered resolver. After clearing, values are obtained from the `application.properties` on the classpath (with JVM system properties taking precedence) as before.

`messages.properties`, `ValidationMessages.properties`, etc. are not affected.

### `${...}` Placeholders and Secrets

ecuacion-lib itself does not resolve `${...}` placeholders in `application.properties` values (it returns the string as is).
With `ecuacion-splib`, values whose `${...}` are resolved by Spring's `Environment` are returned through the resolver.

Since `application.properties` is typically committed to source control, avoid writing secrets such as passwords into it directly.
Instead, write `some.key=${SOME_ENV_VAR}` and let Spring or the like resolve it from an environment variable.
When using ecuacion-lib alone, you can also supply values as JVM system properties (`-D` at startup) (see [JVM System Properties Take Precedence Over Every File](#jvm-system-properties-take-precedence-over-every-file)).

(For `${...}` EL expression evaluation in `ValidationMessages` files, see [ValidationMessages](validation-messages.md).)

---

## Clearing the Cache

`PropertiesFileUtil` caches the contents of `.properties` files in memory after the first read.
If a file is updated on disk while the application is running (e.g. from an admin screen), call
`clearCache()` to force the next `get...` / `has...` call to re-read files from disk.

```java
PropertiesFileUtil.clearCache();
```
