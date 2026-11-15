# SPI

## Overview

SPI (Service Provider Interface) is a mechanism in Java's module system for declaring resource providers.

The `jp.ecuacion.lib.core.spi` package provides Service Provider Interfaces (SPIs) to enable
`PropertiesFileUtil` to load various `.properties` files **when using the Java 9 module system (JPMS)**.

This is not needed for standard classpath-based applications (without `module-info.java`).

---

## When It Is Needed

When using `PropertiesFileUtil` in a Java module system environment with `module-info.java`,
SPI registration is required. `PropertiesFileUtil` internally uses `ResourceBundle` to load
`.properties` files, and in a module system environment, SPI registration is needed for this loading.

When each module of an application has its own `.properties` files,
you need to register an SPI implementation corresponding to that module.

---

## Types of SPI Interfaces

An interface is provided for each properties file type.

| SPI Interface | Corresponding File |
| ------------------ | ------------ |
| `MessagesProvider` / `MessagesBaseProvider` / `MessagesCoreProvider` | messages.properties |
| `MessagesWithItemNamesProvider` / `MessagesWithItemNamesBaseProvider` / `MessagesWithItemNamesCoreProvider` | messages_with_item_names.properties |
| `ConstantsProvider` / `ConstantsBaseProvider` / `ConstantsCoreProvider` | constants.properties |
| `ItemNamesProvider` / `ItemNamesBaseProvider` / `ItemNamesCoreProvider` | item_names.properties |
| `EnumNamesProvider` / `EnumNamesBaseProvider` / `EnumNamesCoreProvider` | enum_names.properties |
| `ApplicationProvider` / `ApplicationBaseProvider` / `ApplicationCoreProvider` / `ApplicationProfileProvider` / `ApplicationCoreProfileProvider` | application.properties |
| `ValidationMessagesPatternDescriptionsProvider` | ValidationMessagesPatternDescriptions.properties |

---

## Configuration in module-info.java

When an application module has `.properties` files, the following two things are required.

### 1. Create an SPI Implementation Class

Create an implementation class that extends `AbstractPropertiesFileProviderImpl`.

```java
// Example: For a module that has messages.properties
public class AppMessagesProvider extends AbstractPropertiesFileProviderImpl
    implements MessagesProvider {
}
```

### 2. Register in module-info.java

The way to write it differs depending on where the `.properties` file is located.

**Pattern A: Placed under a package (individually opened with `opens`)**

```java
module your.app.module {
    requires jp.ecuacion.lib.core;

    // Register the SPI provider
    provides jp.ecuacion.lib.core.spi.MessagesProvider
        with your.app.AppMessagesProvider;

    // Open the package so the library can access the resource files
    opens your.app.resources;
}
```

**Pattern B: Placed directly at the classpath root (fully opened with `open module`)**

When placing `.properties` files at the classpath root without belonging to a package,
you cannot specify a package with `opens`, so the entire module is made `open`.

```java
open module your.app.module {
    requires jp.ecuacion.lib.core;

    // Register the SPI provider
    provides jp.ecuacion.lib.core.spi.MessagesProvider
        with your.app.AppMessagesProvider;
}
```

Note that without `opens` or `open module`, the library cannot load resource files.

For `opens` settings required by features that access application classes via reflection,
see [Using in Named Module Environments](named-module.md).

---

## Impact on Regular Application Development

For applications that do not use `module-info.java`, there is no need to be aware of SPIs.
`PropertiesFileUtil` automatically searches for `.properties` files on the classpath.
