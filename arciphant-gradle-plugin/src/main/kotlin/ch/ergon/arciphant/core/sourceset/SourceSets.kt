package ch.ergon.arciphant.core.sourceset

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.project

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

internal fun Project.projectDependency(projectPath: String, targetConfiguration: String): ProjectDependency {
    return dependencies.project(projectPath, targetConfiguration)
}

internal fun Project.sourceSets(): SourceSetContainer =
    extensions.findByType(SourceSetContainer::class.java)
        ?: throw IllegalArgumentException("Arciphant error: cannot access source sets in project '$path' because no compatible JVM plugin has been applied.")

internal fun Project.getConfiguration(configurationName: String): Configuration =
    configurations.getByName(configurationName)

internal fun String.apiElementsConfigurationName() = "${this}ApiElements"
internal fun String.runtimeElementsConfigurationName() = "${this}RuntimeElements"
