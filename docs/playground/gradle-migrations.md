# OpenSavvy Playground: Gradle migrations

This file contains information about breaking changes made to this repository that require manual changes in user projects.

For more information about the Playground, see [the README](README.md).

Migrations are listed in historical order, newest first.

## Remove Compose from the common versions

Compose Multiplatform has been removed from `common.versions.toml`.

Projects that do not use Compose Multiplatform have no required changes.

Projects that do use Compose Multiplatform should:
- Use the Gradle plugin `alias(libsCommon.plugins.compose.compiler)`: the Compose Compiler plugin must always match the latest Kotlin version.
- Explicitly depend on the Compose modules, by adding them in the project-specific `libs.versions.toml` file.

This will allow each project to follow its own release cycle.

## Remove Intel-based Apple targets

The following Kotlin Multiplatform targets are not supported anymore about should be removed from downstream projects:

- `macosX64`
- `iosX64`
- `watchosX64`
- `watchosArm32`
- `tvosX64`

For more information, see commits `8fd8a14a` and `7220832c`.
