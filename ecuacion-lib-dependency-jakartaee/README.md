# ecuacion-lib-dependency-jakartaee

## What is it?

`ecuacion-lib-dependency-jakartaee` provides `dependencyManagement` settings in `pom.xml`.  
It stores versions for `jakarta EE` and some other modules.  

We have `ecuacion-splib` modules which adopts `spring boot` and uses `ecuacion-lib` as a base library.  
Since `spring boot` provides latest versions for `jakarta EE` and some other modules with its `dependencyManagement` settings, 
we adopt its versions to `ecuacion-splib` and apps based on that.  

On the other hand, even when `ecuacion-splib` is not used we need to set versions for `jakarta EE` and some other modules.  

This is how `ecuacion-lib-dependency-jakartaee` was introduced.  
It stores `jakartaee` versions, `ecuacion-lib` refers to it but `ecuacion-splib` doesn't because it doesn't need those versions.

## System Requirements

- JDK 21 or above.

## Dependent External Libraries

(none)

## Documentation

(none)

## introduction

You never want to introduce this directly. Maybe this will be introduced by introducing other modules you want to introduce.
