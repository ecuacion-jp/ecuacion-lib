# ecuacion-lib

## What is it?

`ecuacion-lib` provides utilities for Jakarta Validation and JPA.
It also provides some utilities used by other ecuacion modules and apps.

`ecuacion-lib` and other ecuacion libraries, utilities and apps depend fully on `Jakarta EE`. `Jakarta EE 11` compatible, which is also compatible with `Spring Boot 3` and `4`.

## System Requirements

- JDK 21 or above.

## Documentation

(See `Documentation` part of the `README` in each module)

## Installation

1. Add the required `ecuacion` modules to your `pom.xml`.
   (The following is an example for the `ecuacion-lib-core` module. Check the `Installation` section of the `README` in the module you want to add to your project.)

    ```xml
    <dependency>
        <groupId>jp.ecuacion.lib</groupId>
        <artifactId>ecuacion-lib-core</artifactId>
        <!-- Put the latest release version -->
        <version>x.x.x</version>
    </dependency>
    ```

2. Add the required external modules to your `pom.xml`.
   (Check the `Dependent External Libraries > Manual Load Needed Libraries` section of the `README` in the module you want to add to your project.)
