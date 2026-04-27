# ecuacion-lib

[![Java CI](https://github.com/ecuacion-jp/ecuacion-lib/actions/workflows/ci.yml/badge.svg)](https://github.com/ecuacion-jp/ecuacion-lib/actions/workflows/ci.yml)
[![GitHub Release](https://img.shields.io/github/v/release/ecuacion-jp/ecuacion-lib)](https://github.com/ecuacion-jp/ecuacion-lib/releases)
[![Maven Central](https://img.shields.io/maven-central/v/jp.ecuacion.lib/ecuacion-lib-validation.svg)](https://search.maven.org/artifact/jp.ecuacion.lib/ecuacion-lib-validation)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

## What is it?

Jakarta Validation is powerful — but it can't express conditional logic out of the box.
"This field is required only when that field equals `CORPORATE`" typically means writing
a custom class-level validator for every case.

`ecuacion-lib-validation` solves this with annotation-based validators:

```java
@NotEmptyWhen(
    propertyPath = "companyName",
    conditionPropertyPath = "type",
    conditionValue = ConditionValue.STRING,
    conditionValueString = "CORPORATE"
)
public class RegistrationForm {
    private String type;
    private String companyName;  // required only when type == "CORPORATE"
}
```

**What's included in `ecuacion-lib-validation`:**

- Conditional validators (`@NotEmptyWhen`, `@EmptyWhen`, `@TrueWhen`, ...)
- Multi-field validators (`@AllEmptyOrAllNotEmpty`, `@AnyNotEmpty`, ...)
- Numeric comparison validators (`@GreaterThan`, `@LessThan`, ...)
- Additional Jakarta Validation constraints (`@PatternWithDescription`, `@ReturnTrue`, ...)

`ecuacion-lib` and other ecuacion libraries, utilities and apps depend fully on `Jakarta EE`.
`Jakarta EE 11` compatible, which is also compatible with `Spring Boot 3` and `4`.

## System Requirements

- JDK 21 or above.

## Documentation

(See `Documentation` part of the `README` in each module)

## Installation

1. Add the required `ecuacion` modules to your `pom.xml`.
   (The following is an example for the `ecuacion-lib-validation` module. Check the `Installation` section of the `README` in the module you want to add to your project.)

    ```xml
    <dependency>
        <groupId>jp.ecuacion.lib</groupId>
        <artifactId>ecuacion-lib-validation</artifactId>
        <!-- Put the latest release version -->
        <version>x.x.x</version>
    </dependency>
    ```

2. Add the required external modules to your `pom.xml`.
   (Check the `Dependent External Libraries > Manual Load Needed Libraries` section of the `README` in the module you want to add to your project.)
