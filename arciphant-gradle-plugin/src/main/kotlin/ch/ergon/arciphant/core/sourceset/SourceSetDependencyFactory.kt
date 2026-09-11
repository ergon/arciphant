package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.ComponentDependencyRegistry
import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.DependencyType
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

class SourceSetDependencyFactory internal constructor(
    private val project: Project,
    private val settings: SourceSetComponentSettings,
    private val registry: ComponentDependencyRegistry,
) {

    internal fun addIntraModuleDependency(type: DependencyType, sourceSet: SourceSet, dependency: SourceSet) {
        doAddIntraModuleDependency(type, sourceSet, dependency)

        val sourceTestFixtures = sourceSet.testFixturesSourceSet()
        val dependencyTestFixtures = dependency.testFixturesSourceSet()
        if (sourceTestFixtures != null && dependencyTestFixtures != null) {
            doAddIntraModuleDependency(type, sourceTestFixtures, dependencyTestFixtures)
        }
    }

    private fun doAddIntraModuleDependency(type: DependencyType, sourceSet: SourceSet, dependency: SourceSet) {
        val dependencyConfiguration = project.dependencyConfiguration(sourceSet, type)
        dependencyConfiguration.extendsFrom(project.apiConfiguration(dependency))
        project.dependencies.add(dependencyConfiguration.name, dependency.output)
        project.extendRuntimeOnly(sourceSet, dependency)
    }

    /**
     * Adds the dependency on the target component's `…ApiElements` configuration. The runtime dependency
     * and the test fixtures mirroring are added by the [SourceSetLayoutComponentDependencyCompleter]
     * registered on the component configurations, which recognizes the dependency through the
     * [ComponentDependencyRegistry].
     */
    internal fun addInterModuleDependency(type: DependencyType, sourceSet: SourceSet, projectPath: String, component: Component) {
        val dependency = project.projectDependency(projectPath, component.reference.name.apiElementsConfigurationName())
        registry.register(dependency, component)
        project.dependencies.add(project.dependencyConfiguration(sourceSet, type).name, dependency)
    }

    private fun SourceSet.testFixturesSourceSet(): SourceSet? =
        project.sourceSets().findByName(settings.testFixturesSourceSetName(name))
}
