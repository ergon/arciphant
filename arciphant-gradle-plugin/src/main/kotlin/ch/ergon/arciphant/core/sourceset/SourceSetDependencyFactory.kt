package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.model.DependencyType
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

class SourceSetDependencyFactory internal constructor(
    private val project: Project,
    private val settings: SourceSetComponentSettings,
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
     * and the test fixtures mirroring are added by the [InterModuleDependencyMirror] registered on the
     * component configurations.
     */
    internal fun addInterModuleDependency(type: DependencyType, sourceSet: SourceSet, projectPath: String, componentName: String) {
        project.dependencies.add(
            project.dependencyConfiguration(sourceSet, type).name,
            project.projectDependency(projectPath, componentName.apiElementsConfigurationName()),
        )
    }

    private fun SourceSet.testFixturesSourceSet(): SourceSet? =
        project.sourceSets().findByName(settings.testFixturesSourceSetName(name))
}
