package ch.ergon.arciphant.dsl

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * This plugin solely creates the extension consumed by the plugin [ch.ergon.arciphant.core.ArciphantCorePlugin].
 * This is necessary, since values set in an extension are not yet available in the apply-method of the plugin creating an extension.
 */
class ArciphantDslPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        settings.extensions.create("arciphant", ArciphantExtension::class.java)
    }
}
