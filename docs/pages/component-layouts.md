---
icon: lucide/layout-grid
---

# Component layouts

Arciphant supports two ways of mapping configured components to Gradle:

* The project layout creates one Gradle project per component. This is the default and preserves the original Arciphant behavior.
* The source set layout creates one Gradle project per functional module and maps every component to source sets inside that project.

## Source set layout

Enable the source set layout with `sourceSetComponentLayout()` in `settings.gradle.kts`:

``` kotlin title="settings.gradle.kts"
arciphant {
    sourceSetComponentLayout()

    val template = template()
        .createComponent("domain")
        .createComponent("application", dependsOnApi = setOf("domain"))

    module("orders", template = template)
}
```

This creates the Gradle project `:orders` with the following source sets by default:

``` text
src/domain/...
src/domainTestFixtures/...
src/domainTest/...
src/application/...
src/applicationTestFixtures/...
src/applicationTest/...
```

Production component outputs are combined into the module's main output and its single archive. A component's test fixtures depend on its production source set; its tests depend on its test fixtures. Component dependencies are mirrored between the corresponding test-fixtures source sets, while test source sets never depend on other components' test source sets.

The source set layout does not require the `java-test-fixtures` plugin. Arciphant also registers one `Test` task per component test source set, named exactly like that source set, and attaches it to the module's standard `test` task. Test and test-fixtures directories are marked as test sources in IntelliJ IDEA.

## Test source set configuration

Tests and test fixtures are enabled by default. Their names and global availability can be configured with source-set-specific settings:

``` kotlin title="settings.gradle.kts"
arciphant {
    sourceSetComponentLayout()

    withTestSourceSet(true)
    withTestFixturesSourceSet(false)
    testSourceSetName { componentName -> "${componentName}Spec" }
    testFixturesSourceSetName { componentName -> "${componentName}Fixtures" }

    module("orders")
        .createComponent(
            name = "domain",
            withTestSourceSet = false,
            withTestFixturesSourceSet = true,
        )
}
```

The component parameters override the global flags.

Source-set settings and component source-set options are rejected in `PROJECT` mode rather than being silently ignored.

## Shared dependency configurations

External dependencies can be declared once for all component source sets of the same kind. In every Arciphant module project, each component configuration extends the corresponding standard configuration:

* `implementation`, `compileOnly` and `runtimeOnly` reach **all production** source sets.
* `testImplementation`, `testCompileOnly` and `testRuntimeOnly` reach **all test** source sets.
* `testFixturesImplementation`, `testFixturesCompileOnly` and `testFixturesRuntimeOnly` (created by Arciphant) reach **all test fixtures** source sets.
* With the `java-library` plugin applied, `api` reaches all production source sets and `testFixturesApi` (created by Arciphant) all test fixtures source sets.

``` kotlin title="build.gradle.kts"
dependencies {
    implementation("org.springframework:spring-context")
    "testFixturesApi"("org.assertj:assertj-core:3.27.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
```

This also works from a convention plugin, which replaces per-component convention plugins in this layout. Since the `testFixtures*` configurations only exist in module projects, apply such a convention plugin only to module projects — selectively, or via `subprojects` if every subproject is an Arciphant module (bundle modules included). Platforms (BOMs) declared this way apply to every component as well. Dependencies of a single component are still declared on its own configurations (e.g. `"domainImplementation"(...)`).

Since the standard configurations of the `main` and `test` source sets are reused, dependencies cannot be declared for only the (typically unused) `main` source set of a module project.

## Project-level utility DSL

The same source-set creation and dependency functions used internally by Arciphant are available through the `arciphant` extension in `build.gradle.kts` files:

``` kotlin title="build.gradle.kts"
arciphant {
    val domain = createComponent("domain")
    createComponent(
        name = "application",
        sourceSetDependencies = { application ->
            api(application, domain)
        },
    )
}
```

A component in another module can be referenced by the module and component names of the Arciphant configuration:

``` kotlin title="build.gradle.kts"
arciphant {
    component("application").implementation(module = "contracts", component = "api")
}
```

Arciphant resolves the target Gradle project path from the module configuration, so no project path has to be spelled out. If both the source and the target component have a test-fixtures source set, the dependency between the test-fixtures source sets is added automatically.

## Layout-specific restrictions

Component plugins cannot be configured in `SOURCE_SET` mode because Gradle plugins can only be applied to projects. Configure the JVM and convention plugins on the module projects instead.

`disableQualifiedArchiveBaseName()` is also invalid in `SOURCE_SET` mode: a module produces only one archive, so component-qualified archive names are unnecessary.

## Demo project

The sub-project `arciphant-source-set-demo` in the [Arciphant repository](https://github.com/ergon/arciphant){ target="_blank" } demonstrates a complete application using the source set layout. It mirrors the structure of the sibling demo project `arciphant-project-demo` that uses the project layout; see [Demo projects](demo-project.md).
