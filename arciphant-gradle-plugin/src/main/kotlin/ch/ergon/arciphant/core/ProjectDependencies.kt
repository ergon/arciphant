package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.util.arciphantPreconditionError
import org.gradle.api.Project
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

internal fun Project.addMainDependency(type: DependencyType, path: GradleProjectPath) {
    logger.info("Add ${type.configurationName} dependency: $path -> ${path.value}")
    try {
        dependencies { add(type.configurationName, project(path.value)) }
    } catch (e: UnknownConfigurationException) {
        arciphantPreconditionError(
            """
            configuration '${type.configurationName}' does not exist in project '${path.value}'.
            In order to use arciphant, you need to apply either 'java' or 'kotlin.jvm' plugin to all projects in order to get the required configurations ('implementation' and 'api').
            This is typically done either in the allprojects-block or inside a convention plugin registered in the arciphant configuration (see documentation).
            """.trimIndent(),
            e,
        )
    }
}
