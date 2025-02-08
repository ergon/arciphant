package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*
import ch.ergon.arciphant.util.emptyToNull

open class ArciphantDsl {

    internal val allModules = AllFunctionalModulesDsl()
    internal val allComponents = AllComponentsDsl()
    internal val modules = mutableSetOf<SingleFunctionalModuleDsl>()
    internal val bundles = mutableSetOf<BundleModuleDsl>()

    fun allModules(configure: AllFunctionalModulesDsl.() -> Unit = {}) {
        allModules.configure()
    }

    fun allComponents(configure: AllComponentsDsl.() -> Unit = {}) {
        allComponents.configure()
    }

    fun stencil(configure: StencilDsl.() -> Unit = {}): Stencil {
        val stencilBuilder = StencilDsl()
        stencilBuilder.configure()
        return stencilBuilder.build()
    }

    fun library(name: String, configure: SingleFunctionalModuleDsl.() -> Unit = {}): LibraryModuleReference {
        return module(LibraryModuleReference(name), configure)
    }

    fun module(name: String, configure: SingleFunctionalModuleDsl.() -> Unit = {}): DomainModuleReference {
        return module(DomainModuleReference(name), configure)
    }

    private fun <M : FunctionalModuleReference> module(reference: M, configure: SingleFunctionalModuleDsl.() -> Unit = {}): M {
        val module = SingleFunctionalModuleDsl(reference)
        module.configure()
        modules.add(module)
        return reference
    }

    fun bundle(name: String? = null): BundleModuleDsl {
        val bundle = BundleModuleDsl(name?.emptyToNull())
        this.bundles.add(bundle)
        return bundle
    }

}

class AllComponentsDsl internal constructor() {

    internal var plugin: Plugin? = null
        private set

    /**
     * This plugin is applied to all components that do NOT specify a specific plugin.
     * The plugin configured here should transitively apply either the Java or Kotlin plugin.
     * The reason is that the arciphant plugin requires the gradle configurations created by these JVM plugins
     * ('implementation', 'api') to apply the configured dependencies.
     */
    fun plugin(pluginId: String) {
        plugin = Plugin(pluginId)
    }

}
