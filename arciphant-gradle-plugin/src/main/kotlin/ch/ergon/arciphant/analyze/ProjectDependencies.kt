package ch.ergon.arciphant.analyze

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

internal fun Project.registerProjectDependenciesTask() {
    tasks.register("projectDependencies") {
        doLast {
            printProjectDependencies()
        }
    }
}

private fun Project.printProjectDependencies() {
    allprojects.forEach { currentProject ->
        println("Project '${currentProject.path}'")
        currentProject.configurations.forEach { configuration ->
            configuration.dependencies.withType(ProjectDependency::class.java).forEach { dependency ->
                if (dependency.path != currentProject.path) {
                    println(" |- [${configuration.name}] ${dependency.path}")
                }
            }
        }
    }
}
