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

A component in another module can be referenced by project path and component name:

``` kotlin title="build.gradle.kts"
import org.gradle.api.tasks.SourceSetContainer

val sourceSets = extensions.getByType<SourceSetContainer>()

arciphant {
    sourceSetDependencies {
        implementation(
            sourceSet = sourceSets.getByName("application"),
            projectPath = ":contracts",
            componentName = "api",
        )
    }
}
```

The name of the target test-fixtures source set is derived from the global `testFixturesSourceSetName` setting, so it never has to be spelled out. Pass `withTestFixturesSourceSet = false` if the target component has no test fixtures.

## Layout-specific restrictions

Component plugins cannot be configured in `SOURCE_SET` mode because Gradle plugins can only be applied to projects. Configure the JVM and convention plugins on the module projects instead.

`disableQualifiedArchiveBaseName()` is also invalid in `SOURCE_SET` mode: a module produces only one archive, so component-qualified archive names are unnecessary.

## Demo project

The sub-project `arciphant-source-set-demo` in the [Arciphant repository](https://github.com/ergon/arciphant){ target="_blank" } demonstrates a complete application using the source set layout. It mirrors the structure of the [demo project](demo-project.md) that uses the project layout.
