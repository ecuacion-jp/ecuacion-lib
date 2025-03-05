# ecuacion-lib-jpa

## What is it?

`ecuacion-lib-jpa` provides JPA-related classes.
It offers customized entity class.

## System Requirements

- JDK 21 or above.

## Dependent Ecuacion Libraries

### Automatically Loaded Libraries

- `ecuacion-lib-core`

### Manual Load Needed Libraries

(none)

## Dependent External Libraries

### Automatically Loaded Libraries

(none)

### Manual Load Needed Libraries

- `jakarta.annotation:jakarta.annotation-api`
- `jakarta.persistence:jakarta.persistence-api`

(modules depending on `ecuacion-lib-core`)
- `jakarta.validation:jakarta.validation-api`
- `jakarta.mail:jakarta.mail-api`
- `org.hibernate.validator:hibernate-validator`
- `org.glassfish:jakarta.el`
- `org.slf4j:slf4j-api`
- (any logging libraries. `ch.qos.logback:logback-classic` is reccomended.)

## Documentation

- [javadoc](https://javadoc.ecuacion.jp/apidocs/ecuacion-lib-jpa/)

## introduction

Check [Introduction](https://github.com/ecuacion-jp/ecuacion-lib) part of `README` page in `ecuacion-lib`.  
dependency description is as follows.

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-jpa</artifactId>
    <!-- Put the latest release version -->
    <version>x.x.x</version>
</dependency>
```
