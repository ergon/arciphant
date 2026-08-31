package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.GradlePluginIds.KOTLIN_JVM
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

/**
 * Associates the Kotlin compilations of two source sets, which is the Kotlin equivalent of the classpath association
 * done for the java source sets. Reflection is required because the Kotlin Gradle Plugin is not on the compile
 * classpath of this plugin: depending on it would force a Kotlin plugin version on every consumer.
 *
 * The association is fully reactive: the source sets may be created while the Kotlin plugin is still being applied
 * (it applies 'java' itself, which triggers the arciphant config), in which case the compilations for them are only
 * created later during the Kotlin plugin application.
 */
internal fun Project.associateKotlinCompilations(sourceSet: SourceSet, dependency: SourceSet) {
    pluginManager.withPlugin(KOTLIN_JVM) {
        try {
            val compilations = kotlinCompilations()
            compilations.whenPresent(sourceSet.name) { sourceCompilation ->
                compilations.whenPresent(dependency.name) { dependencyCompilation ->
                    sourceCompilation.invokeAssociateWith(dependencyCompilation)
                }
            }
        } catch (exception: Exception) {
            throw IllegalStateException(
                "Arciphant could not associate Kotlin compilations '${sourceSet.name}' and '${dependency.name}' in project '$path'.",
                exception,
            )
        }
    }
}

private fun NamedDomainObjectCollection<Any>.whenPresent(name: String, action: (Any) -> Unit) {
    val existing = findByName(name)
    if (existing != null) {
        action(existing)
    } else {
        whenObjectAdded {
            if (namer.determineName(this) == name) action(this)
        }
    }
}

private fun Any.invokeAssociateWith(dependencyCompilation: Any) {
    javaClass.methods
        .first { method ->
            method.name == "associateWith" &&
                    method.parameterCount == 1 &&
                    method.parameterTypes.single().isInstance(dependencyCompilation)
        }
        .invoke(this, dependencyCompilation)
}

@Suppress("UNCHECKED_CAST")
private fun Project.kotlinCompilations(): NamedDomainObjectCollection<Any> {
    val kotlinExtension = extensions.getByName("kotlin")
    val target = kotlinExtension.invokeGetter("getTarget")
    return target.invokeGetter("getCompilations") as NamedDomainObjectCollection<Any>
}

private fun Any.invokeGetter(name: String): Any =
    javaClass.methods.first { it.name == name && it.parameterCount == 0 }.invoke(this)
