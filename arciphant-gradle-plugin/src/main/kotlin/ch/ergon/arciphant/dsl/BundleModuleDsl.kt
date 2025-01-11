package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.ModuleReference

class BundleModuleDsl internal constructor(internal val name: String?) {

    internal val includes = mutableListOf<ModuleReference>()

    fun include(vararg modules: ModuleReference) {
        includes.addAll(modules)
    }

}