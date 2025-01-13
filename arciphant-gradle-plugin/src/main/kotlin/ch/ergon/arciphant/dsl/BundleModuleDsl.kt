package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.ModuleReference
import ch.ergon.arciphant.model.Plugin

class BundleModuleDsl internal constructor(internal val name: String?) {

    internal val includes = mutableListOf<ModuleReference>()

    internal var plugin: Plugin? = null
        private set

    fun include(vararg modules: ModuleReference) {
        includes.addAll(modules)
    }

    fun withPlugin(id: String) {
        plugin = Plugin(id)
    }

}
