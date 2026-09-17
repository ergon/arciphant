package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.sourceset.ConfigureAllComponentsExtension.Companion.CONFIGURE_ALL_COMPONENTS_EXTENSION_NAME
import ch.ergon.arciphant.core.sourceset.ConfigureSingleComponentExtension.Companion.CONFIGURE_COMPONENT_EXTENSION_NAME
import ch.ergon.arciphant.util.arciphantPreconditionError
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

internal fun Project.createConfigureComponentExtensions(sourceSetsByComponentName: Map<String, ComponentSourceSets>) {
    extensions.add(CONFIGURE_ALL_COMPONENTS_EXTENSION_NAME, ConfigureAllComponentsExtension(sourceSetsByComponentName))
    extensions.add(CONFIGURE_COMPONENT_EXTENSION_NAME, ConfigureSingleComponentExtension(sourceSetsByComponentName))
}

/**
 * Registered as the `configureAllComponents` extension in every module project of the source set layout.
 * Configures the source sets of every component of the module — typically used in a precompiled script
 * plugin, where the extension accessor makes it available without imports:
 *
 * ```
 * configureAllComponents {
 *     productionSourceSet { sourceSet, componentName ->
 *         sourceSet.java.setSrcDirs(listOf("$componentName/java"))
 *     }
 *     testSourceSet { sourceSet, componentName ->
 *         sourceSet.java.setSrcDirs(listOf("$componentName/test/java"))
 *     }
 * }
 * ```
 *
 * Components without a test or test fixtures source set are skipped by the corresponding function.
 */
open class ConfigureAllComponentsExtension internal constructor(
    private val sourceSetsByComponentName: Map<String, ComponentSourceSets>,
) {

    fun productionSourceSet(configure: (sourceSet: SourceSet, componentName: String) -> Unit) =
        configureEach(configure) { it.production }

    fun testSourceSet(configure: (sourceSet: SourceSet, componentName: String) -> Unit) =
        configureEach(configure) { it.test }

    fun testFixturesSourceSet(configure: (sourceSet: SourceSet, componentName: String) -> Unit) =
        configureEach(configure) { it.testFixtures }

    private fun configureEach(
        configure: (SourceSet, String) -> Unit,
        sourceSet: (ComponentSourceSets) -> SourceSet?,
    ) {
        sourceSetsByComponentName.forEach { (componentName, sourceSets) ->
            sourceSet(sourceSets)?.let { configure(it, componentName) }
        }
    }

    companion object {
        internal const val CONFIGURE_ALL_COMPONENTS_EXTENSION_NAME = "configureAllComponents"
    }
}

/**
 * Registered as the `configureComponent` extension in every module project of the source set layout.
 * Its invoke operator configures the source sets of a single component, selected by name:
 *
 * ```
 * configureComponent("domain") {
 *     productionSourceSet { sourceSet ->
 *         sourceSet.java.setSrcDirs(listOf("domain/java"))
 *     }
 * }
 * ```
 */
open class ConfigureSingleComponentExtension internal constructor(
    private val sourceSetsByComponentName: Map<String, ComponentSourceSets>,
) {

    operator fun invoke(componentName: String, configure: SingleComponentConfigurer.() -> Unit) {
        val sourceSets = sourceSetsByComponentName[componentName] ?: arciphantPreconditionError(
            "unknown component '$componentName' Known components: ${sourceSetsByComponentName.keys.sorted().joinToString { "'$it'" }}."
        )
        SingleComponentConfigurer(componentName, sourceSets).configure()
    }

    companion object {
        internal const val CONFIGURE_COMPONENT_EXTENSION_NAME = "configureComponent"
    }
}

class SingleComponentConfigurer internal constructor(
    val componentName: String,
    private val sourceSets: ComponentSourceSets,
) {

    fun productionSourceSet(configure: (sourceSet: SourceSet) -> Unit) = configure(sourceSets.production)

    fun testSourceSet(configure: (sourceSet: SourceSet) -> Unit) =
        configure(sourceSets.test ?: missingSourceSetError("test"))

    fun testFixturesSourceSet(configure: (sourceSet: SourceSet) -> Unit) =
        configure(sourceSets.testFixtures ?: missingSourceSetError("test fixtures"))

    private fun missingSourceSetError(sourceSetType: String): Nothing =
        arciphantPreconditionError("component '$componentName' has no $sourceSetType source set.")
}

