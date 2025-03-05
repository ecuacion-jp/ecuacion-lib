# ecuacion-lib-core

## What is it?

`ecuacion-lib-core` provides utilities for jakarta Bean Validation.
It also provides some utilities used by other ecuacion modules.  
This is the base library which is used by other ecuacion apps and libraries.  

## System Requirements

- JDK 21 or above.

## Dependent Ecuacion Libraries

(none)

## Dependent External Libraries

### Automatically Loaded Libraries

- `org.apache.commons:commons-lang3`
- `org.apache.commons:commons-exec` (@Deprecated. It will be removed in the future release)

### Manual Load Needed Libraries

- `jakarta.validation:jakarta.validation-api`
- `jakarta.annotation:jakarta.annotation-api`
- `jakarta.mail:jakarta.mail-api`
- `org.hibernate.validator:hibernate-validator`
- `org.glassfish:jakarta.el`
- `org.slf4j:slf4j-api`
- (any logging libraries. `ch.qos.logback:logback-classic` is reccomended.)

## Documentation

- [javadoc](https://javadoc.ecuacion.jp/apidocs/ecuacion-lib-core/)

## introduction

Check [Introduction](https://github.com/ecuacion-jp/ecuacion-lib) part of `README` page in `ecuacion-lib`.  
Dependency description is as follows.

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-core</artifactId>
    <!-- Put the latest release version -->
    <version>x.x.x</version>
</dependency>
```
