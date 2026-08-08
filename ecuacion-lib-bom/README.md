# ecuacion-lib-bom

## What is it?

`ecuacion-lib-bom` provides `dependencyManagement` settings (version pins only, no plugins, no
actual dependencies) for external libraries that `ecuacion-lib-core` / `ecuacion-lib-validation`
(and non-ecuacion-lib modules such as `ecuacion-util-excel-table` /
`ecuacion-util-excel-report-to-pdf`) depend on directly without specifying a version:
`hibernate-validator`, `jakarta.el`, `jakarta.validation-api`, `jakarta.servlet-api`,
`jakarta.mail-api`, `slf4j-api`, `jackson-databind`, `commons-lang3`.

These pins are kept in a module separate from `ecuacion-lib-dependencies` on purpose. Several of
ecuacion-splib's own modules (`ecuacion-splib-core` and others) use `ecuacion-splib-dependencies` as
their parent POM, whose own `<parent>` is `ecuacion-lib-dependencies` (for its strict
checkstyle/spotbugs/NullAway tooling). If these external-library version pins lived in
`ecuacion-lib-dependencies` instead, that `<parent>` chain would also pull them in, silently
overriding the versions a consuming Spring Boot project would otherwise get from
`spring-boot-dependencies` (e.g. `hibernate-validator`).

Because of this, `ecuacion-lib-bom` is imported directly (via `dependencyManagement` BOM import)
only by the modules that actually need a version for one of the libraries above:
`ecuacion-lib-core`, `ecuacion-lib-validation`, `ecuacion-util-excel-table`,
`ecuacion-util-excel-report-to-pdf`. `ecuacion-lib-dependencies` itself never imports it, so nothing
that uses `ecuacion-lib-dependencies` as a `<parent>` inherits these pins.

## Dependent External Libraries

(none; `dependencyManagement` only, no actual dependencies)

## Documentation

(none)

## Installation

You never want to install this directly. It is used as a `dependencyManagement` BOM import by
modules that need version pins for the external libraries listed above.
