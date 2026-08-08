# ecuacion-lib-dependencies

## What is it?

`ecuacion-lib-dependencies` provides `dependencyManagement` settings in `pom.xml`.  
It stores versions for `jakarta EE` and some other modules.  

We have `ecuacion-splib` modules which adopts `spring boot 4` and uses `ecuacion-lib` as a base library.  
Since `spring boot` provides the latest versions for `jakarta EE` and some other modules with its `dependencyManagement` settings,
we adopt its versions to `ecuacion-splib` and apps based on that.  

On the other hand, even when `ecuacion-splib` is not used we still need to set versions for `jakarta EE` and some other modules.  

This is why `ecuacion-lib-dependencies` was introduced.  
It stores `jakartaee` versions, `ecuacion-lib` modules refer to it but `ecuacion-splib` modules don't.

Note that these settings are intentionally kept in this separate module rather than being merged
into `ecuacion-lib-parent`.  
`ecuacion-splib-parent` uses `ecuacion-lib-parent` as its **parent POM** (not a BOM import), so
anything in `ecuacion-lib-parent` is inherited by every general application that in turn uses
`ecuacion-splib-parent` as its own parent POM. `ecuacion-lib-dependencies` therefore also holds
everything that should stay opt-in rather than being forced onto those general applications:

- The external library version management described above (avoids propagating versions that could
  conflict with Spring Boot's `dependencyManagement`).
- Build tooling for this library's own quality assurance: checkstyle, spotbugs, automatic
  license-header insertion, and NullAway/Error Prone static analysis.
- An actual (not just managed) dependency on `jspecify`, the null-safety annotations NullAway relies on.

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
