package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.Component
import ch.ergon.arciphant.model.Dependency
import ch.ergon.arciphant.model.ModuleReference

class BundleModuleBuilder internal constructor(
    internal val name: String,
    internal val plugin: String?,
    internal val includes: Set<ModuleBuilder>
) : ModuleBuilder

class DomainModuleBuilder internal constructor(name: String, templates: Set<ModuleTemplateBuilder>) : FunctionalModuleBuilder(name, templates)

class LibraryModuleBuilder internal constructor(name: String, templates: Set<ModuleTemplateBuilder>) : FunctionalModuleBuilder(name, templates)

sealed class FunctionalModuleBuilder(
    internal val name: String,
    internal val templates: Set<ModuleTemplateBuilder>
) : ModuleBuilder, ComponentContainerBuilder()

sealed interface ModuleBuilder

class ModuleTemplateBuilder internal constructor() : ComponentContainerBuilder() {

    internal val extends = mutableListOf<ModuleTemplateBuilder>()

    fun extends(template: ModuleTemplateBuilder): ModuleTemplateBuilder {
        extends.add(template)
        return this
    }

}

sealed class ComponentContainerBuilder {
    internal val components = mutableListOf<Component>()
    internal val componentDependencyOverrides = mutableMapOf<String, Set<Dependency>>()
}

internal val ModuleBuilder.reference
    get() = when (this) {
        is LibraryModuleBuilder -> ModuleReference(name)
        is DomainModuleBuilder -> ModuleReference(name)
        is BundleModuleBuilder -> ModuleReference(name)
    }
