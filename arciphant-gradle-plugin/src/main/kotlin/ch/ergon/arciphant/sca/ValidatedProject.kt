package ch.ergon.arciphant.sca

import ch.ergon.arciphant.core.GlobalSettings
import ch.ergon.arciphant.core.GradleBundleModuleProjectConfig
import ch.ergon.arciphant.core.GradleComponentProjectConfig
import ch.ergon.arciphant.core.GradleFunctionalModuleProjectConfig
import ch.ergon.arciphant.core.GradleProjectConfig
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.sourceSetNames
import java.io.Serializable

/**
 * What the package structure validation knows about an Arciphant-managed project: which path segments are
 * base path and which are module/component names — configured name mappings apply only to the latter.
 */
internal data class ValidatedProject(
    /** The base path segments of the project path; normalized, but not subject to name mappings. */
    val basePathFragments: List<String>,
    /** The module name; subject to the configured module name mappings. */
    val moduleName: String,
    /** The component name of a component project (project layout); subject to the component name mappings. */
    val componentName: String?,
    /** The component source sets of a functional module in the source set layout, null otherwise. */
    val componentSourceSets: List<ComponentSourceSets>?,
) : Serializable

/** The source sets a component is mapped to in the source set layout. */
internal data class ComponentSourceSets(
    val componentName: String,
    val sourceSetNames: List<String>,
) : Serializable

/** Returns the [ValidatedProject] of every Arciphant-managed project, keyed by the Gradle project path. */
internal fun List<GradleProjectConfig>.validatedProjectsByPath(
    settings: GlobalSettings,
): Map<String, ValidatedProject> {
    val result = mutableMapOf<String, ValidatedProject>()
    forEach { config ->
        when (config) {
            is GradleComponentProjectConfig -> {
                result[config.path.value] = ValidatedProject(
                    basePathFragments = config.module.reference.parentProjectPath,
                    moduleName = config.module.reference.name,
                    componentName = config.component.reference.name,
                    componentSourceSets = null,
                )
                // the intermediate module project created by including the component projects
                result.putIfAbsent(
                    config.module.gradleProjectPath().value,
                    ValidatedProject(
                        basePathFragments = config.module.reference.parentProjectPath,
                        moduleName = config.module.reference.name,
                        componentName = null,
                        componentSourceSets = null,
                    ),
                )
            }

            is GradleFunctionalModuleProjectConfig -> result[config.path.value] = ValidatedProject(
                basePathFragments = config.module.reference.parentProjectPath,
                moduleName = config.module.reference.name,
                componentName = null,
                componentSourceSets = config.module.components.map { component ->
                    ComponentSourceSets(
                        componentName = component.reference.name,
                        sourceSetNames = component.sourceSetNames(settings.sourceSetComponentSettings),
                    )
                },
            )

            is GradleBundleModuleProjectConfig -> result[config.path.value] = ValidatedProject(
                basePathFragments = config.module.reference.parentProjectPath,
                moduleName = config.module.reference.name,
                componentName = null,
                componentSourceSets = null,
            )
        }
    }
    return result
}
