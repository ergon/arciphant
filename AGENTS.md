# AGENTS.md

## Repository Overview

Arciphant is a Gradle settings plugin with the plugin ID `ch.ergon.arciphant`. It lets users define module templates and instantiate modules in `settings.gradle.kts`; Arciphant then generates and configures the Gradle multi-project structure.

The repository root is a composite build with no tasks of its own:

- `arciphant-gradle-plugin/`: the Kotlin Gradle plugin.
- `arciphant-project-demo/`: an example application for the usage of Arciphant with component layout 'project'
- `arciphant-source-set-demo/`: an example application for the usage of Arciphant with component layout 'source set'
- `docs/`: Zensical user documentation published to GitHub Pages.

## Build and Verification

Always target a sub-build with `-p` when invoking Gradle from the repository root:

```bash
./gradlew -p arciphant-gradle-plugin build
./gradlew -p arciphant-gradle-plugin test
./gradlew -p arciphant-project-demo build
./gradlew -p arciphant-source-set-demo build
./gradlew -p arciphant-gradle-plugin :publishAllPublicationsToLocalRepository
```

The publication task writes to `arciphant-gradle-plugin/build/publishedPlugin`.

Use the narrowest relevant verification while developing, then run the affected sub-build's `build` task before handing off substantial changes. Changes that can affect generated project structure or DSL behavior should also be verified against the demo project.

The JDK toolchain is 21, but the JVM target must remain 17 so the plugin works on Java 17 and newer. `jvmTargetValidationMode = ERROR`; do not raise the JVM target.

`ArciphantPluginTest` is a Gradle TestKit functional test. Build the plugin project before running this test from IntelliJ.

## Plugin Architecture

The entry point is `arciphant-gradle-plugin/src/main/kotlin/ch/ergon/arciphant/ArciphantPlugin.kt`. It implements `Plugin<Settings>` and is applied from `settings.gradle.kts`, not `build.gradle.kts`.

Lifecycle:

1. On apply, register the `arciphant { ... }` DSL extension.
2. In `settingsEvaluated`, load the DSL into `ModuleRepository` and `CoreSettingsRepository`, convert it to `GradleProjectConfig` instances, create project directories, and call `include()` for each project.
3. In `allprojects.beforeEvaluate`, use `GradleProjectConfigApplicator` to apply convention plugins and dependencies.
4. In `projectsLoaded`, register the root tasks `validatePackageStructure` and `projectDependencies`.

Packages below `ch.ergon.arciphant`:

- `dsl`: public DSL surface, including `ArciphantDsl` and builders.
- `core` and `core.model`: internal metamodel and Gradle wiring.
- `sca`: package-structure validation and `validatePackageStructure`.
- `analyze`: the `projectDependencies` task.
- `util`: verification helpers; configuration errors start with `Arciphant configuration error: `.

Preserve these dependency mappings and defaults:

- `dependsOn` maps to `implementation`.
- `dependsOnApi` maps to `api`.
- Every component of a domain module automatically receives an `api` dependency on the same-named library component.
- A `bundle` without `includes` depends on all functional modules.

## Implementation Conventions

- Keep declarations `internal` by default outside the public DSL.
- Prefer extension functions to utility classes.
- Use named arguments in DSL calls.
- Use JUnit 5 and AssertJ for tests.
- Name tests with backtick strings such as `` `it should ...` ``.
- Group related tests in `@Nested` inner classes.
- For parameterized cases, use the `dynamicTest` helper from `arciphant-gradle-plugin/src/test/kotlin/ch/ergon/arciphant/util/DynamicTestExtension.kt`.
- Preserve existing behavior unless the task explicitly requests a behavioral change; add or update focused tests for changed behavior.

The plugin version is declared in `arciphant-gradle-plugin/build.gradle.kts`.

## Demo Project Constraints

- Treat `arciphant-project-demo/settings.gradle.kts` as the canonical DSL example.
- Convention plugins live in `arciphant-project-demo/build-logic/`. At least one must be declared with `apply false` in the settings `plugins` block so Gradle resolves them; see `docs/pages/using-plugins.md`.
- Component directories normally have no `build.gradle.kts`; Arciphant configures them. Add one only when a component needs extra configuration or dependencies.
- `compileKotlin` depends on `validatePackageStructure`, so demo builds validate package structure automatically.

## Documentation

Documentation sources live in `docs/pages/`.

`docs/site/` is checked-in generated output. Never edit it manually; regenerate it with Zensical.

Docker is required for documentation commands:

```bash
cd docs
docker compose up --build zensical_serve -d
docker compose run --build --rm zensical_build
```

The documentation server is available at `http://localhost:8000`.

## Commits

Write commit messages as short imperative sentences ending with a period, for example: `Fix docs.`
