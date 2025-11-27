package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.util.verifyName

open class ArciphantDsl internal constructor() {

    internal var globalBasePath: String? = null
    internal val functionalModules = mutableListOf<FunctionalModuleBuilder>()
    internal val bundleModules = mutableSetOf<BundleModuleBuilder>()

    internal val packageStructureValidation = PackageStructureValidationBuilder()

    fun basePath(basePath: String) {
        globalBasePath = basePath
    }

    fun template(): ModuleTemplateBuilder {
        return ModuleTemplateBuilder()
    }

    fun library(name: String, basePath: String? = null, template: ModuleTemplateBuilder) = library(name, basePath, setOf(template))

    fun library(name: String, basePath: String? = null, templates: Set<ModuleTemplateBuilder> = emptySet()): FunctionalModuleBuilder {
        verifyName(name, "library")
        return FunctionalModuleBuilder(name, basePath, templates, FunctionalModuleType.LIBRARY).also { functionalModules.add(it) }
    }

    fun module(name: String, basePath: String? = null, template: ModuleTemplateBuilder) = module(name, basePath, setOf(template))

    fun module(name: String, basePath: String? = null, templates: Set<ModuleTemplateBuilder> = emptySet()): FunctionalModuleBuilder {
        verifyName(name, "module")
        return FunctionalModuleBuilder(name, basePath, templates, FunctionalModuleType.DOMAIN).also { functionalModules.add(it) }
    }

    fun bundle(
        name: String,
        basePath: String? = null,
        plugin: String? = null,
        includes: Set<ModuleBuilder> = emptySet()
    ): BundleModuleBuilder {
        return BundleModuleBuilder(
            name = name,
            basePath = basePath,
            plugin = plugin,
            includes = includes
        ).also { bundleModules.add(it) }
    }

    fun packageStructureValidation(block: PackageStructureValidationDsl.() -> Unit) {
        packageStructureValidation.block()
    }

}
