package ch.ergon.arciphant.util

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

/**
 * For some reason [Gradle.beforeProject] needs an [Action] as input rather than a lambda.
 * This method provides a convenience method to be used with a lambda.
 */
fun Gradle.beforeProjectAction(action: (project: Project) -> Unit) {
    gradle.beforeProject(object : Action<Project> {
        override fun execute(project: Project) { action(project) }
    })
}
