package ch.ergon.arciphant.core.project

import ch.ergon.arciphant.core.ComponentDependencyFactory
import ch.ergon.arciphant.core.ComponentDependencyRegistry
import ch.ergon.arciphant.core.addTestFixturesDependency
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.project

/**
 * The component dependency factory of the project layout: creates a project dependency on the target
 * component's Gradle project.
 */
internal fun Project.projectLayoutComponentDependency() = ComponentDependencyFactory { module, component ->
    dependencies.project(module.gradleProjectPath(component.reference).value)
}

/**
 * Completes component dependencies of the project layout.
 *
 * A declaration like `"api"(component(module = "exam", component = "api"))` puts a single project
 * dependency on the target component's Gradle project into the `api` configuration. Compile and runtime
 * classpath resolve correctly through Gradle's variant selection, so unlike in the source set component
 * layout no runtime leg is needed. What is missing is the test fixtures dependency
 * (`testFixtures(project(":exam:api"))`), which the notation itself cannot add because it does not know
 * which configuration it is assigned to.
 *
 * This completer therefore registers a hook on the `api` and `implementation` configuration — the hooked
 * configuration determines the test fixtures scope (`testFixturesApi` or `testFixturesImplementation`,
 * for implementation dependencies additionally `testImplementation`). The test fixtures dependency is
 * added once the `java-test-fixtures` plugin is applied. Only dependencies known to the
 * [ComponentDependencyRegistry] (i.e. created by the `component` notation) are completed; hand-written
 * project dependencies are left untouched.
 *
 * Note: the completion only triggers for eagerly added dependencies. Lazily added dependencies
 * (`addLater`) are realized during dependency resolution, when other configurations can no longer
 * be modified.
 */
internal class ProjectLayoutComponentDependencyCompleter(
    private val project: Project,
    private val registry: ComponentDependencyRegistry,
) {

    fun register() {
        register(API)
        register(IMPLEMENTATION)
    }

    private fun register(type: DependencyType) {
        // lazy lookup: the 'api' and 'implementation' configurations only exist once a JVM plugin is applied
        project.configurations.matching { it.name == type.configurationName }.all {
            dependencies.whenObjectAdded {
                if (this is ProjectDependency) complete(type, this)
            }
        }
    }

    private fun complete(type: DependencyType, dependency: ProjectDependency) {
        registry.findComponent(dependency) ?: return
        project.addTestFixturesDependency(type, dependency.path)
    }
}
