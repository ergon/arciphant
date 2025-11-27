package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.model.*
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.Dependency
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.Plugin
import ch.ergon.arciphant.util.verify
import ch.ergon.arciphant.util.verifyName

class BundleModuleBuilder internal constructor(
    internal val name: String,
    internal val plugin: String?,
    internal val includes: Set<ModuleBuilder>
) : ModuleBuilder

internal enum class FunctionalModuleType {
    LIBRARY, DOMAIN
}

class FunctionalModuleBuilder internal constructor(
    internal val name: String,
    internal val templates: Set<ModuleTemplateBuilder>,
    internal val moduleType: FunctionalModuleType,
) : ModuleBuilder {
    internal val componentsBuilder = ComponentsBuilder()

    fun createComponent(
        name: String,
        plugin: String? = null,
        dependsOnApi: Set<String> = emptySet(),
        dependsOn: Set<String> = emptySet(),
    ): FunctionalModuleBuilder {
        componentsBuilder.doCreateComponent(name = name, plugin = plugin, dependsOnApi = dependsOnApi, dependsOn = dependsOn)
        return this
    }

    fun extendComponent(
        name: String,
        dependsOnApi: Set<String> = emptySet(),
        dependsOn: Set<String> = emptySet(),
    ): FunctionalModuleBuilder {
        componentsBuilder.doExtendComponent(name = name, dependsOnApi = dependsOnApi, dependsOn = dependsOn)
        return this
    }

}

class ModuleTemplateBuilder internal constructor() {
    internal val componentsBuilder = ComponentsBuilder()

    internal val extends = mutableListOf<ModuleTemplateBuilder>()

    fun extends(template: ModuleTemplateBuilder): ModuleTemplateBuilder {
        extends.add(template)
        return this
    }

    fun createComponent(
        name: String,
        plugin: String? = null,
        dependsOnApi: Set<String> = emptySet(),
        dependsOn: Set<String> = emptySet(),
    ): ModuleTemplateBuilder {
        componentsBuilder.doCreateComponent(name = name, plugin = plugin, dependsOnApi = dependsOnApi, dependsOn = dependsOn)
        return this
    }

    fun extendComponent(
        name: String,
        dependsOnApi: Set<String> = emptySet(),
        dependsOn: Set<String> = emptySet(),
    ): ModuleTemplateBuilder {
        componentsBuilder.doExtendComponent(name = name, dependsOnApi = dependsOnApi, dependsOn = dependsOn)
        return this
    }
}

internal class ComponentsBuilder {
    internal val components = mutableListOf<Component>()
    internal val componentDependencyOverrides = mutableMapOf<String, Set<Dependency>>()

    fun doCreateComponent(name: String, plugin: String?, dependsOnApi: Set<String>, dependsOn: Set<String>) {
        verifyName(name, "component")
        val dependencies = mapDependencies(dependsOnApi, dependsOn)
        components.add(Component(ComponentReference(name), plugin?.let { Plugin(it) }, dependencies))
    }

    fun doExtendComponent(name: String, dependsOnApi: Set<String>, dependsOn: Set<String>) {
        val dependencies = mapDependencies(dependsOnApi, dependsOn)
        verify(componentDependencyOverrides.putIfAbsent(name, dependencies) == null) {
            "Component '$name' has already been extended in the current context."
        }
    }

    private fun mapDependencies(apiDependencies: Set<String>, implementationDependencies: Set<String>) =
        apiDependencies.toDependencies(API) + implementationDependencies.toDependencies(IMPLEMENTATION)

    private fun Set<String>.toDependencies(type: DependencyType) = map { Dependency(ComponentReference(it), type) }.toSet()
}

sealed interface ModuleBuilder

/**
 * This is only necessary since 'reference' should be internal, and we cannot use internal values on [ModuleBuilder], since it is an internface.
 */
internal val ModuleBuilder.reference
    get() = when (this) {
        is FunctionalModuleBuilder -> ModuleReference(name)
        is BundleModuleBuilder -> ModuleReference(name)
    }
