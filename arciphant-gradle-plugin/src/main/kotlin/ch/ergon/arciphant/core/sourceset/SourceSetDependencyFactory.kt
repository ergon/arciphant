package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.DependencyType
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

class SourceSetDependencyFactory internal constructor(
    private val project: Project,
    private val settings: SourceSetComponentSettings,
) {

    internal fun addSourceSetDependency(type: DependencyType, sourceSet: SourceSet, dependency: SourceSet) {
        doAddSourceSetDependency(type, sourceSet, dependency)

        val sourceTestFixtures = sourceSet.testFixturesSourceSet()
        val dependencyTestFixtures = dependency.testFixturesSourceSet()
        if (sourceTestFixtures != null && dependencyTestFixtures != null) {
            doAddSourceSetDependency(type, sourceTestFixtures, dependencyTestFixtures)
        }
    }

    private fun doAddSourceSetDependency(type: DependencyType, sourceSet: SourceSet, dependency: SourceSet) {
        val dependencyConfiguration = project.dependencyConfiguration(sourceSet, type)
        dependencyConfiguration.extendsFrom(project.apiConfiguration(dependency))
        project.dependencies.add(dependencyConfiguration.name, dependency.output)
        project.extendRuntimeOnly(sourceSet, dependency)
    }

    /**
     * Adds a dependency on the given component of another module: the dependency on the target's
     * `…ApiElements` configuration plus the completing legs (see [completeComponentDependency]).
     */
    internal fun addComponentDependency(
        type: DependencyType,
        sourceSets: ComponentSourceSets,
        projectPath: String,
        component: Component,
    ) {
        project.dependencies.add(
            project.dependencyConfiguration(sourceSets.production, type).name,
            project.projectDependency(projectPath, component.reference.name.apiElementsConfigurationName()),
        )
        completeComponentDependency(type, sourceSets, projectPath, component)
    }

    /**
     * Adds the legs completing a component dependency: the matching `…RuntimeElements`
     * dependency in the source set's `runtimeOnly` configuration, and — if both the source and the target
     * component have a test fixtures source set — the mirrored dependency between the test fixtures source
     * sets. Used by [addComponentDependency] for the dependencies derived from the Arciphant
     * configuration, and by the [SourceSetLayoutComponentDependencyCompleter] for dependencies declared
     * with the `component` notation in a `dependencies` block.
     */
    internal fun completeComponentDependency(
        type: DependencyType,
        sourceSets: ComponentSourceSets,
        projectPath: String,
        component: Component,
    ) {
        val targetComponentName = component.reference.name

        project.dependencies.add(
            sourceSets.production.runtimeOnlyConfigurationName,
            project.projectDependency(projectPath, targetComponentName.runtimeElementsConfigurationName()),
        )

        val sourceTestFixtures = sourceSets.testFixtures ?: return
        if (!(component.withTestFixturesSourceSet ?: settings.withTestFixturesSourceSet)) return
        val targetTestFixturesName = settings.testFixturesSourceSetName(targetComponentName)
        project.dependencies.add(
            project.dependencyConfiguration(sourceTestFixtures, type).name,
            project.projectDependency(projectPath, targetTestFixturesName.apiElementsConfigurationName()),
        )
        project.dependencies.add(
            sourceTestFixtures.runtimeOnlyConfigurationName,
            project.projectDependency(projectPath, targetTestFixturesName.runtimeElementsConfigurationName()),
        )
    }

    private fun SourceSet.testFixturesSourceSet(): SourceSet? =
        project.sourceSets().findByName(settings.testFixturesSourceSetName(name))
}
