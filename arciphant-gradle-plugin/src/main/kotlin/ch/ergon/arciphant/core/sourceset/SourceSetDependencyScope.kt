package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

internal fun Project.sourceSetDependencies(
    settings: SourceSetComponentSettings,
    block: SourceSetDependencyScope.() -> Unit,
) {
    SourceSetDependencyScope(this, settings).block()
}

class SourceSetDependencyScope internal constructor(
    private val project: Project,
    private val settings: SourceSetComponentSettings,
) {

    fun implementation(
        sourceSet: SourceSet,
        projectPath: String,
        componentName: String,
        withTestFixturesSourceSet: Boolean? = null,
    ) {
        addProjectDependency(IMPLEMENTATION, sourceSet, projectPath, componentName, withTestFixturesSourceSet)
    }

    fun api(
        sourceSet: SourceSet,
        projectPath: String,
        componentName: String,
        withTestFixturesSourceSet: Boolean? = null,
    ) {
        addProjectDependency(API, sourceSet, projectPath, componentName, withTestFixturesSourceSet)
    }

    internal fun addLocalDependency(type: DependencyType, sourceSet: SourceSet, dependency: SourceSet) {
        addDependency(type, sourceSet, dependency)

        val sourceTestFixtures = sourceSet.testFixturesSourceSet()
        val dependencyTestFixtures = dependency.testFixturesSourceSet()
        if (sourceTestFixtures != null && dependencyTestFixtures != null) {
            addDependency(type, sourceTestFixtures, dependencyTestFixtures)
        }
    }

    internal fun addProjectDependency(
        type: DependencyType,
        sourceSet: SourceSet,
        projectPath: String,
        componentName: String,
        withTestFixturesSourceSet: Boolean? = null,
    ) {
        addDependency(type, sourceSet, projectPath, componentName)

        val sourceTestFixtures = sourceSet.testFixturesSourceSet()
        if (sourceTestFixtures != null && (withTestFixturesSourceSet ?: settings.withTestFixturesSourceSet)) {
            addDependency(type, sourceTestFixtures, projectPath, settings.testFixturesSourceSetName(componentName))
        }
    }

    private fun addDependency(type: DependencyType, sourceSet: SourceSet, dependency: SourceSet) {
        val dependencyConfiguration = project.dependencyConfiguration(sourceSet, type)
        dependencyConfiguration.extendsFrom(project.apiConfiguration(dependency))
        project.dependencies.add(dependencyConfiguration.name, dependency.output)
        project.extendRuntimeOnly(sourceSet, dependency)
    }

    private fun addDependency(
        type: DependencyType,
        sourceSet: SourceSet,
        projectPath: String,
        dependencySourceSetName: String,
    ) {
        project.dependencies.add(
            project.dependencyConfiguration(sourceSet, type).name,
            project.projectDependency(projectPath, dependencySourceSetName.apiElementsConfigurationName()),
        )
        project.dependencies.add(
            sourceSet.runtimeOnlyConfigurationName,
            project.projectDependency(projectPath, dependencySourceSetName.runtimeElementsConfigurationName()),
        )
    }

    private fun Project.dependencyConfiguration(sourceSet: SourceSet, type: DependencyType) = when (type) {
        API -> apiConfiguration(sourceSet)
        IMPLEMENTATION -> implementationConfiguration(sourceSet)
    }

    private fun SourceSet.testFixturesSourceSet(): SourceSet? =
        project.sourceSets().findByName(settings.testFixturesSourceSetName(name))
}