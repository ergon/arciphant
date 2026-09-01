package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.FunctionalModule
import ch.ergon.arciphant.core.model.Module
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

/**
 * Completes inter-module component dependencies. Whenever a project dependency on a component's
 * `…ApiElements` configuration is added to a component's `api` or `implementation` configuration —
 * declared in a `dependencies` block via [ArciphantModuleDsl.component], through the
 * [ArciphantModuleDsl] methods, or written by Arciphant itself — the mirror automatically adds the
 * matching `…RuntimeElements` dependency to the source set's `runtimeOnly` configuration and mirrors
 * the dependency between the test fixtures source sets if both the source and the target component
 * have one.
 *
 * Note: the completion only triggers for eagerly added dependencies. Lazily added dependencies
 * (`addLater`) are realized during dependency resolution, when other configurations can no longer
 * be modified.
 */
internal class InterModuleDependencyMirror(
    private val project: Project,
    private val settings: SourceSetComponentSettings,
    modules: List<Module>,
) {

    private val targetsByApiElements: Map<TargetCoordinates, Component> = modules
        .filterIsInstance<FunctionalModule>()
        .flatMap { module ->
            module.components.map { component ->
                TargetCoordinates(
                    projectPath = module.gradleProjectPath().value,
                    targetConfiguration = component.reference.name.apiElementsConfigurationName(),
                ) to component
            }
        }
        .toMap()

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
        val targetConfiguration = dependency.targetConfiguration ?: return
        val target = targetsByApiElements[TargetCoordinates(dependency.path, targetConfiguration)] ?: return
        val targetComponentName = target.reference.name

        project.dependencies.add(
            sourceSets.production.runtimeOnlyConfigurationName,
            project.projectDependency(dependency.path, targetComponentName.runtimeElementsConfigurationName()),
        )

        val sourceTestFixtures = sourceSets.testFixtures ?: return
        if (!(target.withTestFixturesSourceSet ?: settings.withTestFixturesSourceSet)) return
        val targetTestFixturesName = settings.testFixturesSourceSetName(targetComponentName)
        project.dependencies.add(
            project.dependencyConfiguration(sourceTestFixtures, type).name,
            project.projectDependency(dependency.path, targetTestFixturesName.apiElementsConfigurationName()),
        )
        project.dependencies.add(
            sourceTestFixtures.runtimeOnlyConfigurationName,
            project.projectDependency(dependency.path, targetTestFixturesName.runtimeElementsConfigurationName()),
        )
    }

    private data class TargetCoordinates(val projectPath: String, val targetConfiguration: String)
}
