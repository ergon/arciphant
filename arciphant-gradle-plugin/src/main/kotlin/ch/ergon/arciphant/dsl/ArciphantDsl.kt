package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*
import ch.ergon.arciphant.model.DependencyType.API
import ch.ergon.arciphant.model.DependencyType.IMPLEMENTATION

class ArciphantDsl internal constructor() {

    internal val functionalModules = mutableListOf<FunctionalModuleBuilder>()
    internal val bundleModules = mutableSetOf<BundleModule>()

    fun componentStructure(basedOn: ComponentStructureBuilder? = null): ComponentStructureBuilder {
        return ComponentStructureBuilder(basedOn)
    }

    fun library(name: String, structure: ComponentStructureBuilder) = library(name, listOf(structure))

    fun library(name: String, structures: List<ComponentStructureBuilder> = emptyList()): LibraryBuilder {
        return LibraryBuilder(name, structures).also { functionalModules.add(it) }
    }

    fun module(name: String, structure: ComponentStructureBuilder) = module(name, listOf(structure))

    fun module(name: String, structures: List<ComponentStructureBuilder> = emptyList()): ModuleBuilder {
        return ModuleBuilder(name, structures).also { functionalModules.add(it) }
    }

    fun <B : ComponentsBuilder> B.createComponent(
        name: String,
        plugin: String? = null,
        dependsOnApi: List<String> = emptyList(),
        dependsOn: List<String> = emptyList(),
    ): B {
        val dependencies = mergeDependencies(dependsOnApi, dependsOn)
        components.add(Component(ComponentReference(name), plugin?.let { Plugin(it) }, dependencies))
        return this
    }

    fun <B : ComponentsBuilder> B.extendComponent(
        name: String,
        dependsOnApi: List<String> = emptyList(),
        dependsOn: List<String> = emptyList(),
    ): B {
        val dependencies = mergeDependencies(dependsOnApi, dependsOn)
        verify(componentDependencyOverrides.putIfAbsent(name, dependencies) == null) {
            "Component '$name' has already been extended in the current context."
        }
        return this
    }

    fun bundle(
        name: String,
        plugin: String? = null,
        includes: Set<FunctionalModuleBuilder> = emptySet()
    ): BundleModuleReference {
        bundleModules.add(
            BundleModule(
                reference = BundleModuleReference(name),
                plugin = plugin?.let { Plugin(it) },
                includes = includes.map { it.reference }.toSet()
            )
        )
        return BundleModuleReference(name)
    }

}

class ModuleBuilder internal constructor(name: String, structures: List<ComponentStructureBuilder>) :
    FunctionalModuleBuilder(name, structures)

class LibraryBuilder internal constructor(name: String, structures: List<ComponentStructureBuilder>) :
    FunctionalModuleBuilder(name, structures)

sealed class FunctionalModuleBuilder(
    internal val name: String,
    internal val structures: List<ComponentStructureBuilder>
) :
    ComponentsBuilder()

class ComponentStructureBuilder internal constructor(internal val basedOn: ComponentStructureBuilder? = null) :
    ComponentsBuilder()

sealed class ComponentsBuilder {

    internal val components = mutableListOf<Component>()
    internal val componentDependencyOverrides = mutableMapOf<String, List<Dependency>>()
}

private fun mergeDependencies(apiDependencies: List<String>, implementationDependencies: List<String>) =
    apiDependencies.map { Dependency(ComponentReference(it), API) } +
            implementationDependencies.map { Dependency(ComponentReference(it), IMPLEMENTATION) }


internal val FunctionalModuleBuilder.reference get() = when(this) {
    is LibraryBuilder -> LibraryModuleReference(name)
    is ModuleBuilder -> DomainModuleReference(name)
}
