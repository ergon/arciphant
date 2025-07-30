package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*

open class DeprecatedArciphantDsl {

    internal val modules = mutableSetOf<FunctionalModuleInstanceDsl>()
    internal val bundles = mutableSetOf<BundleModule>()

    fun stencil(configure: FunctionalModuleStencilDsl.() -> Unit = {}): Stencil {
        val stencilBuilder = FunctionalModuleStencilDsl()
        stencilBuilder.configure()
        return stencilBuilder.build()
    }

    fun library(name: String, configure: FunctionalModuleInstanceDsl.() -> Unit = {}): LibraryModuleReference {
        verifyName(name, "library module")
        return functionalModule(LibraryModuleReference(name), configure)
    }

    fun module(name: String, configure: FunctionalModuleInstanceDsl.() -> Unit = {}): DomainModuleReference {
        verifyName(name, "domain module")
        return functionalModule(DomainModuleReference(name), configure)
    }

    private fun <M : FunctionalModuleReference> functionalModule(
        reference: M,
        configure: FunctionalModuleInstanceDsl.() -> Unit = {}
    ): M {
        val module = FunctionalModuleInstanceDsl(reference)
        module.configure()
        modules.add(module)
        return reference
    }

    fun bundle(name: String, plugin: String? = null, includes: Set<ModuleReference> = emptySet()): BundleModuleReference {
        bundles.add(
            BundleModule(
                reference = BundleModuleReference(name),
                plugin = plugin?.let { Plugin(it) },
                includes = includes
            )
        )
        return BundleModuleReference(name)
    }

}


