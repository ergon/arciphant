package ch.ergon.arciphant.core.project

import ch.ergon.arciphant.core.ComponentDependencyNotation
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
 * The component dependency notation of the project layout: a project dependency on the target
 * component's Gradle project.
 */
internal fun Project.projectComponentDependency() = ComponentDependencyNotation { module, component ->
    dependencies.project(module.gradleProjectPath(component.reference).value)
}

/**
 * Completes inter-module component dependencies in the project layout. Whenever a component dependency
 * notation (created by [ch.ergon.arciphant.core.ComponentDependencyFactory] and identified through the
 * [ComponentDependencyRegistry]) is added to the `api` or `implementation` configuration, the matching
 * test fixtures dependency is added as well — once the `java-test-fixtures` plugin is applied.
 * Hand-written project dependencies are not completed.
 *
 * Note: the completion only triggers for eagerly added dependencies. Lazily added dependencies
 * (`addLater`) are realized during dependency resolution, when other configurations can no longer
 * be modified.
 */
internal class ComponentDependencyMirror(
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
