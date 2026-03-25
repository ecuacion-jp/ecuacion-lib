# ecuacion-lib - Claude Code Guidelines

## Project Overview

An OSS Java library providing utilities including Jakarta Validation support. Multi-module Maven project.

- **Java**: 21
- **Build tool**: Maven
- **Main modules**: `ecuacion-lib-core`, `ecuacion-lib-validation`, `ecuacion-lib-jpa`

## Java Coding Rules

### Style Standards
- Follows **Google Java Style Guide** (enforced by Checkstyle in CI)
- Indentation: **2 spaces** (no tabs)
- Max line length: **100 characters** (excluding package/import statements) — **applies to comments too**
- Encoding: **UTF-8**

### Imports
- Wildcard imports (`.*`) are **prohibited**
- Imports are sorted automatically (follow IDE auto-organize imports)

### Javadoc
- **All public classes, methods, and fields must have Javadoc**
- `@return` and `@param` tags must not be omitted
- When editing existing files, review and update Javadoc for any modified methods

### License Header
- All Java files must have the Apache 2.0 license header at the top
- Follow the same format as existing files

## File Creation and Editing Rules

### Creating New Files
- Always refer to existing files in the same package before creating a new one
- When adding to a package that has `package-info.java`, check its contents first

### Validator Pattern (ecuacion-lib-validation)
When adding a new When-validator, always create 3 files as a set:
1. `XxxWhen.java` - Annotation definition
2. `XxxWhenValidator.java` - Validation logic extending `ValidateWhenValidator`
3. `XxxWhenMessageParameterCreator.java` - Extends `ValidateWhenValidatorMessageParameterCreator` (empty body)

Also add entries to the following files:
- `ValidationMessages_lib_validation.properties` / `_ja.properties`
- `ValidationMessagesWithItemNames_lib_validation.properties` / `_ja.properties`
- `messages_lib_validation.properties` / `_ja.properties`
- The switch statement in `ValidateWhenValidatorMessageParameterCreator.java`

## Work Style

- **Commit only when explicitly instructed**
- **Push only when explicitly instructed**
- Always confirm before destructive operations (file deletion, `git reset --hard`, etc.)
- Do not propose changes to code that has not been read first

## Build and Verification

```bash
# Build all modules
mvn compile

# Test a specific module
mvn test -pl ecuacion-lib-validation -am

# Checkstyle verification (run in CI)
mvn checkstyle:check
```

**Always run the following after editing Java files and fix any violations before finishing:**

```bash
mvn checkstyle:check spotbugs:check
```

The most common violations are:
- Checkstyle: Line length over 100 characters (including comments and Javadoc)
- Checkstyle: Missing Javadoc on public members
- Checkstyle: Wildcard imports
- SpotBugs: Using reflection to access private fields (use `protected` scope workarounds where needed)
