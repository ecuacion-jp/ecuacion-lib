# ecuacion-lib-dependency-jakartaee

## What is it?

`ecuacion-lib-dependency-jakartaee` provides `dependencyManagement` settings in `pom.xml`.  
It stores versions for `jakarta EE` and some other modules.  

We have `ecuacion-splib` modules which adopts `spring boot 3` and uses `ecuacion-lib` as a base library.  
Since `spring boot` provides the latest versions for `jakarta EE` and some other modules with its `dependencyManagement` settings, 
we adopt its versions to `ecuacion-splib` and apps based on that.  

On the other hand, even when `ecuacion-splib` is not used we still need to set versions for `jakarta EE` and some other modules.  

This is why `ecuacion-lib-dependency-jakartaee` was introduced.  
It stores `jakartaee` versions, `ecuacion-lib` modules refer to it but `ecuacion-splib` modules don't.

## System Requirements

- JDK 21 or above.

## Dependent External Libraries

(none)

## Documentation

(none)

## Installation

You never want to install this directly. Maybe this will be installed by installing other modules you want to install.
