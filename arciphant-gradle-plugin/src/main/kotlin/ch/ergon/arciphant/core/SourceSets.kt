package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

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

    fun implementation(sourceSet: SourceSet, dependency: SourceSet) {
        addLocalDependency(IMPLEMENTATION, sourceSet, dependency)
    }

    fun api(sourceSet: SourceSet, dependency: SourceSet) {
        addLocalDependency(API, sourceSet, dependency)
    }

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
            project.createProjectDependency(projectPath, dependencySourceSetName.apiElementsConfigurationName()),
        )
        project.dependencies.add(
            sourceSet.runtimeOnlyConfigurationName,
            project.createProjectDependency(projectPath, dependencySourceSetName.runtimeElementsConfigurationName()),
        )
    }

    private fun SourceSet.testFixturesSourceSet(): SourceSet? =
        project.sourceSets().findByName(settings.testFixturesSourceSetName(name))
}

private fun Project.dependencyConfiguration(sourceSet: SourceSet, type: DependencyType) = when (type) {
    API -> apiConfiguration(sourceSet)
    IMPLEMENTATION -> implementationConfiguration(sourceSet)
}

internal fun Project.apiConfiguration(sourceSet: SourceSet) = getConfiguration(sourceSet.apiConfigurationName)
internal fun Project.implementationConfiguration(sourceSet: SourceSet) = getConfiguration(sourceSet.implementationConfigurationName)
internal fun Project.runtimeConfiguration(sourceSet: SourceSet) = getConfiguration(sourceSet.runtimeOnlyConfigurationName)

internal fun Project.runtimeConfigurations(sourceSet: SourceSet): List<Configuration> = listOf(
    implementationConfiguration(sourceSet),
    runtimeConfiguration(sourceSet),
)

internal fun Project.extendRuntimeOnly(sourceSet: SourceSet, dependency: SourceSet) {
    runtimeConfiguration(sourceSet).extendsFrom(*runtimeConfigurations(dependency).toTypedArray())
}

internal fun Project.createProjectDependency(projectPath: String, targetConfiguration: String): Dependency =
    dependencies.project(
        mapOf(
            "path" to projectPath,
            "configuration" to targetConfiguration,
        )
    )

internal fun Project.sourceSets(): SourceSetContainer =
    extensions.findByType(SourceSetContainer::class.java)
        ?: throw IllegalArgumentException("Arciphant error: cannot access source sets in project '$path' because no compatible JVM plugin has been applied.")

internal fun Project.getConfiguration(configurationName: String): Configuration =
    configurations.getByName(configurationName)

internal fun String.defaultTestSourceSetName() = "${this}Test"
internal fun String.defaultTestFixturesSourceSetName() = "${this}TestFixtures"

internal fun String.apiElementsConfigurationName() = "${this}ApiElements"
internal fun String.runtimeElementsConfigurationName() = "${this}RuntimeElements"
