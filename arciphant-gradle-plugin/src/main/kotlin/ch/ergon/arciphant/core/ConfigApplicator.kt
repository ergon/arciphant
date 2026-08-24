package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

internal interface ConfigApplicator {
    fun applyConfig(project: Project)
}

internal fun Project.addDependency(
    type: DependencyType,
    path: GradleProjectPath,
    withTestFixturesSourceSet: Boolean = true,
) {
    logger.info("Add ${type.configurationName} dependency: $path -> ${path.value}")
    addMainDependency(type, path)
    if (withTestFixturesSourceSet) addTestFixturesDependency(path)
}

private fun Project.addMainDependency(type: DependencyType, path: GradleProjectPath) {
    try {
        dependencies { add(type.configurationName, project(path.value)) }
    } catch (e: UnknownConfigurationException) {
        throw IllegalArgumentException(
            """
            Arciphant error: configuration '${type.configurationName}' does not exist in project '${path.value}'.
            In order to use arciphant, you need to apply either 'java' or 'kotlin.jvm' plugin to all projects in order to get the required configurations ('implementation' and 'api').
            This is typically done either in the allprojects-block or inside a convention plugin registered in the arciphant configuration (see documentation).
            """.trimIndent(),
            e,
        )
    }
}

private fun Project.addTestFixturesDependency(path: GradleProjectPath) {
    pluginManager.withPlugin("java-test-fixtures") {
        dependencies { add("testFixturesApi", testFixtures(project(path.value))) }
    }
}
