# ecuacion-lib

## What is it?

`ecuacion-lib` provides utilities for jakarta Bean Validation and JPA.
It also provides some utilities used by other ecuacion modules.  
This is the base library which is used by other ecuacion libraries, utils and other apps.  

`ecuacion-lib` and other ecuacion libraries, utils and apps are fullyl dependent on `jakarta EE`. `jakarta EE 10` is used to have compatibility with `spring boot 3` in `ecuacion-splib`.

## System Requirements

- JDK 21 or above.

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
   (This is the example of `ecuacion-lib-core` module. Check `Introduction` part of `README` in the module you want to add to your project.)

    ```xml
    <dependency>
        <groupId>jp.ecuacion.lib</groupId>
        <artifactId>ecuacion-lib-core</artifactId>
	    <!-- Put the latest release version -->
	    <version>x.x.x</version>
    </dependency>
    ```
    
3. Add dependent external modules to your `pom.xml`.  
   (Check `Dependent External Libraries > Manual Load Needed Libraries` part of `README` in the module you want to add to your project.)
