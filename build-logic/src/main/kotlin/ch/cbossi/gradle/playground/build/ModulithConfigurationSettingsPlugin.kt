package ch.cbossi.gradle.playground.build

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * This plugin solely created the extension consumed by the plugin [ModulithSettingsPlugin].
 * This is necessary, since values set in an extension are not yet available in the apply-method of the plugin creating an extension.
 */
class ModulithConfigurationSettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        settings.extensions.create("modulith", ModulithExtension::class.java)
    }
}
