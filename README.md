# ecuacion-lib

## What is it?

`ecuacion-lib` provides utilities for jakarta bean-validation and jpa.
It also provides some utilities used for other modules.  
This is the base library which is used by other ecuacion apps and libraries.  

The following public library is introduced by default. (Basically the usage of apache-commons-xxx is preferable.)

- `apache-commons-lang`
- `apache-commons-exec` (@Deprecated. It will be removed in the future release)

## System Requirements

- JDK 21 or above.

## Introduction

1. Put the following tags to your `pom.xml` (put `<repositories>` tag as a child tag of `project`).

    ```xml
    <repositories> 
        <repository>
            <id>ecuacion-repo-http</id>
            <name>ecuacion-repo-http</name>
            <url>http://maven-repo.ecuacion.jp/public</url>
        </repository>
    </repositories>
    ```

1. Add maven dependency to your `pom.xml`.  
   (This is the example of `ecuacion-lib-core` module. Check the page of the module you want to add to your project in detail.)

    ```xml
    <dependency>
        <groupId>jp.ecuacion.lib</groupId>
        <artifactId>ecuacion-lib-core</artifactId>
        <version>14.0.0</version>
    </dependency>
    ```
