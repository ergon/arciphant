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

Use the narrowest relevant verification while developing, then run the affected sub-build's `build` task before handing off substantial changes. Changes that can affect generated project structure or DSL behavior should also be verified against both demo projects.

The JDK toolchain is 21, but the JVM target must remain 17 so the plugin works on Java 17 and newer. `jvmTargetValidationMode = ERROR`; do not raise the JVM target.

`ArciphantSettingsPluginTest` is a Gradle TestKit functional test. Build the plugin project before running this test from IntelliJ.

## Plugin Architecture

The entry point is `arciphant-gradle-plugin/src/main/kotlin/ch/ergon/arciphant/ArciphantPlugin.kt`. It implements `Plugin<Settings>` and is applied from `settings.gradle.kts`, not `build.gradle.kts`.

Lifecycle:

1. On apply, register the `arciphant { ... }` DSL extension.
2. In `settingsEvaluated`, load the DSL into `ModuleRepository` and `GlobalSettingsRepository`, convert it to `GradleProjectConfig` instances, create project directories, and call `include()` for each project.
3. Depending on the component layout, apply convention plugins and dependencies via `ProjectLayoutConfigApplicator` (in `allprojects.beforeEvaluate`) or `SourceSetLayoutConfigApplicator` (in `gradle.lifecycle.beforeProject`).
4. In `projectsLoaded`, register the root tasks `validatePackageStructure` and `projectDependencies`.

Packages below `ch.ergon.arciphant`:

- `dsl`: public DSL surface, including `ArciphantDsl` and builders.
- `core` and `core.model`: internal metamodel and Gradle wiring; layout-specific config applicators live in `core.project` and `core.sourceset`.
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

Both demo projects implement the same "Online Learning Platform" module structure, one per component layout.

- Treat `arciphant-project-demo/settings.gradle.kts` as the canonical DSL example for the project layout, and `arciphant-source-set-demo/settings.gradle.kts` as the canonical example for the source set layout (`sourceSetComponentLayout()`).
- Convention plugins live in each demo's `build-logic/` included build. At least one must be declared with `apply false` in the settings `plugins` block so Gradle resolves them; see `docs/pages/using-plugins.md`.
- In `arciphant-project-demo`, component directories normally have no `build.gradle.kts`; Arciphant configures them. Add one only when a component needs extra configuration or dependencies.
- In `arciphant-source-set-demo`, components are source sets under `src/<component>/` of the module project, and component names use lowerCamelCase because they become source set names. Plugins cannot be applied per component; the root `build.gradle.kts` applies the `module` convention plugin and the Spring plugins to all projects. Module projects normally have no `build.gradle.kts`; add one only for module-specific configuration.
- Keep the two demos structurally in sync: a change to the module structure of one demo should normally be mirrored in the other.
- `compileKotlin` depends on `validatePackageStructure`, so demo builds validate package structure automatically.

## Documentation

Documentation sources live in `docs/pages/`.

`docs/site/` is generated output and gitignored. Never edit it manually; regenerate it with Zensical.

Docker is required for documentation commands:

```bash
cd docs
docker compose up --build zensical_serve -d
docker compose run --build --rm zensical_build
```

The documentation server is available at `http://localhost:8000`.

## Commits

Write commit messages as short imperative sentences ending with a period, for example: `Fix docs.`
