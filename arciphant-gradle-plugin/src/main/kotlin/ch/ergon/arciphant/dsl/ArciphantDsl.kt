package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*
import ch.ergon.arciphant.util.emptyToNull

open class ArciphantDsl {

    internal val modules = mutableSetOf<FunctionalModuleInstanceDsl>()
    internal val bundles = mutableSetOf<BundleModuleDsl>()

    fun stencil(configure: FunctionalModuleStencilDsl.() -> Unit = {}): Stencil {
        val stencilBuilder = FunctionalModuleStencilDsl()
        stencilBuilder.configure()
        return stencilBuilder.build()
    }

    fun library(name: String, configure: FunctionalModuleInstanceDsl.() -> Unit = {}): LibraryModuleReference {
        return functionalModule(LibraryModuleReference(name), configure)
    }

    fun module(name: String, configure: FunctionalModuleInstanceDsl.() -> Unit = {}): DomainModuleReference {
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

    fun bundle(name: String? = null): BundleModuleDsl {
        val bundle = BundleModuleDsl(name?.emptyToNull())
        this.bundles.add(bundle)
        return bundle
    }

}
