# ecuacion-lib-dependency-jakartaee

## What is it?

`ecuacion-lib-dependency-jakartaee` provides `dependencyManagement` settings in `pom.xml`.  
It holds module-versions for `jakartaee` and some other modules.  
We have `splib` modules which uses `ecuacion-lib` as a base library and adopt `spring boot` framework.  
Since `spring boot` provides latest module-versions for `jakartaee` and some other modules, we adopt its versions for `ecuacion-splib` and apps based on that.  
This is how `ecuacion-lib-dependency-jakartaee` was introduced.

## System Requirements

- JDK 21 or above.

## introduction

You never want to introduce this directly. Maybe this will be introduced by introducing other modules you want to introduce.
