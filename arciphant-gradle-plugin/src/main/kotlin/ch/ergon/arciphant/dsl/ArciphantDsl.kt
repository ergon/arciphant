package ch.ergon.arciphant.dsl

open class ArciphantDsl internal constructor() {

    internal val functionalModules = mutableListOf<FunctionalModuleBuilder<*>>()
    internal val bundleModules = mutableSetOf<BundleModuleBuilder>()

    fun template(): ModuleTemplateBuilder {
        return ModuleTemplateBuilder()
    }

    fun library(name: String, template: ModuleTemplateBuilder) = library(name, setOf(template))

    fun library(name: String, templates: Set<ModuleTemplateBuilder> = emptySet()): LibraryModuleBuilder {
        verifyName(name, "library")
        return LibraryModuleBuilder(name, templates).also { functionalModules.add(it) }
    }

    fun module(name: String, template: ModuleTemplateBuilder) = module(name, setOf(template))

    fun module(name: String, templates: Set<ModuleTemplateBuilder> = emptySet()): DomainModuleBuilder {
        verifyName(name, "module")
        return DomainModuleBuilder(name, templates).also { functionalModules.add(it) }
    }

    fun bundle(
        name: String,
        plugin: String? = null,
        includes: Set<ModuleBuilder> = emptySet()
    ): BundleModuleBuilder {
        return BundleModuleBuilder(
            name = name,
            plugin = plugin,
            includes = includes
        ).also { bundleModules.add(it) }
    }

}
