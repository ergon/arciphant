package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.ComponentDependencyNotation
import ch.ergon.arciphant.core.ComponentDependencyRegistry
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

/**
 * The component dependency notation of the source set layout: a project dependency on the target
 * component's `…ApiElements` configuration.
 */
internal fun Project.sourceSetLayoutComponentDependency() = ComponentDependencyNotation { module, component ->
    projectDependency(module.gradleProjectPath().value, component.reference.name.apiElementsConfigurationName())
}

/**
 * Completes component dependencies of the source set layout that are declared in a `dependencies` block.
 *
 * A declaration like `"domainApi"(component(module = "exam", component = "api"))` puts a single project
 * dependency on the target component's `…ApiElements` configuration into the `domainApi` configuration.
 * That alone is not enough:
 *
 * - The notation addresses a named target configuration, so Gradle's variant selection does not apply —
 *   every classpath resolves the same `…ApiElements` configuration, which only exposes the target's
 *   classes and api dependencies. The target's runtime dependencies are exposed through its
 *   `…RuntimeElements` configuration, so a second dependency must be added to the source set's
 *   `runtimeOnly` configuration.
 * - If both the source and the target component have a test fixtures source set, the dependency must be
 *   mirrored between them (with the same api/implementation semantics, plus the runtime leg).
 *
 * The notation itself cannot add these legs because it does not know which configuration it is assigned
 * to. This completer therefore registers a hook on the `api` and `implementation` configuration of every
 * component source set — the hooked configuration determines the source set and the dependency type of
 * the legs, which are added through [SourceSetDependencyFactory.completeInterModuleDependency]. Only
 * dependencies known to the [ComponentDependencyRegistry] (i.e. created by the `component` notation,
 * which also carries the resolved target component) are completed; hand-written project dependencies are
 * left untouched, and the dependencies Arciphant derives from its configuration add their legs directly
 * through [SourceSetDependencyFactory.addInterModuleDependency].
 *
 * Note: the completion only triggers for eagerly added dependencies. Lazily added dependencies
 * (`addLater`) are realized during dependency resolution, when other configurations can no longer
 * be modified.
 */
internal class SourceSetLayoutComponentDependencyCompleter(
    private val project: Project,
    private val registry: ComponentDependencyRegistry,
    private val dependencyFactory: SourceSetDependencyFactory,
) {

    fun register(sourceSets: ComponentSourceSets) {
        register(API, sourceSets)
        register(IMPLEMENTATION, sourceSets)
    }

    private fun register(type: DependencyType, sourceSets: ComponentSourceSets) {
        project.dependencyConfiguration(sourceSets.production, type).dependencies.whenObjectAdded {
            if (this is ProjectDependency) complete(type, this, sourceSets)
        }
    }

    private fun complete(type: DependencyType, dependency: ProjectDependency, sourceSets: ComponentSourceSets) {
        val target = registry.findComponent(dependency) ?: return
        dependencyFactory.completeInterModuleDependency(type, sourceSets, dependency.path, target)
    }
}
