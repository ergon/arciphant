package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*
import ch.ergon.arciphant.model.DependencyType.API
import ch.ergon.arciphant.model.DependencyType.IMPLEMENTATION

open class ArciphantDsl internal constructor() {

    internal val functionalModules = mutableListOf<FunctionalModuleBuilder>()
    internal val bundleModules = mutableSetOf<BundleModuleBuilder>()

    fun template(basedOn: ModuleTemplateBuilder? = null): ModuleTemplateBuilder {
        return ModuleTemplateBuilder(basedOn)
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

    fun <B : ComponentContainerBuilder> B.createComponent(
        name: String,
        plugin: String? = null,
        dependsOnApi: Set<String> = emptySet(),
        dependsOn: Set<String> = emptySet(),
    ): B {
        verifyName(name, "component")
        val dependencies = mapDependencies(dependsOnApi, dependsOn)
        components.add(Component(ComponentReference(name), plugin?.let { Plugin(it) }, dependencies))
        return this
    }

    fun <B : ComponentContainerBuilder> B.extendComponent(
        name: String,
        dependsOnApi: Set<String> = emptySet(),
        dependsOn: Set<String> = emptySet(),
    ): B {
        val dependencies = mapDependencies(dependsOnApi, dependsOn)
        verify(componentDependencyOverrides.putIfAbsent(name, dependencies) == null) {
            "Component '$name' has already been extended in the current context."
        }
        return this
    }

    fun bundle(
        name: String,
        plugin: String? = null,
        includes: Set<FunctionalModuleBuilder> = emptySet()
    ): BundleModuleBuilder {
        return BundleModuleBuilder(
            name = name,
            plugin = plugin,
            includes = includes
        ).also { bundleModules.add(it) }
    }

}

private fun mapDependencies(apiDependencies: Set<String>, implementationDependencies: Set<String>) =
    apiDependencies.toDependencies(API) + implementationDependencies.toDependencies(IMPLEMENTATION)

private fun Set<String>.toDependencies(type: DependencyType) = map { Dependency(ComponentReference(it), type) }.toSet()
