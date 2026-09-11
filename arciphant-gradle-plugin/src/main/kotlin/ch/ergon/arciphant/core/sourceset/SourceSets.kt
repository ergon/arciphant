package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.ComponentDependencyNotation
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.util.arciphantPreconditionError
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.project

internal fun Project.apiConfiguration(sourceSet: SourceSet) = getConfiguration(sourceSet.apiConfigurationName)
internal fun Project.implementationConfiguration(sourceSet: SourceSet) =
    getConfiguration(sourceSet.implementationConfigurationName)

internal fun Project.runtimeConfiguration(sourceSet: SourceSet) =
    getConfiguration(sourceSet.runtimeOnlyConfigurationName)

internal fun Project.dependencyConfiguration(sourceSet: SourceSet, type: DependencyType) = when (type) {
    API -> apiConfiguration(sourceSet)
    IMPLEMENTATION -> implementationConfiguration(sourceSet)
}

internal fun Project.runtimeConfigurations(sourceSet: SourceSet): List<Configuration> = listOf(
    implementationConfiguration(sourceSet),
    runtimeConfiguration(sourceSet),
)

internal fun Project.extendRuntimeOnly(sourceSet: SourceSet, dependency: SourceSet) {
    val runtimeConfiguration = runtimeConfiguration(sourceSet)
    runtimeConfigurations(dependency).forEach { runtimeConfiguration.extendsFrom(it) }
}

internal fun Project.projectDependency(projectPath: String, targetConfiguration: String): ProjectDependency {
    return dependencies.project(projectPath, targetConfiguration)
}

/**
 * The component dependency notation of the source set layout: a project dependency on the target
 * component's `…ApiElements` configuration.
 */
internal fun Project.sourceSetComponentDependency() = ComponentDependencyNotation { module, component ->
    projectDependency(module.gradleProjectPath().value, component.reference.name.apiElementsConfigurationName())
}

internal fun Project.sourceSets(): SourceSetContainer =
    extensions.findByType(SourceSetContainer::class.java) ?: noJvmPluginError()

internal fun Project.noJvmPluginError(): Nothing =
    arciphantPreconditionError("cannot access source sets in project '$path' because no compatible JVM plugin has been applied.")

internal fun Project.getConfiguration(configurationName: String): Configuration =
    configurations.getByName(configurationName)

internal fun String.apiElementsConfigurationName() = "${this}ApiElements"
internal fun String.runtimeElementsConfigurationName() = "${this}RuntimeElements"
