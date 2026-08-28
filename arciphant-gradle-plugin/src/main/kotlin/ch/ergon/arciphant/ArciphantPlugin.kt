package ch.ergon.arciphant

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging

class ArciphantPlugin : Plugin<Settings> {

    override fun apply(context: Settings) {
        ArciphantSettingsPlugin().apply(context)
    }

    companion object {
        internal val logger = Logging.getLogger(ArciphantPlugin::class.java)
    }
}
