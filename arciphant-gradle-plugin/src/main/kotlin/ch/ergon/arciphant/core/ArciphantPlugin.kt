package ch.ergon.arciphant.core

import ch.ergon.arciphant.dsl.ArciphantDsl
import ch.ergon.arciphant.dsl.DslModuleRepository
import ch.ergon.arciphant.util.beforeProjectAction
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class ArciphantPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        with(settings) {
            val extension = extensions.create("arciphant", ArciphantDsl::class.java)

            gradle.settingsEvaluated {
                val modules = DslModuleRepository(extension).load()
                val projectConfigs = modules.flatMap { it.toProjectConfigs() }

                projectConfigs.map { it.path }.filter { !it.isRoot }.forEach { include(it.value) }

                val handler = GradleProjectConfigApplicator(projectConfigs)
                gradle.beforeProjectAction { handler.applyConfig(it) }
            }
        }

    }
}
