package ch.ergon.arciphant.dsl

open class ArciphantDsl internal constructor() {

    internal val functionalModules = mutableListOf<FunctionalModuleBuilder>()
    internal val bundleModules = mutableSetOf<BundleModuleBuilder>()

    fun template(): ModuleTemplateBuilder {
        return ModuleTemplateBuilder()
    }

    fun library(name: String, template: ModuleTemplateBuilder) = library(name, setOf(template))

    fun library(name: String, templates: Set<ModuleTemplateBuilder> = emptySet()): FunctionalModuleBuilder {
        verifyName(name, "library")
        return FunctionalModuleBuilder(name, templates, FunctionalModuleType.LIBRARY).also { functionalModules.add(it) }
    }

    fun module(name: String, template: ModuleTemplateBuilder) = module(name, setOf(template))

    fun module(name: String, templates: Set<ModuleTemplateBuilder> = emptySet()): FunctionalModuleBuilder {
        verifyName(name, "module")
        return FunctionalModuleBuilder(name, templates, FunctionalModuleType.DOMAIN).also { functionalModules.add(it) }
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
