# ecuacion-lib-core

## What is it?

`ecuacion-lib-core` provides utilities for `Jakarta Validation`.
It also provides some utilities used by other ecuacion modules and apps.  

## Dependent Ecuacion Libraries

(none)

## Dependent External Libraries

### Automatically Loaded Libraries

- `org.apache.commons:commons-lang3`
- `jakarta.validation:jakarta.validation-api`
- `jakarta.mail:jakarta.mail-api`
- `org.slf4j:slf4j-api`

### Manual Load Needed Libraries

- (any `jakarta.validation:jakarta.validation-api` compatible Jakarta Validation implementation. `org.hibernate.validator:hibernate-validator` and `org.glassfish:jakarta.el` are recommended.)
- (any `org.slf4j:slf4j-api` compatible logging implementation. `ch.qos.logback:logback-classic` is recommended.)

## Configuration

### Keeping secrets out of `application.properties`

Settings such as `jp.ecuacion.lib.core.mail.smtp.password` are normally read as
plain text from `application.properties`. Since that file is typically committed to
source control, avoid writing real secrets into it directly.

Instead, write a `${...}` placeholder (e.g. `jp.ecuacion.lib.core.mail.smtp.password=${SMTP_PASSWORD}`)
and register an external resolver via `PropertiesFileUtil.setExternalPlaceholderResolver(...)`
to supply the value from an environment variable or another external source at runtime.
(Framework-specific modules such as `ecuacion-splib` wire this up automatically, resolving
`${...}` through Spring's `Environment`.)

## Documentation

- [javadoc](https://javadoc.io/doc/jp.ecuacion.lib/ecuacion-lib-core/latest/index.html)

## Installation

Check [Installation](https://github.com/ecuacion-jp/ecuacion-lib) part of `README` in `ecuacion-lib`.  
The description of dependent `ecuacion` modules is as follows.

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-core</artifactId>
    <!-- Put the latest release version -->
    <version>x.x.x</version>
</dependency>
```
