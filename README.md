# ecuacion-lib
*Version: 9.0.0.Beta3 - 2024-09-04*

## What is it?
"ecuacion-lib" is the base library referred by other ecuacion modules.
It offers utilities for jakarta bean-validation and jpa.
It also offers some utilities used for other modules.

The following public library is introduced by default.
- apache-commons-lang (Especially for StringUtils)

## Documentation

## System Requirements
JDK 21 or above.

* In case you use the distribution archive from the download site, copy _dist/hibernate-validator-&lt;version&gt;.jar_ together with all
jar files from _dist/lib/required_ into the classpath of your application. For the purposes of logging, Hibernate Validator uses
the JBoss Logging API, an abstraction layer which supports several logging solutions such (e.g. log4j or the logging framework
provided by the JDK) as implementation. Just add a supported logging library to the classpath (e.g. _log4j-&lt;version&gt;.jar_) and JBoss
Logging will delegate any log requests to that provider.

* Add the following artifact to your Maven/Ivy/Gradle dependency list:

        <dependency>
            *<groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>9.0.0.Beta3</version>
        </dependency>

  You also need an API and implementation of the Unified Expression Language. These dependencies must be explicitly added in an SE environment.
  In a Jakarta EE environment, they are often already provided.