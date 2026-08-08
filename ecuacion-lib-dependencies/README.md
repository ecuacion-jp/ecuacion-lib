# ecuacion-lib-dependencies

## What is it?

`ecuacion-lib-dependencies` is the parent POM used by `ecuacion-lib`'s own modules
(`ecuacion-lib-core`, `ecuacion-lib-validation`, `ecuacion-lib-validation-business-messages`), not
by application projects.

Note that these settings are intentionally kept in this separate module rather than being merged
into `ecuacion-lib-parent`.  
`ecuacion-splib-parent` uses `ecuacion-lib-parent` as its **parent POM** (not a BOM import), so
anything in `ecuacion-lib-parent` is inherited by every general application that in turn uses
`ecuacion-splib-parent` as its own parent POM. `ecuacion-lib-dependencies` therefore holds
everything that should stay opt-in rather than being forced onto those general applications:

- Build tooling for this library's own quality assurance: checkstyle, spotbugs, automatic
  license-header insertion, and NullAway/Error Prone static analysis.
- Source jar / javadoc jar generation, uploading them to the ecuacion docs server (wagon), test
  coverage measurement (jacoco), and enforcing a minimum Maven version (enforcer).
- An actual (not just managed) dependency on `jspecify` (the null-safety annotations NullAway
  relies on) and a bundled test stack: `junit-jupiter`, `assertj-core`, `allure-jupiter` (test
  report generation).

`ecuacion-lib-dependencies` deliberately does **not** hold version pins for external libraries that
overlap with Spring Boot's own `dependencyManagement` (`hibernate-validator`, `jakarta.el`,
`jakarta.validation-api`, `jakarta.servlet-api`, `jakarta.mail-api`, `slf4j-api`,
`jackson-databind`, `commons-lang3`). Those live in a separate module, `ecuacion-lib-bom`: since
`ecuacion-splib-dependencies` uses `ecuacion-lib-dependencies` as its own **parent POM** (for the
strict tooling above), anything pinned here would also propagate to `ecuacion-splib-dependencies`
and silently override the versions a Spring Boot project would otherwise get from
`spring-boot-dependencies`. See `ecuacion-lib-bom`'s own `pom.xml` for details. The test-stack
libraries above don't have this problem: they're declared as actual dependencies (not just
managed), so they always win Maven's dependency mediation regardless of where the version is
pinned, making the split unnecessary for them.

`ecuacion-lib-core`, `ecuacion-lib-validation`, and `ecuacion-lib-validation-business-messages` use
`ecuacion-lib-dependencies` as their parent POM, so they get all of the above. `ecuacion-splib` has
the equivalent split: `ecuacion-splib-dependencies` plays the same role for `ecuacion-splib-core`
and the other ecuacion-splib modules, while `ecuacion-splib-parent` stays free of it so it's safe
for general applications to use as their own parent POM.

## Dependent External Libraries

(none)

## Documentation

(none)

## Installation

You never want to install this directly. Maybe this will be installed by installing other modules you want to install.
