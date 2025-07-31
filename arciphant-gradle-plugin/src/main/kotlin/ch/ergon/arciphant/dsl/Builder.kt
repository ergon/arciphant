package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*
import ch.ergon.arciphant.model.Component
import ch.ergon.arciphant.model.Dependency

class BundleModuleBuilder internal constructor(
    internal val name: String,
    internal val plugin: String?,
    internal val includes: Set<FunctionalModuleBuilder>
) : ModuleBuilder

class DomainModuleBuilder internal constructor(name: String, structures: Set<ComponentStructureBuilder>) : FunctionalModuleBuilder(name, structures)

class LibraryModuleBuilder internal constructor(name: String, structures: Set<ComponentStructureBuilder>) : FunctionalModuleBuilder(name, structures)

sealed class FunctionalModuleBuilder(
    internal val name: String,
    internal val structures: Set<ComponentStructureBuilder>
) : ModuleBuilder, ComponentContainerBuilder()

sealed interface ModuleBuilder

class ComponentStructureBuilder internal constructor(
    internal val basedOn: ComponentStructureBuilder? = null
) : ComponentContainerBuilder()

sealed class ComponentContainerBuilder {
    internal val components = mutableListOf<Component>()
    internal val componentDependencyOverrides = mutableMapOf<String, Set<Dependency>>()
}

internal val ModuleBuilder.reference
    get() = when (this) {
        is LibraryModuleBuilder -> LibraryModuleReference(name)
        is DomainModuleBuilder -> DomainModuleReference(name)
        is BundleModuleBuilder -> BundleModuleReference(name)
    }
