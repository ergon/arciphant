package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*
import ch.ergon.arciphant.model.DependencyType.API
import ch.ergon.arciphant.model.DependencyType.IMPLEMENTATION

open class ArciphantDsl internal constructor() {

    internal val functionalModules = mutableListOf<FunctionalModuleBuilder>()
    internal val bundleModules = mutableSetOf<BundleModule>()

    fun componentStructure(basedOn: ComponentStructureBuilder? = null): ComponentStructureBuilder {
        return ComponentStructureBuilder(basedOn)
    }

    fun library(name: String, structure: ComponentStructureBuilder) = library(name, setOf(structure))

    fun library(name: String, structures: Set<ComponentStructureBuilder> = emptySet()): LibraryBuilder {
        verifyName(name, "library")
        return LibraryBuilder(name, structures).also { functionalModules.add(it) }
    }

    fun module(name: String, structure: ComponentStructureBuilder) = module(name, setOf(structure))

    fun module(name: String, structures: Set<ComponentStructureBuilder> = emptySet()): ModuleBuilder {
        verifyName(name, "module")
        return ModuleBuilder(name, structures).also { functionalModules.add(it) }
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

private fun mapDependencies(apiDependencies: Set<String>, implementationDependencies: Set<String>) =
    apiDependencies.toDependencies(API) + implementationDependencies.toDependencies(IMPLEMENTATION)

private fun Set<String>.toDependencies(type: DependencyType) = map { Dependency(ComponentReference(it), type) }.toSet()
