package ch.ergon.arciphant.core

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

/**
 * Associates the Kotlin compilations of two source sets, which is the Kotlin equivalent of the classpath association
 * done for the java source sets. Reflection is required because the Kotlin Gradle Plugin is not on the compile
 * classpath of this plugin: depending on it would force a Kotlin plugin version on every consumer.
 */
internal fun Project.associateKotlinCompilations(sourceSet: SourceSet, dependency: SourceSet) {
    if (!pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")) return

    try {
        val compilations = kotlinCompilations()
        val sourceCompilation = compilations.getByName(sourceSet.name)
        val dependencyCompilation = compilations.getByName(dependency.name)
        sourceCompilation.javaClass.methods
            .first { method ->
                method.name == "associateWith" &&
                    method.parameterCount == 1 &&
                    method.parameterTypes.single().isInstance(dependencyCompilation)
            }
            .invoke(sourceCompilation, dependencyCompilation)
    } catch (exception: Exception) {
        throw IllegalStateException(
            "Arciphant could not associate Kotlin compilations '${sourceSet.name}' and '${dependency.name}' in project '$path'.",
            exception,
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun Project.kotlinCompilations(): NamedDomainObjectCollection<Any> {
    val kotlinExtension = extensions.getByName("kotlin")
    val target = kotlinExtension.invokeGetter("getTarget")
    return target.invokeGetter("getCompilations") as NamedDomainObjectCollection<Any>
}

private fun Any.invokeGetter(name: String): Any =
    javaClass.methods.first { it.name == name && it.parameterCount == 0 }.invoke(this)
