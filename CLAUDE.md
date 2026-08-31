# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Arciphant is a Gradle **settings** plugin (plugin ID `ch.ergon.arciphant`) that lets projects declare their module structure with a DSL directly in `settings.gradle.kts`: module *templates* define the technical structure (components + dependencies), modules are instantiated from templates, and Arciphant generates the Gradle multi-project structure from that.

Four parts (the root is a composite build via `includeBuild`):

- `arciphant-gradle-plugin/` — the plugin itself (Kotlin)
- `arciphant-project-demo/` — example app "Online Learning Platform" (Kotlin + Spring Boot) with component layout **project**, reference for DSL usage
- `arciphant-source-set-demo/` — the same example app with component layout **source set** (components are source sets inside one Gradle project per module)
- `docs/` — user documentation (Zensical, published to GitHub Pages)

## Build & Test Commands

The root build has **no tasks of its own** — always target a sub-build with `-p`:

```bash
./gradlew -p arciphant-gradle-plugin build          # build + test the plugin
./gradlew -p arciphant-gradle-plugin test           # plugin tests only
./gradlew -p arciphant-project-demo build           # build + test the demo project with component layout 'project'
./gradlew -p arciphant-source-set-demo build        # build + test the demo project with component layout 'source set'
./gradlew -p arciphant-gradle-plugin :publishAllPublicationsToLocalRepository  # publish to build/publishedPlugin
```

- JDK: toolchain 21, but the JVM target is intentionally **17** so the plugin works on Java 17+ (`jvmTargetValidationMode = ERROR` — do not raise the target).
- Docs (Docker required): `cd docs && docker compose up --build zensical_serve -d` serves on http://localhost:8000; build with `docker compose run --build --rm zensical_build`.

## Architecture (plugin)

Entry point: `arciphant-gradle-plugin/src/main/kotlin/ch/ergon/arciphant/ArciphantPlugin.kt` — a `Plugin<Settings>`, applied in `settings.gradle.kts` (not `build.gradle.kts`).

Flow: the `arciphant { … }` DSL extension is registered on apply → in `settingsEvaluated`, the DSL is loaded into the internal model (`ModuleRepository`, `GlobalSettingsRepository`), converted to `GradleProjectConfig`s, project folders are created, and each project is `include()`d → depending on the component layout, `ProjectLayoutConfigApplicator` (in `allprojects.beforeEvaluate`) or `SourceSetLayoutConfigApplicator` (in `gradle.lifecycle.beforeProject`) applies convention plugins and dependencies to each project → in `projectsLoaded`, the root tasks `validatePackageStructure` and `projectDependencies` are registered.

Packages under `ch.ergon.arciphant`:

- `dsl` — public DSL surface (`ArciphantDsl`, builders); everything else is `internal`
- `core` / `core.model` — internal metamodel (`Module`, `Component`, `Dependency`, sealed interfaces + data classes) and Gradle wiring; layout-specific config applicators live in `core.project` and `core.sourceset`
- `sca` — package structure validation (`validatePackageStructure` task)
- `analyze` — `projectDependencies` task
- `util` — verification helpers (errors are prefixed "Arciphant configuration error: …")

Mapping rules: `dependsOn` → `implementation`, `dependsOnApi` → `api`; every component of a domain module automatically gets an `api` dependency on the same-named library component; a `bundle` without `includes` depends on all functional modules.

## Demo Projects

Both demos implement the same "Online Learning Platform" module structure, once per component layout:

- `arciphant-project-demo/settings.gradle.kts` is the canonical DSL example (project layout); `arciphant-source-set-demo/settings.gradle.kts` is the canonical example for the source set layout (`sourceSetComponentLayout()`).
- Convention plugins live in each demo's `build-logic/` (included build). At least one of them must appear with `apply false` in the settings `plugins` block, otherwise Gradle does not resolve them (known workaround, see `docs/pages/using-plugins.md`).
- Project layout: component folders normally have **no** `build.gradle.kts` — Arciphant configures them. Only components with extra dependencies have one (e.g. `course/domain/build.gradle.kts`). Component-specific convention plugins (e.g. `spring-web-component`) are registered in the DSL.
- Source set layout: components live under `src/<component>/` of the module project; component names are **lowerCamelCase** (e.g. `webApi`) because they become source set names. Plugins cannot be applied per component — every module project has a `build.gradle.kts` applying a convention plugin: `common-module` for functional modules (`filestore-module` for those with the filestore component), `bundle-module` for the bundle; both build on the base `module` plugin (Kotlin plugin + `validatePackageStructure` hook). Module-wide dependencies are declared via Arciphant's shared configurations; the Spring plugins are applied by the root `build.gradle.kts` via `subprojects`. Do not apply the convention plugins from a root `subprojects`/`allprojects` block: at that point neither the version catalog extension exists on the subproject, nor can eagerly declared component configurations be assumed.
- `compileKotlin` depends on `validatePackageStructure`, so every build validates the package structure (in both demos).

## Conventions & Gotchas

- Tests: JUnit 5 + AssertJ; test names are backtick strings (`` `it should …` ``), grouped with `@Nested` inner classes; parameterized tests use the `dynamicTest` helper in `src/test/kotlin/ch/ergon/arciphant/util/DynamicTestExtension.kt`.
- `ArciphantSettingsPluginTest` is a functional test using Gradle TestKit (`GradleRunner`); build the project with Gradle before running it from IntelliJ.
- Code style: `internal` by default outside the DSL, extension functions instead of utility classes, named arguments in DSL calls.
- Docs source is `docs/pages/`; `docs/site/` is **generated build output** (gitignored) — never edit it by hand, regenerate via the Zensical build.
- Plugin version lives in `arciphant-gradle-plugin/build.gradle.kts` (`version = "0.1.9"`).
- Commit messages: short imperative sentence ending with a period (e.g. "Fix docs.").
