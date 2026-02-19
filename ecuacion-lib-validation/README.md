# ecuacion-lib-validation

## What is it?

`ecuacion-lib-validation` provides customized validatiors for Jakarta Validation.

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

(modules depending on `ecuacion-lib-core`)
- `jakarta.validation:jakarta.validation-api`
- (any `jakarta.validation:jakarta.validation-api` compatible Jakarta Validation libraries. `org.hibernate.validator:hibernate-validator` and `org.glassfish:jakarta.el` are recommended.)
- `jakarta.mail:jakarta.mail-api` (If you want to use the mail related utility: `jp.ecuacion.lib.core.util.MailUtil`)
- `org.slf4j:slf4j-api`
- (any `org.slf4j:slf4j-api` compatible logging libraries. `ch.qos.logback:logback-classic` is recommended.)

## Documentation

- [javadoc](https://javadoc.ecuacion.jp/apidocs/ecuacion-lib-validation/)

## Installation

Check [Installation](https://github.com/ecuacion-jp/ecuacion-lib) part of `README` page in `ecuacion-lib`.  
The description of dependent `ecuacion` modules is as follows.

```xml
<dependency>
    <groupId>jp.ecuacion.lib</groupId>
    <artifactId>ecuacion-lib-validation</artifactId>
    <!-- Put the latest release version -->
    <version>x.x.x</version>
</dependency>
```
