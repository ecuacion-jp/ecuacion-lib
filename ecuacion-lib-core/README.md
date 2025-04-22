# ecuacion-lib-core

## What is it?

`ecuacion-lib-core` provides utilities for `Jakarta Validation`.
It also provides some utilities used by other ecuacion modules and apps.  

## System Requirements

- JDK 21 or above.

## Dependent Ecuacion Libraries

(none)

## Dependent External Libraries

### Automatically Loaded Libraries

- `org.apache.commons:commons-lang3`

### Manual Load Needed Libraries

- `jakarta.validation:jakarta.validation-api`
- (any `jakarta.validation:jakarta.validation-api` compatible Bean Validation libraries. `org.hibernate.validator:hibernate-validator` and `org.glassfish:jakarta.el` are recommended.)
- `jakarta.annotation:jakarta.annotation-api`
- `jakarta.mail:jakarta.mail-api`
- `org.slf4j:slf4j-api`
- (any `org.slf4j:slf4j-api` compatible logging libraries. `ch.qos.logback:logback-classic` is recommended.)

## Documentation

- [javadoc](https://javadoc.ecuacion.jp/apidocs/ecuacion-lib-core/)

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
