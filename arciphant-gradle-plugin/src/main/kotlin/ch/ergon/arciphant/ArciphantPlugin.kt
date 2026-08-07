package ch.ergon.arciphant

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginAware

class ArciphantPlugin : Plugin<PluginAware> {

    override fun apply(target: PluginAware) {
        when (target) {
            is Settings -> ArciphantSettingsPlugin().apply(target)
            is Project -> ArciphantProjectPlugin().apply(target)
            else -> error("Plugin ${ArciphantPlugin::class.simpleName} does not support target ${target::class.simpleName}")
        }
    }

    companion object {
        internal val logger = Logging.getLogger(ArciphantPlugin::class.java)

        internal const val EXTENSION_NAME = "arciphant"
    }
}