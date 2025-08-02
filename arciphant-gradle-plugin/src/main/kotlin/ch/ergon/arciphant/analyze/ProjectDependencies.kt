package ch.ergon.arciphant.analyze

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.logging.Logger

internal fun Project.registerProjectDependenciesTask() {
    tasks.register("projectDependencies") {
        doLast {
            printProjectDependencies(logger)
        }
    }
}

private fun Project.printProjectDependencies(logger: Logger) {
    allprojects.forEach { currentProject ->
        logger.quiet("Project '${currentProject.path}'")
        currentProject.configurations.forEach { configuration ->
            configuration.dependencies.withType(ProjectDependency::class.java).forEach { dependency ->
                if (dependency.path != currentProject.path) {
                    logger.quiet(" |- [${configuration.name}] ${dependency.path}")
                }
            }
        }
    }
}
