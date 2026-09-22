package ch.ergon.arciphant.sca

import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.GlobalSettings
import ch.ergon.arciphant.core.GradleFunctionalModuleProjectConfig
import ch.ergon.arciphant.core.GradleProjectConfig
import ch.ergon.arciphant.core.sourceSetNames
import java.io.Serializable

/**
 * The source sets a component is mapped to in the source set layout — input for the component-aware
 * package structure validation of functional module projects.
 */
internal data class ComponentSourceSets(
    val componentName: String,
    val sourceSetNames: List<String>,
) : Serializable

/**
 * Returns the [ComponentSourceSets] of every functional module project, keyed by the Gradle project path.
 * Empty for the project component layout, where every component is validated as its own Gradle project.
 */
internal fun List<GradleProjectConfig>.componentSourceSetsByProjectPath(
    settings: GlobalSettings,
): Map<String, List<ComponentSourceSets>> {
    if (settings.componentLayout != SOURCE_SET) {
        return emptyMap()
    }
    return filterIsInstance<GradleFunctionalModuleProjectConfig>().associate { config ->
        config.path.value to config.module.components.map { component ->
            ComponentSourceSets(
                componentName = component.reference.name,
                sourceSetNames = component.sourceSetNames(settings.sourceSetComponentSettings),
            )
        }
    }
}
