# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Arciphant is a Gradle **settings** plugin (plugin ID `ch.ergon.arciphant`) that lets projects declare their module structure with a DSL directly in `settings.gradle.kts`: module *templates* define the technical structure (components + dependencies), modules are instantiated from templates, and Arciphant generates the Gradle multi-project structure from that.

Three parts (the root is a composite build via `includeBuild`):

- `arciphant-gradle-plugin/` — the plugin itself (Kotlin)
- `arciphant-project-demo/` — example app "Online Learning Platform" (Kotlin + Spring Boot), reference for DSL usage
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

Flow: the `arciphant { … }` DSL extension is registered on apply → in `settingsEvaluated`, the DSL is loaded into the internal model (`ModuleRepository`, `CoreSettingsRepository`), converted to `GradleProjectConfig`s, project folders are created, and each project is `include()`d → in `allprojects.beforeEvaluate`, `GradleProjectConfigApplicator` applies convention plugins and dependencies to each project → in `projectsLoaded`, the root tasks `validatePackageStructure` and `projectDependencies` are registered.

Packages under `ch.ergon.arciphant`:

- `dsl` — public DSL surface (`ArciphantDsl`, builders); everything else is `internal`
- `core` / `core.model` — internal metamodel (`Module`, `Component`, `Dependency`, sealed interfaces + data classes) and Gradle wiring
- `sca` — package structure validation (`validatePackageStructure` task)
- `analyze` — `projectDependencies` task
- `util` — verification helpers (errors are prefixed "Arciphant configuration error: …")

Mapping rules: `dependsOn` → `implementation`, `dependsOnApi` → `api`; every component of a domain module automatically gets an `api` dependency on the same-named library component; a `bundle` without `includes` depends on all functional modules.

## Demo Project

- `arciphant-project-demo/settings.gradle.kts` is the canonical DSL example.
- Convention plugins live in `arciphant-project-demo/build-logic/` (included build). At least one of them must appear with `apply false` in the settings `plugins` block, otherwise Gradle does not resolve them (known workaround, see `docs/pages/using-plugins.md`).
- Component folders normally have **no** `build.gradle.kts` — Arciphant configures them. Only components with extra dependencies have one (e.g. `course/domain/build.gradle.kts`).
- `compileKotlin` depends on `validatePackageStructure`, so every build validates the package structure.

## Conventions & Gotchas

- Tests: JUnit 5 + AssertJ; test names are backtick strings (`` `it should …` ``), grouped with `@Nested` inner classes; parameterized tests use the `dynamicTest` helper in `src/test/kotlin/ch/ergon/arciphant/util/DynamicTestExtension.kt`.
- `ArciphantPluginTest` is a functional test using Gradle TestKit (`GradleRunner`); build the project with Gradle before running it from IntelliJ.
- Code style: `internal` by default outside the DSL, extension functions instead of utility classes, named arguments in DSL calls.
- Docs source is `docs/pages/`; `docs/site/` is **checked-in build output** — never edit it by hand, regenerate via the Zensical build.
- Plugin version lives in `arciphant-gradle-plugin/build.gradle.kts` (`version = "0.1.9"`).
- Commit messages: short imperative sentence ending with a period (e.g. "Fix docs.").
