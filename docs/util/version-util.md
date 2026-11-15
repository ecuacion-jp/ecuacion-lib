# VersionUtil

`VersionUtil` (`jp.ecuacion.lib.core.util.VersionUtil`) is a utility class for reading the
version of ecuacion products (e.g. `ecuacion-lib`, `ecuacion-splib`, `ecuacion-utils`), or of
an individual app.

---

## Method List

| Method | Description |
| --- | --- |
| `getVersion(productName)` | Returns the version string, or `null` if the version file doesn't exist on the classpath |

```java
// Version of an ecuacion product
String libVersion = VersionUtil.getVersion("ecuacion-lib"); // e.g. "16.0.0"

// Version of the app itself (pass "")
String appVersion = VersionUtil.getVersion("");
```

---

## How It Works

Each ecuacion product bundles its own `version_<productName>.properties` file (e.g.
`version_ecuacion-lib.properties`), and an individual app may likewise bundle its own
`version.properties` file (pass `""` as `productName` to read it). In both cases, the file's
`version` key holds `@project.version@`, which Maven resource filtering replaces with the
actual build version at build time.

This class is implemented in `ecuacion-lib-core` because it is always present as a dependency
of every other ecuacion product, letting a single shared implementation read every product's
version file off the classpath. Results are cached per `productName` after the first lookup.

To expose your own app's version the same way, bundle a `version.properties` file with a
`version` key and configure Maven resource filtering (with a non-`${...}`-conflicting delimiter,
e.g. `@...@`) so `@project.version@` is replaced at build time.
