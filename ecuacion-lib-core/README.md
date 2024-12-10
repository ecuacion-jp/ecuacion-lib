# ecuacion-lib-core

## What is it?

"ecuacion-lib-core" is the base library referred by other ecuacion modules.
It offers utilities and customized validators for jakarta bean-validation and some utilities used for other modules.

The following public library is introduced by default. (Basically the usage of apache-commons-xxx is preferable.)

- apache-commons-lang
- apache-commons-exec

## Documentation

- [javadoc](https://javadoc.ecuacion.jp/ecuacion-lib-core/)

## System Requirements

- JDK 21 or above.

## introduction

1. Put `settings.xml ` in ~/.m2 directory to add our maven-repository to your mvn settings.  
(Of course you can choose other ways to add maven repository urls.)
`settings.xml`

    <?xml version="1.0" encoding="UTF-8"?>

    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">

        <profiles>
            <profile>
                <id>default</id>
                <repositories> 
                    <repository>
                        <snapshots>
                            <enabled>false</enabled>
                        </snapshots>
                        <id>ecuacion-repo-http</id>
                        <name>ecuacion-repo-http</name>
                        <url>http://maven-repo.ecuacion.jp/public</url>
                    </repository>
                </repositories>
                <activation>
                    <activeByDefault>true</activeByDefault>
                </activation>
            </profile>
        </profiles>

        <activeProfiles>
            <activeProfile>default</activeProfile>
        </activeProfiles>

    </settings>

1. add maven dependency to your `pom.xmll`

        <dependency>
            <groupId>jp.ecuacion.lib</groupId>
            <artifactId>ecuacion-lib-core</artifactId>
            <version>x.x.x</version>
        </dependency>