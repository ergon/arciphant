package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.sourceset.CustomizeAllComponentsExtension.Companion.CUSTOMIZE_ALL_COMPONENTS_EXTENSION_NAME
import ch.ergon.arciphant.core.sourceset.CustomizeSingleComponentExtension.Companion.CUSTOMIZE_COMPONENT_EXTENSION_NAME
import ch.ergon.arciphant.util.arciphantPreconditionError
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

internal fun Project.createCustomizeComponentExtensions(sourceSetsByComponentName: Map<String, ComponentSourceSets>) {
    extensions.add(CUSTOMIZE_ALL_COMPONENTS_EXTENSION_NAME, CustomizeAllComponentsExtension(sourceSetsByComponentName))
    extensions.add(CUSTOMIZE_COMPONENT_EXTENSION_NAME, CustomizeSingleComponentExtension(sourceSetsByComponentName))
}

/**
 * Registered as the `customizeAllComponents` extension in every module project of the source set layout.
 * Configures the source sets of every component of the module — typically used in a precompiled script
 * plugin, where the extension accessor makes it available without imports:
 *
 * ```
 * customizeAllComponents {
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
open class CustomizeAllComponentsExtension internal constructor(
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
        internal const val CUSTOMIZE_ALL_COMPONENTS_EXTENSION_NAME = "customizeAllComponents"
    }
}

/**
 * Registered as the `customizeComponent` extension in every module project of the source set layout.
 * Its invoke operator configures the source sets of a single component, selected by name:
 *
 * ```
 * customizeComponent("domain") {
 *     productionSourceSet { sourceSet ->
 *         sourceSet.java.setSrcDirs(listOf("domain/java"))
 *     }
 * }
 * ```
 */
open class CustomizeSingleComponentExtension internal constructor(
    private val sourceSetsByComponentName: Map<String, ComponentSourceSets>,
) {

    operator fun invoke(componentName: String, configure: SingleComponentCustomizer.() -> Unit) {
        val sourceSets = sourceSetsByComponentName[componentName] ?: arciphantPreconditionError(
            "unknown component '$componentName' Known components: ${sourceSetsByComponentName.keys.sorted().joinToString { "'$it'" }}."
        )
        SingleComponentCustomizer(componentName, sourceSets).configure()
    }

    companion object {
        internal const val CUSTOMIZE_COMPONENT_EXTENSION_NAME = "customizeComponent"
    }
}

class SingleComponentCustomizer internal constructor(
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

