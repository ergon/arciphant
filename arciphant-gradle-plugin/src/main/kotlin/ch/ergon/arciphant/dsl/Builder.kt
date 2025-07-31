package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.Component
import ch.ergon.arciphant.model.Dependency
import ch.ergon.arciphant.model.DomainModuleReference
import ch.ergon.arciphant.model.LibraryModuleReference

class ModuleBuilder internal constructor(name: String, structures: Set<ComponentStructureBuilder>) : FunctionalModuleBuilder(name, structures)

class LibraryBuilder internal constructor(name: String, structures: Set<ComponentStructureBuilder>) : FunctionalModuleBuilder(name, structures)

sealed class FunctionalModuleBuilder(internal val name: String, internal val structures: Set<ComponentStructureBuilder>) : ComponentContainerBuilder()

class ComponentStructureBuilder internal constructor(internal val basedOn: ComponentStructureBuilder? = null) : ComponentContainerBuilder()

sealed class ComponentContainerBuilder {
    internal val components = mutableListOf<Component>()
    internal val componentDependencyOverrides = mutableMapOf<String, Set<Dependency>>()
}

internal val FunctionalModuleBuilder.reference
    get() = when (this) {
        is LibraryBuilder -> LibraryModuleReference(name)
        is ModuleBuilder -> DomainModuleReference(name)
    }
