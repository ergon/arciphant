package ch.ergon.arciphant

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginAware

class ArciphantPlugin : Plugin<PluginAware> {

    override fun apply(context: PluginAware) {
        when (context) {
            is Settings -> ArciphantSettingsPlugin().apply(context)
            is Project -> ArciphantProjectPlugin().apply(context)
            else -> error("Cannot apply ${ArciphantPlugin::class.simpleName} to ${context::class.simpleName}")
        }
    }

    companion object {
        internal val logger = Logging.getLogger(ArciphantPlugin::class.java)
    }
}
