package ch.ergon.arciphant

import ch.ergon.arciphant.ArciphantPlugin.Companion.logger
import ch.ergon.arciphant.core.ComponentDependencyExtension.Companion.COMPONENT_EXTENSION_NAME
import ch.ergon.arciphant.core.createComponentDependencyExtension
import ch.ergon.arciphant.core.project.projectLayoutComponentDependency
import org.gradle.api.Project

/**
 * Solely used to make arciphant extensions available in precompiled script plugins by applying the plugin
 */
class ArciphantProjectPlugin {

    fun apply(project: Project) {
        if (!project.hasExtension(COMPONENT_EXTENSION_NAME)) {
            if (!project.isKotlinDslAccessorGenerationProject()) {
                logger.warn("Arciphant was applied to project '${project.path}', but the Arciphant settings plugin\n" +
                        "was not applied to this build. Apply 'ch.ergon.arciphant' in settings.gradle(.kts).")
            }
            // the factory is never reached with an empty module list — the extension only exists to make
            // the Kotlin DSL accessor available in precompiled script plugins
            project.extensions.createComponentDependencyExtension(
                modules = emptyList(),
                componentDependencyFactory = project.projectLayoutComponentDependency(),
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
