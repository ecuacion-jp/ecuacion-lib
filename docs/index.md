# ecuacion-lib reference

This is the reference page for ecuacion library (module name: `ecuacion-lib-xxx`).
It provides comprehensive documentation for the utility classes and features of each library.

## What is ecuacion library?

ecuacion library is a library that provides various common features needed for application development.
It can be used standalone, but is typically used in combination with a base library such as ecuacion-splib.

## Module Structure

ecuacion library consists of the following 6 modules.

| Module Name | Role |
| --- | --- |
| `ecuacion-lib-parent` | Parent POM (version management for ecuacion-lib modules) |
| `ecuacion-lib-bom` | BOM (version management for external libraries that ecuacion-lib depends on) |
| `ecuacion-lib-core` | Core features (Item, PropertiesFileUtil, Violation, etc.) |
| `ecuacion-lib-validation` | Custom Jakarta Validation annotation set |
| `ecuacion-lib-validation-business-messages` | Business-oriented validation messages |
| `ecuacion-lib-dependencies` | Used for building ecuacion modules (not recommended for general application developers) |

For details on `ecuacion-lib-core` and `ecuacion-lib-validation`, see the respective pages in the navigation.
`ecuacion-lib-dependencies` is a module used only for building ecuacion-lib itself and is not used by
general application developers, so the following sections describe the remaining 3 modules that are
fully explained on this page.

---

## ecuacion-lib-parent

This is the parent POM for all ecuacion-lib modules. The `dependencyManagement` section defines versions
for each ecuacion-lib module (`ecuacion-lib-core`, `ecuacion-lib-validation`,
`ecuacion-lib-validation-business-messages`), so importing it as a BOM eliminates the need to specify
versions individually. See "Setup" below for how to import it.

## ecuacion-lib-bom

This is a Maven BOM that centrally manages versions of external libraries that ecuacion-lib-core,
ecuacion-lib-validation, and others depend on directly (`jakarta.validation-api`, `hibernate-validator`,
etc.). Importing it into `dependencyManagement` lets these dependencies be declared without
specifying a version.

These version pins are not included in `ecuacion-lib-parent` so that, in a Spring Boot application whose
parent POM is `ecuacion-splib-parent` (which is Spring Boot based and inherits from `ecuacion-lib-parent`),
modules that overlap between ecuacion-lib and Spring Boot can be left under Spring Boot's own version
management rather than ecuacion-lib's.

See "Setup" below for how to import it.

## ecuacion-lib-validation-business-messages

The default Jakarta Validation messages (such as `"must not be null"` provided by Hibernate Validator)
are technical expressions intended for developers. This module replaces them with natural,
user-friendly expressions (such as `"is required"`) and contains **only ValidationMessages property
files**. It contains no Java code and takes effect simply by adding it as a dependency.

### Usage

Add the following to your `pom.xml` (see the "Setup" patterns below for whether a version needs to be specified):

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation-business-messages</artifactId>
</dependency>
```

### Provided Messages

Covers both Jakarta Validation standard annotations and ecuacion-lib-validation custom annotations,
providing messages in English (default), Japanese and the other languages supported by ecuacion-lib
(see "Supported Languages" in the [README](https://github.com/ecuacion-jp/ecuacion-lib#supported-languages)).

The message keys provided by this module can be overridden on the application side using the
`.default` suffix mechanism shared across ecuacion modules. See "Overriding Default Values with
`.default` Suffix" in [PropertiesFileUtil](properties-file-util/loading-rules.md)
for details.

---

## Setup

`ecuacion-lib-core`/`ecuacion-lib-validation` depend on `hibernate-validator`/`jakarta.el` (needed if you
use validation features) and `org.eclipse.angus:angus-mail` (needed if you use `MailUtil`) with
`provided` scope. Since `provided` does not propagate transitively, these must be added explicitly on
the application side. Which of the following 3 patterns you choose depends on how much you want to
automate version management for these libraries and for `ecuacion-lib-xxx` itself.

### Pattern 1: Do not import a BOM; specify versions directly

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation</artifactId>
    <version>(version)</version>
</dependency>

<!-- If you use validation features -->
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>(version)</version>
</dependency>
<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>jakarta.el</artifactId>
    <version>(version)</version>
</dependency>
```

Since neither `ecuacion-lib-parent` nor `ecuacion-lib-bom` is imported, you need to specify all of the
versions above yourself.

### Pattern 2: Import `ecuacion-lib-parent` as a BOM

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>jp.ecuacion.lib</groupId>
            <artifactId>ecuacion-lib-parent</artifactId>
            <version>(version)</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>jp.ecuacion.lib</groupId>
        <artifactId>ecuacion-lib-validation</artifactId>
    </dependency>

    <!-- If you use validation features. ecuacion-lib-parent only manages the versions
         of ecuacion-lib-xxx modules, so these still need a version specified. -->
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>(version)</version>
    </dependency>
    <dependency>
        <groupId>org.glassfish</groupId>
        <artifactId>jakarta.el</artifactId>
        <version>(version)</version>
    </dependency>
</dependencies>
```

Unlike Pattern 1, `ecuacion-lib-parent` manages the version of `ecuacion-lib-validation` itself,
so its version no longer needs to be specified.

### Pattern 3: Import `ecuacion-lib-bom` as a BOM

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>jp.ecuacion.lib</groupId>
            <artifactId>ecuacion-lib-bom</artifactId>
            <version>(version)</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>jp.ecuacion.lib</groupId>
        <artifactId>ecuacion-lib-validation</artifactId>
    </dependency>

    <!-- If you use validation features -->
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.glassfish</groupId>
        <artifactId>jakarta.el</artifactId>
    </dependency>
</dependencies>
```

`ecuacion-lib-bom`'s parent POM is `ecuacion-lib-parent`, and a BOM import also carries over its
parent's `dependencyManagement`. So importing `ecuacion-lib-bom` alone means none of
`ecuacion-lib-validation`, `hibernate-validator`, or `jakarta.el` need a version specified — there is
no need to separately import `ecuacion-lib-parent` as well.

### If you use business-oriented validation messages

Regardless of which pattern above you use, whether a version needs to be specified for
`ecuacion-lib-validation-business-messages` follows the same rule as for `ecuacion-lib-validation`.
