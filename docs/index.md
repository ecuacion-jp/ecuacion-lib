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
| `ecuacion-lib-core` | Core features (Item, Violation, PropertiesFileUtil, various utilities, etc.) |
| `ecuacion-lib-validation` | Custom Jakarta Validation validators |
| `ecuacion-lib-validation-business-messages` | Business-oriented validation messages |
| `ecuacion-lib-parent` | Parent POM (version management for ecuacion-lib modules) |
| `ecuacion-lib-bom` | BOM (version management for external libraries that ecuacion-lib depends on) |
| `ecuacion-lib-dependencies` | Used for building ecuacion modules (not recommended for general application developers) |

`ecuacion-lib-dependencies` is a module used only for building ecuacion-lib itself and is not used by
general application developers, so it is not described further.

### Dependencies

The 3 modules that applications declare as dependencies depend on each other with `compile` scope as
follows.

```text
ecuacion-lib-validation-business-messages
  └─ ecuacion-lib-validation
       └─ ecuacion-lib-core
```

Since `compile`-scoped dependencies are inherited transitively, you only need to declare the single
top-most module among the features you want to use; the modules below it are pulled in automatically.

- Declare `ecuacion-lib-validation-business-messages` → `ecuacion-lib-validation` and `ecuacion-lib-core` are included as well
- Declare `ecuacion-lib-validation` → `ecuacion-lib-core` is included as well

---

## Module Details

The dependency examples below omit the version. See "Setup" below for whether a version needs to be
specified.

### ecuacion-lib-core

This module provides the basic features of ecuacion-lib.
All features described in this documentation under navigation menus **other than** "Custom Validators"
(Item, Violation, PropertiesFileUtil, various utilities, etc.) are included in this module.

If you declare `ecuacion-lib-validation`, this module is pulled in transitively, so you do not need to
declare it separately. If you do not use the custom validators, declare this module directly.

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-core</artifactId>
</dependency>
```

### ecuacion-lib-validation

This module provides custom validators (`@EmptyWhen`, `@GreaterThan`, `@PatternWithDescription`, etc.)
that complement the standard Jakarta Validation annotations (`@NotNull`, `@Size`, etc.).
The features described under the "Custom Validators" navigation menu cannot be used unless you add this
module as a dependency.

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation</artifactId>
</dependency>
```

### ecuacion-lib-validation-business-messages

The default Jakarta Validation messages (such as `"must not be null"` provided by Hibernate Validator)
are technical expressions intended for developers. This module replaces them with natural,
user-friendly expressions (such as `"is required"`) and contains **only ValidationMessages property
files**. It contains no Java code and takes effect simply by adding it as a dependency.

#### Usage

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation-business-messages</artifactId>
</dependency>
```

This module depends on `ecuacion-lib-validation` (which in turn depends on `ecuacion-lib-core`), so
once you add it, you do not need to add `ecuacion-lib-validation` or `ecuacion-lib-core` as separate
dependencies (see "Dependencies" above).

#### Provided Messages

Covers both Jakarta Validation standard annotations and ecuacion-lib-validation custom annotations,
providing messages in English (default), Japanese and the other languages supported by ecuacion-lib
(see "Supported Languages" in the [README](https://github.com/ecuacion-jp/ecuacion-lib#supported-languages)).

The message keys provided by this module can be overridden on the application side using the
`.default` suffix mechanism shared across ecuacion modules. See "Overriding Default Values with
`.default` Suffix" in [PropertiesFileUtil](properties-file-util/loading-rules.md)
for details.

### ecuacion-lib-parent

This is the parent POM for all ecuacion-lib modules. The `dependencyManagement` section defines versions
for each ecuacion-lib module (`ecuacion-lib-core`, `ecuacion-lib-validation`,
`ecuacion-lib-validation-business-messages`).

However, as described in "Dependencies" above, you only need to declare a single
top-most `ecuacion-lib-xxx` module. Importing `ecuacion-lib-parent` alone as a BOM therefore does not
reduce the number of places where you write a version (it stays at one) and only adds the import
declaration, so importing `ecuacion-lib-parent` directly is not recommended.
The `dependencyManagement` of `ecuacion-lib-parent` is carried over automatically when you import
`ecuacion-lib-bom` as in Pattern 2 of "Setup" below.

### ecuacion-lib-bom

This is a Maven BOM that centrally manages versions of external libraries that ecuacion-lib-core,
ecuacion-lib-validation, and others depend on directly (`jakarta.validation-api`, `hibernate-validator`,
etc.). Importing it into `dependencyManagement` lets these dependencies be declared without
specifying a version.

These version pins are not included in `ecuacion-lib-parent` so that, in a Spring Boot application whose
parent POM is `ecuacion-splib-parent` (which is Spring Boot based and inherits from `ecuacion-lib-parent`),
modules that overlap between ecuacion-lib and Spring Boot can be left under Spring Boot's own version
management rather than ecuacion-lib's.

See "Setup" below for how to import it.

---

## Setup

`ecuacion-lib-core`/`ecuacion-lib-validation` depend on `hibernate-validator`/`jakarta.el` (needed if you
use validation features) and `org.eclipse.angus:angus-mail` (needed if you use `MailUtil`) with
`provided` scope. Since `provided` does not propagate transitively, these must be added explicitly on
the application side. Which of the following 2 patterns you choose depends on how much you want to
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

### Pattern 2: Import `ecuacion-lib-bom` as a BOM

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

!!! warning "When using it in a Spring Boot application"
    `ecuacion-lib-bom` pins the versions of external libraries such as `hibernate-validator` that
    Spring Boot also manages. Importing it in a Spring Boot application therefore puts these
    libraries under both Spring Boot's and ecuacion-lib's version management, and a version different
    from the one Spring Boot expects may end up being used (this is the same reason these version pins
    are kept out of `ecuacion-lib-parent`, as explained in the "ecuacion-lib-bom" section above).

    In a Spring Boot application, do not import `ecuacion-lib-bom`; instead, do one of the following.

    - If you use ecuacion-splib: use `ecuacion-splib-parent` as your parent POM
    - Otherwise: as in Pattern 1, specify a version only for `ecuacion-lib-xxx`, and leave the versions
      of `hibernate-validator` and the like to Spring Boot's management

### If you use business-oriented validation messages

Regardless of which pattern above you use, whether a version needs to be specified for
`ecuacion-lib-validation-business-messages` follows the same rule as for `ecuacion-lib-validation`.
