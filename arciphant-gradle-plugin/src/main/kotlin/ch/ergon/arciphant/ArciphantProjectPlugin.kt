package ch.ergon.arciphant

import ch.ergon.arciphant.ArciphantPlugin.Companion.logger
import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.project.ArciphantComponentDsl
import ch.ergon.arciphant.core.project.ArciphantComponentDsl.Companion.ARCIPHANT_COMPONENT_EXTENSION_NAME
import ch.ergon.arciphant.core.project.createArciphantComponentDsl
import ch.ergon.arciphant.core.sourceset.ArciphantModuleDsl
import ch.ergon.arciphant.core.sourceset.ArciphantModuleDsl.Companion.ARCIPHANT_MODULE_EXTENSION_NAME
import ch.ergon.arciphant.core.sourceset.createArciphantModuleDsl
import org.gradle.api.Project
import kotlin.reflect.KClass

/**
 * Solely used to make arciphant extensions available in precompiled script plugins by applying the plugin
 */
class ArciphantProjectPlugin {

    fun apply(project: Project) {
        if (!project.hasExtension(ARCIPHANT_COMPONENT_EXTENSION_NAME) && !project.hasExtension(ARCIPHANT_MODULE_EXTENSION_NAME)) {
            if (!project.isKotlinDslAccessorGenerationProject()) {
                logger.warn("Arciphant was applied to project '${project.path}', but the Arciphant settings plugin\n" +
                        "was not applied to this build. Apply 'ch.ergon.arciphant' in settings.gradle(.kts).")
            }
            project.extensions.createArciphantComponentDsl(
                project = project,
                modules = emptyList(),
            )
            project.extensions.createArciphantModuleDsl(
                project = project,
                modules = emptyList(),
                componentSettings = SourceSetComponentSettings.DEFAULT_SETTINGS,
            )
        }
    }

    private fun Project.hasExtension(name: String): Boolean {
        return project.extensions.findByName(name) != null
    }

    /**
     * Checks whether the plugin is applied in the context of the synthetic project created by Gradle in order to
     * create the type-safe Kotlin DSL accessors in a precompiled script plugin
     */
    private fun Project.isKotlinDslAccessorGenerationProject(): Boolean =
        this == rootProject && name == "gradle-kotlin-dsl-accessors" && gradle.startParameter.isDryRun
}