# ecuacion-lib

## What is it?

`ecuacion-lib` provides utilities for jakarta Bean Validation and JPA.
It also provides some utilities used by other ecuacion modules.  
This is the base library which is used by other ecuacion libraries and apps.  

## System Requirements

- JDK 21 or above.

## Dependent External Libraries

(none)  

\# Basically dependencies to `apache-commons` modules are preferable in ecuacion modules.

## Documentation

(See `Documentation` part of the `README.md` in each module)

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

2. Add dependent `ecuacion` modules to your `pom.xml`.  
   (This is the example of `ecuacion-lib-core` module. Check the page of the module you want to add to your project.)

    ```xml
    <dependency>
        <groupId>jp.ecuacion.lib</groupId>
        <artifactId>ecuacion-lib-core</artifactId>
	    <!-- Put the latest release version -->
	    <version>x.x.x</version>
    </dependency>
    ```
    
3. Add dependent external modules to your `pom.xml`.  
   (Check `Dependent External Libraries > Manual Load Needed Libraries` part of the page of the module you want to add to your project.)
